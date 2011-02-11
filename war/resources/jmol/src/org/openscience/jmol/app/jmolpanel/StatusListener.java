/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-06-26 23:35:44 -0500 (Fri, 26 Jun 2009) $
 * $Revision: 11131 $
 *
 * Copyright (C) 2000-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.jmol.app.jmolpanel;

import org.jmol.api.*;
import org.jmol.export.dialog.Dialog;
import org.jmol.util.*;
import org.jmol.viewer.JmolConstants;
import org.openscience.jmol.app.webexport.WebExport;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

class StatusListener implements JmolStatusListener {

  /*
   * starting with Jmol 11.7.27, JmolStatusListener extends JmolCallbackListener
   * 
   * providing a simpler interface if all that is wanted is callback
   * functionality.
   * 
   * Only three methods are involved:
   * 
   * boolean notifyEnabled(int type) -- lets the statusManager know if there is
   * an implementation of a given callback type
   * 
   * void notifyCallback(int type, Object[] data) -- callback action; data
   * varies with callback type -- see org.jmol.viewer.StatusManager for details
   * 
   * void setCallbackFunction(String callbackType, String callbackFunction) --
   * called by statusManager in response to the "set callback" script command --
   * also used by the Jmol application to change menus and languages -- can
   * remain unimplemented if no such user action is intended
   */

  JmolPanel jmol;
  DisplayPanel display;

  JmolViewer viewer;
  void setViewer(JmolViewer viewer) {
    this.viewer = viewer;
  }
  
  StatusListener(JmolPanel jmol, DisplayPanel display) {
    // just required for Jmol application's particular callbacks
    this.jmol = jmol;
    this.display = display;  
  }
  
  // / JmolCallbackListener interface ///
  public boolean notifyEnabled(int type) {
    switch (type) {
    case JmolConstants.CALLBACK_ANIMFRAME:
    case JmolConstants.CALLBACK_ECHO:
    case JmolConstants.CALLBACK_LOADSTRUCT:
    case JmolConstants.CALLBACK_MEASURE:
    case JmolConstants.CALLBACK_MESSAGE:
    case JmolConstants.CALLBACK_PICK:
    case JmolConstants.CALLBACK_SCRIPT:
      return true;
    case JmolConstants.CALLBACK_CLICK:
    case JmolConstants.CALLBACK_ERROR:
    case JmolConstants.CALLBACK_HOVER:
    case JmolConstants.CALLBACK_MINIMIZATION:
    case JmolConstants.CALLBACK_RESIZE:
    case JmolConstants.CALLBACK_SYNC:
      // applet only (but you could change this for your listener)
    }
    return false;
  }

  public void notifyCallback(int type, Object[] data) {
    String strInfo = (data == null || data[1] == null ? null : data[1]
        .toString());
    switch (type) {
    case JmolConstants.CALLBACK_LOADSTRUCT:
      notifyFileLoaded(strInfo, (String) data[2], (String) data[3],
          (String) data[4]);
      break;
    case JmolConstants.CALLBACK_ANIMFRAME:
      int[] iData = (int[]) data[1];
      int modelIndex = iData[0];
      if (modelIndex <= -2)
        modelIndex = -2 - modelIndex; // animation is running
      if (display.haveDisplay) {
        String menuName = (String) data[2];
        display.status.setStatus(1, menuName);
        jmol.getFrame().setTitle(menuName);
      }
      break;
    case JmolConstants.CALLBACK_SCRIPT:
      int msWalltime = ((Integer) data[3]).intValue();
      if (msWalltime == 0) {
        if (data[2] != null && display.haveDisplay)
          display.status.setStatus(1, (String) data[2]);
      }
      break;
    case JmolConstants.CALLBACK_ECHO:
      sendConsoleEcho(strInfo);
      break;
    case JmolConstants.CALLBACK_MEASURE:
      String mystatus = (String) data[3];
      boolean isSequence = (mystatus.indexOf("Sequence") >= 0);
      if (mystatus.indexOf("Picked") >= 0) // picking mode
        notifyAtomPicked(strInfo);
      else if (mystatus.indexOf("Completed") >= 0 || isSequence)
        sendConsoleEcho(strInfo.substring(strInfo.lastIndexOf(",") + 2, strInfo
            .length() - 1));
      if (!isSequence && mystatus.indexOf("Pending") < 0) {
        // System.out.println("jmol callback measure" + status);
        if (display.haveDisplay)
          display.measurementTable.updateTables();
      }
      break;
    case JmolConstants.CALLBACK_MESSAGE:
      sendConsoleMessage(data == null ? null : strInfo);
      break;
    case JmolConstants.CALLBACK_CLICK:
      // x, y, action, int[] {action}
      // the fourth parameter allows an application to change the action
      if (display.haveDisplay)
        display.status
          .setStatus(1, "(" + data[1] + "," + data[2] + ")");
      break;
    case JmolConstants.CALLBACK_PICK:
      notifyAtomPicked(strInfo);
      break;
    case JmolConstants.CALLBACK_ERROR:
    case JmolConstants.CALLBACK_HOVER:
    case JmolConstants.CALLBACK_MINIMIZATION:
    case JmolConstants.CALLBACK_RESIZE:
    case JmolConstants.CALLBACK_SYNC:
      // applet only (but you could change this for your listener)
      break;
    }
  }

  public void setCallbackFunction(String callbackType, String callbackFunction) {
    if (callbackType.equals("modelkit")) {
      jmol.setButtonMode(callbackFunction.equals("ON") ? "modelkit" : "rotate");
      return; 
    }
    if (callbackType.equalsIgnoreCase("menu")) {
      jmol.setupNewFrame(viewer.getStateInfo());
      return;
    }
    if (callbackType.equalsIgnoreCase("language")) {
      JmolResourceHandler.clear();
      Dialog.setupUIManager();
      if (jmol.webExport != null) {
        WebExport.saveHistory();
        WebExport.dispose();
        jmol.createWebExport();
      }
      jmol.setupNewFrame(viewer.getStateInfo());
      return;
    }
  }

  // / end of JmolCallbackListener interface ///

  public String eval(String strEval) {
   String msg = "# this funcationality is implemented only for the applet.\n" + strEval;
   sendConsoleMessage(msg);
    return msg;
  }

  /**
   * 
   * @param fileName
   * @param type
   * @param text_or_bytes
   * @param quality
   * @return null ("you do it" or canceled) or a message starting with OK or an
   *         error message
   */
  public String createImage(String fileName, String type, Object text_or_bytes,
                            int quality) {
    return null;
  }

  private void notifyAtomPicked(String info) {
    JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
        .getProperty("DATA_API", "getAppConsole", null);
    if (appConsole != null) {
      appConsole.sendConsoleMessage(info);
      appConsole.sendConsoleMessage("\n");
    }
    if (display.haveDisplay)
      display.status.setStatus(1, info);
  }

  private void notifyFileLoaded(String fullPathName, String fileName,
                                String modelName, String errorMsg) {
    if (errorMsg != null) {
      return;
    }
    if (!display.haveDisplay)
      return;

    // this code presumes only ptLoad = -1 (error), 0 (zap), or 3 (completed)
    String title = "Jmol";
    if (modelName != null && fileName != null)
      title = fileName + " - " + modelName;
    else if (fileName != null)
      title = fileName;
    else if (modelName != null)
      title = modelName;
    jmol.notifyFileOpen(fullPathName, title);
  }

  private void sendConsoleEcho(String strEcho) {
    JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
        .getProperty("DATA_API", "getAppConsole", null);
    if (appConsole != null)
      appConsole.sendConsoleEcho(strEcho);
  }

  private void sendConsoleMessage(String strStatus) {
    JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
        .getProperty("DATA_API", "getAppConsole", null);
    if (appConsole != null)
      appConsole.sendConsoleMessage(strStatus);
  }

  public void showUrl(String url) {
    try {
      Class c = Class.forName("java.awt.Desktop");
      Method getDesktop = c.getMethod("getDesktop", new Class[] {});
      Object deskTop = getDesktop.invoke(null, new Object[] {});
      Method browse = c.getMethod("browse", new Class[] { URI.class });
      Object arguments[] = { new URI(url) };
      browse.invoke(deskTop, arguments);
    } catch (Exception e) {
      Logger.error(e.getMessage());
      JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
          .getProperty("DATA_API", "getAppConsole", null);
      if (appConsole != null) {
        appConsole
            .sendConsoleMessage("Java 6 Desktop.browse() capability unavailable. Could not open "
                + url);
      } else {
        Logger
            .error("Java 6 Desktop.browse() capability unavailable. Could not open "
                + url);
      }
    }
  }

  /**
   * this is just a test method for isosurface FUNCTIONXY
   * 
   * @param functionName
   * @param nX
   * @param nY
   * @return f(x,y) as a 2D array
   * 
   */
  public float[][] functionXY(String functionName, int nX, int nY) {
    nX = Math.abs(nX);
    nY = Math.abs(nY);
    float[][] f = new float[nX][nY];
    // boolean isSecond = (functionName.indexOf("2") >= 0);
    for (int i = nX; --i >= 0;)
      for (int j = nY; --j >= 0;) {
        float x = i / 5f; // / 15f - 1;
        float y = j / 5f; // / 15f - 1;
        f[i][j] = (float) /* Math.sqrt */(x * x + y);
        if (Float.isNaN(f[i][j]))
          f[i][j] = -(float) Math.sqrt(-x * x - y);
        // f[i][j] = (isSecond ? (float) ((i + j - nX) / (2f)) : (float) Math
        // .sqrt(Math.abs(i * i + j * j)) / 2f);
        // if (i < 10 && j < 10)
        //System.out.println(" functionXY " + i + " " + j + " " + f[i][j]);
      }

    return f; // for user-defined isosurface functions (testing only -- bob
              // hanson)
  }

  public float[][][] functionXYZ(String functionName, int nX, int nY, int nZ) {
    nX = Math.abs(nX);
    nY = Math.abs(nY);
    nZ = Math.abs(nZ);
    float[][][] f = new float[nX][nY][nZ];
    for (int i = nX; --i >= 0;)
      for (int j = nY; --j >= 0;)
        for (int k = nZ; --k >= 0;) {
          float x = i / ((nX - 1) / 2f) - 1;
          float y = j / ((nY - 1) / 2f) - 1;
          float z = k / ((nZ - 1) / 2f) - 1;
          f[i][j][k] = x * x + y * y - z * z;//(float) x * x + y - z * z;
          // if (i == 22 || i == 23)
          // System.out.println(" functionXYZ " + i + " " + j + " " + k + " " +
          // f[i][j][k]);
        }
    return f; // for user-defined isosurface functions (testing only -- bob
              // hanson)
  }

  public Hashtable getRegistryInfo() {
    return null;
  }

}
