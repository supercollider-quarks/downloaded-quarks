// example for conditional control:
// control elements - here, gamepad axes
// only apply to a target - here an Ndef
// when their respective buttons are held down.

// to test this with gui only,
// focus on bt1 and press key 'T' to switch it to toggle mode,
// and do the same with bt 2.


// make an MKtl gamepad
m = MKtl(\gp, "*drive"); m.gui;
m.trace;

m.trace(false);

s.boot;
s.latency = nil; // fast response to control

// make an Ndef to play
(
Ndef(\fSine).addSpec(\freq, [200, 800, \exp]);
Ndef(\fSine).addSpec(\amp, [0, 1]);
Ndef(\fSine).addSpec(\modAmp, [0, 1]);
Ndef(\fSine).addSpec(\modIndex, [1, 10, \lin, 1]);
Ndef(\fSine).addSpec(\spread, [0, 0.7]);
Ndef(\fSine).addSpec(\lag, [0.1, 10, \exp]);
Ndef(\fSine, {
	var lag = \lag.kr(1);
	var freq = \freq.kr(0, lag);
	var modFreq = freq * \modIndex.kr(0, lag);
	var modAmp = \modAmp.kr(0, lag);
	var src;

	freq = freq + [0, \spread.kr(0)];

	src = SinOsc.ar(
		freq +
		(SinOsc.ar(modFreq, mul: modAmp) * 1000)

	) * \amp.kr(0, lag);

	Pan2.ar(src, SinOsc.ar(0.0124, [0, 0.5pi])).sum;

}).play;

// copy sound and its specs to a second Ndef
Ndef(\fSine).copy(\fSine2);
Ndef(\fSine).getSpec.keysValuesDo { |key, spec|
	Ndef(\fSine2).addSpec(key, spec.copy);
};
Ndef(\fSine2).play;
);

NdefMixer(s);

q = ();

// hold down buttons 1 and 2 to activate control on
// Ndef(\fSine) and Ndef(\fSine2)
// - no buttons, no control, both buttons, both controls
q.activated = [m.elAt(\bt, \1), m.elAt(\bt, \2)];

Ndef(\fSine).addHalo(\actBut, m.elAt(\bt, \1));
Ndef(\fSine2).addHalo(\actBut, m.elAt(\bt, \2));

q.ndefs = [Ndef(\fSine), Ndef(\fSine2)];
q.ndefs.do(_.play);

(
// left throttle sets amp if active
m.elAt(\throt, \l).action_{ |el|
	q.ndefs.do {|ndef|
		if (ndef.getHalo(\actBut).value > 0) {
			ndef.setUni(*[\amp, el.value].postln)
		}
	};
};
// joy stick left y-axis sets freq
m.elAt(\joy, \l, \y).action_{|el|
	q.ndefs.do {|ndef|
		if (ndef.getHalo(\actBut).value > 0) {
			ndef.setUni(\freq, el.value.linlin(0, 1, 1, 0))
		}
	};
};
// left x sets modIndex
m.elAt(\joy, \l, \x).action_{|el|
	q.ndefs.do {|ndef|
		if (ndef.getHalo(\actBut).value > 0) {
			ndef.setUni(\modIndex, el.value)
		}
	};
};

m.elAt(\throt, \r).action_{|el|
	q.ndefs.do {|ndef|
		if (ndef.getHalo(\actBut).value > 0) {
			ndef.setUni(\modAmp, el.value)
		}
	};
};
m.elAt(\joy, \r, \x).action_{|el|
	q.ndefs.do {|ndef|
		if (ndef.getHalo(\actBut).value > 0) {
			ndef.setUni(\spread, el.value)
		}
	};
};
)