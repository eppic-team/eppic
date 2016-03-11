function initRepr(structComp) {

	structureComponent = structComp;

	ligandRepr = structComp.addRepresentation('ball+stick', {
		color : 'element',
		scale : 3.0,
		aspectRatio : 1.3,
		sele : 'hetero and not (water or ion)' 
	});			

	cartoonRepr = structureComponent.addRepresentation('cartoon', {
		colorScheme: 'chainindex',
		colorScale : 'RdYlBu',
		aspectRatio : 5,
		quality : 'medium'
	});

	stage.centerView();

}