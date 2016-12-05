// this is an alternative version to modal_functionality_2,
// aiming for a higher level implementation style,
// using less code, allowing more flexibility,
// but also requiring more conceptual thinking.

// our simple use case:
// we use two sliders for continuous control of two different synths.
// the upper buttons of the MKtl select which synths the controls go to.
// two lower buttons start and stop the synths


// prepare the sound sources:
s.boot;

(
Ndef( \sound1, {
	var freq = \freq.kr(200) * LFNoise0.kr(ExpRand(3, 8), 0.02, 1);
	var snd = RLPF.ar(Pulse.ar(freq, 0.2), \rfreq.kr(2500), 0.3);
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
	// ranges of the two params of interest
Ndef( \sound1 ).addSpec(\freq, [60, 600, \exp]);
Ndef( \sound1 ).addSpec(\rfreq, [1500, 6000, \exp]);

// which param should slider set?
Ndef( \sound1 ).addHalo(\sl1Param, \freq);
Ndef( \sound1 ).addHalo(\sl2Param, \rfreq);

Ndef( \sound2, {
	var dens = \dens.kr(30);
	var freqs = [ \freq1.kr(400), \freq2.kr(500)];
	var snd = Ringz.ar(Dust2.ar(dens, 0.8), freqs, 0.03).sum;
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
Ndef( \sound2 ).addSpec(\dens, [3, 3000, \exp]);
Ndef( \sound2 ).addSpec(\freq1, [300, 2500, \exp]);
Ndef( \sound2 ).addSpec(\freq2, [1500, 3500, \exp]);

Ndef( \sound2 ).addHalo(\sl1Param, \dens);
Ndef( \sound2 ).addHalo(\sl2Param, \freq1);
);

// the MKtl: an iControls ...
k = MKtl( 'icon', "*icontrols" );
/// or a nanoKontrol 2
k = MKtl( 'nk2', "*kontrol2");
k.gui;

Ndef( \sound1).play;
Ndef( \sound1).gui;  // to see params change

// use a global dict q for intermediate variables and functions
q = q ? ();

 // setting the continuous controls:
(
// first, make afunction that can set a param
// after  looking up which param and which spec:

q.setNdefParam = { |q, ndef, ktlParName, normVal|
	var specName = ndef.getHalo(ktlParName);
	var spec = ndef.getSpec( specName );
	// [ktlParName, specName, normVal].postln;
	ndef.set( specName, spec.map( normVal ) );
};

/*   // test that the function works:
q.setNdefParam(Ndef( \sound1 ), \sl2Param, 0.5);
*/
)

(
// now make functions for sl 0
q.sl0 = k.elAt( \sl, 0 );
q.sl0.action = MFunc.new;

// slider posts value
q.sl0.action.add( \trace, { |el| "sl_1 ".post; el.value.postln; });
// the set action for param 1
q.sl0.action.add( \sound1, { |mktel|
	q.setNdefParam(Ndef( \sound1 ), \sl1Param, mktel.value);
});
q.sl0.action.add( \sound2, { |mktel|
	q.setNdefParam(Ndef( \sound2 ), \sl1Param, mktel.value);
});
);

(
// same for sl1, with alternative syntax:
q.sl1 = k.elAt( \sl, 1 );

q.sl1.action = MFunc([
	// avoid copy pasting trace texts which may be confusing
	// by using postf and getting element name from element
	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\sound1, { |mktel|
		q.setNdefParam(Ndef( \sound1 ), \sl2Param, mktel.value);
 	},
	\sound2, { |mktel|
		q.setNdefParam(Ndef( \sound2 ), \sl2Param, mktel.value);
}]);
);

Ndef( \sound1).stop;
Ndef( \sound2).play;
Ndef( \sound2).gui;  // to see params change


// starting and stopping the ndefs, by toggling -
// make a toggle function for Ndefs first :

q.togglePlay = { |q, ndef|
	var wasOn = ndef.monitor.isPlaying;
	if (wasOn){ ndef.stop; } { ndef.play };
	// return resulting playState in case you need it
	wasOn.not
};

// test
q.togglePlay (Ndef(\sound1));

// now, which buttons to use ?
// on a nanoKontrol 2, the two track buttons "<"  ">" seem good:
k.postElements

// they are called \tleft and tright ...
(
q.playBt0 = k.elAt( \tr, \tleft );
q.playBt0.action = MFunc([
 	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\togglePlay, { |mktel|
			// toggle on pressdown
 		if ( mktel.value == 1 ){
			q.togglePlay (Ndef(\sound1));
 		};
	}
]);

q.playBt1 = k.elAt( \tr, \tright );
q.playBt1.action = MFunc([
 	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\togglePlay, { |mktel|
			// toggle on pressdown
		if ( mktel.value == 1 ){
			q.togglePlay (Ndef(\sound2));
 		};
	}
])
);


// now use a button for each sound to enable the two sliders
// for that sound only while that button is pressed:
// use buttons << and >>, called rew and fwd.

(
q.enableBut0 = k.elAt( \tr, \rew );
q.enableBut0.action = MFunc([
	// avoid copy pasting trace texts which may be confusing
	// by using postf and getting element name from element
	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\togKtl, { |mktel|
		if ( mktel.value == 1 ){
			q.sl0.action.enable( \sound1 );
			q.sl1.action.enable( \sound1 );
		}{
			// disable controls
			q.sl0.action.disable( \sound1 );
			q.sl1.action.disable( \sound1 );
		};
	}
]);
);

// same for sound2 and right >> button
(
q.enableBut1 = k.elAt( \tr, \fwd );
q.enableBut1.action = MFunc([
	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\togKtl, { |mktel|
		if ( mktel.value == 1 ){
			q.sl0.action.enable( \sound2 );
			q.sl1.action.enable( \sound2 );
		}{
			// disable controls
			q.sl0.action.disable( \sound2 );
			q.sl1.action.disable( \sound2 );
		};
	}
]);
);


(
// when done testing, disable all the trace actions:
q.sl0.action.disable( \trace );
q.sl1.action.disable( \trace );
q.playBt0.action.disable( \trace );
q.playBt1.action.disable( \trace );
q.enableBut0.action.disable( \trace );
q.enableBut1.action.disable( \trace );
);


//////////////// variants : ////////////

 // - use buttons like piano keys,
// only play while track buttons are held down
(
q.playBt0.action.add( \togglePlay, { |mktel|
	var ndef = Ndef( \sound1 );
	if ( mktel.value == 1 ){ ndef.play; }{ ndef.stop; }
} );

q.playBt1.action.add( \togglePlay, { |mktel|
	var ndef = Ndef( \sound2 );
	if ( mktel.value == 1 ){ ndef.play; }{ ndef.stop; }
} );
)

// - use marker-set button to toggle whether sliders go to sound1 or sound2

(
q.togSlBut = k.elAt( \tr, \mset);

 // when up, sound1, when down, sound2
q.togSlBut.action = MFunc([
	\trace, { |mktel| "% %\n".postf(mktel.name, mktel.value); },
	\togKtl, { |mktel|
		if ( mktel.value == 0 ){
			q.sl0.action.enable( \sound1.postcs ).disable( \sound2 );
			q.sl1.action.enable( \sound1 ).disable( \sound2 );
		}{
			q.sl0.action.disable( \sound1 ).enable( \sound2.postcs );
			q.sl1.action.disable( \sound1 ).enable( \sound2 );
		};
	}
]);
// simplest init by doing the action once !
 q.togSlBut.doAction;
)




/* actually switching between sound1 or sound2 is an example of a
   mode of exclusive alternatives, and MFunc provides a simple
   and elegant way to do this:
*/

// declare a named  list of mutually exclusive modes
q.sl0.action.makeExclusiveModes(\sounds_excl, [\sound1, \sound2]);

// and switch between them by name
// - note that this leaves e.g. \trace untouched.
q.sl0.action.mode_(\sound1);
q.sl0.action.mode_(\sound2);


q.sl1.action.makeExclusiveModes(\sounds_excl, [\sound1, \sound2]);


// step by step, this does:
q.togSlBut.action.modeLists.put (\sounds_excl, [\sound1, \sound2]);
q.togSlBut.action.modes.put (\sound1, (on: \sound1, off: \sounds_excl));
q.togSlBut.action.modes.put (\sound2, (on: \sound2, off: \sounds_excl));

// this makes the action of q.togSlBut really simple :


q.togSlBut.doAction;

q.togSlBut.action.add(\togKtl, { |mktel|
	var which =  mktel.value.asInteger;
	var mode = [\sound1, \sound2][which].postln;
	[q.sl0, q.sl1].do { |sl| sl.action.mode_(mode) };
});

// and now feel free to extend this with more controls!
// also, check out the MPD18 use case in ModalityStuff
// for an example with more complex modes.
