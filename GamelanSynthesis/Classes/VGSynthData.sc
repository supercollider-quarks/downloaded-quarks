VGSynthData {
	classvar <text, <specList, <sonatorSpecs; 

	*loadData { |path|	// put all spec in file into flat, unique name dict of specs.
					// lets hope the file format holds...
						
		var sampleSetPrefixLength = VGTuning.sampSetPrefix.size;
		text = TabFileReader.read(path);
		if(text.isNil) { postln("couldn't open file:" + path.cs) };
		text = text.delimit(_ == [""]); 
		
		sonatorSpecs = (); 
		
		specList = text.drop(1).reject(_.isEmpty).do { |sonatorSpec, i|
			var sonatorKey = sonatorSpec.first.first
				.drop(sampleSetPrefixLength)
				.asSymbol;
			var data = sonatorSpec.drop(2);
			var freqs, levels, phases, attacks, ringtimes;
			var partialDict, partials;
			
			#freqs, levels, phases, attacks, ringtimes = data.collect { |line, j| 
				line.first.split($,).drop(-1).collect(_.interpret);
			}; 
				// missing last value
		//	ringtimes = ringtimes.clipExtend(freqs.size);
			
			partials = [ 	
				freqs, 
				ringtimes * 0.001, // in seconds
				levels.dbamp, 	// and linear amp.
				attacks * 0.001
			];
			
			sonatorSpecs.put(sonatorKey, partials);
		//	sonatorSpecs.put(sonatorKey, (partials: partials));
		};
		
		"VGSynthData: loaded data for % sonators.\n\n".postf(sonatorSpecs.size);
	}
}