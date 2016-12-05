KeyFactory {
	*make {
		SynthDef(\key_sine, { |gate=1 out=0 freq=440 amp=0.1 a=0.05 d=0.3 s=0.5 r=0.5 pan=0|
			var sig, env;
			sig = FSinOsc.ar(freq);
			env = Env.adsr(a, d, s, r).ar(2, gate);
			sig = sig * env;
			sig = sig * amp;
			sig = Pan2.ar(sig, Rand(-1, 1));
			Out.ar(out, sig);
		}).add;
	}
}