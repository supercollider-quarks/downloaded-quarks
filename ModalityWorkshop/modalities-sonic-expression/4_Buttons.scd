/// Buttons

// Buttons can be used in various ways...

// most buttons have a default state (off), and a state when they are pressed (on).

k = MKtl( 'lpd80' );
k.gui;

s.boot;

k.reset;

MIDIIn.noteOnZeroAsNoteOff_( false );


// ---------------- on - off --------------

// the simplest version is just on-off

Ndef( \testSynth, { |on=0| SinOsc.ar( 440, 0, on * 0.5 ) } );
Ndef( \testSynth ).play;

k.elementAt( \pad, \on, 1 ).action = { arg el; Ndef( \testSynth ).set( \on, 1 ) };

k.elementAt( \pad, \off, 1 ).action = { arg el;  Ndef( \testSynth ).set( \on, 0 ) };


// --------------- toggle -------------

// next one up is a toggle:
k.reset;

~buttonState = 0;

k.elementAt( \pad, \on, 1 ).action = { arg el; ~buttonState = (~buttonState + 1).mod(2); Ndef( \testSynth ).set( \on, ~buttonState ); };

// clean up:
k.reset;
Ndef( \testSynth ).stop;

// ------------- multistate ----------

// a toggle presents two different states that the button can be in, you can also have more states, advancing to the next state each time you press the button:

Ndef( \testSynth, { |select=0| Select.ar( select, [ SinOsc.ar( 440, 0, 0.1 ), Saw.ar( 440, 0.1 ), Pulse.ar( 440, 0.5, 0.1 ), WhiteNoise.ar( 0.1 ), PinkNoise.ar(0.1) ] ) } );
Ndef( \testSynth ).play;

~buttonMultiState = 0;
k.elementAt( \pad, \on, 1 ).action = { arg el; ~buttonMultiState = (~buttonMultiState + 1).mod(5); Ndef( \testSynth ).set( \select, ~buttonMultiState ); };


// clean up:
k.reset;

Ndef( \testSynth ).stop;


// ----------- tracking multiple clicks ----------

// You can also make actions dependent on multiple clicks on the same button, e.g. different actions for a double-click or triple-click, or use the time between clicks as a control value, or make a difference between a soft click before a hard one, and vice versa (if the button is sensitive to how hard you press it).

// the code below records the time between clicks, and for every click that is within a maximum time of a previous click (0.2), it records the time between the clicks (clickDeltas), the number of clicks, and the velocity of the clicks.

(
~multiclick = IdentityDictionary.new;
~multiclick.put( \clicks, 0 );
~multiclick.put( \clickDeltas, List.new );
~multiclick.put( \clickTimes, IdentityDictionary.new );
~multiclick.put( \clickVals, IdentityDictionary.new );
~multiclick.put( \clickActions, IdentityDictionary.new );
~multiclick.put( \maxClickDelta, 0.4 );
(
~multiclick.put( \timeOut, Task( {
   var clickcount, action;
   ~multiclick[\maxClickDelta].wait; "timeout".postln;
   // value of multiclick
   clickcount = ~multiclick[\clicks];
   // put an action based on the result of the multiclick here:
	// [clickcount, ~multiclick[\clickVals], ~multiclick[ \clickTimes ], ~multiclick[ \clickDeltas ] ].postln;
	action = ~multiclick[ \clickActions ].at( clickcount );
	if ( action.notNil ){ action.value( ~multiclick[\clickVals], ~multiclick[ \clickDeltas ] ); };
   // reset variables:
   ~multiclick.put( \clicks, 0 );
   ~multiclick.put( \clickTimes, IdentityDictionary.new );
   ~multiclick.put( \clickVals, IdentityDictionary.new );
   ~multiclick.put( \clickDeltas, List.new );
} ) );
);
);

(
k.elementAt( \pad, \on, 1 ).action = { arg el;
   var thisTime = Process.elapsedTime;
//   if ( ~multiclick[ \clicks ] > 0 ){ ~multiclick };
   if ( ~multiclick[ \clicks ] > 0 ){
      ~multiclick[ \clickDeltas ].add( thisTime - ~multiclick[ \clickTimes ][ ~multiclick[ \clicks ] ] )
   };
   ~multiclick[ \clicks ] = ~multiclick[\clicks] + 1;
   ~multiclick[ \clickTimes ].put( ~multiclick[\clicks], thisTime );
   ~multiclick[ \clickVals ].put( ~multiclick[\clicks], el.value );
   ~multiclick[\timeOut].stop.reset.play;
   ~multiclick.put( \lastTime, thisTime );
   ~multiclick[\clicks].postln;
};
);


(
Ndef( \testSynth, { |amp=1,on=1|
	var trig = Impulse.kr( \rate.kr(1) );
	var seq = Demand.kr( trig, 0, Dseq( \dseq.kr( [0,0,0] ), inf ) );
	SinOsc.ar( seq.exprange( 100, 1000 ), mul: on * amp * EnvGen.kr( Env.perc, trig, timeScale: \dur.kr(0.5) ) );
} );
);
Ndef( \testSynth ).play;

~multiclick[ \clickActions ].put( 1, { |vals,deltas| [ 1, vals, deltas ].postln; Ndef( \testSynth ).set( \amp, vals.at(1) ); } );
~multiclick[ \clickActions ].put( 2, { |vals,deltas| [ 2, vals, deltas.asArray ].postln; Ndef( \testSynth ).set( \dur, deltas.first * 2, \rate, ( deltas.first * 3 ).reciprocal ) } );
~multiclick[ \clickActions ].put( 3, { |vals,deltas| [ 3, vals.asArray, deltas ].postln; Ndef( \testSynth ).setn( \dseq, vals.asArray ); } );

// ------------------ buttons in relationship to each other --------------

// You could extend the concept above to button presses on different buttons, so creating "key-combos", like in games where you have special actions dependent on using a specific control-sequence.


// -------------- modal control, a button press selects a mode for another control --------

// You can also use buttons to select between different modes of control, or to shift control from one controller to another.