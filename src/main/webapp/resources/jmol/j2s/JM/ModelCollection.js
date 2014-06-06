Clazz.declarePackage ("JM");
Clazz.load (["JM.BondCollection", "JU.BS", "$.List", "$.P3", "JW.BoxInfo"], "JM.ModelCollection", ["java.lang.Boolean", "$.Character", "$.Float", "java.util.Date", "$.Hashtable", "JU.AU", "$.P4", "$.PT", "$.Quat", "$.SB", "$.V3", "J.api.Interface", "$.JmolModulationSet", "J.bspt.Bspf", "J.c.PAL", "$.VDW", "JM.Atom", "$.AtomIteratorWithinModel", "$.AtomIteratorWithinModelSet", "$.Bond", "$.BondSet", "$.HBond", "$.LabelToken", "$.Model", "$.StateScript", "JW.BSUtil", "$.Edge", "$.Elements", "$.Escape", "$.JmolMolecule", "$.Logger", "$.Point3fi", "$.Txt", "JV.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bsSymmetry = null;
this.modelSetName = null;
this.am = null;
this.mc = 0;
this.unitCells = null;
this.haveUnitCells = false;
this.modelNumbers = null;
this.modelFileNumbers = null;
this.modelNumbersForAtomLabel = null;
this.modelNames = null;
this.frameTitles = null;
this.elementsPresent = null;
this.isXYZ = false;
this.isPDB = false;
this.modelSetProperties = null;
this.modelSetAuxiliaryInfo = null;
this.someModelsHaveSymmetry = false;
this.someModelsHaveAromaticBonds = false;
this.someModelsHaveFractionalCoordinates = false;
this.ptTemp = null;
this.isBbcageDefault = false;
this.bboxModels = null;
this.bboxAtoms = null;
this.boxInfo = null;
this.stateScripts = null;
this.thisStateModel = 0;
this.trajectorySteps = null;
this.vibrationSteps = null;
this.selectedMolecules = null;
this.showRebondTimes = true;
this.bsAll = null;
this.sm = null;
this.ptTemp1 = null;
this.ptTemp2 = null;
this.proteinStructureTainted = false;
this.symTemp = null;
this.htPeaks = null;
this.vOrientations = null;
this.triangulator = null;
Clazz.instantialize (this, arguments);
}, JM, "ModelCollection", JM.BondCollection);
Clazz.prepareFields (c$, function () {
this.am =  new Array (1);
this.modelNumbers =  Clazz.newIntArray (1, 0);
this.modelFileNumbers =  Clazz.newIntArray (1, 0);
this.modelNumbersForAtomLabel =  new Array (1);
this.modelNames =  new Array (1);
this.frameTitles =  new Array (1);
this.ptTemp =  new JU.P3 ();
this.boxInfo =  new JW.BoxInfo ();
{
this.boxInfo.addBoundBoxPoint (JU.P3.new3 (-10, -10, -10));
this.boxInfo.addBoundBoxPoint (JU.P3.new3 (10, 10, 10));
}this.stateScripts =  new JU.List ();
this.selectedMolecules =  new JU.BS ();
this.ptTemp1 =  new JU.P3 ();
this.ptTemp2 =  new JU.P3 ();
});
Clazz.defineMethod (c$, "mergeModelArrays", 
function (mergeModelSet) {
this.at = mergeModelSet.at;
this.bo = mergeModelSet.bo;
this.stateScripts = mergeModelSet.stateScripts;
this.proteinStructureTainted = mergeModelSet.proteinStructureTainted;
this.thisStateModel = -1;
this.bsSymmetry = mergeModelSet.bsSymmetry;
this.modelFileNumbers = mergeModelSet.modelFileNumbers;
this.modelNumbersForAtomLabel = mergeModelSet.modelNumbersForAtomLabel;
this.modelNames = mergeModelSet.modelNames;
this.modelNumbers = mergeModelSet.modelNumbers;
this.frameTitles = mergeModelSet.frameTitles;
this.mergeAtomArrays (mergeModelSet);
}, "JM.ModelSet");
Clazz.overrideMethod (c$, "releaseModelSet", 
function () {
this.am = null;
this.bsSymmetry = null;
this.bsAll = null;
this.unitCells = null;
this.releaseModelSetBC ();
});
Clazz.defineMethod (c$, "getUnitCell", 
function (modelIndex) {
if (!this.haveUnitCells || modelIndex < 0 || modelIndex >= this.mc) return null;
if (this.am[modelIndex].simpleCage != null) return this.am[modelIndex].simpleCage;
return (this.unitCells == null || modelIndex >= this.unitCells.length || !this.unitCells[modelIndex].haveUnitCell () ? null : this.unitCells[modelIndex]);
}, "~N");
Clazz.defineMethod (c$, "setModelCage", 
function (modelIndex, simpleCage) {
if (modelIndex < 0 || modelIndex >= this.mc) return;
this.am[modelIndex].simpleCage = simpleCage;
this.haveUnitCells = true;
}, "~N,J.api.SymmetryInterface");
Clazz.defineMethod (c$, "getPlaneIntersection", 
function (type, plane, scale, flags, uc) {
var pts = null;
switch (type) {
case 1614417948:
if (uc == null) return null;
pts = uc.getCanonicalCopy (scale, true);
break;
case 1679429641:
pts = this.boxInfo.getCanonicalCopy (scale);
break;
}
var v =  new JU.List ();
v.addLast (pts);
return this.intersectPlane (plane, v, flags);
}, "~N,JU.P4,~N,~N,J.api.SymmetryInterface");
Clazz.defineMethod (c$, "getModelName", 
function (modelIndex) {
return this.mc < 1 ? "" : modelIndex >= 0 ? this.modelNames[modelIndex] : this.modelNumbersForAtomLabel[-1 - modelIndex];
}, "~N");
Clazz.defineMethod (c$, "getModelTitle", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "title");
}, "~N");
Clazz.defineMethod (c$, "getModelFileName", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "fileName");
}, "~N");
Clazz.defineMethod (c$, "getModelFileType", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "fileType");
}, "~N");
Clazz.defineMethod (c$, "setFrameTitle", 
function (bsFrames, title) {
if (Clazz.instanceOf (title, String)) {
for (var i = bsFrames.nextSetBit (0); i >= 0; i = bsFrames.nextSetBit (i + 1)) this.frameTitles[i] = title;

} else {
var list = title;
for (var i = bsFrames.nextSetBit (0), n = 0; i >= 0; i = bsFrames.nextSetBit (i + 1)) if (n < list.length) this.frameTitles[i] = list[n++];

}}, "JU.BS,~O");
Clazz.defineMethod (c$, "getFrameTitle", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.mc ? this.frameTitles[modelIndex] : "");
}, "~N");
Clazz.defineMethod (c$, "getModelNumberForAtomLabel", 
function (modelIndex) {
return this.modelNumbersForAtomLabel[modelIndex];
}, "~N");
Clazz.defineMethod (c$, "calculatePolymers", 
function (groups, groupCount, baseGroupIndex, modelsExcluded) {
if (!this.isPDB) return;
var checkConnections = !this.vwr.getBoolean (603979892);
for (var i = 0; i < this.mc; i++) if ((modelsExcluded == null || !modelsExcluded.get (i)) && this.am[i].isBioModel) {
this.am[i].calculatePolymers (groups, groupCount, baseGroupIndex, modelsExcluded, checkConnections);
return;
}
}, "~A,~N,~N,JU.BS");
Clazz.defineMethod (c$, "getGroups", 
function () {
var n = 0;
for (var i = 0; i < this.mc; i++) n += this.am[i].getGroupCount ();

var groups =  new Array (n);
for (var i = 0, iGroup = 0; i < this.mc; i++) for (var j = 0; j < this.am[i].chainCount; j++) for (var k = 0; k < this.am[i].chains[j].groupCount; k++) {
groups[iGroup] = this.am[i].chains[j].groups[k];
groups[iGroup].groupIndex = iGroup;
iGroup++;
}


return groups;
});
Clazz.defineMethod (c$, "getNotionalUnitcell", 
function () {
var c = this.getUnitCell (0);
return (c == null ? null : c.getNotionalUnitCell ());
});
Clazz.defineMethod (c$, "setCrystallographicDefaults", 
function () {
return !this.isPDB && this.someModelsHaveSymmetry && this.someModelsHaveFractionalCoordinates;
});
Clazz.defineMethod (c$, "getBoundBoxCenter", 
function (modelIndex) {
if (this.isJmolDataFrameForModel (modelIndex)) return  new JU.P3 ();
return this.boxInfo.getBoundBoxCenter ();
}, "~N");
Clazz.defineMethod (c$, "getBoundBoxCornerVector", 
function () {
return this.boxInfo.getBoundBoxCornerVector ();
});
Clazz.defineMethod (c$, "getBboxVertices", 
function () {
return this.boxInfo.getBoundBoxVertices ();
});
Clazz.defineMethod (c$, "getBoundBoxModels", 
function () {
return this.bboxModels;
});
Clazz.defineMethod (c$, "setBoundBox", 
function (pt1, pt2, byCorner, scale) {
this.isBbcageDefault = false;
this.bboxModels = null;
this.bboxAtoms = null;
this.boxInfo.setBoundBox (pt1, pt2, byCorner, scale);
}, "JU.P3,JU.P3,~B,~N");
Clazz.defineMethod (c$, "getBoundBoxCommand", 
function (withOptions) {
if (!withOptions && this.bboxAtoms != null) return "boundbox " + JW.Escape.eBS (this.bboxAtoms);
this.ptTemp.setT (this.boxInfo.getBoundBoxCenter ());
var bbVector = this.boxInfo.getBoundBoxCornerVector ();
var s = (withOptions ? "boundbox " + JW.Escape.eP (this.ptTemp) + " " + JW.Escape.eP (bbVector) + "\n#or\n" : "");
this.ptTemp.sub (bbVector);
s += "boundbox corners " + JW.Escape.eP (this.ptTemp) + " ";
this.ptTemp.scaleAdd2 (2, bbVector, this.ptTemp);
var v = Math.abs (8 * bbVector.x * bbVector.y * bbVector.z);
s += JW.Escape.eP (this.ptTemp) + " # volume = " + v;
return s;
}, "~B");
Clazz.defineMethod (c$, "getDefaultVdwType", 
function (modelIndex) {
return (!this.am[modelIndex].isBioModel ? J.c.VDW.AUTO_BABEL : this.am[modelIndex].hydrogenCount == 0 ? J.c.VDW.AUTO_JMOL : J.c.VDW.AUTO_BABEL);
}, "~N");
Clazz.defineMethod (c$, "setRotationRadius", 
function (modelIndex, angstroms) {
if (this.isJmolDataFrameForModel (modelIndex)) {
this.am[modelIndex].defaultRotationRadius = angstroms;
return false;
}return true;
}, "~N,~N");
Clazz.defineMethod (c$, "calcRotationRadius", 
function (modelIndex, center) {
if (this.isJmolDataFrameForModel (modelIndex)) {
var r = this.am[modelIndex].defaultRotationRadius;
return (r == 0 ? 10 : r);
}var maxRadius = 0;
for (var i = this.ac; --i >= 0; ) {
if (this.isJmolDataFrameForAtom (this.at[i])) {
modelIndex = this.at[i].mi;
while (i >= 0 && this.at[i].mi == modelIndex) i--;

continue;
}var atom = this.at[i];
var distAtom = center.distance (atom);
var outerVdw = distAtom + this.getRadiusVdwJmol (atom);
if (outerVdw > maxRadius) maxRadius = outerVdw;
}
return (maxRadius == 0 ? 10 : maxRadius);
}, "~N,JU.P3");
Clazz.defineMethod (c$, "calcBoundBoxDimensions", 
function (bs, scale) {
if (bs != null && bs.nextSetBit (0) < 0) bs = null;
if (bs == null && this.isBbcageDefault || this.ac == 0) return;
this.bboxModels = this.getModelBitSet (this.bboxAtoms = JW.BSUtil.copy (bs), false);
if (this.calcAtomsMinMax (bs, this.boxInfo) == this.ac) this.isBbcageDefault = true;
if (bs == null) {
if (this.unitCells != null) this.calcUnitCellMinMax ();
}this.boxInfo.setBbcage (scale);
}, "JU.BS,~N");
Clazz.defineMethod (c$, "getBoxInfo", 
function (bs, scale) {
if (bs == null) return this.boxInfo;
var bi =  new JW.BoxInfo ();
this.calcAtomsMinMax (bs, bi);
bi.setBbcage (scale);
return bi;
}, "JU.BS,~N");
Clazz.defineMethod (c$, "calcAtomsMinMax", 
function (bs, boxInfo) {
boxInfo.reset ();
var nAtoms = 0;
var isAll = (bs == null);
var i0 = (isAll ? this.ac - 1 : bs.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bs.nextSetBit (i + 1))) {
nAtoms++;
if (!this.isJmolDataFrameForAtom (this.at[i])) boxInfo.addBoundBoxPoint (this.at[i]);
}
return nAtoms;
}, "JU.BS,JW.BoxInfo");
Clazz.defineMethod (c$, "calcUnitCellMinMax", 
 function () {
for (var i = 0; i < this.mc; i++) {
if (!this.unitCells[i].getCoordinatesAreFractional ()) continue;
var vertices = this.unitCells[i].getUnitCellVertices ();
for (var j = 0; j < 8; j++) this.boxInfo.addBoundBoxPoint (vertices[j]);

}
});
Clazz.defineMethod (c$, "calcRotationRadiusBs", 
function (bs) {
var center = this.getAtomSetCenter (bs);
var maxRadius = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var atom = this.at[i];
var distAtom = center.distance (atom);
var outerVdw = distAtom + this.getRadiusVdwJmol (atom);
if (outerVdw > maxRadius) maxRadius = outerVdw;
}
return (maxRadius == 0 ? 10 : maxRadius);
}, "JU.BS");
Clazz.defineMethod (c$, "getCenterAndPoints", 
function (vAtomSets, addCenters) {
var bsAtoms1;
var bsAtoms2;
var n = (addCenters ? 1 : 0);
for (var ii = vAtomSets.size (); --ii >= 0; ) {
var bss = vAtomSets.get (ii);
bsAtoms1 = bss[0];
if (Clazz.instanceOf (bss[1], JU.BS)) {
bsAtoms2 = bss[1];
n += Math.min (bsAtoms1.cardinality (), bsAtoms2.cardinality ());
} else {
n += Math.min (bsAtoms1.cardinality (), (bss[1]).length);
}}
var points =  Clazz.newArray (2, n, null);
if (addCenters) {
points[0][0] =  new JU.P3 ();
points[1][0] =  new JU.P3 ();
}for (var ii = vAtomSets.size (); --ii >= 0; ) {
var bss = vAtomSets.get (ii);
bsAtoms1 = bss[0];
if (Clazz.instanceOf (bss[1], JU.BS)) {
bsAtoms2 = bss[1];
for (var i = bsAtoms1.nextSetBit (0), j = bsAtoms2.nextSetBit (0); i >= 0 && j >= 0; i = bsAtoms1.nextSetBit (i + 1), j = bsAtoms2.nextSetBit (j + 1)) {
points[0][--n] = this.at[i];
points[1][n] = this.at[j];
if (addCenters) {
points[0][0].add (this.at[i]);
points[1][0].add (this.at[j]);
}}
} else {
var coords = bss[1];
for (var i = bsAtoms1.nextSetBit (0), j = 0; i >= 0 && j < coords.length; i = bsAtoms1.nextSetBit (i + 1), j++) {
points[0][--n] = this.at[i];
points[1][n] = coords[j];
if (addCenters) {
points[0][0].add (this.at[i]);
points[1][0].add (coords[j]);
}}
}}
if (addCenters) {
points[0][0].scale (1 / (points[0].length - 1));
points[1][0].scale (1 / (points[1].length - 1));
}return points;
}, "JU.List,~B");
Clazz.defineMethod (c$, "getAtomSetCenter", 
function (bs) {
var ptCenter =  new JU.P3 ();
var nPoints = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (!this.isJmolDataFrameForAtom (this.at[i])) {
nPoints++;
ptCenter.add (this.at[i]);
}}
if (nPoints > 0) ptCenter.scale (1.0 / nPoints);
return ptCenter;
}, "JU.BS");
Clazz.defineMethod (c$, "getAverageAtomPoint", 
function () {
if (this.averageAtomPoint == null) (this.averageAtomPoint =  new JU.P3 ()).setT (this.getAtomSetCenter (this.vwr.getAllAtoms ()));
return this.averageAtomPoint;
});
Clazz.defineMethod (c$, "setAPm", 
function (bs, tok, iValue, fValue, sValue, values, list) {
this.setAPa (bs, tok, iValue, fValue, sValue, values, list);
switch (tok) {
case 1095763990:
case 1632634891:
if (this.vwr.getBoolean (603979944)) this.assignAromaticBonds ();
break;
}
}, "JU.BS,~N,~N,~N,~S,~A,~A");
Clazz.defineMethod (c$, "addStateScript", 
function (script1, bsBonds, bsAtoms1, bsAtoms2, script2, addFrameNumber, postDefinitions) {
var iModel = this.vwr.getCurrentModelIndex ();
if (addFrameNumber) {
if (this.thisStateModel != iModel) script1 = "frame " + (iModel < 0 ? "all #" + iModel : this.getModelNumberDotted (iModel)) + ";\n  " + script1;
this.thisStateModel = iModel;
} else {
this.thisStateModel = -1;
}var stateScript =  new JM.StateScript (this.thisStateModel, script1, bsBonds, bsAtoms1, bsAtoms2, script2, postDefinitions);
if (stateScript.isValid ()) {
this.stateScripts.addLast (stateScript);
}return stateScript;
}, "~S,JU.BS,JU.BS,JU.BS,~S,~B,~B");
Clazz.defineMethod (c$, "calculateStructuresAllExcept", 
function (alreadyDefined, asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha) {
this.freezeModels ();
var ret = "";
var bsModels = JW.BSUtil.copyInvert (alreadyDefined, this.mc);
if (setStructure) this.setDefaultStructure (bsModels);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) {
ret += this.am[i].calculateStructures (asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha);
}
if (setStructure) {
this.setStructureIndexes ();
}return ret;
}, "JU.BS,~B,~B,~B,~B,~B");
Clazz.defineMethod (c$, "setDefaultStructure", 
function (bsModels) {
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) if (this.am[i].isBioModel && this.am[i].defaultStructure == null) this.am[i].defaultStructure = this.getProteinStructureState (this.am[i].bsAtoms, false, false, 0);

}, "JU.BS");
Clazz.defineMethod (c$, "setProteinType", 
function (bs, type) {
var monomerIndexCurrent = -1;
var iLast = -1;
var bsModels = this.getModelBitSet (bs, false);
this.setDefaultStructure (bsModels);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (iLast != i - 1) monomerIndexCurrent = -1;
monomerIndexCurrent = this.at[i].group.setProteinStructureType (type, monomerIndexCurrent);
var modelIndex = this.at[i].mi;
this.proteinStructureTainted = this.am[modelIndex].structureTainted = true;
iLast = i = this.at[i].group.lastAtomIndex;
}
var lastStrucNo =  Clazz.newIntArray (this.mc, 0);
for (var i = 0; i < this.ac; ) {
var modelIndex = this.at[i].mi;
if (!bsModels.get (modelIndex)) {
i = this.am[modelIndex].firstAtomIndex + this.am[modelIndex].ac;
continue;
}iLast = this.at[i].getStrucNo ();
if (iLast < 1000 && iLast > lastStrucNo[modelIndex]) lastStrucNo[modelIndex] = iLast;
i = this.at[i].group.lastAtomIndex + 1;
}
for (var i = 0; i < this.ac; ) {
var modelIndex = this.at[i].mi;
if (!bsModels.get (modelIndex)) {
i = this.am[modelIndex].firstAtomIndex + this.am[modelIndex].ac;
continue;
}if (this.at[i].getStrucNo () > 1000) this.at[i].group.setStrucNo (++lastStrucNo[modelIndex]);
i = this.at[i].group.lastAtomIndex + 1;
}
}, "JU.BS,J.c.STR");
Clazz.defineMethod (c$, "freezeModels", 
function () {
for (var iModel = this.mc; --iModel >= 0; ) this.am[iModel].freeze ();

});
Clazz.defineMethod (c$, "getStructureList", 
function () {
return this.vwr.getStructureList ();
});
Clazz.defineMethod (c$, "setStructureList", 
function (structureList) {
for (var iModel = this.mc; --iModel >= 0; ) this.am[iModel].setStructureList (structureList);

}, "java.util.Map");
Clazz.defineMethod (c$, "setConformation", 
function (bsAtoms) {
var bsModels = this.getModelBitSet (bsAtoms, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.am[i].setConformation (bsAtoms);

return bsAtoms;
}, "JU.BS");
Clazz.defineMethod (c$, "getConformation", 
function (modelIndex, conformationIndex, doSet) {
var bs =  new JU.BS ();
for (var i = this.mc; --i >= 0; ) if (i == modelIndex || modelIndex < 0) {
var altLocs = this.getAltLocListInModel (i);
var nAltLocs = this.getAltLocCountInModel (i);
if (conformationIndex > 0 && conformationIndex >= nAltLocs) continue;
var bsConformation = this.vwr.getModelUndeletedAtomsBitSet (i);
if (conformationIndex >= 0) {
if (!this.am[i].getPdbConformation (bsConformation, conformationIndex)) for (var c = nAltLocs; --c >= 0; ) if (c != conformationIndex) bsConformation.andNot (this.getAtomBitsMDa (1048607, altLocs.substring (c, c + 1)));

}if (bsConformation.nextSetBit (0) >= 0) {
bs.or (bsConformation);
if (doSet) this.am[i].setConformation (bsConformation);
}}
return bs;
}, "~N,~N,~B");
Clazz.defineMethod (c$, "getHeteroList", 
function (modelIndex) {
var htFull =  new java.util.Hashtable ();
var ok = false;
for (var i = this.mc; --i >= 0; ) if (modelIndex < 0 || i == modelIndex) {
var ht = this.getModelAuxiliaryInfoValue (i, "hetNames");
if (ht == null) continue;
ok = true;
for (var entry, $entry = ht.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getKey ();
htFull.put (key, entry.getValue ());
}
}
return (ok ? htFull : this.getModelSetAuxiliaryInfoValue ("hetNames"));
}, "~N");
Clazz.defineMethod (c$, "getModelSetProperties", 
function () {
return this.modelSetProperties;
});
Clazz.defineMethod (c$, "getModelSetAuxiliaryInfo", 
function () {
return this.modelSetAuxiliaryInfo;
});
Clazz.defineMethod (c$, "getModelSetProperty", 
function (propertyName) {
return (this.modelSetProperties == null ? null : this.modelSetProperties.getProperty (propertyName));
}, "~S");
Clazz.defineMethod (c$, "getModelSetAuxiliaryInfoValue", 
function (keyName) {
return (this.modelSetAuxiliaryInfo == null ? null : this.modelSetAuxiliaryInfo.get (keyName));
}, "~S");
Clazz.defineMethod (c$, "getModelSetAuxiliaryInfoBoolean", 
function (keyName) {
var val = this.getModelSetAuxiliaryInfoValue (keyName);
return (Clazz.instanceOf (val, Boolean) && (val).booleanValue ());
}, "~S");
Clazz.defineMethod (c$, "mergeTrajectories", 
function (isTrajectory) {
if (this.trajectorySteps == null) {
if (!isTrajectory) return 0;
this.trajectorySteps =  new JU.List ();
}for (var i = this.trajectorySteps.size (); i < this.mc; i++) this.trajectorySteps.addLast (null);

return this.mc;
}, "~B");
Clazz.defineMethod (c$, "getTrajectoryIndex", 
function (modelIndex) {
return this.am[modelIndex].trajectoryBaseIndex;
}, "~N");
Clazz.defineMethod (c$, "isTrajectory", 
function (modelIndex) {
return this.am[modelIndex].isTrajectory;
}, "~N");
Clazz.defineMethod (c$, "isTrajectoryMeasurement", 
function (countPlusIndices) {
if (countPlusIndices == null) return false;
var count = countPlusIndices[0];
var atomIndex;
for (var i = 1; i <= count; i++) if ((atomIndex = countPlusIndices[i]) >= 0 && this.am[this.at[atomIndex].mi].isTrajectory) return true;

return false;
}, "~A");
Clazz.defineMethod (c$, "getModelBitSet", 
function (atomList, allTrajectories) {
var bs =  new JU.BS ();
var modelIndex = 0;
var isAll = (atomList == null);
var i0 = (isAll ? 0 : atomList.nextSetBit (0));
for (var i = i0; i >= 0 && i < this.ac; i = (isAll ? i + 1 : atomList.nextSetBit (i + 1))) {
bs.set (modelIndex = this.at[i].mi);
if (allTrajectories) {
var iBase = this.am[modelIndex].trajectoryBaseIndex;
for (var j = 0; j < this.mc; j++) if (this.am[j].trajectoryBaseIndex == iBase) bs.set (j);

}i = this.am[modelIndex].firstAtomIndex + this.am[modelIndex].ac - 1;
}
return bs;
}, "JU.BS,~B");
Clazz.defineMethod (c$, "getIterativeModels", 
function (allowJmolData) {
var bs =  new JU.BS ();
for (var i = 0; i < this.mc; i++) {
if (!allowJmolData && this.isJmolDataFrameForModel (i)) continue;
if (this.am[i].trajectoryBaseIndex == i) bs.set (i);
}
return bs;
}, "~B");
Clazz.defineMethod (c$, "isTrajectorySubFrame", 
function (i) {
return (this.am[i].isTrajectory && this.am[i].trajectoryBaseIndex != i);
}, "~N");
Clazz.defineMethod (c$, "selectDisplayedTrajectories", 
function (bs) {
for (var i = 0; i < this.mc; i++) {
if (this.am[i].isTrajectory && this.at[this.am[i].firstAtomIndex].mi != i) bs.clear (i);
}
return bs;
}, "JU.BS");
Clazz.defineMethod (c$, "fillAtomData", 
function (atomData, mode) {
if ((mode & 4) != 0) {
this.getMolecules ();
atomData.bsMolecules =  new Array (this.molecules.length);
atomData.atomMolecule =  Clazz.newIntArray (this.ac, 0);
var bs;
for (var i = 0; i < this.molecules.length; i++) {
bs = atomData.bsMolecules[i] = this.molecules[i].atomList;
for (var iAtom = bs.nextSetBit (0); iAtom >= 0; iAtom = bs.nextSetBit (iAtom + 1)) atomData.atomMolecule[iAtom] = i;

}
}if ((mode & 8) != 0) {
var nH =  Clazz.newIntArray (1, 0);
atomData.hAtomRadius = this.vwr.getVanderwaalsMar (1) / 1000;
atomData.hAtoms = this.calculateHydrogens (atomData.bsSelected, nH, false, true, null);
atomData.hydrogenAtomCount = nH[0];
return;
}if (atomData.modelIndex < 0) atomData.firstAtomIndex = (atomData.bsSelected == null ? 0 : Math.max (0, atomData.bsSelected.nextSetBit (0)));
 else atomData.firstAtomIndex = this.am[atomData.modelIndex].firstAtomIndex;
atomData.lastModelIndex = atomData.firstModelIndex = (this.ac == 0 ? 0 : this.at[atomData.firstAtomIndex].mi);
atomData.modelName = this.getModelNumberDotted (atomData.firstModelIndex);
this.fillADa (atomData, mode);
}, "J.atomdata.AtomData,~N");
Clazz.defineMethod (c$, "getModelNumberDotted", 
function (modelIndex) {
return (this.mc < 1 || modelIndex >= this.mc || modelIndex < 0 ? "" : JW.Escape.escapeModelFileNumber (this.modelFileNumbers[modelIndex]));
}, "~N");
Clazz.defineMethod (c$, "getModelNumber", 
function (modelIndex) {
if (modelIndex == 2147483647) modelIndex = this.mc - 1;
return this.modelNumbers[modelIndex];
}, "~N");
Clazz.defineMethod (c$, "getModelFileNumber", 
function (modelIndex) {
return this.modelFileNumbers[modelIndex];
}, "~N");
Clazz.defineMethod (c$, "getModelProperties", 
function (modelIndex) {
return this.am[modelIndex].properties;
}, "~N");
Clazz.defineMethod (c$, "getModelProperty", 
function (modelIndex, property) {
var props = this.am[modelIndex].properties;
return props == null ? null : props.getProperty (property);
}, "~N,~S");
Clazz.defineMethod (c$, "getModelAuxiliaryInfo", 
function (modelIndex) {
return (modelIndex < 0 ? null : this.am[modelIndex].auxiliaryInfo);
}, "~N");
Clazz.defineMethod (c$, "setModelAuxiliaryInfo", 
function (modelIndex, key, value) {
this.am[modelIndex].auxiliaryInfo.put (key, value);
}, "~N,~O,~O");
Clazz.defineMethod (c$, "getModelAuxiliaryInfoValue", 
function (modelIndex, key) {
if (modelIndex < 0) {
return null;
}return this.am[modelIndex].auxiliaryInfo.get (key);
}, "~N,~S");
Clazz.defineMethod (c$, "getModelAuxiliaryInfoBoolean", 
function (modelIndex, keyName) {
var info = this.am[modelIndex].auxiliaryInfo;
return (info != null && info.containsKey (keyName) && (info.get (keyName)).booleanValue ());
}, "~N,~S");
Clazz.defineMethod (c$, "getModelAuxiliaryInfoInt", 
function (modelIndex, keyName) {
var info = this.am[modelIndex].auxiliaryInfo;
if (info != null && info.containsKey (keyName)) {
return (info.get (keyName)).intValue ();
}return -2147483648;
}, "~N,~S");
Clazz.defineMethod (c$, "getModelAtomProperty", 
function (atom, text) {
var data = this.getModelAuxiliaryInfoValue (atom.mi, text);
if (!(Clazz.instanceOf (data, Array))) return "";
var sdata = data;
var iatom = atom.i - this.am[atom.mi].firstAtomIndex;
return (iatom < sdata.length ? sdata[iatom].toString () : "");
}, "JM.Atom,~S");
Clazz.defineMethod (c$, "getInsertionCountInModel", 
function (modelIndex) {
return this.am[modelIndex].nInsertions;
}, "~N");
c$.modelFileNumberFromFloat = Clazz.defineMethod (c$, "modelFileNumberFromFloat", 
function (fDotM) {
var file = Clazz.doubleToInt (Math.floor (fDotM));
var model = Clazz.doubleToInt (Math.floor ((fDotM - file + 0.00001) * 10000));
while (model != 0 && model % 10 == 0) model /= 10;

return file * 1000000 + model;
}, "~N");
Clazz.defineMethod (c$, "getAltLocCountInModel", 
function (modelIndex) {
return this.am[modelIndex].nAltLocs;
}, "~N");
Clazz.defineMethod (c$, "getChainCount", 
function (addWater) {
var chainCount = 0;
for (var i = this.mc; --i >= 0; ) chainCount += this.am[i].getChainCount (addWater);

return chainCount;
}, "~B");
Clazz.defineMethod (c$, "getBioPolymerCount", 
function () {
var polymerCount = 0;
for (var i = this.mc; --i >= 0; ) if (!this.isTrajectorySubFrame (i)) polymerCount += this.am[i].getBioPolymerCount ();

return polymerCount;
});
Clazz.defineMethod (c$, "getBioPolymerCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.getBioPolymerCount () : this.isTrajectorySubFrame (modelIndex) ? 0 : this.am[modelIndex].getBioPolymerCount ());
}, "~N");
Clazz.defineMethod (c$, "getPolymerPointsAndVectors", 
function (bs, vList, isTraceAlpha, sheetSmoothing) {
for (var i = 0; i < this.mc; ++i) this.am[i].getPolymerPointsAndVectors (bs, vList, isTraceAlpha, sheetSmoothing);

}, "JU.BS,JU.List,~B,~N");
Clazz.defineMethod (c$, "recalculateLeadMidpointsAndWingVectors", 
function (modelIndex) {
if (modelIndex < 0) {
for (var i = 0; i < this.mc; i++) if (!this.isTrajectorySubFrame (i)) this.am[i].recalculateLeadMidpointsAndWingVectors ();

return;
}this.am[modelIndex].recalculateLeadMidpointsAndWingVectors ();
}, "~N");
Clazz.defineMethod (c$, "getPolymerLeadMidPoints", 
function (iModel, iPolymer) {
return this.am[iModel].getPolymerLeadMidPoints (iPolymer);
}, "~N,~N");
Clazz.defineMethod (c$, "getChainCountInModel", 
function (modelIndex, countWater) {
if (modelIndex < 0) return this.getChainCount (countWater);
return this.am[modelIndex].getChainCount (countWater);
}, "~N,~B");
Clazz.defineMethod (c$, "getGroupCount", 
function () {
var groupCount = 0;
for (var i = this.mc; --i >= 0; ) groupCount += this.am[i].getGroupCount ();

return groupCount;
});
Clazz.defineMethod (c$, "getGroupCountInModel", 
function (modelIndex) {
if (modelIndex < 0) return this.getGroupCount ();
return this.am[modelIndex].getGroupCount ();
}, "~N");
Clazz.defineMethod (c$, "calcSelectedGroupsCount", 
function (bsSelected) {
for (var i = this.mc; --i >= 0; ) this.am[i].calcSelectedGroupsCount (bsSelected);

}, "JU.BS");
Clazz.defineMethod (c$, "calcSelectedMonomersCount", 
function (bsSelected) {
for (var i = this.mc; --i >= 0; ) this.am[i].calcSelectedMonomersCount (bsSelected);

}, "JU.BS");
Clazz.defineMethod (c$, "calcRasmolHydrogenBonds", 
function (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds) {
var isSame = (bsB == null || bsA.equals (bsB));
for (var i = this.mc; --i >= 0; ) if (this.am[i].isBioModel && this.am[i].trajectoryBaseIndex == i) {
if (vHBonds == null) {
this.am[i].clearRasmolHydrogenBonds (bsA);
if (!isSame) this.am[i].clearRasmolHydrogenBonds (bsB);
}this.am[i].getRasmolHydrogenBonds (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds);
}
}, "JU.BS,JU.BS,JU.List,~B,~N,~B,JU.BS");
Clazz.defineMethod (c$, "calculateStraightness", 
function () {
if (this.getHaveStraightness ()) return;
var ctype = 'S';
var qtype = this.vwr.getQuaternionFrame ();
var mStep = this.vwr.getInt (553648146);
for (var i = this.mc; --i >= 0; ) this.am[i].calculateStraightness (this.vwr, ctype, qtype, mStep);

this.setHaveStraightness (true);
});
Clazz.defineMethod (c$, "getAtomGroupQuaternions", 
function (bsAtoms, nMax, qtype) {
var n = 0;
var v =  new JU.List ();
for (var i = bsAtoms.nextSetBit (0); i >= 0 && n < nMax; i = bsAtoms.nextSetBit (i + 1)) {
var g = this.at[i].group;
var q = g.getQuaternion (qtype);
if (q == null) {
if (g.seqcode == -2147483648) q = g.getQuaternionFrame (this.at);
if (q == null) continue;
}n++;
v.addLast (q);
i = g.lastAtomIndex;
}
return v.toArray ( new Array (v.size ()));
}, "JU.BS,~N,~S");
Clazz.defineMethod (c$, "getPdbAtomData", 
function (bs, out) {
if (this.ac == 0 || bs.nextSetBit (0) < 0) return "";
if (out == null) out = this.vwr.getOutputChannel (null, null);
var iModel = this.at[bs.nextSetBit (0)].mi;
var iModelLast = -1;
var isPQR = "PQR".equals (out.getType ());
var occTemp = "%6.2Q%6.2b          ";
if (isPQR) {
occTemp = "%8.4P%7.4V       ";
var charge = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) charge += this.at[i].getPartialCharge ();

out.append ("REMARK   1 PQR file generated by Jmol " + JV.Viewer.getJmolVersion ()).append ("\nREMARK   1 " + "created " + ( new java.util.Date ())).append ("\nREMARK   1 Forcefield Used: unknown\nREMARK   1").append ("\nREMARK   5").append ("\nREMARK   6 Total charge on this protein: " + charge + " e\nREMARK   6\n");
}var lastAtomIndex = bs.length () - 1;
var showModels = (iModel != this.at[lastAtomIndex].mi);
var sbCONECT = (showModels ? null :  new JU.SB ());
var isMultipleBondPDB = this.am[iModel].isPdbWithMultipleBonds;
var tokens;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = this.at[i];
if (showModels && a.mi != iModelLast) {
if (iModelLast != -1) out.append ("ENDMDL\n");
iModelLast = a.mi;
out.append ("MODEL     " + (iModelLast + 1) + "\n");
}var sa = a.getAtomName ();
var leftJustify = (a.getElementSymbol ().length == 2 || sa.length >= 4 || Character.isDigit (sa.charAt (0)));
var isBiomodel = this.am[a.mi].isBioModel;
var isHetero = a.isHetero ();
if (!isBiomodel) tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "HETATM%5.-5i %-4.4a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "HETATM%5.-5i  %-3.3a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else if (isHetero) tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "HETATM%5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "HETATM%5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "ATOM  %5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "ATOM  %5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
var XX = a.getElementSymbolIso (false).toUpperCase ();
out.append (JM.LabelToken.formatLabelAtomArray (this.vwr, a, tokens, '\0', null)).append (XX.length == 1 ? " " + XX : XX.substring (0, 2)).append ("  \n");
if (!showModels && (!isBiomodel || isHetero || isMultipleBondPDB)) {
var bonds = a.getBonds ();
if (bonds != null) for (var j = 0; j < bonds.length; j++) {
var iThis = a.getAtomNumber ();
var a2 = bonds[j].getOtherAtom (a);
if (!bs.get (a2.i)) continue;
var n = bonds[j].getCovalentOrder ();
if (n == 1 && isMultipleBondPDB && !isHetero) continue;
var iOther = a2.getAtomNumber ();
switch (n) {
case 2:
case 3:
if (iOther < iThis) continue;
case 1:
sbCONECT.append ("CONECT").append (JW.Txt.formatStringI ("%5i", "i", iThis));
for (var k = 0; k < n; k++) sbCONECT.append (JW.Txt.formatStringI ("%5i", "i", iOther));

sbCONECT.appendC ('\n');
break;
}
}
}}
if (showModels) out.append ("ENDMDL\n");
 else out.append (sbCONECT.toString ());
return out.toString ();
}, "JU.BS,JU.OC");
Clazz.defineMethod (c$, "getPdbData", 
function (modelIndex, type, bsSelected, parameters, out) {
if (this.isJmolDataFrameForModel (modelIndex)) modelIndex = this.getJmolDataSourceFrame (modelIndex);
if (modelIndex < 0) return "";
var isPDB = this.am[modelIndex].isBioModel;
if (parameters == null && !isPDB) return null;
var model = this.am[modelIndex];
if (out == null) out = this.vwr.getOutputChannel (null, null);
var pdbCONECT =  new JU.SB ();
var isDraw = (type.indexOf ("draw") >= 0);
var bsAtoms = null;
var bsWritten =  new JU.BS ();
var ctype = '\u0000';
var tokens = this.getLabeler ().compile (this.vwr, "ATOM  %-6i%4a%1A%3n %1c%4R%1E   ", '\0', null);
if (parameters == null) {
ctype = (type.length > 11 && type.indexOf ("quaternion ") >= 0 ? type.charAt (11) : 'R');
model.getPdbData (this.vwr, type, ctype, isDraw, bsSelected, out, tokens, pdbCONECT, bsWritten);
bsAtoms = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
} else {
bsAtoms = parameters[0];
var dataX = parameters[1];
var dataY = parameters[2];
var dataZ = parameters[3];
var haveZ = (dataZ != null);
var minXYZ = parameters[4];
var maxXYZ = parameters[5];
var factors = parameters[6];
var center = parameters[7];
out.append ("REMARK   6 Jmol PDB-encoded data: ").append (type).append (";\n");
out.append ("REMARK   6 Jmol data").append (" min = ").append (JW.Escape.eP (minXYZ)).append (" max = ").append (JW.Escape.eP (maxXYZ)).append (" unScaledXyz = xyz * ").append (JW.Escape.eP (factors)).append (" + ").append (JW.Escape.eP (center)).append (";\n");
var strExtra = "";
var atomLast = null;
for (var i = bsAtoms.nextSetBit (0), n = 0; i >= 0; i = bsAtoms.nextSetBit (i + 1), n++) {
var x = dataX[n];
var y = dataY[n];
var z = (haveZ ? dataZ[n] : 0);
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z)) continue;
var a = this.at[i];
out.append (JM.LabelToken.formatLabelAtomArray (this.vwr, a, tokens, '\0', null));
if (isPDB) bsWritten.set (i);
out.append (JW.Txt.sprintf ("%-8.2f%-8.2f%-10.2f    %6.3f          %2s    %s\n", "ssF", [a.getElementSymbolIso (false).toUpperCase (), strExtra, [x, y, z, 0]]));
if (atomLast != null && atomLast.getPolymerIndexInModel () == a.getPolymerIndexInModel ()) pdbCONECT.append ("CONECT").append (JW.Txt.formatStringI ("%5i", "i", atomLast.getAtomNumber ())).append (JW.Txt.formatStringI ("%5i", "i", a.getAtomNumber ())).appendC ('\n');
atomLast = a;
}
}out.append (pdbCONECT.toString ());
if (isDraw) return out.toString ();
bsSelected.and (bsAtoms);
if (isPDB) out.append ("\n\n" + this.getProteinStructureState (bsWritten, false, ctype == 'R', 1));
return out.toString ();
}, "~N,~S,JU.BS,~A,JU.OC");
Clazz.defineMethod (c$, "isJmolDataFrameForModel", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.mc && this.am[modelIndex].isJmolDataFrame);
}, "~N");
Clazz.defineMethod (c$, "isJmolDataFrameForAtom", 
 function (atom) {
return (this.am[atom.mi].isJmolDataFrame);
}, "JM.Atom");
Clazz.defineMethod (c$, "setJmolDataFrame", 
function (type, modelIndex, modelDataIndex) {
var model = this.am[type == null ? this.am[modelDataIndex].dataSourceFrame : modelIndex];
if (type == null) {
type = this.am[modelDataIndex].jmolFrameType;
}if (modelIndex >= 0) {
if (model.dataFrames == null) {
model.dataFrames =  new java.util.Hashtable ();
}this.am[modelDataIndex].dataSourceFrame = modelIndex;
this.am[modelDataIndex].jmolFrameType = type;
model.dataFrames.put (type, Integer.$valueOf (modelDataIndex));
}if (type.startsWith ("quaternion") && type.indexOf ("deriv") < 0) {
type = type.substring (0, type.indexOf (" "));
model.dataFrames.put (type, Integer.$valueOf (modelDataIndex));
}}, "~S,~N,~N");
Clazz.defineMethod (c$, "getJmolDataFrameIndex", 
function (modelIndex, type) {
if (this.am[modelIndex].dataFrames == null) {
return -1;
}var index = this.am[modelIndex].dataFrames.get (type);
return (index == null ? -1 : index.intValue ());
}, "~N,~S");
Clazz.defineMethod (c$, "clearDataFrameReference", 
function (modelIndex) {
for (var i = 0; i < this.mc; i++) {
var df = this.am[i].dataFrames;
if (df == null) {
continue;
}var e = df.values ().iterator ();
while (e.hasNext ()) {
if ((e.next ()).intValue () == modelIndex) {
e.remove ();
}}
}
}, "~N");
Clazz.defineMethod (c$, "getJmolFrameType", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.mc ? this.am[modelIndex].jmolFrameType : "modelSet");
}, "~N");
Clazz.defineMethod (c$, "getJmolDataSourceFrame", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.mc ? this.am[modelIndex].dataSourceFrame : -1);
}, "~N");
Clazz.defineMethod (c$, "saveModelOrientation", 
function (modelIndex, orientation) {
this.am[modelIndex].orientation = orientation;
}, "~N,JM.Orientation");
Clazz.defineMethod (c$, "getModelOrientation", 
function (modelIndex) {
return this.am[modelIndex].orientation;
}, "~N");
Clazz.defineMethod (c$, "getPDBHeader", 
function (modelIndex) {
return (this.am[modelIndex].isBioModel ? this.am[modelIndex].getFullPDBHeader () : this.getFileHeader (modelIndex));
}, "~N");
Clazz.defineMethod (c$, "getFileHeader", 
function (modelIndex) {
if (modelIndex < 0) return "";
if (this.am[modelIndex].isBioModel) return this.am[modelIndex].getFullPDBHeader ();
var info = this.getModelAuxiliaryInfoValue (modelIndex, "fileHeader");
if (info == null) info = this.modelSetName;
if (info != null) return info;
return "no header information found";
}, "~N");
Clazz.defineMethod (c$, "getAltLocIndexInModel", 
function (modelIndex, alternateLocationID) {
if (alternateLocationID == '\0') {
return 0;
}var altLocList = this.getAltLocListInModel (modelIndex);
if (altLocList.length == 0) {
return 0;
}return altLocList.indexOf (alternateLocationID) + 1;
}, "~N,~S");
Clazz.defineMethod (c$, "getInsertionCodeIndexInModel", 
function (modelIndex, insertionCode) {
if (insertionCode == '\0') return 0;
var codeList = this.getInsertionListInModel (modelIndex);
if (codeList.length == 0) return 0;
return codeList.indexOf (insertionCode) + 1;
}, "~N,~S");
Clazz.defineMethod (c$, "getAltLocListInModel", 
function (modelIndex) {
if (modelIndex < 0) return "";
var str = this.getModelAuxiliaryInfoValue (modelIndex, "altLocs");
return (str == null ? "" : str);
}, "~N");
Clazz.defineMethod (c$, "getInsertionListInModel", 
 function (modelIndex) {
var str = this.getModelAuxiliaryInfoValue (modelIndex, "insertionCodes");
return (str == null ? "" : str);
}, "~N");
Clazz.defineMethod (c$, "getModelSymmetryCount", 
function (modelIndex) {
return (this.am[modelIndex].biosymmetryCount > 0 ? this.am[modelIndex].biosymmetryCount : this.unitCells == null || this.unitCells[modelIndex] == null ? 0 : this.unitCells[modelIndex].getSpaceGroupOperationCount ());
}, "~N");
Clazz.defineMethod (c$, "getSymmetryInfoString", 
function (modelIndex, spaceGroup, symOp, pt1, pt2, drawID, labelOnly) {
var sginfo = this.getSymTemp (true).getSpaceGroupInfo (this, modelIndex, spaceGroup, symOp, pt1, pt2, drawID);
if (sginfo == null) return "";
return this.symTemp.getSymmetryInfoString (sginfo, symOp, drawID, labelOnly);
}, "~N,~S,~N,JU.P3,JU.P3,~S,~B");
Clazz.defineMethod (c$, "getModelCellRange", 
function (modelIndex) {
if (this.unitCells == null) return null;
return this.unitCells[modelIndex].getCellRange ();
}, "~N");
Clazz.defineMethod (c$, "getLastVibrationVector", 
function (modelIndex, tok) {
if (this.vibrations != null) for (var i = this.ac; --i >= 0; ) if ((modelIndex < 0 || this.at[i].mi == modelIndex) && this.vibrations[i] != null && this.vibrations[i].length () > 0 && (tok == 0 || (tok == 1276121113) == (Clazz.instanceOf (this.vibrations[i], J.api.JmolModulationSet)))) return i;

return -1;
}, "~N,~N");
Clazz.defineMethod (c$, "getModulationList", 
function (bs, type, t456) {
var list =  new JU.List ();
if (this.vibrations != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) if (Clazz.instanceOf (this.vibrations[i], J.api.JmolModulationSet)) list.addLast ((this.vibrations[i]).getModulation (type, t456));
 else list.addLast (null);

return list;
}, "JU.BS,~S,JU.P3");
Clazz.defineMethod (c$, "getElementsPresentBitSet", 
function (modelIndex) {
if (modelIndex >= 0) return this.elementsPresent[modelIndex];
var bs =  new JU.BS ();
for (var i = 0; i < this.mc; i++) bs.or (this.elementsPresent[i]);

return bs;
}, "~N");
Clazz.defineMethod (c$, "getSymmetryInfoAsStringForModel", 
 function (modelIndex) {
var unitCell = this.getUnitCell (modelIndex);
return (unitCell == null ? "no symmetry information" : unitCell.getSymmetryInfoString ());
}, "~N");
Clazz.defineMethod (c$, "getMoleculeIndex", 
function (atomIndex, inModel) {
if (this.moleculeCount == 0) this.getMolecules ();
for (var i = 0; i < this.moleculeCount; i++) {
if (this.molecules[i].atomList.get (atomIndex)) return (inModel ? this.molecules[i].indexInModel : i);
}
return 0;
}, "~N,~B");
Clazz.defineMethod (c$, "getMoleculeBitSet", 
function (bs) {
if (this.moleculeCount == 0) this.getMolecules ();
var bsResult = JW.BSUtil.copy (bs);
var bsInitial = JW.BSUtil.copy (bs);
var i = 0;
var bsTemp =  new JU.BS ();
while ((i = bsInitial.length () - 1) >= 0) {
bsTemp = this.getMoleculeBitSetForAtom (i);
if (bsTemp == null) {
bsInitial.clear (i);
bsResult.clear (i);
continue;
}bsInitial.andNot (bsTemp);
bsResult.or (bsTemp);
}
return bsResult;
}, "JU.BS");
Clazz.defineMethod (c$, "getMoleculeBitSetForAtom", 
function (atomIndex) {
if (this.moleculeCount == 0) this.getMolecules ();
for (var i = 0; i < this.moleculeCount; i++) if (this.molecules[i].atomList.get (atomIndex)) return this.molecules[i].atomList;

return null;
}, "~N");
Clazz.defineMethod (c$, "getModelDipole", 
function (modelIndex) {
if (modelIndex < 0) return null;
var dipole = this.getModelAuxiliaryInfoValue (modelIndex, "dipole");
if (dipole == null) dipole = this.getModelAuxiliaryInfoValue (modelIndex, "DIPOLE_VEC");
return dipole;
}, "~N");
Clazz.defineMethod (c$, "calculateMolecularDipole", 
function (modelIndex) {
if (this.partialCharges == null || modelIndex < 0) return null;
var nPos = 0;
var nNeg = 0;
var cPos = 0;
var cNeg = 0;
var pos =  new JU.V3 ();
var neg =  new JU.V3 ();
for (var i = 0; i < this.ac; i++) {
if (this.at[i].mi != modelIndex) continue;
var c = this.partialCharges[i];
if (c < 0) {
nNeg++;
cNeg += c;
neg.scaleAdd2 (c, this.at[i], neg);
} else if (c > 0) {
nPos++;
cPos += c;
pos.scaleAdd2 (c, this.at[i], pos);
}}
if (nNeg == 0 || nPos == 0) return null;
pos.scale (1 / cPos);
neg.scale (1 / cNeg);
pos.sub (neg);
JW.Logger.warn ("CalculateMolecularDipole: this is an approximate result -- needs checking");
pos.scale (cPos * 4.8);
return pos;
}, "~N");
Clazz.defineMethod (c$, "getMoleculeCountInModel", 
function (modelIndex) {
var n = 0;
if (this.moleculeCount == 0) this.getMolecules ();
if (modelIndex < 0) return this.moleculeCount;
for (var i = 0; i < this.mc; i++) {
if (modelIndex == i) n += this.am[i].moleculeCount;
}
return n;
}, "~N");
Clazz.defineMethod (c$, "calcSelectedMoleculesCount", 
function (bsSelected) {
if (this.moleculeCount == 0) this.getMolecules ();
this.selectedMolecules.xor (this.selectedMolecules);
var bsTemp =  new JU.BS ();
for (var i = 0; i < this.moleculeCount; i++) {
JW.BSUtil.copy2 (bsSelected, bsTemp);
bsTemp.and (this.molecules[i].atomList);
if (bsTemp.length () > 0) {
this.selectedMolecules.set (i);
}}
}, "JU.BS");
Clazz.defineMethod (c$, "setCentroid", 
function (bs, minmax) {
var bsDelete = this.getNotInCentroid (bs, minmax);
if (bsDelete != null && bsDelete.nextSetBit (0) >= 0) this.vwr.deleteAtoms (bsDelete, false);
}, "JU.BS,~A");
Clazz.defineMethod (c$, "getNotInCentroid", 
 function (bs, minmax) {
var iAtom0 = bs.nextSetBit (0);
if (iAtom0 < 0) return null;
var uc = this.getUnitCell (this.at[iAtom0].mi);
return (uc == null ? null : uc.notInCentroid (this, bs, minmax));
}, "JU.BS,~A");
Clazz.defineMethod (c$, "getMolecules", 
function () {
if (this.moleculeCount > 0) return this.molecules;
if (this.molecules == null) this.molecules =  new Array (4);
this.moleculeCount = 0;
var m = null;
var bsModelAtoms =  new Array (this.mc);
var biobranches = null;
for (var i = 0; i < this.mc; i++) {
bsModelAtoms[i] = this.vwr.getModelUndeletedAtomsBitSet (i);
m = this.am[i];
m.moleculeCount = 0;
biobranches = m.getBioBranches (biobranches);
}
this.molecules = JW.JmolMolecule.getMolecules (this.at, bsModelAtoms, biobranches, null);
this.moleculeCount = this.molecules.length;
for (var i = this.moleculeCount; --i >= 0; ) {
m = this.am[this.molecules[i].modelIndex];
m.firstMoleculeIndex = i;
m.moleculeCount++;
}
return this.molecules;
});
Clazz.defineMethod (c$, "initializeBspf", 
function () {
if (this.bspf != null && this.bspf.isInitialized ()) return;
if (this.showRebondTimes) JW.Logger.startTimer ("build bspf");
var bspf =  new J.bspt.Bspf (3);
if (JW.Logger.debugging) JW.Logger.debug ("sequential bspt order");
var bsNew = JU.BS.newN (this.mc);
for (var i = this.ac; --i >= 0; ) {
var atom = this.at[i];
if (!atom.isDeleted () && !this.isTrajectorySubFrame (atom.mi)) {
bspf.addTuple (this.am[atom.mi].trajectoryBaseIndex, atom);
bsNew.set (atom.mi);
}}
if (this.showRebondTimes) {
JW.Logger.checkTimer ("build bspf", false);
bspf.stats ();
}for (var i = bsNew.nextSetBit (0); i >= 0; i = bsNew.nextSetBit (i + 1)) bspf.validateModel (i, true);

bspf.validate (true);
this.bspf = bspf;
});
Clazz.defineMethod (c$, "initializeBspt", 
function (modelIndex) {
this.initializeBspf ();
if (this.bspf.isInitializedIndex (modelIndex)) return;
this.bspf.initialize (modelIndex, this.at, this.vwr.getModelUndeletedAtomsBitSet (modelIndex));
}, "~N");
Clazz.defineMethod (c$, "setIteratorForPoint", 
function (iterator, modelIndex, pt, distance) {
if (modelIndex < 0) {
iterator.setCenter (pt, distance);
return;
}this.initializeBspt (modelIndex);
iterator.setModel (this, modelIndex, this.am[modelIndex].firstAtomIndex, 2147483647, pt, distance, null);
}, "J.api.AtomIndexIterator,~N,JU.P3,~N");
Clazz.defineMethod (c$, "setIteratorForAtom", 
function (iterator, modelIndex, atomIndex, distance, rd) {
if (modelIndex < 0) modelIndex = this.at[atomIndex].mi;
modelIndex = this.am[modelIndex].trajectoryBaseIndex;
this.initializeBspt (modelIndex);
iterator.setModel (this, modelIndex, this.am[modelIndex].firstAtomIndex, atomIndex, this.at[atomIndex], distance, rd);
}, "J.api.AtomIndexIterator,~N,~N,~N,J.atomdata.RadiusData");
Clazz.defineMethod (c$, "getSelectedAtomIterator", 
function (bsSelected, isGreaterOnly, modelZeroBased, hemisphereOnly, isMultiModel) {
this.initializeBspf ();
var iter;
if (isMultiModel) {
var bsModels = this.getModelBitSet (bsSelected, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.initializeBspt (i);

iter =  new JM.AtomIteratorWithinModelSet (bsModels);
} else {
iter =  new JM.AtomIteratorWithinModel ();
}iter.initialize (this.bspf, bsSelected, isGreaterOnly, modelZeroBased, hemisphereOnly, this.vwr.isParallel ());
return iter;
}, "JU.BS,~B,~B,~B,~B");
Clazz.overrideMethod (c$, "getBondCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.bondCount : this.am[modelIndex].getBondCount ());
}, "~N");
Clazz.defineMethod (c$, "calculateStruts", 
function (bs1, bs2) {
return this.calculateStrutsMC (bs1, bs2);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "calculateStrutsMC", 
function (bs1, bs2) {
this.makeConnections2 (0, 3.4028235E38, 32768, 12291, bs1, bs2, null, false, false, 0);
var iAtom = bs1.nextSetBit (0);
if (iAtom < 0) return 0;
var model = this.am[this.at[iAtom].mi];
return (model.isBioModel ? model.calculateStruts (this, bs1, bs2) : 0);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "getAtomCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.ac : this.am[modelIndex].ac);
}, "~N");
Clazz.defineMethod (c$, "getModelAtomBitSetIncludingDeletedBs", 
function (bsModels) {
var bs =  new JU.BS ();
if (bsModels == null && this.bsAll == null) this.bsAll = JW.BSUtil.setAll (this.ac);
if (bsModels == null) bs.or (this.bsAll);
 else for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) bs.or (this.getModelAtomBitSetIncludingDeleted (i, false));

return bs;
}, "JU.BS");
Clazz.defineMethod (c$, "getModelAtomBitSetIncludingDeleted", 
function (modelIndex, asCopy) {
var bs = (modelIndex < 0 ? this.bsAll : this.am[modelIndex].bsAtoms);
if (bs == null) bs = this.bsAll = JW.BSUtil.setAll (this.ac);
return (asCopy ? JW.BSUtil.copy (bs) : bs);
}, "~N,~B");
Clazz.defineMethod (c$, "getAtomBitsMaybeDeleted", 
function (tokType, specInfo) {
var info;
var bs;
switch (tokType) {
default:
return this.getAtomBitsMDa (tokType, specInfo);
case 1678770178:
case 1048585:
return this.getAtomBitsMDb (tokType, specInfo);
case 1073741864:
return this.getBasePairBits (specInfo);
case 1679429641:
var boxInfo = this.getBoxInfo (specInfo, 1);
bs = this.getAtomsWithin (boxInfo.getBoundBoxCornerVector ().length () + 0.0001, boxInfo.getBoundBoxCenter (), null, -1);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) if (!boxInfo.isWithin (this.at[i])) bs.clear (i);

return bs;
case 1095761925:
bs =  new JU.BS ();
info = specInfo;
this.ptTemp1.set (info[0] / 1000, info[1] / 1000, info[2] / 1000);
var isAbsolute = !this.vwr.getBoolean (603979848);
for (var i = this.ac; --i >= 0; ) if (this.isInLatticeCell (i, this.ptTemp1, this.ptTemp2, isAbsolute)) bs.set (i);

return bs;
case 1095761926:
bs = JW.BSUtil.newBitSet2 (0, this.ac);
info = specInfo;
var minmax = [Clazz.doubleToInt (info[0] / 1000) - 1, Clazz.doubleToInt (info[1] / 1000) - 1, Clazz.doubleToInt (info[2] / 1000) - 1, Clazz.doubleToInt (info[0] / 1000), Clazz.doubleToInt (info[1] / 1000), Clazz.doubleToInt (info[2] / 1000), 0];
for (var i = this.mc; --i >= 0; ) {
var uc = this.getUnitCell (i);
if (uc == null) {
JW.BSUtil.andNot (bs, this.am[i].bsAtoms);
continue;
}bs.andNot (uc.notInCentroid (this, this.am[i].bsAtoms, minmax));
}
return bs;
case 1095761936:
return this.getMoleculeBitSet (specInfo);
case 1087373320:
return this.getSequenceBits (specInfo, null);
case 1048615:
info = specInfo;
var seqcodeA = info[0];
var seqcodeB = info[1];
var chainID = info[2];
bs =  new JU.BS ();
var caseSensitive = this.vwr.getBoolean (603979823);
if (chainID >= 0 && chainID < 256 && !caseSensitive) chainID = JM.AtomCollection.chainToUpper (chainID);
for (var i = this.mc; --i >= 0; ) if (this.am[i].isBioModel) this.am[i].selectSeqcodeRange (seqcodeA, seqcodeB, chainID, bs, caseSensitive);

return bs;
case 3145772:
bs = JU.BS.newN (this.ac);
var modelIndex = -1;
var nOps = 0;
for (var i = this.ac; --i >= 0; ) {
var atom = this.at[i];
var bsSym = atom.getAtomSymmetry ();
if (bsSym != null) {
if (atom.mi != modelIndex) {
modelIndex = atom.mi;
if (this.getModelCellRange (modelIndex) == null) continue;
nOps = this.getModelSymmetryCount (modelIndex);
}var n = 0;
for (var j = nOps; --j >= 0; ) if (bsSym.get (j)) if (++n > 1) {
bs.set (i);
break;
}
}}
return bs;
case 1089470478:
return JW.BSUtil.copy (this.bsSymmetry == null ? this.bsSymmetry = JU.BS.newN (this.ac) : this.bsSymmetry);
case 1614417948:
bs =  new JU.BS ();
var unitcell = this.vwr.getCurrentUnitCell ();
if (unitcell == null) return bs;
this.ptTemp1.set (1, 1, 1);
for (var i = this.ac; --i >= 0; ) if (this.isInLatticeCell (i, this.ptTemp1, this.ptTemp2, false)) bs.set (i);

return bs;
}
}, "~N,~O");
Clazz.defineMethod (c$, "isInLatticeCell", 
 function (i, cell, ptTemp, isAbsolute) {
var iModel = this.at[i].mi;
var uc = this.getUnitCell (iModel);
ptTemp.setT (this.at[i]);
return (uc != null && uc.checkUnitCell (uc, cell, ptTemp, isAbsolute));
}, "~N,JU.P3,JU.P3,~B");
Clazz.defineMethod (c$, "getAtomsWithinRD", 
function (distance, bs, withinAllModels, rd) {
var bsResult =  new JU.BS ();
var bsCheck = this.getIterativeModels (false);
bs = JW.BSUtil.andNot (bs, this.vwr.getDeletedAtoms ());
var iter = this.getSelectedAtomIterator (null, false, false, false, false);
if (withinAllModels) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) for (var iModel = this.mc; --iModel >= 0; ) {
if (!bsCheck.get (iModel)) continue;
if (distance < 0) {
this.getAtomsWithin (distance, this.at[i].getFractionalUnitCoordPt (true), bsResult, -1);
continue;
}this.setIteratorForAtom (iter, iModel, i, distance, rd);
iter.addAtoms (bsResult);
}

} else {
bsResult.or (bs);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (distance < 0) {
this.getAtomsWithin (distance, this.at[i], bsResult, this.at[i].mi);
continue;
}this.setIteratorForAtom (iter, -1, i, distance, rd);
iter.addAtoms (bsResult);
}
}iter.release ();
return bsResult;
}, "~N,JU.BS,~B,J.atomdata.RadiusData");
Clazz.defineMethod (c$, "getGroupsWithin", 
function (nResidues, bs) {
var bsCheck = this.getIterativeModels (false);
var bsResult =  new JU.BS ();
for (var iModel = this.mc; --iModel >= 0; ) {
if (!bsCheck.get (iModel) || !this.am[iModel].isBioModel) continue;
this.am[iModel].getGroupsWithin (nResidues, bs, bsResult);
}
return bsResult;
}, "~N,JU.BS");
Clazz.defineMethod (c$, "getAtomsWithin", 
function (distance, coord, bsResult, modelIndex) {
if (bsResult == null) bsResult =  new JU.BS ();
if (distance < 0) {
distance = -distance;
for (var i = this.ac; --i >= 0; ) {
var atom = this.at[i];
if (modelIndex >= 0 && this.at[i].mi != modelIndex) continue;
if (!bsResult.get (i) && atom.getFractionalUnitDistance (coord, this.ptTemp1, this.ptTemp2) <= distance) bsResult.set (atom.i);
}
return bsResult;
}var bsCheck = this.getIterativeModels (true);
var iter = this.getSelectedAtomIterator (null, false, false, false, false);
for (var iModel = this.mc; --iModel >= 0; ) {
if (!bsCheck.get (iModel)) continue;
this.setIteratorForAtom (iter, -1, this.am[iModel].firstAtomIndex, -1, null);
iter.setCenter (coord, distance);
iter.addAtoms (bsResult);
}
iter.release ();
return bsResult;
}, "~N,JU.P3,JU.BS,~N");
Clazz.defineMethod (c$, "getBasePairBits", 
 function (specInfo) {
var bs =  new JU.BS ();
if (specInfo.length % 2 != 0) return bs;
var bsA = null;
var bsB = null;
var vHBonds =  new JU.List ();
if (specInfo.length == 0) {
bsA = bsB = this.vwr.getAllAtoms ();
this.calcRasmolHydrogenBonds (bsA, bsB, vHBonds, true, 1, false, null);
} else {
for (var i = 0; i < specInfo.length; ) {
bsA = this.getSequenceBits (specInfo.substring (i, ++i), null);
if (bsA.cardinality () == 0) continue;
bsB = this.getSequenceBits (specInfo.substring (i, ++i), null);
if (bsB.cardinality () == 0) continue;
this.calcRasmolHydrogenBonds (bsA, bsB, vHBonds, true, 1, false, null);
}
}var bsAtoms =  new JU.BS ();
for (var i = vHBonds.size (); --i >= 0; ) {
var b = vHBonds.get (i);
bsAtoms.set (b.atom1.i);
bsAtoms.set (b.atom2.i);
}
return this.getAtomBitsMDb (1087373318, bsAtoms);
}, "~S");
Clazz.defineMethod (c$, "getSequenceBits", 
function (specInfo, bs) {
if (bs == null) bs = this.vwr.getAllAtoms ();
var bsResult =  new JU.BS ();
if (specInfo.length > 0) for (var i = 0; i < this.mc; ++i) if (this.am[i].isBioModel) this.am[i].getSequenceBits (specInfo, bs, bsResult);

return bsResult;
}, "~S,JU.BS");
Clazz.defineMethod (c$, "deleteBonds", 
function (bsBonds, isFullModel) {
if (!isFullModel) {
var bsA =  new JU.BS ();
var bsB =  new JU.BS ();
for (var i = bsBonds.nextSetBit (0); i >= 0; i = bsBonds.nextSetBit (i + 1)) {
var atom1 = this.bo[i].atom1;
if (this.am[atom1.mi].isModelKit) continue;
bsA.clearAll ();
bsB.clearAll ();
bsA.set (atom1.i);
bsB.set (this.bo[i].getAtomIndex2 ());
this.addStateScript ("connect ", null, bsA, bsB, "delete", false, true);
}
}this.dBb (bsBonds, isFullModel);
}, "JU.BS,~B");
Clazz.defineMethod (c$, "makeConnections2", 
function (minD, maxD, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
if (bsBonds == null) bsBonds =  new JU.BS ();
var matchAny = (order == 65535);
var matchNull = (order == 131071);
if (matchNull) order = 1;
var matchHbond = JM.Bond.isOrderH (order);
var identifyOnly = false;
var idOrModifyOnly = false;
var createOnly = false;
var autoAromatize = false;
switch (connectOperation) {
case 12291:
return this.deleteConnections (minD, maxD, order, bsA, bsB, isBonds, matchNull);
case 603979874:
case 1073741852:
if (order != 515) return this.autoBond (bsA, bsB, bsBonds, isBonds, matchHbond, connectOperation == 603979874);
idOrModifyOnly = autoAromatize = true;
break;
case 1087373321:
identifyOnly = idOrModifyOnly = true;
break;
case 1073742025:
idOrModifyOnly = true;
break;
case 1073741904:
createOnly = true;
break;
}
var anyOrNoId = (!identifyOnly || matchAny);
var notAnyAndNoId = (!identifyOnly && !matchAny);
this.defaultCovalentMad = this.vwr.getMadBond ();
var minDIsFrac = (minD < 0);
var maxDIsFrac = (maxD < 0);
var isFractional = (minDIsFrac || maxDIsFrac);
var checkDistance = (!isBonds || minD != 0.1 || maxD != 1.0E8);
if (checkDistance) {
minD = this.fixD (minD, minDIsFrac);
maxD = this.fixD (maxD, maxDIsFrac);
}var mad = this.getDefaultMadFromOrder (order);
var nNew = 0;
var nModified = 0;
var bondAB = null;
var atomA = null;
var atomB = null;
var altloc = '\u0000';
var newOrder = (order | 131072);
try {
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
if (isBonds) {
bondAB = this.bo[i];
atomA = bondAB.atom1;
atomB = bondAB.atom2;
} else {
atomA = this.at[i];
if (atomA.isDeleted ()) continue;
altloc = (this.isModulated (i) ? '\0' : atomA.altloc);
}for (var j = (isBonds ? 0 : bsB.nextSetBit (0)); j >= 0; j = bsB.nextSetBit (j + 1)) {
if (isBonds) {
j = 2147483646;
} else {
if (j == i) continue;
atomB = this.at[j];
if (atomA.mi != atomB.mi || atomB.isDeleted ()) continue;
if (altloc != '\0' && altloc != atomB.altloc && atomB.altloc != '\0') continue;
bondAB = atomA.getBond (atomB);
}if ((bondAB == null ? idOrModifyOnly : createOnly) || checkDistance && !this.isInRange (atomA, atomB, minD, maxD, minDIsFrac, maxDIsFrac, isFractional)) continue;
if (bondAB == null) {
bsBonds.set (this.bondAtoms (atomA, atomB, order, mad, bsBonds, energy, addGroup, true).index);
nNew++;
} else {
if (notAnyAndNoId) {
bondAB.setOrder (order);
this.bsAromatic.clear (bondAB.index);
}if (anyOrNoId || order == bondAB.order || newOrder == bondAB.order || matchHbond && bondAB.isHydrogen ()) {
bsBonds.set (bondAB.index);
nModified++;
}}}
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (autoAromatize) this.assignAromaticBondsBs (true, bsBonds);
if (!identifyOnly) this.sm.setShapeSizeBs (1, -2147483648, null, bsBonds);
return [nNew, nModified];
}, "~N,~N,~N,~N,JU.BS,JU.BS,JU.BS,~B,~B,~N");
Clazz.defineMethod (c$, "autoBondBs4", 
function (bsA, bsB, bsExclude, bsBonds, mad, preJmol11_9_24) {
if (preJmol11_9_24) return this.autoBond_Pre_11_9_24 (bsA, bsB, bsExclude, bsBonds, mad);
if (this.ac == 0) return 0;
if (mad == 0) mad = 1;
if (this.maxBondingRadius == 1.4E-45) this.findMaxRadii ();
var bondTolerance = this.vwr.getFloat (570425348);
var minBondDistance = this.vwr.getFloat (570425364);
var minBondDistance2 = minBondDistance * minBondDistance;
var nNew = 0;
if (this.showRebondTimes) JW.Logger.startTimer ("autobond");
var lastModelIndex = -1;
var isAll = (bsA == null);
var bsCheck;
var i0;
if (isAll) {
i0 = 0;
bsCheck = null;
} else {
if (bsA.equals (bsB)) {
bsCheck = bsA;
} else {
bsCheck = JW.BSUtil.copy (bsA);
bsCheck.or (bsB);
}i0 = bsCheck.nextSetBit (0);
}var iter = this.getSelectedAtomIterator (null, false, false, true, false);
for (var i = i0; i >= 0 && i < this.ac; i = (isAll ? i + 1 : bsCheck.nextSetBit (i + 1))) {
var isAtomInSetA = (isAll || bsA.get (i));
var isAtomInSetB = (isAll || bsB.get (i));
var atom = this.at[i];
if (atom.isDeleted ()) continue;
var modelIndex = atom.mi;
if (modelIndex != lastModelIndex) {
lastModelIndex = modelIndex;
if (this.isJmolDataFrameForModel (modelIndex)) {
i = this.am[modelIndex].firstAtomIndex + this.am[modelIndex].ac - 1;
continue;
}}var myBondingRadius = atom.getBondingRadius ();
if (myBondingRadius == 0) continue;
var isFirstExcluded = (bsExclude != null && bsExclude.get (i));
var searchRadius = myBondingRadius + this.maxBondingRadius + bondTolerance;
this.setIteratorForAtom (iter, -1, i, searchRadius, null);
while (iter.hasNext ()) {
var atomNear = this.at[iter.next ()];
if (atomNear.isDeleted ()) continue;
var atomIndexNear = atomNear.i;
var isNearInSetA = (isAll || bsA.get (atomIndexNear));
var isNearInSetB = (isAll || bsB.get (atomIndexNear));
if (!isNearInSetA && !isNearInSetB || !(isAtomInSetA && isNearInSetB || isAtomInSetB && isNearInSetA) || isFirstExcluded && bsExclude.get (atomIndexNear)) continue;
var order = JM.BondCollection.getBondOrderFull (myBondingRadius, atomNear.getBondingRadius (), iter.foundDistance2 (), minBondDistance2, bondTolerance);
if (order > 0 && this.checkValencesAndBond (atom, atomNear, order, mad, bsBonds)) nNew++;
}
iter.release ();
}
if (this.showRebondTimes) JW.Logger.checkTimer ("autoBond", false);
return nNew;
}, "JU.BS,JU.BS,JU.BS,JU.BS,~N,~B");
Clazz.defineMethod (c$, "autoBond_Pre_11_9_24", 
 function (bsA, bsB, bsExclude, bsBonds, mad) {
if (this.ac == 0) return 0;
if (mad == 0) mad = 1;
if (this.maxBondingRadius == 1.4E-45) this.findMaxRadii ();
var bondTolerance = this.vwr.getFloat (570425348);
var minBondDistance = this.vwr.getFloat (570425364);
var minBondDistance2 = minBondDistance * minBondDistance;
var nNew = 0;
this.initializeBspf ();
var lastModelIndex = -1;
for (var i = this.ac; --i >= 0; ) {
var isAtomInSetA = (bsA == null || bsA.get (i));
var isAtomInSetB = (bsB == null || bsB.get (i));
if (!isAtomInSetA && !isAtomInSetB) continue;
var atom = this.at[i];
if (atom.isDeleted ()) continue;
var modelIndex = atom.mi;
if (modelIndex != lastModelIndex) {
lastModelIndex = modelIndex;
if (this.isJmolDataFrameForModel (modelIndex)) {
for (; --i >= 0; ) if (this.at[i].mi != modelIndex) break;

i++;
continue;
}}var myBondingRadius = atom.getBondingRadius ();
if (myBondingRadius == 0) continue;
var searchRadius = myBondingRadius + this.maxBondingRadius + bondTolerance;
this.initializeBspt (modelIndex);
var iter = this.bspf.getCubeIterator (modelIndex);
iter.initialize (atom, searchRadius, true);
while (iter.hasMoreElements ()) {
var atomNear = iter.nextElement ();
if (atomNear === atom || atomNear.isDeleted ()) continue;
var atomIndexNear = atomNear.i;
var isNearInSetA = (bsA == null || bsA.get (atomIndexNear));
var isNearInSetB = (bsB == null || bsB.get (atomIndexNear));
if (!isNearInSetA && !isNearInSetB || bsExclude != null && bsExclude.get (atomIndexNear) && bsExclude.get (i)) continue;
if (!(isAtomInSetA && isNearInSetB || isAtomInSetB && isNearInSetA)) continue;
var order = JM.BondCollection.getBondOrderFull (myBondingRadius, atomNear.getBondingRadius (), iter.foundDistance2 (), minBondDistance2, bondTolerance);
if (order > 0) {
if (this.checkValencesAndBond (atom, atomNear, order, mad, bsBonds)) nNew++;
}}
iter.release ();
}
return nNew;
}, "JU.BS,JU.BS,JU.BS,JU.BS,~N");
Clazz.defineMethod (c$, "autoBond", 
 function (bsA, bsB, bsBonds, isBonds, matchHbond, legacyAutoBond) {
if (isBonds) {
var bs = bsA;
bsA =  new JU.BS ();
bsB =  new JU.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
bsA.set (this.bo[i].atom1.i);
bsB.set (this.bo[i].atom2.i);
}
}return [matchHbond ? this.autoHbond (bsA, bsB, false) : this.autoBondBs4 (bsA, bsB, null, bsBonds, this.vwr.getMadBond (), legacyAutoBond), 0];
}, "JU.BS,JU.BS,JU.BS,~B,~B,~B");
Clazz.defineMethod (c$, "autoHbond", 
function (bsA, bsB, onlyIfHaveCalculated) {
if (onlyIfHaveCalculated) {
var bsModels = this.getModelBitSet (bsA, false);
for (var i = bsModels.nextSetBit (0); i >= 0 && onlyIfHaveCalculated; i = bsModels.nextSetBit (i + 1)) onlyIfHaveCalculated = !this.am[i].hasRasmolHBonds;

if (onlyIfHaveCalculated) return 0;
}var haveHAtoms = false;
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) if (this.at[i].getElementNumber () == 1) {
haveHAtoms = true;
break;
}
var bsHBonds =  new JU.BS ();
var useRasMol = this.vwr.getBoolean (603979853);
if (bsB == null || useRasMol && !haveHAtoms) {
JW.Logger.info ((bsB == null ? "DSSP " : "RasMol") + " pseudo-hbond calculation");
this.calcRasmolHydrogenBonds (bsA, bsB, null, false, 2147483647, false, bsHBonds);
return -JW.BSUtil.cardinalityOf (bsHBonds);
}JW.Logger.info (haveHAtoms ? "Standard Hbond calculation" : "Jmol pseudo-hbond calculation");
var bsCO = null;
if (!haveHAtoms) {
bsCO =  new JU.BS ();
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
var atomID = this.at[i].atomID;
switch (atomID) {
case 64:
case 4:
case 14:
case 15:
case 16:
case 17:
bsCO.set (i);
break;
}
}
}var maxXYDistance = this.vwr.getFloat (570425361);
var minAttachedAngle = (this.vwr.getFloat (570425360) * 3.141592653589793 / 180);
var hbondMax2 = maxXYDistance * maxXYDistance;
var hbondMin2 = JM.ModelCollection.hbondMin * JM.ModelCollection.hbondMin;
var hxbondMin2 = 1;
var hxbondMax2 = (maxXYDistance > JM.ModelCollection.hbondMin ? hbondMin2 : hbondMax2);
var hxbondMax = (maxXYDistance > JM.ModelCollection.hbondMin ? JM.ModelCollection.hbondMin : maxXYDistance);
var nNew = 0;
var d2 = 0;
var v1 =  new JU.V3 ();
var v2 =  new JU.V3 ();
if (this.showRebondTimes && JW.Logger.debugging) JW.Logger.startTimer ("hbond");
var C = null;
var D = null;
var iter = this.getSelectedAtomIterator (bsB, false, false, false, false);
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
var atom = this.at[i];
var elementNumber = atom.getElementNumber ();
var isH = (elementNumber == 1);
if (!isH && (haveHAtoms || elementNumber != 7 && elementNumber != 8) || isH && !haveHAtoms) continue;
var min2;
var max2;
var dmax;
var firstIsCO;
if (isH) {
var b = atom.bonds;
if (b == null) continue;
var isOK = false;
for (var j = 0; j < b.length && !isOK; j++) {
var a2 = b[j].getOtherAtom (atom);
var element = a2.getElementNumber ();
isOK = (element == 7 || element == 8);
}
if (!isOK) continue;
dmax = hxbondMax;
min2 = hxbondMin2;
max2 = hxbondMax2;
firstIsCO = false;
} else {
dmax = maxXYDistance;
min2 = hbondMin2;
max2 = hbondMax2;
firstIsCO = bsCO.get (i);
}this.setIteratorForAtom (iter, -1, atom.i, dmax, null);
while (iter.hasNext ()) {
var atomNear = this.at[iter.next ()];
var elementNumberNear = atomNear.getElementNumber ();
if (atomNear === atom || !isH && elementNumberNear != 7 && elementNumberNear != 8 || isH && elementNumberNear == 1 || (d2 = iter.foundDistance2 ()) < min2 || d2 > max2 || firstIsCO && bsCO.get (atomNear.i) || atom.isBonded (atomNear)) {
continue;
}if (minAttachedAngle > 0) {
v1.sub2 (atom, atomNear);
if ((D = JM.ModelCollection.checkMinAttachedAngle (atom, minAttachedAngle, v1, v2, haveHAtoms)) == null) continue;
v1.scale (-1);
if ((C = JM.ModelCollection.checkMinAttachedAngle (atomNear, minAttachedAngle, v1, v2, haveHAtoms)) == null) continue;
}var energy = 0;
var bo;
if (isH && !Float.isNaN (C.x) && !Float.isNaN (D.x)) {
bo = 4096;
energy = JM.HBond.getEnergy (Math.sqrt (d2), C.distance (atom), C.distance (D), atomNear.distance (D)) / 1000;
} else {
bo = 2048;
}bsHBonds.set (this.addHBond (atom, atomNear, bo, energy));
nNew++;
}
}
iter.release ();
this.sm.setShapeSizeBs (1, -2147483648, null, bsHBonds);
if (this.showRebondTimes) JW.Logger.checkTimer ("hbond", false);
return (haveHAtoms ? nNew : -nNew);
}, "JU.BS,JU.BS,~B");
c$.checkMinAttachedAngle = Clazz.defineMethod (c$, "checkMinAttachedAngle", 
 function (atom1, minAngle, v1, v2, haveHAtoms) {
var bonds = atom1.bonds;
if (bonds == null || bonds.length == 0) return JU.P3.new3 (NaN, 0, 0);
var X = null;
var dMin = 3.4028235E38;
for (var i = bonds.length; --i >= 0; ) if (bonds[i].isCovalent ()) {
var atomA = bonds[i].getOtherAtom (atom1);
if (!haveHAtoms && atomA.getElementNumber () == 1) continue;
v2.sub2 (atom1, atomA);
var d = v2.angle (v1);
if (d < minAngle) return null;
if (d < dMin) {
X = atomA;
dMin = d;
}}
return X;
}, "JM.Atom,~N,JU.V3,JU.V3,~B");
Clazz.defineMethod (c$, "setStructureIndexes", 
function () {
var id;
var idnew = 0;
var lastid = -1;
var imodel = -1;
var lastmodel = -1;
for (var i = 0; i < this.ac; i++) {
if ((imodel = this.at[i].mi) != lastmodel) {
idnew = 0;
lastmodel = imodel;
lastid = -1;
}if ((id = this.at[i].getStrucNo ()) != lastid && id != 0) {
this.at[i].getGroup ().setStrucNo (++idnew);
lastid = idnew;
}}
});
Clazz.defineMethod (c$, "getProteinStructureState", 
function (bsAtoms, taintedOnly, needPhiPsi, mode) {
if (!this.isPDB) return "";
for (var i = 0; i < this.mc; i++) if (this.am[i].isBioModel) return this.am[i].getProteinStructureState (bsAtoms, taintedOnly, needPhiPsi, mode);

return "";
}, "JU.BS,~B,~B,~N");
Clazz.defineMethod (c$, "getModelInfoAsString", 
function () {
var sb =  new JU.SB ().append ("<models count=\"");
sb.appendI (this.mc).append ("\" modelSetHasVibrationVectors=\"").append (this.modelSetHasVibrationVectors () + "\">\n<properties>");
if (this.modelSetProperties != null) {
var e = this.modelSetProperties.propertyNames ();
while (e.hasMoreElements ()) {
var propertyName = e.nextElement ();
sb.append ("\n <property name=\"").append (propertyName).append ("\" value=").append (JU.PT.esc (this.modelSetProperties.getProperty (propertyName))).append (" />");
}
sb.append ("\n</properties>");
}for (var i = 0; i < this.mc; ++i) {
sb.append ("\n<model index=\"").appendI (i).append ("\" n=\"").append (this.getModelNumberDotted (i)).append ("\" id=").append (JU.PT.esc ("" + this.getModelAuxiliaryInfoValue (i, "modelID")));
var ib = this.vwr.getJDXBaseModelIndex (i);
if (ib != i) sb.append (" baseModelId=").append (JU.PT.esc (this.getModelAuxiliaryInfoValue (ib, "jdxModelID")));
sb.append (" name=").append (JU.PT.esc (this.getModelName (i))).append (" title=").append (JU.PT.esc (this.getModelTitle (i))).append (" hasVibrationVectors=\"").appendB (this.vwr.modelHasVibrationVectors (i)).append ("\" />");
}
sb.append ("\n</models>");
return sb.toString ();
});
Clazz.defineMethod (c$, "getSymmetryInfoAsString", 
function () {
var sb =  new JU.SB ().append ("Symmetry Information:");
for (var i = 0; i < this.mc; ++i) sb.append ("\nmodel #").append (this.getModelNumberDotted (i)).append ("; name=").append (this.getModelName (i)).append ("\n").append (this.getSymmetryInfoAsStringForModel (i));

return sb.toString ();
});
Clazz.defineMethod (c$, "getAtomsConnected", 
function (min, max, intType, bs) {
var isBonds = Clazz.instanceOf (bs, JM.BondSet);
var bsResult = (isBonds ?  new JM.BondSet () :  new JU.BS ());
var nBonded =  Clazz.newIntArray (this.ac, 0);
var i;
var ishbond = (intType == 30720);
var isall = (intType == 65535);
for (var ibond = 0; ibond < this.bondCount; ibond++) {
var bond = this.bo[ibond];
if (isall || bond.is (intType) || ishbond && bond.isHydrogen ()) {
if (isBonds) {
bsResult.set (ibond);
} else {
if (bs.get (bond.atom1.i)) {
nBonded[i = bond.atom2.i]++;
bsResult.set (i);
}if (bs.get (bond.atom2.i)) {
nBonded[i = bond.atom1.i]++;
bsResult.set (i);
}}}}
if (isBonds) return bsResult;
var nonbonded = (min == 0);
for (i = this.ac; --i >= 0; ) {
var n = nBonded[i];
if (n < min || n > max) bsResult.clear (i);
 else if (nonbonded && n == 0) bsResult.set (i);
}
return bsResult;
}, "~N,~N,~N,JU.BS");
Clazz.defineMethod (c$, "getSymTemp", 
function (forceNew) {
return (this.symTemp == null || forceNew ? (this.symTemp = J.api.Interface.getSymmetry ()) : this.symTemp);
}, "~B");
Clazz.defineMethod (c$, "createModels", 
function (n) {
var newModelCount = this.mc + n;
var newModels = JU.AU.arrayCopyObject (this.am, newModelCount);
this.validateBspf (false);
this.modelNumbers = JU.AU.arrayCopyI (this.modelNumbers, newModelCount);
this.modelFileNumbers = JU.AU.arrayCopyI (this.modelFileNumbers, newModelCount);
this.modelNumbersForAtomLabel = JU.AU.arrayCopyS (this.modelNumbersForAtomLabel, newModelCount);
this.modelNames = JU.AU.arrayCopyS (this.modelNames, newModelCount);
this.frameTitles = JU.AU.arrayCopyS (this.frameTitles, newModelCount);
var f = Clazz.doubleToInt (this.getModelFileNumber (this.mc - 1) / 1000000) + 1;
for (var i = this.mc, pt = 0; i < newModelCount; i++) {
this.modelNumbers[i] = i + this.mc;
this.modelFileNumbers[i] = f * 1000000 + (++pt);
this.modelNumbersForAtomLabel[i] = this.modelNames[i] = f + "." + pt;
}
this.thisStateModel = -1;
var group3Lists = this.getModelSetAuxiliaryInfoValue ("group3Lists");
if (group3Lists != null) {
var group3Counts = this.getModelSetAuxiliaryInfoValue ("group3Counts");
group3Lists = JU.AU.arrayCopyS (group3Lists, newModelCount);
group3Counts = JU.AU.arrayCopyII (group3Counts, newModelCount);
this.modelSetAuxiliaryInfo.put ("group3Lists", group3Lists);
this.modelSetAuxiliaryInfo.put ("group3Counts", group3Counts);
}this.unitCells = JU.AU.arrayCopyObject (this.unitCells, newModelCount);
for (var i = this.mc; i < newModelCount; i++) {
newModels[i] =  new JM.Model (this, i, -1, null, null, null);
newModels[i].loadState = " model create #" + i + ";";
}
this.am = newModels;
this.mc = newModelCount;
}, "~N");
Clazz.defineMethod (c$, "deleteModel", 
function (modelIndex, firstAtomIndex, nAtoms, bsAtoms, bsBonds) {
if (modelIndex < 0) {
this.validateBspf (false);
this.bsAll = null;
this.resetMolecules ();
this.isBbcageDefault = false;
this.calcBoundBoxDimensions (null, 1);
return;
}this.modelNumbers = JU.AU.deleteElements (this.modelNumbers, modelIndex, 1);
this.modelFileNumbers = JU.AU.deleteElements (this.modelFileNumbers, modelIndex, 1);
this.modelNumbersForAtomLabel = JU.AU.deleteElements (this.modelNumbersForAtomLabel, modelIndex, 1);
this.modelNames = JU.AU.deleteElements (this.modelNames, modelIndex, 1);
this.frameTitles = JU.AU.deleteElements (this.frameTitles, modelIndex, 1);
this.thisStateModel = -1;
var group3Lists = this.getModelSetAuxiliaryInfoValue ("group3Lists");
var group3Counts = this.getModelSetAuxiliaryInfoValue ("group3Counts");
var ptm = modelIndex + 1;
if (group3Lists != null && group3Lists[ptm] != null) {
for (var i = Clazz.doubleToInt (group3Lists[ptm].length / 6); --i >= 0; ) if (group3Counts[ptm][i] > 0) {
group3Counts[0][i] -= group3Counts[ptm][i];
if (group3Counts[0][i] == 0) group3Lists[0] = group3Lists[0].substring (0, i * 6) + ",[" + group3Lists[0].substring (i * 6 + 2);
}
}if (group3Lists != null) {
this.modelSetAuxiliaryInfo.put ("group3Lists", JU.AU.deleteElements (group3Lists, modelIndex, 1));
this.modelSetAuxiliaryInfo.put ("group3Counts", JU.AU.deleteElements (group3Counts, modelIndex, 1));
}if (this.unitCells != null) {
this.unitCells = JU.AU.deleteElements (this.unitCells, modelIndex, 1);
}for (var i = this.stateScripts.size (); --i >= 0; ) {
if (!this.stateScripts.get (i).deleteAtoms (modelIndex, bsBonds, bsAtoms)) {
this.stateScripts.remove (i);
}}
this.deleteModelAtoms (firstAtomIndex, nAtoms, bsAtoms);
this.vwr.deleteModelAtoms (modelIndex, firstAtomIndex, nAtoms, bsAtoms);
}, "~N,~N,~N,JU.BS,JU.BS");
Clazz.defineMethod (c$, "getMoInfo", 
function (modelIndex) {
var sb =  new JU.SB ();
for (var m = 0; m < this.mc; m++) {
if (modelIndex >= 0 && m != modelIndex) {
continue;
}var moData = this.vwr.getModelAuxiliaryInfoValue (m, "moData");
if (moData == null) {
continue;
}var mos = (moData.get ("mos"));
var nOrb = (mos == null ? 0 : mos.size ());
if (nOrb == 0) {
continue;
}for (var i = nOrb; --i >= 0; ) {
var mo = mos.get (i);
var type = mo.get ("type");
if (type == null) {
type = "";
}var units = mo.get ("energyUnits");
if (units == null) {
units = "";
}var occ = mo.get ("occupancy");
if (occ != null) {
type = "occupancy " + occ.floatValue () + " " + type;
}var sym = mo.get ("symmetry");
if (sym != null) {
type += sym;
}var energy = "" + mo.get ("energy");
if (Float.isNaN (JU.PT.parseFloat (energy))) sb.append (JW.Txt.sprintf ("model %-2s;  mo %-2i # %s\n", "sis", [this.getModelNumberDotted (m), Integer.$valueOf (i + 1), type]));
 else sb.append (JW.Txt.sprintf ("model %-2s;  mo %-2i # energy %-8.3f %s %s\n", "sifss", [this.getModelNumberDotted (m), Integer.$valueOf (i + 1), mo.get ("energy"), units, type]));
}
}
return sb.toString ();
}, "~N");
Clazz.defineMethod (c$, "assignAtom", 
function (atomIndex, type, autoBond) {
if (type == null) type = "C";
var atom = this.at[atomIndex];
var bs =  new JU.BS ();
var wasH = (atom.getElementNumber () == 1);
var atomicNumber = JW.Elements.elementNumberFromSymbol (type, true);
var isDelete = false;
if (atomicNumber > 0) {
this.setElement (atom, atomicNumber);
this.vwr.setShapeSizeRD (0, this.vwr.getDefaultRadiusData (), JW.BSUtil.newAndSetBit (atomIndex));
this.setAtomName (atomIndex, type + atom.getAtomNumber ());
if (!this.am[atom.mi].isModelKit) this.taintAtom (atomIndex, 0);
} else if (type.equals ("Pl")) {
atom.setFormalCharge (atom.getFormalCharge () + 1);
} else if (type.equals ("Mi")) {
atom.setFormalCharge (atom.getFormalCharge () - 1);
} else if (type.equals ("X")) {
isDelete = true;
} else if (!type.equals (".")) {
return;
}this.removeUnnecessaryBonds (atom, isDelete);
var dx = 0;
if (atom.getCovalentBondCount () == 1) if (wasH) {
dx = 1.50;
} else if (!wasH && atomicNumber == 1) {
dx = 1.0;
}if (dx != 0) {
var v = JU.V3.newVsub (atom, this.at[atom.getBondedAtomIndex (0)]);
var d = v.length ();
v.normalize ();
v.scale (dx - d);
this.setAtomCoordRelative (atomIndex, v.x, v.y, v.z);
}var bsA = JW.BSUtil.newAndSetBit (atomIndex);
if (atomicNumber != 1 && autoBond) {
this.validateBspf (false);
bs = this.getAtomsWithinRD (1.0, bsA, false, null);
bs.andNot (bsA);
if (bs.nextSetBit (0) >= 0) this.vwr.deleteAtoms (bs, false);
bs = this.vwr.getModelUndeletedAtomsBitSet (atom.mi);
bs.andNot (this.getAtomBitsMDa (1613758476, null));
this.makeConnections2 (0.1, 1.8, 1, 1073741904, bsA, bs, null, false, false, 0);
}this.vwr.addHydrogens (bsA, false, true);
}, "~N,~S,~B");
Clazz.defineMethod (c$, "deleteAtoms", 
function (bs) {
this.averageAtomPoint = null;
if (bs == null) return;
var bsBonds =  new JU.BS ();
for (var i = bs.nextSetBit (0); i >= 0 && i < this.ac; i = bs.nextSetBit (i + 1)) this.at[i].deleteBonds (bsBonds);

for (var i = 0; i < this.mc; i++) {
this.am[i].bsAtomsDeleted.or (bs);
this.am[i].bsAtomsDeleted.and (this.am[i].bsAtoms);
}
this.deleteBonds (bsBonds, false);
}, "JU.BS");
Clazz.defineMethod (c$, "getModelCml", 
function (bs, atomsMax, addBonds) {
var sb =  new JU.SB ();
var nAtoms = JW.BSUtil.cardinalityOf (bs);
if (nAtoms == 0) return "";
J.api.Interface.getInterface ("JU.XmlUtil");
JU.XmlUtil.openTag (sb, "molecule");
JU.XmlUtil.openTag (sb, "atomArray");
var bsAtoms =  new JU.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (--atomsMax < 0) break;
var atom = this.at[i];
var name = atom.getAtomName ();
JU.PT.rep (name, "\"", "''");
bsAtoms.set (atom.i);
JU.XmlUtil.appendTag (sb, "atom/", ["id", "a" + (atom.i + 1), "title", atom.getAtomName (), "elementType", atom.getElementSymbol (), "x3", "" + atom.x, "y3", "" + atom.y, "z3", "" + atom.z]);
}
JU.XmlUtil.closeTag (sb, "atomArray");
if (addBonds) {
JU.XmlUtil.openTag (sb, "bondArray");
for (var i = 0; i < this.bondCount; i++) {
var bond = this.bo[i];
var a1 = bond.atom1;
var a2 = bond.atom2;
if (!bsAtoms.get (a1.i) || !bsAtoms.get (a2.i)) continue;
var order = JW.Edge.getCmlBondOrder (bond.order);
if (order == null) continue;
JU.XmlUtil.appendTag (sb, "bond/", ["atomRefs2", "a" + (bond.atom1.i + 1) + " a" + (bond.atom2.i + 1), "order", order]);
}
JU.XmlUtil.closeTag (sb, "bondArray");
}JU.XmlUtil.closeTag (sb, "molecule");
return sb.toString ();
}, "JU.BS,~N,~B");
Clazz.defineMethod (c$, "adjustAtomArrays", 
function (map, i0, ac) {
this.ac = ac;
for (var i = i0; i < ac; i++) {
this.at[i] = this.at[map[i]];
this.at[i].i = i;
var m = this.am[this.at[i].mi];
if (m.firstAtomIndex == map[i]) m.firstAtomIndex = i;
m.bsAtoms.set (i);
}
if (this.vibrations != null) for (var i = i0; i < ac; i++) this.vibrations[i] = this.vibrations[map[i]];

if (this.occupancies != null) for (var i = i0; i < ac; i++) this.occupancies[i] = this.occupancies[map[i]];

if (this.bfactor100s != null) for (var i = i0; i < ac; i++) this.bfactor100s[i] = this.bfactor100s[map[i]];

if (this.partialCharges != null) for (var i = i0; i < ac; i++) this.partialCharges[i] = this.partialCharges[map[i]];

if (this.atomTensorList != null) {
for (var i = i0; i < ac; i++) {
var list = this.atomTensorList[i] = this.atomTensorList[map[i]];
for (var j = list.length; --j >= 0; ) {
var t = list[j];
if (t != null) t.atomIndex1 = map[t.atomIndex1];
}
}
}if (this.atomNames != null) for (var i = i0; i < ac; i++) this.atomNames[i] = this.atomNames[map[i]];

if (this.atomTypes != null) for (var i = i0; i < ac; i++) this.atomTypes[i] = this.atomTypes[map[i]];

if (this.atomSerials != null) for (var i = i0; i < ac; i++) this.atomSerials[i] = this.atomSerials[map[i]];

}, "~A,~N,~N");
Clazz.defineMethod (c$, "growAtomArrays", 
function (newLength) {
this.at = JU.AU.arrayCopyObject (this.at, newLength);
if (this.vibrations != null) this.vibrations = JU.AU.arrayCopyObject (this.vibrations, newLength);
if (this.occupancies != null) this.occupancies = JU.AU.arrayCopyByte (this.occupancies, newLength);
if (this.bfactor100s != null) this.bfactor100s = JU.AU.arrayCopyShort (this.bfactor100s, newLength);
if (this.partialCharges != null) this.partialCharges = JU.AU.arrayCopyF (this.partialCharges, newLength);
if (this.atomTensorList != null) this.atomTensorList = JU.AU.arrayCopyObject (this.atomTensorList, newLength);
if (this.atomNames != null) this.atomNames = JU.AU.arrayCopyS (this.atomNames, newLength);
if (this.atomTypes != null) this.atomTypes = JU.AU.arrayCopyS (this.atomTypes, newLength);
if (this.atomSerials != null) this.atomSerials = JU.AU.arrayCopyI (this.atomSerials, newLength);
}, "~N");
Clazz.defineMethod (c$, "addAtom", 
function (modelIndex, group, atomicAndIsotopeNumber, atomName, atomSerial, atomSite, xyz, radius, vib, formalCharge, partialCharge, occupancy, bfactor, tensors, isHetero, specialAtomID, atomSymmetry) {
var atom =  new JM.Atom ().setAtom (modelIndex, this.ac, xyz, radius, atomSymmetry, atomSite, atomicAndIsotopeNumber, formalCharge, isHetero);
this.am[modelIndex].ac++;
this.am[modelIndex].bsAtoms.set (this.ac);
if (JW.Elements.isElement (atomicAndIsotopeNumber, 1)) this.am[modelIndex].hydrogenCount++;
if (this.ac >= this.at.length) this.growAtomArrays (this.ac + 100);
this.at[this.ac] = atom;
this.setBFactor (this.ac, bfactor);
this.setOccupancy (this.ac, occupancy);
this.setPartialCharge (this.ac, partialCharge);
if (tensors != null) this.setAtomTensors (this.ac, tensors);
atom.group = group;
atom.colixAtom = this.vwr.getColixAtomPalette (atom, J.c.PAL.CPK.id);
if (atomName != null) {
var i;
if ((i = atomName.indexOf ('\0')) >= 0) {
if (this.atomTypes == null) this.atomTypes =  new Array (this.at.length);
this.atomTypes[this.ac] = atomName.substring (i + 1);
atomName = atomName.substring (0, i);
}atom.atomID = specialAtomID;
if (specialAtomID == 0) {
if (this.atomNames == null) this.atomNames =  new Array (this.at.length);
this.atomNames[this.ac] = atomName.intern ();
}}if (atomSerial != -2147483648) {
if (this.atomSerials == null) this.atomSerials =  Clazz.newIntArray (this.at.length, 0);
this.atomSerials[this.ac] = atomSerial;
}if (vib != null) this.setVibrationVector (this.ac, vib);
this.ac++;
return atom;
}, "~N,JM.Group,~N,~S,~N,~N,JU.P3,~N,JU.V3,~N,~N,~N,~N,JU.List,~B,~N,JU.BS");
Clazz.defineMethod (c$, "getInlineData", 
function (modelIndex) {
var data = null;
if (modelIndex >= 0) data = this.am[modelIndex].loadScript;
 else for (modelIndex = this.mc; --modelIndex >= 0; ) if ((data = this.am[modelIndex].loadScript).length () > 0) break;

var pt = data.lastIndexOf ("data \"");
if (pt < 0) return null;
pt = data.indexOf2 ("\"", pt + 7);
var pt2 = data.lastIndexOf ("end \"");
if (pt2 < pt || pt < 0) return null;
return data.substring2 (pt + 2, pt2);
}, "~N");
Clazz.defineMethod (c$, "isAtomPDB", 
function (i) {
return i >= 0 && this.am[this.at[i].mi].isBioModel;
}, "~N");
Clazz.defineMethod (c$, "isAtomAssignable", 
function (i) {
return i >= 0 && this.at[i].mi == this.mc - 1;
}, "~N");
Clazz.defineMethod (c$, "haveModelKit", 
function () {
for (var i = 0; i < this.mc; i++) if (this.am[i].isModelKit) return true;

return false;
});
Clazz.defineMethod (c$, "getModelKitStateBitset", 
function (bs, bsDeleted) {
var bs1 = JW.BSUtil.copy (bsDeleted);
for (var i = 0; i < this.mc; i++) if (!this.am[i].isModelKit) bs1.andNot (this.am[i].bsAtoms);

return JW.BSUtil.deleteBits (bs, bs1);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "setAtomNamesAndNumbers", 
function (iFirst, baseAtomIndex, mergeSet) {
if (baseAtomIndex < 0) iFirst = this.am[this.at[iFirst].mi].firstAtomIndex;
if (this.atomSerials == null) this.atomSerials =  Clazz.newIntArray (this.ac, 0);
if (this.atomNames == null) this.atomNames =  new Array (this.ac);
var isZeroBased = this.isXYZ && this.vwr.getBoolean (603979978);
var lastModelIndex = 2147483647;
var atomNo = 1;
for (var i = iFirst; i < this.ac; ++i) {
var atom = this.at[i];
if (atom.mi != lastModelIndex) {
lastModelIndex = atom.mi;
atomNo = (isZeroBased ? 0 : 1);
}if (i >= -baseAtomIndex) {
if (this.atomSerials[i] == 0 || baseAtomIndex < 0) this.atomSerials[i] = (i < baseAtomIndex ? mergeSet.atomSerials[i] : atomNo);
if (this.atomNames[i] == null || baseAtomIndex < 0) this.atomNames[i] = (atom.getElementSymbol () + this.atomSerials[i]).intern ();
}if (!this.am[lastModelIndex].isModelKit || atom.getElementNumber () > 0 && !atom.isDeleted ()) atomNo++;
}
}, "~N,~N,JM.AtomCollection");
Clazz.defineMethod (c$, "setUnitCellOffset", 
function (unitCell, pt, ijk) {
if (unitCell == null) return;
if (pt == null) unitCell.setOffset (ijk);
 else unitCell.setOffsetPt (pt);
}, "J.api.SymmetryInterface,JU.P3,~N");
Clazz.defineMethod (c$, "connect", 
function (connections) {
this.resetMolecules ();
var bsDelete =  new JU.BS ();
for (var i = 0; i < connections.length; i++) {
var f = connections[i];
if (f == null || f.length < 2) continue;
var index1 = Clazz.floatToInt (f[0]);
var addGroup = (index1 < 0);
if (addGroup) index1 = -1 - index1;
var index2 = Clazz.floatToInt (f[1]);
if (index2 < 0 || index1 >= this.ac || index2 >= this.ac) continue;
var order = (f.length > 2 ? Clazz.floatToInt (f[2]) : 1);
if (order < 0) order &= 0xFFFF;
var mad = (f.length > 3 ? Clazz.floatToShort (1000 * connections[i][3]) : this.getDefaultMadFromOrder (order));
if (order == 0 || mad == 0 && order != 32768 && !JM.Bond.isOrderH (order)) {
var b = this.at[index1].getBond (this.at[index2]);
if (b != null) bsDelete.set (b.index);
continue;
}var energy = (f.length > 4 ? f[4] : 0);
this.bondAtoms (this.at[index1], this.at[index2], order, mad, null, energy, addGroup, true);
}
if (bsDelete.nextSetBit (0) >= 0) this.deleteBonds (bsDelete, false);
}, "~A");
Clazz.defineMethod (c$, "allowSpecAtom", 
function () {
return this.mc != 1 || this.am[0].isBioModel;
});
Clazz.defineMethod (c$, "setFrameDelayMs", 
function (millis, bsModels) {
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.am[this.am[i].trajectoryBaseIndex].frameDelay = millis;

}, "~N,JU.BS");
Clazz.defineMethod (c$, "getFrameDelayMs", 
function (i) {
return (i < this.am.length && i >= 0 ? this.am[this.am[i].trajectoryBaseIndex].frameDelay : 0);
}, "~N");
Clazz.defineMethod (c$, "getModelIndexFromId", 
function (id) {
var haveFile = (id.indexOf ("#") >= 0);
var isBaseModel = id.toLowerCase ().endsWith (".basemodel");
if (isBaseModel) id = id.substring (0, id.length - 10);
var errCode = -1;
var fname = null;
for (var i = 0; i < this.mc; i++) {
var mid = this.getModelAuxiliaryInfoValue (i, "modelID");
var mnum = (id.startsWith ("~") ? "~" + this.getModelNumberDotted (i) : null);
if (mnum == null && mid == null && (mid = this.getModelTitle (i)) == null) continue;
if (haveFile) {
fname = this.getModelFileName (i);
if (fname.endsWith ("#molfile")) {
mid = fname;
} else {
fname += "#";
mid = fname + mid;
}}if (id.equalsIgnoreCase (mid) || id.equalsIgnoreCase (mnum)) return (isBaseModel ? this.vwr.getJDXBaseModelIndex (i) : i);
if (fname != null && id.startsWith (fname)) errCode = -2;
}
return (fname == null && !haveFile ? -2 : errCode);
}, "~S");
Clazz.defineMethod (c$, "getAuxiliaryInfo", 
function (bsModels) {
var info = this.modelSetAuxiliaryInfo;
if (info == null) return null;
var models =  new JU.List ();
for (var i = 0; i < this.mc; ++i) {
if (bsModels != null && !bsModels.get (i)) {
continue;
}var modelinfo = this.getModelAuxiliaryInfo (i);
models.addLast (modelinfo);
}
info.put ("models", models);
return info;
}, "JU.BS");
Clazz.defineMethod (c$, "getDihedralMap", 
function (alist) {
var list =  new JU.List ();
var n = alist.length;
var ai = null;
var aj = null;
var ak = null;
var al = null;
for (var i = n - 1; --i >= 0; ) for (var j = n; --j > i; ) {
ai = this.at[alist[i]];
aj = this.at[alist[j]];
if (ai.isBonded (aj)) {
for (var k = n; --k >= 0; ) if (k != i && k != j && (ak = this.at[alist[k]]).isBonded (ai)) for (var l = n; --l >= 0; ) if (l != i && l != j && l != k && (al = this.at[alist[l]]).isBonded (aj)) {
var a =  Clazz.newIntArray (4, 0);
a[0] = ak.i;
a[1] = ai.i;
a[2] = aj.i;
a[3] = al.i;
list.addLast (a);
}

}}

n = list.size ();
var ilist = JU.AU.newInt2 (n);
for (var i = n; --i >= 0; ) ilist[n - i - 1] = list.get (i);

return ilist;
}, "~A");
Clazz.defineMethod (c$, "setModulation", 
function (bs, isOn, qtOffset, isQ) {
if (this.bsModulated == null) {
if (isOn) this.bsModulated =  new JU.BS ();
 else if (bs == null) return;
}if (bs == null) bs = this.getModelAtomBitSetIncludingDeleted (-1, false);
var scale = this.vwr.getFloat (1276121113);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var v = this.getVibration (i, false);
if (!(Clazz.instanceOf (v, J.api.JmolModulationSet))) continue;
(v).setModTQ (this.at[i], isOn, qtOffset, isQ, scale);
if (this.bsModulated != null) this.bsModulated.setBitTo (i, isOn);
}
}, "JU.BS,~B,JU.P3,~B");
Clazz.defineMethod (c$, "getDynamicAtom", 
function (i, pt) {
var v = this.getVibration (i, false);
if (v == null) return this.at[i];
if (pt == null) pt =  new JW.Point3fi ();
pt.setT (this.at[i]);
pt = this.vwr.getVibrationPoint (v, pt);
pt.sD = -1;
return pt;
}, "~N,JW.Point3fi");
Clazz.defineMethod (c$, "getBoundBoxOrientation", 
function (type, bsAtoms) {
var j0 = bsAtoms.nextSetBit (0);
if (j0 < 0) return "{0 0 0 1}";
var n = (this.vOrientations == null ? 0 : this.vOrientations.length);
if (n == 0) {
var av =  new Array (3375);
n = 0;
var p4 =  new JU.P4 ();
for (var i = -7; i <= 7; i++) for (var j = -7; j <= 7; j++) for (var k = 0; k <= 14; k++, n++) if ((av[n] = JU.V3.new3 (i / 7, j / 7, k / 14)).length () > 1) --n;



this.vOrientations =  new Array (n);
for (var i = n; --i >= 0; ) {
var cos = Math.sqrt (1 - av[i].lengthSquared ());
if (Float.isNaN (cos)) cos = 0;
p4.set (av[i].x, av[i].y, av[i].z, cos);
this.vOrientations[i] = JU.Quat.newP4 (p4);
}
}var pt =  new JU.P3 ();
var vMin = 3.4028235E38;
var q;
var qBest = null;
var bBest = null;
var v;
for (var i = 0; i < n; i++) {
q = this.vOrientations[i];
var b =  new JW.BoxInfo ();
b.setMargin (0);
for (var j = j0; j >= 0; j = bsAtoms.nextSetBit (j + 1)) b.addBoundBoxPoint (q.transformP2 (this.at[j], pt));

switch (type) {
default:
case 1313866249:
case 1073741863:
v = (b.bbCorner1.x - b.bbCorner0.x) * (b.bbCorner1.y - b.bbCorner0.y) * (b.bbCorner1.z - b.bbCorner0.z);
break;
case 1112541205:
v = b.bbCorner1.x - b.bbCorner0.x;
break;
case 1112541206:
v = b.bbCorner1.y - b.bbCorner0.y;
break;
case 1112541207:
v = b.bbCorner1.z - b.bbCorner0.z;
break;
}
if (v < vMin) {
qBest = q;
bBest = b;
vMin = v;
}}
if (type != 1313866249 && type != 1073741863) return qBest.toString ();
q = JU.Quat.newQ (qBest);
var dx = bBest.bbCorner1.x - bBest.bbCorner0.x;
var dy = bBest.bbCorner1.y - bBest.bbCorner0.y;
var dz = bBest.bbCorner1.z - bBest.bbCorner0.z;
if (dx < dy) {
pt.set (0, 0, 1);
q = JU.Quat.newVA (pt, 90).mulQ (q);
var f = dx;
dx = dy;
dy = f;
}if (dy < dz) {
if (dz > dx) {
pt.set (0, 1, 0);
q = JU.Quat.newVA (pt, 90).mulQ (q);
var f = dx;
dx = dz;
dz = f;
}pt.set (1, 0, 0);
q = JU.Quat.newVA (pt, 90).mulQ (q);
var f = dy;
dy = dz;
dz = f;
}return (type == 1313866249 ? vMin + "\t{" + dx + " " + dy + " " + dz + "}" : q.getTheta () == 0 ? "{0 0 0 1}" : q.toString ());
}, "~N,JU.BS");
Clazz.defineMethod (c$, "intersectPlane", 
function (plane, v, i) {
return (this.triangulator == null ? (this.triangulator = J.api.Interface.getUtil ("TriangleData")) : this.triangulator).intersectPlane (plane, v, i);
}, "JU.P4,JU.List,~N");
Clazz.defineMethod (c$, "getUnitCellForAtom", 
function (index) {
if (index < 0 || index > this.ac) return null;
if (this.bsModulated != null) {
var v = this.getVibration (index, false);
if (v != null) return v.getUnitCell ();
}return this.getUnitCell (this.at[index].mi);
}, "~N");
Clazz.defineStatics (c$,
"hbondMin", 2.5);
});
