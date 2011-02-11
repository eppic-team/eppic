package org.jmol.shape;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;

import org.jmol.g3d.Font3D;
import org.jmol.util.Logger;
import org.jmol.util.Point3fi;
import org.jmol.util.TextFormat;
import org.jmol.viewer.Viewer;

public class Object2dShape extends Shape {

  // Echo, Hover, JmolImage

  Hashtable objects = new Hashtable();
  Object2d currentObject;
  Font3D currentFont;
  Object currentColor;
  Object currentBgColor;
  float currentTranslucentLevel;
  float currentBgTranslucentLevel;
  protected String thisID;
  
  boolean isHover;
  boolean isAll;

  public void setProperty(String propertyName, Object value, BitSet bsSelected) {

    if ("allOff" == propertyName) {
      currentObject = null;
      isAll = true;
      objects = new Hashtable();
      return;
    }

    if ("delete" == propertyName) {
      if (currentObject == null) {
        if (isAll || thisID != null) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements()) {
            Text text = (Text) e.nextElement();
            if (isAll
                || TextFormat.isMatch(text.target.toUpperCase(), thisID, true,
                    true))
              objects.remove(text.target);
          }
        }
        return;
      }
      objects.remove(currentObject.target);
      currentObject = null;
      return;
    }

    if ("off" == propertyName) {
      if (isAll) {
        objects = new Hashtable();
        isAll = false;
        currentObject = null;
      }
      if (currentObject == null)
        return;

      objects.remove(currentObject.target);
      currentObject = null;
      return;
    }

    if ("model" == propertyName) {
      int modelIndex = ((Integer) value).intValue();
      if (currentObject == null) {
        if (isAll) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements())
            ((Text) e.nextElement()).setModel(modelIndex);
        }
        return;
      }
      currentObject.setModel(modelIndex);
      return;
    }

    if ("align" == propertyName) {
      String align = (String) value;
      if (currentObject == null) {
        if (isAll) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements())
            ((Text) e.nextElement()).setAlignment(align);
        }
        return;
      }
      if (!currentObject.setAlignment(align))
        Logger.error("unrecognized align:" + align);
      return;
    }

    if ("bgcolor" == propertyName) {
      currentBgColor = value;
      if (currentObject == null) {
        if (isAll) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements())
            ((Text) e.nextElement()).setBgColix(value);
        }
        return;
      }
      currentObject.setBgColix(value);
      return;
    }

    if ("color" == propertyName) {
      currentColor = value;
      if (currentObject == null) {
        if (isAll || thisID != null) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements()) {
            Text text = (Text) e.nextElement();
            if (isAll
                || TextFormat.isMatch(text.target.toUpperCase(), thisID, true,
                    true))
              text.setColix(value);
          }
        }
        return;
      }
      currentObject.setColix(value);
      return;
    }

    if ("target" == propertyName) {
      String target = (String) value;
      isAll = target.equals("all");
      if (isAll || target.equals("none"))
        currentObject = null;
      //handled by individual types -- echo or hover
      return;
    }

    boolean isBackground;
    if ((isBackground = ("bgtranslucency" == propertyName))
        || "translucency" == propertyName) {
      boolean isTranslucent = ("translucent" == value);
      if (isBackground)
        currentBgTranslucentLevel = (isTranslucent ? translucentLevel : 0);
      else
        currentTranslucentLevel = (isTranslucent ? translucentLevel : 0);
      if (currentObject == null) {
        if (isAll) {
          Enumeration e = objects.elements();
          while (e.hasMoreElements())
            ((Text) e.nextElement()).setTranslucent(translucentLevel,
                isBackground);
        }
        return;
      }
      currentObject.setTranslucent(translucentLevel, isBackground);
      return;
    }

    if (propertyName == "deleteModelAtoms") {
      int modelIndex = ((int[]) ((Object[]) value)[2])[0];
      Enumeration e = objects.elements();
      while (e.hasMoreElements()) {
        Text text = (Text) e.nextElement();
        if (text.modelIndex == modelIndex)
          objects.remove(text.target);
        else if (text.modelIndex > modelIndex)
          text.modelIndex--;
      }
      return;
    }

    super.setProperty(propertyName, value, bsSelected);
  }

  protected void initModelSet() {
    currentObject = null;
    isAll = false;
  }


  public void setVisibilityFlags(BitSet bs) {
    if (isHover)
      return;
    Enumeration e = objects.elements();
    while (e.hasMoreElements()) {
      Text t = (Text)e.nextElement();
      t.setVisibility(t.modelIndex < 0 || bs.get(t.modelIndex));
    }
  }

  public Point3fi checkObjectClicked(int x, int y, int modifiers, BitSet bsVisible) {
    if (isHover)
      return null;
    Enumeration e = objects.elements();
    while (e.hasMoreElements()) {
      Object2d obj = (Object2d) e.nextElement();
      if (obj.checkObjectClicked(x, y, bsVisible)) {
        String s = obj.getScript();
        if (s != null)
          viewer.evalStringQuiet(s);
        Point3fi pt = new Point3fi();
        if (obj.xyz != null) {
          pt.set(obj.xyz);
          pt.modelIndex = (short) obj.modelIndex;
        }
        return pt; // may or may not be null
      }
    }
    return null;
  }

  public boolean checkObjectHovered(int x, int y, BitSet bsVisible) {
    if (isHover)
      return false;
    Enumeration e = objects.elements();
    boolean haveScripts = false;
    while (e.hasMoreElements()) {
      Object2d obj = (Object2d) e.nextElement();
      String s = obj.getScript();
      if (s != null) {
        haveScripts = true;
        if (obj.checkObjectClicked(x, y, bsVisible)) {
          viewer.setCursor(Viewer.CURSOR_HAND);
          return true;
        }
      }
    }
    if (haveScripts)
      viewer.setCursor(Viewer.CURSOR_DEFAULT);
    return false;
  }


}
