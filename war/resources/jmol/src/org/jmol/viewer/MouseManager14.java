/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-05-27 10:37:12 -0500 (Thu, 27 May 2010) $
 * $Revision: 13234 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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
package org.jmol.viewer;

import java.awt.Component;
import java.awt.Event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jmol.export.image.ImageCreator;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.viewer.binding.Binding;

class MouseManager14 implements MouseWheelListener, MouseListener,
    MouseMotionListener, KeyListener {

  private Viewer viewer;
  private ActionManager actionManager;

  MouseManager14(Viewer viewer, ActionManager actionManager) {
    this.viewer = viewer;
    this.actionManager = actionManager;
    Component display = viewer.getDisplay();
    display.addKeyListener(this);
    display.addMouseListener(this);
    display.addMouseMotionListener(this);
    display.addMouseWheelListener(this);
  }

  void clear() {
    // nothing to do here now -- see ActionManager
  }

  void dispose() {
    Component display = viewer.getDisplay();
    actionManager.dispose();
    display.removeMouseListener(this);
    display.removeMouseMotionListener(this);
    display.removeMouseWheelListener(this);
    display.removeKeyListener(this);
  }

  private String keyBuffer = "";

  private void clearKeyBuffer() {
    if (keyBuffer.length() == 0)
      return;
    keyBuffer = "";
    if (viewer.getBooleanProperty("showKeyStrokes"))
      viewer
          .evalStringQuiet("!set echo _KEYSTROKES; set echo bottom left;echo \"\"");
  }

  private void addKeyBuffer(char ch) {
    if (ch == 10) {
      sendKeyBuffer();
      return;
    }
    if (ch == 8) {
      if (keyBuffer.length() > 0)
        keyBuffer = keyBuffer.substring(0, keyBuffer.length() - 1);
    } else {
      keyBuffer += ch;
    }
    if (viewer.getBooleanProperty("showKeyStrokes"))
      viewer
          .evalStringQuiet("!set echo _KEYSTROKES; set echo bottom left;echo "
              + Escape.escape("\1" + keyBuffer));
  }

  private void sendKeyBuffer() {
    String kb = keyBuffer;
    if (viewer.getBooleanProperty("showKeyStrokes"))
      viewer
          .evalStringQuiet("!set echo _KEYSTROKES; set echo bottom left;echo "
              + Escape.escape(keyBuffer));
    clearKeyBuffer();
    viewer.script(kb);
  }

  public void keyTyped(KeyEvent ke) {
    ke.consume();
    if (!viewer.menuEnabled())
      return;
    char ch = ke.getKeyChar();
    int modifiers = ke.getModifiers();
    // for whatever reason, CTRL may also drop the 6- and 7-bits,
    // so we are in the ASCII non-printable region 1-31
    if (Logger.debuggingHigh)
      Logger.debug("MouseManager keyTyped: " + ch + " " + (0+ch) + " " + modifiers);
    if (modifiers != 0) {
      switch (ch) {
      case (char) 11:
      case 'k': // keystrokes on/off
        boolean isON = !viewer.getBooleanProperty("allowKeyStrokes");
        switch (modifiers) {
        case Binding.CTRL:
          viewer.setBooleanProperty("allowKeyStrokes", isON);
          viewer.setBooleanProperty("showKeyStrokes", true);
          break;
        case Binding.CTRL_ALT:
        case Binding.ALT:
          viewer.setBooleanProperty("allowKeyStrokes", isON);
          viewer.setBooleanProperty("showKeyStrokes", false);
          break;
        }
        clearKeyBuffer();
        viewer.refresh(3, "showkey");
        break;
      case 22:
      case 'v': // paste
        switch (modifiers) {
        case Binding.CTRL:
          String ret = viewer.loadInline(ImageCreator.getClipboardTextStatic(), false);
          if (ret != null)
            Logger.error(ret);
        }
        break;
      case 26:
      case 'z': // undo
        switch (modifiers) {
        case Binding.CTRL:
          viewer.undoAction(false, 0, 1);
        }
        break;
      case 25:
      case 'y': // redo
        switch (modifiers) {
        case Binding.CTRL:
          viewer.undoAction(false, 0, -1);
        }
        break;        
      }
      return;
    }
    if (!viewer.getBooleanProperty("allowKeyStrokes"))
      return;
    addKeyBuffer(ch);
  }

  public void keyPressed(KeyEvent ke) {
    actionManager.keyPressed(ke);
  }

  public void keyReleased(KeyEvent ke) {
    actionManager.keyReleased(ke);
  }

  void mouseEntered(long time, int x, int y) {
    actionManager.mouseEntered(time, x, y);
  }

  void mouseExited(long time, int x, int y) {
    actionManager.mouseExited(time, x, y);
  }

  void setMouseMode() {
    clearKeyBuffer();
    actionManager.setMouseMode();
  }

  void mouseClicked(long time, int x, int y, int modifiers, int clickCount) {
    clearKeyBuffer();
    // clickedCount is not reliable on some platforms
    // so we will just deal with it ourselves
    actionManager.mouseClicked(time, x, y, modifiers, 1);
  }

  protected void mouseMoved(long time, int x, int y, int modifiers) {
    clearKeyBuffer();
    actionManager.mouseMoved(time, x, y, modifiers);
  }

  void mouseWheel(long time, int rotation, int modifiers) {
    clearKeyBuffer();
    actionManager.mouseWheel(time, rotation, modifiers);
  }

  void mousePressed(long time, int x, int y, int modifiers,
                    boolean isPopupTrigger) {
    clearKeyBuffer();
    actionManager.mousePressed(time, x, y, modifiers);
  }

  void mouseReleased(long time, int x, int y, int modifiers) {
    actionManager.mouseReleased(time, x, y, modifiers);
  }

  void mouseDragged(long time, int x, int y, int modifiers) {
    actionManager.mouseDragged(time, x, y, modifiers);
  }

  private int applyLeftMouse(int modifiers) {
    // if neither BUTTON2 or BUTTON3 then it must be BUTTON1
    return ((modifiers & Binding.LEFT_MIDDLE_RIGHT) == 0) ? (modifiers | Binding.LEFT)
        : modifiers;
  }

  int xWhenPressed, yWhenPressed, modifiersWhenPressed10;

  boolean handleOldJvm10Event(Event e) {
    int x = e.x, y = e.y, modifiers = e.modifiers;
    long time = e.when;
    modifiers = applyLeftMouse(modifiers);
    switch (e.id) {
    case Event.MOUSE_DOWN:
      xWhenPressed = x;
      yWhenPressed = y;
      modifiersWhenPressed10 = modifiers;
      mousePressed(time, x, y, modifiers, false);
      break;
    case Event.MOUSE_DRAG:
      mouseDragged(time, x, y, modifiers);
      break;
    case Event.MOUSE_ENTER:
      mouseEntered(time, x, y);
      break;
    case Event.MOUSE_EXIT:
      mouseExited(time, x, y);
      break;
    case Event.MOUSE_MOVE:
      mouseMoved(time, x, y, modifiers);
      break;
    case Event.MOUSE_UP:
      mouseReleased(time, x, y, modifiers);
      // simulate a mouseClicked event for us
      if (x == xWhenPressed && y == yWhenPressed
          && modifiers == modifiersWhenPressed10) {
        // the underlying code will turn this into dbl clicks for us
        mouseClicked(time, x, y, modifiers, 1);
      }
      break;
    default:
      return false;
    }
    return true;
  }

  public void mouseClicked(MouseEvent e) {
    mouseClicked(e.getWhen(), e.getX(), e.getY(), e.getModifiers(), e
        .getClickCount());
  }

  public void mouseEntered(MouseEvent e) {
    mouseEntered(e.getWhen(), e.getX(), e.getY());
  }

  public void mouseExited(MouseEvent e) {
    mouseExited(e.getWhen(), e.getX(), e.getY());
  }

  public void mousePressed(MouseEvent e) {
    mousePressed(e.getWhen(), e.getX(), e.getY(), e.getModifiers(), e
        .isPopupTrigger());
  }

  public void mouseReleased(MouseEvent e) {
    mouseReleased(e.getWhen(), e.getX(), e.getY(), e.getModifiers());
  }

  public void mouseDragged(MouseEvent e) {
    int modifiers = e.getModifiers();
    /****************************************************************
     * Netscape 4.* Win32 has a problem with mouseDragged if you left-drag then
     * none of the modifiers are selected we will try to fix that here
     ****************************************************************/
    if ((modifiers & Binding.LEFT_MIDDLE_RIGHT) == 0)
      modifiers |= Binding.LEFT;
    /****************************************************************/
    mouseDragged(e.getWhen(), e.getX(), e.getY(), modifiers);
  }

  public void mouseMoved(MouseEvent e) {
    mouseMoved(e.getWhen(), e.getX(), e.getY(), e.getModifiers());
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    e.consume();
    mouseWheel(e.getWhen(), e.getWheelRotation(), e.getModifiers()
        | Binding.WHEEL);
  }
}
