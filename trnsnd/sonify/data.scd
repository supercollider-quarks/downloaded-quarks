~d = Data.new;
~d.readCSV(F.dir +/+ "canbus.csv", 1);
// dist, t, str angle, str vel, accel pos, accel speed
~d.info;
~d.plot;
~d.plot(overlay: true);
~d.sampleRate = 100;
~d.sampleDur;
~d.leakDC;
~d.lpf(0, 3);
~d.normalize2;
~d.writeSoundFile("/Users/yota/Desktop/canbus.wav");

GrainFactory.make;
SynthDescLib.global.browse;
(
p = Pbind(
	\type, \grain,
	\instrument, \g_sine_hann_pan2,
	\freq, ~d.map(3, [100, 1300, \exp]),
	\phase, Pwhite(0, 2pi),
	\dur, ~d.sampleDur,
	\pan, Pwhite(-0.8, 0.8),
	\legato, 5,
);
)
p.play;

//
p.render;
(
p = Pbind(
	\instrument, \g_sine_hann_pan2,
	\freq, ~d.map([3,5], [100, 1300, \exp]),
	\phase, Pwhite(0, 2pi),
	\dur, ~d.sampleDur,
	\pan, [-1, 1],
	\legato, 5
);
)
p.play;

(
p = Pbind(
	\type, if(~d.map(4).dif < 0.0001, \rest, \grain),
	\instrument, \g_sinefb_perc_pan2,
	\degree, ~d.map(4, [-10, 5, \lin, 1]),
	\fb, ~d.map(4, [0.9, 0]),
	\phase, Pwhite(0, 2pi),
	\dur, ~d.sampleDur,
	\pan, ~d.map(4) * Prand([-1, 1], inf),
	\legato, ~d.map(4, [20, 80, \exp])
);
)
p.play;


// realtime
~d.readCAN;
~d.readDrivingForceGT;
~d.readHID;
~d.readOSC;
~d.readArduino;
~d.readSerial;
~d.readAudio;

// read & write at the same time
~d.writeCSV;