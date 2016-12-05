// SynthDef Definition

SynthDef(\SineBleep, { | out, freq = 440, amp = 0.1, gate = 1, atk = 0.003, rel = 0.4, curveParam = -4, pan = 0|
	var env = EnvGen.kr(Env.asr(atk, 1, rel, curveParam), gate, doneAction: 2);
	var sig = SinOsc.ar(freq, mul: amp * env);
	sig = Pan2.ar(sig, pan);
	Out.ar(out, sig);
}).store;

// Call and process SynthDef metadata

m = SynthDesc.readDef(\SineBleep); if (m.metadata.isNil) { m.metadata = (); };

// Assign specs
(
if (m.metadata[\specs].isNil) { m.metadata[\specs] = ();  };

m.metadata[\specs][\atk] = ControlSpec(0.001, 2, \exp);
m.metadata[\specs][\rel] = ControlSpec(0.01, 2, \exp);
m.metadata[\specs][\curveParam] = ControlSpec(-20, 0,);

// Ignore key list
m.metadata[\noGui] = [\gate];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\SineBleep, m.metadata);
);

// Test SynthDef
EmbedGui(\SineBleep).makeGui;