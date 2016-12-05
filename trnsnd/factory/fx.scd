
// must first
s.boot;

// generate
Fx2Factory.make;
// to make some source signals
GrainFactory.make;

// browse. look for prefix fx2_
SynthDescLib.global.browse;

// check parameters
SynthDescLib.at(\fx2_verb).controlNames;

(
// source
Pbind(
	\instrument, \g_sine_perc_pan2,
	\dur, 0.25,
	\degree, Pseries(0, 1, 7).loop
).play;
)
// reverb
x = Synth.tail(s, \fx2_verb, [room: 1, damp: 1]);
x.set(\xfade, 0.25);
x.set(\xfade, 0);
x.set(\xfade, 1);
x.set(\damp, 1);
x.set(\damp, 0);
x.set(\room, 0);
x.set(\room, 1);
x.release(3);

// tape echo
SynthDescLib.at(\fx2_tape).controlNames;
x = Synth.tail(s, \fx2_tape, [del: 0.3, rotate: -0.2, fb: 0.5]);
x.set(\fb, 0.1);
x.set(\fb, 0.9);
x.set(\rotate, 0.1);
x.set(\del, 0.03);
x.release(0.1);

// slicer
SynthDescLib.at(\fx2_slice).controlNames;
x = Synth.tail(s, \fx2_slice, [dur: 0.5/3]);
x.release(0.1);
x = Synth.tail(s, \fx2_slice, [dur: 1/3]);
x.release(0.1);
x = Synth.tail(s, \fx2_slice, [dur: 0.25/3]);
x.set(\rate, 0.5);
x.set(\rate, 2);
x.release(0.1);