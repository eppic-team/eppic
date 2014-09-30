Clazz.declarePackage ("J.jsv");
Clazz.load (["J.api.JmolJSpecView"], "J.jsv.JSpecView", ["java.util.Hashtable", "JU.BS", "$.List", "$.PT", "JW.Escape", "$.Logger", "JV.FileManager"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
Clazz.instantialize (this, arguments);
}, J.jsv, "JSpecView", null, J.api.JmolJSpecView);
Clazz.overrideMethod (c$, "setViewer", 
function (vwr) {
this.vwr = vwr;
}, "JV.Viewer");
Clazz.overrideMethod (c$, "atomPicked", 
function (atomIndex) {
if (atomIndex < 0) return;
var peak = this.getPeakAtomRecord (atomIndex);
if (peak != null) this.sendJSpecView (peak + " src=\"JmolAtomSelect\"");
}, "~N");
Clazz.defineMethod (c$, "getPeakAtomRecord", 
 function (atomIndex) {
var atoms = this.vwr.ms.at;
var iModel = atoms[atomIndex].mi;
var type = null;
switch (atoms[atomIndex].getElementNumber ()) {
case 1:
type = "1HNMR";
break;
case 6:
type = "13CNMR";
break;
default:
return null;
}
var peaks = this.vwr.getModelAuxiliaryInfoValue (iModel, "jdxAtomSelect_" + type);
if (peaks == null) return null;
this.vwr.ms.htPeaks =  new java.util.Hashtable ();
var htPeaks = this.vwr.ms.htPeaks;
for (var i = 0; i < peaks.size (); i++) {
var peak = peaks.get (i);
System.out.println ("Jmol JSpecView.java peak=" + peak);
var bsPeak = htPeaks.get (peak);
System.out.println ("Jmol JSpecView.java bspeak=" + bsPeak);
if (bsPeak == null) {
htPeaks.put (peak, bsPeak =  new JU.BS ());
var satoms = JU.PT.getQuotedAttribute (peak, "atoms");
var select = JU.PT.getQuotedAttribute (peak, "select");
System.out.println ("Jmol JSpecView.java satoms select " + satoms + " " + select);
var script = "";
if (satoms != null) script += "visible & (atomno=" + JU.PT.rep (satoms, ",", " or atomno=") + ")";
 else if (select != null) script += "visible & (" + select + ")";
System.out.println ("Jmol JSpecView.java script : " + script);
bsPeak.or (this.vwr.getAtomBitSet (script));
}System.out.println ("Jmol JSpecView bsPeak now : " + bsPeak + " " + atomIndex);
if (bsPeak.get (atomIndex)) return peak;
}
return null;
}, "~N");
Clazz.defineMethod (c$, "sendJSpecView", 
 function (peak) {
var msg = JU.PT.getQuotedAttribute (peak, "title");
if (msg != null) this.vwr.scriptEcho (JW.Logger.debugging ? peak : msg);
peak = this.vwr.fullName + "JSpecView: " + peak;
JW.Logger.info ("Jmol.JSpecView.sendJSpecView Jmol>JSV " + peak);
this.vwr.sm.syncSend (peak, ">", 0);
}, "~S");
Clazz.overrideMethod (c$, "setModel", 
function (modelIndex) {
var syncMode = ("sync on".equals (this.vwr.ms.getModelSetAuxiliaryInfoValue ("jmolscript")) ? 1 : this.vwr.sm.getSyncMode ());
if (syncMode != 1) return;
var peak = this.vwr.getModelAuxiliaryInfoValue (modelIndex, "jdxModelSelect");
if (peak != null) this.sendJSpecView (peak + " src=\"Jmol\"");
}, "~N");
Clazz.overrideMethod (c$, "getBaseModelIndex", 
function (modelIndex) {
var baseModel = this.vwr.getModelAuxiliaryInfoValue (modelIndex, "jdxBaseModel");
if (baseModel != null) for (var i = this.vwr.getModelCount (); --i >= 0; ) if (baseModel.equals (this.vwr.getModelAuxiliaryInfoValue (i, "jdxModelID"))) return i;

return modelIndex;
}, "~N");
Clazz.overrideMethod (c$, "processSync", 
function (script, jsvMode) {
switch (jsvMode) {
default:
return null;
case 0:
this.vwr.sm.syncSend (this.vwr.fullName + "JSpecView" + script.substring (9), ">", 0);
return null;
case 7:
var list = JW.Escape.unescapeStringArray (script.substring (7));
var peaks =  new JU.List ();
for (var i = 0; i < list.length; i++) peaks.addLast (list[i]);

this.vwr.getModelSet ().setModelAuxiliaryInfo (this.vwr.getCurrentModelIndex (), "jdxAtomSelect_1HNMR", peaks);
return null;
case 14:
var filename = JU.PT.getQuotedAttribute (script, "file");
var isSimulation = filename.startsWith (JV.FileManager.SIMULATION_PROTOCOL);
var modelID = (isSimulation ? "molfile" : JU.PT.getQuotedAttribute (script, "model"));
filename = JU.PT.rep (filename, "#molfile", "");
var baseModel = JU.PT.getQuotedAttribute (script, "baseModel");
var atoms = JU.PT.getQuotedAttribute (script, "atoms");
var select = JU.PT.getQuotedAttribute (script, "select");
var script2 = JU.PT.getQuotedAttribute (script, "script");
var id = (modelID == null ? null : (filename == null ? "" : filename + "#") + modelID);
if ("".equals (baseModel)) id += ".baseModel";
var modelIndex = (id == null ? -3 : this.vwr.getModelIndexFromId (id));
if (modelIndex == -2) return null;
if (isSimulation) filename += "#molfile";
script = (modelIndex == -1 && filename != null ? script = "load " + JU.PT.esc (filename) : "");
if (id != null) script += ";model " + JU.PT.esc (id);
if (atoms != null) script += ";select visible & (@" + JU.PT.rep (atoms, ",", " or @") + ")";
 else if (select != null) script += ";select visible & (" + select + ")";
if (script2 != null) script += ";" + script2;
return script;
}
}, "~S,~N");
});
