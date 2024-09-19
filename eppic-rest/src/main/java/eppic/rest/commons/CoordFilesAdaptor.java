package eppic.rest.commons;

import eppic.commons.sequence.AAAlphabet;
import eppic.commons.util.FileTypeGuesser;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.GraphNodeDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.ResidueInfoDB;
import org.biojava.nbio.core.sequence.io.util.IOUtils;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.cif.AbstractCifFileSupplier;
import org.biojava.nbio.structure.io.cif.CifStructureConverter;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.rcsb.cif.CifBuilder;
import org.rcsb.cif.CifIO;
import org.rcsb.cif.model.Category;
import org.rcsb.cif.schema.StandardSchemata;
import org.rcsb.cif.schema.mm.MmCifBlockBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
    public static final Pattern ncsOpsChainNameRegex = Pattern.compile("([A-Za-z]+)(\\d+n)");

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param auFile the URL of input file with a PDB structure (AU) in mmCIF/PDB format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param assemblyId the eppic assembly id
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public byte[] getAssemblyCoordsMmcif(String jobId, URL auFile, PdbInfoDB pdbInfoDB, int assemblyId, boolean withEvolScores) throws IOException {
        Structure s = readCoords(auFile);
        return getAssemblyCoordsMmcif(jobId, s, pdbInfoDB, assemblyId, withEvolScores);
    }

    public byte[] getAssemblyCoordsMmcif(String jobId, InputStream is, PdbInfoDB pdbInfoDB, int assemblyId, boolean withEvolScores) throws IOException {
        Structure s = readCoords(is);
        return getAssemblyCoordsMmcif(jobId, s, pdbInfoDB, assemblyId, withEvolScores);
    }

    /**
     * Given an input file with coordinates of an AU in mmCIF format produces
     * an mmCIF format output stream representing the given assembly db model object
     * with b-factors set to the evolutionary scores from the db model objects.
     *
     * @param jobId      the job identifier to write in output stream, can be null if unavailable from caller
     * @param s          the input structure with a PDB structure (AU)
     * @param pdbInfoDB  the pdb data with chain clusters
     * @param assemblyId the eppic assembly id
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    private byte[] getAssemblyCoordsMmcif(String jobId, Structure s, PdbInfoDB pdbInfoDB, int assemblyId, boolean withEvolScores) throws IOException {

        List<AbstractCifFileSupplier.WrappedAtom> wrappedAtoms = new ArrayList<>();

        for (GraphNodeDB node : pdbInfoDB.getAssemblyById(assemblyId).getGraphNodes()) {

            if (!node.isIn3dStructure())
                continue;

            String chainName = node.getLabel().split("_")[0];
            String opId = node.getLabel().split("_")[1];
            String chainNameOrig = fixChainNameForNcsEntry(chainName);

            Chain c = (Chain) s.getPolyChainByPDB(chainNameOrig).clone();
            List<Chain> nonPolyChains = new ArrayList<>();
            for (Chain nonPolyChain: s.getNonPolyChainsByPDB(chainNameOrig)) {
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

            addAtomSites(c, wrappedAtoms, chainName, opId);
            nonPolyChains.forEach(nonPolyChain -> addAtomSites(nonPolyChain, wrappedAtoms, chainName, opId));

        }

        // TODO check what's the right charset to use

        MmCifBlockBuilder mmCifBlockBuilder = CifBuilder.enterFile(StandardSchemata.MMCIF)
                .enterBlock("eppic_jobId_" + jobId + "_assemblyId_" + assemblyId);

        Category atomSite = wrappedAtoms.stream().collect(AbstractCifFileSupplier.toAtomSite());
        mmCifBlockBuilder.addCategory(atomSite);

        // writing content
        return  CifIO.writeText(mmCifBlockBuilder.leaveBlock().leaveFile());
    }

    /**
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param auFile the URL of input file with a PDB structure (AU) in mmCIF/PDB format
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceId the interface id
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    public byte[] getInterfaceCoordsMmcif(String jobId, URL auFile, PdbInfoDB pdbInfoDB, int interfaceId, boolean withEvolScores) throws IOException {
        Structure s = readCoords(auFile);
        return getInterfaceCoordsMmcif(jobId, s, pdbInfoDB, interfaceId, withEvolScores);
    }

    public byte[] getInterfaceCoordsMmcif(String jobId, InputStream is, PdbInfoDB pdbInfoDB, int interfaceId, boolean withEvolScores) throws IOException {
        Structure s = readCoords(is);
        return getInterfaceCoordsMmcif(jobId, s, pdbInfoDB, interfaceId, withEvolScores);
    }

    /**
     *
     * @param jobId the job identifier to write in output stream, can be null if unavailable from caller
     * @param s the input structure with a PDB structure (AU)
     * @param pdbInfoDB the pdb data with chain clusters
     * @param interfaceId the interface id
     * @param withEvolScores whether to set b-factors to evolutionary scores from residue info data or not
     * @throws IOException
     */
    private byte[] getInterfaceCoordsMmcif(String jobId, Structure s, PdbInfoDB pdbInfoDB, int interfaceId, boolean withEvolScores) throws IOException {

        List<AbstractCifFileSupplier.WrappedAtom> wrappedAtoms = new ArrayList<>();

        InterfaceDB interfaceDB = pdbInfoDB.getInterface(interfaceId);

        String chainName1 = interfaceDB.getChain1();
        String chainName2 = interfaceDB.getChain2();
        String chainNameOrig1 = fixChainNameForNcsEntry(chainName1);
        String chainNameOrig2 = fixChainNameForNcsEntry(chainName2);

        Chain c1 = s.getPolyChainByPDB(chainNameOrig1);
        Chain c2 = (Chain) s.getPolyChainByPDB(chainNameOrig2).clone();
        List<Chain> nonPolyChains1 = s.getNonPolyChainsByPDB(chainNameOrig1);
        List<Chain> nonPolyChains2 = new ArrayList<>();
        for (Chain nonPolyChain : s.getNonPolyChainsByPDB(chainNameOrig2)) {
            nonPolyChains2.add((Chain)nonPolyChain.clone());
        }

        if (withEvolScores) {
            addEvolutionaryScores(c1, pdbInfoDB);
            addEvolutionaryScores(c2, pdbInfoDB);
        }

        // for the NCS case (mostly viral capsids) we've got to hack in the operators that create the full AU
        if (pdbInfoDB.isNcsOpsPresent()) {
            // we don't have the NCS ops at interface level, we've got to grab them from the unit cell assembly
            AssemblyDB unitCellAssembly = pdbInfoDB.getAssemblyById(0);
            Matrix4d ncsOp1 = findNcsOp(unitCellAssembly.getGraphNodes(), chainName1);
            Matrix4d ncsOp2 = findNcsOp(unitCellAssembly.getGraphNodes(), chainName2);
            if (ncsOp1 != null) {
                c1 = (Chain) c1.clone();
                Calc.transform(c1, ncsOp1);
                for (Chain nonPolyChain1 : nonPolyChains1) {
                    nonPolyChain1 = (Chain) nonPolyChain1.clone();
                    Calc.transform(nonPolyChain1, ncsOp1);
                }
            }
            if (ncsOp2 != null) {
                Calc.transform(c2, ncsOp2);
                for (Chain nonPolyChain2 : nonPolyChains2) {
                    Calc.transform(nonPolyChain2, ncsOp2);
                }
            }
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
        addAtomSites(c1, wrappedAtoms, chainName1, null);
        nonPolyChains1.forEach(nonPolyChain -> addAtomSites(nonPolyChain, wrappedAtoms, chainName1, null));

        // 2: all atoms (poly and non-poly) of second chain
        String opId2 = null;
        if (chainName1.equals(chainName2)) {
            opId2 = String.valueOf(interfaceDB.getOperatorId());
        }
        addAtomSites(c2, wrappedAtoms, chainName2, opId2);
        for (Chain nonPolyChain : nonPolyChains2) {
            addAtomSites(nonPolyChain, wrappedAtoms, chainName2, opId2);
        }

        // TODO check what's the right charset to use

        MmCifBlockBuilder mmCifBlockBuilder = CifBuilder.enterFile(StandardSchemata.MMCIF)
                .enterBlock("eppic_jobId_" + jobId + "_interfaceId_" + interfaceDB.getInterfaceId());

        Category atomSite = wrappedAtoms.stream().collect(AbstractCifFileSupplier.toAtomSite());
        mmCifBlockBuilder.addCategory(atomSite);

        // writing content
        return CifIO.writeText(mmCifBlockBuilder.leaveBlock().leaveFile());
    }

    private Structure readCoords(URL auCoordsFileUrl) throws IOException {

        long start = System.currentTimeMillis();

        Structure structure;
        FileParsingParameters fileParsingParams = new FileParsingParameters();
        fileParsingParams.setAlignSeqRes(true);

        if (auCoordsFileUrl.getProtocol().startsWith("http")) {
            if (!auCoordsFileUrl.toString().endsWith(".cif.gz")) {
                throw new IOException("Expected a URL ending with .cif.gz for PDB archive url " + auCoordsFileUrl);
            }
            structure = CifStructureConverter.fromInputStream(new GZIPInputStream(auCoordsFileUrl.openStream()), fileParsingParams);
        } else if (auCoordsFileUrl.getProtocol().equals("file")) {
            File auFile = new File(auCoordsFileUrl.getFile());
            int fileType = FileTypeGuesser.guessFileType(auFile);

            if (fileType==FileTypeGuesser.CIF_FILE) {
                structure = CifStructureConverter.fromInputStream(IOUtils.openFile(auFile), fileParsingParams);
            } else if (fileType == FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {
                PDBFileParser parser = new PDBFileParser();

                parser.setFileParsingParameters(fileParsingParams);

                structure = parser.parsePDBFile(IOUtils.openFile(auFile));
            } else {
                // TODO support mmtf, add it to file type guesser
                throw new IOException("AU coordinate file "+auCoordsFileUrl.toString()+" does not seem to be in one of the supported formats");
            }

        } else {
            throw new IOException("Unsupported protocol for file url " + auCoordsFileUrl);
        }

        long end = System.currentTimeMillis();

        logger.info("Time needed to parse file {}: {} ms", auCoordsFileUrl.toString(), end-start);

        return structure;
    }

    private Structure readCoords(InputStream is) throws IOException {

        long start = System.currentTimeMillis();

        Structure structure;
        FileParsingParameters fileParsingParams = new FileParsingParameters();
        fileParsingParams.setAlignSeqRes(true);

        // we assume that the blob in db is always gzipped

        // read the stream once into memory, in case it is from network (e.g. db or url)
        byte[] bytes = new GZIPInputStream(is).readAllBytes();

        int fileType = FileTypeGuesser.guessFileType(new ByteArrayInputStream(bytes));

        if (fileType==FileTypeGuesser.CIF_FILE) {
            structure = CifStructureConverter.fromInputStream(new ByteArrayInputStream(bytes), fileParsingParams);
        } else if (fileType == FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {
            PDBFileParser parser = new PDBFileParser();

            parser.setFileParsingParameters(fileParsingParams);

            structure = parser.parsePDBFile(new ByteArrayInputStream(bytes));
        } else {
            // TODO support bcif, add it to file type guesser
            throw new IOException("AU coordinate stream does not seem to be in one of the supported formats");
        }

        long end = System.currentTimeMillis();

        logger.info("Time needed to parse coordinates input stream: {} ms", end-start);

        return structure;
    }


    private void addEvolutionaryScores(Chain c, PdbInfoDB pdbInfoDB) {

        ChainClusterDB chainClusterDB = getChainCluster(pdbInfoDB, c.getEntityInfo().getRepresentative().getName());

        if (chainClusterDB == null) {
            //logger.warn("Could not find ChainCluster for job {} and representative chain {}", pdbInfoDB.getJobId(), c.getEntityInfo().getRepresentative().getName());
            logger.warn("Could not find ChainCluster for job {} and representative chain {}", "FIXME", c.getEntityInfo().getRepresentative().getName());
            return;
        }

        AAAlphabet alphabet = new AAAlphabet(pdbInfoDB.getRunParameters().getAlphabet());
        double maxEntropy = Math.log(alphabet.getNumLetters()) / Math.log(2);

        for (Group g : c.getAtomGroups()) {
            int resSerial = c.getEntityInfo().getAlignedResIndex(g, c);
            ResidueInfoDB res = getResidue(chainClusterDB, resSerial);
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

    private void addAtomSites(Chain c, List<AbstractCifFileSupplier.WrappedAtom> wrappedAtoms, String chainName, String opId) {
        // TODO set atom ids to be unique throughout (problem for sym mates)

        // for NCS case (mostly viral capsids) we get the possible ncs suffix in chainName so that we can also add it to the chainId
        String chainIdWithNcsOpsSuffix = c.getId();
        if (chainName.length()>2 && chainName.endsWith("n")) {
            Matcher m = ncsOpsChainNameRegex.matcher(chainName);
            if (m.matches()) {
                chainIdWithNcsOpsSuffix = c.getId() + m.group(2);
            }
        }

        for (Group g : c.getAtomGroups()) {
            for (Atom a : g.getAtoms()) {
                String chainId = chainIdWithNcsOpsSuffix + ((opId==null)?"":"_" + opId);
                String chainNameForOutput = chainName + ((opId==null)?"":"_" + opId);
                wrappedAtoms.add(new AbstractCifFileSupplier.WrappedAtom(1, chainNameForOutput, chainId, a, a.getPDBserial()));
            }
            // we intentionally not write altloc groups
            // if we decide to write out altloc groups then #220 has to be taken into account
            // and make sure that we eliminate duplicate atoms that can be present in biojava altloc groups
            // see also https://github.com/biojava/biojava/issues/778
        }
    }

    private ChainClusterDB getChainCluster(PdbInfoDB pdbInfo, String chainName) {
        for (ChainClusterDB chainCluster : pdbInfo.getChainClusters()) {
            if (chainCluster.getRepChain().equals(chainName)) {
                return chainCluster;
            }
        }

        return null;
    }

    private ResidueInfoDB getResidue(ChainClusterDB chainCluster, int resSerial) {
        for (ResidueInfoDB residueInfo : chainCluster.getResidueInfos()) {
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

    private Matrix4d findNcsOp(List<GraphNodeDB> graphNodes, String chainName) {
        for (GraphNodeDB graphNode : graphNodes) {
            String[] tokens = graphNode.getLabel().split("_");
            // the NCS op is in the matching chain name with opId=0
            if (!tokens[1].equals("0")) continue;
            if (tokens[0].equals(chainName)) {
                Matrix4d op = new Matrix4d();
                op.m00 = graphNode.getRxx();
                op.m01 = graphNode.getRxy();
                op.m02 = graphNode.getRxz();

                op.m10 = graphNode.getRyx();
                op.m11 = graphNode.getRyy();
                op.m12 = graphNode.getRyz();

                op.m20 = graphNode.getRzx();
                op.m21 = graphNode.getRzy();
                op.m22 = graphNode.getRzz();

                op.m03 = graphNode.getTx();
                op.m13 = graphNode.getTy();
                op.m23 = graphNode.getTz();

                op.m30 = 0;
                op.m31 = 0;
                op.m32 = 0;
                op.m33 = 1;
                return op;
            }
        }
        return null;
    }
 }
