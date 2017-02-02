var stage;
var structureComponent;
var surf;
var surfaceOn = false;
var currentChainSurfIndex = 0;


document.onkeypress = function (e) {
    e = e || window.event;

    if (e !== undefined && e.which == 112) { // 112 is p
    	console.log("p was pressed, toggling surface");
    	toggleSurface();
    }
    
    if (e !== undefined && e.which == 110) { // 110 is n
    	console.log("n was pressed, drawing next surface");
    	nextSurface();
    }
    
	if (e !== undefined && e.which == 115) { // 115 is s
		console.log("s was pressed: small screenshot (1x)");
		screenshot(1);
	}

	if (e !== undefined && e.which == 109) { // 109 is m
		console.log("m was pressed: medium screenshot (2x)");
		screenshot(2);
	}

	if (e !== undefined && e.which == 108) { // 108 is l
		console.log("l was pressed: large screenshot (3x)");
		screenshot(3);
	}

	if (e !== undefined && e.which == 120) { // 120 is x
		console.log("x was pressed: xtra large screenshot (4x)");
		screenshot(4);
	}


};

/**
 * A function to trigger a png transparent screenshot with given factor
 * @param factor the number of times the current canvas size will be multiplied by
 */
function screenshot(factor) {
	if (stage!==undefined) {
		stage.makeImage({factor: factor, antialias: true, trim: false, transparent: true}).then(function(blob) { NGL.download(blob) } )
	}
}

function onInit() {
	stage = new NGL.Stage("viewport");
	stage.setParameters({ backgroundColor:"white", theme:"light"} );
	stage.loadFile(inputFile, {
		defaultRepresentation : false
	}).then(function(structComp) {
		initRepr(structComp)
	});

}


function initRepr(structComp) {

	structureComponent = structComp;

	ligandRepr = structureComponent.addRepresentation('ball+stick', {
		color : 'element',
		scale : 3.0,
		aspectRatio : 1.3,
		sele : 'hetero and not (water or ion)'
	});

	/*waterRepr = structureComponent.addRepresentation('licorice',{
		color : 'element',
		sele  : 'water'
	});*/

	for (i=0; i<chains.length; i++) {
		structureComponent.addRepresentation('cartoon', {
			color : colors[i],
			sele : ":"+chains[i],
			aspectRatio : 5,
			quality : 'medium'
		});
	}
	
	// from here the interface stuff that's only valid if interface vars are defined
	
	if (typeof colorCore1!= 'undefined' && typeof seleCore1!= 'undefined') { 
		core1Repr = structureComponent.addRepresentation('licorice', {
			color : colorCore1,
			sele: seleCore1
		});
	}

	if (typeof colorCore2!= 'undefined' && typeof seleCore2!= 'undefined') {
		core2Repr = structureComponent.addRepresentation('licorice', {
			color : colorCore2,
			sele: seleCore2
		});
	}

	if (typeof colorRim1!= 'undefined' && typeof seleRim1!= 'undefined') {
		rim1Repr = structureComponent.addRepresentation('licorice', {
			color : colorRim1,
			sele: seleRim1
		});
	}

	if (typeof colorRim2!= 'undefined' && typeof seleRim2!= 'undefined') {
		rim2Repr = structureComponent.addRepresentation('licorice', {
			color : colorRim2,
			sele: seleRim2
		});
	}
	
	stage.centerView();

}

function toggleSurface() {

	if (surf === undefined) {
		showSurface(chains[currentChainSurfIndex])
		surfaceOn = true;
	} else {
		if (surfaceOn) {
			surf.setVisibility(false);
			surfaceOn = false;
		} else {
			surf.setVisibility(true);
			surfaceOn = true;
		}


	}
}

function nextSurface() {
	if (surfaceOn && chains.length>1) {
		surf.dispose();
		currentChainSurfIndex++;
		if (currentChainSurfIndex==chains.length) currentChainSurfIndex = 0;
		showSurface(chains[currentChainSurfIndex]);
	}
}

function showSurface(chain) {
	surf = structureComponent.addRepresentation('surface', {
		colorScheme: 'bfactor',
		//opacity: 0.7,
		colorDomain: [maxEntropy, 0],
		colorScale: 'roygb',
		//surfaceType: 'sas',
		sele: ':'+chain+" and polymer" // we need to select polymer only or otherwise water and ligands are also displayed
	});
}


// convert from a 6-digit hex string to [r,g,b] array with values in [0,1)
function hexToRGB(hex) {
	var hexVal = parseInt(hex,16);
	var r = hexVal >> 16;
	var g = hexVal >> 8 & 0xFF;
	var b = hexVal & 0xFF;
	return [r/256.,g/256.,b/256.];
}