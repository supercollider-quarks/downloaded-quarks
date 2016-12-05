s.boot;

// get access to an MKtl. This example uses a nanoKontrol2.
// You should be able to adapt it to your available controller quite easily.
// For a more detailed version of this file, see
"Tutorials/ModalityTutorial".openHelpFile;

// open the MKtl
k = MKtl('nk2', "korg-nanokontrol2");

// make a gui for the mktl
k.gui;

// create a Synth to control
(
SynthDef(\blippy, {
    var sound = Blip.ar(
        \freq.kr(440).lag(0.1),
        \numHarmonics.kr(100),
        \amp.kr(1.0).lag(0.1)
    ) * 0.5;

    sound = RLPF.ar(sound, 5000);
    Out.ar(0, Pan2.ar(sound, \pan.kr(0).lag(0.1) ) )
}).add;
)

(
//start it
x = Synth(\blippy, [\freq, 440, \numHarmonics, 100, \amp, 0.5, \pan, 0]);

//clear all actions first
k.reset;

//knob 1 -> amplitude
k.elAt(\kn, 0).action_({ |el| x.set(\amp, el.value) });


//Alternative syntax:
k.elAt(\kn, 0).action = { |el| x.set(\amp, el.value) };


//knob 2 -> pan
k.elAt(\kn, 1).action_({ |el| x.set(\pan, el.value.linlin(0.0, 1.0, -1.0, 1.0) ) });

//slider 1 -> frequency
k.elAt(\sl, 0).action_({ |el| x.set(\freq, el.value.linlin(0.0, 1.0, 100, 400) ) });

//slider 2 -> number of harmonics
k.elAt(\sl, 1).action_({ |el| x.set(\numHarmonics, el.value.linexp(0.0, 1.0, 5, 200) ) });
)
