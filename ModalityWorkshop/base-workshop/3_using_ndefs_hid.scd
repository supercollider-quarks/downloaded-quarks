// Ndef is a named placeholder for a sound process
Ndef(\x ).play;

// put a sound there
Ndef(\x, { GrayNoise.ar } ).play;

// replace it with a more specific version, e.g. softer?
Ndef(\x, { GrayNoise.ar([0.2, 0.2]) } ).play;

// or a different sound
Ndef(\x, { Dust.ar([30, 30], 0.5 ) } ).play;

// add a control input, as in all synth functions
Ndef(\x, { |amp = 0.1| Dust.ar([30, 30], amp ) } ).play;

// and a control input for density:
Ndef(\x, { |dens 30, amp = 0.1| Dust.ar([dens, dens], amp ) } ).play;

// make a simple gui for it, NdefGui
Ndef(\x).gui;
// this guesses a range for the dens parameter;
// one can also set the spec globally:
Spec.add(\dens, [1, 1000, \exp]);

// set amp directly
Ndef(\x).set(\amp, 0.2);
// or set amp from unipolar value, by mapping thu \amp spec
Ndef(\x).setUni(\amp, 0.5);

// the \amp warp is designed so that faders have -12dB,
// which is 0.25 linear amp) in the middle, and it looks like this:
(0, 0.01 .. 1).collect(\amp.asSpec.map(_)).plot;

// same with density - to center of range:
Ndef(\x).setUni(\dens, 0.5);

// use a gui as pseudo-physical controller
// this requires the Quark "adclib"
m = MView(0.25);
// mview sets amp directly
m.action.add ( \setx, { |mv| Ndef(\x).set(\amp, mv.value) });

// mv sets amp thru \amp spec
m.action.add ( \setx, { |mv| Ndef(\x).setUni(\amp, mv.value) });

// change Ndef(\x) to have a freq input (which has a default spec)
Ndef(\x, { |amp = 0.1, freq = 440| RLPF.ar(GrayNoise.ar([amp, amp]), freq, 0.2) } ).play;

// change mview's action to set x freq by default spec
m.action.add ( \setx, { |mv| Ndef(\x).setUni(\freq, mv.value) });

// rather than using a global spec,
// one can locally tell the Ndef which spec to use for a param:
Ndef(\x).addSpec (\freq, [200, 20000 ]);

// oh, this was linear, better make it exponential:
Ndef(\x).addSpec (\freq, [200, 20000,\exp ]);

// or e.g. invert the range ...
Ndef(\x).addSpec (\freq, [20000, 200,\exp ]);


// Now with Modality, control process params from an MKtl
HIDMKtl.find;
// the MKtl we want to use:
k = MKtl( 'gp', "*drive" ); // match name for a thrustmaster run'n'drive

// none at hand? then get use its replacement gui:
k.gui;

// all its elements are in a hierarchical group,
// and one can go down to individual elements:
k.elements;

// post element names in hierarchical form:
k.postElements;

k.elAt(\joy);			// the joystick
k.elAt(\joy, \l);		// the left joystick
k.elAt(\joy, \l, \x);	// a single element: the x axis of the left joystick
k.dictAt(\joy_l_x);		// direct access also in a dict

//control a parameter
k.elAt(\joy,\l, \x).action = { |el| Ndef(\x).setUni(\freq,  el.value) };

// if this feels too slow, try setting
// server latency to nil, or a low number like 0.03 secs
s.latency = nil;
s.latency = 0.03;


// make a start / stop button
(
k.elAt(\bt, \1).action = { |el|
	// only toggle on button down
	if(el.value==1){
		// ask the object for its state, then toggle
		if (Ndef(\x).monitor.isPlaying) {
			Ndef(\x).stop
		} {
			Ndef(\x).play
		}
	}
};
)

