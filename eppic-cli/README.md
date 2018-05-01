## EPPIC

Evolutionary Protein-Protein Interface Classifier
http://www.eppic-web.org

If you use this software for your research, please cite:
 
> Bliven S, Lafita A, Parker A, Capitani G, Duarte JM (2018) 
> Automated evaluation of quaternary structures from protein crystals. 
> PLoS Comput Biol 14(4): e1006104. https://doi.org/10.1371/journal.pcbi.1006104 
>
> http://journals.plos.org/ploscompbiol/article/metrics?id=10.1371/journal.pcbi.1006104

The software is released under the GNU General Public License (GPL).


## USAGE

Use 'eppic -h' to obtain help about the available command line options for 
eppic. A typical run would be:
```
eppic -i 1smt.pdb -s
```

That will first calculate all interfaces present in the given PDB file (expanded to entire crystal),
then find homologs in UniProt by using BLAST and finally align them to 
produce evolutionary scores (core-rim score and core-surface score) for all
interfaces found. The geometry scores will also be output. Additionally 
all valid (point-group symmetric) assemblies resulting from combinations of the 
interfaces are also output, together with stoichiometry, symmetry and prediction
scores (probability of assembly being biologically relevant).

Many output files are produced. The most important ones are the .assemblies and .scores 
files containing the assemblies and interfaces enumeration together with scores:
  
*  1smt.assemblies         -  The assemblies table with scores and predictions
*  1smt.scores             -  The interfaces table with scores and predictions

Also interesting are the sequence-related files, one per chain (the chain 
code e.g. 'A' is used for the extension):

*  1smt.A.aln              -  The alignment of the homolog sequences and query
*  1smt.A.entropies        -  The entropy values for each of the query's 
                              positions
*  1smt.A.log              -  The list of homologs used with some taxonomy info
  
With the -p option mmCIF files per assembly and interface are produced (interfaces identified 
by a serial number from 1 to n going from largest interface to smallest).

*  1smt.assembly.1.cif.gz  -  The structure of assembly 1.
*  1smt.interface.1.cif.gz -  The structure of interface 1: first partner of 
                              interface is in the original PDB file position, 
                              second partner is the one transformed with a 
                              crystal operator (unless it is in asymmetric 
                              unit).
                              The entropy values per residue are encoded as 
                              b-factors (useful for PyMOL coloring) 

Note that more structure files can be produced per interface by running with 
the -l switch (requires PyMOL): PyMOL session files (pse), PyMOL script files 
(pml), thumbnail png images of interfaces, etc.



## INSTALLATION

EPPIC is a java program that should work in any Unix like system. 
It has only been tested in Linux but there is no reason why it should
not work in MacOSX.

You need Java 1.8 or newer. Most systems will have java already 
installed. Otherwise get it by installing the OpenJDK package in 
Linux or downloading java from http://java.com/en/download/index.jsp


### Prerequisites

   As minimum prerequisites you will need:

   - The UniRef100 database downloadable from UniProt 
     (http://www.uniprot.org/downloads), formatted for blasting with 
     makeblastdb (from the blast+ suite). This is the command needed for formatting:
```
cd uniprot
gunzip -c uniref100.fasta.gz | makeblastdb -dbtype prot -logfile makeblastdb.log -parse_seqids -out uniref100.fasta -title uniref100.fasta
```
   
   - Blast+ binaries, downloadable from 
     http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=Download

   - Blastclust binary found in legacy blast package, downloadable from 
     http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=Download
     
   - Clustalo, downloadable from http://www.clustal.org/omega/

### Building
   - A pre-built package is available from http://eppic-web.org/downloads/eppic.zip
   - To build from source, run `mvn package` (optionally with the `-DskipTests` option)
     from the top-level. This will generate the package
     `eppic-cli/target/eppic-cli-<version>.zip`

### Configuring
   
   - Unzip the eppic.zip file. This will produce an eppic directory with 
     a few files in it (including this README) and two subdirectories bin
     and lib.
   
   - Copy the eppic.conf file provided to your home directory with the 
     name `.eppic.conf`. Edit it and set the parameters:
   
     - BLAST_DB_DIR
     - BLAST_DB
     - BLASTCLUST_BIN
     - BLASTP_BIN
     - CLUSTALO_BIN
   
     to their appropriate paths. 
     Optionally you can also set PYMOL_EXE and GRAPHVIZ_EXE (needed for
     -l option) and LOCAL_CIF_DIR (needed to provide PDB codes with -i). 


### Running
   
        ./bin/eppic
   
   In order to run it from anywhere in your system you will need to
   add the directory containing it to your path or simply add an alias
   to your .bashrc:
   
        alias eppic='/path/to/unpacked/eppic/bin/eppic'  



## THIRD PARTY LICENSE INFORMATION

EPPIC uses the following third-party libraries which are contained in the EPPIC download archive:

1.	**[BioJava](https://github.com/biojava/biojava)**
	License: [LGPL](http://www.gnu.org/licenses/lgpl.html)
		
2.	**[Jgrapht graph library](http://jgrapht.org/).**
	License:  [LGPL](http://www.gnu.org/licenses/lgpl.html)
	
3.	**[java-getopt](http://www.urbanophile.com/arenn/hacking/download.html).**
	License:  [LGPL](http://www.gnu.org/licenses/lgpl.html)
	
4.	**[UniProt JAPI](http://www.ebi.ac.uk/uniprot/remotingAPI/).**
	License:  Apache License, version 2

