package eppic;

import eppic.assembly.Assembly;
import eppic.assembly.CrystalAssemblies;
import eppic.model.db.AssemblyDB;
import eppic.model.db.GraphNodeDB;
import eppic.model.db.PdbInfoDB;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.geometry.SuperPosition;
import org.biojava.nbio.structure.geometry.SuperPositionQCP;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.junit.Test;

import javax.vecmath.Matrix4d;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;

public class TestAssemblyStructure {

    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    @Test
    public void testStructureLayout() throws IOException{

        testPdbId("1smt");
        testPdbId("5wjc");

    }

    private void testPdbId(String pdbId) throws IOException {
        File outDir = new File(TMPDIR, "eppicTestAssemblyStructure");

        boolean couldMakeDir = outDir.mkdir();
        outDir.deleteOnExit();
        //assertTrue(couldMakeDir);

        assertTrue(outDir.isDirectory());

        EppicParams params = Utils.generateEppicParams(pdbId, outDir);

        Main m = new Main();

        m.run(params);
        Structure auStruct = m.getStructure();

        CrystalAssemblies validAssemblies = m.getCrystalAssemblies();
        PdbInfoDB pdbInfoDB = m.getDataModelAdaptor().getPdbInfo();

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

        for (AssemblyDB assemblyDB : pdbInfoDB.getValidAssemblies()) {
            Structure assemblyStruct = getAssemblyStructFromDbOps(assemblyDB, auStruct);
            // writing out the file (needed only for debugging)
            File fileFromOps = new File(outDir, pdbId+".assembly_from_ops."+assemblyDB.getId()+".cif.gz");
            fileFromOps.deleteOnExit();
            PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(fileFromOps)));
            ps.println(assemblyStruct.toMMCIF());
            ps.close();

            Structure structFromFile = structFromFiles.get(assemblyDB.getId());

            Atom[] atomsFromAssemblyStruct = getAllAtomArray(assemblyStruct);
            Atom[] atomsFromFile = getAllAtomArray(structFromFile);

            SuperPosition superPosition = new SuperPositionQCP(false);
            double rmsd = superPosition.getRmsd(Calc.atomsToPoints(atomsFromAssemblyStruct), Calc.atomsToPoints(atomsFromFile));
            String msg = pdbId + ", assembly id "+assemblyDB.getId()+": rmsd between struct from file and from operators should be 0";
            //System.out.println(msg);
            assertEquals(msg, 0, rmsd, 0.001);
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
                //int opId = Integer.parseInt(labelTokens[1]);
                Chain c = (Chain)s.getPolyChainByPDB(chainId).clone();
                Calc.transform(c, op);
                assemblyStruct.addChain(c);

                System.out.println("Operator for " + s.getPDBCode() +", assembly id "+assemblyDB.getId()+", chain id "+chainId+":");
                System.out.println(op);

            }
        }

        return assemblyStruct;
    }

    /**
     * A custom getAllAtomArray so that we always return the atoms in chainId alphabetical order,
     * so that the rmsd calculation makes sense.
     * @param s
     * @return
     */
    public static Atom[] getAllAtomArray(Structure s) {
        List<Atom> atoms = new ArrayList<Atom>();

        Set<String> sortecChainIds = new TreeSet<>();
        for (Chain c : s.getPolyChains()) {
            sortecChainIds.add(c.getName());
        }

        for (String chainId : sortecChainIds) {
            for (Group g : s.getPolyChainByPDB(chainId).getAtomGroups()) {
                atoms.addAll(g.getAtoms());
            }

        }
        return atoms.toArray(new Atom[atoms.size()]);
    }
}
