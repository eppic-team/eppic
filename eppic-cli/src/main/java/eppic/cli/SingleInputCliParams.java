package eppic.cli;

import picocli.CommandLine;

public class SingleInputCliParams {

    @CommandLine.Option(
            names = "-i",
            required = true,
            paramLabel = "<string>",
            description = "Input PDB code or PDB/mmCIF file"
    )
    public String inputStr;

    @CommandLine.Option(
            names = "-b",
            paramLabel = "<string>",
            description = "Basename for output files. Default: input PDB code or file name. " +
                    "Useful for web server to override entryId from job id."
    )
    public String baseName;
}
