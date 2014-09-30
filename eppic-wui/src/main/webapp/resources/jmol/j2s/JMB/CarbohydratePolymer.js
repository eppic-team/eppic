Clazz.declarePackage ("JMB");
Clazz.load (["JMB.BioPolymer"], "JMB.CarbohydratePolymer", null, function () {
c$ = Clazz.declareType (JMB, "CarbohydratePolymer", JMB.BioPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, JMB.CarbohydratePolymer, [monomers]);
this.type = 3;
}, "~A");
});
