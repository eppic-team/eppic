Clazz.declarePackage ("JMB");
Clazz.load (["JMB.ProteinStructure"], "JMB.Sheet", ["JU.P3", "$.V3", "J.c.STR", "JMB.AminoPolymer", "JW.Measure"], function () {
c$ = Clazz.decorateAsClass (function () {
this.alphaPolymer = null;
this.widthUnitVector = null;
this.heightUnitVector = null;
Clazz.instantialize (this, arguments);
}, JMB, "Sheet", JMB.ProteinStructure);
Clazz.makeConstructor (c$, 
function (alphaPolymer, monomerIndex, monomerCount, subtype) {
Clazz.superConstructor (this, JMB.Sheet, []);
this.setupPS (alphaPolymer, J.c.STR.SHEET, monomerIndex, monomerCount);
this.alphaPolymer = alphaPolymer;
this.subtype = subtype;
}, "JMB.AlphaPolymer,~N,~N,J.c.STR");
Clazz.overrideMethod (c$, "calcAxis", 
function () {
if (this.axisA != null) return;
if (this.monomerCount == 2) {
this.axisA = this.alphaPolymer.getLeadPoint (this.monomerIndexFirst);
this.axisB = this.alphaPolymer.getLeadPoint (this.monomerIndexFirst + 1);
} else {
this.axisA =  new JU.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + 1, this.axisA);
this.axisB =  new JU.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + this.monomerCount - 1, this.axisB);
}this.axisUnitVector =  new JU.V3 ();
this.axisUnitVector.sub2 (this.axisB, this.axisA);
this.axisUnitVector.normalize ();
var tempA =  new JU.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst, tempA);
if (this.lowerNeighborIsHelixOrSheet ()) {
} else {
JW.Measure.projectOntoAxis (tempA, this.axisA, this.axisUnitVector, this.vectorProjection);
}var tempB =  new JU.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + this.monomerCount, tempB);
if (this.upperNeighborIsHelixOrSheet ()) {
} else {
JW.Measure.projectOntoAxis (tempB, this.axisA, this.axisUnitVector, this.vectorProjection);
}this.axisA = tempA;
this.axisB = tempB;
});
Clazz.defineMethod (c$, "calcSheetUnitVectors", 
function () {
if (!(Clazz.instanceOf (this.alphaPolymer, JMB.AminoPolymer))) return;
if (this.widthUnitVector == null) {
var vectorCO =  new JU.V3 ();
var vectorCOSum =  new JU.V3 ();
var amino = this.alphaPolymer.monomers[this.monomerIndexFirst];
vectorCOSum.sub2 (amino.getCarbonylOxygenAtom (), amino.getCarbonylCarbonAtom ());
for (var i = this.monomerCount; --i > this.monomerIndexFirst; ) {
amino = this.alphaPolymer.monomers[i];
vectorCO.sub2 (amino.getCarbonylOxygenAtom (), amino.getCarbonylCarbonAtom ());
if (vectorCOSum.angle (vectorCO) < 1.5707964) vectorCOSum.add (vectorCO);
 else vectorCOSum.sub (vectorCO);
}
this.heightUnitVector = vectorCO;
this.heightUnitVector.cross (this.axisUnitVector, vectorCOSum);
this.heightUnitVector.normalize ();
this.widthUnitVector = vectorCOSum;
this.widthUnitVector.cross (this.axisUnitVector, this.heightUnitVector);
}});
Clazz.defineMethod (c$, "getWidthUnitVector", 
function () {
if (this.widthUnitVector == null) this.calcSheetUnitVectors ();
return this.widthUnitVector;
});
Clazz.defineMethod (c$, "getHeightUnitVector", 
function () {
if (this.heightUnitVector == null) this.calcSheetUnitVectors ();
return this.heightUnitVector;
});
});
