/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-05-13 19:17:06 -0500 (Sat, 13 May 2006) $
 * $Revision: 5114 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.quantum;

import org.jmol.api.MOCalculationInterface;
import org.jmol.api.VolumeDataInterface;
import org.jmol.util.Logger;
import org.jmol.viewer.JmolConstants;

import javax.vecmath.Point3f;

import java.util.BitSet;
import java.util.List;

/*
 * See J. Computational Chemistry, vol 7, p 359, 1986.
 * thanks go to Won Kyu Park, wkpark@chem.skku.ac.kr, 
 * jmol-developers list communication "JMOL AND CALCULATED ORBITALS !!!!"
 * and his http://chem.skku.ac.kr/~wkpark/chem/mocube.f
 * based on PSI88 http://www.ccl.net/cca/software/SOURCES/FORTRAN/psi88/index.shtml
 * http://www.ccl.net/cca/software/SOURCES/FORTRAN/psi88/src/psi1.f
 * 
 * While we are not exactly copying this code, I include here the information from that
 * FORTRAN as acknowledgment of the source of the algorithmic idea to use single 
 * row arrays to reduce the number of calculations.
 *  
 * Slater functions provided by JR Schmidt and Will Polik. Many thanks!
 * 
 * Spherical functions by Matthew Zwier <mczwier@gmail.com>
 * 
 * A neat trick here is using Java Point3f. null atoms allow selective removal of
 * their contribution to the MO. Maybe a first time this has ever been done?
 * 
 * Bob Hanson hansonr@stolaf.edu 7/3/06
 * 
 C
 C      DANIEL L. SEVERANCE
 C      WILLIAM L. JORGENSEN
 C      DEPARTMENT OF CHEMISTRY
 C      YALE UNIVERSITY
 C      NEW HAVEN, CT 06511
 C
 C      THIS CODE DERIVED FROM THE PSI1 PORTION OF THE ORIGINAL PSI77
 C      PROGRAM WRITTEN BY WILLIAM L. JORGENSEN, PURDUE.
 C      IT HAS BEEN REWRITTEN TO ADD SPEED AND BASIS FUNCTIONS. DLS
 C
 C      THE CONTOURING CODE HAS BEEN MOVED TO A SEPARATE PROGRAM TO ALLOW
 C      MULTIPLE CONTOURS TO BE PLOTTED WITHOUT RECOMPUTING THE
 C      ORBITAL VALUE MATRIX.
 C
 C Redistribution and use in source and binary forms are permitted
 C provided that the above paragraphs and this one are duplicated in 
 C all such forms and that any documentation, advertising materials,
 C and other materials related to such distribution and use acknowledge 
 C that the software was developed by Daniel Severance at Purdue University
 C The name of the University or Daniel Severance may not be used to endorse 
 C or promote products derived from this software without specific prior 
 C written permission.  The authors are now at Yale University.
 C THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 C IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 C WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

/*
 * NOTE -- THIS CLASS IS INSTANTIATED USING Interface.getOptionInterface
 * NOT DIRECTLY -- FOR MODULARIZATION. NEVER USE THE CONSTRUCTOR DIRECTLY!
 * 
 */

public class MOCalculation extends QuantumCalculation implements
    MOCalculationInterface {

  private final static double CUT = -50;
  
  // slater coefficients in Bohr
  private float[] CX, CY, CZ;

  // d-orbital partial coefficients in Bohr
  private float[] DXY, DXZ, DYZ;

  // exp(-alpha x^2...)
  private float[] EX, EY, EZ;

  private String calculationType;
  private List shells;
  private float[][] gaussians;
  //Hashtable aoOrdersDF;
  private SlaterData[] slaters;
  private float[] moCoefficients;
  private int moCoeff;
  private int gaussianPtr;
  private int firstAtomOffset;
  private boolean isElectronDensity;
  private float occupancy = 2f; //for now -- RHF only
  //private float coefMax = Integer.MAX_VALUE;
  private boolean doNormalize = true;
  //                                              S           SP            DS         DC          FS          FC
  private int[][] dfCoefMaps = new int[][] {null, new int[3], new int[4], new int[5], new int[6], new int[7], new int[10]};

//  private float[] nuclearCharges;

  protected float[][][] voxelDataTemp;

  public MOCalculation() {
  }

  public void calculate(VolumeDataInterface volumeData, BitSet bsSelected,
                        String calculationType, Point3f[] atomCoordAngstroms,
                        int firstAtomOffset, List shells,
                        float[][] gaussians, int[][] dfCoefMaps,
                        Object slaters,
                        float[] moCoefficients, float[] nuclearCharges, boolean doNormalize) {
    boolean testing = false;
    this.calculationType = calculationType;
    this.firstAtomOffset = firstAtomOffset;
    this.shells = shells;
    this.gaussians = gaussians;
    if (dfCoefMaps != null)
      this.dfCoefMaps = dfCoefMaps;
    this.slaters = (SlaterData[]) slaters;
    this.moCoefficients = moCoefficients;
    //this.nuclearCharges = nuclearCharges;
    this.isElectronDensity = (testing || nuclearCharges != null);
    this.doNormalize = doNormalize;
    
    int[] countsXYZ = volumeData.getVoxelCounts();
    initialize(countsXYZ[0], countsXYZ[1], countsXYZ[2]);
    voxelData = volumeData.getVoxelData();
    voxelDataTemp = (isElectronDensity ? new float[nX][nY][nZ] : voxelData);
    setupCoordinates(volumeData.getOriginFloat(), 
        volumeData.getVolumetricVectorLengths(), 
        bsSelected, atomCoordAngstroms);
    atomIndex = firstAtomOffset - 1;
    doDebug = (Logger.debugging);
    if (slaters == null)
      createGaussianCube();
    else
      createSlaterCube();
    if (doDebug || testing || isElectronDensity)
      calculateElectronDensity(nuclearCharges);
  }  

  protected void initialize(int nX, int nY, int nZ) {
    super.initialize(nX, nY, nZ);
    
    CX = new float[nX];
    CY = new float[nY];
    CZ = new float[nZ];

    DXY = new float[nX];
    DXZ = new float[nX];
    DYZ = new float[nY];

    EX = new float[nX];
    EY = new float[nY];
    EZ = new float[nZ];
  }

  public void calculateElectronDensity(float[] nuclearCharges) {
    //TODO
    float t = 0;
    for (int ix = nX; --ix >= 0;)
      for (int iy = nY; --iy >= 0;)
        for (int iz = nZ; --iz >= 0;) {
          float x = voxelData[ix][iy][iz];
          t += x * x;
        }
    float volume = stepBohr[0] * stepBohr[1] * stepBohr[2]; 
        // / bohr_per_angstrom / bohr_per_angstrom / bohr_per_angstrom;
    t = t * volume;
    Logger.info("Integrated density = " + t);
    //processMep(nuclearCharges);
  }

  private void createSlaterCube() {
    moCoeff = 0;
    for (int i = 0; i < slaters.length; i++) {
      if (!processSlater(i))
        break;
    }
  }

  private void createGaussianCube() {
    if (!checkCalculationType())
      return;
    check5D();
    int nShells = shells.size();
    // each STO shell is the combination of one or more gaussians
    moCoeff = 0;
    for (int i = 0; i < nShells; i++) {
      processShell(i);
      if (doDebug)
        Logger.debug("createGaussianCube shell=" + i + " moCoeff=" + moCoeff
            + "/" + moCoefficients.length);
    }
  }

  boolean as5D = false;
  /**
   * Idea here is that we skip all the atoms, just increment moCoeff,
   * and compare the number of coefficients run through to the 
   * size of the moCoefficients array. If there are more coefficients
   * than there should be, we have to assume 5D orbitals were not recognized
   * by the file loader
   * 
   */
  private void check5D() {
    int nShells = shells.size();
    // each STO shell is the combination of one or more gaussians
    moCoeff = 0;
    thisAtom = null;
    for (int i = 0; i < nShells; i++) {
      int[] shell = (int[]) shells.get(i);
      int basisType = shell[1];
      gaussianPtr = shell[2];
      nGaussians = shell[3];
      addData(basisType);
    }
    as5D = (moCoeff > moCoefficients.length);
    if (as5D)
      Logger.error("MO calculation does not have the proper number of coefficients! Assuming spherical (5D,7F) orbitals");
  }

  private boolean checkCalculationType() {
    if (calculationType == null) {
      Logger.warn("calculation type not identified -- continuing");
      return true;
    }
    /*if (calculationType.indexOf("5D") >= 0) {
     Logger
     .error("QuantumCalculation.checkCalculationType: can't read 5D basis sets yet: "
     + calculationType + " -- exit");
     return false;
     }*/
    if (calculationType.indexOf("+") >= 0 || calculationType.indexOf("*") >= 0) {
      Logger
          .warn("polarization/diffuse wavefunctions have not been tested fully: "
              + calculationType + " -- continuing");
    }
    if (calculationType.indexOf("?") >= 0) {
      Logger
          .warn("unknown calculation type may not render correctly -- continuing");
    } else {
      Logger.info("calculation type: " + calculationType + " OK.");
    }
    return true;
  }

  private int nGaussians;
  
  private void processShell(int iShell) {
    int lastAtom = atomIndex;
    int[] shell = (int[]) shells.get(iShell);
    atomIndex = shell[0] + firstAtomOffset;
    int basisType = shell[1];
    gaussianPtr = shell[2];
    nGaussians = shell[3];

    if (doDebug)
      Logger.debug("processShell: " + iShell + " type="
          + JmolConstants.getQuantumShellTag(basisType) + " nGaussians="
          + nGaussians + " atom=" + atomIndex);
    if (atomIndex != lastAtom && (thisAtom = qmAtoms[atomIndex]) != null)
      thisAtom.setXYZ(true);
    addData(basisType);
  }

  private void addData(int basisType) {
    switch (basisType) {
    case JmolConstants.SHELL_S:
      addDataS();
      break;
    case JmolConstants.SHELL_P:
      addDataP();
      break;
    case JmolConstants.SHELL_SP:
      addDataSP();
      break;
    case JmolConstants.SHELL_D_SPHERICAL:
      addData5D();
      break;
    case JmolConstants.SHELL_D_CARTESIAN:
      if (as5D)
        addData5D();
      else
        addData6D();
      break;
    case JmolConstants.SHELL_F_SPHERICAL:
      addData7F();
      break;
    case JmolConstants.SHELL_F_CARTESIAN:
      if (as5D)
        addData7F();
      else        
        addData10F();
      break;
    default:
      Logger.warn(" Unsupported basis type for atomno=" + (atomIndex + 1) + ": " + basisType);
      break;
    }
  }
  
  private void setTemp() {
    for (int ix = xMax; --ix >= xMin;) {
      for (int iy = yMax; --iy >= yMin;) {
        for (int iz = zMax; --iz >= zMin;) {
          float value = voxelDataTemp[ix][iy][iz];
          voxelData[ix][iy][iz] += value * value * occupancy;
          voxelDataTemp[ix][iy][iz] = 0;
        }
      }
    }
  }
  
  private void addDataS() {
    if (thisAtom == null) {
      moCoeff++;
      return;
    }
    if (doDebug)
      dumpInfo("S ");

    float m1 = moCoefficients[moCoeff++];
    for (int ig = 0; ig < nGaussians; ig++) {
      float alpha = gaussians[gaussianPtr + ig][0];
      float c1 = gaussians[gaussianPtr + ig][1];
      // (2 alpha^3/pi^3)^0.25 exp(-alpha r^2)
      float a = m1 * c1;
      if (doNormalize)
        a *= (float) Math.pow(alpha, 0.75) * 0.712705470f;
      // the coefficients are all included with the X factor here

      for (int i = xMax; --i >= xMin;) {
        EX[i] = a * (float) Math.exp(-X2[i] * alpha);
      }
      for (int i = yMax; --i >= yMin;) {
        EY[i] = (float) Math.exp(-Y2[i] * alpha);
      }
      for (int i = zMax; --i >= zMin;) {
        EZ[i] = (float) Math.exp(-Z2[i] * alpha);
      }

      for (int ix = xMax; --ix >= xMin;) {
        float eX = EX[ix];
        for (int iy = yMax; --iy >= yMin;) {
          float eXY = eX * EY[iy];
          for (int iz = zMax; --iz >= zMin;) {
            voxelDataTemp[ix][iy][iz] += eXY * EZ[iz];
          }
        }
      }
    }
    if (isElectronDensity)
      setTemp();
  }

  private void addDataP() {
    int[] map = dfCoefMaps[JmolConstants.SHELL_P];
    if (thisAtom == null || map[0] == Integer.MIN_VALUE) {
      moCoeff += 3;
      return;
    }
    if (doDebug)
      dumpInfo(JmolConstants.SHELL_P, map);
    float mx = moCoefficients[map[0] + moCoeff++];
    float my = moCoefficients[map[1] + moCoeff++];
    float mz = moCoefficients[map[2] + moCoeff++];
    if (isElectronDensity) {
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c1 = gaussians[gaussianPtr + ig][1];
        // (128 alpha^5/pi^3)^0.25 [x|y|z]exp(-alpha r^2)
        float a = c1;
        if (doNormalize)
          a *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, a * mx, 0, 0);
      }
      setTemp();
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c1 = gaussians[gaussianPtr + ig][1];
        // (128 alpha^5/pi^3)^0.25 [x|y|z]exp(-alpha r^2)
        float a = c1;
        if (doNormalize)
          a *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, 0, a * my, 0);
      }
      setTemp();
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c1 = gaussians[gaussianPtr + ig][1];
        // (128 alpha^5/pi^3)^0.25 [x|y|z]exp(-alpha r^2)
        float a = c1;
        if (doNormalize)
          a *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, 0, 0, a * mz);
      }
      setTemp();
    } else {
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c1 = gaussians[gaussianPtr + ig][1];
        // (128 alpha^5/pi^3)^0.25 [x|y|z]exp(-alpha r^2)
        float a = c1;
        if (doNormalize)
          a *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, a * mx, a * my, a * mz);
      }
    }
  }

  private void addDataSP() {
    // spartan uses format "1" for BOTH SP and P, which is fine, but then
    // when c1 = 0, there is no mo coefficient, of course. 
    float c1 = gaussians[gaussianPtr][1];
    int[] map = dfCoefMaps[JmolConstants.SHELL_SP];
    if (thisAtom == null || map[0] == Integer.MIN_VALUE) {
      moCoeff += 3;
      return;
    }
    if (thisAtom == null) {
      moCoeff += (c1 == 0 ? 3 : 4);
      return;
    }
    float ms, mx, my, mz;
    if (c1 == 0) {
      if (doDebug)
        dumpInfo("X Y Z ");
      ms = 0;      
    } else {
      if (doDebug)
        dumpInfo(JmolConstants.SHELL_SP, map);
      ms = moCoefficients[map[0] + moCoeff++];
    }
    mx = moCoefficients[map[1] + moCoeff++];
    my = moCoefficients[map[2] + moCoeff++];
    mz = moCoefficients[map[3] + moCoeff++];
    if (isElectronDensity) {
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        c1 = gaussians[gaussianPtr + ig][1];
        float a1 = c1;
        if (doNormalize)
          a1 *= (float) Math.pow(alpha, 0.75) * 0.712705470f;
        calcSP(alpha, a1 * ms, 0, 0, 0);
      }
      setTemp();
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c2 = gaussians[gaussianPtr + ig][2];
        float a2 = c2;
        if (doNormalize)
          a2 *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, a2 * mx, 0, 0);
      }
      setTemp();
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c2 = gaussians[gaussianPtr + ig][2];
        float a2 = c2;
        if (doNormalize)
          a2 *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, 0, a2 * my, 0);
      }
      setTemp();
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        float c2 = gaussians[gaussianPtr + ig][2];
        float a2 = c2;
        if (doNormalize)
          a2 *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, 0, 0, 0, a2 * mz);
      }
      setTemp();
    } else {
      for (int ig = 0; ig < nGaussians; ig++) {
        float alpha = gaussians[gaussianPtr + ig][0];
        c1 = gaussians[gaussianPtr + ig][1];
        float c2 = gaussians[gaussianPtr + ig][2];
        float a1 = c1;
        if (doNormalize)
          a1 *= (float) Math.pow(alpha, 0.75) * 0.712705470f;
        float a2 = c2;
        if (doNormalize)
          a2 *= (float) Math.pow(alpha, 1.25) * 1.42541094f;
        calcSP(alpha, a1 * ms, a2 * mx, a2 * my, a2 * mz);
      }
    }
  }

  private void setCE(float[] CX, float[] EX, float alpha, float as, float ax,
                     float ay, float az) {
    for (int i = xMax; --i >= xMin;) {
      CX[i] = as + ax * X[i];
      EX[i] = (float) Math.exp(-X2[i] * alpha);
    }
    for (int i = yMax; --i >= yMin;) {
      CY[i] = ay * Y[i];
      EY[i] = (float) Math.exp(-Y2[i] * alpha);
    }
    for (int i = zMax; --i >= zMin;) {
      CZ[i] = az * Z[i];
      EZ[i] = (float) Math.exp(-Z2[i] * alpha);
    }
  }

  private void setE(float[] EX, float alpha) {
    for (int i = xMax; --i >= xMin;)
      EX[i] = (float) Math.exp(-X2[i] * alpha);
    for (int i = yMax; --i >= yMin;)
      EY[i] = (float) Math.exp(-Y2[i] * alpha);
    for (int i = zMax; --i >= zMin;)
      EZ[i] = (float) Math.exp(-Z2[i] * alpha);
  }

  private void calcSP(float alpha, float as, float ax, float ay, float az) {
    setCE(CX, EX, alpha, as, ax, ay, az);
    for (int ix = xMax; --ix >= xMin;) {
      float eX = EX[ix];
      float cX = CX[ix];
      for (int iy = yMax; --iy >= yMin;) {
        float eXY = eX * EY[iy];
        float cXY = cX + CY[iy];
        for (int iz = zMax; --iz >= zMin;) {
          voxelDataTemp[ix][iy][iz] += (cXY + CZ[iz]) * eXY * EZ[iz];
        }
      }
    }
  }

  private final static float ROOT3 = 1.73205080756887729f;

  private void addData6D() {
    //expects 6 orbitals in the order XX YY ZZ XY XZ YZ
    int[] map = dfCoefMaps[JmolConstants.SHELL_D_CARTESIAN];
    if (thisAtom == null || isElectronDensity || map[0] == Integer.MIN_VALUE) {
      moCoeff += 6;
      return;
    }
    if (doDebug)
      dumpInfo(JmolConstants.SHELL_D_CARTESIAN, map);
    float mxx = moCoefficients[map[0] + moCoeff++];
    float myy = moCoefficients[map[1] + moCoeff++];
    float mzz = moCoefficients[map[2] + moCoeff++];
    float mxy = moCoefficients[map[3] + moCoeff++];
    float mxz = moCoefficients[map[4] + moCoeff++];
    float myz = moCoefficients[map[5] + moCoeff++];
    for (int ig = 0; ig < nGaussians; ig++) {
      float alpha = gaussians[gaussianPtr + ig][0];
      float c1 = gaussians[gaussianPtr + ig][1];
      // xx|yy|zz: (2048 alpha^7/9pi^3)^0.25 [xx|yy|zz]exp(-alpha r^2)
      // xy|xz|yz: (2048 alpha^7/pi^3)^0.25 [xy|xz|yz]exp(-alpha r^2)
      float a = c1;
      if (doNormalize)
        a *= (float) Math.pow(alpha, 1.75) * 2.8508219178923f;
      float axx = a / ROOT3 * mxx;
      float ayy = a / ROOT3 * myy;
      float azz = a / ROOT3 * mzz;
      float axy = a * mxy;
      float axz = a * mxz;
      float ayz = a * myz;
      setCE(CX, EX, alpha, 0, axx, ayy, azz);

      for (int i = xMax; --i >= xMin;) {
        DXY[i] = axy * X[i];
        DXZ[i] = axz * X[i];
      }
      for (int i = yMax; --i >= yMin;) {
        DYZ[i] = ayz * Y[i];
      }
      for (int ix = xMax; --ix >= xMin;) {
        float axx_x2 = CX[ix] * X[ix];
        float axy_x = DXY[ix];
        float axz_x = DXZ[ix];
        float eX = EX[ix];
        for (int iy = yMax; --iy >= yMin;) {
          float axx_x2__ayy_y2__axy_xy = axx_x2 + (CY[iy] + axy_x) * Y[iy];
          float axz_x__ayz_y = axz_x + DYZ[iy];
          float eXY = eX * EY[iy];
          for (int iz = zMax; --iz >= zMin;) {
            voxelDataTemp[ix][iy][iz] += (axx_x2__ayy_y2__axy_xy + (CZ[iz] + axz_x__ayz_y) * Z[iz])
                * eXY * EZ[iz];
            // giving (axx_x2 + ayy_y2 + azz_z2 + axy_xy + axz_xz + ayz_yz)e^-br2; 
          }
        }
      }
    }
  }

  private void addData5D() {
    // expects 5 real orbitals in the order d0, d+1, d-1, d+2, d-2
    // (i.e. dz^2, dxz, dyz, dx^2-y^2, dxy)
    // To avoid actually having to use spherical harmonics, we use 
    // linear combinations of Cartesian harmonics.  

    // For conversions between spherical and Cartesian gaussians, see
    // "Trasnformation Between Cartesian and Pure Spherical Harmonic Gaussians",
    // Schelgel and Frisch, Int. J. Quant. Chem 54, 83-87, 1995

    int[] map = dfCoefMaps[JmolConstants.SHELL_D_SPHERICAL];
    if (thisAtom == null || isElectronDensity || map[0] == Integer.MIN_VALUE) {
      moCoeff += 5;
      return;
    }
    if (doDebug)
      dumpInfo(JmolConstants.SHELL_D_SPHERICAL, map);

    float alpha, c1, a;
    float x, y, z;
    float cxx, cyy, czz, cxy, cxz, cyz;
    float ad0, ad1p, ad1n, ad2p, ad2n;

    /*
     Cartesian forms for d (l = 2) basis functions:
     Type         Normalization
     xx           [(2048 * alpha^7) / (9 * pi^3))]^(1/4)
     xy           [(2048 * alpha^7) / (1 * pi^3))]^(1/4)
     xz           [(2048 * alpha^7) / (1 * pi^3))]^(1/4)
     yy           [(2048 * alpha^7) / (9 * pi^3))]^(1/4)
     yz           [(2048 * alpha^7) / (1 * pi^3))]^(1/4)
     zz           [(2048 * alpha^7) / (9 * pi^3))]^(1/4)
     */

    final float norm1 =(doNormalize ? (float) Math.pow(2048.0 / (Math.PI * Math.PI * Math.PI), 0.25) : 1);
    final float norm2 = (doNormalize ? (float) (norm1 / Math.sqrt(3)) : 1);

    // Normalization constant that shows up for dx^2-y^2
    final float root34 =(doNormalize ? (float) Math.sqrt(0.75) : 1);

    float m0 = moCoefficients[map[0] + moCoeff++];
    float m1p = moCoefficients[map[1] + moCoeff++];
    float m1n = moCoefficients[map[2] + moCoeff++];
    float m2p = moCoefficients[map[3] + moCoeff++];
    float m2n = moCoefficients[map[4] + moCoeff++];
    
    for (int ig = 0; ig < nGaussians; ig++) {
      alpha = gaussians[gaussianPtr + ig][0];
      c1 = gaussians[gaussianPtr + ig][1];
      a = c1;
      if (doNormalize)
        a *= (float) Math.pow(alpha, 1.75);

      ad0 = a * m0;
      ad1p = a * m1p;
      ad1n = a * m1n;
      ad2p = a * m2p;
      ad2n = a * m2n;

      setE(EX, alpha);

      for (int ix = xMax; --ix >= xMin;) {
        x = X[ix];
        float eX = EX[ix];
        cxx = norm2 * x * x;

        for (int iy = yMax; --iy >= yMin;) {
          y = Y[iy];
          float eXY = eX * EY[iy];

          cyy = norm2 * y * y;
          cxy = norm1 * x * y;

          for (int iz = zMax; --iz >= zMin;) {
            z = Z[iz];

            czz = norm2 * z * z;
            cxz = norm1 * x * z;
            cyz = norm1 * y * z;

            voxelDataTemp[ix][iy][iz] += (ad0 * (czz - 0.5f * (cxx + cyy)) + ad1p * cxz + ad1n
                * cyz + ad2p * root34 * (cxx - cyy) + ad2n * cxy)
                * eXY * EZ[iz];
          }
        }
      }
    }
  }

  private void addData10F() {
    // expects 10 orbitals in the order XXX, YYY, ZZZ, XYY, XXY, 
    //                                  XXZ, XZZ, YZZ, YYZ, XYZ
    int[] map = dfCoefMaps[JmolConstants.SHELL_F_CARTESIAN];
    if (thisAtom == null || isElectronDensity || map[0] == Integer.MIN_VALUE) {
      moCoeff += 10;
      return;
    }
    if (doDebug)
      dumpInfo(JmolConstants.SHELL_F_CARTESIAN, map);
    float alpha;
    float c1;
    float a;
    float x, y, z, xx, yy, zz;
    float axxx, ayyy, azzz, axyy, axxy, axxz, axzz, ayzz, ayyz, axyz;
    float cxxx, cyyy, czzz, cxyy, cxxy, cxxz, cxzz, cyzz, cyyz, cxyz;

    /*
     Cartesian forms for f (l = 3) basis functions:
     Type         Normalization
     xxx          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     xxy          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xxz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xyy          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xyz          [(32768 * alpha^9) / (1 * pi^3))]^(1/4)
     xzz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     yyy          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     yyz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     yzz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     zzz          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     */

    final float norm1 =(doNormalize ?  (float) Math.pow(
        32768.0 / (Math.PI * Math.PI * Math.PI), 0.25) : 1);
    final float norm2 =(doNormalize ?  (float) (norm1 / Math.sqrt(3)) : 1);
    final float norm3 =(doNormalize ?  (float) (norm1 / Math.sqrt(15)) : 1);

    float mxxx = moCoefficients[map[0] + moCoeff++];
    float myyy = moCoefficients[map[1] + moCoeff++];
    float mzzz = moCoefficients[map[2] + moCoeff++];
    float mxyy = moCoefficients[map[3] + moCoeff++];
    float mxxy = moCoefficients[map[4] + moCoeff++];
    float mxxz = moCoefficients[map[5] + moCoeff++];
    float mxzz = moCoefficients[map[6] + moCoeff++];
    float myzz = moCoefficients[map[7] + moCoeff++];
    float myyz = moCoefficients[map[8] + moCoeff++];
    float mxyz = moCoefficients[map[9] + moCoeff++];
    for (int ig = 0; ig < nGaussians; ig++) {
      alpha = gaussians[gaussianPtr + ig][0];
      c1 = gaussians[gaussianPtr + ig][1];
      setE(EX, alpha);

      // common factor of contraction coefficient and alpha normalization 
      // factor; only call pow once per primitive
      a = c1;
      if (doNormalize)
        a *= (float) Math.pow(alpha, 2.25);

      axxx = a * norm3 * mxxx;
      ayyy = a * norm3 * myyy;
      azzz = a * norm3 * mzzz;
      axyy = a * norm2 * mxyy;
      axxy = a * norm2 * mxxy;
      axxz = a * norm2 * mxxz;
      axzz = a * norm2 * mxzz;
      ayzz = a * norm2 * myzz;
      ayyz = a * norm2 * myyz;
      axyz = a * norm1 * mxyz;

      for (int ix = xMax; --ix >= xMin;) {
        x = X[ix];
        xx = x * x;

        float Ex = EX[ix];
        cxxx = axxx * xx * x;

        for (int iy = yMax; --iy >= yMin;) {
          y = Y[iy];
          yy = y * y;
          float Exy = Ex * EY[iy];
          cyyy = ayyy * yy * y;
          cxxy = axxy * xx * y;
          cxyy = axyy * x * yy;

          for (int iz = zMax; --iz >= zMin;) {
            z = Z[iz];
            zz = z * z;
            czzz = azzz * zz * z;
            cxxz = axxz * xx * z;
            cxzz = axzz * x * zz;
            cyyz = ayyz * yy * z;
            cyzz = ayzz * y * zz;
            cxyz = axyz * x * y * z;
            voxelDataTemp[ix][iy][iz] +=  (cxxx + cyyy + czzz + cxyy + cxxy + cxxz + cxzz + cyzz
                + cyyz + cxyz)
                * Exy * EZ[iz];
          }
        }
      }
    }
  }

  private void addData7F() {
    // expects 7 real orbitals in the order f0, f+1, f-1, f+2, f-2, f+3, f-3

    int[] map = dfCoefMaps[JmolConstants.SHELL_F_SPHERICAL];
    if (thisAtom == null || isElectronDensity || map[0] == Integer.MIN_VALUE) {
      moCoeff += 7;
      return;
    }

    if (doDebug)
      dumpInfo(JmolConstants.SHELL_F_SPHERICAL, map);

    float alpha, c1, a;
    float x, y, z, xx, yy, zz;
    float cxxx, cyyy, czzz, cxyy, cxxy, cxxz, cxzz, cyzz, cyyz, cxyz;
    float af0, af1p, af1n, af2p, af2n, af3p, af3n;
    float f0, f1p, f1n, f2p, f2n, f3p, f3n;
    /*
     Cartesian forms for f (l = 3) basis functions:
     Type         Normalization
     xxx          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     xxy          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xxz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xyy          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     xyz          [(32768 * alpha^9) / (1 * pi^3))]^(1/4)
     xzz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     yyy          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     yyz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     yzz          [(32768 * alpha^9) / (9 * pi^3))]^(1/4)
     zzz          [(32768 * alpha^9) / (225 * pi^3))]^(1/4)
     */

    final float norm1 = (doNormalize ?  (float) Math.pow(
        32768.0 / (Math.PI * Math.PI * Math.PI), 0.25) : 1);
    final float norm2 = (doNormalize ?  (float) (norm1 / Math.sqrt(3)) : 1);
    final float norm3 = (doNormalize ? (float) (norm1 / Math.sqrt(15)) : 1);

    // Linear combination coefficients for the various Cartesian gaussians
    final float c0_xxz_yyz = (float) (3.0 / (2.0 * Math.sqrt(5)));

    final float c1p_xzz = (float) Math.sqrt(6.0 / 5.0);
    final float c1p_xxx = (float) Math.sqrt(3.0 / 8.0);
    final float c1p_xyy = (float) Math.sqrt(3.0 / 40.0);
    final float c1n_yzz = c1p_xzz;
    final float c1n_yyy = c1p_xxx;
    final float c1n_xxy = c1p_xyy;

    final float c2p_xxz_yyz = (float) Math.sqrt(3.0 / 4.0);

    final float c3p_xxx = (float) Math.sqrt(5.0 / 8.0);
    final float c3p_xyy = 0.75f * (float) Math.sqrt(2);
    final float c3n_yyy = c3p_xxx;
    final float c3n_xxy = c3p_xyy;

    float m0 = moCoefficients[map[0] + moCoeff++];
    float m1p = moCoefficients[map[1] + moCoeff++];
    float m1n = moCoefficients[map[2] + moCoeff++];
    float m2p = moCoefficients[map[3] + moCoeff++];
    float m2n = moCoefficients[map[4] + moCoeff++];
    float m3p = moCoefficients[map[5] + moCoeff++];
    float m3n = moCoefficients[map[6] + moCoeff++];

    for (int ig = 0; ig < nGaussians; ig++) {
      alpha = gaussians[gaussianPtr + ig][0];
      c1 = gaussians[gaussianPtr + ig][1];
      a = c1;
      if (doNormalize)
        a *= (float) Math.pow(alpha, 2.25);

      af0 = a * m0;
      af1p = a * m1p;
      af1n = a * m1n;
      af2p = a * m2p;
      af2n = a * m2n;
      af3p = a * m3p;
      af3n = a * m3n;

      setE(EX, alpha);

      for (int ix = xMax; --ix >= xMin;) {
        x = X[ix];
        xx = x * x;
        float eX = EX[ix];
        cxxx = norm3 * x * xx;
        for (int iy = yMax; --iy >= yMin;) {
          y = Y[iy];
          yy = y * y;
          float eXY = eX * EY[iy];

          cyyy = norm3 * y * yy;
          cxyy = norm2 * x * yy;
          cxxy = norm2 * xx * y;

          for (int iz = zMax; --iz >= zMin;) {
            z = Z[iz];
            zz = z * z;

            czzz = norm3 * z * zz;
            cxxz = norm2 * xx * z;
            cxzz = norm2 * x * zz;
            cyyz = norm2 * yy * z;
            cyzz = norm2 * y * zz;
            cxyz = norm1 * x * y * z;

            f0 = af0 * (czzz - c0_xxz_yyz * (cxxz + cyyz));
            f1p = af1p * (c1p_xzz * cxzz - c1p_xxx * cxxx - c1p_xyy * cxyy);
            f1n = af1n * (c1n_yzz * cyzz - c1n_yyy * cyyy - c1n_xxy * cxxy);
            f2p = af2p * (c2p_xxz_yyz * (cxxz - cyyz));
            f2n = af2n * cxyz;
            f3p = af3p * (c3p_xxx * cxxx - c3p_xyy * cxyy);
            f3n = af3n * (-c3n_yyy * cyyy + c3n_xxy * cxxy);
            voxelDataTemp[ix][iy][iz] += (f0 + f1p + f1n + f2p + f2n + f3p + f3n) 
                * eXY * EZ[iz];
          }
        }
      }
    }
  }

  private boolean processSlater(int slaterIndex) {
    /*
     * We have two data structures for each slater, using the WebMO format: 
     * 
     * int[] slaterInfo[] = {iatom, a, b, c, d}
     * float[] slaterData[] = {zeta, coef}
     * 
     * where
     * 
     *  psi = (coef)(x^a)(y^b)(z^c)(r^d)exp(-zeta*r)
     * 
     * except: a == -2 ==> z^2 ==> (coef)(2z^2-x^2-y^2)(r^d)exp(-zeta*r)
     *    and: b == -2 ==> (coef)(x^2-y^2)(r^d)exp(-zeta*r)
     *    
     *    NOTE: A negative zeta means this is contracted!
     */

    int lastAtom = atomIndex;
    SlaterData slater = slaters[slaterIndex];
    atomIndex = slater.iAtom;
    //System.out.println("MOCALC SLATER " + slaterIndex + " " + lastAtom + " " + atomIndex);
    double minuszeta = -slater.zeta;
    if ((thisAtom = qmAtoms[atomIndex]) == null) {
      if (minuszeta <= 0)
        moCoeff++;
      return true;
    }
    if (minuszeta > 0) { //this is contracted; use previous moCoeff
      minuszeta = -minuszeta;
      moCoeff--;
    }
    if (moCoeff >= moCoefficients.length)
      return false;
    double coef = slater.coef * moCoefficients[moCoeff++];
    //coefMax = 0.2f;
    if (coef == 0) { //|| coefMax != Integer.MAX_VALUE && Math.abs(coef) > coefMax) {
      atomIndex = -1;
      return true;
    }
    if (atomIndex != lastAtom)
      thisAtom.setXYZ(true);
    int a = slater.x;
    int b = slater.y;
    int c = slater.z;
    int d = slater.r;
    //  System.out.println("MOCALC " + slaterIndex + " atomNo=" + (atomIndex+1) + "\tx^" + a + " y^"+ b + " z^" + c + " r^" + d + "\tzeta=" + (-minuszeta) + "\tcoef=" + coef);
          //+ " minmax " + xMin + " " + xMax + " " + yMin + " " + yMax + " " + zMin + " " + zMax
    if (a == -2) /* if dz2 */
      for (int ix = xMax; --ix >= xMin;) {
        double dx2 = X2[ix];
        for (int iy = yMax; --iy >= yMin;) {
          double dy2 = Y2[iy];
          double dx2y2 = dx2 + dy2;
          for (int iz = zMax; --iz >= zMin;) {
            double dz2 = Z2[iz];
            double r2 = dx2y2 + dz2;
            double r = Math.sqrt(r2);
            double exponent = minuszeta * r;
            if (exponent < CUT)
              continue;
            double value = (coef * Math.exp(exponent)
                * (3 * dz2 - r2));
            switch(d) {
            case 3:
              value *= r;
              //fall through
            case 2:
              value *= r2;
              break;
            case 1:
              value *= r;
              break;
            }
            voxelDataTemp[ix][iy][iz] += value;
          }
        }
      }
    else if (b == -2) /* if dx2-dy2 */
      for (int ix = xMax; --ix >= xMin;) {
        double dx2 = X2[ix];
        for (int iy = yMax; --iy >= yMin;) {
          double dy2 = Y2[iy];
          double dx2y2 = dx2 + dy2;
          double dx2my2 = coef * (dx2 - dy2);
          for (int iz = zMax; --iz >= zMin;) {
            double dz2 = Z2[iz];
            double r2 = dx2y2 + dz2;
            double r = Math.sqrt(r2);
            double exponent = minuszeta * r;
            if (exponent < CUT)
              continue;
            double value = dx2my2 * Math.exp(exponent);
            switch(d) {
            case 3:
              value *= r;
              //fall through
            case 2:
              value *= r2;
              break;
            case 1:
              value *= r;
              break;
            }
            voxelDataTemp[ix][iy][iz] += value;
          }
        }
      }
    else
      /* everything else */
      for (int ix = xMax; --ix >= xMin;) {
        double dx2 = X2[ix];
        double vdx = coef;
        switch(a) {
        case 3:
          vdx *= X[ix];
          //fall through
        case 2:
          vdx *= dx2;
          break;
        case 1:
          vdx *= X[ix];
          break;
        }
        for (int iy = yMax; --iy >= yMin;) {
          double dy2 = Y2[iy];
          double dx2y2 = dx2 + dy2;
          double vdy = vdx;
          switch(b) {
          case 3:
            vdy *= Y[iy];
            //fall through
          case 2:
            vdy *= dy2;
            break;
          case 1:
            vdy *= Y[iy];
            break;
          }
          for (int iz = zMax; --iz >= zMin;) {
            double dz2 = Z2[iz];
            double r2 = dx2y2 + dz2;
            double r = Math.sqrt(r2);
            double exponent = minuszeta * r;
            if (exponent < CUT)
              continue;
            double value = vdy * Math.exp(exponent);
            switch(c) {
            case 3:
              value *= Z[iz];
              //fall through
            case 2:
              value *= dz2;
              break;
            case 1:
              value *= Z[iz];
              break;
            }
            switch(d) {
            case 3:
              value *= r;
              //fall through
            case 2:
              value *= r2;
              break;
            case 1:
              value *= r;
              break;
            }
            voxelDataTemp[ix][iy][iz] += value;
            //if (ix == 27 && iy == 27)
              //System.out.println(iz + "\t"  
                //  + xBohr[ix] + " " + yBohr[iy] + " " 
                //  +  zBohr[iz] + "\t" 
                //  + X[ix] + " " + Y[iy] + " " + Z[iz] 
                //  + "--  r=" + r + " v=" + value + "\t" + voxelDataTemp[ix][iy][iz]);
             
            

          }
        }
      }
    
/*    for (int iz = 0; iz < 55; iz++) {
      System.out.println(iz + "\t"  
          //  + xBohr[ix] + " " + yBohr[iy] + " " 
            +  zBohr[iz] + "\t" 
          //  + X[ix] + " " + Y[iy] + " " + Z[iz] 
            + "\t"  + voxelDataTemp[27][27][iz]);
    }
*/ 
    if (isElectronDensity)
      setTemp();
    return true;
  }

  private void dumpInfo(String info) {
    for (int ig = 0; ig < nGaussians; ig++) {
      float alpha = gaussians[gaussianPtr + ig][0];
      float c1 = gaussians[gaussianPtr + ig][1];
      if (Logger.debugging) {
        Logger.debug("Gaussian " + (ig + 1) + " alpha=" + alpha + " c=" + c1);
      }
    }
    int n = info.length() / 2;
    if (Logger.debugging) {
      for (int i = 0; i < n; i++)
        Logger.debug("MO coeff " + info.substring(2 * i, 2 * i + 2) + " "
            + (moCoeff + i + 1) + " " + moCoefficients[moCoeff + i]);
    }
    return;
  }

  private void dumpInfo(int shell, int[] map) {
    for (int ig = 0; ig < nGaussians; ig++) {
      float alpha = gaussians[gaussianPtr + ig][0];
      float c1 = gaussians[gaussianPtr + ig][1];
      Logger.debug("Gaussian " + (ig + 1) + " alpha=" + alpha + " c=" + c1);
    }
    if (shell >= 0 && Logger.debugging) {
      String[] so = JmolConstants.getShellOrder(shell);
      for (int i = 0; i < so.length; i++)
        Logger.debug(thisAtom + " MO coeff " + (so == null ? "?" + i  : so[i]) + " " + (map[i] + moCoeff + i + 1) + " "
            + moCoefficients[map[i] + moCoeff + i]);
    }
  }

}
