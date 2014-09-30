Clazz.declarePackage ("JU");
Clazz.load (["javajs.api.JSONEncodable"], "JU.T4", ["JU.T3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.x = 0;
this.y = 0;
this.z = 0;
this.w = 0;
Clazz.instantialize (this, arguments);
}, JU, "T4", null, [java.io.Serializable, javajs.api.JSONEncodable]);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.defineMethod (c$, "set", 
function (x, y, z, w) {
this.x = x;
this.y = y;
this.z = z;
this.w = w;
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "scale", 
function (s) {
this.x *= s;
this.y *= s;
this.z *= s;
this.w *= s;
}, "~N");
Clazz.overrideMethod (c$, "hashCode", 
function () {
return JU.T3.floatToIntBits0 (this.x) ^ JU.T3.floatToIntBits0 (this.y) ^ JU.T3.floatToIntBits0 (this.z) ^ JU.T3.floatToIntBits0 (this.w);
});
Clazz.overrideMethod (c$, "equals", 
function (o) {
if (!(Clazz.instanceOf (o, JU.T4))) return false;
var t = o;
return (this.x == t.x && this.y == t.y && this.z == t.z && this.w == t.w);
}, "~O");
Clazz.overrideMethod (c$, "toString", 
function () {
return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
});
Clazz.overrideMethod (c$, "toJSON", 
function () {
return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
});
});
