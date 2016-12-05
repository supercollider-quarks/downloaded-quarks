// SynthDef Definition

SynthDef(\BufferPlay, {
	arg out = 0, amp = 0, bufnum = 0, pitch = 0, start = 0, loop = 1, pan = 0, gate = 1;
	var aenv, sig;
	aenv	= EnvGen.kr(Env.asr(0.005,1,0.05,-2), gate: gate, doneAction: 2);
	sig	= PlayBuf.ar(1, bufnum, pitch.midiratio * BufRateScale.kr(bufnum), 1, start, loop);
	sig	= Pan2.ar(sig,pan, aenv * amp);
	Out.ar(out,sig);
}).store;



(
// Call and process SynthDef metadata
m = SynthDesc.readDef(\BufferPlay); if (m.metadata.isNil) { m.metadata = (); };

if (m.metadata[\specs].isNil) { m.metadata[\specs] = ();  };

m.metadata[\specs] = (
	pitch: ControlSpec(-40, 40),
	start: ControlSpec(0, 1.0),
	amp: ControlSpec(0, 1.0)
);

// Ignore key list
m.metadata[\noGui] = [\gate, \loop];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\BufferPlay, m.metadata);
);


s.boot;
// Test SynthDef
EmbedGui(\BufferPlay).makeGui;







