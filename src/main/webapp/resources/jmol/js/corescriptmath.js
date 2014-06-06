(function(Clazz
,Clazz_doubleToInt
,Clazz_declarePackage
,Clazz_instanceOf
,Clazz_load
,Clazz_instantialize
,Clazz_decorateAsClass
,Clazz_floatToInt
,Clazz_makeConstructor
,Clazz_defineEnumConstant
,Clazz_exceptionOf
,Clazz_newIntArray
,Clazz_defineStatics
,Clazz_newFloatArray
,Clazz_declareType
,Clazz_prepareFields
,Clazz_superConstructor
,Clazz_newByteArray
,Clazz_declareInterface
,Clazz_p0p
,Clazz_pu$h
,Clazz_newShortArray
,Clazz_innerTypeInstance
,Clazz_isClassDefined
,Clazz_prepareCallback
,Clazz_newArray
,Clazz_castNullAs
,Clazz_floatToShort
,Clazz_superCall
,Clazz_decorateAsType
,Clazz_newBooleanArray
,Clazz_newCharArray
,Clazz_implementOf
,Clazz_newDoubleArray
,Clazz_overrideConstructor
,Clazz_clone
,Clazz_doubleToShort
,Clazz_getInheritedLevel
,Clazz_getParamsType
,Clazz_isAF
,Clazz_isAI
,Clazz_isAS
,Clazz_isASS
,Clazz_isAP
,Clazz_isAFloat
,Clazz_isAII
,Clazz_isAFF
,Clazz_isAFFF
,Clazz_tryToSearchAndExecute
,Clazz_getStackTrace
,Clazz_inheritArgs
,Clazz_alert
,Clazz_defineMethod
,Clazz_overrideMethod
,Clazz_declareAnonymous
//,Clazz_checkPrivateMethod
,Clazz_cloneFinals
){
var $t$;
//var c$;
Clazz_declarePackage ("J.scriptext");
Clazz_load (["J.script.JmolMathExtension"], "J.scriptext.MathExt", ["java.lang.Float", "java.util.Date", "JU.AU", "$.BS", "$.Base64", "$.CU", "$.List", "$.M3", "$.M4", "$.P3", "$.P4", "$.PT", "$.Quat", "$.SB", "$.V3", "J.api.Interface", "J.atomdata.RadiusData", "J.c.VDW", "J.i18n.GT", "JM.BondSet", "J.script.SV", "$.ScriptParam", "$.T", "JW.BSUtil", "$.Escape", "$.JmolMolecule", "$.Logger", "$.Measure", "$.Parser", "$.Point3fi", "$.Txt"], function () {
c$ = Clazz_decorateAsClass (function () {
this.vwr = null;
this.e = null;
this.pm = null;
Clazz_instantialize (this, arguments);
}, J.scriptext, "MathExt", null, J.script.JmolMathExtension);
Clazz_makeConstructor (c$, 
function () {
});
Clazz_overrideMethod (c$, "init", 
function (se) {
this.e = se;
this.vwr = this.e.vwr;
return this;
}, "~O");
Clazz_overrideMethod (c$, "evaluate", 
function (mp, op, args, tok) {
switch (tok) {
case 135266826:
case 135266819:
case 135266821:
case 135266318:
case 135266820:
case 135266822:
return this.evaluateMath (mp, args, tok);
case 1276118017:
case 1276117504:
case 1276117507:
case 1276117508:
case 1276117511:
case 1276383749:
case 1276383249:
return this.evaluateList (mp, op.intValue, args);
case 135266306:
case 269484096:
return this.evaluateArray (mp, args, tok == 269484096);
case 135266307:
case 135270418:
return this.evaluateQuaternion (mp, args, tok);
case 1276118529:
return this.evaluateBin (mp, args);
case 135270423:
return this.evaluateCache (mp, args);
case 1276117514:
case 1276117515:
return this.evaluateRowCol (mp, args, tok);
case 1766856708:
return this.evaluateColor (mp, args);
case 135270405:
return this.evaluateCompare (mp, args);
case 135266310:
return this.evaluateConnected (mp, args);
case 135402505:
return this.evaluateContact (mp, args);
case 135267329:
return this.evaluateCross (mp, args);
case 135270408:
return this.evaluateData (mp, args);
case 1276118018:
case 1276117505:
if (op.tok == 269484241) return this.evaluateDot (mp, args, tok, op.intValue);
case 135266305:
case 1746538509:
return this.evaluateMeasure (mp, args, op.tok);
case 1229984263:
case 135271426:
return this.evaluateLoad (mp, args, tok == 1229984263);
case 1276118531:
return this.evaluateFind (mp, args);
case 1288701959:
case 1826248716:
return this.evaluateLabel (mp, op.intValue, args);
case 135368713:
return this.evaluateUserFunction (mp, op.value, args, op.intValue, op.tok == 269484241);
case 1276121098:
return this.evaluateGetProperty (mp, args, op.tok == 269484241);
case 137363467:
return this.evaluateHelix (mp, args);
case 135267841:
case 135266319:
case 135267842:
return this.evaluatePlane (mp, args, tok);
case 135287308:
case 135271429:
return this.evaluateScript (mp, args, tok);
case 1276117506:
case 1276117510:
case 1276117512:
return this.evaluateString (mp, op.intValue, args);
case 135266320:
return this.evaluatePoint (mp, args);
case 135304707:
return this.evaluatePrompt (mp, args);
case 135267332:
return this.evaluateRandom (mp, args);
case 1276118019:
return this.evaluateReplace (mp, args);
case 135267335:
case 135267336:
case 1238369286:
return this.evaluateSubstructure (mp, args, tok);
case 1276121113:
return this.evaluateModulation (mp, args);
case 1276117011:
case 1276117012:
return this.evaluateSort (mp, args, tok);
case 1297090050:
return this.evaluateSymop (mp, args, op.tok == 269484241);
case 1276117016:
return this.evaluateTensor (mp, args);
case 135266325:
return this.evaluateWithin (mp, args);
case 135270422:
return this.evaluateWrite (mp, args);
}
return false;
}, "J.script.ScriptMathProcessor,J.script.T,~A,~N");
Clazz_defineMethod (c$, "evaluateArray", 
 function (mp, args, allowMatrix) {
var len = args.length;
if (allowMatrix && (len == 4 || len == 3)) {
var isMatrix = true;
for (var i = 0; i < len && isMatrix; i++) isMatrix = (args[i].tok == 7 && args[i].getList ().size () == len);

if (isMatrix) {
var m =  Clazz_newFloatArray (len * len, 0);
var pt = 0;
for (var i = 0; i < len && isMatrix; i++) {
var list = args[i].getList ();
for (var j = 0; j < len; j++) {
var x = J.script.SV.fValue (list.get (j));
if (Float.isNaN (x)) {
isMatrix = false;
break;
}m[pt++] = x;
}
}
if (isMatrix) return (len == 3 ? mp.addXM3 (JU.M3.newA9 (m)) : mp.addXM4 (JU.M4.newA16 (m)));
}}var a =  new Array (args.length);
if (!allowMatrix && args.length == 1 && args[0].tok == 4 && args[0].asString ().startsWith (";base64,")) return mp.addXObj (J.script.SV.newV (15, JU.Base64.decodeBase64 (args[0].asString ())));
for (var i = a.length; --i >= 0; ) a[i] = J.script.SV.newT (args[i]);

return mp.addXAV (a);
}, "J.script.ScriptMathProcessor,~A,~B");
Clazz_defineMethod (c$, "evaluateBin", 
 function (mp, args) {
if (args.length != 3) return false;
var x1 = mp.getX ();
var isListf = (x1.tok == 13);
if (!isListf && x1.tok != 7) return mp.addX (x1);
var f0 = J.script.SV.fValue (args[0]);
var f1 = J.script.SV.fValue (args[1]);
var df = J.script.SV.fValue (args[2]);
var data;
if (isListf) {
data = x1.value;
} else {
var list = x1.getList ();
data =  Clazz_newFloatArray (list.size (), 0);
for (var i = list.size (); --i >= 0; ) data[i] = J.script.SV.fValue (list.get (i));

}var nbins = Math.max (Clazz_doubleToInt (Math.floor ((f1 - f0) / df + 0.01)), 1);
var array =  Clazz_newIntArray (nbins, 0);
var nPoints = data.length;
for (var i = 0; i < nPoints; i++) {
var v = data[i];
var bin = Clazz_doubleToInt (Math.floor ((v - f0) / df));
if (bin < 0) bin = 0;
 else if (bin >= nbins) bin = nbins - 1;
array[bin]++;
}
return mp.addXAI (array);
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateCache", 
 function (mp, args) {
if (args.length > 0) return false;
return mp.addXMap (this.vwr.cacheList ());
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateColor", 
 function (mp, args) {
var colorScheme = (args.length > 0 ? J.script.SV.sValue (args[0]) : "");
var isIsosurface = colorScheme.startsWith ("$");
if (args.length == 2 && colorScheme.equalsIgnoreCase ("TOHSL")) return mp.addXPt (JU.CU.rgbToHSL (JU.P3.newP (args[1].tok == 8 ? J.script.SV.ptValue (args[1]) : JU.CU.colorPtFromString (args[1].asString (),  new JU.P3 ())), true));
if (args.length == 2 && colorScheme.equalsIgnoreCase ("TORGB")) {
var pt = JU.P3.newP (args[1].tok == 8 ? J.script.SV.ptValue (args[1]) : JU.CU.colorPtFromString (args[1].asString (),  new JU.P3 ()));
return mp.addXPt (args[1].tok == 8 ? JU.CU.hslToRGB (pt) : pt);
}if (args.length == 4 && (args[3].tok == 1048589 || args[3].tok == 1048588)) {
var pt1 = JU.P3.newP (args[0].tok == 8 ? J.script.SV.ptValue (args[0]) : JU.CU.colorPtFromString (args[0].asString (),  new JU.P3 ()));
var pt2 = JU.P3.newP (args[1].tok == 8 ? J.script.SV.ptValue (args[1]) : JU.CU.colorPtFromString (args[1].asString (),  new JU.P3 ()));
var usingHSL = (args[3].tok == 1048589);
if (usingHSL) {
pt1 = JU.CU.rgbToHSL (pt1, false);
pt2 = JU.CU.rgbToHSL (pt2, false);
}var sb =  new JU.SB ();
var vd = JU.V3.newVsub (pt2, pt1);
var n = args[2].asInt ();
if (n < 2) n = 20;
vd.scale (1 / (n - 1));
for (var i = 0; i < n; i++) {
sb.append (JW.Escape.escapeColor (JU.CU.colorPtToFFRGB (usingHSL ? JU.CU.hslToRGB (pt1) : pt1)));
pt1.add (vd);
}
return mp.addXStr (sb.toString ());
}var ce = (isIsosurface ? null : this.vwr.getColorEncoder (colorScheme));
if (!isIsosurface && ce == null) return mp.addXStr ("");
var lo = (args.length > 1 ? J.script.SV.fValue (args[1]) : 3.4028235E38);
var hi = (args.length > 2 ? J.script.SV.fValue (args[2]) : 3.4028235E38);
var value = (args.length > 3 ? J.script.SV.fValue (args[3]) : 3.4028235E38);
var getValue = (value != 3.4028235E38 || lo != 3.4028235E38 && hi == 3.4028235E38);
var haveRange = (hi != 3.4028235E38);
if (!haveRange && colorScheme.length == 0) {
value = lo;
var range = this.vwr.getCurrentColorRange ();
lo = range[0];
hi = range[1];
}if (isIsosurface) {
var id = colorScheme.substring (1);
var data = [id, null];
if (!this.vwr.getShapePropertyData (24, "colorEncoder", data)) return mp.addXStr ("");
ce = data[1];
} else {
ce.setRange (lo, hi, lo > hi);
}var key = ce.getColorKey ();
if (getValue) return mp.addXPt (JU.CU.colorPtFromInt (ce.getArgb (hi == 3.4028235E38 ? lo : value)));
return mp.addX (J.script.SV.getVariableMap (key));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateCompare", 
 function (mp, args) {
if (args.length < 2 || args.length > 5) return false;
var stddev;
var sOpt = J.script.SV.sValue (args[args.length - 1]);
var isStdDev = sOpt.equalsIgnoreCase ("stddev");
var isIsomer = sOpt.equalsIgnoreCase ("ISOMER");
var isBonds = sOpt.equalsIgnoreCase ("BONDS");
var isSmiles = (!isIsomer && args.length > (isStdDev ? 3 : 2));
var bs1 = (args[0].tok == 10 ? args[0].value : null);
var bs2 = (args[1].tok == 10 ? args[1].value : null);
var smiles1 = (bs1 == null ? J.script.SV.sValue (args[0]) : "");
var smiles2 = (bs2 == null ? J.script.SV.sValue (args[1]) : "");
var m =  new JU.M4 ();
stddev = NaN;
var ptsA;
var ptsB;
if (isSmiles) {
if (bs1 == null || bs2 == null) return false;
}if (isBonds) {
if (args.length != 4) return false;
smiles1 = J.script.SV.sValue (args[2]);
isSmiles = smiles1.equalsIgnoreCase ("SMILES");
try {
if (isSmiles) smiles1 = this.vwr.getSmiles (bs1);
} catch (ex) {
if (Clazz_exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
var data = this.e.getSmilesExt ().getFlexFitList (bs1, bs2, smiles1, !isSmiles);
return (data == null ? mp.addXStr ("") : mp.addXAF (data));
}try {
if (isIsomer) {
if (args.length != 3) return false;
if (bs1 == null && bs2 == null) return mp.addXStr (this.vwr.getSmilesMatcher ().getRelationship (smiles1, smiles2).toUpperCase ());
var mf1 = (bs1 == null ? this.vwr.getSmilesMatcher ().getMolecularFormula (smiles1, false) : JW.JmolMolecule.getMolecularFormula (this.vwr.getModelSet ().at, bs1, false));
var mf2 = (bs2 == null ? this.vwr.getSmilesMatcher ().getMolecularFormula (smiles2, false) : JW.JmolMolecule.getMolecularFormula (this.vwr.getModelSet ().at, bs2, false));
if (!mf1.equals (mf2)) return mp.addXStr ("NONE");
if (bs1 != null) smiles1 = this.e.getSmilesExt ().getSmilesMatches ("", null, bs1, null, false, true);
var check;
if (bs2 == null) {
check = (this.vwr.getSmilesMatcher ().areEqual (smiles2, smiles1) > 0);
} else {
check = ((this.e.getSmilesExt ().getSmilesMatches (smiles1, null, bs2, null, false, true)).nextSetBit (0) >= 0);
}if (!check) {
var s = smiles1 + smiles2;
if (s.indexOf ("/") >= 0 || s.indexOf ("\\") >= 0 || s.indexOf ("@") >= 0) {
if (smiles1.indexOf ("@") >= 0 && (bs2 != null || smiles2.indexOf ("@") >= 0)) {
smiles1 = this.vwr.getSmilesMatcher ().reverseChirality (smiles1);
if (bs2 == null) {
check = (this.vwr.getSmilesMatcher ().areEqual (smiles1, smiles2) > 0);
} else {
check = ((this.e.getSmilesExt ().getSmilesMatches (smiles1, null, bs2, null, false, true)).nextSetBit (0) >= 0);
}if (check) return mp.addXStr ("ENANTIOMERS");
}if (bs2 == null) {
check = (this.vwr.getSmilesMatcher ().areEqual ("/nostereo/" + smiles2, smiles1) > 0);
} else {
var ret = this.e.getSmilesExt ().getSmilesMatches ("/nostereo/" + smiles1, null, bs2, null, false, true);
check = ((ret).nextSetBit (0) >= 0);
}if (check) return mp.addXStr ("DIASTERIOMERS");
}return mp.addXStr ("CONSTITUTIONAL ISOMERS");
}if (bs1 == null || bs2 == null) return mp.addXStr ("IDENTICAL");
stddev = this.e.getSmilesExt ().getSmilesCorrelation (bs1, bs2, smiles1, null, null, null, null, false, false, null, null, false, false);
return mp.addXStr (stddev < 0.2 ? "IDENTICAL" : "IDENTICAL or CONFORMATIONAL ISOMERS (RMSD=" + stddev + ")");
} else if (isSmiles) {
ptsA =  new JU.List ();
ptsB =  new JU.List ();
sOpt = J.script.SV.sValue (args[2]);
var isMap = sOpt.equalsIgnoreCase ("MAP");
isSmiles = (sOpt.equalsIgnoreCase ("SMILES"));
var isSearch = (isMap || sOpt.equalsIgnoreCase ("SMARTS"));
if (isSmiles || isSearch) sOpt = (args.length > 3 ? J.script.SV.sValue (args[3]) : null);
var hMaps = (("H".equals (sOpt) || "allH".equals (sOpt) || "bestH".equals (sOpt)));
var allMaps = (("all".equals (sOpt) || "allH".equals (sOpt)));
var bestMap = (("best".equals (sOpt) || "bestH".equals (sOpt)));
if (sOpt == null || hMaps || allMaps || bestMap) {
if (isMap || isSmiles) {
sOpt = "/noaromatic" + (allMaps || bestMap ? "/" : " nostereo/") + this.e.getSmilesExt ().getSmilesMatches ((hMaps ? "H" : ""), null, bs1, null, false, true);
} else {
return false;
}} else {
allMaps = true;
}stddev = this.e.getSmilesExt ().getSmilesCorrelation (bs1, bs2, sOpt, ptsA, ptsB, m, null, !isSmiles, isMap, null, null, !allMaps && !bestMap, bestMap);
if (isMap) {
var nAtoms = ptsA.size ();
if (nAtoms == 0) return mp.addXStr ("");
var nMatch = Clazz_doubleToInt (ptsB.size () / nAtoms);
var ret =  new JU.List ();
for (var i = 0, pt = 0; i < nMatch; i++) {
var a = JU.AU.newInt2 (nAtoms);
ret.addLast (a);
for (var j = 0; j < nAtoms; j++, pt++) a[j] = [(ptsA.get (j)).i, (ptsB.get (pt)).i];

}
if (!allMaps) return (ret.size () > 0 ? mp.addXAII (ret.get (0)) : mp.addXStr (""));
return mp.addXList (ret);
}} else {
ptsA = this.e.getPointVector (args[0], 0);
ptsB = this.e.getPointVector (args[1], 0);
if (ptsA != null && ptsB != null) stddev = JW.Measure.getTransformMatrix4 (ptsA, ptsB, m, null, false);
}return (isStdDev || Float.isNaN (stddev) ? mp.addXFloat (stddev) : mp.addXM4 (m));
} catch (ex) {
if (Clazz_exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage () == null ? ex.toString () : ex.getMessage (), null);
return false;
} else {
throw ex;
}
}
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateConnected", 
 function (mp, args) {
if (args.length > 5) return false;
var min = -2147483648;
var max = 2147483647;
var fmin = 0;
var fmax = 3.4028235E38;
var order = 65535;
var atoms1 = null;
var atoms2 = null;
var haveDecimal = false;
var isBonds = false;
for (var i = 0; i < args.length; i++) {
var $var = args[i];
switch ($var.tok) {
case 10:
isBonds = (Clazz_instanceOf ($var.value, JM.BondSet));
if (isBonds && atoms1 != null) return false;
if (atoms1 == null) atoms1 = J.script.SV.bsSelectVar ($var);
 else if (atoms2 == null) atoms2 = J.script.SV.bsSelectVar ($var);
 else return false;
break;
case 4:
var type = J.script.SV.sValue ($var);
if (type.equalsIgnoreCase ("hbond")) order = 30720;
 else order = J.script.ScriptParam.getBondOrderFromString (type);
if (order == 131071) return false;
break;
case 3:
haveDecimal = true;
default:
var n = $var.asInt ();
var f = $var.asFloat ();
if (max != 2147483647) return false;
if (min == -2147483648) {
min = Math.max (n, 0);
fmin = f;
} else {
max = n;
fmax = f;
}}
}
if (min == -2147483648) {
min = 1;
max = 100;
fmin = 0.1;
fmax = 1.0E8;
} else if (max == 2147483647) {
max = min;
fmax = fmin;
fmin = 0.1;
}if (atoms1 == null) atoms1 = this.vwr.getAllAtoms ();
if (haveDecimal && atoms2 == null) atoms2 = atoms1;
if (atoms2 != null) {
var bsBonds =  new JU.BS ();
this.vwr.makeConnections (fmin, fmax, order, 1087373321, atoms1, atoms2, bsBonds, isBonds, false, 0);
return mp.addX (J.script.SV.newV (10,  new JM.BondSet (bsBonds, this.vwr.getAtomIndices (this.vwr.getAtomBits (1678770178, bsBonds)))));
}return mp.addXBs (this.vwr.ms.getAtomsConnected (min, max, order, atoms1));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateContact", 
 function (mp, args) {
if (args.length < 1 || args.length > 3) return false;
var i = 0;
var distance = 100;
var tok = args[0].tok;
switch (tok) {
case 3:
case 2:
distance = J.script.SV.fValue (args[i++]);
break;
case 10:
break;
default:
return false;
}
if (i == args.length || !(Clazz_instanceOf (args[i].value, JU.BS))) return false;
var bsA = JW.BSUtil.copy (J.script.SV.bsSelectVar (args[i++]));
var bsB = (i < args.length ? JW.BSUtil.copy (J.script.SV.bsSelectVar (args[i])) : null);
var rd =  new J.atomdata.RadiusData (null, (distance > 10 ? distance / 100 : distance), (distance > 10 ? J.atomdata.RadiusData.EnumType.FACTOR : J.atomdata.RadiusData.EnumType.OFFSET), J.c.VDW.AUTO);
bsB = this.setContactBitSets (bsA, bsB, true, NaN, rd, false);
bsB.or (bsA);
return mp.addXBs (bsB);
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateCross", 
 function (mp, args) {
if (args.length != 2) return false;
var x1 = args[0];
var x2 = args[1];
if (x1.tok != 8 || x2.tok != 8) return false;
var a = JU.V3.newV (x1.value);
var b = JU.V3.newV (x2.value);
a.cross (a, b);
return mp.addXPt (JU.P3.newP (a));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateData", 
 function (mp, args) {
if (args.length != 1 && args.length != 2 && args.length != 4) return false;
var selected = J.script.SV.sValue (args[0]);
var type = (args.length == 2 ? J.script.SV.sValue (args[1]) : "");
if (args.length == 4) {
var iField = args[1].asInt ();
var nBytes = args[2].asInt ();
var firstLine = args[3].asInt ();
var f = JW.Parser.parseFloatArrayFromMatchAndField (selected, null, 0, 0, null, iField, nBytes, null, firstLine);
return mp.addXStr (JW.Escape.escapeFloatA (f, false));
}if (selected.indexOf ("data2d_") == 0) {
var f1 = this.vwr.getDataFloat2D (selected);
if (f1 == null) return mp.addXStr ("");
if (args.length == 2 && args[1].tok == 2) {
var pt = args[1].intValue;
if (pt < 0) pt += f1.length;
if (pt >= 0 && pt < f1.length) return mp.addXStr (JW.Escape.escapeFloatA (f1[pt], false));
return mp.addXStr ("");
}return mp.addXStr (JW.Escape.escapeFloatAA (f1, false));
}if (selected.indexOf ("property_") == 0) {
var f1 = this.vwr.getDataFloat (selected);
if (f1 == null) return mp.addXStr ("");
var f2 = (type.indexOf ("property_") == 0 ? this.vwr.getDataFloat (type) : null);
if (f2 != null) {
f1 = JU.AU.arrayCopyF (f1, -1);
for (var i = Math.min (f1.length, f2.length); --i >= 0; ) f1[i] += f2[i];

}return mp.addXStr (JW.Escape.escapeFloatA (f1, false));
}if (args.length == 1) {
var data = this.vwr.getData (selected);
return mp.addXStr (data == null ? "" : "" + data[1]);
}return mp.addXStr (this.vwr.getData (selected, type));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateDot", 
 function (mp, args, tok, intValue) {
switch (args.length) {
case 1:
if (tok == 1276117505) return false;
case 2:
break;
default:
return false;
}
var x1 = mp.getX ();
var x2 = args[0];
var pt2 = (x2.tok == 7 ? null : mp.ptValue (x2, false));
var plane2 = mp.planeValue (x2);
if (tok == 1276118018) {
var minMax = intValue & 480;
var isMinMax = (minMax == 32 || minMax == 64);
var isAll = minMax == 480;
switch (x1.tok) {
case 10:
var bs = J.script.SV.bsSelectVar (x1);
var bs2 = null;
var returnAtom = (isMinMax && args.length == 2 && args[1].asBoolean ());
switch (x2.tok) {
case 10:
bs2 = (x2.tok == 10 ? J.script.SV.bsSelectVar (x2) : null);
case 8:
var atoms = this.vwr.ms.at;
if (returnAtom) {
var dMinMax = NaN;
var iMinMax = 2147483647;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var d = (bs2 == null ? atoms[i].distanceSquared (pt2) : (this.e.getBitsetProperty (bs2, intValue, atoms[i], plane2, x1.value, null, false, x1.index, false)).floatValue ());
if (minMax == 32 ? d >= dMinMax : d <= dMinMax) continue;
dMinMax = d;
iMinMax = i;
}
return mp.addXBs (iMinMax == 2147483647 ?  new JU.BS () : JW.BSUtil.newAndSetBit (iMinMax));
}if (isAll) {
if (bs2 == null) {
var data =  Clazz_newFloatArray (bs.cardinality (), 0);
for (var p = 0, i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1), p++) data[p] = atoms[i].distance (pt2);

return mp.addXAF (data);
}var data2 =  Clazz_newFloatArray (bs.cardinality (), bs2.cardinality (), 0);
for (var p = 0, i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1), p++) for (var q = 0, j = bs2.nextSetBit (0); j >= 0; j = bs2.nextSetBit (j + 1), q++) data2[p][q] = atoms[i].distance (atoms[j]);


return mp.addXAFF (data2);
}if (isMinMax) {
var data =  Clazz_newFloatArray (bs.cardinality (), 0);
for (var i = bs.nextSetBit (0), p = 0; i >= 0; i = bs.nextSetBit (i + 1)) data[p++] = (this.e.getBitsetProperty (bs2, intValue, atoms[i], plane2, x1.value, null, false, x1.index, false)).floatValue ();

return mp.addXAF (data);
}return mp.addXObj (this.e.getBitsetProperty (bs, intValue, pt2, plane2, x1.value, null, false, x1.index, false));
}
}
}return mp.addXFloat (this.getDistance (mp, x1, x2, tok));
}, "J.script.ScriptMathProcessor,~A,~N,~N");
Clazz_defineMethod (c$, "evaluateHelix", 
 function (mp, args) {
if (args.length < 1 || args.length > 5) return false;
var pt = (args.length > 2 ? 3 : 1);
var type = (pt >= args.length ? "array" : J.script.SV.sValue (args[pt]));
var tok = J.script.T.getTokFromName (type);
if (args.length > 2) {
var pta = mp.ptValue (args[0], true);
var ptb = mp.ptValue (args[1], true);
if (args[2].tok != 9) return false;
var dq = JU.Quat.newP4 (args[2].value);
switch (tok) {
case 0:
break;
case 135266320:
case 1073741854:
case 1666189314:
case 135266305:
case 1746538509:
return mp.addXObj (JW.Measure.computeHelicalAxis (null, tok, pta, ptb, dq));
case 135266306:
var data = JW.Measure.computeHelicalAxis (null, 1073742001, pta, ptb, dq);
if (data == null) return false;
return mp.addXAS (data);
default:
return mp.addXObj (JW.Measure.computeHelicalAxis (type, 135176, pta, ptb, dq));
}
} else {
var bs = (Clazz_instanceOf (args[0].value, JU.BS) ? args[0].value : this.vwr.getAtomBits (1095761939,  new Integer (args[0].asInt ())));
switch (tok) {
case 135266320:
return mp.addXObj (this.vwr.getHelixData (bs, 135266320));
case 1073741854:
return mp.addXObj (this.vwr.getHelixData (bs, 1073741854));
case 1666189314:
return mp.addXObj (this.vwr.getHelixData (bs, 1666189314));
case 135266305:
return mp.addXFloat ((this.vwr.getHelixData (bs, 135266305)).floatValue ());
case 135176:
case 1746538509:
return mp.addXObj (this.vwr.getHelixData (bs, tok));
case 135266306:
var data = this.vwr.getHelixData (bs, 1073742001);
if (data == null) return false;
return mp.addXAS (data);
}
}return false;
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateFind", 
 function (mp, args) {
if (args.length == 0) return false;
var x1 = mp.getX ();
var sFind = J.script.SV.sValue (args[0]);
var flags = (args.length > 1 && args[1].tok != 1048589 && args[1].tok != 1048588 ? J.script.SV.sValue (args[1]) : "");
var isSequence = sFind.equalsIgnoreCase ("SEQUENCE");
var isSmiles = sFind.equalsIgnoreCase ("SMILES");
var isSearch = sFind.equalsIgnoreCase ("SMARTS");
var isChemical = sFind.equalsIgnoreCase ("CHEMICAL");
var isMF = sFind.equalsIgnoreCase ("MF");
try {
if (isChemical) {
var data = (x1.tok == 10 ? this.vwr.getSmiles (J.script.SV.getBitSet (x1, false)) : J.script.SV.sValue (x1));
data = data.length == 0 ? "" : this.vwr.getChemicalInfo (data, args.length > 1 ? J.script.T.getTokenFromName (flags.toLowerCase ()) : null);
if (data.endsWith ("\n")) data = data.substring (0, data.length - 1);
if (data.startsWith ("InChI")) data = JU.PT.rep (JU.PT.rep (data, "InChI=", ""), "InChIKey=", "");
return mp.addXStr (data);
}if (isSmiles || isSearch || x1.tok == 10) {
var iPt = (isSmiles || isSearch ? 2 : 1);
var bs2 = (iPt < args.length && args[iPt].tok == 10 ? args[iPt++].value : null);
var asBonds = ("bonds".equalsIgnoreCase (J.script.SV.sValue (args[args.length - 1])));
var isAll = (asBonds || args[args.length - 1].tok == 1048589);
var ret = null;
switch (x1.tok) {
case 4:
var smiles = J.script.SV.sValue (x1);
if (bs2 != null) return false;
if (flags.equalsIgnoreCase ("mf")) {
ret = this.vwr.getSmilesMatcher ().getMolecularFormula (smiles, isSearch);
} else {
ret = this.e.getSmilesExt ().getSmilesMatches (flags, smiles, null, null, isSearch, !isAll);
}break;
case 10:
if (isMF) return mp.addXStr (JW.JmolMolecule.getMolecularFormula (this.vwr.getModelSet ().at, x1.value, false));
if (isSequence) return mp.addXStr (this.vwr.getSmilesOpt (x1.value, -1, -1, false, true, isAll, isAll, false));
if (isSmiles || isSearch) sFind = flags;
var bsMatch3D = bs2;
if (asBonds) {
var map = this.vwr.getSmilesMatcher ().getCorrelationMaps (sFind, this.vwr.ms.at, this.vwr.getAtomCount (), x1.value, !isSmiles, true);
ret = (map.length > 0 ? this.vwr.getDihedralMap (map[0]) :  Clazz_newIntArray (0, 0));
} else {
ret = this.e.getSmilesExt ().getSmilesMatches (sFind, null, x1.value, bsMatch3D, !isSmiles, !isAll);
}break;
}
if (ret == null) this.e.invArg ();
return mp.addXObj (ret);
}} catch (ex) {
if (Clazz_exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
var isReverse = (flags.indexOf ("v") >= 0);
var isCaseInsensitive = (flags.indexOf ("i") >= 0);
var asMatch = (flags.indexOf ("m") >= 0);
var isList = (x1.tok == 7);
var isPattern = (args.length == 2);
if (isList || isPattern) {
var pm = this.getPatternMatcher ();
var pattern = null;
try {
pattern = pm.compile (sFind, isCaseInsensitive);
} catch (ex) {
if (Clazz_exceptionOf (ex, Exception)) {
this.e.evalError (ex.toString (), null);
} else {
throw ex;
}
}
var list = J.script.SV.listValue (x1);
if (JW.Logger.debugging) JW.Logger.debug ("finding " + sFind);
var bs =  new JU.BS ();
var ipt = 0;
var n = 0;
var matcher = null;
var v = (asMatch ?  new JU.List () : null);
for (var i = 0; i < list.length; i++) {
var what = list[i];
matcher = pattern.matcher (what);
var isMatch = matcher.find ();
if (asMatch && isMatch || !asMatch && isMatch == !isReverse) {
n++;
ipt = i;
bs.set (i);
if (asMatch) v.addLast (isReverse ? what.substring (0, matcher.start ()) + what.substring (matcher.end ()) : matcher.group ());
}}
if (!isList) {
return (asMatch ? mp.addXStr (v.size () == 1 ? v.get (0) : "") : isReverse ? mp.addXBool (n == 1) : asMatch ? mp.addXStr (n == 0 ? "" : matcher.group ()) : mp.addXInt (n == 0 ? 0 : matcher.start () + 1));
}if (n == 1) return mp.addXStr (asMatch ? v.get (0) : list[ipt]);
var listNew =  new Array (n);
if (n > 0) for (var i = list.length; --i >= 0; ) if (bs.get (i)) {
--n;
listNew[n] = (asMatch ? v.get (n) : list[i]);
}
return mp.addXAS (listNew);
}return mp.addXInt (J.script.SV.sValue (x1).indexOf (sFind) + 1);
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateGetProperty", 
 function (mp, args, isAtomProperty) {
var pt = 0;
var propertyName = (args.length > pt ? J.script.SV.sValue (args[pt++]).toLowerCase () : "");
var isJSON = false;
if (propertyName.equals ("json") && args.length > pt) {
isJSON = true;
propertyName = J.script.SV.sValue (args[pt++]);
}if (propertyName.startsWith ("$")) {
}if (isAtomProperty && !propertyName.equalsIgnoreCase ("bondInfo")) propertyName = "atomInfo." + propertyName;
var propertyValue = "";
if (propertyName.equalsIgnoreCase ("fileContents") && args.length > 2) {
var s = J.script.SV.sValue (args[1]);
for (var i = 2; i < args.length; i++) s += "|" + J.script.SV.sValue (args[i]);

propertyValue = s;
pt = args.length;
} else if (args.length > pt) {
switch (args[pt].tok) {
case 10:
propertyValue = J.script.SV.bsSelectVar (args[pt++]);
if (propertyName.equalsIgnoreCase ("bondInfo") && args.length > pt && args[pt].tok == 10) propertyValue = [propertyValue, J.script.SV.bsSelectVar (args[pt])];
break;
case 4:
if (this.vwr.checkPropertyParameter (propertyName)) propertyValue = args[pt++].value;
break;
}
}if (isAtomProperty) {
var x = mp.getX ();
if (x.tok != 10) return false;
var iAtom = J.script.SV.bsSelectVar (x).nextSetBit (0);
if (iAtom < 0) return mp.addXStr ("");
propertyValue = JW.BSUtil.newAndSetBit (iAtom);
}var property = this.vwr.getProperty (null, propertyName, propertyValue);
if (pt < args.length) property = this.vwr.extractProperty (property, args, pt);
if (isAtomProperty && Clazz_instanceOf (property, JU.List)) property = ((property).size () > 0 ? (property).get (0) : "");
return mp.addXObj (isJSON ? "{" + JU.PT.toJSON ("value", property) + "}" : J.script.SV.isVariableType (property) ? property : JW.Escape.toReadable (propertyName, property));
}, "J.script.ScriptMathProcessor,~A,~B");
Clazz_defineMethod (c$, "evaluateLabel", 
 function (mp, intValue, args) {
var x1 = (args.length < 2 ? mp.getX () : null);
var format = (args.length == 0 ? "%U" : J.script.SV.sValue (args[0]));
var asArray = J.script.T.tokAttr (intValue, 480);
if (x1 == null) {
if (args.length < 2 || args[1].tok != 7) return mp.addXStr (J.script.SV.sprintfArray (args));
var a = args[1].getList ();
var args2 = [args[0], null];
var sa =  new Array (a.size ());
for (var i = sa.length; --i >= 0; ) {
args2[1] = a.get (i);
sa[i] = J.script.SV.sprintfArray (args2);
}
return mp.addXAS (sa);
}var bs = J.script.SV.getBitSet (x1, true);
if (bs == null) return mp.addXObj (J.script.SV.sprintf (JW.Txt.formatCheck (format), x1));
return mp.addXObj (this.e.getCmdExt ().getBitsetIdent (bs, format, x1.value, true, x1.index, asArray));
}, "J.script.ScriptMathProcessor,~N,~A");
Clazz_defineMethod (c$, "evaluateList", 
 function (mp, tok, args) {
var len = args.length;
var x1 = mp.getX ();
var x2;
switch (tok) {
case 1276383749:
return (len == 1 && mp.addX (x1.pushPop (args[0])));
case 1276383249:
return (len == 0 && mp.addX (x1.pushPop (null)));
case 1276118017:
if (len != 1 && len != 2) return false;
break;
default:
if (len != 1) return false;
}
var sList1 = null;
var sList2 = null;
var sList3 = null;
if (len == 2) {
var itab = (args[0].tok == 4 ? 0 : 1);
var tab = J.script.SV.sValue (args[itab]);
sList1 = (x1.tok == 7 ? J.script.SV.listValue (x1) : JU.PT.split (J.script.SV.sValue (x1), "\n"));
x2 = args[1 - itab];
sList2 = (x2.tok == 7 ? J.script.SV.listValue (x2) : JU.PT.split (J.script.SV.sValue (x2), "\n"));
sList3 =  new Array (len = Math.max (sList1.length, sList2.length));
for (var i = 0; i < len; i++) sList3[i] = (i >= sList1.length ? "" : sList1[i]) + tab + (i >= sList2.length ? "" : sList2[i]);

return mp.addXAS (sList3);
}x2 = (len == 0 ? J.script.SV.newV (1048579, "all") : args[0]);
var isAll = (x2.tok == 1048579);
if (x1.tok != 7 && x1.tok != 4) return mp.binaryOp (this.opTokenFor (tok), x1, x2);
var isScalar1 = J.script.SV.isScalar (x1);
var isScalar2 = J.script.SV.isScalar (x2);
var list1 = null;
var list2 = null;
var alist1 = x1.getList ();
var alist2 = x2.getList ();
if (x1.tok == 7) {
len = alist1.size ();
} else if (isScalar1) {
len = 2147483647;
} else {
sList1 = (JU.PT.split (J.script.SV.sValue (x1), "\n"));
list1 =  Clazz_newFloatArray (len = sList1.length, 0);
JU.PT.parseFloatArrayData (sList1, list1);
}if (isAll) {
var sum = 0;
if (x1.tok == 7) {
for (var i = len; --i >= 0; ) sum += J.script.SV.fValue (alist1.get (i));

} else if (!isScalar1) {
for (var i = len; --i >= 0; ) sum += list1[i];

}return mp.addXFloat (sum);
}if (tok == 1276117506 && x2.tok == 4) {
var sb =  new JU.SB ();
if (isScalar1) sb.append (J.script.SV.sValue (x1));
 else for (var i = 0; i < len; i++) sb.appendO (i > 0 ? x2.value : null).append (J.script.SV.sValue (alist1.get (i)));

return mp.addXStr (sb.toString ());
}var scalar = null;
if (isScalar2) {
scalar = x2;
} else if (x2.tok == 7) {
len = Math.min (len, alist2.size ());
} else {
sList2 = JU.PT.split (J.script.SV.sValue (x2), "\n");
list2 =  Clazz_newFloatArray (sList2.length, 0);
JU.PT.parseFloatArrayData (sList2, list2);
len = Math.min (len, list2.length);
}var token = this.opTokenFor (tok);
var olist =  new Array (len);
var a = (isScalar1 ? x1 : null);
var b;
for (var i = 0; i < len; i++) {
if (isScalar2) b = scalar;
 else if (x2.tok == 7) b = alist2.get (i);
 else if (Float.isNaN (list2[i])) b = J.script.SV.getVariable (J.script.SV.unescapePointOrBitsetAsVariable (sList2[i]));
 else b = J.script.SV.newV (3, Float.$valueOf (list2[i]));
if (!isScalar1) {
if (x1.tok == 7) a = alist1.get (i);
 else if (Float.isNaN (list1[i])) a = J.script.SV.getVariable (J.script.SV.unescapePointOrBitsetAsVariable (sList1[i]));
 else a = J.script.SV.newV (3, Float.$valueOf (list1[i]));
}if (tok == 1276117506) {
if (a.tok != 7) {
var l =  new JU.List ();
l.addLast (a);
a = J.script.SV.getVariableList (l);
}}if (!mp.binaryOp (token, a, b)) return false;
olist[i] = mp.getX ();
}
return mp.addXAV (olist);
}, "J.script.ScriptMathProcessor,~N,~A");
Clazz_defineMethod (c$, "evaluateLoad", 
 function (mp, args, isFile) {
if (args.length > 2 || args.length < 1) return false;
var file = J.script.SV.sValue (args[0]);
file = file.$replace ('\\', '/');
var nBytesMax = (args.length == 2 ? args[1].asInt () : -1);
var asBytes = (args.length == 2 && args[1].tok == 1048589);
if (asBytes) return mp.addXMap (this.vwr.getFileAsMap (file));
if (this.vwr.isJS && file.startsWith ("?")) {
if (isFile) return mp.addXStr ("");
file = this.e.loadFileAsync ("load()_", file, mp.oPt, true);
}return mp.addXStr (isFile ? this.vwr.getFilePath (file, false) : this.vwr.getFileAsString4 (file, nBytesMax, false, false, true));
}, "J.script.ScriptMathProcessor,~A,~B");
Clazz_defineMethod (c$, "evaluateMath", 
 function (mp, args, tok) {
if (tok == 135266318) {
if (args.length == 1 && args[0].tok == 4) return mp.addXStr (( new java.util.Date ()) + "\t" + J.script.SV.sValue (args[0]));
return mp.addXInt ((System.currentTimeMillis () & 0x7FFFFFFF) - (args.length == 0 ? 0 : args[0].asInt ()));
}if (args.length != 1) return false;
if (tok == 135266826) {
if (args[0].tok == 2) return mp.addXInt (Math.abs (args[0].asInt ()));
return mp.addXFloat (Math.abs (args[0].asFloat ()));
}var x = J.script.SV.fValue (args[0]);
switch (tok) {
case 135266819:
return mp.addXFloat ((Math.acos (x) * 180 / 3.141592653589793));
case 135266821:
return mp.addXFloat (Math.cos (x * 3.141592653589793 / 180));
case 135266820:
return mp.addXFloat (Math.sin (x * 3.141592653589793 / 180));
case 135266822:
return mp.addXFloat (Math.sqrt (x));
}
return false;
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateMeasure", 
 function (mp, args, tok) {
var nPoints = 0;
switch (tok) {
case 1746538509:
var points =  new JU.List ();
var rangeMinMax = [3.4028235E38, 3.4028235E38];
var strFormat = null;
var units = null;
var isAllConnected = false;
var isNotConnected = false;
var rPt = 0;
var isNull = false;
var rd = null;
var nBitSets = 0;
var vdw = 3.4028235E38;
var asMinArray = false;
var asArray = false;
for (var i = 0; i < args.length; i++) {
switch (args[i].tok) {
case 10:
var bs = args[i].value;
if (bs.length () == 0) isNull = true;
points.addLast (bs);
nPoints++;
nBitSets++;
break;
case 8:
var v =  new JW.Point3fi ();
v.setT (args[i].value);
points.addLast (v);
nPoints++;
break;
case 2:
case 3:
rangeMinMax[rPt++ % 2] = J.script.SV.fValue (args[i]);
break;
case 4:
var s = J.script.SV.sValue (args[i]);
if (s.equalsIgnoreCase ("vdw") || s.equalsIgnoreCase ("vanderwaals")) vdw = (i + 1 < args.length && args[i + 1].tok == 2 ? args[++i].asInt () : 100) / 100;
 else if (s.equalsIgnoreCase ("notConnected")) isNotConnected = true;
 else if (s.equalsIgnoreCase ("connected")) isAllConnected = true;
 else if (s.equalsIgnoreCase ("minArray")) asMinArray = (nBitSets >= 1);
 else if (s.equalsIgnoreCase ("asArray")) asArray = (nBitSets >= 1);
 else if (JU.PT.isOneOf (s.toLowerCase (), ";nm;nanometers;pm;picometers;angstroms;ang;au;") || s.endsWith ("hz")) units = s.toLowerCase ();
 else strFormat = nPoints + ":" + s;
break;
default:
return false;
}
}
if (nPoints < 2 || nPoints > 4 || rPt > 2 || isNotConnected && isAllConnected) return false;
if (isNull) return mp.addXStr ("");
if (vdw != 3.4028235E38 && (nBitSets != 2 || nPoints != 2)) return mp.addXStr ("");
rd = (vdw == 3.4028235E38 ?  new J.atomdata.RadiusData (rangeMinMax, 0, null, null) :  new J.atomdata.RadiusData (null, vdw, J.atomdata.RadiusData.EnumType.FACTOR, J.c.VDW.AUTO));
return mp.addXObj ((this.vwr.newMeasurementData (null, points)).set (0, null, rd, strFormat, units, null, isAllConnected, isNotConnected, null, true, 0, 0, null).getMeasurements (asArray, asMinArray));
case 135266305:
if ((nPoints = args.length) != 3 && nPoints != 4) return false;
break;
default:
if ((nPoints = args.length) != 2) return false;
}
var pts =  new Array (nPoints);
for (var i = 0; i < nPoints; i++) pts[i] = mp.ptValue (args[i], true);

switch (nPoints) {
case 2:
return mp.addXFloat (pts[0].distance (pts[1]));
case 3:
return mp.addXFloat (JW.Measure.computeAngleABC (pts[0], pts[1], pts[2], true));
case 4:
return mp.addXFloat (JW.Measure.computeTorsion (pts[0], pts[1], pts[2], pts[3], true));
}
return false;
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateModulation", 
 function (mp, args) {
var type = "D";
var t = NaN;
var t456 = null;
var pt = -1;
switch (args.length) {
case 0:
break;
case 1:
pt = 0;
break;
case 2:
type = J.script.SV.sValue (args[0]).toUpperCase ();
t = J.script.SV.fValue (args[1]);
break;
default:
return false;
}
if (pt >= 0) {
if (args[pt].tok == 8) t456 = args[pt].value;
 else t = J.script.SV.fValue (args[pt]);
}if (t456 == null && t < 1e6) t456 = JU.P3.new3 (t, t, t);
var bs = J.script.SV.getBitSet (mp.getX (), false);
return mp.addXList (this.vwr.getModulationList (bs, type, t456));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluatePlane", 
 function (mp, args, tok) {
if (tok == 135267841 && args.length != 3 || tok == 135267842 && args.length != 2 && args.length != 3 || args.length == 0 || args.length > 4) return false;
var pt1;
var pt2;
var pt3;
var plane;
var norm;
var vTemp;
switch (args.length) {
case 1:
if (args[0].tok == 10) {
var bs = J.script.SV.getBitSet (args[0], false);
if (bs.cardinality () == 3) {
var pts = this.vwr.getAtomPointVector (bs);
var vNorm =  new JU.V3 ();
var vAB =  new JU.V3 ();
var vAC =  new JU.V3 ();
plane =  new JU.P4 ();
JW.Measure.getPlaneThroughPoints (pts.get (0), pts.get (1), pts.get (2), vNorm, vAB, vAC, plane);
return mp.addXPt4 (plane);
}}var pt = JW.Escape.uP (J.script.SV.sValue (args[0]));
if (Clazz_instanceOf (pt, JU.P4)) return mp.addXPt4 (pt);
return mp.addXStr ("" + pt);
case 2:
if (tok == 135267842) {
if (args[1].tok != 9) return false;
pt3 =  new JU.P3 ();
norm =  new JU.V3 ();
vTemp =  new JU.V3 ();
plane = args[1].value;
if (args[0].tok == 9) {
var list = JW.Measure.getIntersectionPP (args[0].value, plane);
if (list == null) return mp.addXStr ("");
return mp.addXList (list);
}pt2 = mp.ptValue (args[0], false);
if (pt2 == null) return mp.addXStr ("");
return mp.addXPt (JW.Measure.getIntersection (pt2, null, plane, pt3, norm, vTemp));
}case 3:
case 4:
switch (tok) {
case 135267841:
return mp.addXPt4 (this.e.getHklPlane (JU.P3.new3 (J.script.SV.fValue (args[0]), J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2]))));
case 135267842:
pt1 = mp.ptValue (args[0], false);
pt2 = mp.ptValue (args[1], false);
if (pt1 == null || pt2 == null) return mp.addXStr ("");
var vLine = JU.V3.newV (pt2);
vLine.normalize ();
if (args[2].tok == 9) {
pt3 =  new JU.P3 ();
norm =  new JU.V3 ();
vTemp =  new JU.V3 ();
pt1 = JW.Measure.getIntersection (pt1, vLine, args[2].value, pt3, norm, vTemp);
if (pt1 == null) return mp.addXStr ("");
return mp.addXPt (pt1);
}pt3 = mp.ptValue (args[2], false);
if (pt3 == null) return mp.addXStr ("");
var v =  new JU.V3 ();
JW.Measure.projectOntoAxis (pt3, pt1, vLine, v);
return mp.addXPt (pt3);
}
switch (args[0].tok) {
case 2:
case 3:
if (args.length == 3) {
var r = J.script.SV.fValue (args[0]);
var theta = J.script.SV.fValue (args[1]);
var phi = J.script.SV.fValue (args[2]);
norm = JU.V3.new3 (0, 0, 1);
pt2 = JU.P3.new3 (0, 1, 0);
var q = JU.Quat.newVA (pt2, phi);
q.getMatrix ().rotate (norm);
pt2.set (0, 0, 1);
q = JU.Quat.newVA (pt2, theta);
q.getMatrix ().rotate (norm);
pt2.setT (norm);
pt2.scale (r);
plane =  new JU.P4 ();
JW.Measure.getPlaneThroughPoint (pt2, norm, plane);
return mp.addXPt4 (plane);
}break;
case 10:
case 8:
pt1 = mp.ptValue (args[0], false);
pt2 = mp.ptValue (args[1], false);
if (pt2 == null) return false;
pt3 = (args.length > 2 && (args[2].tok == 10 || args[2].tok == 8) ? mp.ptValue (args[2], false) : null);
norm = JU.V3.newV (pt2);
if (pt3 == null) {
plane =  new JU.P4 ();
if (args.length == 2 || !args[2].asBoolean ()) {
pt3 = JU.P3.newP (pt1);
pt3.add (pt2);
pt3.scale (0.5);
norm.sub (pt1);
norm.normalize ();
} else {
pt3 = pt1;
}JW.Measure.getPlaneThroughPoint (pt3, norm, plane);
return mp.addXPt4 (plane);
}var vAB =  new JU.V3 ();
var vAC =  new JU.V3 ();
var nd = JW.Measure.getDirectedNormalThroughPoints (pt1, pt2, pt3, (args.length == 4 ? mp.ptValue (args[3], true) : null), norm, vAB, vAC);
return mp.addXPt4 (JU.P4.new4 (norm.x, norm.y, norm.z, nd));
}
}
if (args.length != 4) return false;
var x = J.script.SV.fValue (args[0]);
var y = J.script.SV.fValue (args[1]);
var z = J.script.SV.fValue (args[2]);
var w = J.script.SV.fValue (args[3]);
return mp.addXPt4 (JU.P4.new4 (x, y, z, w));
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluatePoint", 
 function (mp, args) {
if (args.length != 1 && args.length != 3 && args.length != 4) return false;
switch (args.length) {
case 1:
if (args[0].tok == 3 || args[0].tok == 2) return mp.addXInt (args[0].asInt ());
var s = J.script.SV.sValue (args[0]);
if (args[0].tok == 7) s = "{" + s + "}";
var pt = JW.Escape.uP (s);
if (Clazz_instanceOf (pt, JU.P3)) return mp.addXPt (pt);
return mp.addXStr ("" + pt);
case 3:
return mp.addXPt (JU.P3.new3 (args[0].asFloat (), args[1].asFloat (), args[2].asFloat ()));
case 4:
return mp.addXPt4 (JU.P4.new4 (args[0].asFloat (), args[1].asFloat (), args[2].asFloat (), args[3].asFloat ()));
}
return false;
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluatePrompt", 
 function (mp, args) {
if (args.length != 1 && args.length != 2 && args.length != 3) return false;
var label = J.script.SV.sValue (args[0]);
var buttonArray = (args.length > 1 && args[1].tok == 7 ? J.script.SV.listValue (args[1]) : null);
var asButtons = (buttonArray != null || args.length == 1 || args.length == 3 && args[2].asBoolean ());
var input = (buttonArray != null ? null : args.length >= 2 ? J.script.SV.sValue (args[1]) : "OK");
var s = "" + this.vwr.prompt (label, input, buttonArray, asButtons);
return (asButtons && buttonArray != null ? mp.addXInt (Integer.parseInt (s) + 1) : mp.addXStr (s));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateQuaternion", 
 function (mp, args, tok) {
var pt0 = null;
var nArgs = args.length;
var nMax = 2147483647;
var isRelative = false;
if (tok == 135270418) {
if (nArgs > 1 && args[nArgs - 1].tok == 4 && (args[nArgs - 1].value).equalsIgnoreCase ("relative")) {
nArgs--;
isRelative = true;
}if (nArgs > 1 && args[nArgs - 1].tok == 2 && args[0].tok == 10) {
nMax = args[nArgs - 1].asInt ();
if (nMax <= 0) nMax = 2147483646;
nArgs--;
}}switch (nArgs) {
case 0:
case 1:
case 4:
break;
case 2:
if (tok == 135270418) {
if (args[0].tok == 7 && (args[1].tok == 7 || args[1].tok == 1048589)) break;
if (args[0].tok == 10 && (args[1].tok == 2 || args[1].tok == 10)) break;
}if ((pt0 = mp.ptValue (args[0], false)) == null || tok != 135270418 && args[1].tok == 8) return false;
break;
case 3:
if (tok != 135270418) return false;
if (args[0].tok == 9) {
if (args[2].tok != 8 && args[2].tok != 10) return false;
break;
}for (var i = 0; i < 3; i++) if (args[i].tok != 8 && args[i].tok != 10) return false;

break;
default:
return false;
}
var q = null;
var qs = null;
var p4 = null;
switch (nArgs) {
case 0:
return mp.addXPt4 (JU.Quat.newQ (this.vwr.getRotationQuaternion ()).toPoint4f ());
case 1:
default:
if (tok == 135270418 && args[0].tok == 7) {
var data1 = this.e.getQuaternionArray (args[0].getList (), 1073742001);
var mean = JU.Quat.sphereMean (data1, null, 0.0001);
q = (Clazz_instanceOf (mean, JU.Quat) ? mean : null);
break;
} else if (tok == 135270418 && args[0].tok == 10) {
qs = this.vwr.getAtomGroupQuaternions (args[0].value, nMax);
} else if (args[0].tok == 11) {
q = JU.Quat.newM (args[0].value);
} else if (args[0].tok == 9) {
p4 = args[0].value;
} else {
var s = J.script.SV.sValue (args[0]);
var v = JW.Escape.uP (s.equalsIgnoreCase ("best") ? this.vwr.getOrientationText (1073741863, null) : s);
if (!(Clazz_instanceOf (v, JU.P4))) return false;
p4 = v;
}if (tok == 135266307) q = JU.Quat.newVA (JU.P3.new3 (p4.x, p4.y, p4.z), p4.w);
break;
case 2:
if (tok == 135270418) {
if (args[0].tok == 7 && args[1].tok == 7) {
var data1 = this.e.getQuaternionArray (args[0].getList (), 1073742001);
var data2 = this.e.getQuaternionArray (args[1].getList (), 1073742001);
qs = JU.Quat.div (data2, data1, nMax, isRelative);
break;
}if (args[0].tok == 7 && args[1].tok == 1048589) {
var data1 = this.e.getQuaternionArray (args[0].getList (), 1073742001);
var stddev =  Clazz_newFloatArray (1, 0);
JU.Quat.sphereMean (data1, stddev, 0.0001);
return mp.addXFloat (stddev[0]);
}if (args[0].tok == 10 && args[1].tok == 10) {
var data1 = this.vwr.getAtomGroupQuaternions (args[0].value, 2147483647);
var data2 = this.vwr.getAtomGroupQuaternions (args[1].value, 2147483647);
qs = JU.Quat.div (data2, data1, nMax, isRelative);
break;
}}var pt1 = mp.ptValue (args[1], false);
p4 = mp.planeValue (args[0]);
if (pt1 != null) q = JU.Quat.getQuaternionFrame (JU.P3.new3 (0, 0, 0), pt0, pt1);
 else q = JU.Quat.newVA (pt0, J.script.SV.fValue (args[1]));
break;
case 3:
if (args[0].tok == 9) {
var pt = (args[2].tok == 8 ? args[2].value : this.vwr.getAtomSetCenter (args[2].value));
return mp.addXStr (JW.Escape.drawQuat (JU.Quat.newP4 (args[0].value), "q", J.script.SV.sValue (args[1]), pt, 1));
}var pts =  new Array (3);
for (var i = 0; i < 3; i++) pts[i] = (args[i].tok == 8 ? args[i].value : this.vwr.getAtomSetCenter (args[i].value));

q = JU.Quat.getQuaternionFrame (pts[0], pts[1], pts[2]);
break;
case 4:
if (tok == 135270418) p4 = JU.P4.new4 (J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2]), J.script.SV.fValue (args[3]), J.script.SV.fValue (args[0]));
 else q = JU.Quat.newVA (JU.P3.new3 (J.script.SV.fValue (args[0]), J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2])), J.script.SV.fValue (args[3]));
break;
}
if (qs != null) {
if (nMax != 2147483647) {
var list =  new JU.List ();
for (var i = 0; i < qs.length; i++) list.addLast (qs[i].toPoint4f ());

return mp.addXList (list);
}q = (qs.length > 0 ? qs[0] : null);
}return mp.addXPt4 ((q == null ? JU.Quat.newP4 (p4) : q).toPoint4f ());
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateRandom", 
 function (mp, args) {
if (args.length > 2) return false;
var lower = (args.length < 2 ? 0 : J.script.SV.fValue (args[0]));
var range = (args.length == 0 ? 1 : J.script.SV.fValue (args[args.length - 1]));
range -= lower;
return mp.addXFloat ((Math.random () * range) + lower);
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateRowCol", 
 function (mp, args, tok) {
if (args.length != 1) return false;
var n = args[0].asInt () - 1;
var x1 = mp.getX ();
var f;
switch (x1.tok) {
case 11:
if (n < 0 || n > 2) return false;
var m = x1.value;
switch (tok) {
case 1276117515:
f =  Clazz_newFloatArray (3, 0);
m.getRow (n, f);
return mp.addXAF (f);
case 1276117514:
default:
f =  Clazz_newFloatArray (3, 0);
m.getColumn (n, f);
return mp.addXAF (f);
}
case 12:
if (n < 0 || n > 2) return false;
var m4 = x1.value;
switch (tok) {
case 1276117515:
f =  Clazz_newFloatArray (4, 0);
m4.getRow (n, f);
return mp.addXAF (f);
case 1276117514:
default:
f =  Clazz_newFloatArray (4, 0);
m4.getColumn (n, f);
return mp.addXAF (f);
}
}
return false;
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateReplace", 
 function (mp, args) {
if (args.length != 2) return false;
var x = mp.getX ();
var sFind = J.script.SV.sValue (args[0]);
var sReplace = J.script.SV.sValue (args[1]);
var s = (x.tok == 7 ? null : J.script.SV.sValue (x));
if (s != null) return mp.addXStr (JU.PT.rep (s, sFind, sReplace));
var list = J.script.SV.listValue (x);
for (var i = list.length; --i >= 0; ) list[i] = JU.PT.rep (list[i], sFind, sReplace);

return mp.addXAS (list);
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateScript", 
 function (mp, args, tok) {
if (tok == 135287308 && args.length != 1 || args.length == 0 || args.length > 2) return false;
var s = J.script.SV.sValue (args[0]);
var sb =  new JU.SB ();
switch (tok) {
case 135271429:
var appID = (args.length == 2 ? J.script.SV.sValue (args[1]) : ".");
if (!appID.equals (".")) sb.append (this.vwr.jsEval (appID + "\1" + s));
if (appID.equals (".") || appID.equals ("*")) this.e.runScriptBuffer (s, sb);
break;
case 135287308:
sb.append (this.vwr.jsEval (s));
break;
}
s = sb.toString ();
var f;
return (Float.isNaN (f = JU.PT.parseFloatStrict (s)) ? mp.addXStr (s) : s.indexOf (".") >= 0 ? mp.addXFloat (f) : mp.addXInt (JU.PT.parseInt (s)));
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateSort", 
 function (mp, args, tok) {
if (args.length > 1) return false;
if (tok == 1276117011) {
var n = (args.length == 0 ? 0 : args[0].asInt ());
return mp.addX (mp.getX ().sortOrReverse (n));
}var x = mp.getX ();
var match = (args.length == 0 ? null : args[0]);
if (x.tok == 4) {
var n = 0;
var s = J.script.SV.sValue (x);
if (match == null) return mp.addXInt (0);
var m = J.script.SV.sValue (match);
for (var i = 0; i < s.length; i++) {
var pt = s.indexOf (m, i);
if (pt < 0) break;
n++;
i = pt;
}
return mp.addXInt (n);
}var counts =  new JU.List ();
var last = null;
var count = null;
var xList = J.script.SV.getVariable (x.value).sortOrReverse (0).getList ();
if (xList == null) return (match == null ? mp.addXStr ("") : mp.addXInt (0));
for (var i = 0, nLast = xList.size (); i <= nLast; i++) {
var a = (i == nLast ? null : xList.get (i));
if (match != null && a != null && !J.script.SV.areEqual (a, match)) continue;
if (J.script.SV.areEqual (a, last)) {
count.intValue++;
continue;
} else if (last != null) {
var y =  new JU.List ();
y.addLast (last);
y.addLast (count);
counts.addLast (J.script.SV.getVariableList (y));
}count = J.script.SV.newI (1);
last = a;
}
if (match == null) return mp.addX (J.script.SV.getVariableList (counts));
if (counts.isEmpty ()) return mp.addXInt (0);
return mp.addX (counts.get (0).getList ().get (1));
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateString", 
 function (mp, tok, args) {
if (args.length > 1) return false;
var x = mp.getX ();
if (x.tok == 7 && tok != 1276117510 && tok != 1276117512) {
mp.addX (x);
return this.evaluateList (mp, tok, args);
}var s = (tok == 1276117510 && x.tok == 10 || tok == 1276117512 && x.tok == 7 ? null : J.script.SV.sValue (x));
var sArg = (args.length == 1 ? J.script.SV.sValue (args[0]) : tok == 1276117512 ? "" : "\n");
switch (tok) {
case 1276117510:
if (x.tok == 10) {
var bsSelected = J.script.SV.bsSelectVar (x);
sArg = "\n";
var modelCount = this.vwr.getModelCount ();
s = "";
for (var i = 0; i < modelCount; i++) {
s += (i == 0 ? "" : "\n");
var bs = this.vwr.getModelUndeletedAtomsBitSet (i);
bs.and (bsSelected);
s += JW.Escape.eBS (bs);
}
}return mp.addXAS (JU.PT.split (s, sArg));
case 1276117506:
if (s.length > 0 && s.charAt (s.length - 1) == '\n') s = s.substring (0, s.length - 1);
return mp.addXStr (JU.PT.rep (s, "\n", sArg));
case 1276117512:
if (s != null) return mp.addXStr (JU.PT.trim (s, sArg));
var list = J.script.SV.listValue (x);
for (var i = list.length; --i >= 0; ) list[i] = JU.PT.trim (list[i], sArg);

return mp.addXAS (list);
}
return mp.addXStr ("");
}, "J.script.ScriptMathProcessor,~N,~A");
Clazz_defineMethod (c$, "evaluateSubstructure", 
 function (mp, args, tok) {
if (args.length == 0) return false;
var bs =  new JU.BS ();
var pattern = J.script.SV.sValue (args[0]);
if (pattern.length > 0) try {
var bsSelected = (args.length == 2 && args[1].tok == 10 ? J.script.SV.bsSelectVar (args[1]) : null);
bs = this.vwr.getSmilesMatcher ().getSubstructureSet (pattern, this.vwr.getModelSet ().at, this.vwr.getAtomCount (), bsSelected, tok != 135267336, false);
} catch (ex) {
if (Clazz_exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
return mp.addXBs (bs);
}, "J.script.ScriptMathProcessor,~A,~N");
Clazz_defineMethod (c$, "evaluateSymop", 
 function (mp, args, haveBitSet) {
if (args.length == 0) return false;
var x1 = (haveBitSet ? mp.getX () : null);
if (x1 != null && x1.tok != 10) return false;
var bs = (x1 != null ? x1.value : args.length > 2 && args[1].tok == 10 ? args[1].value : this.vwr.getAllAtoms ());
var xyz;
switch (args[0].tok) {
case 4:
xyz = J.script.SV.sValue (args[0]);
break;
case 12:
xyz = args[0].escape ();
break;
default:
xyz = null;
}
var iOp = (xyz == null ? args[0].asInt () : 0);
var pt = (args.length > 1 ? mp.ptValue (args[1], true) : null);
if (args.length == 2 && !Float.isNaN (pt.x)) return mp.addXObj (this.vwr.getSymmetryInfo (bs, xyz, iOp, pt, null, null, 135266320));
var desc = (args.length == 1 ? "" : J.script.SV.sValue (args[args.length - 1])).toLowerCase ();
var tok = 135176;
if (args.length == 1 || desc.equalsIgnoreCase ("matrix")) {
tok = 12;
} else if (desc.equalsIgnoreCase ("array") || desc.equalsIgnoreCase ("list")) {
tok = 1073742001;
} else if (desc.equalsIgnoreCase ("description")) {
tok = 1826248716;
} else if (desc.equalsIgnoreCase ("xyz")) {
tok = 1073741982;
} else if (desc.equalsIgnoreCase ("translation")) {
tok = 1073742178;
} else if (desc.equalsIgnoreCase ("axis")) {
tok = 1073741854;
} else if (desc.equalsIgnoreCase ("plane")) {
tok = 135266319;
} else if (desc.equalsIgnoreCase ("angle")) {
tok = 135266305;
} else if (desc.equalsIgnoreCase ("axispoint")) {
tok = 135266320;
} else if (desc.equalsIgnoreCase ("center")) {
tok = 12289;
}return mp.addXObj (this.vwr.getSymmetryInfo (bs, xyz, iOp, pt, null, desc, tok));
}, "J.script.ScriptMathProcessor,~A,~B");
Clazz_defineMethod (c$, "evaluateTensor", 
 function (mp, args) {
if (args.length > 2) return false;
var bs = J.script.SV.getBitSet (mp.getX (), false);
var tensorType = (args.length == 0 ? null : J.script.SV.sValue (args[0]).toLowerCase ());
var calc = this.vwr.getNMRCalculation ();
if ("unique".equals (tensorType)) return mp.addXBs (calc.getUniqueTensorSet (bs));
var infoType = (args.length < 2 ? null : J.script.SV.sValue (args[1]).toLowerCase ());
return mp.addXList (calc.getTensorInfo (tensorType, infoType, bs));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateUserFunction", 
 function (mp, name, args, tok, isSelector) {
var x1 = null;
if (isSelector) {
x1 = mp.getX ();
if (x1.tok != 10) return false;
}mp.wasX = false;
var params =  new JU.List ();
for (var i = 0; i < args.length; i++) {
params.addLast (args[i]);
}
if (isSelector) {
return mp.addXObj (this.e.getBitsetProperty (J.script.SV.bsSelectVar (x1), tok, null, null, x1.value, [name, params], false, x1.index, false));
}var $var = this.e.getUserFunctionResult (name, params, null);
return ($var == null ? false : mp.addX ($var));
}, "J.script.ScriptMathProcessor,~S,~A,~N,~B");
Clazz_defineMethod (c$, "evaluateWithin", 
 function (mp, args) {
if (args.length < 1 || args.length > 5) return false;
var i = args.length;
var distance = 0;
var withinSpec = args[0].value;
var withinStr = "" + withinSpec;
var tok = args[0].tok;
if (tok == 4) tok = J.script.T.getTokFromName (withinStr);
var isVdw = (tok == 1649412120);
if (isVdw) {
distance = 100;
withinSpec = null;
}var bs;
var isWithinModelSet = false;
var isWithinGroup = false;
var isDistance = (isVdw || tok == 3 || tok == 2);
var rd = null;
switch (tok) {
case 1048580:
if (i != 3 || !(Clazz_instanceOf (args[1].value, JU.BS)) || !(Clazz_instanceOf (args[2].value, JU.BS))) return false;
return mp.addXBs (this.vwr.getBranchBitSet ((args[2].value).nextSetBit (0), (args[1].value).nextSetBit (0), true));
case 135267336:
case 1238369286:
case 135267335:
var bsSelected = null;
var isOK = true;
switch (i) {
case 2:
break;
case 3:
isOK = (args[2].tok == 10);
if (isOK) bsSelected = args[2].value;
break;
default:
isOK = false;
}
if (!isOK) this.e.invArg ();
return mp.addXObj (this.e.getSmilesExt ().getSmilesMatches (J.script.SV.sValue (args[1]), null, bsSelected, null, tok == 135267335, mp.asBitSet));
}
if (Clazz_instanceOf (withinSpec, String)) {
if (tok == 0) {
tok = 1048614;
if (i > 2) return false;
i = 2;
}} else if (isDistance) {
if (!isVdw) distance = J.script.SV.fValue (args[0]);
if (i < 2) return false;
switch (tok = args[1].tok) {
case 1048589:
case 1048588:
isWithinModelSet = args[1].asBoolean ();
i = 0;
break;
case 4:
var s = J.script.SV.sValue (args[1]);
if (s.startsWith ("$")) return mp.addXBs (this.getAtomsNearSurface (distance, s.substring (1)));
isWithinGroup = (s.equalsIgnoreCase ("group"));
isVdw = (s.equalsIgnoreCase ("vanderwaals"));
if (isVdw) {
withinSpec = null;
tok = 1649412120;
} else {
tok = 1087373318;
}break;
}
} else {
return false;
}var pt = null;
var plane = null;
switch (i) {
case 1:
switch (tok) {
case 137363467:
case 3145760:
case 1679429641:
return mp.addXBs (this.vwr.getAtomBits (tok, null));
case 1073741864:
return mp.addXBs (this.vwr.getAtomBits (tok, ""));
case 1048614:
return mp.addXBs (this.vwr.getAtomBits (1087373320, withinStr));
}
return false;
case 2:
switch (tok) {
case 1048614:
tok = 1087373320;
break;
case 1087375362:
case 1087375361:
case 1073741864:
case 1087373320:
return mp.addXBs (this.vwr.getAtomBits (tok, J.script.SV.sValue (args[args.length - 1])));
}
break;
case 3:
switch (tok) {
case 1048589:
case 1048588:
case 1087373318:
case 1649412120:
case 135266319:
case 135267841:
case 1048581:
break;
case 1087373320:
withinStr = J.script.SV.sValue (args[2]);
break;
default:
return false;
}
break;
}
i = args.length - 1;
if (Clazz_instanceOf (args[i].value, JU.P4)) {
plane = args[i].value;
} else if (Clazz_instanceOf (args[i].value, JU.P3)) {
pt = args[i].value;
if (J.script.SV.sValue (args[1]).equalsIgnoreCase ("hkl")) plane = this.e.getHklPlane (pt);
}if (i > 0 && plane == null && pt == null && !(Clazz_instanceOf (args[i].value, JU.BS))) return false;
if (plane != null) return mp.addXBs (this.vwr.getAtomsNearPlane (distance, plane));
if (pt != null) return mp.addXBs (this.vwr.getAtomsNearPt (distance, pt));
bs = (args[i].tok == 10 ? J.script.SV.bsSelectVar (args[i]) : null);
if (tok == 1087373320) return mp.addXBs (this.vwr.getSequenceBits (withinStr, bs));
if (bs == null) bs =  new JU.BS ();
if (!isDistance) return mp.addXBs (this.vwr.getAtomBits (tok, bs));
if (isWithinGroup) return mp.addXBs (this.vwr.getGroupsWithin (Clazz_floatToInt (distance), bs));
if (isVdw) rd =  new J.atomdata.RadiusData (null, (distance > 10 ? distance / 100 : distance), (distance > 10 ? J.atomdata.RadiusData.EnumType.FACTOR : J.atomdata.RadiusData.EnumType.OFFSET), J.c.VDW.AUTO);
return mp.addXBs (this.vwr.getAtomsWithinRadius (distance, bs, isWithinModelSet, rd));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "evaluateWrite", 
 function (mp, args) {
if (args.length == 0) return false;
if (args.length == 1 && args[0].tok == 4 && args[0].asString ().equalsIgnoreCase ("PNGJ")) return mp.addXMap (this.vwr.getFileAsMap (null));
return mp.addXStr (this.e.getCmdExt ().write (args));
}, "J.script.ScriptMathProcessor,~A");
Clazz_defineMethod (c$, "getAtomsNearSurface", 
 function (distance, surfaceId) {
var data = [surfaceId, null, null];
if (this.e.getShapePropertyData (24, "getVertices", data)) return this.vwr.getAtomsNearPts (distance, data[1], data[2]);
data[1] = Integer.$valueOf (0);
data[2] = Integer.$valueOf (-1);
if (this.e.getShapePropertyData (22, "getCenter", data)) return this.vwr.getAtomsNearPt (distance, data[2]);
return  new JU.BS ();
}, "~N,~S");
Clazz_defineMethod (c$, "getDistance", 
 function (mp, x1, x2, tok) {
var pt1 = mp.ptValue (x1, true);
var plane1 = mp.planeValue (x1);
var pt2 = mp.ptValue (x2, true);
var plane2 = mp.planeValue (x2);
if (tok == 1276117505) {
if (plane1 != null && plane2 != null) return plane1.x * plane2.x + plane1.y * plane2.y + plane1.z * plane2.z + plane1.w * plane2.w;
if (plane1 != null) pt1 = JU.P3.new3 (plane1.x, plane1.y, plane1.z);
if (plane2 != null) pt2 = JU.P3.new3 (plane2.x, plane2.y, plane2.z);
return pt1.x * pt2.x + pt1.y * pt2.y + pt1.z * pt2.z;
}if (plane1 == null) return (plane2 == null ? pt2.distance (pt1) : JW.Measure.distanceToPlane (plane2, pt1));
return JW.Measure.distanceToPlane (plane1, pt2);
}, "J.script.ScriptMathProcessor,J.script.SV,J.script.SV,~N");
Clazz_overrideMethod (c$, "getMinMax", 
function (floatOrSVArray, tok) {
var data = null;
var sv = null;
var ndata = 0;
while (true) {
if (JU.PT.isAF (floatOrSVArray)) {
data = floatOrSVArray;
ndata = data.length;
if (ndata == 0) break;
} else if (Clazz_instanceOf (floatOrSVArray, JU.List)) {
sv = floatOrSVArray;
ndata = sv.size ();
if (ndata == 0) break;
var sv0 = sv.get (0);
if (sv0.tok == 4 && (sv0.value).startsWith ("{")) {
var pt = J.script.SV.ptValue (sv0);
if (Clazz_instanceOf (pt, JU.P3)) return this.getMinMaxPoint (sv, tok);
if (Clazz_instanceOf (pt, JU.P4)) return this.getMinMaxQuaternion (sv, tok);
break;
}} else {
break;
}var sum;
switch (tok) {
case 32:
sum = 3.4028235E38;
break;
case 64:
sum = -3.4028235E38;
break;
default:
sum = 0;
}
var sum2 = 0;
var n = 0;
for (var i = ndata; --i >= 0; ) {
var v = (data == null ? J.script.SV.fValue (sv.get (i)) : data[i]);
if (Float.isNaN (v)) continue;
n++;
switch (tok) {
case 160:
case 192:
sum2 += (v) * v;
case 128:
case 96:
sum += v;
break;
case 32:
if (v < sum) sum = v;
break;
case 64:
if (v > sum) sum = v;
break;
}
}
if (n == 0) break;
switch (tok) {
case 96:
sum /= n;
break;
case 192:
if (n == 1) break;
sum = Math.sqrt ((sum2 - sum * sum / n) / (n - 1));
break;
case 32:
case 64:
case 128:
break;
case 160:
sum = sum2;
break;
}
return Float.$valueOf (sum);
}
return "NaN";
}, "~O,~N");
Clazz_defineMethod (c$, "getMinMaxPoint", 
 function (pointOrSVArray, tok) {
var data = null;
var sv = null;
var ndata = 0;
if (Clazz_instanceOf (pointOrSVArray, Array)) {
data = pointOrSVArray;
ndata = data.length;
} else if (Clazz_instanceOf (pointOrSVArray, JU.List)) {
sv = pointOrSVArray;
ndata = sv.size ();
}if (sv != null || data != null) {
var result =  new JU.P3 ();
var fdata =  Clazz_newFloatArray (ndata, 0);
var ok = true;
for (var xyz = 0; xyz < 3 && ok; xyz++) {
for (var i = 0; i < ndata; i++) {
var pt = (data == null ? J.script.SV.ptValue (sv.get (i)) : data[i]);
if (pt == null) {
ok = false;
break;
}switch (xyz) {
case 0:
fdata[i] = pt.x;
break;
case 1:
fdata[i] = pt.y;
break;
case 2:
fdata[i] = pt.z;
break;
}
}
if (!ok) break;
var f = this.getMinMax (fdata, tok);
if (Clazz_instanceOf (f, Float)) {
var value = (f).floatValue ();
switch (xyz) {
case 0:
result.x = value;
break;
case 1:
result.y = value;
break;
case 2:
result.z = value;
break;
}
} else {
break;
}}
return result;
}return "NaN";
}, "~O,~N");
Clazz_defineMethod (c$, "getMinMaxQuaternion", 
 function (svData, tok) {
var data;
switch (tok) {
case 32:
case 64:
case 128:
case 160:
return "NaN";
}
while (true) {
data = this.e.getQuaternionArray (svData, 1073742001);
if (data == null) break;
var retStddev =  Clazz_newFloatArray (1, 0);
var result = JU.Quat.sphereMean (data, retStddev, 0.0001);
switch (tok) {
case 96:
return result;
case 192:
return Float.$valueOf (retStddev[0]);
}
break;
}
return "NaN";
}, "JU.List,~N");
Clazz_defineMethod (c$, "getPatternMatcher", 
 function () {
return (this.pm == null ? this.pm = J.api.Interface.getUtil ("PatternMatcher") : this.pm);
});
Clazz_defineMethod (c$, "opTokenFor", 
 function (tok) {
switch (tok) {
case 1276118017:
case 1276117506:
return J.script.T.tokenPlus;
case 1276117511:
return J.script.T.tokenMinus;
case 1276117507:
return J.script.T.tokenTimes;
case 1276117508:
return J.script.T.tokenMul3;
case 1276117504:
return J.script.T.tokenDivide;
}
return null;
}, "~N");
Clazz_overrideMethod (c$, "setContactBitSets", 
function (bsA, bsB, localOnly, distance, rd, warnMultiModel) {
var withinAllModels;
var bs;
if (bsB == null) {
bsB = JW.BSUtil.setAll (this.vwr.getAtomCount ());
JW.BSUtil.andNot (bsB, this.vwr.getDeletedAtoms ());
bsB.andNot (bsA);
withinAllModels = false;
} else {
bs = JW.BSUtil.copy (bsA);
bs.or (bsB);
var nModels = this.vwr.getModelBitSet (bs, false).cardinality ();
withinAllModels = (nModels > 1);
if (warnMultiModel && nModels > 1 && !this.e.tQuiet) this.e.showString (J.i18n.GT._ ("Note: More than one model is involved in this contact!"));
}if (!bsA.equals (bsB)) {
var setBfirst = (!localOnly || bsA.cardinality () < bsB.cardinality ());
if (setBfirst) {
bs = this.vwr.getAtomsWithinRadius (distance, bsA, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsB.and (bs);
}if (localOnly) {
bs = this.vwr.getAtomsWithinRadius (distance, bsB, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsA.and (bs);
if (!setBfirst) {
bs = this.vwr.getAtomsWithinRadius (distance, bsA, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsB.and (bs);
}bs = JW.BSUtil.copy (bsB);
bs.and (bsA);
if (bs.equals (bsA)) bsB.andNot (bsA);
 else if (bs.equals (bsB)) bsA.andNot (bsB);
}}return bsB;
}, "JU.BS,JU.BS,~B,~N,J.atomdata.RadiusData,~B");
});
})(Clazz
,Clazz.doubleToInt
,Clazz.declarePackage
,Clazz.instanceOf
,Clazz.load
,Clazz.instantialize
,Clazz.decorateAsClass
,Clazz.floatToInt
,Clazz.makeConstructor
,Clazz.defineEnumConstant
,Clazz.exceptionOf
,Clazz.newIntArray
,Clazz.defineStatics
,Clazz.newFloatArray
,Clazz.declareType
,Clazz.prepareFields
,Clazz.superConstructor
,Clazz.newByteArray
,Clazz.declareInterface
,Clazz.p0p
,Clazz.pu$h
,Clazz.newShortArray
,Clazz.innerTypeInstance
,Clazz.isClassDefined
,Clazz.prepareCallback
,Clazz.newArray
,Clazz.castNullAs
,Clazz.floatToShort
,Clazz.superCall
,Clazz.decorateAsType
,Clazz.newBooleanArray
,Clazz.newCharArray
,Clazz.implementOf
,Clazz.newDoubleArray
,Clazz.overrideConstructor
,Clazz.clone
,Clazz.doubleToShort
,Clazz.getInheritedLevel
,Clazz.getParamsType
,Clazz.isAF
,Clazz.isAI
,Clazz.isAS
,Clazz.isASS
,Clazz.isAP
,Clazz.isAFloat
,Clazz.isAII
,Clazz.isAFF
,Clazz.isAFFF
,Clazz.tryToSearchAndExecute
,Clazz.getStackTrace
,Clazz.inheritArgs
,Clazz.alert
,Clazz.defineMethod
,Clazz.overrideMethod
,Clazz.declareAnonymous
//,Clazz.checkPrivateMethod
,Clazz.cloneFinals
);
