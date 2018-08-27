package eppic;

import eppic.commons.sequence.AAAlphabet;
import eppic.commons.util.FileTypeGuesser;
import eppic.model.dto.*;
import org.biojava.nbio.core.sequence.io.util.IOUtils;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.MMCIFFileTools;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.io.mmcif.model.AtomSite;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.IOUtil;

import javax.vecmath.Matrix4d;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Regex to capture the actual chainName out of a chain name for an entry with NCS ops
     */
    private static final Pattern ncsOpsChainNameRegex = Pattern.compile("([A-Za-z])+\\d+n");

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param auFile the input file with a PDB structure (AU) in mmCIF/PDB format
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param assemblyDB the db model data with assembly and residue info data
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public void getAssemblyCoordsMmcif(String jobId, File auFile, OutputStream os, PdbInfo pdbInfoDB, Assembly assemblyDB, boolean withEvolScores) throws IOException {
        Structure s = readCoords(auFile);
        getAssemblyCoordsMmcif(jobId, s, os, pdbInfoDB, assemblyDB, withEvolScores);
    }

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param jobId      the job identifier to write in output stream, can be null if unavailable from caller
     * @param s          the input structure with a PDB structure (AU)
     * @param os         the output stream with the assembly in mmCIF format
     * @param pdbInfoDB  the pdb data with chain clusters
     * @param assemblyDB the db model data with assembly and residue info data
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public void getAssemblyCoordsMmcif(String jobId, Structure s, OutputStream os, PdbInfo pdbInfoDB, Assembly assemblyDB, boolean withEvolScores) throws IOException {

        List<AtomSite> atomSiteList = new ArrayList<>();

        for (GraphNode node : assemblyDB.getGraphNodes()) {

            if (!node.isIn3dStructure())
                continue;

            String chainName = fixChainNameForNcsEntry(node.getLabel().split("_")[0]);
            String opId = node.getLabel().split("_")[1];

            Chain c = (Chain) s.getPolyChainByPDB(chainName).clone();
            List<Chain> nonPolyChains = new ArrayList<>();
            for (Chain nonPolyChain: s.getNonPolyChainsByPDB(chainName)) {
                nonPolyChains.add((Chain) nonPolyChain.clone());
            }

            if (withEvolScores)
                addEvolutionaryScores(c, pdbInfoDB);

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
        os.write((SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_jobId_" + jobId + "_assemblyId_" + assemblyDB.getId()+ "\n").getBytes());
        os.write(FileConvert.getAtomSiteHeader().getBytes());

        // writing content
        os.write(MMCIFFileTools.toMMCIF(atomSiteList, AtomSite.class).getBytes());
    }

    /**
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param auFile the input file with a PDB structure (AU) in mmCIF/PDB format
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceDB the interface data
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public void getInterfaceCoordsMmcif(String jobId, File auFile, OutputStream os, PdbInfo pdbInfoDB, Interface interfaceDB, boolean withEvolScores) throws IOException {
        Structure s = readCoords(auFile);
        getInterfaceCoordsMmcif(jobId, s, os, pdbInfoDB, interfaceDB, withEvolScores);
    }

    /**
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param s the input structure with a PDB structure (AU)
     * @param os the output stream with the assembly in mmCIF format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceDB the interface data
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public void getInterfaceCoordsMmcif(String jobId, Structure s, OutputStream os, PdbInfo pdbInfoDB, Interface interfaceDB, boolean withEvolScores) throws IOException {

        List<AtomSite> atomSiteList = new ArrayList<>();

        String chainName1 = fixChainNameForNcsEntry(interfaceDB.getChain1());
        String chainName2 = fixChainNameForNcsEntry(interfaceDB.getChain2());

        Chain c1 = s.getPolyChainByPDB(chainName1);
        Chain c2 = (Chain) s.getPolyChainByPDB(chainName2).clone();
        List<Chain> nonPolyChains1 = s.getNonPolyChainsByPDB(chainName1);
        List<Chain> nonPolyChains2 = new ArrayList<>();
        for (Chain nonPolyChain : s.getNonPolyChainsByPDB(chainName2)) {
            nonPolyChains2.add((Chain)nonPolyChain.clone());
        }

        if (withEvolScores) {
            addEvolutionaryScores(c1, pdbInfoDB);
            addEvolutionaryScores(c2, pdbInfoDB);
        }

        Matrix4d tranform = SpaceGroup.getMatrixFromAlgebraic(interfaceDB.getOperator());
        CrystalCell cell = s.getPDBHeader().getCrystallographicInfo().getCrystalCell();
        if (cell!=null) {
            tranform = cell.transfToOrthonormal(tranform);
        }
        Calc.transform(c2, tranform);
        for (Chain nonPolyChain : nonPolyChains2) {
            Calc.transform(nonPolyChain, tranform);
        }

        // 1: all atoms (poly and non-poly) of first chain
        addAtomSites(c1, atomSiteList, null);
        nonPolyChains1.forEach(nonPolyChain -> addAtomSites(nonPolyChain, atomSiteList, null));

        // 2: all atoms (poly and non-poly) of second chain
        String opId2 = null;
        if (c1.getId().equals(c2.getId())) {
            opId2 = String.valueOf(interfaceDB.getOperatorId());
        }
        addAtomSites(c2, atomSiteList, opId2);
        for (Chain nonPolyChain : nonPolyChains2) {
            addAtomSites(nonPolyChain, atomSiteList, opId2);
        }

        // TODO check what's the right charset to use
        // writing header
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

            parser.parse(new BufferedReader(new InputStreamReader(IOUtils.openFile(auFile))));

            structure = consumer.getStructure();
        } else if (fileType == FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {
            PDBFileParser parser = new PDBFileParser();

            parser.setFileParsingParameters(fileParsingParams);

            structure = parser.parsePDBFile(IOUtils.openFile(auFile));

        } else {
            // TODO support mmtf, add it to file type guesser
            throw new IOException("AU coordinate file "+auFile.toString()+" does not seem to be in one of the supported formats");
        }

        long end = System.currentTimeMillis();

        logger.info("Time needed to parse file {}: {} ms", auFile.toString(), end-start);

        return structure;
    }

    private void addEvolutionaryScores(Chain c, PdbInfo pdbInfoDB) {

        ChainCluster chainClusterDB = getChainCluster(pdbInfoDB, c.getEntityInfo().getRepresentative().getName());

        if (chainClusterDB == null) {
            logger.warn("Could not find ChainCluster for job {} and representative chain {}", pdbInfoDB.getJobId(), c.getEntityInfo().getRepresentative().getName());
            return;
        }

        AAAlphabet alphabet = new AAAlphabet(pdbInfoDB.getRunParameters().getAlphabet());
        double maxEntropy = Math.log(alphabet.getNumLetters()) / Math.log(2);

        for (Group g : c.getAtomGroups()) {
            int resSerial = c.getEntityInfo().getAlignedResIndex(g, c);
            ResidueInfo res = getResidue(chainClusterDB, resSerial);
            double entropy;
            if (res != null) {
                entropy = res.getEntropyScore();
                if (Double.isNaN(entropy)) {
                    entropy = maxEntropy;
                }

            } else {
                entropy = maxEntropy;
                logger.warn("Residue info could not be found from data in db: res serial {}, chain asym id {}. Setting entropy to maxEntropy", resSerial, c.getId());
            }
            for (Atom a : g.getAtoms()) {
                a.setTempFactor((float) entropy);
            }

        }
    }

    private void addAtomSites(Chain c, List<AtomSite> atomSites, String opId) {
        // TODO set atom ids to be unique throughout (problem for sym mates)
        for (Group g : c.getAtomGroups()) {
            for (Atom a : g.getAtoms()) {
                String chainId = c.getId() + ((opId==null)?"":"_" + opId);
                String chainName = c.getName() + ((opId==null)?"":"_" + opId);
                atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, chainName, chainId));
            }
            // we intentionally not write altloc groups
            // if we decide to write out altloc groups then #220 has to be taken into account
            // and make sure that we eliminate duplicate atoms that can be present in biojava altloc groups
            // see also https://github.com/biojava/biojava/issues/778
        }
    }

    private ChainCluster getChainCluster(PdbInfo pdbInfo, String chainName) {
        for (ChainCluster chainCluster : pdbInfo.getChainClusters()) {
            if (chainCluster.getRepChain().equals(chainName)) {
                return chainCluster;
            }
        }

        return null;
    }

    private ResidueInfo getResidue(ChainCluster chainCluster, int resSerial) {
        for (ResidueInfo residueInfo : chainCluster.getResidueInfos()) {
            if (residueInfo.getResidueNumber() == resSerial) {
                return residueInfo;
            }
        }
        return null;
    }

    /**
     * Entries with NCS ops are assigned by biojava an artificial chain name ending with "n" and
     * with an NCS operator identifier, e.g. C5n. This reverses the name to the original
     * chain name.
     * @param chainName the chain name
     * @return the original chain name if chain name contains "n" as last char or the same as input if it's
     * a standard chain name
     */
    private String fixChainNameForNcsEntry(String chainName) {

        if (chainName.length()>2 && chainName.endsWith("n")) {
            Matcher m = ncsOpsChainNameRegex.matcher(chainName);
            if (m.matches()) {
                return m.group(1);
            }
        }

        return chainName;
    }
 }
