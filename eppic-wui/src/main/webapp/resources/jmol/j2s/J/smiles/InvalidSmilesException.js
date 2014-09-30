Clazz.declarePackage ("J.smiles");
Clazz.load (["java.lang.Exception"], "J.smiles.InvalidSmilesException", null, function () {
c$ = Clazz.declareType (J.smiles, "InvalidSmilesException", Exception);
c$.getLastError = Clazz.defineMethod (c$, "getLastError", 
function () {
return J.smiles.InvalidSmilesException.lastError;
});
c$.clear = Clazz.defineMethod (c$, "clear", 
function () {
J.smiles.InvalidSmilesException.lastError = null;
});
Clazz.makeConstructor (c$, 
function (message) {
Clazz.superConstructor (this, J.smiles.InvalidSmilesException, [message]);
J.smiles.InvalidSmilesException.lastError = message;
}, "~S");
Clazz.defineStatics (c$,
"lastError", null);
});
