
(
Ndef( \sound1, { var freq = \freq.kr(200) * LFNoise0.kr(ExpRand(3, 8), 0.02, 1);
	var snd = RLPF.ar(Pulse.ar(freq, 0.2), \rfreq.kr(2500), 0.3);
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
	// range of specific parameters
Ndef( \sound1 ).addSpec(\freq, [60, 600, \exp]);
Ndef( \sound1 ).addSpec(\rfreq, [1500, 6000, \exp]);

// which param should xy-controller set?
Ndef( \sound1 ).addHalo(\xParam, \freq);
Ndef( \sound1 ).addHalo(\yParam, \rfreq);

);

