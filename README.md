eppic
=====

EPPIC is a software package for the analysis of interfaces in protein crystal structures and for quaternary structure prediction. It uses mainly evolutionary criteria to assess whether interfaces are biologically relevant or simply crystal contacts. The pairwise interface scores are then analysed together to predict the likely oligomeric structure.

If you use this software for your research, please cite:
 
> Bliven S, Lafita A, Parker A, Capitani G, Duarte JM (2018) 
> Automated evaluation of quaternary structures from protein crystals. 
> PLoS Comput Biol 14(4): e1006104. https://doi.org/10.1371/journal.pcbi.1006104 
>
> http://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1006104

The software is released under the GNU General Public License (GPL).

The software is divided into submodules: 
* **[eppic-cli](eppic-cli/README.md):** the command line interface
* **eppic-model:** Data model, used by all other modules.
* **eppic-dbtools:** The DAO layer (with a JPA implementation). Also includes some tools for managing the database.
* **eppic-rest:** The REST API, new since 3.1.0.
* **eppic-util:** Common utilities

Feel free to fork or clone the repository. The project is written in Java.
