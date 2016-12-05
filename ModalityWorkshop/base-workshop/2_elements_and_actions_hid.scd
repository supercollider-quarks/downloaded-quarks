/// Now that we have a device active, what can we do with it?
k = MKtl( 'myGP', "*drive" );

// print a nice list of elements
k.postElements;

// the elements are in a group and organized hierarchically:
k.elementGroup
// the buttons are a group in elementGroup
k.elementGroup[\bt]
// hierarchical access works best with elAt:
k.elAt(\bt);
k.elAt(\bt, \1); // button names as printed on device
k.elAt(\bt, 0); // also access by index -  begins with 0
// direct access by full element name
k.dictAt(\bt_1);

// the joysticks are hierarchical - two joysticks with each x and y:
k.elAt(\joy);
k.elAt(\joy, \l);
k.elAt(\joy, \r);
k.elAt(\joy, \l, \x);
k.elAt(\joy, nil, \x); // list of both x-axes


// getting the current value:
k.elAt(\joy, \l, \x).value;


//Add an action to the element:
k.elAt(\joy, \l, \x).action_({ |elem| elem.value.postln })

//to remove the action
k.elAt(\joy, \l, \x).resetAction;

// example with sound:
s.boot;

(
SynthDef(\blippy, {
    var out = Blip.ar(\freq.kr(440).lag(0.1), \numHarmonics.kr(100), \amp.kr(1.0).lag(0.1) ) * 0.5;
    Out.ar(0, Pan2.ar(out, \pan.kr(0).lag(0.1) ) )
}).add
);

//start it
x = Synth(\blippy, [\freq, 440, \numHarmonics, 100, \amp, 0.5, \pan, 0]);

(
//clear all actions first
k.resetAction;

// left joystick:
y-axis -> amp, 0.5 is rest position, so we rescale to range of [-1,1]
k.elAt(\joy, \l, \y).action_({ |elem| x !? _.set(\amp, (elem.value - 0.5 * 2).squared)});

//x-axis -> pan
k.elAt(\joy, \l, \x).action_({ |elem| x !? _.set(\pan, elem.value.linlin(0.0, 1.0, -1.0, 1.0) ) });

//other joystick y -> frequency
k.elAt(\joy, \r, \y).action_({ |elem| x !? _.set(\freq, elem.value.linlin(0.0, 1.0, 2000, 50) ) });

//other joystick x -> number of harmonics
k.elAt(\joy, \r, \x).action_({ |elem| x !? _.set(\numHarmonics, elem.value.linexp(0.0, 1.0, 1, 50) ) });

// button 6 (right shoulder) -> start and stop synth
k.elAt(\bt, \6).action_({  |elem|
    if(elem.value.booleanValue) {
        x !? _.free;
        x = Synth(\blippy, [\freq, 440, \numHarmonics, 100, \amp, 0.5, \pan, 0])
    } {
        x !? _.free;
        x = nil;
    }
})
)
);

// clear all functions
k.resetAction;
// stop the synth:
x.free;


// assigning multiple actions can be done with .addAction;
(
k.elAt(\joy, \l, \x).addAction({ |elem| ("1 :"++elem.value).postln });
k.elAt(\joy, \l, \x).addAction({ |elem| ("2 :"++elem.value).postln });
)
//clear all
k.elAt(\joy, \l, \x).resetAction;

/// to remove them you need to have a reference to the function
(
f = { |elem| ("1 :"++elem.value).postln };
g = { |elem| ("2 :"++elem.value).postln };
k.elAt(\joy, \l, \x).addAction(f);
k.elAt(\joy, \l, \x).addAction(g);
)

//remove first action
k.elAt(\joy, \l, \x).removeAction(f); // just 2 is left

//clear all
k.elAt(\joy, \l, \x).resetAction;


/// =========== output to elements =============

// check which inputs are there:
k.inputElements;

// check which outputs are there:
k.outputElements;


// sending data to the output elements (may not work properly on OSX with HID)
// value range between 0 and 1, will be mapped according to the spec given in the description file
k.elAt(\rumble, \l).value = 0.75;
k.elAt(\rumble, \l).value = 0;
k.elAt(\rumble, \r).value = 0.5;
k.elAt(\rumble, \r).value = 0;
