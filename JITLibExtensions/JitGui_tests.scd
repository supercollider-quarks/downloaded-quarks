/* JITLibExtensions allows adding specs to objects,
	and various JITGuis use that for controller ranges:
EnvirGui, ParamGui, NdefParamGui as used in TdefGui, PdefGui, NdefGui, NdefMixer etc.

From version 3.7.2 and 3.8, EnvirGui and its subclasses
moved from creating different views for different params
to the ParamView class (which encapsulates the switching).

This makes it necessary that the checkUpdate methods concerned
are compatible with pre-3.8 and post 3.8 versions of SC.

This file contains examples to test whether everything works in all combinations.

*/

// Tests for EnvirGui / ParamGui:

// simple example
g = EnvirGui.new(nil, 5);    // empty with 5 slots
g.parent.alwaysOnTop_(true);

// put some things in - gui will guess specs for numbers
g.object_((a: 1, b: \werty, freq: [500, 2000]));
g.envir.put(\karl1, \otto1);                // one more
g.envir.put(\caesar, \julius);                // one more

g.envir.putAll((b: -12, r: 1, s: 2, t: 3, u: 4, v: 5))

e = (x: 2);
g.object_(e);    // put another object in
// add specs to the envir:
e.addSpec(\x, [2, 200, \exp]);
e.addSpec(\x, [1, 10]);
g.close;


// ParamGui, as used in TdefGui
Tdef(\a);
g = TdefGui(Tdef(\a), 3);
g.envirGui.specs; // specs still empty
g.envirGui.object; // Tdef has no envir yet
Tdef(\a).set(\a, 12); // now it does
g.envirGui.object; // envir is there
g.envirGui.specs; // gui guessed this spec for \a
g.envirGui.getSpec(\a);

g.envirGui.specs
// envirgui paramview has the proper spec
// 3.8
try { g.envirGui.paramViews[0].slider.controlSpec };
// pre 3.8
try { g.envirGui.widgets[0].controlSpec };

// change spec -> slider view updates position, slider range changes
Tdef(\a).addSpec(\a, [1, 100, \lin]);
Tdef(\a).addSpec(\a, [1, 1000, \exp]);

g.envirGui.specs; // has same spec as:
Tdef(\a).getSpec(\a);



// Tests for NdefParamGui in NdefGui in NdefMixer

m = NdefMixer(s.boot, 5);
m.switchSize(2);
Ndef(\a).play;
g = m.editGui;
g.object_(Ndef(\a));
g.paramGui.specs;   // no specs so far
Ndef(\a).set(\freq, 300); // finds global specs
g.paramGui.getSpec(\freq);
// 3.7.2
try { g.paramGui.widgets[0].controlSpec };
// 3.8
try { g.paramGui.paramViews[0].spec };

Ndef(\a).addSpec(\freq, [30, 3000, \exp]); // overwrite global spec for Ndef(\a)
g.paramGui.getSpec(\freq);
Ndef(\a).addSpec(\freq, nil); // clear global spec again
g.paramGui.getSpec(\freq);
Ndef(\a).set(\freq, 2010); // change val

Ndef(\a).set(\amp, 0.2);  // finds global

Ndef(\a).set(\width, 0.2); // add new param, no global -> guesses spec
g.paramGui.getSpec;
	// set to a custom spec
Ndef(\a).addSpec(\width, [0.5, 0.99, -3]);

