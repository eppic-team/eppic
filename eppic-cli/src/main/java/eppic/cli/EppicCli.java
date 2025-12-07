package eppic.cli;

import eppic.EppicException;
import eppic.EppicParams;
import eppic.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = EppicParams.PROGRAM_NAME,
        mixinStandardHelpOptions = true, // adds -h, --help, -V, --version
        description = "EPPIC: Evolutionary Protein-Protein Interface Classifier.")
public class EppicCli implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EppicCli.class);

    @CommandLine.Mixin
    private CommonCliParams commonCliParams;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EppicCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        EppicParams eppicParams = null;
        try {
            eppicParams = commonCliParams.toEppicParams();
        } catch (EppicException e) {
            LOGGER.error("Problem in CLI parameters: {}", e.getMessage());
            System.exit(1);
        }
        boolean useSubdirPerInput = commonCliParams.inputs.size() > 1;

        List<String> failedInputs = new ArrayList<>();
        for (String input : commonCliParams.inputs) {
            Main main = new Main();
            try {
                // note that basename is only allowed when size of inputs is 1. Thus for size>1 basename is always null
                setInput(eppicParams, input, commonCliParams.baseName);
                if (useSubdirPerInput) {
                    eppicParams.setOutDir(new File(commonCliParams.outDir, input));
                }
                main.run(eppicParams, true);
            } catch (Exception e) {
                LOGGER.error("Failed processing input [ {} ], due to error: {}", input, e.getMessage());
                failedInputs.add(input);
            }
        }

        if (failedInputs.size() > commonCliParams.toleratedFailureRate * commonCliParams.inputs.size()) {
            LOGGER.error("There were {} failed inputs, which is above the failure rate {}. Exiting with error state. Failed inputs: {}", failedInputs.size(), commonCliParams.toleratedFailureRate, failedInputs);
            throw new RuntimeException("Failed inputs: "+failedInputs);
        } else if (!failedInputs.isEmpty()) {
            LOGGER.warn("There were {} failed inputs, which is below the failure rate {}. Exiting with success state. Failed inputs: {}", failedInputs.size(), commonCliParams.toleratedFailureRate, failedInputs);
        } else {
            LOGGER.info("All inputs processed successfully.");
        }
    }

    private void setInput(EppicParams eppicParams, String inputStr, String baseName) throws EppicException{
        eppicParams.setInput(inputStr);
        if (eppicParams.isInputAFile()) {
            if (!eppicParams.getInFile().exists()) {
                throw new EppicException(null, "Input file "+eppicParams.getInFile()+" does not exist!", true);
            }
        }
        // important: must be set after inputStr
        eppicParams.setBaseName(baseName);
    }

}
