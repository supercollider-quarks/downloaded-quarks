/////////////////
// access a keyboard and play a basic multitimbral sound
// uses NPVoicer from the JITLibExtensions quark
/*
Quarks.install("JITLibExtensions");
*/

// boot server
s.boot;
s.latency = nil; // make sure there is no artificial latency


m = MKtl('op1', "teenage-engineering-op-1");
// gui to see all elements and names
m.gui;
(
(
SynthDef(\a, {|out = 0, amp = 0.25, midiNote = 50, bendFac = 0.5, res = 0, ffac = 1, gate = 1|
	var freq = (midiNote + (12 * bendFac)).midicps;
	var env = EnvGen.kr(Env.adsr(0.1, releaseTime: 0.1), gate, doneAction: 2);
	var src = Saw.ar(freq) * env;
	var ffreq = freq * ffac;

	var rq = 0.1 ** res.clip(0, 1);
	src = RLPF.ar(src, ffreq, rq);
	// original used this filter:
	// src = MoogLadder.ar(src, ffreq, res);


	Out.ar(out,
		src * amp !2;
	)
}).add;
);
// set up the sound
g = NPVoicer(Ndef(\piano)); // make an NPVoicer with an Ndef in it
g.play;                     // play it
NdefMixer(s);               // sound control
g.prime(\a);            // prepare it with the synthdef a
)

// test NPVoicer
g.put(55, [\midiNote, 55]);
g.put(64, [\midiNote, 64]);
g.release(55);
g.release(64);
g.release(0);

m.postElements;

(
// keys do noteOn
~onAction = {|el|
	var note = el.elemDesc.midiNum; "on: %\n".postf(note);
	g.put(note, [\midiNote, note, \amp, el.value.squared]);
};
m.elAt(\key, \all, \on).do(_.action_(~onAction));

// and noteOff
~offAction = {|el|
	var note = el.elemDesc.midiNum;  "off: %\n".postf(note);
	g.release(note);
};
m.elAt(\key, \all, \off).do(_.action_(~offAction));

// bending
m.elAt(\bend).action_{|el|
	Ndef(\piano).set(\bendFac, el.value);
};
// first knob changes resonance
m.elAt(\kn, \turn, 0).action_{|el|
	Ndef(\piano).set(\res, el.value.linlin(0, 1, 0, 1).postln)
};

m.elAt(\kn, 1).action_{|el|
	Ndef(\piano).set(\ffac, el.value.linexp(0, 1, 0.125, 8).postln)
};
)

// now hit one of its keys, or simulate:
g.play;

m.elAt(\key, 64, \on).valueAction_(0.5);
m.elAt(\bend).valueAction_(0.75);
m.elAt(\bend).valueAction_(0.5);

m.elAt(\kn, \turn, 0).valueAction_(0.5);

m.elAt(\kn, \turn, 1).valueAction_(0.7);
m.elAt(\key, 64, \off).valueAction_(0);

m.elAt(\key, 67, \on).valueAction_(0.25);
m.elAt(\key, 67, \off).valueAction_(0.25);


Tdef(\noodle, {
	g.play;
	inf.do {
		var dt = 0.25;
		var note = [0, 2, 5, 10].choose + 60;
		var elemOn = m.elAt(\key, note, \on);
		var elemOff = m.elAt(\key, note, \off);
		elemOn.valueAction_(0.5);
		defer ({ elemOn.valueAction_(0) }, dt);
		dt.wait;
	};
}).play;

