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
    	//console.log("n was pressed, drawing next surface");
    	nextSurface();
    }

};

function onInit() {
	stage = new NGL.Stage("viewport");
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

	waterRepr = structureComponent.addRepresentation('licorice',{
		color : 'element',
		sele  : 'water'
	});

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
	if (surfaceOn) {
		currentChainSurfIndex++;
		if (currentChainSurfIndex==chains.length) currentChainSurfIndex = 0;
		showSurface(chains[currentChainSurfIndex]);
	}
}

function showSurface(chain) {
	surf = structureComponent.addRepresentation('surface', {
		colorScheme: 'bfactor',
		//opacity: 0.7,
		colorDomain: [2.58,0],
		colorScale: 'roygb',
		//surfaceType: 'sas',
		sele: ':'+chain
	});
}
