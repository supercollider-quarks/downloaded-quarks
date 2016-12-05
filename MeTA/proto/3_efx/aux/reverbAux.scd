Ndef(\reverb).addSpec(\t60, [0.1, 60, \exp]);
Ndef(\reverb).addSpec(\damp, [0, 1]);
Ndef(\reverb).addSpec(\size, [0.5, 3]);
Ndef(\reverb).addSpec(\earlydiff, [0, 1]);
Ndef(\reverb).addSpec(\mdepth, [0, 50]);
Ndef(\reverb).addSpec(\mfreq, [0, 10]);
Ndef(\reverb).addSpec(\lowx, [0, 1]);
Ndef(\reverb).addSpec(\midx, [0, 1]);
Ndef(\reverb).addSpec(\highx, [0, 1]);
Ndef(\reverb).addSpec(\lowband, [100, 6000, \exp]);
Ndef(\reverb).addSpec(\highband, [1000, 10000, \exp]);

// s.sync;
// Ndef('reverb').set('lowx', 0.52777777777778, 't60', 8.2004470369725, 'damp', 0.53333333333333, 'size', 0.54166666666667, 'highband', 7233.9416273667, 'mdepth', 16.944444444444, 'highx', 1.0, 'mfreq', 0.16666666666667, 'lowband', 681.56675509913, 'fadeTime', 1, 'earlydiff', 0.7, 'midx', 0.88888888888889);


m.aux[\reverb] = Ndef(\reverb, {
	var in = Splay.ar(\in.ar(0!m.config.numChans));
	JPverb.ar(
		in,
		\t60.kr(1,           0.05),
		\damp.kr(0,          0.05),
		\size.kr(1,          0.05),
		\earlydiff.kr(0.707, 0.05),
		\mdepth.kr(5,        0.05),
		\mfreq.kr(2,         0.05),
		\lowx.kr(1,          0.05),
		\midx.kr(1,          0.05),
		\highx.kr(1,         0.05),
		\lowband.kr(500,     0.05),
		\highband.kr(2000,   0.05)
	);

	// TODO: spread to q.numChans
});

Ndef(\reverb).play;


Ndef(\reverb); // return value