// SynthDef Definition

(
SynthDef(\DaGrain, { |trate = 3.4, pitchShift = -18, sndBuf = 0, position = 0, posTime = 3, posDev = 0.1, pitchTime = 5, envBuf = 1,
	dampFreq = 3500, fxAmount = -18, lpFreq = 13500, panTime = 3, amp = 0.1 |
	var chain,
	n = 5,
	trigger = { Dust.ar(trate) } ! n,
	sig = distort(sum(
		{ |i|
			GrainBuf.ar(2,
				trigger[i],
				trate.first.reciprocal,
				sndBuf,
				midiratio(i*6+pitchShift),
				pos:position + LFNoise2.kr(posTime.reciprocal,posDev) % 1,
				envbufnum: envBuf,
				pan: LFNoise2.kr(panTime.reciprocal))

	} ! n));
	chain = sig;
	8 do: {
		chain = PitchShift.ar(
			LeakDC.ar(AllpassL.ar(
				RLPF.ar(chain*0.9,dampFreq, 0.84),
				0.2, {0.19.rand+0.01}!2, 3)),
			0.1, LFNoise1.kr(pitchTime.reciprocal).rangeExp(0.25,4), 0.02, 0.02)
		+ chain;

	};
	sig = fxAmount.dbamp * chain + sig;
	sig = Limiter.ar(sig);    // dry + wet
	sig = RLPF.ar(sig, lpFreq, 0.34);
	LocalOut.ar(sig);
	Out.ar(0, sig.softclip * amp)
}).store
)



// Call and process SynthDef metadata
(
m = SynthDesc.readDef(\DaGrain); if (m.metadata.isNil) { m.metadata = (); };

m.metadata[\specs] = (
	trate: ControlSpec(5, 13000, \exp),
	pitchShift: ControlSpec(-40,50, 'lin', 4),
	position: ControlSpec(),
	posDev: ControlSpec(0,0.5),
	posTime: ControlSpec(0.1,18),
	pitchTime: ControlSpec(0.1,18),
	dampFreq: ControlSpec(10, 17000, 'exp'),
	fxAmount: ControlSpec(0, 1.0),
	lpFreq: ControlSpec(10, 17000, 'exp'),
	panTime: ControlSpec(0.1,18),
	amp: ControlSpec(),
);
// Ignore key list
m.metadata[\noGui] = [];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\DaGrain, m.metadata);
);
s.boot;
// Test SynthDef
EmbedGui(\DaGrain).makeGui;



