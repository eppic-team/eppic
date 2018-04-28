package eppic.commons.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MmseqsRunner {

    private static final Logger logger = LoggerFactory.getLogger(MmseqsRunner.class);

    private static final String MMSEQS_TSV_SUFFIX = "_cluster.tsv";
    private static final String MMSEQS_REQSEQ_SUFFIX = "_req_seq.fasta";
    private static final String MMSEQS_ALLSEQS_SUFFIX = "_all_seqs.fasta";

    private MmseqsRunner() {

    }

    /**
     * Runs mmseqs2 and clusters the given input FASTA file on the given clusteringPercentId threshold.
     * @param mmseqsBin the mmseqs2 binary executable
     * @param inFile the input FASTA file
     * @param outFilePrefix the output file path, to be considered as prefix for all output files produced
     * @param clusteringPercentId the percent identity to cluster, e.g. 30
     * @param clusteringCoverage the coverage to apply to both query and target sequences
     * @param numThreads number of threads to run mmseqs2
     * @return a list with list of cluster members
     * @throws IOException
     * @throws InterruptedException
     * @throws BlastException
     */
    public static List<List<String>> runMmseqsEasyCluster(File mmseqsBin, File inFile, File outFilePrefix, int clusteringPercentId, double clusteringCoverage, int numThreads)
            throws IOException, InterruptedException, BlastException {

        // command to run mmseqs2 for 30% clustering over 0.9 coverage on both sequences
        // mmseqs easy-cluster pdb_seqres_pr.fasta clusterOutput tmp --min-seq-id 0.3 -c 0.9 -s 8 --max-seqs 1000 --cluster-mode 1

        List<String> cmd = new ArrayList<String>();

        File tmpDir = new File(outFilePrefix.getParent(), "tmp-" + outFilePrefix.getName());

        cmd.add(mmseqsBin.getAbsolutePath());
        cmd.add("easy-cluster");
        cmd.add(inFile.getAbsolutePath());
        cmd.add(outFilePrefix.getAbsolutePath());
        cmd.add(tmpDir.getAbsolutePath());
        cmd.add("--min-seq-id"); cmd.add(String.format("%4.2f",((double)clusteringPercentId/100.0)));
        cmd.add("-c"); cmd.add(String.format("%4.2f",clusteringCoverage));
        cmd.add("-s"); cmd.add("8");
        cmd.add("--max-seqs"); cmd.add("1000");
        cmd.add("--cluster-mode"); cmd.add("1");
        cmd.add("--threads"); cmd.add(Integer.toString(numThreads));

        StringBuilder cmdLine = new StringBuilder();
        for (String token:cmd) {
            cmdLine.append(token).append(" ");
        }

        logger.info("Will run mmseqs command: {}", cmdLine);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        File stdout = new File(outFilePrefix.getParent(), outFilePrefix.getName() + "_mmseqs-out.log");
        File stderr = new File(outFilePrefix.getParent(), outFilePrefix.getName() + "_mmseqs-err.log");
        pb.redirectOutput(stdout);
        pb.redirectError(stderr);

        Process proc = pb.start();

        int exitValue = proc.waitFor();
        if (exitValue>0) {
            throw new BlastException(mmseqsBin.getName() + " exited with error value " + exitValue);
        }

        // remove temp trash when successful
        Files.walk(tmpDir.toPath())
                .filter(Files::isRegularFile) // this catches symlinks too (tested in TestFileWalk)
                .map(Path::toFile)
                .forEach(File::delete);

        Files.walk(tmpDir.toPath())
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .forEach(File::delete);

        tmpDir.delete();

        // delete the log files if successful
        stdout.delete();
        stderr.delete();

        if (tmpDir.exists()) {
            logger.warn("Could not remove mmseqs2 temp dir {}", tmpDir);
        }

        // and we parse the output file
        File outFile = new File(outFilePrefix.getParent(), outFilePrefix.getName() + MMSEQS_TSV_SUFFIX);
        List<List<String>> clusters = getClustersFromTsv(outFile);

        // note that if mmseqs throws an exception then this is not
        // reached and thus files not removed on exit
        outFile.delete();
        // other files that mmseqs creates
        File reqseqFile = new File(outFilePrefix.getParent(), outFilePrefix.getName() + MMSEQS_REQSEQ_SUFFIX);
        File allseqsFile = new File(outFilePrefix.getParent(), outFilePrefix.getName() + MMSEQS_ALLSEQS_SUFFIX);
        reqseqFile.delete();
        allseqsFile.delete();

        return clusters;
    }

    /**
     * Parses mmseqs2 tsv clusers output: 2 values per line, first is representative, second member.
     * Output is sorted from larger to smaller
     * @param file the mmseqs2 output cluster tsv file
     * @return
     * @throws IOException
     */
    private static List<List<String>> getClustersFromTsv(File file) throws IOException {
        Map<String, List<String>> allClusters = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file));){
            String line;
            while ( (line = br.readLine())!=null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue;
                String[] tokens = line.split("\\s+");
                String rep = tokens[0];
                String member = tokens[1];
                List<String> members;
                if (!allClusters.containsKey(rep)) {
                    members = new ArrayList<>();
                    allClusters.put(rep, members);
                } else {
                    members = allClusters.get(rep);
                }
                members.add(member);
            }
        }
        List<List<String>> list = new ArrayList<>(allClusters.values());
        // sorting from largest to smallest
        list = list.stream().sorted( (o1, o2) -> Integer.compare(o2.size(), o1.size())).collect(Collectors.toList());
        return list;
    }
}
