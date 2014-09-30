Clazz.declarePackage ("JMB");
Clazz.load (["JMB.ProteinStructure"], "JMB.Helix", ["JU.P3", "$.V3", "J.c.STR", "JW.Measure"], function () {
c$ = Clazz.declareType (JMB, "Helix", JMB.ProteinStructure);
Clazz.makeConstructor (c$, 
function (apolymer, monomerIndex, monomerCount, subtype) {
Clazz.superConstructor (this, JMB.Helix, []);
this.setupPS (apolymer, J.c.STR.HELIX, monomerIndex, monomerCount);
this.subtype = subtype;
}, "JMB.AlphaPolymer,~N,~N,J.c.STR");
Clazz.overrideMethod (c$, "calcAxis", 
function () {
if (this.axisA != null) return;
var points =  new Array (this.monomerCount + 1);
for (var i = 0; i <= this.monomerCount; i++) {
points[i] =  new JU.P3 ();
this.apolymer.getLeadMidPoint (this.monomerIndexFirst + i, points[i]);
}
this.axisA =  new JU.P3 ();
this.axisUnitVector =  new JU.V3 ();
JW.Measure.calcBestAxisThroughPoints (points, this.axisA, this.axisUnitVector, this.vectorProjection, 4);
this.axisB = JU.P3.newP (points[this.monomerCount]);
JW.Measure.projectOntoAxis (this.axisB, this.axisA, this.axisUnitVector, this.vectorProjection);
});
});
