Clazz.declarePackage ("J.adapter.readers.cif");
Clazz.load (["J.adapter.smarter.MSInterface"], "J.adapter.readers.cif.MSReader", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "JU.List", "$.M3", "$.Matrix", "$.P3", "$.SB", "J.adapter.readers.cif.Subsystem", "J.adapter.smarter.AtomSetCollectionReader", "JW.BSUtil", "$.BoxInfo", "$.Escape", "$.Logger", "$.Modulation", "$.ModulationSet"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cr = null;
this.modDim = 0;
this.modAxes = null;
this.modAverage = false;
this.isCommensurate = false;
this.commensurateSection1 = 0;
this.modPack = false;
this.modVib = false;
this.modType = null;
this.modCell = null;
this.modDebug = false;
this.modSelected = -1;
this.modLast = false;
this.sigma = null;
this.q1 = null;
this.q1Norm = null;
this.htModulation = null;
this.htAtomMods = null;
this.iopLast = -1;
this.gammaE = null;
this.nOps = 0;
this.haveOccupancy = false;
this.atoms = null;
this.ac = 0;
this.haveAtomMods = false;
this.modCoord = false;
this.finalized = false;
this.atModel = "@0";
this.modMatrices = null;
this.qs = null;
this.modCount = 0;
this.htSubsystems = null;
this.minXYZ0 = null;
this.maxXYZ0 = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.cif, "MSReader", null, J.adapter.smarter.MSInterface);
Clazz.defineMethod (c$, "getSigma", 
function () {
return this.sigma;
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "initialize", 
function (r, data) {
this.cr = r;
this.modCoord = r.checkFilterKey ("MODCOORD");
this.modDebug = r.checkFilterKey ("MODDEBUG");
this.modPack = !r.checkFilterKey ("MODNOPACK");
this.modLast = r.checkFilterKey ("MODLAST");
this.modAxes = r.getFilter ("MODAXES=");
this.modType = r.getFilter ("MODTYPE=");
this.modCell = r.getFilter ("MODCELL=");
this.modSelected = r.parseIntStr ("" + r.getFilter ("MOD="));
this.modVib = r.checkFilterKey ("MODVIB");
this.modAverage = r.checkFilterKey ("MODAVE");
this.setModDim (r.parseIntStr (data));
return this.modDim;
}, "J.adapter.smarter.AtomSetCollectionReader,~S");
Clazz.defineMethod (c$, "setSubsystemOptions", 
 function () {
this.cr.doPackUnitCell = this.modPack;
if (!this.cr.doApplySymmetry) {
this.cr.doApplySymmetry = true;
this.cr.latticeCells[0] = 1;
this.cr.latticeCells[1] = 1;
this.cr.latticeCells[2] = 1;
}if (this.modCell != null) this.cr.addJmolScript ("unitcell {%" + this.modCell + "}");
});
Clazz.defineMethod (c$, "setModDim", 
function (ndim) {
if (this.modAverage) return;
this.modDim = ndim;
if (this.modDim > 3) {
this.cr.appendLoadNote ("Too high modulation dimension (" + this.modDim + ") -- reading average structure");
this.modDim = 0;
this.modAverage = true;
} else {
this.cr.appendLoadNote ("Modulation dimension = " + this.modDim);
this.htModulation =  new java.util.Hashtable ();
}}, "~N");
Clazz.overrideMethod (c$, "addModulation", 
function (map, id, pt, iModel) {
var ch = id.charAt (0);
switch (ch) {
case 'O':
case 'D':
case 'U':
if (this.modType != null && this.modType.indexOf (ch) < 0 || this.modSelected > 0 && this.modSelected != 1) return;
break;
}
var isOK = false;
for (var i = pt.length; --i >= 0; ) {
if (this.modSelected > 0 && i + 1 != this.modSelected && id.contains ("_coefs_")) {
pt[i] = 0;
} else if (pt[i] != 0) {
isOK = true;
break;
}}
if (!isOK) return;
if (map == null) map = this.htModulation;
id += "@" + (iModel >= 0 ? iModel : this.cr.asc.currentAtomSetIndex);
JW.Logger.info ("Adding " + id + " " + JW.Escape.e (pt));
map.put (id, pt);
}, "java.util.Map,~S,~A,~N");
Clazz.overrideMethod (c$, "setModulation", 
function (isPost) {
if (this.modDim == 0 || this.htModulation == null) return;
if (this.modDebug) JW.Logger.debugging = JW.Logger.debuggingHigh = true;
this.cr.asc.setAtomSetCollectionAuxiliaryInfo ("someModelsAreModulated", Boolean.TRUE);
this.setModulationForStructure (this.cr.asc.currentAtomSetIndex, isPost);
if (this.modDebug) JW.Logger.debugging = JW.Logger.debuggingHigh = false;
}, "~B");
Clazz.overrideMethod (c$, "finalizeModulation", 
function () {
if (!this.finalized && this.modDim > 0 && !this.modVib) {
this.cr.asc.setAtomSetCollectionAuxiliaryInfo ("modulationOn", Boolean.TRUE);
this.cr.addJmolScript ((this.haveOccupancy && !this.isCommensurate ? ";display occupancy >= 0.5" : ""));
}this.finalized = true;
});
Clazz.defineMethod (c$, "checkKey", 
 function (key, checkQ) {
var pt = key.indexOf (this.atModel);
return (pt < 0 || key.indexOf ("*;*") >= 0 || checkQ && key.indexOf ("?") >= 0 ? null : key.substring (0, pt));
}, "~S,~B");
Clazz.overrideMethod (c$, "getMod", 
function (key) {
return this.htModulation.get (key + this.atModel);
}, "~S");
Clazz.defineMethod (c$, "setModulationForStructure", 
 function (iModel, isPost) {
this.atModel = "@" + iModel;
if (this.htModulation.containsKey ("X_" + this.atModel)) return;
if (!isPost) {
this.initModForStructure (iModel);
return;
}this.htModulation.put ("X_" + this.atModel,  Clazz.newDoubleArray (0, 0));
if (!this.haveAtomMods) return;
var n = this.cr.asc.ac;
this.atoms = this.cr.asc.atoms;
this.cr.symmetry = this.cr.asc.getSymmetry ();
if (this.cr.symmetry != null) this.nOps = this.cr.symmetry.getSpaceGroupOperationCount ();
this.iopLast = -1;
var sb =  new JU.SB ();
for (var i = this.cr.asc.getLastAtomSetAtomIndex (); i < n; i++) this.modulateAtom (this.atoms[i], sb);

this.cr.asc.setAtomSetAtomProperty ("modt", sb.toString (), -1);
this.cr.appendLoadNote (this.modCount + " modulations for " + this.ac + " atoms");
this.htAtomMods = null;
if (this.minXYZ0 != null) this.trimAtomSet ();
this.htSubsystems = null;
}, "~N,~B");
Clazz.defineMethod (c$, "initModForStructure", 
 function (iModel) {
var key;
this.sigma =  new JU.Matrix (null, this.modDim, 3);
this.qs = null;
this.modMatrices = [this.sigma, null];
for (var i = 0; i < this.modDim; i++) {
var pt = this.getMod ("W_" + (i + 1));
if (pt == null) {
JW.Logger.info ("Not enough cell wave vectors for d=" + this.modDim);
return;
}this.cr.appendLoadNote ("W_" + (i + 1) + " = " + JW.Escape.e (pt));
this.sigma.getArray ()[i] = [pt[0], pt[1], pt[2]];
}
this.q1 = this.sigma.getArray ()[0];
this.q1Norm = JU.P3.new3 (this.q1[0] == 0 ? 0 : 1, this.q1[1] == 0 ? 0 : 1, this.q1[2] == 0 ? 0 : 1);
var qlist100 =  Clazz.newDoubleArray (this.modDim, 0);
qlist100[0] = 1;
var pt;
var map =  new java.util.Hashtable ();
for (var e, $e = this.htModulation.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
if ((key = this.checkKey (e.getKey (), false)) == null) continue;
pt = e.getValue ();
switch (key.charAt (0)) {
case 'O':
this.haveOccupancy = true;
case 'U':
case 'D':
if (pt[2] == 1 && key.charAt (2) != 'S') {
var ipt = key.indexOf ("?");
if (ipt >= 0) {
var s = key.substring (ipt + 1);
pt = this.getMod (key.substring (0, 2) + s + "#*;*");
if (pt != null) this.addModulation (map, key = key.substring (0, ipt), pt, iModel);
} else {
var a = pt[0];
var d = 2 * 3.141592653589793 * pt[1];
pt[0] = (a * Math.cos (d));
pt[1] = (a * Math.sin (-d));
pt[2] = 0;
JW.Logger.info ("msCIF setting " + key + " " + JW.Escape.e (pt));
}}break;
case 'W':
if (this.modDim > 1) {
continue;
}case 'F':
if (key.indexOf ("_coefs_") >= 0) {
this.cr.appendLoadNote ("Wave vector " + key + "=" + JW.Escape.e (pt));
} else {
var ptHarmonic = this.getQCoefs (pt);
if (ptHarmonic == null) {
this.cr.appendLoadNote ("Cannot match atom wave vector " + key + " " + JW.Escape.eAD (pt) + " to a cell wave vector or its harmonic");
} else {
var k2 = key + "_coefs_";
if (!this.htModulation.containsKey (k2 + this.atModel)) {
this.addModulation (map, k2, ptHarmonic, iModel);
if (key.startsWith ("F_")) this.cr.appendLoadNote ("atom wave vector " + key + " = " + JW.Escape.e (pt) + " fn = " + JW.Escape.e (ptHarmonic));
}}}break;
}
}
if (!map.isEmpty ()) this.htModulation.putAll (map);
if (this.htSubsystems == null) {
this.haveAtomMods = false;
} else {
this.haveAtomMods = true;
this.htAtomMods =  new java.util.Hashtable ();
}for (var e, $e = this.htModulation.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
if ((key = this.checkKey (e.getKey (), true)) == null) continue;
var params = e.getValue ();
var atomName = key.substring (key.indexOf (";") + 1);
var pt_ = atomName.indexOf ("#=");
if (pt_ >= 0) {
params = this.getMod (atomName.substring (pt_ + 2));
atomName = atomName.substring (0, pt_);
}if (JW.Logger.debuggingHigh) JW.Logger.debug ("SetModulation: " + key + " " + JW.Escape.e (params));
var type = key.charAt (0);
pt_ = key.indexOf ("#") + 1;
var utens = null;
switch (type) {
case 'U':
utens = key.substring (4, key.indexOf (";"));
case 'O':
case 'D':
var id = key.charAt (2);
var axis = key.charAt (pt_);
type = (id == 'S' ? 's' : id == '0' ? 'c' : type == 'O' ? 'o' : type == 'U' ? 'u' : 'f');
if (this.htAtomMods == null) this.htAtomMods =  new java.util.Hashtable ();
var fn = (id == 'S' ? 0 : this.cr.parseIntStr (key.substring (2)));
var p = [params[0], params[1], params[2]];
if (fn == 0) {
this.addAtomModulation (atomName, axis, type, p, utens, qlist100);
} else {
var qlist = this.getMod ("F_" + fn + "_coefs_");
if (qlist == null) {
JW.Logger.error ("Missing qlist for F_" + fn);
this.cr.appendLoadNote ("Missing cell wave vector for atom wave vector " + fn + " for " + key + " " + JW.Escape.e (params));
qlist = this.getMod ("F_1_coefs_");
}if (qlist != null) {
this.addAtomModulation (atomName, axis, type, p, utens, qlist);
}}this.haveAtomMods = true;
break;
}
}
}, "~N");
Clazz.defineMethod (c$, "getQCoefs", 
 function (p) {
if (this.qs == null) {
this.qs =  new Array (this.modDim);
for (var i = 0; i < this.modDim; i++) {
this.qs[i] = this.toP3 (this.getMod ("W_" + (i + 1)));
}
}var pt = this.toP3 (p);
for (var i = 0; i < this.modDim; i++) if (this.qs[i] != null) {
var ifn = this.approxInt (pt.dot (this.qs[i]) / this.qs[i].dot (this.qs[i]));
if (ifn != 0) {
p =  Clazz.newDoubleArray (this.modDim, 0);
p[i] = ifn;
return p;
}}
var p3 = this.toP3 (p);
var jmin = (this.modDim < 2 ? 0 : -3);
var jmax = (this.modDim < 2 ? 0 : 3);
var kmin = (this.modDim < 3 ? 0 : -3);
var kmax = (this.modDim < 3 ? 0 : 3);
for (var i = -3; i <= 3; i++) for (var j = jmin; j <= jmax; j++) for (var k = kmin; k <= kmax; k++) {
pt.setT (this.qs[0]);
pt.scale (i);
if (this.modDim > 1 && this.qs[1] != null) pt.scaleAdd2 (j, this.qs[1], pt);
if (this.modDim > 2 && this.qs[2] != null) pt.scaleAdd2 (k, this.qs[2], pt);
if (pt.distanceSquared (p3) < 0.0001) {
p =  Clazz.newDoubleArray (this.modDim, 0);
switch (this.modDim) {
default:
p[2] = k;
case 2:
p[1] = j;
case 1:
p[0] = i;
break;
}
return p;
}}


pt = this.toP3 (p);
for (var i = 0; i < this.modDim; i++) if (this.qs[i] != null) {
p3 = this.qs[i];
var ifn = 0;
if (pt.x != 0) ifn = this.approxInt (pt.x / p3.x);
if (pt.y != 0) ifn = Math.max (this.approxInt (pt.y / p3.y), ifn);
if (ifn == 0 && pt.z != 0) ifn = Math.max (this.approxInt (pt.z / p3.z), ifn);
if (ifn == 0) continue;
if (p3.x != 0 && this.approxInt (10 + p3.x * ifn - pt.x) == 0 || p3.y != 0 && this.approxInt (10 + p3.y * ifn - pt.y) == 0 || p3.z != 0 && this.approxInt (10 + p3.z * ifn - pt.z) == 0) continue;
p =  Clazz.newDoubleArray (this.modDim, 0);
p[i] = ifn;
return p;
}
return null;
}, "~A");
Clazz.defineMethod (c$, "approxInt", 
 function (fn) {
var ifn = Math.round (fn);
return (Math.abs (fn - ifn) < 0.001 ? ifn : 0);
}, "~N");
Clazz.defineMethod (c$, "toP3", 
 function (x) {
return JU.P3.new3 (x[0], x[1], x[2]);
}, "~A");
Clazz.defineMethod (c$, "addAtomModulation", 
 function (atomName, axis, type, params, utens, qcoefs) {
var list = this.htAtomMods.get (atomName);
if (list == null) {
this.ac++;
this.htAtomMods.put (atomName, list =  new JU.List ());
}list.addLast ( new JW.Modulation (axis, type, params, utens, qcoefs));
this.modCount++;
}, "~S,~S,~S,~A,~S,~A");
Clazz.overrideMethod (c$, "addSubsystem", 
function (code, w) {
if (code == null) return;
var ss =  new J.adapter.readers.cif.Subsystem (this, code, w);
this.cr.appendLoadNote ("subsystem " + code + "\n" + w);
this.setSubsystem (code, ss);
}, "~S,JU.Matrix");
Clazz.defineMethod (c$, "addUStr", 
 function (atom, id, val) {
var i = Clazz.doubleToInt ("U11U22U33U12U13U23UISO".indexOf (id) / 3);
if (JW.Logger.debuggingHigh) JW.Logger.debug ("MOD RDR adding " + id + " " + i + " " + val + " to " + atom.anisoBorU[i]);
if (atom.anisoBorU == null) JW.Logger.error ("MOD RDR cannot modulate nonexistent atom anisoBorU for atom " + atom.atomName);
 else this.cr.setU (atom, i, val + atom.anisoBorU[i]);
}, "J.adapter.smarter.Atom,~S,~N");
Clazz.defineMethod (c$, "modulateAtom", 
 function (a, sb) {
if (this.modCoord && this.htSubsystems != null) {
var ptc = JU.P3.newP (a);
var spt = this.getSymmetry (a);
spt.toCartesian (ptc, true);
}var list = this.htAtomMods.get (a.atomName);
if (list == null && a.altLoc != '\0' && this.htSubsystems != null) {
list =  new JU.List ();
}if (list == null || this.cr.symmetry == null || a.bsSymmetry == null) return;
var iop = Math.max (a.bsSymmetry.nextSetBit (0), 0);
if (this.modLast) iop = Math.max ((a.bsSymmetry.length () - 1) % this.nOps, iop);
if (JW.Logger.debuggingHigh) JW.Logger.debug ("\nsetModulation: i=" + a.index + " " + a.atomName + " xyz=" + a + " occ=" + a.foccupancy);
if (iop != this.iopLast) {
this.iopLast = iop;
this.gammaE =  new JU.M3 ();
this.getSymmetry (a).getSpaceGroupOperation (iop).getRotationScale (this.gammaE);
}if (JW.Logger.debugging) {
JW.Logger.debug ("setModulation iop = " + iop + " " + this.cr.symmetry.getSpaceGroupXyz (iop, false) + " " + a.bsSymmetry);
}var ms =  new JW.ModulationSet ().set (a.index + " " + a.atomName, JU.P3.newP (a), this.modDim, list, this.gammaE, this.getMatrices (a), iop, this.getSymmetry (a));
ms.calculate (null, false);
if (!Float.isNaN (ms.vOcc)) {
var pt = this.getMod ("J_O#0;" + a.atomName);
var occ0 = ms.vOcc0;
var occ;
if (Float.isNaN (occ0)) {
occ = ms.vOcc;
} else if (pt == null) {
occ = a.foccupancy + ms.vOcc;
} else if (a.vib != null) {
var site_mult = a.vib.x;
var o_site = a.foccupancy * site_mult / this.nOps / pt[1];
occ = o_site * (pt[1] + ms.vOcc);
} else {
occ = pt[0] * (pt[1] + ms.vOcc);
}a.foccupancy = Math.min (1, Math.max (0, occ));
}if (ms.htUij != null) {
if (JW.Logger.debuggingHigh) {
JW.Logger.debug ("setModulation Uij(initial)=" + JW.Escape.eAF (a.anisoBorU));
JW.Logger.debug ("setModulation tensor=" + JW.Escape.e ((a.tensors.get (0)).getInfo ("all")));
}for (var e, $e = ms.htUij.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) this.addUStr (a, e.getKey (), e.getValue ().floatValue ());

if (a.tensors != null) (a.tensors.get (0)).isUnmodulated = true;
var symmetry = this.getAtomSymmetry (a, this.cr.symmetry);
var t = this.cr.asc.getXSymmetry ().addRotatedTensor (a, symmetry.getTensor (a.anisoBorU), iop, false, symmetry);
t.isModulated = true;
if (JW.Logger.debuggingHigh) {
JW.Logger.debug ("setModulation Uij(final)=" + JW.Escape.eAF (a.anisoBorU) + "\n");
JW.Logger.debug ("setModulation tensor=" + (a.tensors.get (0)).getInfo ("all"));
}}if (Float.isNaN (ms.x)) ms.set (0, 0, 0);
a.vib = ms;
if (this.modVib || a.foccupancy != 0) {
var t = this.q1Norm.dot (a);
if (Math.abs (t - Clazz.floatToInt (t)) > 0.001) t = Clazz.doubleToInt (Math.floor (t));
sb.append ((Clazz.floatToInt (t)) + "\n");
}}, "J.adapter.smarter.Atom,JU.SB");
Clazz.overrideMethod (c$, "getAtomSymmetry", 
function (a, defaultSymmetry) {
var ss;
return (this.htSubsystems == null || (ss = this.getSubsystem (a)) == null ? defaultSymmetry : ss.getSymmetry ());
}, "J.adapter.smarter.Atom,J.api.SymmetryInterface");
Clazz.defineMethod (c$, "setSubsystem", 
 function (code, system) {
if (this.htSubsystems == null) this.htSubsystems =  new java.util.Hashtable ();
this.htSubsystems.put (code, system);
this.setSubsystemOptions ();
}, "~S,J.adapter.readers.cif.Subsystem");
Clazz.defineMethod (c$, "getMatrices", 
 function (a) {
var ss = this.getSubsystem (a);
return (ss == null ? this.modMatrices : ss.getModMatrices ());
}, "J.adapter.smarter.Atom");
Clazz.defineMethod (c$, "getSymmetry", 
 function (a) {
var ss = this.getSubsystem (a);
return (ss == null ? this.cr.symmetry : ss.getSymmetry ());
}, "J.adapter.smarter.Atom");
Clazz.defineMethod (c$, "getSubsystem", 
 function (a) {
return (this.htSubsystems == null ? null : this.htSubsystems.get ("" + a.altLoc));
}, "J.adapter.smarter.Atom");
Clazz.overrideMethod (c$, "setMinMax0", 
function (minXYZ, maxXYZ) {
if (this.htSubsystems == null) return;
var symmetry = this.getDefaultUnitCell ();
this.minXYZ0 = JU.P3.newP (minXYZ);
this.maxXYZ0 = JU.P3.newP (maxXYZ);
var pt0 = JU.P3.newP (minXYZ);
var pt1 = JU.P3.newP (maxXYZ);
var pt =  new JU.P3 ();
symmetry.toCartesian (pt0, true);
symmetry.toCartesian (pt1, true);
var pts = JW.BoxInfo.unitCubePoints;
for (var e, $e = this.htSubsystems.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var sym = e.getValue ().getSymmetry ();
for (var i = 8; --i >= 0; ) {
pt.x = (pts[i].x == 0 ? pt0.x : pt1.x);
pt.y = (pts[i].y == 0 ? pt0.y : pt1.y);
pt.z = (pts[i].z == 0 ? pt0.z : pt1.z);
this.expandMinMax (pt, sym, minXYZ, maxXYZ);
}
}
}, "JU.P3,JU.P3");
Clazz.defineMethod (c$, "expandMinMax", 
 function (pt, sym, minXYZ, maxXYZ) {
var pt2 = JU.P3.newP (pt);
var slop = 0.0001;
sym.toFractional (pt2, false);
if (minXYZ.x > pt2.x + slop) minXYZ.x = Clazz.doubleToInt (Math.floor (pt2.x)) - 1;
if (minXYZ.y > pt2.y + slop) minXYZ.y = Clazz.doubleToInt (Math.floor (pt2.y)) - 1;
if (minXYZ.z > pt2.z + slop) minXYZ.z = Clazz.doubleToInt (Math.floor (pt2.z)) - 1;
if (maxXYZ.x < pt2.x - slop) maxXYZ.x = Clazz.doubleToInt (Math.ceil (pt2.x)) + 1;
if (maxXYZ.y < pt2.y - slop) maxXYZ.y = Clazz.doubleToInt (Math.ceil (pt2.y)) + 1;
if (maxXYZ.z < pt2.z - slop) maxXYZ.z = Clazz.doubleToInt (Math.ceil (pt2.z)) + 1;
}, "JU.P3,J.api.SymmetryInterface,JU.P3,JU.P3");
Clazz.defineMethod (c$, "trimAtomSet", 
 function () {
if (!this.cr.doApplySymmetry) return;
var asc = this.cr.asc;
var bs = asc.bsAtoms;
var sym = this.getDefaultUnitCell ();
var atoms = asc.atoms;
var pt =  new JU.P3 ();
if (bs == null) bs = asc.bsAtoms = JW.BSUtil.newBitSet2 (0, asc.ac);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = atoms[i];
pt.setT (a);
pt.add (a.vib);
this.getSymmetry (a).toCartesian (pt, false);
sym.toFractional (pt, false);
if (!asc.xtalSymmetry.isWithinCell (3, pt, this.minXYZ0.x, this.maxXYZ0.x, this.minXYZ0.y, this.maxXYZ0.y, this.minXYZ0.z, this.maxXYZ0.z, 0.001) || this.isCommensurate && a.foccupancy < 0.5) bs.clear (i);
}
});
Clazz.defineMethod (c$, "getDefaultUnitCell", 
 function () {
return (this.modCell != null && this.htSubsystems.containsKey (this.modCell) ? this.htSubsystems.get (this.modCell).getSymmetry () : this.cr.asc.getSymmetry ());
});
Clazz.overrideMethod (c$, "getSymmetryFromCode", 
function (code) {
return this.htSubsystems.get (code).getSymmetry ();
}, "~S");
Clazz.overrideMethod (c$, "addLatticeVector", 
function (lattvecs, data) {
var a = null;
var c = data.charAt (0);
switch (c) {
case 'P':
case 'X':
break;
case 'A':
case 'B':
case 'C':
case 'I':
a = [0.5, 0.5, 0.5];
if (c != 'I') a[c.charCodeAt (0) - 65] = 0;
break;
case 'F':
this.addLatticeVector (lattvecs, "A");
this.addLatticeVector (lattvecs, "B");
this.addLatticeVector (lattvecs, "C");
break;
case '0':
if (data.indexOf (".") >= 0) a = J.adapter.smarter.AtomSetCollectionReader.getTokensFloat (data, null, this.modDim + 3);
break;
default:
return false;
}
if (a != null) lattvecs.addLast (a);
return true;
}, "JU.List,~S");
Clazz.defineStatics (c$,
"U_LIST", "U11U22U33U12U13U23UISO");
});
