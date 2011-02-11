package org.jmol.api;

import java.awt.Component;

import org.jmol.viewer.Viewer;

public interface JmolAppConsoleInterface {

  JmolScriptEditorInterface getScriptEditor();

  JmolAppConsoleInterface getAppConsole(Viewer viewer, Component display);

  String getText();

  Object getMyMenuBar();

  void setVisible(boolean b);

  void sendConsoleEcho(String strEcho);

  void sendConsoleMessage(String strInfo);

  void zap();

  void dispose();


}
