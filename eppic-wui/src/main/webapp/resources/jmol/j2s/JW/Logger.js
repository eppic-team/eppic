Clazz.declarePackage ("JW");
Clazz.load (["java.util.Hashtable", "JW.DefaultLogger"], "JW.Logger", ["java.lang.Long"], function () {
c$ = Clazz.declareType (JW, "Logger");
c$.getProperty = Clazz.defineMethod (c$, "getProperty", 
 function (level, defaultValue) {
try {
var property = System.getProperty ("jmol.logger." + level, null);
if (property != null) {
return (property.equalsIgnoreCase ("true"));
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return defaultValue;
}, "~S,~B");
c$.setLogger = Clazz.defineMethod (c$, "setLogger", 
function (logger) {
JW.Logger._logger = logger;
JW.Logger.debugging = JW.Logger.isActiveLevel (5) || JW.Logger.isActiveLevel (6);
JW.Logger.debuggingHigh = (JW.Logger.debugging && JW.Logger._activeLevels[6]);
}, "JW.LoggerInterface");
c$.isActiveLevel = Clazz.defineMethod (c$, "isActiveLevel", 
function (level) {
return JW.Logger._logger != null && level >= 0 && level < 7 && JW.Logger._activeLevels[level];
}, "~N");
c$.setActiveLevel = Clazz.defineMethod (c$, "setActiveLevel", 
function (level, active) {
if (level < 0) level = 0;
if (level >= 7) level = 6;
JW.Logger._activeLevels[level] = active;
JW.Logger.debugging = JW.Logger.isActiveLevel (5) || JW.Logger.isActiveLevel (6);
JW.Logger.debuggingHigh = (JW.Logger.debugging && JW.Logger._activeLevels[6]);
}, "~N,~B");
c$.setLogLevel = Clazz.defineMethod (c$, "setLogLevel", 
function (level) {
for (var i = 7; --i >= 0; ) JW.Logger.setActiveLevel (i, i <= level);

}, "~N");
c$.getLevel = Clazz.defineMethod (c$, "getLevel", 
function (level) {
switch (level) {
case 6:
return "DEBUGHIGH";
case 5:
return "DEBUG";
case 4:
return "INFO";
case 3:
return "WARN";
case 2:
return "ERROR";
case 1:
return "FATAL";
}
return "????";
}, "~N");
c$.logLevel = Clazz.defineMethod (c$, "logLevel", 
function () {
return JW.Logger._logLevel;
});
c$.doLogLevel = Clazz.defineMethod (c$, "doLogLevel", 
function (log) {
JW.Logger._logLevel = log;
}, "~B");
c$.debug = Clazz.defineMethod (c$, "debug", 
function (txt) {
if (!JW.Logger.debugging) return;
try {
JW.Logger._logger.debug (txt);
} catch (t) {
}
}, "~S");
c$.info = Clazz.defineMethod (c$, "info", 
function (txt) {
try {
if (JW.Logger.isActiveLevel (4)) {
JW.Logger._logger.info (txt);
}} catch (t) {
}
}, "~S");
c$.warn = Clazz.defineMethod (c$, "warn", 
function (txt) {
try {
if (JW.Logger.isActiveLevel (3)) {
JW.Logger._logger.warn (txt);
}} catch (t) {
}
}, "~S");
c$.warnEx = Clazz.defineMethod (c$, "warnEx", 
function (txt, e) {
try {
if (JW.Logger.isActiveLevel (3)) {
JW.Logger._logger.warnEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.error = Clazz.defineMethod (c$, "error", 
function (txt) {
try {
if (JW.Logger.isActiveLevel (2)) {
JW.Logger._logger.error (txt);
}} catch (t) {
}
}, "~S");
c$.errorEx = Clazz.defineMethod (c$, "errorEx", 
function (txt, e) {
try {
if (JW.Logger.isActiveLevel (2)) {
JW.Logger._logger.errorEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.getLogLevel = Clazz.defineMethod (c$, "getLogLevel", 
function () {
for (var i = 7; --i >= 0; ) if (JW.Logger.isActiveLevel (i)) return i;

return 0;
});
c$.fatal = Clazz.defineMethod (c$, "fatal", 
function (txt) {
try {
if (JW.Logger.isActiveLevel (1)) {
JW.Logger._logger.fatal (txt);
}} catch (t) {
}
}, "~S");
c$.fatalEx = Clazz.defineMethod (c$, "fatalEx", 
function (txt, e) {
try {
if (JW.Logger.isActiveLevel (1)) {
JW.Logger._logger.fatalEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.startTimer = Clazz.defineMethod (c$, "startTimer", 
function (msg) {
if (msg != null) JW.Logger.htTiming.put (msg, Long.$valueOf (System.currentTimeMillis ()));
}, "~S");
c$.getTimerMsg = Clazz.defineMethod (c$, "getTimerMsg", 
function (msg, time) {
if (time == 0) time = JW.Logger.getTimeFrom (msg);
return "Time for " + msg + ": " + (time) + " ms";
}, "~S,~N");
c$.getTimeFrom = Clazz.defineMethod (c$, "getTimeFrom", 
 function (msg) {
var t;
return (msg == null || (t = JW.Logger.htTiming.get (msg)) == null ? -1 : System.currentTimeMillis () - t.longValue ());
}, "~S");
c$.checkTimer = Clazz.defineMethod (c$, "checkTimer", 
function (msg, andReset) {
var time = JW.Logger.getTimeFrom (msg);
if (time >= 0 && !msg.startsWith ("(")) JW.Logger.info (JW.Logger.getTimerMsg (msg, time));
if (andReset) JW.Logger.startTimer (msg);
return time;
}, "~S,~B");
c$.checkMemory = Clazz.defineMethod (c$, "checkMemory", 
function () {
var bTotal = 0;
var bFree = 0;
var bMax = 0;
{
}JW.Logger.info ("Memory: Total-Free=" + (bTotal - bFree) + "; Total=" + bTotal + "; Free=" + bFree + "; Max=" + bMax);
});
c$._logger = c$.prototype._logger =  new JW.DefaultLogger ();
Clazz.defineStatics (c$,
"LEVEL_FATAL", 1,
"LEVEL_ERROR", 2,
"LEVEL_WARN", 3,
"LEVEL_INFO", 4,
"LEVEL_DEBUG", 5,
"LEVEL_DEBUGHIGH", 6,
"LEVEL_MAX", 7,
"_activeLevels",  Clazz.newBooleanArray (7, false),
"_logLevel", false,
"debugging", false,
"debuggingHigh", false);
{
JW.Logger._activeLevels[6] = JW.Logger.getProperty ("debugHigh", false);
JW.Logger._activeLevels[5] = JW.Logger.getProperty ("debug", false);
JW.Logger._activeLevels[4] = JW.Logger.getProperty ("info", true);
JW.Logger._activeLevels[3] = JW.Logger.getProperty ("warn", true);
JW.Logger._activeLevels[2] = JW.Logger.getProperty ("error", true);
JW.Logger._activeLevels[1] = JW.Logger.getProperty ("fatal", true);
JW.Logger._logLevel = JW.Logger.getProperty ("logLevel", false);
JW.Logger.debugging = (JW.Logger._logger != null && (JW.Logger._activeLevels[5] || JW.Logger._activeLevels[6]));
JW.Logger.debuggingHigh = (JW.Logger.debugging && JW.Logger._activeLevels[6]);
}c$.htTiming = c$.prototype.htTiming =  new java.util.Hashtable ();
});
