EPPIC
=====

Evolutionary Protein-Protein Interface Classifier
http://www.eppic-web.org

If you use this software for your research, please cite:
 
> Duarte JM, Srebniak A, Schärer MA and Capitani G, Protein interface 
> classification by evolutionary analysis, BMC Bioinformatics 2012 13:334 
> doi:10.1186/1471-2105-13-334 
>
> http://www.biomedcentral.com/1471-2105/13/334

The software is released under the GNU General Public License (GPL).


USAGE
=====

Use 'eppic -h' to obtain help about the available command line options for 
eppic. A typical run would be:

    eppic -i 1smt.pdb -s

That will first calculate all interfaces present in the given PDB file,
then find homologs in UniProt by using BLAST and finally align them to 
produce evolutionary scores (core-rim score and core-surface score) for all
interfaces found. The geometry scores will also be output.

Many output files are produced. The most important one is the .scores 
file containing the scoring for the 3 indicators and for the combined 
consensus calls:
  
*  1smt.scores             -  The scores table

Also interesting are the sequence-related files, one per chain (the chain 
code e.g. 'A' is used for the extension):

*  1smt.A.aln              -  The alignment of the homolog sequences and query
*  1smt.A.entropies        -  The entropy values for each of the query's 
                              positions
*  1smt.A.log              -  The list of homologs used with some taxonomy info
  
With the -p option PDB files per interface are produced (interface identified 
by a serial number from 1 to n going from largest interface to smallest).

*  1smt.1.pdb.gz           -  The structure of interface 1: first partner of 
                              interface is in the original PDB file position, 
                              second partner is the one transformed with a 
                              crystal operator (unless it is in asymmetric 
                              unit).
                              The entropy values per residue are encoded as 
                              b-factors (useful for PyMOL coloring) 

Note that more structure files can be produced per interface by running with 
the -l switch (requires PyMOL): PyMOL session files (pse), PyMOL script files 
(pml), thumbnail png images of interfaces, etc.



INSTALLATION
============

EPPIC is a java program that should work in any Unix like system. 
It has only been tested in Linux but there is no reason why it should
not work in MacOSX.

You need Java 1.7 or newer. Most systems will have java already 
installed. Otherwise get it by installing the OpenJDK package in 
Linux or downloading java from http://java.com/en/download/index.jsp


1. Prerequisites:

   As minimum prerequisites you will need:

   - The UniRef100 database downloadable from UniProt 
     (http://www.uniprot.org/downloads), formatted for blasting with 
     formatdb.
   
   - Blast+ binaries, downloadable from 
     http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=Download

   - Blastclust binary found in legacy blast package, downloadable from 
     http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=Download
     
   - Clustalo (recommended) or T-coffee binary, downloadable from
     http://www.clustal.org/omega/
     http://www.tcoffee.org/Projects_home_page/t_coffee_home_page.html
     
   - The UniProt JAPI, downloadable from
     http://www.ebi.ac.uk/uniprot/remotingAPI/download/uniprotjapi.jar
     (can be automatically installed by the eppic script).

2. Building:
   - A pre-built package is available from http://eppic-web.org/downloads/eppic.zip
   - To build from source, run `mvn package` (optionally with the `-DskipTests` option)
     from the top-level or eppic-cli directory. This will generate the package
     `eppic-cli/target/eppic-cli-<version>.zip`

2. Configuring it: 
   
   - Unzip the eppic.zip file. This will produce an eppic directory with 
     a few files in it (including this README) and two subdirectories bin
     and lib.
     
   - (Optional) To use a local copy of `uniprotjapi.jar`, copy or symlink it to the `lib/` subdirectory.
     Otherwise, this will be automatically downloaded on the first run.
   
   - Copy the eppic.conf file provided to your home directory with the 
     name `.eppic.conf`. Edit it and set the parameters:
   
     - BLAST_DB_DIR
     - BLAST_DB
     - BLASTCLUST_BIN
     - BLASTP_BIN
     - CLUSTALO_BIN or TCOFFEE_BIN
   
     to their appropriate paths. 
     Optionally you can also set PYMOL_EXE and GRAPHVIZ_EXE (needed for
     -l option) and LOCAL_CIF_DIR (needed to provide PDB codes with -i). 


3. Now you can run eppic:
   
        ./bin/eppic
   
   In order to run it from anywhere in your system you will need to
   add the directory containing it to your path or simply add an alias
   to your .bashrc:
   
        alias eppic='/path/to/unpacked/eppic/bin/eppic'  



THIRD PARTY LICENSE INFORMATION
===============================

EPPIC uses the following third-party libraries
which are contained in the EPPIC download archive:

1.	**[OWL library for Structural Bioinformatics]
	(http://www.bioinformatics.org/owl).**
	License:  [GPL](http://www.gnu.org/copyleft/gpl.html)

2.	**[Jama]
	(http://math.nist.gov/javanumerics/jama/).**
	License:  Public Domain
	
3.	**[Jaligner]
	(http://jaligner.sourceforge.net/).**
	License:  [GPL](http://www.gnu.org/copyleft/gpl.html)
	
4.	**[MySQL java connector]
	(http://dev.mysql.com/downloads/connector/j/5.1.html).**
	License:  [GPL](http://www.gnu.org/copyleft/gpl.html)
	
5.	**[Java vecmath]
	(https://java3d.dev.java.net/binary-builds.html).**
	License:  [GPL with Classpath exception]
			  (http://www.gnu.org/software/classpath/license.html)
	
6.	**[JUNG graph library]
	(http://jung.sourceforge.net/).**
	License:  [JUNG license (BSD)]
			  (http://jung.sourceforge.net/license.txt)
	
7.	**[java-getopt]
	(http://www.urbanophile.com/arenn/hacking/download.html).**
	License:  [LGPL](http://www.gnu.org/licenses/lgpl.html)
	
8.	**[UniProt JAPI]
	(http://www.ebi.ac.uk/uniprot/remotingAPI/).**
	License:  Apache License, version 2

