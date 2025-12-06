package eppic;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "eppic-suite",
        description = "EPPIC CLI Tool Suite",
        mixinStandardHelpOptions = true,
        subcommands = {
                Main.class,             // Registers the single input command (named "eppic")
                MainMultiInput.class    // Registers the multi input command (named "eppic-multi")
        })
public class UnifiedMain implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new UnifiedMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // This executes if no subcommand is provided
        System.out.println("Please specify a subcommand: " + EppicParams.PROGRAM_NAME + " or " + EppicParams.PROGRAM_NAME_MULTI);
        new CommandLine(this).usage(System.out);
    }
}