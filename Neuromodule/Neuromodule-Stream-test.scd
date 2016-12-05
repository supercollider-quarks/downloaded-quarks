

// create a new module
(
~module = Neuromodule(
	x: [0.1, 0],
	theta: [0.1, 0.3],
	weights: [[-3, 2], [-2, 0]],
	outs: 0
);
)

// forward time
~module.next;
~module.next;
~module.next;

// or (this is implemented in a superclass)
~module.nextN(8); // 8 values
~module.nextN(128).plot;


// sonification: interactive, rewrite while running ...

// synthdef
(
SynthDef(\gabor, { |out, freq=440, sustain=0.1, amp = 0.1, width = 0.1|
	var sig, env;
	env = LFGauss.ar(sustain, width, loop:0, doneAction: 2).range;
	sig = SinOsc.ar(freq, 0.5pi) * env * amp;
	OffsetOut.ar(out, sig);
}).add;
)

(
Tdef(\x, {
	loop {
		var dt = 0.1;
		(
			instrument: \gabor,
			note: ~module.next * 4,
			sustain: dt * 1.5, // 50% overlap
			width: 0.3
		).play;

		dt.wait;
	};
}).play
)

// play with the parameters



// fig. 82
~module.theta = [2, -1]; ~module.weights = [[-3, 2], [-2, 0]];
~module.weights = ~module.weights + [0.2, -0.1];
~module.weights = ~module.weights + [-0.2, 0.1];
~module.theta = [2, -1]; ~module.weights = [[-3.2, 2], [-2.2, 0.5]];

// different scaling
~module.outs = [0, 1]; //  two dimensions

(
Tdef(\x, {
	loop {
		var dt = 0.001; // maximum is about 1 ms. more will be problematic...
		(
			instrument: \gabor,
			freq: ~module.next.linexp(-2, 3, 300, 10000),
			sustain: dt * 1.5, // 50% overlap
			width: 0.6,
			out: [0, 1] // map to two audio channels
		).play;

		dt.wait;
	};
}).play
)





// a GUI:

(
~makeWindow = { |module|

	var w = Window("module", Rect(200, 300, 400, 30 * module.numCells + 100));
	var l = VLayout();
	var h = HLayout(l);
	var xsl = Set[], ysl = Set[];
	var sliders, twod;
	var update = { |index, val|
		sliders[index].value = val;
		module.theta[index] = val * 2 - 1;
		module.theta.postln;
	};
	w.layout = h;
	w.front;

	h.add(
		twod = Slider2D().action_({ |v|
			xsl.do { |i| update.(i, v.x) };
			ysl.do { |i| update.(i, v.y) };
		});
	);

	sliders = module.theta.collect { |x, i|
		var sl = Slider();
		sl.action = { |v|
			update.(i, v.value);
			if(xsl.includes(i)) { twod.x = v.value };
			if(ysl.includes(i)) { twod.y = v.value };
		};
		sl.value = x + 1 / 2;
		sl.orientation = \vertical;

		sl
	};

	sliders.do { |sl, i|
		var ll = VLayout();

		ll.add(
			Button().states_([["x"], ["y"], [" "]]).action_({ |b|
				if(b.value == 0) { ysl.remove(i);xsl.add(i); sl.value = twod.x; };
				if(b.value == 1) { xsl.remove(i); ysl.add(i); sl.value = twod.y; };
				if(b.value == 2) { xsl.remove(i); ysl.remove(i) };
			})
			.valueAction_(2);

		);
		ll.add(sl);
		h.add(ll);
	};


};
~makeWindow.(~module);
)

// modules are streams (see Stream help)

~m1 = Neuromodule([0.1, 0],  [0.1, 0.3], [[-3, 2], [-2, 0]], 0);
~m2 = Neuromodule([0.1, 0],  [0.5, -0.1], [[-3, 2], [2, 0]], 0);

~z = ~m1.sin + ~m2.squared;
~z.nextN(8);


// merging two modules:
~m1 = Neuromodule([0.1, 0],  [0.1, 0.3], [[-3, 2], [-2, 0]], 0);
~m2 = Neuromodule([0.1, 0],  [0.5, -0.3], [[-3, 2], [2, 0]], 0);
~m3 = merge(~m1, ~m2);



~module.addAll(~m3); // combine with the one further up
~makeWindow.(~module);


// rewiring:

~m = Neuromodule(0 ! 4).fillMissing; // simplest empty module
~m.rewire([[0, 0, -1], [1, 0, 2]]);
~m.rewire([[0, 0, 0.1 ], [1, 0,  0.1]], { |prev, new| prev.neg + new }); // passing in a function



// combining modules in one way direction works like this:
~m1 = Neuromodule([0.1, 0],  [0.1, 0.3], [[-3, 2], [-2, 0]], 0);
~m2 = Neuromodule([0.1, 0],  [~m1, 0.3], [[-3, 2], [-2, 0]], 0);


~m2.nextN(8);


// combining modules in a concurrent way
~m1 = Neuromodule([0.1, 0],  [{ ~m2.x[1] }, 0.3], [[-3, 2], [-2, 0]], 0);
~m2 = Neuromodule([0.1, 0],  [0.1, { ~m2.x[0] }], [[-3, 2], [-2, 0]], 0);

Tdef(\x, { loop { 0.1.wait; "m1: ".post; ~m1.next.postln; } }).play;
Tdef(\y, { loop { 0.15.wait; "m2: ".post; ~m2.next.postln; } }).play;

// sonifying them in stereo
(
Tdef(\x, {
	var dt = 0.1;
	loop { dt.wait; (
			instrument: \gabor,
			freq: ~m1.next.linexp(-2, 2, 300, 10000),
			sustain: dt * 1.5, // 50% overlap
			width: 0.6,
			out: 0 // left
		).play; }
}).play;
Tdef(\y, {
	var dt = 0.14;
	loop {
		dt.wait; (
			instrument: \gabor,
			freq: ~m2.next.linexp(-2, 2, 300, 3000),
			sustain: dt * 1.5, // 50% overlap
			width: 0.6,
			out: 1 // right
		).play; }
}).play;
)

~m1.theta[1] = -1; ~m1.weights = [[-3, 2], [-2, 0]];
~m2.theta[0] = 1; ~m1.weights = [[3, -2], [0, 2]];
