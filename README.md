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
* **[eppic-wui](eppic-wui/README.md):** the web graphical user interface, a front-end to the command line tool. You can see it working live at http://www.eppic-web.org
* **eppic-model:** Data model, used by all other modules.
* **eppic-dbtools:** The DAO layer (with a JPA implementation). Also includes some tools for managing the database.
* **eppic-rest:** The REST API, new since 3.1.0.

Please note that the project was previously in two separate repositories (eppic-cli and eppic-wui) and has now been unified into a single repository here.

Feel free to fork or clone the repository, if you want to work with the source code, we would recommend using [eclipse](https://www.eclipse.org/). You will need the [m2e](https://www.eclipse.org/m2e/) (maven) and [google](https://developers.google.com/eclipse/) (GWT) plugins for the whole thing to work properly under eclipse. The whole project is written in Java, for the WUI with the help of [GWT](http://www.gwtproject.org) and [GXT](http://www.sencha.com/products/gxt/) frameworks.
