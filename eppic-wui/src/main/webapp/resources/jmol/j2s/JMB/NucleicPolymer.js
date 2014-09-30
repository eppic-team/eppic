Clazz.declarePackage ("JMB");
Clazz.load (["JMB.BioPolymer"], "JMB.NucleicPolymer", ["JU.P4", "$.V3", "JM.HBond", "JW.Measure"], function () {
c$ = Clazz.declareType (JMB, "NucleicPolymer", JMB.BioPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, JMB.NucleicPolymer, [monomers]);
this.type = 2;
this.hasWingPoints = true;
}, "~A");
Clazz.defineMethod (c$, "getNucleicPhosphorusAtom", 
function (monomerIndex) {
return this.monomers[monomerIndex].getLeadAtom ();
}, "~N");
Clazz.overrideMethod (c$, "calcEtaThetaAngles", 
function () {
var eta = NaN;
for (var i = 0; i < this.monomerCount - 2; ++i) {
var m1 = this.monomers[i];
var m2 = this.monomers[i + 1];
var p1 = m1.getP ();
var c41 = m1.getC4P ();
var p2 = m2.getP ();
var c42 = m2.getC4P ();
if (i > 0) {
var m0 = this.monomers[i - 1];
var c40 = m0.getC4P ();
eta = JW.Measure.computeTorsion (c40, p1, c41, p2, true);
}var theta = JW.Measure.computeTorsion (p1, c41, p2, c42, true);
if (eta < 0) eta += 360;
if (theta < 0) theta += 360;
m1.setGroupParameter (1112539141, eta);
m1.setGroupParameter (1112539152, theta);
}
return true;
});
Clazz.overrideMethod (c$, "calcRasmolHydrogenBonds", 
function (polymer, bsA, bsB, vAtoms, nMaxPerResidue, min, checkDistances, dsspIgnoreHydrogens) {
var other = polymer;
var vNorm =  new JU.V3 ();
var vAB =  new JU.V3 ();
var vAC =  new JU.V3 ();
for (var i = this.monomerCount; --i >= 0; ) {
var myNucleotide = this.monomers[i];
if (!myNucleotide.isPurine ()) continue;
var myN3 = myNucleotide.getN3 ();
var isInA = bsA.get (myN3.i);
if (!isInA && !bsB.get (myN3.i)) continue;
var myN1 = myNucleotide.getN1 ();
var myN9 = myNucleotide.getN0 ();
var plane =  new JU.P4 ();
JW.Measure.getPlaneThroughPoints (myN3, myN1, myN9, vNorm, vAB, vAC, plane);
var bestN3 = null;
var minDist2 = 25;
var bestNucleotide = null;
for (var j = other.monomerCount; --j >= 0; ) {
var otherNucleotide = other.monomers[j];
if (!otherNucleotide.isPyrimidine ()) continue;
var otherN3 = otherNucleotide.getN3 ();
if (isInA ? !bsB.get (otherN3.i) : !bsA.get (otherN3.i)) continue;
var otherN1 = otherNucleotide.getN0 ();
var dist2 = myN1.distanceSquared (otherN3);
if (dist2 < minDist2 && myN9.distanceSquared (otherN1) > 50 && Math.abs (JW.Measure.distanceToPlane (plane, otherN3)) < 1) {
bestNucleotide = otherNucleotide;
bestN3 = otherN3;
minDist2 = dist2;
}}
var n = 0;
if (bestN3 != null) {
n += JMB.NucleicPolymer.addHydrogenBond (vAtoms, myN1, bestN3);
if (n >= nMaxPerResidue) continue;
if (myNucleotide.isGuanine ()) {
n += JMB.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getN2 (), bestNucleotide.getO2 ());
if (n >= nMaxPerResidue) continue;
n += JMB.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getO6 (), bestNucleotide.getN4 ());
if (n >= nMaxPerResidue) continue;
} else {
n += JMB.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getN6 (), bestNucleotide.getO4 ());
}}}
}, "JMB.BioPolymer,JU.BS,JU.BS,JU.List,~N,~A,~B,~B");
c$.addHydrogenBond = Clazz.defineMethod (c$, "addHydrogenBond", 
function (vAtoms, atom1, atom2) {
if (atom1 == null || atom2 == null) return 0;
vAtoms.addLast ( new JM.HBond (atom1, atom2, 18432, 1, 0, 0));
return 1;
}, "JU.List,JM.Atom,JM.Atom");
Clazz.defineMethod (c$, "getPdbData", 
function (vwr, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
JMB.BioPolymer.getPdbData (vwr, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
}, "JV.Viewer,~S,~S,~N,~N,JU.BS,JU.BS,~B,~B,~B,~A,JU.OC,JU.SB,JU.BS");
});
