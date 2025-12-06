package eppic;

import eppic.cli.CommonCliParams;
import eppic.cli.MultiInputCliParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

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
        multiInputCliParams.inputs.forEach(input -> {
            Main main = new Main();
            EppicParams eppicParams = null;
            try {
                eppicParams = commonCliParams.toEppicParams(input, null);
            } catch (EppicException e) {
                LOGGER.error("Skipping input [ {} ], due to parameters parsing error: {}", input, e.getMessage());
                return;
            }
            try {
                main.run(eppicParams);
            } catch (Exception e) {
                LOGGER.error("Failed processing input [ {} ], due to error: {}", input, e.getMessage());
            }
        });
    }
}
