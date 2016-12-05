/// Creating an MKtlElement that is based on values of other MKtlElements.
// The example converts values from x and y controls
// to polar.


// Choose two Elements of a device you have at hand
(
a = MKtl(\a, "thrustmaster-run-n-drive");
	// left joystick
x = a.elementAt(\joy, \l, \x);
x.action_(MFunc());
y = a.elementAt(\joy, \l, \y);
y.action_(MFunc());
)

a.gui;

// or alternatively, make some free MKtl elements by hand:

(
x = MKtlElement(\x, (spec: [-100, 100], type: \joyAxis, ioType: \in));
x.action_(MFunc());  // use an MFunc for convenience and flexibility

y = MKtlElement(\y, (spec: [-100, 100], type: \joyAxis, ioType: \in));
y.action_(MFunc());
)

// make a Slider2D to set x and y from gui
(
Slider2D(nil, Rect(0,0,200,200)).action_({ |sl2d|
	x.valueAction_(sl2d.x); y.valueAction_(sl2d.y);
}).front;
)


(
	// add logging to x and y
x.action.add(\post, { |el| [el.name, el.value].postln });
y.action.add(\post, { |el| [el.name, el.value].postln });
)

	// disable logging
[x, y].do { |el| el.action.disable(\post) };
	// enable logging again
[x, y].do { |el| el.action.enable(\post) };


// make a derived element z, which converts x and y to polar angle
(
z = MKtlElement(\polar, (spec: [-pi, pi], type: \xyPolar, ioType: 'filter'));

	// x and y trigger z.action
[x, y].do { |el| el.action.add(\toPolar, { z.doAction }) };

	// define z.action
z.action = MFunc();

	// add function to get x and y values, calculate angle
z.action.add(\calcAngle, { |polar|
	var xy = [x.deviceValue, y.deviceValue];
	var angle = atan2(x.deviceValue, y.deviceValue);
	// [xy, angle].round(0.001).postln;
	polar.deviceValue_(angle);
});
)

(
	// do something with it, e.g. post it
z.action.add(\postAtan2, { |polar|
	"polar atan2 value: % \n".postf(polar.deviceValue);
});
)

// use z like any MKtlElement, e.g. in a running pattern:
// - move 2dslider while pattern plays,
// - and watch its normalized value
a = Pbind(\polar, z).trace.play;
a.stop;

// get the angle value from z, and use that for other things,
// e.g. to calculate other param values with it
a = Pbind(
	\angle, Pfunc({ z.deviceValue }),
	\panPos, Pkey(\angle) * 100
).trace.play;
a.stop;