// SOUND 1,  from orig Influx Example
(
Ndef(\dog).addSpec(\intv, [-24, 24]);
Ndef(\dog).addSpec(\timescale, [0.01, 10, \exp]);
Ndef(\dog).addSpec(\curve, [-10, 10]);
Ndef(\dog).addSpec(\ffreq, \freq);

Ndef(\dog).addSpec(\loopnode, [0, 12]);
Ndef(\dog).addSpec(\dens, [10, 1000]);
Ndef(\dog).addSpec(\resfreq, \freq);
Ndef(\dog).addSpec(\ringtime, [0.01, 1, \exp]);

Ndef(\dog, { |amp, freq=250, intv=19, timescale=1, curve=0, loopnode=0, dens = 10, resfreq = 500, ringtime = 0.01|
	var n = 15;
	var env = EnvGen.kr(
		Env({ 1.0.rand2 }!(n+1), {1.0.rand}!n, curve, releaseNode: n-1, loopNode: loopnode.min(n-3)),
		timeScale: timescale);
	var pitch = (env * [1, 0.33, -1] * intv).midiratio * freq;
	var form = Formant.ar(pitch, pitch.rotate(1) * 1.6, pitch.rotate(2) * 2.6);

	var dusty = Ringz.ar(
		Dust.ar(dens!3), 
		resfreq * LFNoise0.kr([3, 5, 7], 0.5, 1), 
		ringtime) 
		* ((dens ** -0.5) * (ringtime ** -0.3)
	);
	
    Splay.ar(form * dusty, level: amp) ;
});
);

Ndef(\dog).addHalo(\orderedNames, Ndef(\dog).controlKeys);

/*
Ndef(\dog).controlKeys;
Ndef(\dog).vol_(0.5);
NdefPreset( Ndef(\dog) ).loadSettings;
Ndef(\dog).play;
Ndef(\dog).gui;
*/