package eppic;

import eppic.assembly.Assembly;
import eppic.assembly.CrystalAssemblies;
import eppic.model.db.*;
import eppic.model.dto.Interface;
import eppic.model.dto.InterfaceCluster;
import eppic.model.dto.PdbInfo;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.EntityFinder;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;

public class TestAssemblyStructure {

    private static final Logger logger = LoggerFactory.getLogger(TestAssemblyStructure.class);

    private static final String TMPDIR = System.getProperty("java.io.tmpdir");
    private static final File outDir = new File(TMPDIR, "eppicTestAssemblyStructure");

    /**
     * Tests that applying operators extracted from {@link Assembly#getStructurePacked()} produces
     * the same structures as output of {@link Assembly#writeToMmCifFile(File)}
     * @throws IOException
     */
    @Test
    public void testAssemblyStructureLayout() throws IOException{

        testAssemblyLayoutForPdbId("4ht5");
        testAssemblyLayoutForPdbId("5wjc");

    }

    private void testAssemblyLayoutForPdbId(String pdbId) throws IOException {

        outDir.mkdir();
        outDir.deleteOnExit();
        //assertTrue(couldMakeDir);

        assertTrue(outDir.isDirectory());

        EppicParams params = Utils.generateEppicParams(pdbId, outDir);

        Main m = new Main();

        m.run(params);
        Structure auStruct = m.getStructure();

        CrystalAssemblies validAssemblies = m.getCrystalAssemblies();
        PdbInfoDB pdbInfoDB = m.getDataModelAdaptor().getPdbInfo();

        Map<Integer, Structure> structFromFiles = writeAssemblyStructFiles(validAssemblies, pdbId, outDir);

        for (AssemblyDB assemblyDB : pdbInfoDB.getValidAssemblies()) {
            Structure assemblyStruct = getAssemblyStructFromDbOps(assemblyDB, auStruct);
            // writing out the file (needed only for debugging)
            if (logger.isDebugEnabled()) {
                File fileFromOps = new File(outDir, pdbId + ".assembly_from_ops." + assemblyDB.getId() + ".cif.gz");
                fileFromOps.deleteOnExit();
                PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(fileFromOps)));
                ps.println(assemblyStruct.toMMCIF());
                ps.close();
            }

            Structure structFromFile = structFromFiles.get(assemblyDB.getId());

            assertEquals(structFromFile.getPolyChains().size(), assemblyStruct.getPolyChains().size());

            Atom[] atomsFromAssemblyStruct = getAllAtomArray(assemblyStruct);
            Atom[] atomsFromFile = getAllAtomArray(structFromFile);

            assertEquals(atomsFromFile.length, atomsFromAssemblyStruct.length);

            // the rmsd should be 0 without need to superpose, the 2 structures must be exactly in same location
            double rmsd = Calc.rmsd(atomsFromAssemblyStruct, atomsFromFile);
            String msg = pdbId + ", assembly id "+assemblyDB.getId()+": rmsd between struct from file and from operators should be 0";
            //System.out.println(msg);
            assertEquals(msg, 0, rmsd, 0.001);
        }

        // finally we test that the unit cell assembly has consistent operators
        AssemblyDB assemblyDB = null;
        for (AssemblyDB adb:pdbInfoDB.getAssemblies()) {
            if (adb.getId()==0) assemblyDB = adb;
        }
        assertNotNull(assemblyDB);

        // the reference node: 1st opId type seen
        Map<Integer, GraphNodeDB> refNodeDBs = new HashMap<>();

        for (GraphNodeDB nodeDB : assemblyDB.getGraphNodes()) {
            // all same op ids should have same operators
            int opId = Integer.parseInt(nodeDB.getLabel().split("_")[1]);
            if (!refNodeDBs.containsKey(opId)) {
                refNodeDBs.put(opId, nodeDB);
            } else {
                GraphNodeDB refNodeDB = refNodeDBs.get(opId);
                assertEquals(refNodeDB.getRxx(), nodeDB.getRxx(), 0.0001);
                assertEquals(refNodeDB.getRxy(), nodeDB.getRxy(), 0.0001);
                assertEquals(refNodeDB.getRxz(), nodeDB.getRxz(), 0.0001);

                assertEquals(refNodeDB.getRyx(), nodeDB.getRyx(), 0.0001);
                assertEquals(refNodeDB.getRyy(), nodeDB.getRyy(), 0.0001);
                assertEquals(refNodeDB.getRyz(), nodeDB.getRyz(), 0.0001);

                assertEquals(refNodeDB.getRzx(), nodeDB.getRzx(), 0.0001);
                assertEquals(refNodeDB.getRzy(), nodeDB.getRzy(), 0.0001);
                assertEquals(refNodeDB.getRzz(), nodeDB.getRzz(), 0.0001);

                assertEquals(refNodeDB.getTx(), nodeDB.getTx(), 0.0001);
                assertEquals(refNodeDB.getTy(), nodeDB.getTy(), 0.0001);
                assertEquals(refNodeDB.getTz(), nodeDB.getTz(), 0.0001);

            }

        }

    }

    private Structure getAssemblyStructFromDbOps(AssemblyDB assemblyDB, Structure s) {
        Structure assemblyStruct = new StructureImpl();
        for (GraphNodeDB nodeDB : assemblyDB.getGraphNodes()) {
            if (nodeDB.isIn3dStructure()) {
                Matrix4d op = new Matrix4d();
                op.m00 = nodeDB.getRxx();
                op.m01 = nodeDB.getRxy();
                op.m02 = nodeDB.getRxz();

                op.m10 = nodeDB.getRyx();
                op.m11 = nodeDB.getRyy();
                op.m12 = nodeDB.getRyz();

                op.m20 = nodeDB.getRzx();
                op.m21 = nodeDB.getRzy();
                op.m22 = nodeDB.getRzz();

                op.m03 = nodeDB.getTx();
                op.m13 = nodeDB.getTy();
                op.m23 = nodeDB.getTz();
                op.m33 = 1;

                String[] labelTokens = nodeDB.getLabel().split("_");
                String chainId = labelTokens[0];
                int opId = Integer.parseInt(labelTokens[1]);
                Chain c = (Chain)s.getPolyChainByPDB(chainId).clone();
                c.setName(chainId+"_"+opId);
                c.setId(chainId+"_"+opId);
                Calc.transform(c, op);
                assemblyStruct.addChain(c);

                System.out.println("Operator for " + s.getPDBCode() +", assembly id "+assemblyDB.getId()+", chain id "+chainId+":");
                System.out.println(op);

            }

        }
        List<List<Chain>> allModels = new ArrayList<>();
        allModels.add(assemblyStruct.getPolyChains());
        List<EntityInfo> entityInfos = EntityFinder.findPolyEntities(allModels);

        assemblyStruct.setEntityInfos(entityInfos);
        return assemblyStruct;
    }

    /**
     * A custom getAllAtomArray so that we always return the atoms in chainId alphabetical order,
     * so that the rmsd calculation makes sense.
     * @param s
     * @return
     */
    private static Atom[] getAllAtomArray(Structure s) {
        List<Atom> atoms = new ArrayList<>();

        List<String> sortedChainIds = new ArrayList<>();
        for (Chain c : s.getPolyChains()) {
            sortedChainIds.add(c.getName());
        }
        sortedChainIds = sortedChainIds.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());


        for (String chainId : sortedChainIds) {
            for (Group g : s.getPolyChainByPDB(chainId).getAtomGroups()) {
                atoms.addAll(g.getAtoms());
            }

        }
        return atoms.toArray(new Atom[atoms.size()]);
    }

    /**
     * Write structure files for all given assemblies to outDir, using pdbId to name files.
     * @param validAssemblies the assemblies
     * @param pdbId the pdb id
     * @param outDir the output dir
     * @return a map of assembly ids to parsed (from output files) structures
     * @throws IOException
     */
    private Map<Integer, Structure> writeAssemblyStructFiles(CrystalAssemblies validAssemblies, String pdbId, File outDir) throws IOException {
        Map<Integer, Structure> structFromFiles = new HashMap<>();
        for (Assembly a:validAssemblies) {
            File structFile = new File(outDir, pdbId+".assembly." +a.getId()+".cif.gz");
            structFile.deleteOnExit();
            a.writeToMmCifFile(structFile);

            // parse file
            SimpleMMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
            parser.addMMcifConsumer(consumer);
            BufferedReader buf = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(structFile))));
            parser.parse(buf);
            buf.close();
            Structure s = consumer.getStructure();

            structFromFiles.put(a.getId(), s);
        }
        return structFromFiles;
    }

    private Map<Integer, Structure> writeInterfaceStructFiles(StructureInterfaceList interfaces, String pdbId, File outDir) throws IOException {
        Map<Integer, Structure> structFromFiles = new HashMap<>();
        for (StructureInterface interf : interfaces) {
            File structFile = new File(outDir, pdbId+".interface." +interf.getId()+".cif.gz");
            structFile.deleteOnExit();
            PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(structFile)));
            ps.print(interf.toMMCIF());
            ps.close();

            // parse file
            SimpleMMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
            parser.addMMcifConsumer(consumer);
            BufferedReader buf = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(structFile))));
            parser.parse(buf);
            buf.close();
            Structure s = consumer.getStructure();

            structFromFiles.put(interf.getId(), s);
        }
        return structFromFiles;
    }

    /**
     * Tests that {@link CoordFilesAdaptor#getAssemblyCoordsMmcif(String, File, OutputStream, PdbInfo, eppic.model.dto.Assembly, boolean)} produces
     * the same structures as output of {@link Assembly#writeToMmCifFile(File)}
     * @throws IOException
     */
    @Test
    public void testAssembliesCoordFilesAdaptor() throws IOException{

        testAssembliesCoordFilesAdaptorForPdbId("4ht5");
        testAssembliesCoordFilesAdaptorForPdbId("5wjc");
        testAssembliesCoordFilesAdaptorForPdbId("4hhb");

    }

    private void testAssembliesCoordFilesAdaptorForPdbId(String pdbId) throws IOException {

        outDir.mkdir();
        outDir.deleteOnExit();
        //assertTrue(couldMakeDir);

        assertTrue(outDir.isDirectory());

        EppicParams params = Utils.generateEppicParams(pdbId, outDir);

        Main m = new Main();

        m.run(params);
        Structure auStruct = m.getStructure();

        CrystalAssemblies validAssemblies = m.getCrystalAssemblies();
        PdbInfo pdbInfoDB = PdbInfo.create(m.getDataModelAdaptor().getPdbInfo());

        Map<Integer, Structure> structFromFiles = writeAssemblyStructFiles(validAssemblies, pdbId, outDir);

        CoordFilesAdaptor coordFilesAdaptor = new CoordFilesAdaptor();

        for (eppic.model.dto.Assembly assemblyDB : pdbInfoDB.getValidAssemblies()) {

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            coordFilesAdaptor.getAssemblyCoordsMmcif(null, auStruct, os, pdbInfoDB, assemblyDB, false);

            if (logger.isDebugEnabled()) {
                // for debugging: we write file out
                File file = new File(outDir, pdbId + ".assemblyFromAdaptor." + assemblyDB.getId() + ".cif");
                file.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(os.toByteArray());
                fos.close();
            }

            SimpleMMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
            parser.addMMcifConsumer(consumer);
            BufferedReader buf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray())));
            parser.parse(buf);
            buf.close();

            Structure structFromAdaptor = consumer.getStructure();
            Structure structFromFile = structFromFiles.get(assemblyDB.getId());

            assertEquals(structFromFile.getPolyChains().size(), structFromAdaptor.getPolyChains().size());

            List<Chain> polyChainsFromFile = new ArrayList<>(structFromFile.getPolyChains());
            List<Chain> polyChainsFromAdaptor = new ArrayList<>(structFromAdaptor.getPolyChains());
            sortById(polyChainsFromFile);
            sortById(polyChainsFromAdaptor);

            for (int i = 0; i<polyChainsFromFile.size(); i++) {
                assertEquals(polyChainsFromFile.get(i).getId(), polyChainsFromAdaptor.get(i).getId());
                assertEquals(polyChainsFromFile.get(i).getName(), polyChainsFromAdaptor.get(i).getName());
            }

            // in Assembly#writeToMmCifFile(File) we don't do non-polys since move to biojava 5, thus we
            // can't test for that here unless we fix Assembly#writeToMmCifFile(File)
//            assertEquals(structFromFile.getNonPolyChains().size(), structFromAdaptor.getNonPolyChains().size());
//            List<Chain> nonPolyChainsFromFile = new ArrayList<>(structFromFile.getNonPolyChains());
//            List<Chain> nonPolyChainsFromAdaptor = new ArrayList<>(structFromAdaptor.getNonPolyChains());
//            sortById(nonPolyChainsFromFile);
//            sortById(nonPolyChainsFromAdaptor);
//
//            for (int i = 0; i<nonPolyChainsFromFile.size(); i++) {
//                assertEquals(nonPolyChainsFromFile.get(i).getId(), nonPolyChainsFromAdaptor.get(i).getId());
//                assertEquals(nonPolyChainsFromFile.get(i).getName(), nonPolyChainsFromAdaptor.get(i).getName());
//            }

            Atom[] atomsFromAdaptor = getAllAtomArray(structFromAdaptor);
            Atom[] atomsFromFile = getAllAtomArray(structFromFile);

            assertEquals(atomsFromFile.length, atomsFromAdaptor.length);

            // the rmsd should be 0 without need to superpose, the 2 structures must be exactly in same location
            double rmsd = Calc.rmsd(atomsFromAdaptor, atomsFromFile);
            String msg = pdbId + ", assembly id "+assemblyDB.getId()+": rmsd between struct from file and from coords adaptor should be 0";
            //System.out.println(msg);
            assertEquals(msg, 0, rmsd, 0.001);
        }

    }

    private void sortById(List<Chain> chains) {
        chains.sort(Comparator.comparing(Chain::getId));
    }

    /**
     * Tests that {@link CoordFilesAdaptor#getAssemblyCoordsMmcif(String, File, OutputStream, PdbInfo, eppic.model.dto.Assembly, boolean)} produces
     * the same structures as output of {@link Assembly#writeToMmCifFile(File)}
     * @throws IOException
     */
    @Test
    public void testInterfacesCoordFilesAdaptor() throws IOException{

        testInterfacesCoordFilesAdaptorForPdbId("4ht5");
        testInterfacesCoordFilesAdaptorForPdbId("5wjc");
        testInterfacesCoordFilesAdaptorForPdbId("4hhb");

    }

    private void testInterfacesCoordFilesAdaptorForPdbId(String pdbId) throws IOException {

        outDir.mkdir();
        outDir.deleteOnExit();
        //assertTrue(couldMakeDir);

        assertTrue(outDir.isDirectory());

        EppicParams params = Utils.generateEppicParams(pdbId, outDir);

        Main m = new Main();

        m.run(params);
        Structure auStruct = m.getStructure();

        StructureInterfaceList interfaces = m.getInterfaces();
        PdbInfo pdbInfoDB = PdbInfo.create(m.getDataModelAdaptor().getPdbInfo());

        Map<Integer, Structure> structFromFiles = writeInterfaceStructFiles(interfaces, pdbId, outDir);

        CoordFilesAdaptor coordFilesAdaptor = new CoordFilesAdaptor();

        for (InterfaceCluster interfaceClusterDB : pdbInfoDB.getInterfaceClusters()) {
            for (Interface interf : interfaceClusterDB.getInterfaces()) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();

                coordFilesAdaptor.getInterfaceCoordsMmcif(null, auStruct, os, pdbInfoDB, interf, false);

                //if (logger.isDebugEnabled()) {
                    // for debugging: we write file out
                    File file = new File(outDir, pdbId + ".interfaceFromAdaptor." + interf.getInterfaceId() + ".cif");
                    file.deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(os.toByteArray());
                    fos.close();
                //}

                SimpleMMcifParser parser = new SimpleMMcifParser();
                SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
                parser.addMMcifConsumer(consumer);
                BufferedReader buf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray())));
                parser.parse(buf);
                buf.close();

                Structure structFromAdaptor = consumer.getStructure();
                Structure structFromFile = structFromFiles.get(interf.getInterfaceId());

                assertEquals(2, structFromFile.getPolyChains().size());
                assertEquals(structFromFile.getPolyChains().size(), structFromAdaptor.getPolyChains().size());

                assertEquals(structFromFile.getPolyChains().get(0).getId(), structFromAdaptor.getPolyChains().get(0).getId());
                assertEquals(structFromFile.getPolyChains().get(0).getName(), structFromAdaptor.getPolyChains().get(0).getName());
                assertEquals(structFromFile.getPolyChains().get(1).getId(), structFromAdaptor.getPolyChains().get(1).getId());
                assertEquals(structFromFile.getPolyChains().get(1).getName(), structFromAdaptor.getPolyChains().get(1).getName());

                Atom[] atomsFromAdaptor = getAllAtomArray(structFromAdaptor);
                Atom[] atomsFromFile = getAllAtomArray(structFromFile);

                assertEquals(atomsFromFile.length, atomsFromAdaptor.length);

                // the rmsd should be 0 without need to superpose, the 2 structures must be exactly in same location
                double rmsd = Calc.rmsd(atomsFromAdaptor, atomsFromFile);
                String msg = pdbId + ", interface id "+interf.getInterfaceId()+": rmsd between struct from file and from coords adaptor should be 0";
                //System.out.println(msg);
                assertEquals(msg, 0, rmsd, 0.001);
            }

        }
    }

}
