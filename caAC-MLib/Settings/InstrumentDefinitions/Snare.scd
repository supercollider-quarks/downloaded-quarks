// SynthDef Definition


(
SynthDef(\Snare, {
	| out,
	stretchFactor = 0.2, noiseAmp = 0.2,
	bodyShift = 0, bodyDistort = 1,
	freqPeak = 6900, rqPeak = 0.6, dbPeak = 10,
	distort = 1,

	pan=0, amp=0.1 |
	var env0, env1, env2, env1m, oscs, noise, sig;

	env0 = EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.03, 0.10 * stretchFactor], [-4, -2, -4]));
	env1 = EnvGen.ar(Env.new(midiratio(bodyShift) * [110, 60, 49], [0.005, 0.1 * stretchFactor], [-4, -5]));
	env1m = env1.midicps;
	env2 = EnvGen.ar(Env.new([1, 0.4, 0], [0.05, 0.13 * stretchFactor], [-2, -2]), doneAction:2);

	oscs = LFPulse.ar(env1m, 0, 0.5, 1, -0.5) + LFPulse.ar(env1m * 1.6, 0, 0.5, 0.5, -0.25);
	oscs = LPF.ar(oscs, env1m*1.2, env0);
	oscs = oscs + SinOsc.ar(env1m, 0.8, env0);
	oscs = distort(oscs * bodyDistort);

	noise = WhiteNoise.ar(noiseAmp);
	noise = HPF.ar(noise, 200, 2);
	noise = BPF.ar(noise, freqPeak, rqPeak, dbPeak.dbamp) + noise;
	noise = noise * env2;

	sig = oscs + noise;
	sig = sig.clip2(distort) * amp;

	Out.ar(out, Pan2.ar(sig, pan));
}
).store
)


// Call and process SynthDef metadata
(
m = SynthDesc.readDef(\Snare); if (m.metadata.isNil) { m.metadata = (); };

m.metadata[\specs] = (
	stretchFactor: ControlSpec(0.1,10,\exp),
	noiseAmp: \amp.asSpec,
	bodyShift: ControlSpec(-12,15),
	bodyDistort: ControlSpec(1,100),
	freqPeak: \freq.asSpec,
	rqPeak: \rq.asSpec,
	dbPeak: ControlSpec(-50,50),
	distort: ControlSpec(1,1000)
);
// Ignore key list
m.metadata[\noGui] = [];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\Snare, m.metadata);
);

// Test SynthDef
EmbedGui(\Snare).makeGui;