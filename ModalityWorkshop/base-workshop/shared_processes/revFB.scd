/*
Jeff Carey-style feedback patch, adc 2014
Requires AdCVerb2 class, from SpeakersCorner quark
Quarks.update("SpeakersCorner");
Quarks.install("SpeakersCorner");

To Do:
* better sounding noise spikes to drop into the feedback loop,
* task or function plays them on trigger from interface

*/

// two grain-like synthdefs, just to show the concept
SynthDef(\whytnoyz, { |out, amp = 0.25, gdur = 0.01, ramp = 4, rq = 0.5|
	var snd = BPF.ar(WhiteNoise.ar(1 ! 2), ExpRand(400, 15000), rq);
	var env = XLine.kr(ramp ** 0.5, ramp ** -0.5, gdur, amp, 0, 2);
	Out.ar(out, snd * env);
}).add;

SynthDef(\imp, { |out, frq = 100, gdur = 0.1, ramp = 4, amp = 0.1|
	var snd = Impulse.ar(XLine.kr(ramp ** 0.5, ramp ** -0.5, gdur, frq, 0, 2));
	Out.ar(out, Ringz.ar(snd, 2500, 0.003) * amp);
}).add;

// specs
Ndef(\revFB).addSpec(\feedback, [0, 0.5, \amp]);

// special parameters for AdCVerb2
Ndef(\revFB).addSpec(\animRate, [0.01, 100, \exp]);
Ndef(\revFB).addSpec(\animDepth, [0.0, 1, \amp]);

// general parameters for AdCVerb as well
Ndef(\revFB).addSpec(\hfDamping, [0, 1]);
Ndef(\revFB).addSpec(\predelay, [0.0, 0.2, \amp]);
Ndef(\revFB).addSpec(\revTime, [0.01, 100, \exp]);
Ndef(\revFB).addSpec(\inFilter, [-0.99, 0.99]);
Ndef(\revFB).addSpec(\leakCoeff, [0.9, 0.998]);
Ndef(\revFB).addSpec(\combScale, [0.1, 2, \exp]);
Ndef(\revFB).addSpec(\apScale, [0.1, 2, \exp]);
Ndef(\revFB).addSpec(\direct, \amp);


Ndef(\revFB).ar(2);
Ndef(\revFBin).ar(2);

Ndef(\revFB, { |direct = 0.2, feedback = 0.1, revTime = 3, animRate = 0.1, animDepth = 0.03,
	hfDamping = 0.1, predelay = 0.02, inFilter = 0.6,
	leakCoeff = 0.995, combScale = 1|
	var extraIn = Ndef(\revFBin).ar(2);
	var fb = Limiter.ar(
		(Ndef(\revFB).ar.sum * feedback)
		+ (PinkNoise.ar(0.001) + Dust2.ar(21, 0.01))
		+ extraIn.sum

	);
	var snd = AdCVerb2.ar(fb,
		animRate: animRate.lag(0.5),
		animDepth: animDepth.lag(0.5),
        revTime: revTime.lag(0.5),
        hfDamping: hfDamping.lag(0.5),
        nOuts: 2,         // fixed number of decorrelated output channels
        predelay: predelay.lag(0.5),
        numCombs:     8,     // fixed number - increase for more density
        numAllpasses: 4,     // fixed number - increase for more diffuseness
        inFilter: inFilter,
        leakCoeff: leakCoeff,
        combScale: combScale.lag(0.5)
	) + (extraIn * direct);

	// round off highs somewhat
	// maybe resurrect DualBandComp?
	OnePole.ar(snd.softclip, 0.2);
});

/* // tests
Ndef(\revFB).play;
Ndef(\revFB).gui;

// drop a grain into revFB
(instrument: \whytnoyz, amp: 1, gdur: 0.01, out: Ndef(\revFBin).index, addAction: \addToHead).play;

(instrument: \imp, frq: exprand(10, 100), gdur: exprand(0.01, 0.3), ramp: exprand(0.25, 4), out: Ndef(\revFBin).index, amp: 4, addAction: \addToHead).play;
*/


