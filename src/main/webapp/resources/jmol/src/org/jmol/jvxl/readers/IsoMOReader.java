/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-03-30 11:40:16 -0500 (Fri, 30 Mar 2007) $
 * $Revision: 7273 $
 *
 * Copyright (C) 2007 Miguel, Bob, Jmol Development
 *
 * Contact: hansonr@stolaf.edu
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.jvxl.readers;

import java.util.Hashtable;
import java.util.List;

import org.jmol.util.Logger;
import org.jmol.util.TextFormat;
import org.jmol.api.Interface;
import org.jmol.api.MOCalculationInterface;

class IsoMOReader extends AtomDataReader {

  IsoMOReader(SurfaceGenerator sg) {
    super(sg);
  }
  
  /////// ab initio/semiempirical quantum mechanical orbitals ///////

  protected void setup() {
    super.setup();
    doAddHydrogens = false;
    getAtoms(params.qm_marginAngstroms, true, false);
    setHeader("MO", "calculation type: " + params.moData.get("calculationType"));
    setRangesAndAddAtoms(params.qm_ptsPerAngstrom, params.qm_gridMax, myAtomCount);
    for (int i = params.title.length; --i >= 0;)
      fixTitleLine(i, params.mo);
  }
  
  private void fixTitleLine(int iLine, Hashtable mo) {
    // see Parameters.Java for defaults here. 
    if (!fixTitleLine(iLine))
       return;
    String line = params.title[iLine];
    int pt = line.indexOf("%");
    if (line.length() == 0 || pt < 0)
      return;
    int rep = 0;
    if (line.indexOf("%F") >= 0)
      line = TextFormat.formatString(line, "F", params.fileName);
    if (line.indexOf("%I") >= 0)
      line = TextFormat.formatString(line, "I", "" + params.qm_moNumber);
    if (line.indexOf("%N") >= 0)
      line = TextFormat.formatString(line, "N", "" + params.qmOrbitalCount);
    if (line.indexOf("%E") >= 0)
      line = TextFormat.formatString(line, "E", mo.containsKey("energy") && ++rep != 0 ? "" + mo.get("energy") : "");
    if (line.indexOf("%U") >= 0)
      line = TextFormat.formatString(line, "U", params.moData.containsKey("energyUnits") && ++rep != 0 ? (String) params.moData.get("energyUnits") : "");
    if (line.indexOf("%S") >= 0)
      line = TextFormat.formatString(line, "S", mo.containsKey("symmetry") && ++rep != 0 ? "" + mo.get("symmetry") : "");
    if (line.indexOf("%O") >= 0)
      line = TextFormat.formatString(line, "O", mo.containsKey("occupancy") && ++rep != 0  ? "" + mo.get("occupancy") : "");
    if (line.indexOf("%T") >= 0)
      line = TextFormat.formatString(line, "T", mo.containsKey("type") && ++rep != 0  ? "" + mo.get("type") : "");
    boolean isOptional = (line.indexOf("?") == 0);
    params.title[iLine] = (!isOptional ? line : rep > 0 && !line.trim().endsWith("=") ? line.substring(1) : "");
  }
  
  protected void generateCube() {
    volumeData.voxelData = voxelData = new float[nPointsX][nPointsY][nPointsZ];
    MOCalculationInterface q = (MOCalculationInterface) Interface.getOptionInterface("quantum.MOCalculation");
    Hashtable moData = params.moData;
    float[] coef = params.moCoefficients; 
    int[][] dfCoefMaps = params.dfCoefMaps;

    if (coef == null) {
      // electron density calc
      List mos = (List) (moData.get("mos"));
      if (mos == null)
        return;
      for (int i = params.qm_moNumber; --i >= 0; ) {
        Logger.info(" generating isosurface data for MO " + (i + 1));
        Hashtable mo = (Hashtable) mos.get(i);
        coef = (float[]) mo.get("coefficients");
        dfCoefMaps = (int[][]) mo.get("dfCoefMaps");
        getData(q, moData, coef, dfCoefMaps, params.theProperty);
      }
    } else {
      Logger.info("generating isosurface data for MO using cutoff " + params.cutoff);
      getData(q, moData, coef, dfCoefMaps, null);
    }
  }
  
  private void getData(MOCalculationInterface q, Hashtable moData,
                       float[] coef, int[][] dfCoefMaps, float[] nuclearCharges) {
    switch (params.qmOrbitalType) {
    case Parameters.QM_TYPE_GAUSSIAN:
      q.calculate(volumeData, bsMySelected, (String) moData
          .get("calculationType"), atomData.atomXyz, atomData.firstAtomIndex,
          (List) moData.get("shells"), (float[][]) moData.get("gaussians"),
          dfCoefMaps, null, coef,
          nuclearCharges, moData.get("isNormalized") == null);
      break;
    case Parameters.QM_TYPE_SLATER:
      q.calculate(volumeData, bsMySelected, (String) moData
          .get("calculationType"), atomData.atomXyz, atomData.firstAtomIndex,
          null, null, null, moData.get("slaters"), coef, nuclearCharges, true);
      break;
    default:
    }

  }
}
