var stage;


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

	ligandRepr = structComp.addRepresentation('ball+stick', {
		color : 'element',
		scale : 3.0,
		aspectRatio : 1.3,
		sele : 'hetero and not (water or ion)'
	});

	waterRepr = structComp.addRepresentation('licorice',{
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
