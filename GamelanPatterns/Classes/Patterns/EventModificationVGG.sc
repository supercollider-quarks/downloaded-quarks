// timeOffset -> timingOffset !

/*EventModificationVGG {

	*initClass {
		
		Event.addEventType(\note, {|server|
								var instrumentName, freqs, lag, dur, strum, sustain, desc, msgFunc;
								var bndl, synthLib, addAction, group, hasGate;
								
								freqs = ~freq = ~freq.value + ~detune;
												
								if (freqs.isKindOf(Symbol).not) {
									~amp = ~amp.value;
									addAction = ~addAction;
									group = ~group;
									lag = ~lag;
									strum = ~strum;
									sustain = ~sustain = ~sustain.value;
									instrumentName = ~instrument.asSymbol;
									msgFunc = ~msgFunc;
									if (msgFunc.isNil) {
										synthLib = ~synthLib ?? { SynthDescLib.global };
										desc = synthLib.synthDescs[instrumentName];
										if (desc.notNil) { 
											hasGate = desc.hasGate;
											msgFunc = desc.msgFunc;
										}{
											hasGate = ~hasGate ? true;
											msgFunc = ~defaultMsgFunc;
										};
									}{
										hasGate = ~hasGate ? true;
									};
								//	~hasGate = hasGate;
									bndl = msgFunc.valueEnvir.flop;
									bndl.do {|msgArgs, i|
										var id, timeOffset;
										
										timeOffset = i * strum + lag;
										id = server.nextNodeID;
										
									//	(timeOffset: timeOffset).postln;
										
										if (timeOffset > 0) {
											//send the note on bundle
											thisThread.clock.sched(timeOffset) { 
												server.sendBundle(server.latency, [\s_new, instrumentName, id, addAction, group] ++ msgArgs); 
											};
										} {  
												server.sendBundle(server.latency, [\s_new, instrumentName, id, addAction, group] ++ msgArgs); 
										};
										if (hasGate) {
											// send note off bundle.
											thisThread.clock.sched(sustain + timeOffset) { 
												server.sendBundle(server.latency, [\n_set, id, \gate, 0]); 
											};
										};
									}
								};
							});

	}

}*/