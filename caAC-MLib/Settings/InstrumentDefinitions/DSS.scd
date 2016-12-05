// SynthDef Definition

(
SynthDef(\DSS_4,
	{| freqA = 30, freqB = 2500, maxDurSt = 8, maxAmp = 0.5,
		 maxAmpSt = 0.1,  gate = 1 |
	 	var numBPs, maxDur, minDur, signal, freqs, env;

		numBPs			= 17;
		freqs			= [ min(freqA, freqB), max(freqA, freqB) ];
		maxDur			= 44100 / freqs[0] / numBPs;
		minDur			= 44100 / freqs[1] / numBPs;
		maxDurSt		= (maxDur - minDur).abs * maxDurSt;
		maxAmpSt		= (maxAmp * 2) * maxAmpSt;

		env = Linen.kr(gate, 0.01, 1, 0.1, 2);

		signal =
		 DemandEnvGen.ar(
			Dswitch1(
				{ Dbrown(maxAmp.neg, maxAmp, maxAmpSt) } ! numBPs,
				Dseq(Array.series(numBPs, 0, 1), inf)
			),
			Dswitch1(
				{ Dbrown(minDur, maxDur, maxDurSt) } ! numBPs,
				Dseq(Array.series(numBPs, 0, 1), inf)
			).round * SampleDur.ir
		 ) ! 2;
		Out.ar(0, signal * env);
}).store;
)

(
m = SynthDesc.readDef(\DSS_4); if (m.metadata.isNil) { m.metadata = (); };
m.metadata[\specs] = (
	freqA: ControlSpec(20, 2500, \lin, 0, 100 ),
	freqB: ControlSpec(20, 2500, \lin, 0, 100),
	\maxDurSt: ControlSpec(0.01, 1, \exp, 0, 0.1),
	\maxAmp: ControlSpec(0.001,1, \exp, 0, 0.5),
	\maxAmpSt: ControlSpec(0.01, 1, \exp, 0, 0.1),

);

// Ignore key list
m.metadata[\noGui] = [\gate];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\DSS_4, m.metadata);
);

// Test SynthDef
EmbedGui(\DSS_4).makeGui;




