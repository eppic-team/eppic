Clazz.declarePackage ("J.script");
Clazz.load (["java.lang.Exception"], "J.script.ScriptException", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.eval = null;
this.message = null;
this.untranslated = null;
this.isError = false;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptException", Exception);
Clazz.makeConstructor (c$, 
function (se, msg, untranslated, isError) {
Clazz.superConstructor (this, J.script.ScriptException, []);
this.eval = se;
this.message = msg;
this.isError = isError;
if (!isError) return;
this.eval.setException (this, msg, untranslated);
}, "J.script.ScriptError,~S,~S,~B");
Clazz.defineMethod (c$, "getErrorMessageUntranslated", 
function () {
return this.untranslated;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return this.message;
});
});
