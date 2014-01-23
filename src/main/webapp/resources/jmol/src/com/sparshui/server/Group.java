package com.sparshui.server;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.sparshui.common.TouchState;
import com.sparshui.gestures.Gesture;

/**
 * Represents a group of touch points 
 * for the gesture server.
 * 
 * @author Tony Ross
 * 
 */
public class Group {

	private int _id;
	private List _gestureIDs;
	private List _gestures;
	private List _touchPoints;
	private ServerToClientProtocol _clientProtocol;

	/**
	 * Construct a new group with the given gesture IDs and
	 * the given connection to the client.  Groups are associate
	 * with one client only.
	 * @param id 
	 * 
	 * @param gestureIDs
	 * 		The list of gesture IDs or String class names 
	 *    that this group should process.
	 * @param clientProtocol
	 * 		Represents the connection to the client.
	 */
	public Group(int id, List gestureIDs,
			ServerToClientProtocol clientProtocol) {
		_id = id;
		_gestureIDs = gestureIDs;
		_gestures = new ArrayList();
		_touchPoints = new ArrayList();
		_clientProtocol = clientProtocol;
		for (int i = 0; i < _gestureIDs.size(); i++) {
		  Gesture gesture = GestureFactory.createGesture(_gestureIDs.get(i));
		  if (gesture != null)
  			_gestures.add(gesture);
		}
	}

	/**
	 * 
	 * @return
	 * 		The group ID
	 */
	public int getID() {
		return _id;
	}

	/**
   * Update the given touch point that belongs to this group.
   * 
   * @param changedPoint
   *          The changed touch point.
   */
  public synchronized void update(TouchPoint changedPoint) {
    List events = new ArrayList();
    
    int state = changedPoint.getState();

    if (state == TouchState.BIRTH)
      _touchPoints.add(changedPoint);

    /*
    System.out.print("Group _touchPoints ");
    for (int i = 0; i < _touchPoints.size(); i++) {
      System.out.print(" / " + i + ": " + (TouchPoint) _touchPoints.get(i));
    }
    System.out.println();
    */
    
    List clonedPoints = null;
    /*
     * // until this is implemented somewhere, why go to the trouble? -- BH
     * 
     * clonedPoints = new ArrayList(); for (int i = 0; i < nPoints; i++) {
     * TouchPoint touchPoint = (TouchPoint) _touchPoints.get(i); synchronized
     * (touchPoint) { TouchPoint clonedPoint = (TouchPoint) touchPoint.clone();
     * clonedPoints.add(clonedPoint); } }
     */
    for (int i = 0; i < _gestures.size(); i++) {
      Gesture gesture = (Gesture) _gestures.get(i);
      // System.out.println(_gestures.size());
      // System.out.println("Gesture allowed: " + gesture.getName());
      events.addAll(gesture.processChange(clonedPoints == null ? _touchPoints
          : clonedPoints, changedPoint));
      // System.out.println("Got some events - size: " + events.size());
    }

    // moved to after processing. 
    if (state == TouchState.DEATH)
      _touchPoints.remove(changedPoint);

    try {
      _clientProtocol.processEvents(_id, events);
    } catch (IOException e) {
      /*
       * Do nothing here. We're ignoring the error because the client will get
       * killed on the next touch point birth and we do not have a reference to
       * the client or the server from group to avoid circular references.
       */
    }
  }

}
