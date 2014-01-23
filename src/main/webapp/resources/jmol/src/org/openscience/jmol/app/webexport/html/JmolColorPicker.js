/* Jmol Simple JavaScript Color Picker
 by Jonathan Gutow
V1.2
June 17, 2010

requires
   Jmol.js

Usage
Where ever you want a popup color picker box include a script like

<script type="text/javascript">
var scriptStr2 = 'select carbon; color atom $COLOR$;';
jmolColorPickerBox(scriptStr2, "rgb[100,100,100]", "colorBox1", "0");
</script>

The only function that will not change name or syntax is jmolColorPickerBox(scriptStr, rgb, boxIdStr,  appletId).

USE OTHER FUNCTIONS IN THE JAVASCRIPT LIBRARY AT YOUR OWN RISK.
All parameters are strings although appletId could potentially be a number, but it is used to make a string.
  scriptStr should contain $COLOR$ where you wish the color string to be passed to Jmol in the script you provide.
  rgb is the browser standard 0-255 red-green-blue values specified as an array [red, green, blue] default = [127,127,127] a dark grey.
  boxIdStr should be a string that is unique to the web document, if not provided it will be set to colorBoxJ, J=0, 1, 2... in the order created.
  appletId is the standard Jmol id of applet you want the colorpicker to send the script to.  Default = "0".
>>>>Advanced use<<<<<<<<
To have the colorPickerBox pass the picked color to a function of your own so that you can modify the script after the colorBox 
  has been defined, you can pass an array in place of scriptStr.  This behaves much the way functions in Jmol.js do.  The array
  must have the following format [yourFunctionName, yourParam1, yourParam2,...]:
      yourFunctionName should not be in quotes, just the exact character sequence used to name your function.
      yourParamX can be anything you want.
      
      This array should be a variable with global scope on the page.  It is suggested that you declare and populate it with
      default values in the header of the page.
      
  The declaration of your function must be exactly (choose your own name for the function and variables):
  function yourFunctionName(rgbCodeStr, yourArray, appletID)
      rgbCodeStr is the rgb code string to pass to Jmol as part of the script command.  Make sure to put spaces on either side
          when adding it to the scriptStr.
      yourArray should be your global array, which you can update based on your own criteria.  Remember that element 0 is the
          name of your function.
      appletID is the applet number of string name that should be passed through jmolScript type functions to make sure that the
          correct applet gets the script.
>>>>>>End Advanced Use<<<<<<<<<<

*/

//globals and their defaults

var jmolColorPickerStatus = {
    lastPicked: '', //last picked color...not used at present
    funcName: '', //where to pass to next after _jmolColorPickerPickedColor()
    passThrough: '' //name of the global variable or structure containing information to be passed
    }

var jmolColorPickerBoxes=new Array();//array of _jmolColorBoxInfo

function _jmolColorBoxInfo(boxID, appletID, scriptStr){//used when using a predefined colorPickerBox
    this.boxID=boxID;
    this.appletID=appletID; //applet ID
    this.scriptStr=scriptStr; //script with $COLOR$ where the color should be placed.(((tentatively also a array to pass a function))).
    }

function _jmolChangeClass(someObj,someClassName) {
    someObj.setAttribute("class",someClassName);
    someObj.setAttribute("className",someClassName);  // this is for IE
}

//Build the ColorPicker Div.

// detect if browser supports data:URI   (IE6 & IE7 do not)
    var dataURIsupported = true;
    var testImg64 = new Image();
    testImg64.onload = testImg64.onerror = function() {
        if(this.width != 1 || this.height != 1) { dataURIsupported = false; }
    }
    testImg64.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";

function _jmolMakeColorPicker(){
    JmolColorPickerDiv = document.getElementById("JmolColorPickerDiv");
    if(! JmolColorPickerDiv){
        var colorPickerCSS = document.createElement('style');
        colorPickerCSS.type = 'text/css';
        CSSStr ='.JmolColorPicker_vis {border-style:solid;border-width:thin;clear:both;display:block;overflow:visible;position:absolute;margin-left:-52px;width:104px;z-index:2;}'
        CSSStr +='.JmolColorPicker_hid {height:0;min-height:0;display:none;overflow:hidden;z-index:0;}';
        if (colorPickerCSS.styleSheet) { // IE
            colorPickerCSS.styleSheet.cssText = CSSStr;
        } else { // W3C
            content = document.createTextNode(CSSStr); 
            colorPickerCSS.appendChild(content);
        }
        document.getElementsByTagName('head')[0].appendChild(colorPickerCSS);
        JmolColorPickerDiv = document.createElement("div");
        JmolColorPickerDiv.setAttribute("id", "JmolColorPickerDiv");
        _jmolChangeClass(JmolColorPickerDiv,"JmolColorPicker_hid");
        }
   var rgbs=[[255,0,0]
       ,[255,128,0]
       ,[255,255,0]
       ,[128,255,0]
       ,[0,255,0]
       ,[0,255,128]
       ,[0,255,255]
       ,[0,128,255]
       ,[0,0,255]
       ,[128,0,255]
       ,[255,0,255]
       ,[255,0,128]
       ,[255,255,255]
   ];
   var hues=[[190,100],
             [175,95],
             [150,90],
             [135,80],
             [100,68],
             [85,55],
             [70,40],
             [60,30],
             [50,20],
             [35,0]
     ];
    var tempwidth = 8*(rgbs.length);
    var htmlStr = '<div id="JmolColorPickerHover" style="font-size:2px;width:'+tempwidth+'px;text-align:right;background-color:white;cursor:default;">';
    if (dataURIsupported) {
        htmlStr += '<image id="JmolColorPickerCancel" onclick="_jmolColorPickerPickedColor(\'cancel\');" src="data:image/bmp;base64,Qk3CAQAAAAAAADYAAAAoAAAACwAAAAsAAAABABgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAdnZ2j4+PoKCgqampqampoKCgj4+PAAAAAAAAAAAAAAAAAAAAAAAAsbGxwsLCysrKysrKwsLCAAAAAAAAAAAAAAAAZWVlAAAAAAAAAAAA29vb5OTk5OTkAAAAAAAAAAAAj4+PAAAAdnZ2oKCgAAAAAAAAAAAA9PT0AAAAAAAAAAAAwsLCoKCgAAAAfn5+qampysrKAAAAAAAAAAAAAAAAAAAA5OTkysrKqampAAAAfn5+qampysrK5OTkAAAAAAAAAAAA9PT05OTkysrKqampAAAAdnZ2oKCgwsLCAAAAAAAAAAAAAAAAAAAA29vbwsLCoKCgAAAAZWVlj4+PAAAAAAAAAAAA5OTkAAAAAAAAAAAAsbGxj4+PAAAATExMAAAAAAAAAAAAwsLCysrKysrKAAAAAAAAAAAAdnZ2AAAAAAAAAAAAAAAAj4+PoKCgqampqampoKCgAAAAAAAAAAAAAAAAAAAAAAAATExMZWVldnZ2fn5+fn5+dnZ2ZWVlAAAAAAAAAAAA">';
    } else {
        htmlStr += '<span id="JmolColorPickerCancel" onclick="_jmolColorPickerPickedColor(\'cancel\');" style="font-size:10px; padding:0 2px; background-color:#A0A0A0; font-family:Verdana, Arial, Helvetica, sans-serif;">X</span>';
    }
    htmlStr += '</div>';	 
    htmlStr += '<table cellspacing="0" cellpadding="0" border="0" style="font-size:2px; cursor:default;"><tbody>';
    for (j = 0; j < hues.length;j++){
    htmlStr += '<tr>'
    var f = (hues[j][0])/100.0;
       for (k = 0; k < rgbs.length; k++){
       if(rgbs[k][0]==255&&rgbs[k][1]==255&&rgbs[k][2]==255) f =(hues[j][1])/100.0;; 
       r = Math.min(Math.max(Math.round(rgbs[k][0] * f),Math.round(255-rgbs[k][0])*(f-1)^2),255);
       g = Math.min(Math.max(Math.round(rgbs[k][1] * f),Math.round(255-rgbs[k][1])*(f-1)^2),255);
       b = Math.min(Math.max(Math.round(rgbs[k][2] * f),Math.round(255-rgbs[k][2])*(f-1)^2),255);
          htmlStr +='<td style="background-color: rgb(' + r + "," + g + ","+ b + ');">';
          htmlStr +='<div style="width: 8px; height: 8px;" onclick=\'_jmolColorPickerPickedColor("rgb('+r+','+g+','+b+')");\' ';
          htmlStr +='onmouseover=\'_jmolColorPickerHoverColor("rgb('+r+','+g+','+b+')");\'></div>';
          htmlStr +='</td>';
       }//for k
   htmlStr +='</tr>';
   }//for j
   htmlStr += '</tbody></table>'; 
    content = document.createTextNode("loading color picker...");
    JmolColorPickerDiv.appendChild(content);
    JmolColorPickerDiv.innerHTML = htmlStr;
    return(JmolColorPickerDiv);   
}

// IE6 puts the SELECT control on top of the popup colorpicker DIV, so we trick that:
var IEversion = 999;
if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)) { //test for MSIE x.x;
    IEversion=new Number(RegExp.$1); // capture x.x portion and store as a number
}

function _jmolColorPickerPickedColor(colorStr){
    _jmolChangeClass(document.getElementById('JmolColorPickerDiv'), "JmolColorPicker_hid");
    if(colorStr!='cancel'){
        var evalStr = ''+ jmolColorPickerStatus.funcName+'("'+colorStr+'",'+ jmolColorPickerStatus.passThrough+');';
        eval(evalStr);
    }
    if (IEversion<7) { document.getElementById("StereoMode0").style.visibility='visible'; }
}

function _jmolColorPickerHoverColor(colorStr){
    document.getElementById("JmolColorPickerHover").style.background = colorStr;
}

function _jmolPopUpPicker(whereID, funcName, passThrough){
    var pickerDiv = document.getElementById("JmolColorPickerDiv");
    if (!pickerDiv) {//make a new picker
        JmolColorPickerDiv =  _jmolMakeColorPicker();
        document.body.appendChild(JmolColorPickerDiv);
        pickerDiv = document.getElementById("JmolColorPickerDiv");
        }
    jmolColorPickerStatus.funcName = funcName;
    jmolColorPickerStatus.passThrough = passThrough;
    var where = document.getElementById(whereID);
    where.appendChild(pickerDiv);
    _jmolChangeClass(pickerDiv,"JmolColorPicker_vis");
    if (IEversion<7) { document.getElementById("StereoMode0").style.visibility='hidden'; }
}


function jmolColorPickerBox(scriptStr, startColor, boxID, appletID){
    if (!appletID) appletID = "0";
    var boxNum = jmolColorPickerBoxes.length;
    if (!boxID) boxID = 'colorBox'+boxNum;
    if (!startColor) startColor = [127,127,127];
    var presentColor = 'rgb('+startColor[0]+','+startColor[1]+','+startColor[2]+')';
    jmolColorPickerBoxes[boxNum]= new _jmolColorBoxInfo(boxID, appletID, scriptStr);  
    var boxDiv = document.createElement("div");
    boxDiv.setAttribute("id",boxID);
    content = document.createTextNode("building color box...");
    boxDiv.appendChild(content);
    boxDiv.style.background=presentColor;
    boxDiv.style.height='14px';
    boxDiv.style.width='28px';
    htmlStr = '<table style="font-size:0px; cursor:default;" cellspacing="0" cellpadding="0" border="1" onclick=\'_jmolPopUpPicker(';
    htmlStr += '"'+boxID+'","_jmolColorBoxUpdate",'+boxNum+');\' ';
    htmlStr += '><tbody>';
    htmlStr += '<tr><td><div style="height: 12px; width: 12px;"></div></td><td>';
    var boxArrowName = 'colorBoxArrow'+boxNum;
    if (dataURIsupported) {
        // up arrowhead:   "data:image/bmp;base64,Qk3mAQAAAAAAADYAAAAoAAAACwAAAAwAAAABABgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIAAAAAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIAAAAyMjIyMjIyMjIAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIAAAAAAAAAAAAyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAA"
        // down arrowhead: "data:image/bmp;base64,Qk3mAQAAAAAAADYAAAAoAAAACwAAAAwAAAABABgAAAAAALABAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIAAAAAAAAAAAAyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIAAAAyMjIyMjIAAAAAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAA"
        htmlStr += '<image id="'+ boxArrowName+'" src="data:image/bmp;base64,Qk3mAQAAAAAAADYAAAAoAAAACwAAAAwAAAABABgAAAAAALABAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIAAAAAAAAAAAAyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIAAAAAAAAAAAAAAAAAAAAyMjIyMjIyMjIAAAAyMjIyMjIAAAAAAAAAAAAAAAAAAAAAAAAAAAAyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAAyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIAAAA">';
    } else {
        htmlStr += '<span id="'+ boxArrowName+'" style="font-size:10px; padding:0 2px; background-color:#A0A0A0; font-family:Verdana, Arial, Helvetica, sans-serif;">V</span>';
    }
    htmlStr += '</td></tr></tbody></table>';
    boxDiv.innerHTML = htmlStr;
    scripts = document.getElementsByTagName("script");
    scriptNode = scripts.item(scripts.length-1);
    parentNode = scriptNode.parentNode;
    parentNode.appendChild(boxDiv);
}


function _jmolColorBoxUpdate(pickedColor, boxNum){
    document.getElementById(jmolColorPickerBoxes[boxNum].boxID).style.background = pickedColor;
    _jmolChangeClass(document.getElementById('JmolColorPickerDiv'), "JmolColorPicker_hid");
    var rgbCodes = pickedColor.replace(/rgb/i,'').replace('(','[').replace(')',']');
    if (typeof(jmolColorPickerBoxes[boxNum].scriptStr) == "object"){
        jmolColorPickerBoxes[boxNum].scriptStr[0](rgbCodes,jmolColorPickerBoxes[boxNum].scriptStr, jmolColorPickerBoxes[boxNum].appletID);
    }else {
    	var scriptStr = jmolColorPickerBoxes[boxNum].scriptStr.replace('$COLOR$', rgbCodes);
    	jmolScript(scriptStr,jmolColorPickerBoxes[boxNum].appletID);
    }
}

