Clazz.declarePackage ("JM");
Clazz.load (["JM.ModelCollection", "JU.M3", "$.M4", "$.V3"], "JM.ModelSet", ["java.lang.Boolean", "java.util.Hashtable", "JU.A4", "$.BS", "$.List", "$.P3", "$.Quat", "$.SB", "J.api.Interface", "J.atomdata.RadiusData", "JW.BSUtil", "$.JmolMolecule", "$.Measure", "JV.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.selectionHaloEnabled = false;
this.echoShapeActive = false;
this.modelSetTypeName = null;
this.closest = null;
this.pointGroup = null;
this.matTemp = null;
this.matInv = null;
this.mat4 = null;
this.mat4t = null;
this.vTemp = null;
Clazz.instantialize (this, arguments);
}, JM, "ModelSet", JM.ModelCollection);
Clazz.prepareFields (c$, function () {
this.closest =  new Array (1);
this.matTemp =  new JU.M3 ();
this.matInv =  new JU.M3 ();
this.mat4 =  new JU.M4 ();
this.mat4t =  new JU.M4 ();
this.vTemp =  new JU.V3 ();
});
Clazz.makeConstructor (c$, 
function (vwr, name) {
Clazz.superConstructor (this, JM.ModelSet, []);
this.vwr = vwr;
this.modelSetName = name;
}, "JV.Viewer,~S");
Clazz.defineMethod (c$, "releaseModelSet", 
function () {
this.am = null;
this.closest[0] = null;
Clazz.superCall (this, JM.ModelSet, "releaseModelSet", []);
});
Clazz.defineMethod (c$, "setSelectionHaloEnabled", 
function (selectionHaloEnabled) {
this.selectionHaloEnabled = selectionHaloEnabled;
}, "~B");
Clazz.defineMethod (c$, "getSelectionHaloEnabled", 
function () {
return this.selectionHaloEnabled;
});
Clazz.defineMethod (c$, "getEchoStateActive", 
function () {
return this.echoShapeActive;
});
Clazz.defineMethod (c$, "setEchoStateActive", 
function (TF) {
this.echoShapeActive = TF;
}, "~B");
Clazz.defineMethod (c$, "getModelSetTypeName", 
function () {
return this.modelSetTypeName;
});
Clazz.defineMethod (c$, "getModelNumberIndex", 
function (modelNumber, useModelNumber, doSetTrajectory) {
if (useModelNumber) {
for (var i = 0; i < this.mc; i++) if (this.modelNumbers[i] == modelNumber || modelNumber < 1000000 && this.modelNumbers[i] == 1000000 + modelNumber) return i;

return -1;
}for (var i = 0; i < this.mc; i++) if (this.modelFileNumbers[i] == modelNumber) {
if (doSetTrajectory && this.isTrajectory (i)) this.setTrajectory (i);
return i;
}
return -1;
}, "~N,~B,~B");
Clazz.defineMethod (c$, "getBitSetTrajectories", 
function () {
if (this.trajectorySteps == null) return null;
var bsModels =  new JU.BS ();
for (var i = this.mc; --i >= 0; ) {
var t = this.am[i].getSelectedTrajectory ();
if (t >= 0) {
bsModels.set (t);
i = this.am[i].trajectoryBaseIndex;
}}
return bsModels;
});
Clazz.defineMethod (c$, "setTrajectoryBs", 
function (bsModels) {
for (var i = 0; i < this.mc; i++) if (bsModels.get (i)) this.setTrajectory (i);

}, "JU.BS");
Clazz.defineMethod (c$, "setTrajectory", 
function (modelIndex) {
if (modelIndex < 0 || !this.isTrajectory (modelIndex)) return;
if (this.at[this.am[modelIndex].firstAtomIndex].mi == modelIndex) return;
var baseModelIndex = this.am[modelIndex].trajectoryBaseIndex;
this.am[baseModelIndex].setSelectedTrajectory (modelIndex);
this.setAtomPositions (baseModelIndex, modelIndex, this.trajectorySteps.get (modelIndex), null, 0, (this.vibrationSteps == null ? null : this.vibrationSteps.get (modelIndex)), true);
var m = this.vwr.getCurrentModelIndex ();
if (m >= 0 && m != modelIndex && this.am[m].fileIndex == this.am[modelIndex].fileIndex) this.vwr.setCurrentModelIndexClear (modelIndex, false);
}, "~N");
Clazz.defineMethod (c$, "morphTrajectories", 
function (m1, m2, f) {
if (m1 < 0 || m2 < 0 || !this.isTrajectory (m1) || !this.isTrajectory (m2)) return;
if (f == 0) {
this.setTrajectory (m1);
return;
}if (f == 1) {
this.setTrajectory (m2);
return;
}var baseModelIndex = this.am[m1].trajectoryBaseIndex;
this.am[baseModelIndex].setSelectedTrajectory (m1);
this.setAtomPositions (baseModelIndex, m1, this.trajectorySteps.get (m1), this.trajectorySteps.get (m2), f, (this.vibrationSteps == null ? null : this.vibrationSteps.get (m1)), true);
var m = this.vwr.getCurrentModelIndex ();
if (m >= 0 && m != m1 && this.am[m].fileIndex == this.am[m1].fileIndex) this.vwr.setCurrentModelIndexClear (m1, false);
}, "~N,~N,~N");
Clazz.defineMethod (c$, "setAtomPositions", 
 function (baseModelIndex, modelIndex, t1, t2, f, vibs, isFractional) {
var bs =  new JU.BS ();
var vib =  new JU.V3 ();
var iFirst = this.am[baseModelIndex].firstAtomIndex;
var iMax = iFirst + this.getAtomCountInModel (baseModelIndex);
if (f == 0) {
for (var pt = 0, i = iFirst; i < iMax && pt < t1.length; i++, pt++) {
this.at[i].mi = modelIndex;
if (t1[pt] == null) continue;
if (isFractional) this.at[i].setFractionalCoordTo (t1[pt], true);
 else this.at[i].setT (t1[pt]);
if (this.vibrationSteps != null) {
if (vibs != null && vibs[pt] != null) vib = vibs[pt];
this.setVibrationVector (i, vib);
}bs.set (i);
}
} else {
var p =  new JU.P3 ();
var n = Math.min (t1.length, t2.length);
for (var pt = 0, i = iFirst; i < iMax && pt < n; i++, pt++) {
this.at[i].mi = modelIndex;
if (t1[pt] == null || t2[pt] == null) continue;
p.sub2 (t2[pt], t1[pt]);
p.scaleAdd2 (f, p, t1[pt]);
if (isFractional) this.at[i].setFractionalCoordTo (p, true);
 else this.at[i].setT (p);
bs.set (i);
}
}this.initializeBspf ();
this.validateBspfForModel (baseModelIndex, false);
this.recalculateLeadMidpointsAndWingVectors (baseModelIndex);
this.sm.refreshShapeTrajectories (baseModelIndex, bs, null);
if (this.am[baseModelIndex].hasRasmolHBonds) {
this.am[baseModelIndex].clearRasmolHydrogenBonds (null);
this.am[baseModelIndex].getRasmolHydrogenBonds (bs, bs, null, false, 2147483647, false, null);
}}, "~N,~N,~A,~A,~N,~A,~B");
Clazz.defineMethod (c$, "getFrameOffsets", 
function (bsAtoms) {
if (bsAtoms == null) return null;
var offsets =  new Array (this.mc);
for (var i = 0; i < this.mc; i++) offsets[i] =  new JU.P3 ();

var lastModel = 0;
var n = 0;
var offset = offsets[0];
var asTrajectory = (this.trajectorySteps != null && this.trajectorySteps.size () == this.mc);
var m1 = (asTrajectory ? this.mc : 1);
for (var m = 0; m < m1; m++) {
if (asTrajectory) this.setTrajectory (m);
for (var i = 0; i <= this.ac; i++) {
if (i == this.ac || this.at[i].mi != lastModel) {
if (n > 0) {
offset.scale (-1.0 / n);
if (lastModel != 0) offset.sub (offsets[0]);
n = 0;
}if (i == this.ac) break;
lastModel = this.at[i].mi;
offset = offsets[lastModel];
}if (!bsAtoms.get (i)) continue;
offset.add (this.at[i]);
n++;
}
}
offsets[0].set (0, 0, 0);
return offsets;
}, "JU.BS");
Clazz.defineMethod (c$, "getAtomBits", 
function (tokType, specInfo) {
switch (tokType) {
default:
return JW.BSUtil.andNot (this.getAtomBitsMaybeDeleted (tokType, specInfo), this.vwr.getDeletedAtoms ());
case 1048610:
var modelNumber = (specInfo).intValue ();
var modelIndex = this.getModelNumberIndex (modelNumber, true, true);
return (modelIndex < 0 && modelNumber > 0 ?  new JU.BS () : this.vwr.getModelUndeletedAtomsBitSet (modelIndex));
}
}, "~N,~O");
Clazz.defineMethod (c$, "getAtomLabel", 
function (i) {
return this.vwr.getShapePropertyIndex (5, "label", i);
}, "~N");
Clazz.defineMethod (c$, "findNearestAtomIndex", 
function (x, y, bsNot, min) {
if (this.ac == 0) return -1;
this.closest[0] = null;
if (this.g3d.isAntialiased ()) {
x <<= 1;
y <<= 1;
}this.findNearest2 (x, y, this.closest, bsNot, min);
this.sm.findNearestShapeAtomIndex (x, y, this.closest, bsNot);
var closestIndex = (this.closest[0] == null ? -1 : this.closest[0].i);
this.closest[0] = null;
return closestIndex;
}, "~N,~N,JU.BS,~N");
Clazz.defineMethod (c$, "calculateStructures", 
function (bsAtoms, asDSSP, doReport, dsspIgnoreHydrogen, setStructure) {
var bsAllAtoms =  new JU.BS ();
var bsModelsExcluded = JW.BSUtil.copyInvert (this.modelsOf (bsAtoms, bsAllAtoms), this.mc);
if (!setStructure) return this.calculateStructuresAllExcept (bsModelsExcluded, asDSSP, doReport, dsspIgnoreHydrogen, false, false);
for (var i = 0; i < this.mc; i++) if (!bsModelsExcluded.get (i)) this.am[i].clearBioPolymers ();

this.calculatePolymers (null, 0, 0, bsModelsExcluded);
var ret = this.calculateStructuresAllExcept (bsModelsExcluded, asDSSP, doReport, dsspIgnoreHydrogen, true, false);
this.vwr.resetBioshapes (bsAllAtoms);
this.setStructureIndexes ();
return ret;
}, "JU.BS,~B,~B,~B,~B");
Clazz.defineMethod (c$, "calculatePointGroup", 
function (bsAtoms) {
return this.calculatePointGroupForFirstModel (bsAtoms, false, false, false, null, 0, 0);
}, "JU.BS");
Clazz.defineMethod (c$, "getPointGroupInfo", 
function (bsAtoms) {
return this.calculatePointGroupForFirstModel (bsAtoms, false, false, true, null, 0, 0);
}, "JU.BS");
Clazz.defineMethod (c$, "getPointGroupAsString", 
function (bsAtoms, asDraw, type, index, scale) {
return this.calculatePointGroupForFirstModel (bsAtoms, true, asDraw, false, type, index, scale);
}, "JU.BS,~B,~S,~N,~N");
Clazz.defineMethod (c$, "calculatePointGroupForFirstModel", 
 function (bsAtoms, doAll, asDraw, asInfo, type, index, scale) {
var modelIndex = this.vwr.getCurrentModelIndex ();
var iAtom = (bsAtoms == null ? -1 : bsAtoms.nextSetBit (0));
if (modelIndex < 0 && iAtom >= 0) modelIndex = this.at[iAtom].getModelIndex ();
if (modelIndex < 0) {
modelIndex = this.vwr.getVisibleFramesBitSet ().nextSetBit (0);
bsAtoms = null;
}var bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
if (bsAtoms != null) bs.and (bsAtoms);
iAtom = bs.nextSetBit (0);
if (iAtom < 0) {
bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
iAtom = bs.nextSetBit (0);
}var obj = this.vwr.getShapePropertyIndex (18, "mad", iAtom);
var haveVibration = (obj != null && (obj).intValue () != 0 || this.vwr.isVibrationOn ());
var symmetry = J.api.Interface.getSymmetry ();
this.pointGroup = symmetry.setPointGroup (this.pointGroup, this.at, bs, haveVibration, this.vwr.getFloat (570425382), this.vwr.getFloat (570425384));
if (!doAll && !asInfo) return this.pointGroup.getPointGroupName ();
var ret = this.pointGroup.getPointGroupInfo (modelIndex, asDraw, asInfo, type, index, scale);
if (asInfo) return ret;
return (this.mc > 1 ? "frame " + this.getModelNumberDotted (modelIndex) + "; " : "") + ret;
}, "JU.BS,~B,~B,~B,~S,~N,~N");
Clazz.defineMethod (c$, "modelsOf", 
 function (bsAtoms, bsAllAtoms) {
var bsModels = JU.BS.newN (this.mc);
var isAll = (bsAtoms == null);
var i0 = (isAll ? this.ac - 1 : bsAtoms.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsAtoms.nextSetBit (i + 1))) {
var modelIndex = this.am[this.at[i].mi].trajectoryBaseIndex;
if (this.isJmolDataFrameForModel (modelIndex)) continue;
bsModels.set (modelIndex);
bsAllAtoms.set (i);
}
return bsModels;
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "getDefaultStructure", 
function (bsAtoms, bsAllAtoms) {
var bsModels = this.modelsOf (bsAtoms, bsAllAtoms);
var ret =  new JU.SB ();
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) if (this.am[i].isBioModel && this.am[i].defaultStructure != null) ret.append (this.am[i].defaultStructure);

return ret.toString ();
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "makeConnections", 
function (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
if (connectOperation == 1073741852 && order != 2048) {
var stateScript = "connect ";
if (minDistance != 0.1) stateScript += minDistance + " ";
if (maxDistance != 1.0E8) stateScript += maxDistance + " ";
this.addStateScript (stateScript, (isBonds ? bsA : null), (isBonds ? null : bsA), (isBonds ? null : bsB), " auto", false, true);
}this.moleculeCount = 0;
return this.makeConnections2 (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy);
}, "~N,~N,~N,~N,JU.BS,JU.BS,JU.BS,~B,~B,~N");
Clazz.defineMethod (c$, "setPdbConectBonding", 
function (baseAtomIndex, baseModelIndex, bsExclude) {
var mad = this.vwr.getMadBond ();
for (var i = baseModelIndex; i < this.mc; i++) {
var vConnect = this.getModelAuxiliaryInfoValue (i, "PDB_CONECT_bonds");
if (vConnect == null) continue;
var nConnect = vConnect.size ();
this.setModelAuxiliaryInfo (i, "initialBondCount", Integer.$valueOf (nConnect));
var atomInfo = this.getModelAuxiliaryInfoValue (i, "PDB_CONECT_firstAtom_count_max");
var firstAtom = atomInfo[0] + baseAtomIndex;
var atomMax = firstAtom + atomInfo[1];
var max = atomInfo[2];
var serialMap =  Clazz.newIntArray (max + 1, 0);
var iSerial;
for (var iAtom = firstAtom; iAtom < atomMax; iAtom++) if ((iSerial = this.atomSerials[iAtom]) > 0) serialMap[iSerial] = iAtom + 1;

for (var iConnect = 0; iConnect < nConnect; iConnect++) {
var pair = vConnect.get (iConnect);
var sourceSerial = pair[0];
var targetSerial = pair[1];
var order = pair[2];
if (sourceSerial < 0 || targetSerial < 0 || sourceSerial > max || targetSerial > max) continue;
var sourceIndex = serialMap[sourceSerial] - 1;
var targetIndex = serialMap[targetSerial] - 1;
if (sourceIndex < 0 || targetIndex < 0) continue;
if (bsExclude != null) {
if (this.at[sourceIndex].isHetero ()) bsExclude.set (sourceIndex);
if (this.at[targetIndex].isHetero ()) bsExclude.set (targetIndex);
}this.checkValencesAndBond (this.at[sourceIndex], this.at[targetIndex], order, (order == 2048 ? 1 : mad), null);
}
}
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "deleteAllBonds", 
function () {
this.moleculeCount = 0;
for (var i = this.stateScripts.size (); --i >= 0; ) {
if (this.stateScripts.get (i).isConnect ()) {
this.stateScripts.remove (i);
}}
this.deleteAllBonds2 ();
});
Clazz.defineMethod (c$, "includeAllRelatedFrames", 
 function (bsModels) {
var j;
for (var i = 0; i < this.mc; i++) {
if (bsModels.get (i)) {
if (this.isTrajectory (i) && !bsModels.get (j = this.am[i].trajectoryBaseIndex)) {
bsModels.set (j);
this.includeAllRelatedFrames (bsModels);
return;
}continue;
}if (this.isTrajectory (i) && bsModels.get (this.am[i].trajectoryBaseIndex) || this.isJmolDataFrameForModel (i) && bsModels.get (this.am[i].dataSourceFrame)) bsModels.set (i);
}
}, "JU.BS");
Clazz.defineMethod (c$, "deleteModels", 
function (bsAtoms) {
this.moleculeCount = 0;
var bsModels = this.getModelBitSet (bsAtoms, false);
this.includeAllRelatedFrames (bsModels);
var nModelsDeleted = JW.BSUtil.cardinalityOf (bsModels);
if (nModelsDeleted == 0) return null;
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.clearDataFrameReference (i);

var bsDeleted;
if (nModelsDeleted == this.mc) {
bsDeleted = this.getModelAtomBitSetIncludingDeleted (-1, true);
this.vwr.zap (true, false, false);
return bsDeleted;
}this.validateBspf (false);
var newModels =  new Array (this.mc - nModelsDeleted);
var oldModels = this.am;
bsDeleted =  new JU.BS ();
for (var i = 0, mpt = 0; i < this.mc; i++) if (bsModels.get (i)) {
this.getAtomCountInModel (i);
bsDeleted.or (this.getModelAtomBitSetIncludingDeleted (i, false));
} else {
this.am[i].modelIndex = mpt;
newModels[mpt++] = this.am[i];
}
this.am = newModels;
var oldModelCount = this.mc;
var bsBonds = this.getBondsForSelectedAtoms (bsDeleted, true);
this.deleteBonds (bsBonds, true);
for (var i = 0, mpt = 0; i < oldModelCount; i++) {
if (!bsModels.get (i)) {
mpt++;
continue;
}var nAtoms = oldModels[i].ac;
if (nAtoms == 0) continue;
var bs = oldModels[i].bsAtoms;
var firstAtomIndex = oldModels[i].firstAtomIndex;
JW.BSUtil.deleteBits (this.bsSymmetry, bs);
this.deleteModel (mpt, firstAtomIndex, nAtoms, bs, bsBonds);
for (var j = oldModelCount; --j > i; ) oldModels[j].fixIndices (mpt, nAtoms, bs);

this.vwr.deleteShapeAtoms ([newModels, this.at, [mpt, firstAtomIndex, nAtoms]], bs);
this.mc--;
}
this.deleteModel (-1, 0, 0, null, null);
return bsDeleted;
}, "JU.BS");
Clazz.defineMethod (c$, "setAtomProperty", 
function (bs, tok, iValue, fValue, sValue, values, list) {
switch (tok) {
case 1115297793:
case 1113200642:
case 1113200647:
case 1113200649:
case 1113200650:
case 1650071565:
case 1113200654:
if (fValue > 4.0) fValue = 4.0;
if (values != null) {
var newValues =  Clazz.newFloatArray (this.ac, 0);
for (var i = bs.nextSetBit (0), ii = 0; i >= 0; i = bs.nextSetBit (i + 1)) newValues[i] = values[ii++];

values = newValues;
}case 1113200646:
case 1113200652:
var rd = null;
var mar = 0;
if (values == null) {
if (fValue > 16) fValue = 16.1;
if (fValue < 0) fValue = 0;
mar = Clazz.doubleToInt (Math.floor (fValue * 2000));
} else {
rd =  new J.atomdata.RadiusData (values, 0, null, null);
}this.sm.setShapeSizeBs (JV.JC.shapeTokenIndex (tok), mar, rd, bs);
return;
}
this.setAPm (bs, tok, iValue, fValue, sValue, values, list);
}, "JU.BS,~N,~N,~N,~S,~A,~A");
Clazz.defineMethod (c$, "getFileData", 
function (modelIndex) {
if (modelIndex < 0) return "";
var fileData = this.getModelAuxiliaryInfoValue (modelIndex, "fileData");
if (fileData != null) return fileData;
if (!this.getModelAuxiliaryInfoBoolean (modelIndex, "isCIF")) return this.getPDBHeader (modelIndex);
fileData = this.vwr.getCifData (modelIndex);
this.setModelAuxiliaryInfo (modelIndex, "fileData", fileData);
return fileData;
}, "~N");
Clazz.overrideMethod (c$, "calculateStruts", 
function (bs1, bs2) {
this.vwr.setModelVisibility ();
return this.calculateStrutsMC (bs1, bs2);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "addHydrogens", 
function (vConnections, pts) {
var modelIndex = this.mc - 1;
var bs =  new JU.BS ();
if (this.isTrajectory (modelIndex) || this.am[modelIndex].getGroupCount () > 1) {
return bs;
}this.growAtomArrays (this.ac + pts.length);
var rd = this.vwr.getDefaultRadiusData ();
var mad = this.getDefaultMadFromOrder (1);
for (var i = 0, n = this.am[modelIndex].ac + 1; i < vConnections.size (); i++, n++) {
var atom1 = vConnections.get (i);
var atom2 = this.addAtom (modelIndex, atom1.group, 1, "H" + n, n, n, pts[i], NaN, null, 0, 0, 100, NaN, null, false, 0, null);
atom2.setMadAtom (this.vwr, rd);
bs.set (atom2.i);
this.bondAtoms (atom1, atom2, 1, mad, null, 0, false, false);
}
this.sm.loadDefaultShapes (this);
return bs;
}, "JU.List,~A");
Clazz.defineMethod (c$, "setAtomCoordsRelative", 
function (offset, bs) {
this.setAtomsCoordRelative (bs, offset.x, offset.y, offset.z);
this.mat4.setIdentity ();
this.vTemp.setT (offset);
this.mat4.setTranslation (this.vTemp);
this.recalculatePositionDependentQuantities (bs, this.mat4);
}, "JU.T3,JU.BS");
Clazz.defineMethod (c$, "setAtomCoords", 
function (bs, tokType, xyzValues) {
this.setAtomCoord2 (bs, tokType, xyzValues);
switch (tokType) {
case 1112541202:
case 1112541203:
case 1112541204:
case 1146095631:
break;
default:
this.recalculatePositionDependentQuantities (bs, null);
}
}, "JU.BS,~N,~O");
Clazz.defineMethod (c$, "invertSelected", 
function (pt, plane, iAtom, invAtoms, bs) {
if (pt != null) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var x = (pt.x - this.at[i].x) * 2;
var y = (pt.y - this.at[i].y) * 2;
var z = (pt.z - this.at[i].z) * 2;
this.setAtomCoordRelative (i, x, y, z);
}
return;
}if (plane != null) {
var norm = JU.V3.new3 (plane.x, plane.y, plane.z);
norm.normalize ();
var d = Math.sqrt (plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var twoD = -JW.Measure.distanceToPlaneD (plane, d, this.at[i]) * 2;
var x = norm.x * twoD;
var y = norm.y * twoD;
var z = norm.z * twoD;
this.setAtomCoordRelative (i, x, y, z);
}
return;
}if (iAtom >= 0) {
var thisAtom = this.at[iAtom];
var bonds = thisAtom.bonds;
if (bonds == null) return;
var bsAtoms =  new JU.BS ();
var vNot =  new JU.List ();
var bsModel = this.vwr.getModelUndeletedAtomsBitSet (thisAtom.mi);
for (var i = 0; i < bonds.length; i++) {
var a = bonds[i].getOtherAtom (thisAtom);
if (invAtoms.get (a.i)) {
bsAtoms.or (JW.JmolMolecule.getBranchBitSet (this.at, a.i, bsModel, null, iAtom, true, true));
} else {
vNot.addLast (a);
}}
if (vNot.size () == 0) return;
pt = JW.Measure.getCenterAndPoints (vNot)[0];
var v = JU.V3.newVsub (thisAtom, pt);
var q = JU.Quat.newVA (v, 180);
this.moveAtoms (null, q.getMatrix (), null, bsAtoms, thisAtom, true, false);
}}, "JU.P3,JU.P4,~N,JU.BS,JU.BS");
Clazz.defineMethod (c$, "setDihedrals", 
function (dihedralList, bsBranches, f) {
var n = Clazz.doubleToInt (dihedralList.length / 6);
if (f > 1) f = 1;
for (var j = 0, pt = 0; j < n; j++, pt += 6) {
var bs = bsBranches[j];
if (bs == null || bs.isEmpty ()) continue;
var a1 = this.at[Clazz.floatToInt (dihedralList[pt + 1])];
var v = JU.V3.newVsub (this.at[Clazz.floatToInt (dihedralList[pt + 2])], a1);
var angle = (dihedralList[pt + 5] - dihedralList[pt + 4]) * f;
var aa = JU.A4.newVA (v, (-angle / 57.29577951308232));
this.matTemp.setAA (aa);
this.ptTemp.setT (a1);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
this.at[i].sub (this.ptTemp);
this.matTemp.rotate (this.at[i]);
this.at[i].add (this.ptTemp);
this.taintAtom (i, 2);
}
}
}, "~A,~A,~N");
Clazz.defineMethod (c$, "moveAtoms", 
function (mNew, matrixRotate, translation, bs, center, isInternal, translationOnly) {
if (!translationOnly) {
if (mNew == null) {
this.matTemp.setM3 (matrixRotate);
} else {
this.matInv.setM3 (matrixRotate);
this.matInv.invert ();
this.ptTemp.set (0, 0, 0);
this.matTemp.mul2 (mNew, matrixRotate);
this.matTemp.mul2 (this.matInv, this.matTemp);
}if (isInternal) {
this.vTemp.setT (center);
this.mat4.setIdentity ();
this.mat4.setTranslation (this.vTemp);
this.mat4t.setToM3 (this.matTemp);
this.mat4.mul (this.mat4t);
this.mat4t.setIdentity ();
this.vTemp.scale (-1);
this.mat4t.setTranslation (this.vTemp);
this.mat4.mul (this.mat4t);
} else {
this.mat4.setToM3 (this.matTemp);
}for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (isInternal) {
this.mat4.rotTrans (this.at[i]);
} else {
this.ptTemp.add (this.at[i]);
this.mat4.rotTrans (this.at[i]);
this.ptTemp.sub (this.at[i]);
}this.taintAtom (i, 2);
}
if (!isInternal) {
this.ptTemp.scale (1 / bs.cardinality ());
if (translation == null) translation =  new JU.V3 ();
translation.add (this.ptTemp);
}}if (translation != null) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.at[i].add (translation);

if (!translationOnly) {
this.mat4t.setIdentity ();
this.mat4t.setTranslation (translation);
this.mat4.mul2 (this.mat4t, this.mat4);
}}this.recalculatePositionDependentQuantities (bs, this.mat4);
}, "JU.M3,JU.M3,JU.V3,JU.BS,JU.P3,~B,~B");
Clazz.defineMethod (c$, "recalculatePositionDependentQuantities", 
function (bs, mat) {
if (this.getHaveStraightness ()) this.calculateStraightness ();
this.recalculateLeadMidpointsAndWingVectors (-1);
var bsModels = this.getModelBitSet (bs, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.sm.refreshShapeTrajectories (i, bs, mat);

this.averageAtomPoint = null;
}, "JU.BS,JU.M4");
Clazz.defineMethod (c$, "getBsBranches", 
function (dihedralList) {
var n = Clazz.doubleToInt (dihedralList.length / 6);
var bsBranches =  new Array (n);
var map =  new java.util.Hashtable ();
for (var i = 0, pt = 0; i < n; i++, pt += 6) {
var dv = dihedralList[pt + 5] - dihedralList[pt + 4];
if (Math.abs (dv) < 1) continue;
var i0 = Clazz.floatToInt (dihedralList[pt + 1]);
var i1 = Clazz.floatToInt (dihedralList[pt + 2]);
var s = "" + i0 + "_" + i1;
if (map.containsKey (s)) continue;
map.put (s, Boolean.TRUE);
var bs = this.vwr.getBranchBitSet (i1, i0, true);
var bonds = this.at[i0].bonds;
var a0 = this.at[i0];
for (var j = 0; j < bonds.length; j++) {
var b = bonds[j];
if (!b.isCovalent ()) continue;
var i2 = b.getOtherAtom (a0).i;
if (i2 == i1) continue;
if (bs.get (i2)) {
bs = null;
break;
}}
bsBranches[i] = bs;
}
return bsBranches;
}, "~A");
Clazz.defineMethod (c$, "getSymMatrices", 
function (modelIndex) {
var n = this.getModelSymmetryCount (modelIndex);
if (n == 0) return null;
var ops =  new Array (n);
var unitcell = this.am[modelIndex].biosymmetry;
if (unitcell == null) unitcell = this.vwr.getModelUnitCell (modelIndex);
for (var i = n; --i >= 0; ) ops[i] = unitcell.getSpaceGroupOperation (i);

return ops;
}, "~N");
});
