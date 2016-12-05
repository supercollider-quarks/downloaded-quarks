
Fx2Factory {
	classvar <fx;
	*initClass {
		fx = IdentityDictionary[
			// ------------------------------ buf
			// n.b. no more than one because of Bdef
			\slice -> { |sig|
				SynthDef.wrap({
					var buf, phasor;
					buf = Bdef(\fx_slice, Server.default.sampleRate, 2);
					phasor = Phasor.ar(0, \rate.kr(1), 0, SampleRate.ir*\dur.kr(0.1).min(1));
					RecordBuf.ar(sig, Bdef(\fx_slice), loop: 0);
					BufRd.ar(2, Bdef(\fx_slice), phasor);
				}, prependArgs: sig)
			},
			// ------------------------------ delay
			\tape -> { |sig|
				SynthDef.wrap({
					var fbk;
					var del = \del.kr(0.5);
					// noise gate
					sig = sig * (Amplitude.kr(sig) > 0.02);
					fbk = LocalIn.ar(2)*\fb.kr(1);
					fbk = OnePole.ar(fbk, 0.4);
					fbk = OnePole.ar(fbk, -0.08);
					fbk = DelayC.ar(fbk, del, del);
					fbk = LeakDC.ar(fbk);
					fbk = ((fbk + sig) * 1.25).softclip;
					fbk = Rotate2.ar(fbk[0], fbk[1], \rotate.kr(0));
					LocalOut.ar(fbk);
					// return
					fbk
				}, prependArgs: sig)
			},
			\comb-> { |sig|
				SynthDef.wrap({
					CombC.ar(sig, \del.kr(0.2), \del.kr(0.2), \decay.kr(0.5), 0.5, sig);
				}, prependArgs: sig)
			},
			\verb -> { |sig|
				SynthDef.wrap({
					FreeVerb2.ar(sig[0], sig[1], 1, \room.kr(0.7), \damp.kr(0.5));
				}, prependArgs: sig)
			},
			// ------------------------------ distortion
			\distort -> { |sig|
				SynthDef.wrap({
					sig = sig * \amp.kr(1);
					sig = sig.distort * \amp.kr(1).distort.reciprocal;
				}, prependArgs: sig)
			},
			\softclip -> { |sig|
				SynthDef.wrap({
					sig = sig * \amp.kr(1);
					sig = sig.softclip * \amp.kr(1).softclip.reciprocal;
				}, prependArgs: sig)
			},
			\round -> { |sig|
				SynthDef.wrap({
					sig.round(\amp.kr(0));
				}, prependArgs: sig)
			},
			\xces -> { |sig|
				SynthDef.wrap({
					sig.excess(\amp.kr(0));
				}, prependArgs: sig)
			},
			// ------------------------------ 4-pole filters
			\hip4 -> { |sig|
				SynthDef.wrap({
					BHiPass4.ar(sig, \freq.kr(200, 0.01), \q.kr(0.5));
				}, prependArgs: sig)
			},
			\lop4 -> { |sig|
				SynthDef.wrap({
					BLowPass4.ar(sig, \freq.kr(5000, 0.01), \q.kr(0.5));
				}, prependArgs: sig)
			},
			// ------------------------------ dynamics
			\norm -> { |sig|
				SynthDef.wrap({
					Normalizer.ar(sig, \amp.kr(0.3));
				}, prependArgs: sig)
			},
			\comp -> { |sig|
				SynthDef.wrap({
					// more kr?
					// side-chain?
					CompanderD.ar(sig, \thresh.kr(0.5), 0.5, 0.5, 0.002, 0.002);
					// sig = Limiter.ar(sig);// safeguard? CheckBadValues?
				}, prependArgs: sig)
			},
			\rm -> { |sig|
				SynthDef.wrap({ sig.ring1(SinOsc.ar(\freq.kr(440))) }, prependArgs: sig);
			},
		];
	}
	*make {
		fx.keysValuesDo { |name, func|
			SynthDef(\fx2_ ++ name, {
				var sig, env;
				// asr avoid pops at both sides
				// must .ar for fast env
				env = Env.asr(0.005).ar(2, \gate.kr(1));
				sig = In.ar(\out.ir(0), 2);
				sig = func.(sig);
				XOut.ar(\out.ir(0), \xfade.kr(1)*env, sig);
			}).add;
		}
	}
	*store {
		fx.keysValuesDo { |name, func|
			SynthDef(\fx2_ ++ name, {
				var sig, env;
				// asr avoid pops at both sides.
				// must .ar for fast env
				env = Env.asr.ar(2, \gate.kr(1));
				sig = In.ar(\out.ir(0), 2);
				sig = func.(sig);
				XOut.ar(\out.ir(0), \xfade.kr(1)*env, sig);
			}).store;
		}
	}
}