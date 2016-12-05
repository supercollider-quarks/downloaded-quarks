// SynthDef Definition
(
SynthDef(\Gendy_4, {
	| freqA = 100, freqB = 100, maxDurSt1 = 0.1, maxDurSt2 = 0.5, durRange = 0.1
	maxAmp = 0.5, maxAmpSt1 = 0.1, maxAmpSt2 = 0.5, ampRange = 0.2, gate = 1 |

	var maxDur, minDur,freqs,durSpace,ampSpace, numBPs, nGendys, env;

	numBPs         = 18;
	nGendys        = 3;

	ampSpace       = maxAmp * 0.5 * ampRange; // 0.5 is an optional scalar to divide to total amplitude by 2 for each gravitation point.
	maxAmpSt2      = maxAmp * 2 * maxAmpSt2;
	freqs	       = [ min(freqA, freqB), max(freqA, freqB) ];
	maxDur	       = 44100 / freqs[0] / numBPs;
	minDur	       = 44100 / freqs[1] / numBPs;
	maxDurSt2      = (maxDur - minDur).abs * maxDurSt2;
	durSpace       = (maxDur - minDur).abs * 0.5 * durRange; // 0.5 is an optional scalar to divide to total amplitude by 2 for each gravitation point.

	env = Linen.kr(gate, 0.01, 1, 0.1, 2);
Out.ar(0,env * Splay.ar({DemandEnvGen.ar(
		Dswitch1({
			// breakpoint amplitude control
			var rw1 = Dstutter(2,Dbrown(0,1.0, maxAmpSt1));
			var bn1 = Dstutter(2,Dif(rw1*2-1,4,0,maxAmp.neg,maxAmp));
			Dbrown(bn1-ampRange,bn1+ampRange,rw1 * maxAmpSt2);
			} ! numBPs,
			Dseq((0,1..(numBPs-1)), inf)),
		Dswitch1({
			// breakpoint interpolation duration control
			var rw1 = Dstutter(2,Dbrown(0, 1.0, maxDurSt1)).asStream;
			var bn1 = Dstutter(2,Dif(rw1*2-1,4,0,minDur, maxDur));
			Dbrown(bn1-durSpace,bn1+durSpace,rw1*maxDurSt2).round * SampleDur.ir;
			} ! numBPs,
			Dseq((0,1..(numBPs-1)), inf)),
		3) * (1 - (ampRange * 0.5)); // amp range amplitude correction // linear interpolation

	}!nGendys));
}).store;
)
// Call and process SynthDef metadata

(
m = SynthDesc.readDef(\Gendy_4); if (m.metadata.isNil) { m.metadata = (); };
m.metadata[\specs] = (
	freqA: ControlSpec(20, 2500, \lin, 0, 100 ),
	freqB: ControlSpec(20, 2500, \lin, 0, 100),
	\maxDurSt1: ControlSpec(0.01, 1, \exp, 0, 0.1),
	\maxDurSt2: ControlSpec(0.01, 1, \exp, 0, 0.1),
	\durRange: ControlSpec(0, 1, \lin, 0, 0.1),
	\maxAmp: ControlSpec(0.001,1, \exp, 0, 0.5),
	\maxAmpSt1: ControlSpec(0.01, 1, \exp, 0, 0.1),
	\maxAmpSt2: ControlSpec(0.01, 1, \exp, 0, 0.1),
	\ampRange: ControlSpec(0, 1, \lin, 0, 0.1)
);

// Ignore key list
m.metadata[\noGui] = [\gate];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\Gendy_4, m.metadata);
);

// Test SynthDef
EmbedGui(\Gendy_4).makeGui;

{ SinOsc.ar() }.play
s.makeGui; s.meter;


