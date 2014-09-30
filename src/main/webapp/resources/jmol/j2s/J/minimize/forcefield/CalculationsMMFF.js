Clazz.declarePackage ("J.minimize.forcefield");
Clazz.load (["J.minimize.forcefield.Calculations"], "J.minimize.forcefield.CalculationsMMFF", ["JU.List", "J.minimize.MinAtom", "$.MinObject", "J.minimize.forcefield.MMFFAngleCalc", "$.MMFFDistanceCalc", "$.MMFFESCalc", "$.MMFFOOPCalc", "$.MMFFSBCalc", "$.MMFFTorsionCalc", "$.MMFFVDWCalc", "JW.Txt"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bondCalc = null;
this.angleCalc = null;
this.torsionCalc = null;
this.oopCalc = null;
this.vdwCalc = null;
this.esCalc = null;
this.sbCalc = null;
this.mmff = null;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "CalculationsMMFF", J.minimize.forcefield.Calculations);
Clazz.makeConstructor (c$, 
function (ff, ffParams, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints) {
Clazz.superConstructor (this, J.minimize.forcefield.CalculationsMMFF, [ff, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints]);
this.mmff = ff;
this.ffParams = ffParams;
this.bondCalc =  new J.minimize.forcefield.MMFFDistanceCalc ().set (this);
this.angleCalc =  new J.minimize.forcefield.MMFFAngleCalc ().set (this);
this.sbCalc =  new J.minimize.forcefield.MMFFSBCalc ().set (this);
this.torsionCalc =  new J.minimize.forcefield.MMFFTorsionCalc ().set (this);
this.oopCalc =  new J.minimize.forcefield.MMFFOOPCalc ().set (this);
this.vdwCalc =  new J.minimize.forcefield.MMFFVDWCalc ().set (this);
this.esCalc =  new J.minimize.forcefield.MMFFESCalc ().set (this);
}, "J.minimize.forcefield.ForceField,java.util.Map,~A,~A,~A,~A,~A,JU.List");
Clazz.overrideMethod (c$, "getUnits", 
function () {
return "kcal";
});
Clazz.overrideMethod (c$, "setupCalculations", 
function () {
var calc;
var distanceCalc =  new J.minimize.forcefield.MMFFDistanceCalc ().set (this);
calc = this.calculations[0] =  new JU.List ();
for (var i = 0; i < this.bondCount; i++) distanceCalc.setData (calc, this.minBonds[i]);

calc = this.calculations[1] =  new JU.List ();
var angleCalc =  new J.minimize.forcefield.MMFFAngleCalc ().set (this);
for (var i = 0; i < this.angleCount; i++) angleCalc.setData (calc, this.minAngles[i]);

calc = this.calculations[2] =  new JU.List ();
var sbCalc =  new J.minimize.forcefield.MMFFSBCalc ().set (this);
for (var i = 0; i < this.angleCount; i++) sbCalc.setData (calc, this.minAngles[i]);

calc = this.calculations[3] =  new JU.List ();
var torsionCalc =  new J.minimize.forcefield.MMFFTorsionCalc ().set (this);
for (var i = 0; i < this.torsionCount; i++) torsionCalc.setData (calc, this.minTorsions[i]);

calc = this.calculations[4] =  new JU.List ();
var oopCalc =  new J.minimize.forcefield.MMFFOOPCalc ().set (this);
for (var i = 0; i < this.ac; i++) if (J.minimize.forcefield.CalculationsMMFF.isInvertible (this.minAtoms[i])) oopCalc.setData (calc, i);

this.pairSearch (this.calculations[5] =  new JU.List (),  new J.minimize.forcefield.MMFFVDWCalc ().set (this), this.calculations[6] =  new JU.List (),  new J.minimize.forcefield.MMFFESCalc ().set (this));
return true;
});
Clazz.overrideMethod (c$, "isLinear", 
function (i) {
return J.minimize.MinAtom.isLinear (this.minAtoms[i]);
}, "~N");
c$.isInvertible = Clazz.defineMethod (c$, "isInvertible", 
 function (a) {
switch (a.ffType) {
default:
return false;
case 2:
case 3:
case 10:
case 30:
case 37:
case 39:
case 40:
case 41:
case 45:
case 49:
case 54:
case 55:
case 56:
case 57:
case 58:
case 63:
case 64:
case 67:
case 69:
case 78:
case 80:
case 81:
return true;
}
}, "J.minimize.MinAtom");
Clazz.overrideMethod (c$, "compute", 
function (iType, dataIn) {
switch (iType) {
case 0:
return this.bondCalc.compute (dataIn);
case 1:
return this.angleCalc.compute (dataIn);
case 2:
return this.sbCalc.compute (dataIn);
case 3:
return this.torsionCalc.compute (dataIn);
case 4:
return this.oopCalc.compute (dataIn);
case 5:
return this.vdwCalc.compute (dataIn);
case 6:
return this.esCalc.compute (dataIn);
}
return 0.0;
}, "~N,~A");
Clazz.overrideMethod (c$, "getParameterObj", 
function (a) {
return (a.key == null || a.ddata != null ? a.ddata : this.ffParams.get (a.key));
}, "J.minimize.MinObject");
Clazz.overrideMethod (c$, "getDebugHeader", 
function (iType) {
switch (iType) {
case -1:
return "MMFF94 Force Field -- T. A. Halgren, J. Comp. Chem. 5 & 6 490-519ff (1996).\n";
case 3:
return "\nT O R S I O N A L (" + this.minTorsions.length + " torsions)\n\n" + "      ATOMS           ATOM TYPES          TORSION\n" + "  I   J   K   L   I     J     K     L      ANGLE       V1       V2       V3     ENERGY\n" + "--------------------------------------------------------------------------------------\n";
default:
return this.getDebugHeader2 (iType);
}
}, "~N");
Clazz.overrideMethod (c$, "getDebugLine", 
function (iType, c) {
var energy = this.ff.toUserUnits (c.energy);
switch (iType) {
case 1:
case 2:
return JW.Txt.sprintf ("%11s  %-5s %-5s %-5s  %8.3f  %8.3f     %8.3f   %8.3f", "ssssFI", [J.minimize.MinObject.decodeKey (c.key), this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, [(c.theta * 57.29577951308232), c.dData[1], c.dData[0], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber (), this.minAtoms[c.ic].atom.getAtomNumber ()]]);
case 3:
return JW.Txt.sprintf ("%15s  %-5s %-5s %-5s %-5s  %8.3f %8.3f %8.3f %8.3f %8.3f", "sssssF", [J.minimize.MinObject.decodeKey (c.key), this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, this.minAtoms[c.id].sType, [(c.theta * 57.29577951308232), c.dData[0], c.dData[1], c.dData[2], energy]]);
default:
return this.getDebugLineC (iType, c);
}
}, "~N,J.minimize.forcefield.Calculation");
Clazz.defineStatics (c$,
"FPAR", 143.9325,
"DA_D", 'D',
"DA_DA", 133);
});
