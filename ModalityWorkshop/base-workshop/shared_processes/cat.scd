// SOUND 2 // the DubCat example from udk gencomp class:
(
// controlspecs
Ndef(\cat).addSpec(\pFreq, [20, 400, \exp]);
Ndef(\cat).addSpec(\pWidth, [0.01, 0.5, \exp]);
Ndef(\cat).addSpec(\filt, [1, 32, \exp]);
Ndef(\cat).addSpec(\q, [1, 1000, \exp]);
Ndef(\cat).addSpec(\modFreq, [0, 200, 7]);
Ndef(\cat).addSpec(\moDepth, [0, 12*8]);

// a Synth with 6 control parameters
(
Ndef(\cat, { |pFreq=100, pWidth=0.5, filt=2, q=1, modFreq=0, moDepth=12|
	var signal;
	pFreq = pFreq * SinOsc.ar(modFreq * 0.5 + [0, 0.1], 0).product.madd(moDepth).midiratio;
	signal = RLPF.ar(
		LFPulse.ar(
			pFreq * [1, 1.01],
			0,
			pWidth
		),
		(pFreq * filt).clip(50, 15000),
		q.reciprocal
	)
	* AmpComp.kr(pFreq.lag(1));
	Limiter.ar(signal) * 0.3;
})
);

Ndef(\cat).vol_(0.2);	// don't play it yet, just prepare
// NdefPreset( Ndef(\cat) ).loadSettings;
);
