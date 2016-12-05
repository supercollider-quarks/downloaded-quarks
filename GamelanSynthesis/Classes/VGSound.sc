VGSound { 
	classvar <synthEngines, <server; 
	classvar <>verbose = true; 
	classvar <>eventType = \vg_sound;
	classvar <>playbackSettings;
	classvar <>loaded = false;
	
	classvar <ampComps, <rootPitches, <refPartialIndices; 
	
	*initClass { 
		synthEngines = ();
		this.makeDefaultEvent;
				
		ampComps = ();	// should maybe eventually go into VGTuning.tunings 
						// ... where \rawname is. who knows.
		rootPitches = ();
		refPartialIndices = (); 
		
		this.initPlaybackSettings;
	}
	
	*initPlaybackSettings {
		playbackSettings = ();
		VGTuning.instGroupings.flat.do { |instName|
			playbackSettings[instName] =  
				(
					level: 1, 
					pan: 0, 
					out: 0,
					synthDef: \vgSampDyn, 
					mute: false, 
					solo: false
				);
		}
	}
	
	*getAmpComp { |fileKey| ^ampComps[fileKey] ? 1 }
	*getRootPitch { |fileKey| ^rootPitches[fileKey] ? 958 }
	*getRefPartial { |fileKey| ^refPartialIndices[fileKey] ? 0 }

	
	*startUp { |doneFunc, initSynth=true, loadSamp=true| 

	

		var s = server = server ? Server.default;


		"************* Starting VGSound: ****************\n".postln; 
				// we need around 64k bus channels
		s.options.numControlBusChannels = 2 ** 16; 
		VGTuning.synthDataFixFunc = {};
		VGTuning.free; // free old, if exists

		

		fork { 
			0.2.wait; 

			

			Server.default.reboot;

	

			s.waitForBoot { 
				1.wait; 
				"VGTuning initialises (may take a while)..."; 
				1.wait; 

				

				VGTuning.init;
				1.wait; 
				"VGTuning initialised."; 
				0.1.wait;

				

				this.initPlaybackSettings;

				

				if (initSynth) { 
				VGSynth.initSynthDefs({ 
					if (loadSamp) {
						1.wait; 
						VGSamp.startUp({ 
							doneFunc.value; 
							loaded = true;
							"******* VGSound: initialised fully.".postln;
						});
					} { 
						doneFunc.value; 
						"******* VGSound: initialised VGSynth only.".postln;
						// HIER: 
						// when not loading samples, addiAr should be default.
						VGSound.playbackSettings.do(_.synthDef = \vgAddiAr);
					};
				});
				} { 
						doneFunc.value; 
						loaded = true;
						"******* VGSound: initialised VGTuning only.".postln;
				}
			};
		};
	}
	
	*quit {
		// todo..
	}
	
	*play { arg laras, synthDef, event; 
		laras = laras ? [\pelog, \slendro, \neutral]; 
		event = event ? ();
		
		Tdef(\VGSound).quant_(0);
		
		Tdef(\VGSound, { 
			laras.do { |inlaras| 
				var insts = VGTuning.tunings[inlaras.postcs];
				var instKeys = event[\instKey] ?? { insts.keys.asArray.sort };

		 "\n**********    VGSound plays laras '%':   ************\n".postf(laras);
				
				instKeys.do { |instKey| 
				
					var noteDict = insts[instKey]; 
					var notes = event[\noteKey] ?? { noteDict.keys.asArray.sort }; 
					
					synthDef = synthDef 
						?? { try { VGSound.playbackSettings[instKey][\synthDef] } } 
						?? VGSamp.defaultDef;

					"\n ** VGSound plays inst: %  --- notes: %:\n".postf(instKey, notes);
					 
					notes.do { |note| 
						var playEvent = (
							\laras: inlaras,
							\synthDef: synthDef,
							\instKey: instKey, 
							\noteKey: note
						);
						playEvent.postcs;
						
						VGSynth.eventFor(playEvent).play;
						
						0.2.wait;
					};
					0.4.wait;
				 };
				 0.4.wait;
			};

			"VGSound.play is done.".postln;
		}).stop.play(quant: 0);		
	}
	
	*makeDefaultEvent {
		
		Event.addEventType(VGSound.eventType, {|server|
						var freqs, lag, strum, strumTime, sustain;
						var bndl, addAction, group, sendGate, ids;
						var msgFunc, desc, synthLib, bundle, instrumentName, 
							schedBundleArray, offset, instrument;
						
						freqs = ~detunedFreq.value;
										
						if (freqs.isKindOf(Symbol).not) {
							// determine msgFunc - it gets the synth's control values from the Event							
									// multichan expand for instrument name!
									// rest is plain.
							if(~instrument.isKindOf(Array)) { 
								instrument = ~instrument.collect(_.asSymbol);
								instrumentName = instrument.first;
							
							} { 
								instrument = ~instrument.asSymbol;
								instrumentName = instrument;
							};

							msgFunc = ~msgFunc ?? {
								synthLib = ~synthLib ?? { SynthDescLib.global };
								desc = synthLib.synthDescs[instrumentName];
								if (desc.notNil) { 
									~hasGate = desc.hasGate;
									~msgFunc = msgFunc = desc.msgFunc;
								}{
									~msgFunc = ~defaultMsgFunc;
								};
							};
						
							// now update values in the Event that may be determined by functions
							~freq = freqs;
							~amp = ~amp.value;
							~sustain = sustain = ~sustain.value;
			
							// compute the control values and generate OSC commands
							bndl = msgFunc.valueEnvir.asControlInput;							bndl = [\s_new, instrument, ids, 
								Node.actionNumberFor(~addAction), ~group.asControlInput] ++ bndl; 
								
							bndl = bndl.flop;
							ids = Array.fill(bndl.size, {server.nextNodeID });
							bndl.do { | msg, i | msg[2] = ids[i]  };
							
							// determine how to send those commands
							lag = ~lag;
							offset = ~timingOffset;
							sendGate = ~sendGate ? ~hasGate;         // sendGate == false turns off releases
							schedBundleArray = ~schedBundleArray;
							if (	 (strum = ~strum) == 0 ) {
								schedBundleArray.value(lag, offset, server, bndl);
								if (sendGate) { 
									~schedBundleArray.value(lag, sustain + offset, server,
										[['/error', -1]] ++ 
										[\n_set, ids, \gate, 0].flop
										++[['/error', -2]]
									)
								}
							} {	
								if (strum < 0) { bndl = bndl.reverse };
								strumTime = 0;
								bndl.do { | msg, i | 
									~schedStrummedNote.value( lag, strumTime, sustain, server, msg, sendGate); 
									strumTime = strumTime + strum;
								}
							}
						}
					});
	
	}

}