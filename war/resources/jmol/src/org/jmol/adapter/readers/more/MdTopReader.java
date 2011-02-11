/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-03-15 07:52:29 -0600 (Wed, 15 Mar 2006) $
 * $Revision: 4614 $
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

package org.jmol.adapter.readers.more;

import org.jmol.adapter.smarter.*;
import org.jmol.api.JmolAdapter;
import org.jmol.util.Logger;

/**
 * A reader for Amber Molecular Dynamics topology files --
 * requires subsequent COORD "xxxx.mdcrd" file 
 * 
 *<p>
 * <a href=''>
 *  
 * </a>
 * 
 * PDB note:
 * 
 * Note that topology format does not include chain designations,
 * chain terminator, chain designator, or element symbol.
 * 
 * Chains based on numbering reset just labeled A B C D .... Z a b c d .... z
 * Element symbols based on reasoned guess and properties of hetero groups
 * 
 * In principal we could use average atomic mass.
 * 
 * 
 *<p>
 */

public class MdTopReader extends ForceFieldReader {

  private int nAtoms = 0;
  private int atomCount = 0;

  protected void initializeReader() throws Exception {
    setUserAtomTypes();
  }

  protected boolean checkLine() throws Exception {
    if (line.indexOf("%FLAG ") != 0)
      return true;
    line = line.substring(6).trim();
    if (line.equals("POINTERS"))
      getPointers();
    else if (line.equals("ATOM_NAME"))
      getAtomNames();
    else if (line.equals("CHARGE"))
      getCharges();
    else if (line.equals("RESIDUE_LABEL"))
      getResidueLabels();
    else if (line.equals("RESIDUE_POINTER"))
      getResiduePointers();
    else if (line.equals("AMBER_ATOM_TYPE"))
      getAtomTypes();
    else if (line.equals("MASS"))
      getMasses();
    return false;
  }
  
  protected void finalizeReader() throws Exception {
    super.finalizeReader();
    Atom[] atoms = atomSetCollection.getAtoms();
    if (filter == null) {
      nAtoms = atomCount;
    } else {
      Atom[] atoms2 = new Atom[atoms.length];
      nAtoms = 0;
      for (int i = 0; i < atomCount; i++)
        if (filterAtom(atoms[i], i))
          atoms2[nAtoms++] = atoms[i];
      atomSetCollection.discardPreviousAtoms();
      for (int i = 0; i < nAtoms; i++) {
        Atom atom = atoms2[i];
        atomSetCollection.addAtom(atom);
      }
    }
    Logger.info("Total number of atoms used=" + nAtoms);
    int j = 0;
    for (int i = 0; i < nAtoms; i++) {
      Atom atom = atoms[i];
      if (i % 100 == 0)
        j++;
      setAtomCoord(atom, (i % 100)*2, j*2, 0);
      atom.isHetero = JmolAdapter.isHetero(atom.group3);
      String atomType = atom.atomName;
      atomType = atomType.substring(atomType.indexOf('\0') + 1);
      if (!getElementSymbol(atom, atomType))
        atom.elementSymbol = deducePdbElementSymbol(atom.isHetero, atom.atomName,
            atom.group3);
    }
    atomSetCollection.setAtomSetCollectionAuxiliaryInfo("isPDB", Boolean.TRUE);
    atomSetCollection.setAtomSetAuxiliaryInfo("isPDB", Boolean.TRUE);
  }

  private String getDataBlock() throws Exception {
    StringBuffer sb = new StringBuffer();
    while (readLine() != null && line.indexOf("%FLAG") != 0)
      sb.append(line);
    return sb.toString();
  }

  private void getMasses() throws Exception {
/*    float[] data = new float[atomCount];
    readLine();
    getTokensFloat(getDataBlock(), data, atomCount);
*/
  }

  private void getAtomTypes() throws Exception {
    readLine(); // #FORMAT
    String[] data = getTokens(getDataBlock());
    Atom[] atoms = atomSetCollection.getAtoms();
    for (int i = atomCount; --i >= 0;)  
      atoms[i].atomName += '\0' + data[i];
  }

  private void getCharges() throws Exception {
    float[] data = new float[atomCount];
    readLine(); // #FORMAT
    getTokensFloat(getDataBlock(), data, atomCount);
    Atom[] atoms = atomSetCollection.getAtoms();
    for (int i = atomCount; --i >= 0;)
      atoms[i].partialCharge = data[i];
  }

  private void getResiduePointers() throws Exception {
    readLine(); // #FORMAT
    String[] resPtrs = getTokens(getDataBlock());
    Logger.info("Total number of residues=" + resPtrs.length);
    int pt1 = atomCount;
    int pt2;
    Atom[] atoms = atomSetCollection.getAtoms();
    for (int i = resPtrs.length; --i >= 0;) {
      int ptr = pt2 = parseInt(resPtrs[i]) - 1;
      while (ptr < pt1) {
        if (group3s != null)
          atoms[ptr].group3 = group3s[i];
        atoms[ptr++].sequenceNumber = i + 1;
      }
      pt1 = pt2;
    }
  }

  String[] group3s;
  
  private void getResidueLabels() throws Exception {
    readLine(); // #FORMAT
    group3s = getTokens(getDataBlock());
  }

  private void getAtomNames() throws Exception {
    readLine(); // #FORMAT
    Atom[] atoms = atomSetCollection.getAtoms();
    int pt = 0;
    int i = 0;
    int len = 0;
    while (pt < atomCount) {
      if (i >= len) {
        readLine();
        i = 0;
        len = line.length();
      }
      atoms[pt++].atomName = line.substring(i, i + 4).trim();
      i += 4;
    }
  }

  /*
FORMAT(12i6)  NATOM,  NTYPES, NBONH,  MBONA,  NTHETH, MTHETA,
              NPHIH,  MPHIA,  NHPARM, NPARM,  NEXT,   NRES,
              NBONA,  NTHETA, NPHIA,  NUMBND, NUMANG, NPTRA,
              NATYP,  NPHB,   IFPERT, NBPER,  NGPER,  NDPER,
              MBPER,  MGPER,  MDPER,  IFBOX,  NMXRS,  IFCAP
  NATOM  : total number of atoms 
  NTYPES : total number of distinct atom types
  NBONH  : number of bonds containing hydrogen
  MBONA  : number of bonds not containing hydrogen
  NTHETH : number of angles containing hydrogen
  MTHETA : number of angles not containing hydrogen
  NPHIH  : number of dihedrals containing hydrogen
  MPHIA  : number of dihedrals not containing hydrogen
  NHPARM : currently not used
  NPARM  : currently not used
  NEXT   : number of excluded atoms
  NRES   : number of residues
  NBONA  : MBONA + number of constraint bonds
  NTHETA : MTHETA + number of constraint angles
  NPHIA  : MPHIA + number of constraint dihedrals
  NUMBND : number of unique bond types
  NUMANG : number of unique angle types
  NPTRA  : number of unique dihedral types
  NATYP  : number of atom types in parameter file, see SOLTY below
  NPHB   : number of distinct 10-12 hydrogen bond pair types
  IFPERT : set to 1 if perturbation info is to be read in
  NBPER  : number of bonds to be perturbed
  NGPER  : number of angles to be perturbed
  NDPER  : number of dihedrals to be perturbed
  MBPER  : number of bonds with atoms completely in perturbed group
  MGPER  : number of angles with atoms completely in perturbed group
  MDPER  : number of dihedrals with atoms completely in perturbed groups
  IFBOX  : set to 1 if standard periodic box, 2 when truncated octahedral
  NMXRS  : number of atoms in the largest residue
  IFCAP  : set to 1 if the CAP option from edit was specified

%FLAG POINTERS                                                                  
%FORMAT(10I8)                                                                   
   37300      16   29669    6234   12927    6917   28267    6499       0       0
   87674    9013    6234    6917    6499      47     101      41      31       1
       0       0       0       0       0       0       0       1      24       0
       0

0         1         2         3         4         5         6         7         
01234567890123456789012345678901234567890123456789012345678901234567890123456789

   */
  private void getPointers() throws Exception {
    readLine(); // #FORMAT
    String data = "";
    int pt = 0;
    while (pt++ < 3 && (line = readLine()) != null && !line.startsWith("#"))
        data += line;
    String[] tokens = getTokens(data); 
    atomCount = parseInt(tokens[0]);
    boolean isPeriodic = (tokens[27].charAt(0) != '0');
    if (isPeriodic) {
      Logger.info("Periodic type: " + tokens[27]);
      htParams.put("isPeriodic", Boolean.TRUE);
    }
    Logger.info("Total number of atoms read=" + atomCount);
    htParams.put("templateAtomCount", new Integer(atomCount));
    for (int i = 0; i < atomCount; i++) 
      atomSetCollection.addAtom(new Atom());
  }
}
