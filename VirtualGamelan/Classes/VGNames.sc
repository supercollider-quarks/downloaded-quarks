VGNames { 
	classvar <instGroupNames, <instGroupNamesMixer, <instNames, <instNamesMixer, <instNamesScGraph;
		
	*initClass { 

		instGroupNames = ['kendhang','interpunktion', 'balungan', 'panerusan'];

		instNames = [
			'Kendhang',
			'GongAgeng', 'GongSuw', 'Kempul', 'Kenong', 'Kethuk', 'Kempyang', 
			'Slenthem', 'DemungA', 'SaronA', 'SaronB', 'PekingA',
			'BonangBar', 'BonangPan', 'GenderBar', 'GenderPan'
		];

		instGroupNamesMixer = ['  group 1 kendhang','  group 2 interpunkt', '  group 3 balungan', '  group 4 panerusan'];

		instNamesMixer = [
			' kendhang', //mix bus1
			'     gong', '  suwukan', '   kempul', '   kenong', '   kethuk', ' kempyang', //mix bus2
			'  slenthem', '  demung', '   saron 1', '   saron 2', /* 'saron wayangan', */ '  peking 1', /* 'peking 2',*/ //mix bus3
			'  bonang _barung', '  bonang panerus' , '  gender _barung', '  gender panerus' /* , 'gambang' */ //mix bus4
		];
		instNamesScGraph = [
			'kendhang', //mix bus1
			'gong', 'suwukan', 'kempul', 'kenong', 'kethuk', 'kempyang', //mix bus2
			'slenthem', 'demung', 'saron 1', 'saron 2', /* 'saron wayangan', */ 'peking 1', /* 'peking 2',*/ //mix bus3
			'bonang barung', 'bonang panerus' , 'gender barung', 'gender panerus' /* , 'gambang' */ //mix bus4
		]; 
	}
	
	*instNameMixer { |instName| 
		var nameIndex = instNames.indexOf(instName); 
		var butName = instNamesMixer[nameIndex];
		
		^butName
	}

	*instNameScGraph { |instName| 
		var nameIndex = instNames.indexOf(instName); 
		var butName = instNamesScGraph[nameIndex];
		
		^butName
	}
	
	*instNameByGraphName { |instNameGraph| 
		var nameIndex = instNamesScGraph.indexOf(instNameGraph);
		^if (nameIndex.notNil) { instNames[nameIndex] };
	}

}
