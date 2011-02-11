package com.sparshui.server;

import org.jmol.util.Logger;

import com.sparshui.gestures.Gesture;

//import com.sparshui.gestures.Flick;
//import com.sparshui.gestures.GestureType;
//import com.sparshui.gestures.MultiPointDragGesture;
//import com.sparshui.gestures.RelativeDragGesture;
//import com.sparshui.gestures.RotateGesture;
//import com.sparshui.gestures.SinglePointDragGesture;
//import com.sparshui.gestures.SpinGesture;
//import com.sparshui.gestures.TouchGesture;
//import com.sparshui.gestures.ZoomGesture;
//import com.sparshui.gestures.DblClkGesture;

class GestureFactory {

	/**
   * 
   * Given either an Integer or a String, 
   * return a valid Gesture instance or null
   * 
   * adapted by Bob Hanson for Jmol 11/29/2009
   * 
   * @param gid an Integer or String
   * 
   * @return A new Gesture of type gestureID
   */
  static Gesture createGesture(Object gid) {
   if (gid instanceof String) {
     String name = (String) gid;
     try {
       return (Gesture) Class.forName(name).newInstance();
     } catch (Exception e) {
       Logger.error("[GestureFactory] Error creating instance for " + name + ": \n" + e.getMessage());
       return null;
     }
   }
   int gestureID = ((Integer) gid).intValue();
   /* unused in Jmol
	  switch (gestureID) {
	  case GestureType.DRAG_GESTURE:
			return new SinglePointDragGesture();
		case GestureType.MULTI_POINT_DRAG_GESTURE:
			return new MultiPointDragGesture();
		case GestureType.ROTATE_GESTURE:
			return new RotateGesture();
		case GestureType.SPIN_GESTURE:
			return new SpinGesture();
		case GestureType.TOUCH_GESTURE:
			return new TouchGesture();
		case GestureType.ZOOM_GESTURE:
			return new ZoomGesture();
		case GestureType.DBLCLK_GESTURE:
			return new DblClkGesture();
		case GestureType.FLICK_GESTURE:
			return new Flick();
		case GestureType.RELATIVE_DRAG_GESTURE:
			return new RelativeDragGesture();
	  }
	  */
	  Logger.error("[GestureFactory] Gesture not recognized: " + gestureID);
		return null;
	}

}
