Clazz.declarePackage ("JW");
Clazz.load (["JW.Geodesic"], "JW.Normix", ["JU.BS"], function () {
c$ = Clazz.declareType (JW, "Normix");
c$.getNormixCount = Clazz.defineMethod (c$, "getNormixCount", 
function () {
if (JW.Normix.normixCount == 0) JW.Normix.normixCount = JW.Geodesic.getVertexCount (3);
return JW.Normix.normixCount;
});
c$.newVertexBitSet = Clazz.defineMethod (c$, "newVertexBitSet", 
function () {
return JU.BS.newN (JW.Normix.getNormixCount ());
});
c$.getVertexVectors = Clazz.defineMethod (c$, "getVertexVectors", 
function () {
if (JW.Normix.vertexVectors == null) JW.Normix.vertexVectors = JW.Geodesic.getVertexVectors ();
return JW.Normix.vertexVectors;
});
c$.setInverseNormixes = Clazz.defineMethod (c$, "setInverseNormixes", 
function () {
if (JW.Normix.inverseNormixes != null) return;
JW.Normix.getNormixCount ();
JW.Normix.getVertexVectors ();
JW.Normix.inverseNormixes =  Clazz.newShortArray (JW.Normix.normixCount, 0);
var bsTemp =  new JU.BS ();
for (var n = JW.Normix.normixCount; --n >= 0; ) {
var v = JW.Normix.vertexVectors[n];
JW.Normix.inverseNormixes[n] = JW.Normix.getNormix (-v.x, -v.y, -v.z, 3, bsTemp);
}
});
c$.getInverseNormix = Clazz.defineMethod (c$, "getInverseNormix", 
function (normix) {
return JW.Normix.inverseNormixes[normix];
}, "~N");
c$.getNeighborVertexArrays = Clazz.defineMethod (c$, "getNeighborVertexArrays", 
 function () {
if (JW.Normix.neighborVertexesArrays == null) {
JW.Normix.neighborVertexesArrays = JW.Geodesic.getNeighborVertexesArrays ();
}return JW.Normix.neighborVertexesArrays;
});
c$.getNormixV = Clazz.defineMethod (c$, "getNormixV", 
function (v, bsTemp) {
return JW.Normix.getNormix (v.x, v.y, v.z, 3, bsTemp);
}, "JU.V3,JU.BS");
c$.get2SidedNormix = Clazz.defineMethod (c$, "get2SidedNormix", 
function (v, bsTemp) {
return ~JW.Normix.getNormixV (v, bsTemp);
}, "JU.V3,JU.BS");
c$.getNormix = Clazz.defineMethod (c$, "getNormix", 
 function (x, y, z, geodesicLevel, bsConsidered) {
var champion;
var t;
if (z >= 0) {
champion = 0;
t = z - 1;
} else {
champion = 11;
t = z - (-1);
}bsConsidered.clearAll ();
bsConsidered.set (champion);
JW.Normix.getVertexVectors ();
JW.Normix.getNeighborVertexArrays ();
var championDist2 = x * x + y * y + t * t;
for (var lvl = 0; lvl <= geodesicLevel; ++lvl) {
var neighborVertexes = JW.Normix.neighborVertexesArrays[lvl];
for (var offsetNeighbors = 6 * champion, i = offsetNeighbors + (champion < 12 ? 5 : 6); --i >= offsetNeighbors; ) {
var challenger = neighborVertexes[i];
if (bsConsidered.get (challenger)) continue;
bsConsidered.set (challenger);
var v = JW.Normix.vertexVectors[challenger];
var d;
d = v.x - x;
var d2 = d * d;
if (d2 >= championDist2) continue;
d = v.y - y;
d2 += d * d;
if (d2 >= championDist2) continue;
d = v.z - z;
d2 += d * d;
if (d2 >= championDist2) continue;
champion = challenger;
championDist2 = d2;
}
}
return champion;
}, "~N,~N,~N,~N,JU.BS");
Clazz.defineStatics (c$,
"NORMIX_GEODESIC_LEVEL", 3,
"normixCount", 0,
"vertexVectors", null,
"inverseNormixes", null,
"neighborVertexesArrays", null,
"NORMIX_NULL", 9999);
});
