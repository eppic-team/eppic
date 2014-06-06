Clazz.declarePackage ("J.adapter.readers.cif");
Clazz.load (["J.adapter.readers.cif.MSCifInterface", "$.MSReader"], "J.adapter.readers.cif.MSCifReader", ["java.lang.Character", "$.Double", "JU.Matrix", "$.PT"], function () {
c$ = Clazz.decorateAsClass (function () {
this.field = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.cif, "MSCifReader", J.adapter.readers.cif.MSReader, J.adapter.readers.cif.MSCifInterface);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.cif.MSCifReader, []);
});
Clazz.overrideMethod (c$, "processEntry", 
function () {
var cr = this.cr;
if (cr.key.equals ("_jana_cell_commen_t_section_1")) {
this.isCommensurate = true;
this.commensurateSection1 = cr.parseIntStr (cr.data);
}});
Clazz.overrideMethod (c$, "processLoopBlock", 
function () {
var cr = this.cr;
if (cr.key.equals ("_cell_subsystem_code")) {
this.processSubsystemLoopBlock ();
return 1;
}if (!cr.key.startsWith ("_cell_wave") && !cr.key.contains ("fourier") && !cr.key.contains ("_special_func")) return 0;
if (this.modAverage) return -1;
if (cr.asc.currentAtomSetIndex < 0) cr.asc.newAtomSet ();
cr.parseLoopParameters (J.adapter.readers.cif.MSCifReader.modulationFields);
var tok;
while (cr.parser.getData ()) {
var ignore = false;
var id = null;
var atomLabel = null;
var axis = null;
var pt = [NaN, NaN, NaN];
var c = NaN;
var w = NaN;
var fid = null;
var n = cr.parser.getFieldCount ();
for (var i = 0; i < n; ++i) {
switch (tok = this.fieldProperty (cr, i)) {
case 40:
case 41:
case 42:
case 0:
case 4:
pt[0] = pt[1] = pt[2] = 0;
case 13:
case 19:
case 35:
case 43:
case 44:
case 45:
switch (tok) {
case 0:
id = "W_";
break;
case 4:
id = "F_";
break;
case 40:
case 41:
case 42:
fid = "?" + this.field;
pt[2] = 1;
continue;
case 43:
case 44:
case 45:
atomLabel = axis = "*";
case 13:
case 19:
case 35:
id = Character.toUpperCase (J.adapter.readers.cif.MSCifReader.modulationFields[tok].charAt (11)) + "_";
break;
}
id += this.field;
break;
case 46:
id = "J_O";
pt[0] = pt[2] = 1;
case 24:
if (id == null) id = "D_S";
case 30:
if (id == null) id = "O_0";
axis = "0";
case 11:
case 18:
case 33:
atomLabel = this.field;
break;
case 12:
if (this.modAxes != null && this.modAxes.indexOf (axis.toUpperCase ()) < 0) ignore = true;
axis = this.field;
break;
case 34:
axis = this.field.toUpperCase ();
break;
case 50:
case 48:
case 52:
case 20:
case 14:
case 36:
case 31:
pt[2] = 0;
case 1:
case 5:
case 25:
pt[0] = cr.parseFloatStr (this.field);
break;
case 8:
id += "_coefs_";
pt =  Clazz.newDoubleArray (this.modDim, 0);
pt[0] = cr.parseFloatStr (this.field);
break;
case 16:
case 22:
case 38:
pt[0] = cr.parseFloatStr (this.field);
pt[2] = 1;
break;
case 51:
case 21:
axis = "0";
case 2:
case 6:
case 9:
case 15:
case 17:
case 23:
case 37:
case 39:
case 32:
case 26:
case 47:
case 53:
case 49:
pt[1] = cr.parseFloatStr (this.field);
break;
case 3:
case 7:
case 10:
case 27:
pt[2] = cr.parseFloatStr (this.field);
break;
case 28:
c = cr.parseFloatStr (this.field);
break;
case 29:
w = cr.parseFloatStr (this.field);
break;
}
if (ignore || id == null || atomLabel != null && !atomLabel.equals ("*") && cr.rejectAtomName (atomLabel)) continue;
var d = 0;
for (var j = 0; j < pt.length; j++) d += pt[j];

if (Double.isNaN (d) || d > 1e10 || d == 0) continue;
switch (id.charAt (0)) {
case 'W':
case 'F':
break;
case 'D':
case 'O':
case 'U':
case 'J':
if (atomLabel == null || axis == null) continue;
if (id.equals ("D_S")) {
if (Double.isNaN (c) || Double.isNaN (w)) continue;
if (pt[0] != 0) this.addMod ("D_S#x;" + atomLabel, fid, [c, w, pt[0]]);
if (pt[1] != 0) this.addMod ("D_S#y;" + atomLabel, fid, [c, w, pt[1]]);
if (pt[2] != 0) this.addMod ("D_S#z;" + atomLabel, fid, [c, w, pt[2]]);
continue;
}id += "#" + axis + ";" + atomLabel;
break;
}
this.addMod (id, fid, pt);
}
}
return 1;
});
Clazz.defineMethod (c$, "addMod", 
 function (id, fid, params) {
if (fid != null) id += fid;
this.addModulation (null, id, params, -1);
}, "~S,~S,~A");
Clazz.defineMethod (c$, "processSubsystemLoopBlock", 
 function () {
var cr = this.cr;
cr.parseLoopParameters (null);
while (cr.parser.getData ()) {
this.fieldProperty (cr, 0);
var id = this.field;
this.addSubsystem (id, this.getSubSystemMatrix (cr, 1));
}
});
Clazz.defineMethod (c$, "getSubSystemMatrix", 
 function (cr, i) {
var m =  new JU.Matrix (null, 3 + this.modDim, 3 + this.modDim);
var a = m.getArray ();
var key;
var p;
var n = cr.parser.getFieldCount ();
for (; i < n; ++i) {
if ((p = this.fieldProperty (cr, i)) < 0 || !(key = cr.parser.getField (p)).contains ("_w_")) continue;
var tokens = JU.PT.split (key, "_");
var r = cr.parseIntStr (tokens[tokens.length - 2]) - 1;
var c = cr.parseIntStr (tokens[tokens.length - 1]) - 1;
a[r][c] = cr.parseFloatStr (this.field);
}
return m;
}, "J.adapter.readers.cif.CifReader,~N");
Clazz.defineMethod (c$, "fieldProperty", 
 function (cr, i) {
return ((this.field = cr.parser.getLoopData (i)).length > 0 && this.field.charAt (0) != '\0' ? cr.propertyOf[i] : -1);
}, "J.adapter.readers.cif.CifReader,~N");
Clazz.defineStatics (c$,
"WV_ID", 0,
"WV_X", 1,
"WV_Y", 2,
"WV_Z", 3,
"FWV_ID", 4,
"FWV_X", 5,
"FWV_Y", 6,
"FWV_Z", 7,
"FWV_Q1_COEF", 8,
"FWV_Q2_COEF", 9,
"FWV_Q3_COEF", 10,
"FWV_DISP_LABEL", 11,
"FWV_DISP_AXIS", 12,
"FWV_DISP_SEQ_ID", 13,
"FWV_DISP_COS", 14,
"FWV_DISP_SIN", 15,
"FWV_DISP_MODULUS", 16,
"FWV_DISP_PHASE", 17,
"FWV_OCC_LABEL", 18,
"FWV_OCC_SEQ_ID", 19,
"FWV_OCC_COS", 20,
"FWV_OCC_SIN", 21,
"FWV_OCC_MODULUS", 22,
"FWV_OCC_PHASE", 23,
"DISP_SPEC_LABEL", 24,
"DISP_SAW_AX", 25,
"DISP_SAW_AY", 26,
"DISP_SAW_AZ", 27,
"DISP_SAW_C", 28,
"DISP_SAW_W", 29,
"OCC_SPECIAL_LABEL", 30,
"OCC_CRENEL_C", 31,
"OCC_CRENEL_W", 32,
"FWV_U_LABEL", 33,
"FWV_U_TENS", 34,
"FWV_U_SEQ_ID", 35,
"FWV_U_COS", 36,
"FWV_U_SIN", 37,
"FWV_U_MODULUS", 38,
"FWV_U_PHASE", 39,
"FD_ID", 40,
"FO_ID", 41,
"FU_ID", 42,
"FDP_ID", 43,
"FOP_ID", 44,
"FUP_ID", 45,
"JANA_OCC_ABS_LABEL", 46,
"JANA_OCC_ABS_O_0", 47,
"DEPR_FD_COS", 48,
"DEPR_FD_SIN", 49,
"DEPR_FO_COS", 50,
"DEPR_FO_SIN", 51,
"DEPR_FU_COS", 52,
"DEPR_FU_SIN", 53,
"modulationFields", ["_cell_wave_vector_seq_id", "_cell_wave_vector_x", "_cell_wave_vector_y", "_cell_wave_vector_z", "_atom_site_fourier_wave_vector_seq_id", "_atom_site_fourier_wave_vector_x", "_atom_site_fourier_wave_vector_y", "_atom_site_fourier_wave_vector_z", "_jana_atom_site_fourier_wave_vector_q1_coeff", "_jana_atom_site_fourier_wave_vector_q2_coeff", "_jana_atom_site_fourier_wave_vector_q3_coeff", "_atom_site_displace_fourier_atom_site_label", "_atom_site_displace_fourier_axis", "_atom_site_displace_fourier_wave_vector_seq_id", "_atom_site_displace_fourier_param_cos", "_atom_site_displace_fourier_param_sin", "_atom_site_displace_fourier_param_modulus", "_atom_site_displace_fourier_param_phase", "_atom_site_occ_fourier_atom_site_label", "_atom_site_occ_fourier_wave_vector_seq_id", "_atom_site_occ_fourier_param_cos", "_atom_site_occ_fourier_param_sin", "_atom_site_occ_fourier_param_modulus", "_atom_site_occ_fourier_param_phase", "_atom_site_displace_special_func_atom_site_label", "_atom_site_displace_special_func_sawtooth_ax", "_atom_site_displace_special_func_sawtooth_ay", "_atom_site_displace_special_func_sawtooth_az", "_atom_site_displace_special_func_sawtooth_c", "_atom_site_displace_special_func_sawtooth_w", "_atom_site_occ_special_func_atom_site_label", "_atom_site_occ_special_func_crenel_c", "_atom_site_occ_special_func_crenel_w", "_atom_site_u_fourier_atom_site_label", "_atom_site_u_fourier_tens_elem", "_atom_site_u_fourier_wave_vector_seq_id", "_atom_site_u_fourier_param_cos", "_atom_site_u_fourier_param_sin", "_atom_site_u_fourier_param_modulus", "_atom_site_u_fourier_param_phase", "_atom_site_displace_fourier_id", "_atom_site_occ_fourier_id", "_atom_site_u_fourier_id", "_atom_site_displace_fourier_param_id", "_atom_site_occ_fourier_param_id", "_atom_site_u_fourier_param_id", "_jana_atom_site_occ_fourier_absolute_site_label", "_jana_atom_site_occ_fourier_absolute", "_atom_site_displace_fourier_cos", "_atom_site_displace_fourier_sin", "_atom_site_occ_fourier_cos", "_atom_site_occ_fourier_sin", "_atom_site_u_fourier_cos", "_atom_site_u_fourier_sin"],
"NONE", -1);
});
