/// Mapping data

/// What is mapping?

/// the relationship between incoming data and resulting sound of the parameters

/// most simple mapping - button on/off

k = MKtl( 'rnnd0' );
k.gui;

s.boot;

SynthDef( \testSynth, { |out=0,on=0| Out.ar( out, SinOsc.ar( 440, 0, on * 0.1 ) ) } ).add;
x = Synth.new( \testSynth );

k.elementAt( \bt, 1 ).action = { |but| x.set( \on, but.value ) };

// button press turns the sound on
// button release turns sound off

// reset all
k.reset; x.free;

// slightly more complex - continuous control
SynthDef( \testSynth2, { |out=0,on=0,amp=0| Out.ar( out, SinOsc.ar( 440, 0, on * amp ) ) } ).add;
x = Synth.new( \testSynth2 );

k.elementAt( \bt, 1 ).action = { |but| x.set( \on, but.value ) };
k.elementAt( \throt, \r ).action = { |el| x.set( \amp, el.value ); };

// this is slightly bumpy, so we add a lag on the amp control:

x.free;

SynthDef( \testSynth2, { |out=0,on=0,amp=0| Out.ar( out, SinOsc.ar( 440, 0, on * amp.lag( 0.1 ) ) ) } ).add;
x = Synth.new( \testSynth2 );

// we can also add a lag on the on/off control to get an envelope:

x.free;

SynthDef( \testSynth2, { |out=0,on=0,amp=0| Out.ar( out, SinOsc.ar( 440, 0, on.lag(0.01,0.5) * amp.lag( 0.1 ) ) ) } ).add;
x = Synth.new( \testSynth2 );

// mapping the range in a different way:

~ampSpec = [0.01, 0.5, \exponential ].asSpec;

k.elementAt( \throt, \r ).action = { |el| x.set( \amp, ~ampSpec.map( el.value ).postln; ); };

k.elementAt( \throt, \r ).action = { |el| x.set( \amp, el.value.postln; ); };

// reset and free all
k.reset; x.free;