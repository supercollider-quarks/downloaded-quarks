/// Methods for mapping on the server:

MKtl.find;

k = MKtl( 'lpd80' );
k.gui;

s.boot;

k.reset;

MIDIIn.noteOnZeroAsNoteOff_( false );

// we use Ndef in this example, to allow for a flexible redefining of the sound on the server

Ndef( \testSynth, { arg freq=440, amp=0.1, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );
Ndef( \testSynth ).play;


k.elementAt( \pad, 1 ).action = { arg el; Ndef( \testSynth ).set( \t_trig, 1, \amp, el.value ) };

//// mapping ranges:

/// linlin:
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.linlin( 0, 1, 0, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

k.elementAt( \pad, 1 ).action = { arg el; Ndef( \testSynth ).set( \t_trig, 1, \ampin, el.value ) };

// linexp
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.linexp( 0, 1, 0.01, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

// explin
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.explin( 0.1, 1.0, 0.1, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

// expexp
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.expexp( 0.1, 1.0, 0.01, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

// lincurve
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.lincurve( 0, 1, 0, 0.8, -4 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

// curvelin
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.curvelin( 0, 1, 0, 0.8, -4 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );


/// range:
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.range( 0, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

/// exprange:
Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.exprange( 0.01, 0.8 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );


/// clipping, folding, wrapping

Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.clip( 0, 0.2 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.fold( 0, 0.2 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );

Ndef( \testSynth, { arg freq=440, ampin=64, t_trig=1, dur=1; SinOsc.ar( freq, 0, ampin.wrap( 0, 0.2 ) ) * EnvGen.kr( Env.perc, t_trig, timeScale: dur ) } );


/// these are all direct... more interesting is also the behaviour over time:

k.elementAt( \pad, 1 ).action = { arg el; Ndef( \testSynth ).set( \amp, el.value ) };

k.elementAt( \pad, \off, 1 ).action = { arg el;  Ndef( \testSynth ).set( \amp, 0 ) };

// lag
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.lag( 0.5 ) ) } );

// lag up down
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.lag( 0.1, 2 ) ) } );


// different curve
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.lag2( 0.1, 2 ) ) } );


// different curve...
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.lag3( 0.1, 2 ) ) } );


// variable curve
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.varlag( 0.1, -4, 4 ) ) } ); // [VarLag] ugen

// limit the slope
Ndef( \testSynth, { arg freq=440, amp=0, t_trig=1, dur=1; SinOsc.ar( freq, 0, amp.slew( 0.1, 0.2 ) ) } ); // [Slew] ugen

// Also: [Ramp]


// useful conversions for frequencies:

// midicps
// cpsmidi


Ndef( \testSynth ).clear;
k.reset;
