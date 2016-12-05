

/*
// Hannes example for writing Ndefs step by step

Ndef(\etude).document;

// compare that we have all the specs, except globals like \amp:
Ndef(\etude).controlKeys
Ndef(\etude).getSpec.keys

Ndef(\etude).edit(8);
Ndef(\etude).play;

*/


Ndef(\etude).addSpec(\bpm, [12, 1200, \exp]);
Ndef(\etude).addSpec(\att, [0, 0.2, 5]);
Ndef(\etude).addSpec(\dcy, [0.01, 1, 5]);
Ndef(\etude).addSpec(\ffreq, \freq);
Ndef(\etude).addSpec(\reso, [1, 100, \exp]);
Ndef(\etude).addSpec(\mod, \amp);

// the Ndef iself
Ndef(\etude, { arg bpm=120, att=0.002, dcy=0.05, ffreq=5000, reso=2, amp=0.2, mod=0.2;
	var metro = Impulse.ar(bpm/60);
	var env = Decay2.ar(metro, att, dcy);
	var sig = GrayNoise.ar(amp.dup);
	sig = env * sig;
	ffreq = ((mod*env+1) * ffreq).clip(20, 20000);
	sig = RLPF.ar(sig, ffreq, 1/reso);
	Limiter.ar(sig);
});

