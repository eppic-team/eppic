package eppic;

import eppic.cli.CommonCliParams;
import eppic.cli.MultiInputCliParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = EppicParams.PROGRAM_NAME_MULTI,
        mixinStandardHelpOptions = true, // adds -h, --help, -V, --version
        description = "EPPIC: Evolutionary Protein-Protein Interface Classifier. Multiple inputs executable")
public class MainMultiInput implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMultiInput.class);

    @CommandLine.Mixin
    private CommonCliParams commonCliParams;
    @CommandLine.Mixin
    private MultiInputCliParams multiInputCliParams;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainMultiInput()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        List<String> failedInputs = new ArrayList<>();
        multiInputCliParams.inputs.forEach(input -> {
            Main main = new Main();
            try {
                EppicParams eppicParams = commonCliParams.toEppicParams(input, null);
                main.run(eppicParams, true);
            } catch (Exception e) {
                LOGGER.error("Failed processing input [ {} ], due to error: {}", input, e.getMessage());
                failedInputs.add(input);
            }
        });

        if (failedInputs.size() > multiInputCliParams.toleratedFailureRate * multiInputCliParams.inputs.size()) {
            LOGGER.error("There were {} failed inputs, which is above the failure rate {}. Exiting with error state. Failed inputs: {}", failedInputs.size(), multiInputCliParams.toleratedFailureRate, failedInputs);
            throw new RuntimeException("Failed inputs: "+failedInputs);
        } else if (!failedInputs.isEmpty()) {
            LOGGER.warn("There were {} failed inputs, which is below the failure rate {}. Exiting with success state. Failed inputs: {}", failedInputs.size(), multiInputCliParams.toleratedFailureRate, failedInputs);
        } else {
            LOGGER.info("All inputs processed successfully.");
        }
    }
}
