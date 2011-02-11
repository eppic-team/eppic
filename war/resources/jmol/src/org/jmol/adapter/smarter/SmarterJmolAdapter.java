/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-01-24 06:57:01 -0600 (Mon, 24 Jan 2011) $
 * $Revision: 15033 $
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

package org.jmol.adapter.smarter;

import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolFileReaderInterface;
import org.jmol.util.CompoundDocument;
import org.jmol.util.Logger;
import org.jmol.util.TextFormat;
import org.jmol.util.ZipUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SmarterJmolAdapter extends JmolAdapter {

  public SmarterJmolAdapter() {
    super("SmarterJmolAdapter");
  }

  /**************************************************************
   * 
   * AtomSetCollectionReader.readData() will close any BufferedReader
   *  
   * **************************************************************/

  public final static String PATH_KEY = ".PATH";
  public final static String PATH_SEPARATOR =
    System.getProperty("path.separator");

  /**
   * Just get the resolved file type; if a file, does NOT close the reader
   * 
   * @param atomSetCollectionOrReader 
   * @return a file type or null
   * 
   */
  public String getFileTypeName(Object atomSetCollectionOrReader) {
    if (atomSetCollectionOrReader instanceof AtomSetCollection)
      return ((AtomSetCollection)atomSetCollectionOrReader).getFileTypeName();
    if (atomSetCollectionOrReader instanceof BufferedReader)
      return Resolver.getFileType((BufferedReader)atomSetCollectionOrReader);
    return null;
  }

  /**
   * The primary file or string reader -- returns just the reader now
   * 
   * @param name 
   * @param type 
   * @param bufferedReader 
   * @param htParams 
   * @return        an AtomSetCollectionReader or an error string
   * 
   */
  public Object getAtomSetCollectionReader(String name, String type,
                                   BufferedReader bufferedReader, Hashtable htParams) {
    try {
      Object ret = Resolver.getAtomCollectionReader(name, type,
          bufferedReader, htParams, -1);
      if (ret instanceof String) {
        try {
          bufferedReader.close();
        } catch (Exception e) {
          //
        }
      } else {
        ((AtomSetCollectionReader) ret).setup(name, htParams, bufferedReader);
      }
      return ret;        
    } catch (Throwable e) {
      try {
        bufferedReader.close();
      } catch (Exception ex) {
        //
      }
      bufferedReader = null;
      Logger.error(null, e);
      return "" + e;
    }
  }

  /**
   * Create the AtomSetCollection and return it
   * 
   * @param atomSetCollectionReader 
   * @return an AtomSetCollection or an error string
   * 
   */
  public Object getAtomSetCollection(Object atomSetCollectionReader) {
    BufferedReader br = null;
    try {
      AtomSetCollectionReader a = (AtomSetCollectionReader) atomSetCollectionReader;
      br = a.reader;
      Object ret = a.readData();
      if (!(ret instanceof AtomSetCollection))
        return ret;
      AtomSetCollection atomSetCollection = (AtomSetCollection) ret;
      if (atomSetCollection.errorMessage != null)
        return atomSetCollection.errorMessage;
      return atomSetCollection;
    } catch (Throwable e) {
      try {
        br.close();
      } catch (Exception ex) {
        //
      }
      br = null;
      Logger.error(null, e);
      return "" + e;
    }
  }

  /**
   * primary for String[] or File[] reading -- two options
   * are implemented --- return a set of simultaneously open readers,
   * or return one single collection using a single reader
   * 
   * @param fileReader 
   * @param names 
   * @param types 
   * @param htParams 
   * @param getReadersOnly  TRUE for a set of readers; FALSE for one atomSetCollection
   * 
   * @return a set of AtomSetCollectionReaders, a single AtomSetCollection, or an error string
   * 
   */
  public Object getAtomSetCollectionReaders(JmolFileReaderInterface fileReader, String[] names, String[] types,
                                    Hashtable htParams, boolean getReadersOnly) {
    //FilesOpenThread
    int size = names.length;
    AtomSetCollectionReader[] readers = (getReadersOnly ? new AtomSetCollectionReader[size] : null);
    AtomSetCollection[] atomsets = (getReadersOnly ? null : new AtomSetCollection[size]);
    for (int i = 0; i < size; i++) {
      try {
        Object reader = fileReader.getBufferedReader(i);
        if (!(reader instanceof BufferedReader))
          return reader;
        Object ret = Resolver.getAtomCollectionReader(names[i],
            (types == null ? null : types[i]), (BufferedReader) reader, htParams, i);
        if (!(ret instanceof AtomSetCollectionReader))
          return ret;
          AtomSetCollectionReader r = (AtomSetCollectionReader) ret;
          r.setup(names[i], htParams, (BufferedReader) reader);
          if (getReadersOnly) {
            readers[i] = r;
          } else {
            ret = r.readData();
            if (!(ret instanceof AtomSetCollection))
              return ret;
            atomsets[i] = (AtomSetCollection) ret;
            if (atomsets[i].errorMessage != null)
              return atomsets[i].errorMessage;
          }
      } catch (Throwable e) {
        Logger.error(null, e);
        return "" + e;
      }
    }
    if (getReadersOnly)
      return readers;
    return getAtomSetCollectionFromSet(readers, atomsets, htParams);
  }
   
  /**
   * 
   * needed to consolidate a set of models into one model; could start
   * with AtomSetCollectionReader[] or with AtomSetCollection[]
   * 
   * @param readerSet 
   * @param atomsets 
   * @param htParams 
   * @return a single AtomSetCollection or an error string
   * 
   */
  public Object getAtomSetCollectionFromSet(Object readerSet, Object atomsets,
                                            Hashtable htParams) {
    AtomSetCollectionReader[] readers = (AtomSetCollectionReader[]) readerSet;
    AtomSetCollection[] asc = (atomsets == null ? new AtomSetCollection[readers.length]
        : (AtomSetCollection[]) atomsets);
    if (atomsets == null) {
      for (int i = 0; i < readers.length; i++) {
        try {
          Object ret = readers[i].readData();
          if (!(ret instanceof AtomSetCollection))
            return ret;
          asc[i] = (AtomSetCollection) ret;
          if (asc[i].errorMessage != null)
            return asc[i].errorMessage;
        } catch (Throwable e) {
          Logger.error(null, e);
          return "" + e;
        }
      }
    }
    if (htParams.containsKey("trajectorySteps")) {
      // this is one model with a set of coordinates from a 
      // molecular dynamics calculation
      // all the htParams[] entries point to the same Hashtable
      asc[0].finalizeTrajectory((List) htParams.get("trajectorySteps"));
      return asc[0];
    }
    AtomSetCollection result = new AtomSetCollection(asc);
    return (result.errorMessage == null ? result : (Object) result.errorMessage);
  }

  /**
   * A rather complicated means of reading a ZIP file, which could be a 
   * single file, or it could be a manifest-organized file, or it could be
   * a Spartan directory.
   * 
   * @param is 
   * @param fileName 
   * @param zipDirectory 
   * @param htParams 
   * @param asBufferedReader 
   * @return a single atomSetCollection
   * 
   */
  public Object getAtomSetCollectionOrBufferedReaderFromZip(InputStream is, String fileName, String[] zipDirectory,
                             Hashtable htParams, boolean asBufferedReader) {
    return staticGetAtomSetCollectionOrBufferedReaderFromZip(is, fileName, zipDirectory, htParams, 1, asBufferedReader);
  }

  private static Object staticGetAtomSetCollectionOrBufferedReaderFromZip(
                                                                          InputStream is,
                                                                          String fileName,
                                                                          String[] zipDirectory,
                                                                          Hashtable htParams,
                                                                          int subFilePtr,
                                                                          boolean asBufferedReader) {

    // we're here because user is using | in a load file name
    // or we are opening a zip file.

    boolean doCombine = (subFilePtr == 1);
    String[] subFileList = (htParams == null ? null : (String[]) htParams
        .get("subFileList"));
    if (subFileList == null)
      subFileList = Resolver.checkSpecialInZip(zipDirectory);

    String subFileName = (subFileList == null
        || subFilePtr >= subFileList.length ? null : subFileList[subFilePtr]);
    if (subFileName != null
        && (subFileName.startsWith("/") || subFileName.startsWith("\\")))
      subFileName = subFileName.substring(1);
    int selectedFile = 0;
    if (subFileName == null && htParams != null
        && htParams.containsKey("modelNumber")) {
      selectedFile = ((Integer) htParams.get("modelNumber")).intValue();
      if (selectedFile > 0 && doCombine)
        htParams.remove("modelNumber");
    }

    // zipDirectory[0] is the manifest if present
    String manifest = (htParams == null ? null : (String) htParams
        .get("manifest"));
    if (manifest == null)
      manifest = (zipDirectory.length > 0 ? zipDirectory[0] : "");
    boolean haveManifest = (manifest.length() > 0);
    if (haveManifest) {
      if (Logger.debugging)
        Logger.info("manifest for  " + fileName + ":\n" + manifest);
      manifest = '|' + manifest.replace('\r', '|').replace('\n', '|') + '|';
    }
    boolean ignoreErrors = (manifest.indexOf("IGNORE_ERRORS") >= 0);
    boolean selectAll = (manifest.indexOf("IGNORE_MANIFEST") >= 0);
    boolean exceptFiles = (manifest.indexOf("EXCEPT_FILES") >= 0);
    if (selectAll || subFileName != null)
      haveManifest = false;
    List vCollections = new ArrayList();
    Hashtable htCollections = (haveManifest ? new Hashtable() : null);
    int nFiles = 0;
    // 0 entry is manifest

    // check for a Spartan directory. This is not entirely satisfying,
    // because we aren't reading the file in the proper sequence.
    // this code is a hack that should be replaced with the sort of code
    // running in FileManager now.

    Object ret = Resolver.checkSpecialData(is, zipDirectory);
    if (ret instanceof String)
      return (String) ret;
    StringBuffer data = (StringBuffer) ret;
    try {
      if (data != null) {
        BufferedReader reader = new BufferedReader(new StringReader(data
            .toString()));
        if (asBufferedReader) {
          return reader;
        }
        ret = Resolver.getAtomCollectionReader(fileName, null, reader,
            htParams, -1);
        if (!(ret instanceof AtomSetCollectionReader))
          return ret;
        ((AtomSetCollectionReader) ret).setup(fileName, htParams, reader);
        ret = ((AtomSetCollectionReader) ret).readData();
        if (ret instanceof AtomSetCollection) {
          AtomSetCollection atomSetCollection = (AtomSetCollection) ret;
          if (atomSetCollection.errorMessage != null) {
            if (ignoreErrors)
              return null;
            return atomSetCollection.errorMessage;
          }
          return atomSetCollection;
        }
        if (ignoreErrors)
          return null;
        return "unknown reader error";
      }
      ZipInputStream zis = ZipUtil.getStream(is);
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null
          && (selectedFile <= 0 || vCollections.size() < selectedFile)) {
        if (ze.isDirectory())
          continue;
        String thisEntry = ze.getName();
        if (subFileName != null && !thisEntry.equals(subFileName))
          continue;
        if (ZipUtil.isJmolManifest(thisEntry) || haveManifest
            && exceptFiles == manifest.indexOf("|" + thisEntry + "|") >= 0)
          continue;
        byte[] bytes = ZipUtil.getZipEntryAsBytes(zis);
        if (ZipUtil.isZipFile(bytes)) {
          BufferedInputStream bis = new BufferedInputStream(
              new ByteArrayInputStream(bytes));
          String[] zipDir2 = ZipUtil.getZipDirectoryAndClose(bis, true);
          bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
          Object atomSetCollections = staticGetAtomSetCollectionOrBufferedReaderFromZip(
              bis, fileName + "|" + thisEntry, zipDir2, htParams, ++subFilePtr,
              asBufferedReader);
          if (atomSetCollections instanceof String) {
            if (ignoreErrors)
              continue;
            return atomSetCollections;
          } else if (atomSetCollections instanceof AtomSetCollection
              || atomSetCollections instanceof List) {
            if (haveManifest && !exceptFiles)
              htCollections.put(thisEntry, atomSetCollections);
            else
              vCollections.add(atomSetCollections);
          } else if (atomSetCollections instanceof BufferedReader) {
            if (doCombine)
              zis.close();
            return atomSetCollections; // FileReader has requested a zip file
            // BufferedReader
          } else {
            if (ignoreErrors)
              continue;
            zis.close();
            return "unknown zip reader error";
          }
        } else {
          String sData = (CompoundDocument.isCompoundDocument(bytes) ? (new CompoundDocument(
              new BufferedInputStream(new ByteArrayInputStream(bytes))))
              .getAllData("Molecule").toString()
              : ZipUtil.isGzip(bytes) ? ZipUtil.getGzippedBytesAsString(bytes)
                  : new String(bytes));
          BufferedReader reader = new BufferedReader(new StringReader(sData));
          if (asBufferedReader) {
            if (doCombine)
              zis.close();
            return reader;
          }
          String fname = fileName + "|" + ze.getName();
          ret = Resolver.getAtomCollectionReader(fname,
              null, reader, htParams, -1);
          if (ret instanceof AtomSetCollectionReader) {
            ((AtomSetCollectionReader) ret).setup(fname, htParams, reader);
            ret = ((AtomSetCollectionReader) ret).readData();
          }
          if (!(ret instanceof AtomSetCollection)) {
            if (ignoreErrors)
              continue;
            zis.close();
            return "" + ret;
          }
          if (haveManifest && !exceptFiles)
            htCollections.put(thisEntry, ret);
          else
            vCollections.add(ret);
          AtomSetCollection a = (AtomSetCollection) ret;
          if (a.errorMessage != null) {
            if (ignoreErrors)
              continue;
            zis.close();
            return a.errorMessage;
          }
        }
      }
      if (doCombine)
        zis.close();

      // if a manifest exists, it sets the files and file order

      if (haveManifest && !exceptFiles) {
        String[] list = TextFormat.split(manifest, '|');
        for (int i = 0; i < list.length; i++) {
          String file = list[i];
          if (file.length() == 0 || file.indexOf("#") == 0)
            continue;
          if (htCollections.containsKey(file))
            vCollections.add(htCollections.get(file));
          else if (Logger.debugging)
            Logger.info("manifested file " + file + " was not found in "
                + fileName);
        }
      }
      if (!doCombine)
        return vCollections;
      AtomSetCollection result = new AtomSetCollection(vCollections);
      if (result.errorMessage != null) {
        if (ignoreErrors)
          return null;
        return result.errorMessage;
      }
      if (nFiles == 1)
        selectedFile = 1;
      if (selectedFile > 0 && selectedFile <= vCollections.size())
        return vCollections.get(selectedFile - 1);
      return result;

    } catch (Exception e) {
      if (ignoreErrors)
        return null;
      Logger.error(null, e);
      return "" + e;
    } catch (Error er) {
      Logger.error(null, er);
      return "" + er;
    }
  }

  /**
   * Direct DOM HTML4 page reading; Egon was interested in this at one point.
   * 
   * @param DOMNode 
   * @param htParams 
   * @return a single AtomSetCollection or an error string
   * 
   */
  public Object getAtomSetCollectionFromDOM(Object DOMNode, Hashtable htParams) {
    try {
      Object ret = Resolver.DOMResolve(DOMNode, htParams);
      if (!(ret instanceof AtomSetCollectionReader))
        return ret;
      AtomSetCollectionReader a = (AtomSetCollectionReader) ret;
      a.setup("DOM node", htParams, null);
      ret = a.readData(DOMNode);
      if (!(ret instanceof AtomSetCollection))
        return ret;
      AtomSetCollection asc = (AtomSetCollection) ret;
      if (asc.errorMessage != null)
        return asc.errorMessage;
      return asc;
    } catch (Throwable e) {
      Logger.error(null, e);
      return "" + e;
    }
  }

  public String[] specialLoad(String name, String type) {
    return Resolver.specialLoad(name, type);  
  }
  
  public void finish(Object atomSetCollection) {
    ((AtomSetCollection)atomSetCollection).finish();
  }

  ////////////////////////// post processing ////////////////////////////
  
  public String getAtomSetCollectionName(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getCollectionName();
  }
  
  public Hashtable getAtomSetCollectionAuxiliaryInfo(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetCollectionAuxiliaryInfo();
  }

  public int getAtomSetCount(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetCount();
  }

  public int getAtomSetNumber(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetNumber(atomSetIndex);
  }

  public String getAtomSetName(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetName(atomSetIndex);
  }
  
  public Hashtable getAtomSetAuxiliaryInfo(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection) atomSetCollection)
        .getAtomSetAuxiliaryInfo(atomSetIndex);
  }

  /* **************************************************************
   * The frame related methods
   * **************************************************************/

  public int getAtomCount(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getAtomCount();
  }

  public boolean coordinatesAreFractional(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).coordinatesAreFractional;
  }

  public float[] getNotionalUnitcell(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).notionalUnitCell;
  }

  public float[] getPdbScaleMatrix(Object atomSetCollection) {
    float[] a = ((AtomSetCollection)atomSetCollection).notionalUnitCell;
    if (a.length < 22)
      return null;
    float[] b = new float[16];
    for (int i = 0; i < 16; i++)
      b[i] = a[6 + i];
    return b;
  }

  public float[] getPdbScaleTranslate(Object atomSetCollection) {
    float[] a = ((AtomSetCollection)atomSetCollection).notionalUnitCell;
    if (a.length < 22)
      return null;
    float[] b = new float[3];
    b[0] = a[6 + 4*0 + 3];
    b[1] = a[6 + 4*1 + 3];
    b[2] = a[6 + 4*2 + 3];
    return b;
  }
  
  ////////////////////////////////////////////////////////////////

  public JmolAdapter.AtomIterator
    getAtomIterator(Object atomSetCollection) {
    return new AtomIterator((AtomSetCollection)atomSetCollection);
  }

  public JmolAdapter.BondIterator
    getBondIterator(Object atomSetCollection) {
    return new BondIterator((AtomSetCollection)atomSetCollection);
  }

  public JmolAdapter.StructureIterator
    getStructureIterator(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getStructureCount() == 0 ? 
        null : new StructureIterator((AtomSetCollection)atomSetCollection);
  }

  /* **************************************************************
   * the frame iterators
   * **************************************************************/
  class AtomIterator extends JmolAdapter.AtomIterator {
    private int iatom;
    private Atom atom;
    private int atomCount;
    private Atom[] atoms;

    AtomIterator(AtomSetCollection atomSetCollection) {
      atomCount = atomSetCollection.getAtomCount();
      atoms = atomSetCollection.getAtoms();
      iatom = 0;
    }
    public boolean hasNext() {
      if (iatom == atomCount)
        return false;
      atom = atoms[iatom];
      atoms[iatom++] = null; // single pass
      return true;
    }
    public int getAtomSetIndex() { return atom.atomSetIndex; }
    public BitSet getAtomSymmetry() { return atom.bsSymmetry; }
    public int getAtomSite() { return atom.atomSite + 1; }
    public Object getUniqueID() { return new Integer(atom.atomIndex); }
    public String getElementSymbol() { return atom.getElementSymbol(); }
    public short getElementNumber() { 
      return (atom.elementNumber > 0 ?
        atom.elementNumber : JmolAdapter.getElementNumber(atom.getElementSymbol())); }
    public String getAtomName() { return atom.atomName; }
    public int getFormalCharge() { return atom.formalCharge; }
    public float getPartialCharge() { return atom.partialCharge; }
    public Object[] getEllipsoid() { return atom.ellipsoid; }
    public float getRadius() { return atom.radius; }
    public float getX() { return atom.x; }
    public float getY() { return atom.y; }
    public float getZ() { return atom.z; }
    public float getVectorX() { return atom.vectorX; }
    public float getVectorY() { return atom.vectorY; }
    public float getVectorZ() { return atom.vectorZ; }
    public float getBfactor() { return Float.isNaN(atom.bfactor) && atom.anisoBorU != null ?
        atom.anisoBorU[7] * 100f : atom.bfactor; }
    public int getOccupancy() { return atom.occupancy; }
    public boolean getIsHetero() { return atom.isHetero; }
    public int getAtomSerial() { return atom.atomSerial; }
    public char getChainID() { return canonizeChainID(atom.chainID); }
    public char getAlternateLocationID()
    { return canonizeAlternateLocationID(atom.alternateLocationID); }
    public String getGroup3() { return atom.group3; }
    public int getSequenceNumber() { return atom.sequenceNumber; }
    public char getInsertionCode()
    { return canonizeInsertionCode(atom.insertionCode); }
    
  }

  class BondIterator extends JmolAdapter.BondIterator {
    private Bond[] bonds;
    private int ibond;
    private Bond bond;
    private int bondCount;
    
    BondIterator(AtomSetCollection atomSetCollection) {
      bonds = atomSetCollection.getBonds();
      bondCount = atomSetCollection.getBondCount();      
      ibond = 0;
    }
    public boolean hasNext() {
      if (ibond == bondCount)
        return false;
      bond = bonds[ibond++];
      return true;
    }
    public Object getAtomUniqueID1() {
      return new Integer(bond.atomIndex1);
    }
    public Object getAtomUniqueID2() {
      return new Integer(bond.atomIndex2);
    }
    public int getEncodedOrder() {
      return bond.order;
    }
  }

  public class StructureIterator extends JmolAdapter.StructureIterator {
    private int structureCount;
    private Structure[] structures;
    private Structure structure;
    private int istructure;
    
    StructureIterator(AtomSetCollection atomSetCollection) {
      structureCount = atomSetCollection.getStructureCount();
      structures = atomSetCollection.getStructures();
      istructure = 0;
    }

    public boolean hasNext() {
      if (istructure == structureCount)
        return false;
      structure = structures[istructure++];
      return true;
    }

    public int getModelIndex() {
      return structure.modelIndex;
    }

    public int getStructureType() {
      return structure.structureType;
    }

    public int getSubstructureType() {
      return structure.substructureType;
    }

    public String getStructureID() {
      return structure.structureID;
    }

    public int getSerialID() {
      return structure.serialID;
    }

    public char getStartChainID() {
      return canonizeChainID(structure.startChainID);
    }
    
    public int getStartSequenceNumber() {
      return structure.startSequenceNumber;
    }
    
    public char getStartInsertionCode() {
      return canonizeInsertionCode(structure.startInsertionCode);
    }
    
    public char getEndChainID() {
      return canonizeChainID(structure.endChainID);
    }
    
    public int getEndSequenceNumber() {
      return structure.endSequenceNumber;
    }
      
    public char getEndInsertionCode() {
      return structure.endInsertionCode;
    }

    public int getStrandCount() {
      return structure.strandCount;
    }
  }
}
