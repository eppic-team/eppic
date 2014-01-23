package com.sparshui.gestures;

/**
 * This class is a place for the enum GestureType to stand.
 * If you modify this class, you must also modify the corresponding
 * enum in GestureType.h in the C++ Gesture Server.  ALWAYS assign
 * this enum value to a 4-byte network-ordered integer before attempting
 * to transmit this value over the network.
 * 
 * @author Jay Roltgen
 *
 */
public class GestureType {

  public final static int DRAG_GESTURE = 0;
  public final static int MULTI_POINT_DRAG_GESTURE = 1;
  public final static int ROTATE_GESTURE = 2;
  public final static int SPIN_GESTURE = 3;
  public final static int TOUCH_GESTURE = 4;
  public final static int ZOOM_GESTURE = 5;
  public final static int DBLCLK_GESTURE = 6;
  public final static int FLICK_GESTURE = 7;
  public final static int RELATIVE_DRAG_GESTURE = 8;

  public static final int DRAG_EVENT = 0;
  public static final int ROTATE_EVENT = 1;
  public static final int SPIN_EVENT = 2;
  public final static int TOUCH_EVENT = 3;
  public final static int ZOOM_EVENT = 4;
  public final static int DBLCLK_EVENT = 5;
  public final static int FLICK_EVENT = 6;
  public final static int RELATIVE_DRAG_EVENT = 7;

}
