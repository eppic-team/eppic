Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.BioShapeRenderer"], "J.renderbio.BackboneRenderer", ["JW.C"], function () {
c$ = Clazz.declareType (J.renderbio, "BackboneRenderer", J.renderbio.BioShapeRenderer);
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
var isDataFrame = this.vwr.isJmolDataFrameForModel (bioShape.modelIndex);
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) {
var atomA = this.ms.at[this.leadAtomIndices[i]];
var atomB = this.ms.at[this.leadAtomIndices[i + 1]];
if (atomA.getNBackbonesDisplayed () == 0 || atomB.getNBackbonesDisplayed () == 0 || this.ms.isAtomHidden (atomB.i)) continue;
if (!isDataFrame && atomA.distance (atomB) > 10) continue;
var colixA = JW.C.getColixInherited (this.colixes[i], atomA.getColix ());
var colixB = JW.C.getColixInherited (this.colixes[i + 1], atomB.getColix ());
if (!this.isExport && !this.isPass2 && !this.setBioColix (colixA) && !this.setBioColix (colixB)) continue;
var xA = atomA.sX;
var yA = atomA.sY;
var zA = atomA.sZ;
var xB = atomB.sX;
var yB = atomB.sY;
var zB = atomB.sZ;
this.mad = this.mads[i];
if (this.mad < 0) {
this.g3d.drawLine (colixA, colixB, xA, yA, zA, xB, yB, zB);
} else {
var width = Clazz.floatToInt (this.exportType == 1 ? this.mad : this.vwr.scaleToScreen (Clazz.doubleToInt ((zA + zB) / 2), this.mad));
this.g3d.fillCylinderXYZ (colixA, colixB, 3, width, xA, yA, zA, xB, yB, zB);
}}
}, "J.shapebio.BioShape");
});
