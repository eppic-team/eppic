function default_keypress(stage, e) {
	e = e || window.event;

	if (e !== undefined && e.key == "s") { // 115 is s
		console.log("s was pressed: small screenshot (1x)");
		screenshot(stage,1);
	}

	if (e !== undefined && e.key == "m") { // 109 is m
		console.log("m was pressed: medium screenshot (2x)");
		screenshot(stage,2);
	}

	if (e !== undefined && e.key == "l") { // 108 is l
		console.log("l was pressed: large screenshot (3x)");
		screenshot(stage,3);
	}

	if (e !== undefined && e.key == "x") { // 120 is x
		console.log("x was pressed: xtra large screenshot (4x)");
		screenshot(stage,4);
	}

};

/**
 * A function to trigger a png transparent screenshot with given factor
 * @param factor the number of times the current canvas size will be multiplied by
 */
function screenshot(stage,factor) {
	if (stage!==undefined) {
		stage.makeImage({factor: factor, antialias: true, trim: false, transparent: true}).then(function(blob) { NGL.download(blob) } )
	}
}

// convert from a 6-digit hex string to [r,g,b] array with values in [0,1)
function hexToRGB(hex) {
	var hexVal = parseInt(hex,16);
	var r = hexVal >> 16;
	var g = hexVal >> 8 & 0xFF;
	var b = hexVal & 0xFF;
	return [r/256.,g/256.,b/256.];
}