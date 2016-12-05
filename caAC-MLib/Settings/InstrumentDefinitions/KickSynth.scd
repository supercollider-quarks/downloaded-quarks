// SynthDef Definition
(
SynthDef(\KickSynth, { |
	out, freqFund = 40, freqRange = 4000, freqDecTime = 0.1, freqCurve,
	ampAtk = 0.003, ampDec = 0.4, ampCurve = -4,
	eqFreq = 2000, eqRQ = 0.01, eqDb = 12, distort = 1,
	pan = 0, amp = 0.1, gate = 1|
	var env =   EnvGen.kr(Env.asr(ampAtk, 1, ampDec, ampCurve), gate, doneAction: 2);
	var shift = EnvGen.kr(Env.new([1,0], [freqDecTime], freqCurve));
	var sig = SinOsc.ar(shift * freqRange + freqFund);
	sig= BPeakEQ.ar(sig, freq: eqFreq, rq: eqRQ, db: eqDb, mul: distort).distort;
	sig = Pan2.ar(sig, pan, amp * env);
	Out.ar(out, sig);
}).store;
)

(
// Call and process SynthDef metadata
m = SynthDesc.readDef(\KickSynth); if (m.metadata.isNil) { m.metadata = (); };

if (m.metadata[\specs].isNil) { m.metadata[\specs] = ();  };

m.metadata[\specs] = (
	freqFund: \midfreq.asSpec,
	freqRange: ControlSpec(0, 13000),
	freqDecTime: ControlSpec(0.001, 2, \exp),
	freqCurve: ControlSpec(-24,0),
	ampAtk: ControlSpec(0.001, 2, \exp),
	ampDec: ControlSpec(0.01, 2, \exp),
	ampCurve: ControlSpec(-24,0),
	eqFreq: \freq.asSpec,
	eqRQ: \rq.asSpec,
	eqDb: ControlSpec(-24, 24),
	distort: ControlSpec(1,1000, \exp)
);

// Ignore key list
m.metadata[\noGui] = [\gate];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\KickSynth, m.metadata);
);

3
s.boot;
// Test SynthDef
EmbedGui(\KickSynth).makeGui;





