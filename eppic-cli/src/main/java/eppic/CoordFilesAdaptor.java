package eppic;

import eppic.commons.util.FileTypeGuesser;
import eppic.model.db.*;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.MMCIFFileTools;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.io.mmcif.model.AtomSite;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptor of db data models to produce coordinate mmcif files for
 * assemblies and interfaces.
 *
 * @author Jose Duarte
 * @since 3.1.0
 */
public class CoordFilesAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(CoordFilesAdaptor.class);

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param auFile the input file with a PDB structure (AU) in mmCIF/PDB format
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param assemblyDB the db model data with assembly and residue info data
     * @throws IOException
     */
    public void getAssemblyCoordsMmcif(File auFile, OutputStream os, PdbInfoDB pdbInfoDB, AssemblyDB assemblyDB) throws IOException {
        Structure s = readCoords(auFile);
        getAssemblyCoordsMmcif(s, os, pdbInfoDB, assemblyDB);
    }

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param s          the input structure with a PDB structure (AU)
     * @param os         the output stream with the assembly in mmCIF format
     * @param pdbInfoDB  the pdb data with chain clusters
     * @param assemblyDB the db model data with assembly and residue info data
     * @throws IOException
     */
    public void getAssemblyCoordsMmcif(Structure s, OutputStream os, PdbInfoDB pdbInfoDB, AssemblyDB assemblyDB) throws IOException {

        List<AtomSite> atomSiteList = new ArrayList<>();

        for (GraphNodeDB node : assemblyDB.getGraphNodes()) {

            if (!node.isIn3dStructure())
                continue;

            String chainName = node.getLabel().split("_")[0];
            String opId = node.getLabel().split("_")[1];

            Chain c = (Chain) s.getPolyChainByPDB(chainName).clone();
            List<Chain> nonPolyChains = new ArrayList<>();
            for (Chain nonPolyChain: s.getNonPolyChainsByPDB(chainName)) {
                nonPolyChains.add((Chain) nonPolyChain.clone());
            }

            addEvolutionaryScores(c, pdbInfoDB.getChainCluster(c.getEntityInfo().getRepresentative().getName()));

            Matrix4d op = new Matrix4d();
            op.m00 = node.getRxx();
            op.m01 = node.getRxy();
            op.m02 = node.getRxz();

            op.m10 = node.getRyx();
            op.m11 = node.getRyy();
            op.m12 = node.getRyz();

            op.m20 = node.getRzx();
            op.m21 = node.getRzy();
            op.m22 = node.getRzz();

            op.m03 = node.getTx();
            op.m13 = node.getTy();
            op.m23 = node.getTz();

            op.m30 = 0;
            op.m31 = 0;
            op.m32 = 0;
            op.m33 = 1;

            Calc.transform(c, op);

            nonPolyChains.forEach(nonPolyChain -> Calc.transform(nonPolyChain, op));

            addAtomSites(c, atomSiteList, opId);
            nonPolyChains.forEach(nonPolyChain -> addAtomSites(nonPolyChain, atomSiteList, opId));

        }

        // TODO check what's the right charset to use

        // writing header
        String jobId = pdbInfoDB.getJob()==null?"unknown":pdbInfoDB.getJob().getJobId();
        os.write((SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_jobId_" + jobId + "_assemblyId_" + assemblyDB.getId()+ "\n").getBytes());
        os.write(FileConvert.getAtomSiteHeader().getBytes());

        // writing content
        os.write(MMCIFFileTools.toMMCIF(atomSiteList, AtomSite.class).getBytes());
    }

    /**
     *
     * @param auFile the input file with a PDB structure (AU) in mmCIF/PDB format
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceDB the interface data
     * @throws IOException
     */
    public void getInterfaceCoordsMmcif(File auFile, OutputStream os, PdbInfoDB pdbInfoDB, InterfaceDB interfaceDB) throws IOException {
        Structure s = readCoords(auFile);
        getInterfaceCoordsMmcif(s, os, pdbInfoDB, interfaceDB);
    }

    /**
     *
     * @param s the input structure with a PDB structure (AU)
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceDB the interface data
     * @throws IOException
     */
    public void getInterfaceCoordsMmcif(Structure s, OutputStream os, PdbInfoDB pdbInfoDB, InterfaceDB interfaceDB) throws IOException {

        List<AtomSite> atomSiteList = new ArrayList<>();

        String chainName1 = interfaceDB.getChain1();
        String chainName2 = interfaceDB.getChain2();

        Chain c1 = s.getPolyChainByPDB(chainName1);
        Chain c2 = (Chain) s.getPolyChainByPDB(chainName2).clone();
        List<Chain> nonPolyChains1 = s.getNonPolyChainsByPDB(chainName1);
        List<Chain> nonPolyChains2 = new ArrayList<>();
        for (Chain nonPolyChain : s.getNonPolyChainsByPDB(chainName2)) {
            nonPolyChains2.add((Chain)nonPolyChain.clone());
        }

        addEvolutionaryScores(c1, pdbInfoDB.getChainCluster(c1.getEntityInfo().getRepresentative().getName()));
        addEvolutionaryScores(c2, pdbInfoDB.getChainCluster(c2.getEntityInfo().getRepresentative().getName()));

        Matrix4d tranform = SpaceGroup.getMatrixFromAlgebraic(interfaceDB.getOperator());
        Calc.transform(c2, tranform);
        nonPolyChains2.forEach(nonPolyChain -> Calc.transform(nonPolyChain, tranform));

        addAtomSites(c1, atomSiteList, "0");
        // TODO we need to assign the correct opId here instead of 1
        addAtomSites(c2, atomSiteList, "1");
        nonPolyChains1.forEach(nonPolyChain -> addAtomSites(nonPolyChain, atomSiteList, "0"));
        // TODO we need to assign the correct opId here instead of 1
        nonPolyChains2.forEach(nonPolyChain -> addAtomSites(nonPolyChain, atomSiteList, "1"));

        // TODO check what's the right charset to use
        // writing header
        String jobId = pdbInfoDB.getJob()==null?"unknown":pdbInfoDB.getJob().getJobId();
        os.write((SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_jobId_" + jobId + "_interfaceId_" + interfaceDB.getInterfaceId()+ "\n" ).getBytes());
        os.write(FileConvert.getAtomSiteHeader().getBytes());
        // writing content
        os.write(MMCIFFileTools.toMMCIF(atomSiteList, AtomSite.class).getBytes());

    }

    private Structure readCoords(File auFile) throws IOException {

        long start = System.currentTimeMillis();

        Structure structure;

        int fileType = FileTypeGuesser.guessFileType(auFile);

        FileParsingParameters fileParsingParams = new FileParsingParameters();
        fileParsingParams.setAlignSeqRes(true);

        if (fileType==FileTypeGuesser.CIF_FILE) {

            MMcifParser parser = new SimpleMMcifParser();
            SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

            consumer.setFileParsingParameters(fileParsingParams);

            parser.addMMcifConsumer(consumer);

            parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(auFile))));

            structure = consumer.getStructure();
        } else if (fileType == FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {
            PDBFileParser parser = new PDBFileParser();

            parser.setFileParsingParameters(fileParsingParams);

            structure = parser.parsePDBFile(new FileInputStream(auFile));

        } else {
            // TODO support mmtf, add it to file type guesser
            throw new IOException("AU coordinate file "+auFile.toString()+" does not seem to be in one of the supported formats");
        }

        long end = System.currentTimeMillis();

        logger.info("Time needed to parse file {}: {} ms", auFile.toString(), end-start);

        return structure;
    }

    private void addEvolutionaryScores(Chain c, ChainClusterDB chainClusterDB) {

        for (Group g : c.getAtomGroups()) {
            int resSerial = c.getEntityInfo().getAlignedResIndex(g, c);
            ResidueInfoDB res = chainClusterDB.getResidue(resSerial);
            final double entropy;
            if (res != null) {
                entropy = res.getEntropyScore();
            } else {
                entropy = 0;
                logger.warn("Residue info could not be found from data in db: res serial {}, chain asym id {}. Setting entropy to 0", resSerial, c.getId());
            }
            g.getAtoms().forEach(a -> a.setTempFactor((float) entropy));

        }
    }

    private void addAtomSites(Chain c, List<AtomSite> atomSites, String opId) {
        // TODO set atom ids to be unique throughout (problem for sym mates)
        for (Group g : c.getAtomGroups()) {
            for (Atom a : g.getAtoms()) {
                atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, c.getName() + "_" +opId, c.getId() + "_" + opId));
            }
            // TODO how about alt locs?
        }
    }
 }
