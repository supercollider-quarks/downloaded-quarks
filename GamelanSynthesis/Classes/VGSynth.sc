VGSynth : VGSound { 
	classvar <>maxPartials = 40; 
	classvar <>synthDefs;
	classvar <>defNames = #[\vgRingz, \vgFormlet, \vgAddiAr, \vgAddiKr, \vgTest]; 
	classvar <>defaultDef = \vgAddiAr;
	classvar <>loaded = false;
	
	*initClass { 
		Class.initClassTree(VGTuning);
		defNames.do { |name| synthEngines.put(name, this) };
	}
		
	*sendSynthDefs { 
		synthDefs.do(_.send(server));
	}
	
	*initSynthDefs { |doneFunc| 
		var numArgs = VGTuning.busKeys.size;
		var maxPartials = VGSynth.maxPartials;
		var partialNumbers = IdentitySet.new;
		
			// quick and dirty: dont add numPartials to synthdefname.
		VGSound.synthEngines.put(\vgPartial, VGSamp);	

		// for now, change deepDo later!
		
		VGTuning.mulTrees.do { |tree|
			tree.deepDo({ |node, i|
				if(i == 1) {
					partialNumbers.add(node.baseValues.first.size)
				}
			}, 3)
		
		};
		
			// make all those that are needed:
		partialNumbers = partialNumbers.asArray.sort.reject(_ == 0);

	
		if ((SynthDef.synthDefDir +/+ "vgPartia*").pathMatch.notEmpty) { 
			SynthDescLib.global.read;
			"\n\n*** VGSynth found written synthdefs and reads them from %.\n\n"
				.postf(SynthDef.synthDefDir);
			doneFunc.value;
			loaded = true;
			^this; 
		};

		"\n\n*** VGSynth: creating synthdefs...".postln;
		
		Task { 
			0.02.wait; 

			
			// VGSynth.synthDefs = (1..maxPartials)
		VGSynth.synthDefs = partialNumbers.collect { |numPartials, i|
					
			[
			SynthDef(("vgFormlet" ++ numPartials), { |out, gate=1, pan, amp=1, ampComp=10, 
				ebus, ibus, sbus, rbus, bbus, rev, noisy=0.2| 
				
				var freqs, attacks, ringtimes, amps;
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs)); 
				var partialBusses = [rbus, bbus].collect (In.kr(_, numPartials * numArgs)).product; 
				var exciter, sound; 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);

				#freqs, ringtimes, amps, attacks = 
				shiftBusses.product * partialBusses.clump(numPartials);
				
				amps = amps * ((60 * (attacks / ringtimes)).dbamp * (3 * 3.0));
				
				exciter = Impulse.ar(0); 
				exciter = exciter * (1-noisy) + Decay.ar(exciter, 0.01, PinkNoise.ar(noisy));

				sound = Formlet.ar(exciter, freqs, attacks * 0.01, ringtimes, amps * (amp * ampComp)).sum;
				
				
				DetectSilence.ar(sound , doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, cutoff));
			}),
			
			
			SynthDef(("vgRingz" ++ numPartials), { |out, gate=1, pan, amp=1, ampComp=10, ebus, ibus, sbus, rbus, bbus, rev, noisy=0.2| 
				
				var freqs, attacks, ringtimes, amps;
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs));
				var partialBusses = [rbus, bbus].collect (In.kr(_, numPartials * numArgs)).product; 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);

				var exciter, sound; 
				#freqs, ringtimes, amps, attacks = 
				shiftBusses.product * partialBusses.clump(numPartials);
				
					// how to round off attack? 
				exciter = Decay2.ar(Impulse.ar(0), 0.001, 0.01, PinkNoise.ar(noisy, 1-noisy));
				sound = Ringz.ar(exciter, freqs, ringtimes, amps * (amp * ampComp * 0.15)).sum;
			
				DetectSilence.ar(sound , doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, cutoff));
			}),
			
			SynthDef(("vgAddiAr" ++ numPartials), { |out, gate=1, pan, amp=1, ampComp=10, ebus, ibus, sbus, rbus, bbus, rev, curve= -3| 
				
				var freqs, attacks, ringtimes, amps;
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs));
				var partialBusses = [rbus, bbus].collect (In.kr(_, numPartials * numArgs)).product; 
				var exciter, sound, envs; 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);

				#freqs, ringtimes, amps, attacks = 
				shiftBusses.product * partialBusses.clump(numPartials);
				
				amp = (amp * ampComp);
				envs = [attacks, ringtimes, amps].flop.collect { |parlist| 
					var att, dec, lev; #att, dec, lev = parlist;
					EnvGen.ar(Env([0, lev * amp, 0], [att, dec], curve));
				};
				sound = (FSinOsc.ar(freqs) * envs).sum; 
			
				DetectSilence.ar(sound, doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, cutoff));
			}),
			
			SynthDef(("vgAddiKr" ++ numPartials), { |out, gate=1, pan, amp=1, ampComp=10, 
				ebus, ibus, sbus, rbus, bbus, 
				rev, curve= -3| 
				
				var freqs, attacks, ringtimes, amps;
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs));
				var partialBusses = [rbus, bbus].collect (In.kr(_, numPartials * numArgs)).product; 
				var exciter, sound, envs; 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);

				#freqs, ringtimes, amps, attacks = 
				shiftBusses.product * partialBusses.clump(numPartials);
				
				amp = (amp * ampComp);
				envs = [attacks, ringtimes, amps].flop.collect { |parlist| 
					var att, dec, lev; #att, dec, lev = parlist;
					EnvGen.kr(Env([0, lev * amp, 0], [att, dec], curve));
				};
				sound = (FSinOsc.ar(freqs) * envs).sum; 
			
				DetectSilence.ar(sound , doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, cutoff));
			}),
			
				// vgAddiKrRes, vgAddiArRes
				
			SynthDef(("vgTest" ++ numPartials), { |out, gate=1, pan, amp=1, ampComp=10, 
				ebus, ibus, sbus, rbus, bbus, rev, noisy=0.2| 
				
				var freqs, attacks, ringtimes, amps;	
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs)); 
				var partialBusses = [rbus, bbus].collect (In.kr(_, numPartials * numArgs)).product; 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);
				
				shiftBusses.do { |keybus, i| 
					keybus.poll(1, "% bus[%]".format(#["  ensemble", "instrument", "   sonator"][i], i)) 
				};
				
				#freqs, ringtimes, amps, attacks = 
				shiftBusses.product * partialBusses.clump(numPartials);
					[
						[freqs, ringtimes, amps, attacks], 
						["    freqs", "ringtimes", "     amps", "  attacks"]
					].flop.do { |pairs|
					pairs[0].poll(
						1, 
						label: {|i| "%[%]".format(
							pairs[1], 
							i
						) }.dup(numPartials)
					);
					};
				ampComp.poll(1, "------------- ampComp");
				
			})
			]; 
			
		}.add([ 
			SynthDef("vgPartial", { |out, gate=1, pan, amp=1, ampComp=10, 
			ebus, ibus, sbus, rbus, bbus, rev, curve= -3, flattenLevel=0,
			numPartials=5, partIndex=0|
			
			var numArgs = 4;
			var freq, attack, ringtime, busamp;
			var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs));
			
				// more efficient: get right indices of relbus, basebus directly. 
			var partialArgIndices = numArgs.collect(_ * numPartials) + partIndex; 
			
			var partialBusses = [rbus, bbus].collect { |busnum| 
				partialArgIndices.collect { |pind| In.kr(pind + busnum) } 
			}; 
		
			var exciter, sound, env; 
			var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);
		
		//	partialArgIndices.poll(1, ["freqbus", "ringbus", "ampbus", "attbus"]);
		//	partialBusses[1].poll(1, ["freq", "ring", "amp", "att"]);
			
			#freq, ringtime, busamp, attack = shiftBusses.product * partialBusses.product;
			
			amp = (amp * ampComp);
				// pull towards 0.1? 
				// make soft ones louder easily.
			busamp = busamp * 10 ** (1 - flattenLevel.clip(0, 1)) * 0.1; 			
			env = EnvGen.ar(Env([0, busamp * amp, 0], [attack, ringtime], curve));
		
			sound = FSinOsc.ar(freq) * env; 
		
			DetectSilence.ar(sound, doneAction: 2); 
			Out.ar(out, Pan2.ar(sound, pan, cutoff));
			
		}), 
		SynthDef(\vgAmpTest, { |out, gate=1, freq=489.075, ring=3, attack=0.003, amp=0.1, pan| 
			OffsetOut.ar(out, Pan2.ar(
				FSinOsc.ar([1, 2, 3, 4] * freq, 
					mul: ([1, 2, 3, 4] ** -3).normalizeSum).sum
				* EnvGen.ar(Env([0, 1, 0.001], [attack, ring], [-3, \exp]), doneAction: 2),
				pan, 
				EnvGen.kr(Env.cutoff, gate: gate)
				* AmpComp.ir(freq.max(50)).max(0.32)
			));
		})
		]).flat;
		 
			VGSynth.synthDefs.do { |def| def.store; (def.name ++ "   ").post; 0.003.wait; };
		
			server.sync;
			"\n\n*** VGSynth: created and sent % synthdefs.\n\n".postf(VGSynth.synthDefs.size);
			doneFunc.value;
			loaded = true;
		}.play(AppClock);	
	}
}