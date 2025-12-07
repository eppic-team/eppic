package eppic.cli;

import eppic.EppicException;
import eppic.EppicParams;
import eppic.HomologsSearchMode;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;

public class CommonCliParams {

    // the parameters

    @CommandLine.Option(
            names = "-i",
            required = true,
            paramLabel = "<string>",
            split = ",",
            description = "Input PDB codes or PDB/mmCIF files, comma separated"
    )
    public List<String> inputs;

    @CommandLine.Option(
            names = "-tfr",
            paramLabel = "<float>",
            description = "Tolerated failure rate for each input. The program will exit with error state if the failure rate exceeds this value. Default: 0 tolerance, i.e. a single failed input leads to error state"
    )
    public double toleratedFailureRate = 0.0;

    @CommandLine.Option(
            names = "-b",
            paramLabel = "<string>",
            description = "Basename for output files. Default: input PDB code or file name. " +
                    "Useful for web server to override entryId from job id."
    )
    public String baseName;

    @Option(
            names = "-s",
            description = "Calculate evolutionary entropy-based scores (core-rim and core-surface). " +
                    "If not specified, only geometric scoring is done.",
            defaultValue = "false"
    )
    private boolean doEvolScoring;

    @Option(
            names = "-d",
            paramLabel = "<float>",
            description = "Sequence identity soft cutoff; if enough homologs ("+ EppicParams.DEF_MIN_NUM_SEQUENCES+") above this threshold " +
                    "the search for homologs stops. Default: ${DEFAULT-VALUE}"
    )
    private double homSoftIdCutoff = EppicParams.DEF_HOM_SOFT_ID_CUTOFF;

    @Option(
            names = "-D",
            paramLabel = "<float>",
            description = "Sequence identity hard cutoff; after applying the soft cutoff (-d), " +
                    "threshold is lowered in "+EppicParams.DEF_HOM_ID_STEP+" steps until this cutoff is reached. " +
                    "Default: ${DEFAULT-VALUE}"
    )
    private double homHardIdCutoff = EppicParams.DEF_HOM_HARD_ID_CUTOFF;

    @Option(
            names = "-o",
            paramLabel = "<dir>",
            description = "Output directory for result files. Default: current directory",
            defaultValue = EppicParams.DEF_OUT_DIR
    )
    public File outDir;

    @Option(
            names = "-a",
            paramLabel = "<int>",
            description = "Number of threads for BLAST, alignment and ASA calculation. Default: ${DEFAULT-VALUE}",
            defaultValue = "" + EppicParams.DEF_NUMTHREADS
    )
    private int numThreads;

    @Option(
            names = "-e",
            paramLabel = "<float>",
            description = "BSA/ASA cutoff for core assignment in geometry predictor. Default: ${DEFAULT-VALUE}"
    )
    private double caCutoffForGeom = EppicParams.DEF_CA_CUTOFF_FOR_GEOM;

    @Option(
            names = "-c",
            paramLabel = "<float>",
            description = "BSA/ASA cutoff for core assignment in core-rim evolutionary predictor. " +
                    "Default: ${DEFAULT-VALUE}"
    )
    private double caCutoffForRimCore = EppicParams.DEF_CA_CUTOFF_FOR_RIMCORE;

    @Option(
            names = "-z",
            paramLabel = "<float>",
            description = "BSA/ASA cutoff for core assignment in core-surface evolutionary predictor. " +
                    "Default: ${DEFAULT-VALUE}"
    )
    private double caCutoffForZscore = EppicParams.DEF_CA_CUTOFF_FOR_ZSCORE;

    @Option(
            names = "-m",
            paramLabel = "<int>",
            description = "Geometry scoring cutoff for number of interface core residues. " +
                    "Below this value geometry call is XTAL, above or equal BIO. Default: ${DEFAULT-VALUE}",
            defaultValue = "" + EppicParams.DEF_MIN_CORE_SIZE_FOR_BIO
    )
    private int minCoreSizeForBio;

    @Option(
            names = "-q",
            paramLabel = "<int>",
            description = "Maximum number of sequences to keep for conservation scores. Default: ${DEFAULT-VALUE}",
            defaultValue = "" + EppicParams.DEF_MAX_NUM_SEQUENCES
    )
    private int maxNumSeqs;

    @Option(
            names = "-x",
            paramLabel = "<float>",
            description = "Core-rim score cutoff for calling BIO/XTAL. Below this score: BIO, above: XTAL. " +
                    "Default: ${DEFAULT-VALUE}",
            defaultValue = "" + EppicParams.DEF_CORERIM_SCORE_CUTOFF
    )
    private double coreRimScoreCutoff;

    @Option(
            names = "-y",
            paramLabel = "<float>",
            description = "Core-surface score cutoff for calling BIO/XTAL. Below this score: BIO, above: XTAL. " +
                    "Default: ${DEFAULT-VALUE}",
            defaultValue = "" + EppicParams.DEF_CORESURF_SCORE_CUTOFF
    )
    private double coreSurfScoreCutoff;

    @Option(
            names = "-p",
            description = "Generate coordinate files (gzipped mmCIF) for each interface and assembly."
    )
    private boolean generateOutputCoordFiles = false;

    @Option(
            names = "-t",
            paramLabel = "<dir>",
            description = "Temporary directory for gzipped mmCIF files (-p) and PyMOL files (-l). " +
                    "Useful together with -w to use fast storage."
    )
    private File tempCoordFilesDir;

    @Option(
            names = "-l",
            description = "Generate PyMOL thumbnail PNGs for each interface and assembly. Implies -p."
    )
    private boolean generateThumbnails = false;

    @Option(
            names = "-P",
            description = "Generate assembly diagram images and thumbnails."
    )
    private boolean generateDiagrams = false;

    @Option(
            names = "-f",
            description = "When used with -p, also generate PDB (gzipped) coordinate files."
    )
    private boolean generatePdbFiles = false;

    @Option(
            names = "-w",
            description = "Produce a zip file containing serialized JSON with all data. " +
                    "Coordinate files are removed in this mode."
    )
    private boolean generateModelSerializedFile = false;

    @Option(
            names = "-B",
            description = "Disable BLAST: use SIFTS only and sequence search cache only. " +
                    "Useful for precomputation to avoid dependency on BLAST index files."
    )
    private boolean noBlast = false;

    @Option(
            names = "-U",
            description = "Use local UniProt info via MongoDB (requires -G). " +
                    "Otherwise UniProt REST API is used."
    )
    private boolean useLocalUniProtInfo = false;

    @Option(
            names = "-g",
            paramLabel = "<file>",
            description = "EPPIC configuration file. Overrides config file in user's home directory."
    )
    private File configFile;

    @Option(
            names = "-G",
            paramLabel = "<file>",
            description = "MongoDB config file for sequence search cache and local UniProt info."
    )
    private File dbConfigFile;

    // We parse the raw string from the CLI and convert it to HomologsSearchMode after parsing.
    @Option(
            names = "-H",
            paramLabel = "<string>",
            description = "Homologs search mode: \"local\" (only UniProt region covered by PDB) " +
                    "or \"global\" (full UniProt entry). Default: ${DEFAULT-VALUE}"
    )
    private HomologsSearchMode homologsSearchMode = EppicParams.DEF_HOMOLOGS_SEARCH_MODE;

    @Option(
            names = "-O",
            description = "Restrict homolog search to same domain of life as query."
    )
    private boolean filterByDomain = false;


    private void checkCommandLineInput() throws EppicException {

        if (inputs.size()>1 && baseName!=null){
            throw new EppicException(null, "Basename cannot be specified when multiple inputs are given.", true);
        }

        if (configFile!=null && !configFile.exists()) {
            throw new EppicException(null, "Specified config file "+configFile+" doesn't exist",true);
        }

        if (dbConfigFile!=null && !dbConfigFile.exists()) {
            throw new EppicException(null, "Specified config file "+dbConfigFile+" doesn't exist",true);
        }

        if (noBlast && dbConfigFile == null) {
            throw new EppicException(null, "The no-blast mode (-B) requires using a db config file to query sequence search cache (-G)", true);
        }

        if (homologsSearchMode==null) {
            // invalid string passed as homologs search mode
            throw new EppicException(null, "Invalid string specified as homologs search mode (-H).", true);
        }

        if (homSoftIdCutoff<homHardIdCutoff) {
            homHardIdCutoff = homSoftIdCutoff;
        }

        // -l implies -p
        if (generateThumbnails) {
            generateOutputCoordFiles = true;
        }

        if (tempCoordFilesDir == null) {
            // if no temp coords file dir specified (-t), then write them to outDir
            tempCoordFilesDir = outDir;
        }

        if (useLocalUniProtInfo && dbConfigFile == null) {
            throw new EppicException(null, "A db config file must be provided (-G) when using local UniProt info from db (-U)", true);
        }
    }

    public EppicParams toEppicParams() throws EppicException {
        checkCommandLineInput();

        EppicParams eppicParams = new EppicParams();

        eppicParams.setCoreRimScoreCutoff( coreRimScoreCutoff);
        eppicParams.setCoreSurfScoreCutoff(coreSurfScoreCutoff);
        eppicParams.setHomHardCutoff(homHardIdCutoff);
        eppicParams.setHomSoftIdCutoff(homSoftIdCutoff);
        eppicParams.setMaxNumSeqs(maxNumSeqs);
        eppicParams.setHomologsSearchMode(homologsSearchMode);
        eppicParams.setCAcutoffForGeom(caCutoffForGeom);
        eppicParams.setCAcutoffForRimCore(caCutoffForRimCore);
        eppicParams.setCAcutoffForZscore(caCutoffForZscore);
        eppicParams.setMinCoreSizeForBio(minCoreSizeForBio);

        eppicParams.setDoEvolScoring(doEvolScoring);
        eppicParams.setNoBlast(noBlast);
        eppicParams.setIsFilterByDomain(filterByDomain);
        eppicParams.setUseLocalUniProtInfo(useLocalUniProtInfo);
        eppicParams.setGenerateOutputCoordFiles(generateOutputCoordFiles);
        eppicParams.setGenerateDiagrams(generateDiagrams);
        eppicParams.setGenerateModelSerializedFile(generateModelSerializedFile);
        eppicParams.setGenerateThumbnails(generateThumbnails);
        eppicParams.setGeneratePdbFiles(generatePdbFiles);

        eppicParams.setNumThreads(numThreads);
        eppicParams.setOutDir(outDir);
        eppicParams.setTempCoordFilesDir(tempCoordFilesDir);

        eppicParams.setConfigFile(configFile);
        eppicParams.setDbConfigFile(dbConfigFile);

        return eppicParams;
    }

}
