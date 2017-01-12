eppic-wui
=========

The web user interface for EPPIC. Based on the GXT and GWT frameworks. Visit the latest release at http://www.eppic-web.org

### Note on debugging
Running the client code on the java debugger should be possible by using GWT's superdev mode, but we haven't managed to make it work so far. 

Regarding the server side code, running it on the debugger is possible by simply using the [jetty eclipse plugin](http://eclipse-jetty.github.io/) (available from eclipse marketplace). Beware that at the moment the plugin comes bundled with jetty 8, which doesn't support java 8. One needs to point the jetty launcher to a 9.2+ jetty installation. This can be set in the Options tab in the jetty run configurations dialog.
