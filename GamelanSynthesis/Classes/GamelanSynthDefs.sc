GamelanSynthDefs {

	*initClass {
		StartUp.add {
			//----- start - from here to -end the block can run independently
			
			var synthData, synthNames, synthConstituents;
//			try {
//			synthData = CSVFileReader.read(
//				Document.dir ++ "VGG/synthData/" ++ "gamelanSynthDefsData.csv", true);
//			synthNames = synthData.copySeries(0,4,synthData.size);
//	
//	// the following somewhat forced (first ++ arrays and then re-clump them again) construction 
//	// allows to manipulate each array individually (amp is converted from dB to amp here)
//	// maybe there is a more elegant/efficient way.
//	
//			synthConstituents = synthData.clump(4).collect{ |currentLine| 
//				(currentLine[1].asFloat ++ 
//				currentLine[2].asFloat.dbamp ++ 
//				currentLine[3].asFloat
//				).clump(currentLine[1].size)
//										 		        };
//		synthConstituents.do { |data, i|
//			 var name = synthNames[i][0];
//			SynthDef(name, 
//				{ |out=0, gate=0.1, amp=0.5, pan=0.5, attack=0.01, sustain = 1, release = 0.5|
//					var env, trig;
//					
//					trig = Impulse.ar(0/*, 0, 1*/).lag(attack);
//					env = EnvGen.kr(
//						Env([1,1,0], [sustain, release]/*, 2, 1*/)/*, gate: gate*/, 
//						levelScale: amp, 
//						doneAction: 2
//						);
//						OffsetOut.ar(0, 
//								Pan2.ar(
//									DynKlank.ar(`data, 
//											   input: trig, 
//											   freqscale:  MouseX.kr(0.5, 2, 1), 
//											   freqoffset: MouseY.kr(1000, 10, 1), 
//											   decayscale: MouseButton.kr(1, 1.5)) * env, 
//										  pan) 
//									   );
//						}).store;
//			 	("gamelan synthdef " ++ name ++ " written").postln;
//			};
//			} { |err| err.postln };
// ----- end independent block
		
		
		// simple sine
		(
		SynthDef("sine", { arg out=0, freq=1000, attack=0, sustain=0.1, release=0.05, 
					amp=0.1, iphase=0, pan; 
			var env, u;
			amp = AmpCompA.kr(freq) * amp;
			env = EnvGen.ar(
				Env(
					[0, amp, amp, 0], 
					[attack, sustain, release], 
					[\lin, \lin, \lin]
				),
				doneAction:2);
			u = Pan2.ar(SinOsc.ar(freq, iphase, env), pan);
			OffsetOut.ar(out, u)
		}, [\kr, \kr, \ir, \ir, \ir, \kr, \ir, \kr]).store;
		);
		
		// default synthdef
		
		SynthDef(\demung, { |out=0, freq=1900.09, amp=0.1, pan=0, 
									attack=0.001, sustain = 6, release = 0.2|
					var env, damp;
					env = EnvGen.kr(Env([1,1,0], [sustain, release]), 
						levelScale: amp, doneAction: 2);
					OffsetOut.ar(0, 
						Pan2.ar(
							Klank.ar(	`[[01.00, 04.8564], [00.72, 00.4200], [10.00, 00.6000]],
							 Impulse.ar(0, 0, 1).lag(attack), 
							freqscale: freq) * env,
							pan
						) 
					);
				}).store;
				(
		(
		SynthDef("gamGongAgeng", { |out=0, gate = 0.1, freq=45, amp=0.1, pan=0, attack=0.02, sustain = 14, release = 2|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 01.98, 02.00, 03.05, 04.00, 05.00, 06.00] * freq, 
						[01.00, 01.00, 01.00, 00.40, 00.00, 00.30, 00.10],
						[15.00, 15.00, 11.00, 08.00, 15.00, 15.00, 07.00]
						], 
						Impulse.ar(0).lag(attack)
								)
				) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
						);
				}).store;
		);
		
		(
		SynthDef("gamKenong", { |out=0, gate = 0.1, freq=475.02, amp=0.1, pan=0, attack=0.005, sustain = 6, release = 2|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 02.00, 02.01, 03.00, 03.10, 04.00] * freq, 
						[01.00, 00.20, 00.15, 00.05, 00.05, 00.02],
						[06.00, 04.00, 05.00, 04.00, 03.00, 02.00]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKempul", { |out=0, gate = 0.1, freq=118.76, amp=0.1, pan=0, attack=0.02, sustain = 6, release = 2|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 01.99, 02.00] * freq, 
						[01.00, 00.60, 00.60],
						[08.00, 04.00, 05.00]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKethuk", { |out=0, gate = 0.1, freq=156.70, amp=0.1, pan=0, attack=0.01, sustain = 1, release = 0.5|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00] * freq, 
						[01.00],
						[00.40]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKempyang", { |out=0, gate = 0.1, freq=545.66, amp=0.1, pan=0, attack=0.01, sustain = 1, release = 1|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 01.98, 02.01, 03.00, 03.10] * freq, 
						[00.40, 00.00, 00.60, 00.03, 00.03],
						[02.00, 00.00, 01.70, 01.80, 01.50]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamSlenthem", { |out=0, gate = 0.1, freq=181.42, amp=0.1, pan=0, attack=0.01, sustain = 0.9, release = 0.1|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00] * freq, 
						[00.90],
						[10.00]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([0, 1,1,0], [0.01, 0.85, 0.1], 'sine', 2), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		( 
		SynthDef("gamBonangBar", { |out=0, gate = 0.1, freq=365, amp=0.1, pan=0, attack=0.005, sustain = 0.1, release = 0.1|
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 01.4932, 03.014, 03.3233, 03.7260] * freq, 
						[00.90, 00.60, 00.650, 00.6000, 00.5200],
						[01.90, 11.10, 01.200, 03.0000, 01.6000]
						], 
						Impulse.ar(0).lag(attack)
								)
						) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKdhBem", 
							{ |out=0, gate=0.1, freq=80, amp=0.1, pan=0, attack=0.02, sustain = 3.7, release = 0.3| 
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
							[1, 2, 3, 4] * freq, 
							[1, 0.7, 0.5, 0.35].squared, 
							[4, 3, 2, 1.5] * 1	// should ringtime depend on freq?
						], Impulse.ar(0).lag(attack)
					)
				) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKdhThung", { |out=0, gate=0.1, freq=340, amp=0.1, pan=0, attack=0.02, sustain = 3.7, release = 0.3| 
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
							[1, 2, 3] * freq, 
							[1, 0.4, 0.2].squared, 
							[3, 2, 1] * 1	// should ringtime depend on freq?
						], Impulse.ar(0).lag(attack)
					)
				) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		(
		SynthDef("gamKdhKet", { |out=0, gate=0.01, freq=540, amp=0.1, pan=0, attack=0, sustain = 0.1, release = 0.1| 
			OffsetOut.ar(0, 
				Pan2.ar(
					Resonz.ar(PinkNoise.ar(amp), freq, 0.1).lag(attack) * EnvGen.kr(Env([1,1,0], [sustain, release], 2, 1), gate: gate, levelScale: amp, doneAction: 2)
			))
		}).store;
		);
		
		(
		SynthDef("gamKdhTak", { |out=0, gate=0.1, freq=270, amp=1, pan=0, attack=0| 
			OffsetOut.ar(0, 
				Pan2.ar(
					PinkNoise.ar(0.2) * EnvGen.kr(Env.perc(0.01, 1, curve: -36), gate: gate, levelScale: amp, doneAction: 2)
			))
		}).store;
		);
		
		(
		SynthDef("gamKdhThong", { |out=0, gate=0.1, freq=540, amp=0.1, pan=0, attack=0| 
			OffsetOut.ar(0, 
				Pan2.ar(
					Klank.ar(	`[ 
						[01.00, 01.50, 02.00, 03.00, 03.32, 03.72] * freq, 
						[00.50, 00.20, 00.00, 00.10, 00.10, 00.02],
						[01.00, 00.50, 00.00, 00.20, 00.01, 00.20]
						], Impulse.ar(0).lag(attack)
					)
				) * EnvGen.kr(Env.perc(0.002, 0.2, 0.3, curve: -12), gate: gate, levelScale: amp, doneAction: 2)
			);
		}).store;
		);
		
		);
		}
	}

}