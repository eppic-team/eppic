package eppic.cli;

import picocli.CommandLine;

import java.util.List;

public class MultiInputCliParams {

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
}
