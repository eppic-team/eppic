/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2008-08-05 14:00:33 -0500 (Tue, 05 Aug 2008) $
 * $Revision: 9671 $
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

package org.jmol.shapespecial;

import java.util.BitSet;

import org.jmol.shape.AtomShape;

public class Vectors extends AtomShape {

 protected void initModelSet() {
    if (!(isActive = modelSet.modelSetHasVibrationVectors()))
      return;
    super.initModelSet();
  }

 public void setProperty(String propertyName, Object value, BitSet bsSelected) {
    if (!isActive)
      return;
    super.setProperty(propertyName, value, bsSelected);
  }
  
 public Object getProperty(String propertyName, int param) {
   if (propertyName == "mad")
     return new Integer(mads == null || param < 0 || mads.length <= param ? 0 : mads[param]);
   return super.getProperty(propertyName, param);
 }

 public String getShapeState() {
    return (isActive ? super.getShapeState() : "");
  }
}
