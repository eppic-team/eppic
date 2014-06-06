Clazz.declarePackage ("JMB");
Clazz.load (["JMB.BioPolymer"], "JMB.PhosphorusPolymer", null, function () {
c$ = Clazz.declareType (JMB, "PhosphorusPolymer", JMB.BioPolymer);
Clazz.defineMethod (c$, "getPdbData", 
function (vwr, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
JMB.BioPolymer.getPdbData (vwr, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
}, "JV.Viewer,~S,~S,~N,~N,JU.BS,JU.BS,~B,~B,~B,~A,JU.OC,JU.SB,JU.BS");
});
