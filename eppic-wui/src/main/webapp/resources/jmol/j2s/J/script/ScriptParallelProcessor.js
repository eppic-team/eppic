Clazz.declarePackage ("J.script");
Clazz.load (["J.api.JmolParallelProcessor", "J.script.ScriptFunction", "JU.List"], "J.script.ScriptParallelProcessor", ["java.util.concurrent.Executors", "J.script.ScriptProcess", "$.ScriptProcessRunnable", "JW.Logger", "JV.ShapeManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.counter = 0;
this.error = null;
this.lock = null;
this.processes = null;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptParallelProcessor", J.script.ScriptFunction, J.api.JmolParallelProcessor);
Clazz.prepareFields (c$, function () {
this.lock =  new Clazz._O ();
this.processes =  new JU.List ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.script.ScriptParallelProcessor, []);
});
Clazz.overrideMethod (c$, "getExecutor", 
function () {
return java.util.concurrent.Executors.newCachedThreadPool ();
});
Clazz.overrideMethod (c$, "runAllProcesses", 
function (vwr) {
if (this.processes.size () == 0) return;
this.vwr = vwr;
var inParallel = !vwr.isParallel () && vwr.setParallel (true);
var vShapeManagers =  new JU.List ();
this.error = null;
this.counter = 0;
if (JW.Logger.debugging) JW.Logger.debug ("running " + this.processes.size () + " processes on " + JV.Viewer.nProcessors + " processesors inParallel=" + inParallel);
this.counter = this.processes.size ();
for (var i = this.processes.size (); --i >= 0; ) {
var shapeManager = null;
if (inParallel) {
shapeManager =  new JV.ShapeManager (vwr, vwr.getModelSet ());
vShapeManagers.addLast (shapeManager);
}this.runProcess (this.processes.remove (0), shapeManager);
}
{
while (this.counter > 0) {
try {
this.lock.wait ();
} catch (e) {
if (Clazz.exceptionOf (e, InterruptedException)) {
} else {
throw e;
}
}
if (this.error != null) throw this.error;
}
}this.mergeResults (vShapeManagers);
vwr.setParallel (false);
}, "JV.Viewer");
Clazz.defineMethod (c$, "mergeResults", 
function (vShapeManagers) {
try {
for (var i = 0; i < vShapeManagers.size (); i++) this.vwr.mergeShapes (vShapeManagers.get (i).getShapes ());

} catch (e) {
if (Clazz.exceptionOf (e, Error)) {
throw e;
} else {
throw e;
}
} finally {
this.counter = -1;
vShapeManagers = null;
}
}, "JU.List");
Clazz.defineMethod (c$, "clearShapeManager", 
function (er) {
{
this.error = er;
this.notifyAll ();
}}, "Error");
Clazz.overrideMethod (c$, "addProcess", 
function (name, context) {
this.processes.addLast ( new J.script.ScriptProcess (name, context));
}, "~S,J.script.ScriptContext");
Clazz.defineMethod (c$, "runProcess", 
 function (process, shapeManager) {
var r =  new J.script.ScriptProcessRunnable (this, process, this.lock, shapeManager);
var exec = (shapeManager == null ? null : this.vwr.getExecutor ());
if (exec != null) {
exec.execute (r);
} else {
r.run ();
}}, "J.script.ScriptProcess,JV.ShapeManager");
Clazz.defineMethod (c$, "eval", 
function (context, shapeManager) {
this.vwr.evalParallel (context, shapeManager);
}, "J.script.ScriptContext,JV.ShapeManager");
});
