/// Now that we have a device active, what can we do with it?

MKtl.find(\midi);
k = MKtl( 'icon', "*icontrols" ); // an iControls

// or if it is a nanoKontrol 2:
k = MKtl( 'nk2', "*nanokontrol2");
k.gui;

// print a nice list of elements
k.postElements;

// the elements are in a group and organized hierarchically:
k.elementGroup
// the buttons are a group in elementGroup
k.elementGroup[\bt]

// hierarchical access works best with elAt:
k.elAt(\bt); // all buttons
k.elAt(\sl); // sliders
k.elAt(\kn); // knobs

// access by name or index, fullname:
k.elAt(\sl);
k.elAt(\sl, 0);
k.elAt(\sl, \1);
k.dictAt(\sl_1);

// buttons are nested more deeply:
k.elAt(\bt, 'S'); // all buttons named 'S' as printed on device
k.elAt(\bt, 'S', \1); // button 'S' of first column
k.elAt(\bt, 0, 0); // also access by index -  begins with 0
k.dictAt(\bt_S_1);

// getting the current value:
k.elAt(\sl, 0).value;


//Add an action to the element:
k.elAt(\sl, 0).action_({ |elem| elem.value.postln })

//to remove the action
k.elAt(\sl, 0).resetAction;



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

//slider 0 -> amplitude
k.elAt(\sl, 0).action_({ |elem| x !? _.set(\amp, \amp.asSpec.map(elem.value)) });

//knob 0 -> pan
k.elAt(\kn, 0).action_({ |elem| x !? _.set(\pan, \pan.asSpec.map(elem.value)) });

//slider 1
k.elAt(\sl, 1).action_({ |elem| x !? _.set(\freq, elem.value.linlin(0.0, 1.0, 50, 2000) ) });

//knob 1 -> number of harmonics
k.elAt(\kn, 1).action_({ |elem| x !? _.set(\numHarmonics, elem.value.linexp(0.0, 1.0, 1, 50) ) });
)

(
// play button -> start and stop synth
k.elAt(\tr, \play).action_({  |elem|
    if(elem.value > 0) {
        x !? _.free;
        x = Synth(\blippy, [\freq, 440, \numHarmonics, 100, \amp, 0.5, \pan, 0])
    } {
        x !? _.free;
        x = nil;
    }
});
);

// clear all functions
k.resetAction;
// stop the synth:
x.free;


// assigning multiple actions can be done with .addAction;
(
k.elAt(\sl, 0).addAction({ |elem| ("1 :"++elem.value).postln });
k.elAt(\sl, 0).addAction({ |elem| ("2 :"++elem.value).postln });
)
//clear all
k.elAt(\sl, 0).resetAction;

/// to remove them you need to have a reference to the function
(
f = { |elem| ("1 :"++elem.value).postln };
g = { |elem| ("2 :"++elem.value).postln };
k.elAt(\sl, 0).addAction(f);
k.elAt(\sl, 0).addAction(g);
)

//remove first action
k.elAt(\sl, 0).removeAction(f);
// just 2 is left

//clear all
k.elAt(\sl, 0).action = nil;


/// =========== output to elements (if you have any on your midi device) =============

// check which inputs are there:
k.inputElements;

// check which outputs are there:
k.outputElements;

// sending data to the output elements
// value range between 0 and 1, will be mapped according to the
// deviceSpec given in the description file
// should set LEDs on all S buttons
/*
k.elAt(\bt, \S).do { |elem| elem.value_(1.0) };
k.elAt(\bt, \S).do { |elem| elem.value_(0.0) };
*/
