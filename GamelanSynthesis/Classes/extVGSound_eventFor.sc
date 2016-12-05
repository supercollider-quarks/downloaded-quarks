+ VGSound { 	

	*eventFor { |inval| 
		var scalename, instKey, noteKey, synthDef;
		var allPBSettings, myPBSettings, failureEvent, pbSynthDef;
		var synthEngine, addNumPartialsToSynthDef;
		var synthSpecs, outval; 

		if (inval.isNil) { 
			"VGSound-eventFor: no event passed in.\n\n".postvg;
			^(type: \rest)
		};
			// rests are just rests
		if (inval[\type] == \rest) { 
			"VGSynth-eventFor: inval is resting.".postln 
			^inval 
		};
		 
		 	// else my event type.
		inval.put(\type, this.eventType);
		
			// inval is an event that should play: 
		instKey = inval[\instKey];
		noteKey = inval[\noteKey];
		
		failureEvent = inval.copy.put(\type, \rest);
		
		if ([instKey, noteKey].any(_.isNil)) { 
			"VGSound-eventFor: no instKey or noteKey given in %.\n\n".postf(inval);
			^failureEvent
		};
		
		allPBSettings = VGSound.playbackSettings;
		myPBSettings = allPBSettings[instKey];
		
		if (myPBSettings.notNil) { 
			// myPBSettings.postcs; 
			
				// mute/solo negotiations:
			if (myPBSettings[\mute] or: { allPBSettings.any(_[\solo]) and: myPBSettings[\solo].not }) { 
				"VGSound-eventFor: others soloed, or me muted.".postln;
				^inval.put(\type, \rest)
			};
			pbSynthDef = myPBSettings[\synthDef];
		} { 
			"VGSound-eventFor: no playbackSettings for instKey %.\n\n".postf(instKey); 
		};
		
		synthDef = inval[\synthDef] ? pbSynthDef ? VGSynth.defaultDef; // no partials number attached yet;
		scalename = inval.atDefinedKeys(\laras ? \tuning) ? VGTuning.laras;
		
		if (Set[ \Ketipung, \Kendhang, \KendhangGdh, \Ciblon ].includes(instKey)) { 
			scalename = \neutral;
			synthDef = \vgSampFix;			
		};
				
		synthEngine = synthEngines[synthDef.asSymbol];
		addNumPartialsToSynthDef = (synthEngine != VGSamp);
		
		if (synthEngine.isNil) {
			("No Synth engine for synthDef:" + synthDef + "!").warn;
			^failureEvent
		};
		
		synthSpecs = this.specsFor(*[instKey, noteKey, scalename, synthDef, addNumPartialsToSynthDef]);
		
		if (synthSpecs.isNil) { 
			("VGSynth: no synthSpecs found for: " + [instKey, noteKey, scalename, synthDef, addNumPartialsToSynthDef] + ".").postvg;
			^failureEvent
		};
		
		if (synthSpecs.isEmpty) { 
			("VGSynth: synthSpecs is empty: " + [instKey, noteKey, scalename, synthDef, addNumPartialsToSynthDef] + ".").postvg;
			^failureEvent
		};

		if (synthSpecs.includes(nil)) { 
			("VGSynth: synthSpecs includes nil: " + [instKey, noteKey, scalename, synthDef, addNumPartialsToSynthDef] + ".").postvg;
			^failureEvent
		};
		
		if (synthSpecs.isArray) { 
			if (synthSpecs.includes(nil)) { 
				("VGSynth: one synthspec missing...").postvg;
				synthSpecs = synthSpecs.select(_.notNil);
			};
			synthSpecs = synthSpecs.flopDict ?? { failureEvent }; // for graceful failure.
		};
		
		
		outval = inval.copy; 
		outval.putAll(synthSpecs);

		if(outval.at(\type) == \rest) { 
			"VGSound-eventFor: outval is resting.".postvg 
		};
		
	// OVERRIDES AMP, OUT, PAN
		if (myPBSettings.notNil) { 
			[\out, \pan, \level].do { |key| 	outval.put(key, myPBSettings[key]) };
		};
		
		outval[\amp] = (outval[\amp] ? 0.1) * (outval[\level] ? 1.0);
		
		^outval

	} 
	
	*specsFor { |instKey, noteKey, laras, synthDefName, addPartNum=false| 
		var noteSymbol, instDict; 
		var noteDesc, fileKey, buffer, bufnum, busses, ampComp, rootPitch; 
		var tree, numPartials, node; 
		
		var args = [instKey, noteKey, laras, synthDefName, addPartNum];
		
		if (args.any(_.isKindOf(Array))) { 
			^args.flop.collect(this.specsFor(*_)); 
		};
		
		instDict = VGTuning.tunings[laras].atDefinedKeys(instKey); 

		if (instDict.isNil) { 
			if (verbose, { 
			"VGSound: no instrument found for instKey: % and noteKey: %.".format(
				instKey.asCompileString, noteKey.asCompileString).postvg; 
			});
			^nil
		};
		
		noteDesc = instDict[noteKey];
			
		if (noteDesc.isNil) { 
			if (verbose, { 
			("VGSound: no note found for instKey: % and noteKey: %.").format(
				instKey.asCompileString, noteKey.asCompileString).postvg; 
			});
			^nil
		};

		fileKey = noteDesc[\rawname]; 
			// ampComp should be in notedesc really!
		ampComp = VGSound.getAmpComp(fileKey); 
		rootPitch = VGSound.getRootPitch(fileKey); 
		
		tree = VGTuning.mulTrees[laras]; 
		
		if (tree.notNil and: { #[].includes(instKey).not }) { 
			busses = tree.bussesFor(
				[instKey, noteKey], 
				[\ebus, \ibus, \sbus, \rbus, \bbus], 
				true
			);
			
			numPartials = (busses.last.numChannels div: VGTuning.busKeys.size);
			
			if (addPartNum) {			
				synthDefName = (synthDefName ++ numPartials);
			};
			node = tree.nodeAt(instKey, noteKey);
		};
				
//			///////////// VGSamp needs these: 		
		if (synthEngines[synthDefName] == VGSamp and: VGSamp.loaded) {  
		
			buffer = VGSamp.bufenv.envir.atDefinedKeys(fileKey);
			if (buffer.isNil) { 
				postvg("VGSamp: no buffer found for fileKey:" + fileKey + "!");
				^nil
			};
		
			bufnum = buffer.bufnum;
		}; 
		
		^(	fileKey: fileKey, 
			buffer: buffer, 
			bufnum: bufnum, 
			ampComp: ampComp, 
			numPartials: numPartials,
			instrument: synthDefName, 
			node: node
		).putPairs(busses);
	}
}