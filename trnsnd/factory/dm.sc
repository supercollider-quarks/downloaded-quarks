DmFactory {
	*make {
		// analog style kicks
		SynthDef(\dm_kSine, { |out=0 freqAdd=40 freqMul=40 amp=0.1 a=0.005 r=0.5 pan=0|
			var sig, env;
			sig = SinOsc.ar(Env.perc(0.01, 0.09).ar(0, 1, 1, freqMul, freqAdd), 0.5pi);
			env = Env.perc(a, r).ar(2);
			sig = sig * env;
			sig = sig * amp;
			sig = Pan2.ar(sig, pan);
			Out.ar(out, sig);
		}).add;
		SynthDef(\dm_kTri, { |out=0 freqAdd=40 freqMul=40 amp=0.1 a=0.005 r=0.5 pan=0|
			var sig, env;
			sig = LFTri.ar(Env.perc(0.01, 0.09).ar(0, 1, 1, freqMul, freqAdd), 1);
			env = Env.perc(a, r).ar(2);
			sig = sig * env;
			sig = sig * amp;
			sig = Pan2.ar(sig, pan);
			Out.ar(out, sig);
		}).add;
		SynthDef(\dm_kPar, { |out=0 freqAdd=40 freqMul=40 amp=0.1 a=0.005 r=0.5 pan=0|
			var sig, env;
			sig = LFPar.ar(Env.perc(0.01, 0.09).ar(0, 1, 1, freqMul, freqAdd));
			env = Env.perc(a, r).ar(2);
			sig = sig * env;
			sig = sig * amp;
			sig = Pan2.ar(sig, pan);
			Out.ar(out, sig);
		}).add;
		SynthDef(\dm_clap, { |out=0 amp=0.1 rFreq=1000 rQ=4 r=0.2|
			var env, sig;
			env = Env([0,1,0.1,1,0.1,1,0.1,1,0],[0,0.008,0,0.008,0,0.005,0,r],-4).kr(2);
			sig = ClipNoise.ar(1.dup);
			sig = sig * env;
			sig = Resonz.ar(sig, rFreq, rQ);
			sig = sig * amp;
			Out.ar(out, sig);
		}).add;
		SynthDef(\dm_snare, { |out=0 sustain=1 amp=0.1|
			var env, sig;
			env = Env.perc(0.01, 0.3).kr(2, timeScale: sustain);
			sig = WhiteNoise.ar(env);
			sig = Resonz.ar(sig, env.range(2000, 5000), Line.kr(1, 0.2, 0.3), 2).distort*amp;
			sig = sig + SinOsc.ar(Env.perc(0.01, 0.01).kr.range(110, 190), 0, env*amp*0.5);
			sig = Pan2.ar(sig);
			Out.ar(out, sig);
		}).add;
		SynthDef(\dm_hat, { |out=0 amp=0.1 a=0.005 r=0.1|
			var env, sig;
			env = Env.perc(a, r).ar(2);
			sig = ClipNoise.ar(1.dup);
			sig = BHiPass4.ar(sig, 7000, 1, 3);
			sig = sig * env;
			sig = sig * amp;
			Out.ar(out, sig);
		}).add;
	}
}