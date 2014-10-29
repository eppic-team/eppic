eppic
=====

EPPIC is a software package for the analysis of interfaces present in protein crystal structures. It uses mainly evolutionary criteria to try to assess whether interfaces are biologically relevant or simply crystal contacts.

The software is divided into submodules: 
* eppic-cli: the command line interface
* eppic-wui: the web graphical user interface, a front-end to the command line tool. Tou can see it working live at http://www.eppic-web.org

Please note that the project was previously in two separate repositories (eppic-cli and eppic-wui) and has now been unified into a single repository here.

Feel free to fork or clone the repository, if you want to work with the source code, we would recommend using [eclipse](https://www.eclipse.org/). You will need the [m2e](https://www.eclipse.org/m2e/) (maven) and [google](https://developers.google.com/eclipse/) (GWT) plugins for the whole thing to work properly under eclipse. The whole project is written in Java, for the WUI with the help of [GWT](http://www.gwtproject.org) and [GXT](http://www.sencha.com/products/gxt/) frameworks.
