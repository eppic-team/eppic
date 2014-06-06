Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.JanaReader", ["java.lang.Float", "java.util.Hashtable", "JU.BS", "$.List", "$.Matrix", "$.PT", "$.Rdr", "J.adapter.smarter.Atom", "J.api.Interface", "JW.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.lattvecs = null;
this.thisSub = 0;
this.modAverage = false;
this.modAxes = null;
this.modDim = 0;
this.qicount = 0;
this.LABELS = "xyz";
this.floats = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "JanaReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.floats =  Clazz.newFloatArray (6, 0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.modAverage = this.checkFilterKey ("MODAVE");
this.modAxes = this.getFilter ("MODAXES=");
this.setFractionalCoordinates (true);
this.asc.newAtomSet ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.length < 3) return true;
JW.Logger.info (this.line);
this.parseTokenStr (this.line);
switch ("tit  cell ndim qi   lat  sym  spg  end  wma".indexOf (this.line.substring (0, 3))) {
case 0:
this.asc.setAtomSetName (this.line.substring (5).trim ());
break;
case 5:
this.cell ();
this.setSymmetryOperator ("x,y,z");
break;
case 10:
this.ndim ();
break;
case 20:
if (this.lattvecs == null) this.lattvecs =  new JU.List ();
if (!this.ms.addLatticeVector (this.lattvecs, this.line.substring (8))) this.appendLoadNote (this.line + " not supported");
break;
case 30:
this.setSpaceGroupName (this.getTokens ()[1]);
break;
case 25:
this.symmetry ();
break;
case 15:
if (!this.modAverage) this.qi ();
break;
case 35:
this.continuing = false;
break;
case 40:
var n = 3 + this.modDim;
var m;
if (this.thisSub++ == 0) {
m = JU.Matrix.identity (n, n);
this.ms.addSubsystem ("" + this.thisSub++, m);
}m =  new JU.Matrix (null, n, n);
var a = m.getArray ();
var data =  Clazz.newFloatArray (n * n, 0);
this.fillFloatArray (null, 0, data);
for (var i = 0, pt = 0; i < n; i++) for (var j = 0; j < n; j++, pt++) a[i][j] = data[pt];


this.ms.addSubsystem ("" + this.thisSub, m);
}
return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.readM40Data ();
if (this.lattvecs != null && this.lattvecs.size () > 0) this.asc.getSymmetry ().addLatticeVectors (this.lattvecs);
if (this.ms != null) {
this.ms.setModulation (false);
}this.applySymmetryAndSetTrajectory ();
this.adjustM40Occupancies ();
if (this.ms != null) {
this.ms.setModulation (true);
this.ms.finalizeModulation ();
}this.finalizeReaderASCR ();
});
Clazz.defineMethod (c$, "cell", 
 function () {
for (var ipt = 0; ipt < 6; ipt++) this.setUnitCellItem (ipt, this.parseFloat ());

});
Clazz.defineMethod (c$, "ndim", 
 function () {
this.ms = J.api.Interface.getOption ("adapter.readers.cif.MSReader");
this.modDim = this.ms.initialize (this, "" + (this.parseIntStr (this.getTokens ()[1]) - 3));
});
Clazz.defineMethod (c$, "qi", 
 function () {
var pt =  Clazz.newDoubleArray (this.modDim, 0);
pt[this.qicount] = 1;
this.ms.addModulation (null, "W_" + (++this.qicount), [this.parseFloat (), this.parseFloat (), this.parseFloat ()], -1);
this.ms.addModulation (null, "F_" + this.qicount + "_coefs_", pt, -1);
});
Clazz.defineMethod (c$, "symmetry", 
 function () {
this.setSymmetryOperator (JU.PT.rep (this.line.substring (9).trim (), " ", ","));
});
Clazz.defineMethod (c$, "readM40Data", 
 function () {
var name = this.filePath;
var ipt = name.lastIndexOf (".");
if (ipt < 0) return;
name = name.substring (0, ipt + 2) + "40";
var id = name.substring (0, ipt);
ipt = id.lastIndexOf ("/");
id = id.substring (ipt + 1);
var r = JU.Rdr.getBR (this.vwr.getLigandModel (id, name, "_file", "----"));
if (this.readM40Floats (r).startsWith ("command")) this.readM40WaveVectors (r);
var newSub = this.getSubSystemList ();
var iSub = (newSub == null ? 0 : 1);
var nAtoms = -1;
while (this.readM40Floats (r) != null) {
while (this.line != null && (this.line.length == 0 || this.line.charAt (0) == ' ' || this.line.charAt (0) == '-')) {
this.readM40Floats (r);
}
if (this.line == null) break;
nAtoms++;
var atom =  new J.adapter.smarter.Atom ();
JW.Logger.info (this.line);
atom.atomName = this.line.substring (0, 9).trim ();
if (!this.filterAtom (atom, 0)) continue;
if (iSub > 0) {
if (newSub.get (nAtoms)) iSub++;
atom.altLoc = ("" + iSub).charAt (0);
}var o_site = atom.foccupancy = this.floats[2];
this.setAtomCoordXYZ (atom, this.floats[3], this.floats[4], this.floats[5]);
this.asc.addAtom (atom);
if (this.modDim == 0) continue;
var label = ";" + atom.atomName;
var haveSpecialOcc = (this.getInt (60, 61) > 0);
var haveSpecialDisp = (this.getInt (61, 62) > 0);
var haveSpecialUij = (this.getInt (62, 63) > 0);
var nOcc = this.getInt (65, 68);
var nDisp = this.getInt (68, 71);
var nUij = this.getInt (71, 74);
this.readM40Floats (r);
var extended = false;
if (Float.isNaN (this.floats[0])) {
extended = true;
this.readM40Floats (r);
}var isIso = true;
for (var j = 1; j < 6; j++) if (this.floats[j] != 0) {
isIso = false;
break;
}
if (isIso) {
if (this.floats[0] != 0) this.setU (atom, 7, this.floats[0]);
} else {
for (var j = 0; j < 6; j++) this.setU (atom, j, this.floats[j]);

}if (extended) {
r.readLine ();
r.readLine ();
}var pt;
var o_0 = (nOcc > 0 && !haveSpecialOcc ? this.parseFloatStr (r.readLine ()) : 1);
if (o_0 != 1) {
this.ms.addModulation (null, "J_O#0;" + atom.atomName, [o_site, o_0, 0], -1);
}atom.foccupancy = o_0 * o_site;
var wv = 0;
var a1;
var a2;
for (var j = 0; j < nOcc; j++) {
if (haveSpecialOcc) {
var data = this.readM40FloatLines (2, 1, r);
a2 = data[0][0];
a1 = data[1][0];
} else {
wv = j + 1;
this.readM40Floats (r);
a2 = this.floats[0];
a1 = this.floats[1];
}id = "O_" + wv + "#0" + label;
pt = [a1, a2, 0];
if (a1 != 0 || a2 != 0) this.ms.addModulation (null, id, pt, -1);
}
for (var j = 0; j < nDisp; j++) {
if (haveSpecialDisp) {
this.readM40Floats (r);
var c = this.floats[3];
var w = this.floats[4];
for (var k = 0; k < 3; k++) if (this.floats[k] != 0) this.ms.addModulation (null, "D_S#" + "xyz".charAt (k) + label, [c, w, this.floats[k]], -1);

} else {
this.addSinCos (j, "D_", label, r);
}}
for (var j = 0; j < nUij; j++) {
this.checkFourier (j);
if (isIso) {
this.addSinCos (j, "U_", label, r);
} else {
if (haveSpecialUij) {
JW.Logger.error ("JanaReader -- not interpreting SpecialUij flag: " + this.line);
} else {
var data = this.readM40FloatLines (2, 6, r);
for (var k = 0, p = 0; k < 6; k++, p += 3) this.ms.addModulation (null, "U_" + (j + 1) + "#" + "U11U22U33U12U13U23UISO".substring (p, p + 3) + label, [data[1][k], data[0][k], 0], -1);

}}}
}
r.close ();
});
Clazz.defineMethod (c$, "getSubSystemList", 
 function () {
if (this.thisSub == 0) return null;
var bs =  new JU.BS ();
var tokens = this.getTokens ();
for (var i = 0, n = 0; i < tokens.length; i += 2) {
var nAtoms = this.parseIntStr (tokens[i]);
if (nAtoms == 0) break;
bs.set (n = n + nAtoms);
}
return bs;
});
Clazz.defineMethod (c$, "readM40WaveVectors", 
 function (r) {
while (!this.readM40Floats (r).contains ("end")) if (this.line.startsWith ("wave")) {
var tokens = this.getTokens ();
var pt =  Clazz.newDoubleArray (this.modDim, 0);
for (var i = 0; i < this.modDim; i++) pt[i] = this.parseFloatStr (tokens[i + 2]);

this.ms.addModulation (null, "F_" + this.parseIntStr (tokens[1]) + "_coefs_", pt, -1);
}
this.readM40Floats (r);
}, "java.io.BufferedReader");
Clazz.defineMethod (c$, "addSinCos", 
 function (j, key, label, r) {
this.checkFourier (j);
this.readM40Floats (r);
for (var k = 0; k < 3; ++k) {
var ccos = this.floats[k + 3];
var csin = this.floats[k];
if (csin == 0 && ccos == 0) continue;
var axis = "" + "xyz".charAt (k % 3);
if (this.modAxes != null && this.modAxes.indexOf (axis.toUpperCase ()) < 0) continue;
var id = key + (j + 1) + "#" + axis + label;
this.ms.addModulation (null, id, [ccos, csin, 0], -1);
}
}, "~N,~S,~S,java.io.BufferedReader");
Clazz.defineMethod (c$, "checkFourier", 
 function (j) {
var pt;
if (j > 0 && this.ms.getMod ("F_" + (++j) + "_coefs_") == null && (pt = this.ms.getMod ("F_1_coefs_")) != null) {
var p =  Clazz.newDoubleArray (this.modDim, 0);
for (var i = this.modDim; --i >= 0; ) p[i] = pt[i] * j;

this.ms.addModulation (null, "F_" + j + "_coefs_", p, -1);
}}, "~N");
Clazz.defineMethod (c$, "getInt", 
 function (col1, col2) {
var n = this.line.length;
return (n > col1 ? this.parseIntStr (this.line.substring (col1, Math.min (n, col2))) : 0);
}, "~N,~N");
Clazz.defineMethod (c$, "readM40Floats", 
 function (r) {
if ((this.line = r.readLine ()) == null || this.line.indexOf ("-------") >= 0) return (this.line = null);
if (JW.Logger.debugging) JW.Logger.debug (this.line);
var ptLast = this.line.length - 9;
for (var i = 0, pt = 0; i < 6 && pt <= ptLast; i++, pt += 9) this.floats[i] = this.parseFloatStr (this.line.substring (pt, pt + 9));

return this.line;
}, "java.io.BufferedReader");
Clazz.defineMethod (c$, "readM40FloatLines", 
 function (nLines, nFloats, r) {
var data =  Clazz.newFloatArray (nLines, nFloats, 0);
for (var i = 0; i < nLines; i++) {
this.readM40Floats (r);
for (var j = 0; j < nFloats; j++) data[i][j] = this.floats[j];

}
return data;
}, "~N,~N,java.io.BufferedReader");
Clazz.defineMethod (c$, "adjustM40Occupancies", 
 function () {
var htSiteMult =  new java.util.Hashtable ();
var atoms = this.asc.atoms;
var symmetry = this.asc.getSymmetry ();
for (var i = this.asc.ac; --i >= 0; ) {
var a = atoms[i];
var ii = htSiteMult.get (a.atomName);
if (ii == null) htSiteMult.put (a.atomName, ii = Integer.$valueOf (symmetry.getSiteMultiplicity (a)));
a.foccupancy *= ii.intValue ();
}
});
Clazz.overrideMethod (c$, "doPreSymmetry", 
function () {
if (this.ms != null) this.ms.setModulation (false);
});
Clazz.defineStatics (c$,
"records", "tit  cell ndim qi   lat  sym  spg  end  wma",
"TITLE", 0,
"CELL", 5,
"NDIM", 10,
"QI", 15,
"LATT", 20,
"SYM", 25,
"SPG", 30,
"END", 35,
"WMATRIX", 40,
"U_LIST", "U11U22U33U12U13U23UISO");
});
