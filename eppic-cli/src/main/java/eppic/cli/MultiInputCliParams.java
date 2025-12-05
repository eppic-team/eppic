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
}
