DmFactory.make;

Pbind(\instrument, \dm_kTri, \db, -3).play;
Pbind(\instrument, \dm_kPar, \db, -3).play;
Pbind(\instrument, \dm_kSine, \db, -3).play;

Ndef.clear;
Ndef(\trig, { Impulse.ar(9) }).play;
Ndef(\x, { LFTri.ar(Ndef(\freq).ar(1), 0, Ndef(\amp).ar(1)).dup }).play;
Ndef(\amp, { Env.perc(0.001, 0.5).ar(0, Ndef(\trig)) });
Ndef(\freq, { Env.perc(0.001, 0.1).ar(0, Ndef(\trig), 1, 40, 40) });