/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-01-23 00:24:52 -0600 (Sun, 23 Jan 2011) $
 * $Revision: 15021 $
 *
 * Copyright (C) 2002-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.applet;

import org.jmol.appletwrapper.AppletWrapper;

import java.applet.Applet;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;

import netscape.javascript.JSObject;

import org.jmol.util.Logger;
import org.jmol.util.TextFormat;

final class JmolAppletRegistry {

  static Hashtable htRegistry = new Hashtable();

  synchronized static void checkIn(String name, Applet applet) {
    cleanRegistry();
    if (name != null) {
      Logger.info("AppletRegistry.checkIn(" + name + ")");
      htRegistry.put(name, applet);
    }
    if (Logger.debugging) {
      Enumeration keys = htRegistry.keys();
      while (keys.hasMoreElements()) {
        String theApplet = (String) keys.nextElement();
        Logger.debug(theApplet + " " + htRegistry.get(theApplet));
      }
    }
  }

  synchronized static void checkOut(String name) {
    htRegistry.remove(name);
  }

  synchronized private static void cleanRegistry() {
    Enumeration keys = htRegistry.keys();
    AppletWrapper app = null;
    boolean closed = true;
    while (keys.hasMoreElements()) {
      String theApplet = (String) keys.nextElement();
      try {
        app = (AppletWrapper) (htRegistry.get(theApplet));
        JSObject theWindow = JSObject.getWindow(app);
        //System.out.print("checking " + app + " window : ");
        closed = ((Boolean) theWindow.getMember("closed")).booleanValue();
        //System.out.println(closed);
        if (closed || theWindow.hashCode() == 0) {
          //error trap
        }
        if (Logger.debugging)
          Logger.debug("Preserving registered applet " + theApplet
              + " window: " + theWindow.hashCode());
      } catch (Exception e) {
        closed = true;
      }
      if (closed) {
        if (Logger.debugging)
          Logger.debug("Dereferencing closed window applet " + theApplet);
        htRegistry.remove(theApplet);
        app.destroy();
      }
    }
  }

  synchronized public static void findApplets(String appletName,
                                              String mySyncId,
                                              String excludeName, List apps) {
    if (appletName != null && appletName.indexOf(",") >= 0) {
      String[] names = TextFormat.split(appletName, ",");
      for (int i = 0; i < names.length; i++)
        findApplets(names[i], mySyncId, excludeName, apps);
      return;
    }
    String ext = "__" + mySyncId + "__";
    if (appletName == null || appletName.equals("*") || appletName.equals(">")) {
      Enumeration keys = htRegistry.keys();
      while (keys.hasMoreElements()) {
        appletName = (String) keys.nextElement();
        if (!appletName.equals(excludeName) && appletName.indexOf(ext) > 0) {
          apps.add(appletName);
        }
      }
      return;
    }
    if (appletName.indexOf("__") < 0)
      appletName += ext;
    if (!htRegistry.containsKey(appletName))
      appletName = "jmolApplet" + appletName;
    if (!appletName.equals(excludeName) && htRegistry.containsKey(appletName)) {
      apps.add(appletName);
    }
  }
}
