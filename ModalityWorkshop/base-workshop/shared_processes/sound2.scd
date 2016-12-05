(
Ndef( \sound2, {
	var dens = \dens.kr(30);
	var snd = Ringz.ar(Dust2.ar(dens, 0.8), [ \freq1.kr(400), \freq2.kr(500)], 0.03).sum;
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});

// specific specs for this process's parameters
Ndef( \sound2 ).addSpec(\dens, [3, 3000, \exp]);
Ndef( \sound2 ).addSpec(\freq1, [300, 2500, \exp]);
Ndef( \sound2 ).addSpec(\freq2, [1500, 3500, \exp]);

// which params should xy controller set?
Ndef( \sound2 ).addHalo(\xParam, \dens);
Ndef( \sound2 ).addHalo(\yParam, \freq1);
Ndef( \sound2 ).addHalo(\xParam1, \freq2);

);
