Clazz.declarePackage ("JW");
Clazz.load (["JU.AU"], "JW.Geodesic", ["java.lang.NullPointerException", "$.Short", "java.util.Hashtable", "JU.V3"], function () {
c$ = Clazz.declareType (JW, "Geodesic");
c$.getNeighborVertexesArrays = Clazz.defineMethod (c$, "getNeighborVertexesArrays", 
function () {
if (JW.Geodesic.vertexCounts == null) JW.Geodesic.createGeodesic (3);
return JW.Geodesic.neighborVertexesArrays;
});
c$.getVertexCount = Clazz.defineMethod (c$, "getVertexCount", 
function (level) {
if (JW.Geodesic.vertexCounts == null) JW.Geodesic.createGeodesic (3);
return JW.Geodesic.vertexCounts[level];
}, "~N");
c$.getVertexVectors = Clazz.defineMethod (c$, "getVertexVectors", 
function () {
if (JW.Geodesic.vertexCounts == null) JW.Geodesic.createGeodesic (3);
return JW.Geodesic.vertexVectors;
});
c$.getVertexVector = Clazz.defineMethod (c$, "getVertexVector", 
function (i) {
return JW.Geodesic.vertexVectors[i];
}, "~N");
c$.getFaceVertexes = Clazz.defineMethod (c$, "getFaceVertexes", 
function (level) {
return JW.Geodesic.faceVertexesArrays[level];
}, "~N");
c$.createGeodesic = Clazz.defineMethod (c$, "createGeodesic", 
function (lvl) {
if (lvl < JW.Geodesic.currentLevel) return;
JW.Geodesic.currentLevel = lvl;
var v =  Clazz.newShortArray (lvl + 1, 0);
JW.Geodesic.neighborVertexesArrays = JU.AU.newShort2 (lvl + 1);
JW.Geodesic.faceVertexesArrays = JU.AU.newShort2 (lvl + 1);
JW.Geodesic.vertexVectors =  new Array (12);
JW.Geodesic.vertexVectors[0] = JU.V3.new3 (0, 0, JW.Geodesic.halfRoot5);
for (var i = 0; i < 5; ++i) {
JW.Geodesic.vertexVectors[i + 1] = JU.V3.new3 (Math.cos (i * 1.2566371), Math.sin (i * 1.2566371), 0.5);
JW.Geodesic.vertexVectors[i + 6] = JU.V3.new3 (Math.cos (i * 1.2566371 + 0.62831855), Math.sin (i * 1.2566371 + 0.62831855), -0.5);
}
JW.Geodesic.vertexVectors[11] = JU.V3.new3 (0, 0, -JW.Geodesic.halfRoot5);
for (var i = 12; --i >= 0; ) JW.Geodesic.vertexVectors[i].normalize ();

JW.Geodesic.faceVertexesArrays[0] = JW.Geodesic.faceVertexesIcosahedron;
JW.Geodesic.neighborVertexesArrays[0] = JW.Geodesic.neighborVertexesIcosahedron;
v[0] = 12;
for (var i = 0; i < lvl; ++i) JW.Geodesic.quadruple (i, v);

JW.Geodesic.vertexCounts = v;
}, "~N");
c$.quadruple = Clazz.defineMethod (c$, "quadruple", 
 function (level, counts) {
JW.Geodesic.htVertex =  new java.util.Hashtable ();
var oldVertexCount = JW.Geodesic.vertexVectors.length;
var oldFaceVertexes = JW.Geodesic.faceVertexesArrays[level];
var oldFaceVertexesLength = oldFaceVertexes.length;
var oldFaceCount = Clazz.doubleToInt (oldFaceVertexesLength / 3);
var oldEdgesCount = oldVertexCount + oldFaceCount - 2;
var newVertexCount = oldVertexCount + oldEdgesCount;
var newFaceCount = 4 * oldFaceCount;
JW.Geodesic.vertexVectors = JU.AU.arrayCopyObject (JW.Geodesic.vertexVectors, newVertexCount);
var newFacesVertexes =  Clazz.newShortArray (3 * newFaceCount, 0);
JW.Geodesic.faceVertexesArrays[level + 1] = newFacesVertexes;
var neighborVertexes =  Clazz.newShortArray (6 * newVertexCount, 0);
JW.Geodesic.neighborVertexesArrays[level + 1] = neighborVertexes;
for (var i = neighborVertexes.length; --i >= 0; ) neighborVertexes[i] = -1;

counts[level + 1] = newVertexCount;
JW.Geodesic.vertexNext = oldVertexCount;
var iFaceNew = 0;
for (var i = 0; i < oldFaceVertexesLength; ) {
var iA = oldFaceVertexes[i++];
var iB = oldFaceVertexes[i++];
var iC = oldFaceVertexes[i++];
var iAB = JW.Geodesic.getVertex (iA, iB);
var iBC = JW.Geodesic.getVertex (iB, iC);
var iCA = JW.Geodesic.getVertex (iC, iA);
newFacesVertexes[iFaceNew++] = iA;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iB;
newFacesVertexes[iFaceNew++] = iBC;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iC;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iBC;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iBC;
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iA);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iCA);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iBC);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iB);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iB);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iCA);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iC);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iCA, iC);
JW.Geodesic.addNeighboringVertexes (neighborVertexes, iCA, iA);
}
if (true) {
var vertexCount = JW.Geodesic.vertexVectors.length;
if (iFaceNew != newFacesVertexes.length) throw  new NullPointerException ();
if (JW.Geodesic.vertexNext != newVertexCount) throw  new NullPointerException ();
for (var i = 0; i < 12; ++i) {
for (var j = 0; j < 5; ++j) {
var neighbor = neighborVertexes[i * 6 + j];
if (neighbor < 0) throw  new NullPointerException ();
if (neighbor >= vertexCount) throw  new NullPointerException ();
if (neighborVertexes[i * 6 + 5] != -1) throw  new NullPointerException ();
}
}
for (var i = 72; i < neighborVertexes.length; ++i) {
var neighbor = neighborVertexes[i];
if (neighbor < 0) throw  new NullPointerException ();
if (neighbor >= vertexCount) throw  new NullPointerException ();
}
for (var i = 0; i < newVertexCount; ++i) {
var neighborCount = 0;
for (var j = neighborVertexes.length; --j >= 0; ) if (neighborVertexes[j] == i) ++neighborCount;

if ((i < 12 && neighborCount != 5) || (i >= 12 && neighborCount != 6)) throw  new NullPointerException ();
var faceCount = 0;
for (var j = newFacesVertexes.length; --j >= 0; ) if (newFacesVertexes[j] == i) ++faceCount;

if ((i < 12 && faceCount != 5) || (i >= 12 && faceCount != 6)) throw  new NullPointerException ();
}
}JW.Geodesic.htVertex = null;
}, "~N,~A");
c$.addNeighboringVertexes = Clazz.defineMethod (c$, "addNeighboringVertexes", 
 function (neighborVertexes, v1, v2) {
for (var i = v1 * 6, iMax = i + 6; i < iMax; ++i) {
if (neighborVertexes[i] == v2) return;
if (neighborVertexes[i] < 0) {
neighborVertexes[i] = v2;
for (var j = v2 * 6, jMax = j + 6; j < jMax; ++j) {
if (neighborVertexes[j] == v1) return;
if (neighborVertexes[j] < 0) {
neighborVertexes[j] = v1;
return;
}}
}}
throw  new NullPointerException ();
}, "~A,~N,~N");
c$.getVertex = Clazz.defineMethod (c$, "getVertex", 
 function (v1, v2) {
if (v1 > v2) {
var t = v1;
v1 = v2;
v2 = t;
}var hashKey = Integer.$valueOf ((v1 << 16) + v2);
var iv = JW.Geodesic.htVertex.get (hashKey);
if (iv != null) {
return iv.shortValue ();
}var newVertexVector = JW.Geodesic.vertexVectors[JW.Geodesic.vertexNext] =  new JU.V3 ();
newVertexVector.add2 (JW.Geodesic.vertexVectors[v1], JW.Geodesic.vertexVectors[v2]);
newVertexVector.normalize ();
JW.Geodesic.htVertex.put (hashKey, Short.$valueOf (JW.Geodesic.vertexNext));
return JW.Geodesic.vertexNext++;
}, "~N,~N");
c$.halfRoot5 = c$.prototype.halfRoot5 = (0.5 * Math.sqrt (5));
Clazz.defineStatics (c$,
"oneFifth", 1.2566371,
"oneTenth", 0.62831855,
"faceVertexesIcosahedron", [0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 1, 1, 6, 2, 2, 7, 3, 3, 8, 4, 4, 9, 5, 5, 10, 1, 6, 1, 10, 7, 2, 6, 8, 3, 7, 9, 4, 8, 10, 5, 9, 11, 6, 10, 11, 7, 6, 11, 8, 7, 11, 9, 8, 11, 10, 9],
"neighborVertexesIcosahedron", [1, 2, 3, 4, 5, -1, 0, 5, 10, 6, 2, -1, 0, 1, 6, 7, 3, -1, 0, 2, 7, 8, 4, -1, 0, 3, 8, 9, 5, -1, 0, 4, 9, 10, 1, -1, 1, 10, 11, 7, 2, -1, 2, 6, 11, 8, 3, -1, 3, 7, 11, 9, 4, -1, 4, 8, 11, 10, 5, -1, 5, 9, 11, 6, 1, -1, 6, 7, 8, 9, 10, -1],
"standardLevel", 3,
"maxLevel", 3,
"vertexCounts", null,
"vertexVectors", null,
"faceVertexesArrays", null,
"neighborVertexesArrays", null,
"currentLevel", 0,
"vertexNext", 0,
"htVertex", null,
"VALIDATE", true);
});
