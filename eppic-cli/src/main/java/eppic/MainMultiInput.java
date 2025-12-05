package eppic;

import eppic.cli.CommonCliParams;
import eppic.cli.MultiInputCliParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

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
            try {
                EppicParams eppicParams = commonCliParams.toEppicParams(input, null);
                main.run(eppicParams);
            } catch (EppicException e) {
                LOGGER.error("Skipping input [ {} ], due to parameters parsing error: {}", input, e.getMessage());
            }
        });
    }
}
