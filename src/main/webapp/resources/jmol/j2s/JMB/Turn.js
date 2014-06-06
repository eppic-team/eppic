Clazz.declarePackage ("JMB");
Clazz.load (["JMB.ProteinStructure"], "JMB.Turn", ["J.c.STR"], function () {
c$ = Clazz.declareType (JMB, "Turn", JMB.ProteinStructure);
Clazz.makeConstructor (c$, 
function (apolymer, monomerIndex, monomerCount) {
Clazz.superConstructor (this, JMB.Turn, []);
this.setupPS (apolymer, J.c.STR.TURN, monomerIndex, monomerCount);
this.subtype = J.c.STR.TURN;
}, "JMB.AlphaPolymer,~N,~N");
});
