Clazz.declarePackage ("J.script");
Clazz.load (["J.script.ScriptException"], "J.script.ScriptInterruption", null, function () {
c$ = Clazz.declareType (J.script, "ScriptInterruption", J.script.ScriptException);
Clazz.makeConstructor (c$, 
function (eval, why, millis) {
Clazz.superConstructor (this, J.script.ScriptInterruption, [eval, why, "!", millis == -2147483648 || eval.vwr.autoExit]);
if (why.equals ("delay")) eval.delayScript (millis);
}, "J.script.ScriptEval,~S,~N");
});
