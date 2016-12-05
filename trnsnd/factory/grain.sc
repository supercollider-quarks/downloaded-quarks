GrainFactory {
	classvar osc, env, pan;
	*initClass {
		osc = IdentityDictionary[
			\sine   -> { FSinOsc.ar(\freq.ir(440),\phase.ir(0)) },
			\sinefb -> { SinOscFB.ar(\freq.ir(440),\fb.ir(0)) },
			\pm     -> { PMOsc.ar(\freq.ir(440),\modFreq.ir(100),\modIndex.ir(1),\phase.ir(0)) },
			\rm     -> { FSinOsc.ar(\freq.ir(440),\phase.ir(0)).ring1(FSinOsc.ar(\modFreq.ir(100))) },
			\par    -> { LFPar.ar(\freq.ir(440),\phase.ir(0)) },
			\tri    -> { LFTri.ar(\freq.ir(440),\phase.ir(0)) },
			\saw    -> { Saw.ar(\freq.ir(440)) },
			\var    -> { VarSaw.ar(\freq.ir(440),\phase.ir(0),\sawWidth.ir(0.5)) },
			\sync   -> { SyncSaw.ar(\freq.ir(440),\sawFreq.ir(440).max(\freq.ir(440))) },
			\blip   -> { Blip.ar(\freq.ir(440),\numHarm.ir(10)) },
			\pulse  -> { Pulse.ar(\freq.ir(440),\pulseWidth.ir(0.5)) },
			\impulse-> { Impulse.ar(\freq.ir(440),\phase.ir(0)) },
			\formant-> { Formant.ar(\freq.ir(440),\formFreq.ir(1760),\bwFreq.ir(800).max(\freq.ir(440))) },
		];
		env = IdentityDictionary[
			// must .ar
			\hann  -> { Env.sine(\sustain.ir(1),\amp.ir(0.1)).ar(2) },
			\perc  -> { Env.perc(\a.ir(0.05),\r.ir(0.95),\amp.ir(0.1),\c.ir(-4)).ar(2,1,\sustain.ir(1)) },
			\rect  -> { Env.linen(\a.ir(0.25),\s.ir(0.5),\r.ir(0.25),\amp.ir(0.1),\c.ir(0)).ar(2,1,\sustain.ir(1)) },
			\gauss -> { LFGauss.ar(\sustain.ir(1),\width.ir(0.1),0,0,2)*\amp.ir(0.1) },
		];
		pan = IdentityDictionary[
			\pan2 -> { |sig| SynthDef.wrap({ Pan2.ar(sig, \pan.ir(0)) }, prependArgs: sig) },
			\pan4 -> { |sig| SynthDef.wrap({ Pan4.ar(sig, \panX.ir(0), \panY.ir(0)) }, prependArgs: sig) },
		];
	}
	*make {
		osc.keysValuesDo { |oscName, oscFunc|
			env.keysValuesDo { |envName, envFunc|
				pan.keysValuesDo { |panName, panFunc|
					SynthDef(\g_ ++ oscName ++ \_ ++ envName ++ \_ ++ panName, {
						var sig, env;
						sig = oscFunc.();
						env = envFunc.();
						sig = sig * env * AmpCompA.ir(\freq.ir(440).max(40), 40);
						OffsetOut.ar(\out.ir(0), panFunc.(sig));
					}).add;
				}
			}
		}
	}
	*store {
		osc.keysValuesDo { |oscName, oscFunc|
			env.keysValuesDo { |envName, envFunc|
				pan.keysValuesDo { |panName, panFunc|
					SynthDef(\g_ ++ oscName ++ \_ ++ envName ++ \_ ++ panName, {
						var sig, env;
						sig = oscFunc.();
						env = envFunc.();
						sig = sig * env * AmpCompA.ir(\freq.ir(440).max(40), 40);
						OffsetOut.ar(\out.ir(0), panFunc.(sig));
					}).store;
				}
			}
		}
	}
}