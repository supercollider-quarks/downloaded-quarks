+ VGSound { 
	*comp { |fileKey, ampComp, rootPitch, octave| 
		VGSound.ampComps.put(fileKey, ampComp);
		if ([ampComp, rootPitch].every(_.notNil)) {
			VGSound.rootPitches.put(fileKey, rootPitch + octave);
		};
	}
	
	*tryComp { |fileKey, ampComp, rootPitch=0, octave=60, repeats=4, wait=1|
		Tdef(\VGSound_tryComp, { 
			var bufnum = VGSamp.bufenv.envir[fileKey].bufnum;
		repeats.do { 
			(instrument: \vgSampFix, bufnum: bufnum, amp: 0.1, ampComp: ampComp, dur: wait * 4).play;
			wait.wait;
			(instrument: \vgAmpTest, amp: 0.1, midinote: rootPitch + octave, dur: wait * 4).play;
			wait.wait;
		} }).play;
	}

	*orderOfPartials { |partials, freq=0.3, ring=0.4, amp=0.3, attack=0|
	
		var weights = [freq, ring, amp, attack];
		var weightedVals  = partials.collect { |list| (list ** weights).product };
		var order = weightedVals.order({ |a, b| a > b });
		^order;
	}
	
	*findRefPartialFor {  |laras, instKey, noteKey, verbose=false|

		var event = VGSound.eventFor((laras: laras, instKey: instKey, noteKey: noteKey));
		var node = event[\node];
		
		var scaleToneFreq = VGScale.scales[laras][noteKey];
		
		var partialPitches, scaleTonePitch, deltas, order, partIndex; 
		
		if (scaleToneFreq.isNil) { 
			"VGSound: no scaleToneFreq for %.\n\n".postf([laras, instKey, noteKey]);
			^nil;
		};
		if (node.isNil) { 
			"VGSound: no node for %.\n\n".postf([laras, instKey, noteKey]);
			^nil;
		};
		
		scaleTonePitch = scaleToneFreq.cpsmidi; 
		partialPitches = (node.baseValues.first ? []).cpsmidi;
		deltas = (partialPitches - scaleTonePitch);
		order = deltas.abs.order;
		partIndex = order.first;
		
		if (verbose) { 
			"   % - % - %: % Hz = % cents - diff: % cents.\n"
				.postf(*[laras, instKey, noteKey, 
					scaleToneFreq, 
					(scaleTonePitch * 100), 
					(deltas[partIndex] * 100)].round(0.01)
				);
		}
		
		^[partIndex, node];
	}

	*suggestComp { |laras, instKey, noteKey, baseAmp=2, weights, index=0| 
	
		var event = VGSound.eventFor((laras: laras, instKey: instKey, noteKey: noteKey));
		var node, baseVals, order;
		var pitch, note=0, octave=5, amp=1; 

		node = event[\node];
		if (node.notNil) { 
			baseVals = node.baseValues;
			order = this.orderOfPartials(baseVals.flop, *(weights ? [0.3, 0.4, 0.3, 0]));
			pitch = baseVals.first[order[index]].cpsmidi;
			note = pitch % 12; 
			octave = pitch trunc: 12; 
			amp = baseVals[2].sum;	// maybe too simple 

		};
		^"VGSound.tryComp( %, %, %, %);".format(
			event.fileKey.asCompileString, 
			(baseAmp / amp).round(0.01), 
			note.round(0.001), 
			octave).postln;
		
	}
}