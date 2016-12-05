// to create a modal control, we will use MFunc from the adclib.

// our simple use case:
// we have two sliders that we use for continuous control of different synths.
// the upper buttons of the icontrols select which synths the controls go to.
// two lower buttons start and stop the synths

// this version creates all functions very explicitly,
// see version 2b for higher level implementation style,
// using less code, requiring more conceptual thinking,

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

Ndef( \sound1).play(0.25);
Ndef( \sound1).gui;  // to see params change

// setting the continuous controls:
(
// sl 0 can set one param each of sound1 and sound2
k.elAt( \sl, 0 ).action = MFunc.new;
k.elAt( \sl, 0 ).action.add( \trace, { |mktel|
	"sl0 ".post; mktel.value.postln;
} );
k.elAt( \sl, 0 ).action.add( \sl1Sound1, { |mktel|
	var ctlName = Ndef( \sound1 ).getHalo(\sl1Param);
	Ndef( \sound1 ).setUni( ctlName, mktel.value )
});
k.elAt( \sl, 0 ).action.add( \sl1Sound2, { |mktel|
	var ctlName = Ndef( \sound2 ).getHalo(\sl1Param);
	Ndef( \sound2 ).setUni( ctlName, mktel.value )
});
);

(
// sl 1 can also set one param each of sound1 and sound2

k.elAt( \sl, 1 ).action = MFunc.new;
k.elAt( \sl, 1 ).action.add( \trace, { |mktel|
	"sl1 ".post; mktel.value.postln;
} );
k.elAt( \sl, 1 ).action.add( \sl2Sound1, { |mktel|
	var ctlName = Ndef( \sound1 ).getHalo(\sl2Param);
	Ndef( \sound1 ).setUni( ctlName, mktel.value );
});
k.elAt( \sl, 1 ).action.add( \sl2Sound2, { |mktel|
	var ctlName = Ndef( \sound2 ).getHalo(\sl2Param);
	Ndef( \sound2 ).setUni( ctlName, mktel.value );
});
);

Ndef( \sound1).stop;
Ndef( \sound2).play;
Ndef( \sound2).gui;  // to see params change


// starting and stopping the ndefs, by toggling,
// use leftmost 2 buttons, middle row:
(
k.elAt( \bt, 1, 0 ).action = MFunc.new;
k.elAt( \bt, 1, 0 ).action.add( \trace, { |mktel| "b1,0 ".post; mktel.value.postln; } );
k.elAt( \bt, 1, 0 ).action.add( \togglePlay, { |mktel|
	if ( mktel.value == 1 ){ // only when pressed
		if ( Ndef( \sound1 ).monitor.isPlaying ){
			Ndef( \sound1 ).stop;
			// disable controls
			k.elAt( \sl, 0 ).action.disable( \sl1Sound1 );
			k.elAt( \sl, 1 ).action.disable( \sl2Sound1 );
		}{ Ndef( \sound1 ).play }
	};
} );

k.elAt( \bt, 1, 1 ).action = MFunc.new;
k.elAt( \bt, 1, 1 ).action.add( \trace, { |mktel| "b1,1 ".post; mktel.value.postln; } );
k.elAt( \bt, 1, 1 ).action.add( \togglePlay, { |mktel|
	if ( mktel.value == 1 ){ // only when pressed
		if ( Ndef( \sound2 ).monitor.isPlaying ){
			Ndef( \sound2 ).stop;
			// disable controls
			k.elAt( \sl, 0 ).action.disable( \sl1Sound2 );
			k.elAt( \sl, 1 ).action.disable( \sl2Sound2 );
		}{ Ndef( \sound2 ).play }
	};
} );
);

// just enable controls for sound1 when top button of column 0 is pressed:
(
k.elAt( \bt, 0, 0 ).action = MFunc.new;
k.elAt( \bt, 0, 0 ).action.add( \trace, { |mktel| "b0,0 ".post; mktel.value.postln; } );
k.elAt( \bt, 0, 0 ).action.add( \togglePlay, { |mktel|
	if ( Ndef( \sound1 ).monitor.isPlaying ){
		if ( mktel.value == 1 ){ // only when pressed
			k.elAt( \sl, 0 ).action.enable( \sl1Sound1 );
			k.elAt( \sl, 1 ).action.enable( \sl2Sound1 );
		}{
			// disable controls
			k.elAt( \sl, 0 ).action.disable( \sl1Sound1 );
			k.elAt( \sl, 1 ).action.disable( \sl2Sound1 );
		};
	};
} );
);

// same for sound2 and column 1
(
k.elAt( \bt, 0, 1 ).action = MFunc.new;
k.elAt( \bt, 0, 1 ).action.add( \trace, { |mktel| "b0, 1 ".post; mktel.value.postln; } );
k.elAt( \bt, 0, 1 ).action.add( \togglePlay, { |mktel|
	if ( Ndef( \sound2 ).monitor.isPlaying ){
		if ( mktel.value == 1 ){ // only when pressed
			k.elAt( \sl, 0 ).action.enable( \sl1Sound2 );
			k.elAt( \sl, 1 ).action.enable( \sl2Sound2 );
		}{
			// disable controls
			k.elAt( \sl, 0 ).action.disable( \sl1Sound2 );
			k.elAt( \sl, 1 ).action.disable( \sl2Sound2 );
		};
	};
} );
);

(
// after testing, disable the trace actions:
k.elAt( \bt, 0, 0 ).action.disable( \trace );
k.elAt( \bt, 1, 0 ).action.disable( \trace );
k.elAt( \bt, 0, 1 ).action.disable( \trace );
k.elAt( \bt, 1, 1 ).action.disable( \trace );
k.elAt( \sl, 0 ).action.disable( \trace );
k.elAt( \sl, 1 ).action.disable( \trace );
);


//////////////// variants : ////////////
// - only play when play/stop button is pressed
(
k.elAt( \bt, 1, 0 ).action = MFunc.new;
k.elAt( \bt, 1, 0 ).action.add( \trace, { |mktel| "b1,0 ".post;
mktel.value.postln; } );
k.elAt( \bt, 1, 0 ).action.add( \togglePlay, { |mktel|
	if ( mktel.value == 1 ){
		Ndef( \sound1 ).play
	}{
		Ndef( \sound1 ).stop;
	}
} );

k.elAt( \bt, 1, 1 ).action = MFunc.new;
k.elAt( \bt, 1, 1 ).action.add( \trace, { |mktel| "b1,1 ".post;
mktel.value.postln; } );
k.elAt( \bt, 1, 1 ).action.add( \togglePlay, { |mktel|
	if ( mktel.value == 1 ){
		Ndef( \sound2 ).play
	}{
		Ndef( \sound2 ).stop;
	}
} );
)

// - toggle slider functionality:
// third from left button in middle row toggles which sound is set:
// when pressed, sound1, when up, sound2

(
k.elAt( \bt, 1, 2 ).action = MFunc.new;
k.elAt( \bt, 1, 2 ).action.add( \trace, { |mktel| "b1,2 ".post;
mktel.value.postln; } );
k.elAt( \bt, 1, 2 ).action.add( \toggleSliders, { |mktel|
	if ( mktel.value == 1 ){
		k.elAt( \sl, 0 ).action.enable( \sl1Sound1 );
		k.elAt( \sl, 1 ).action.enable( \sl2Sound1 );
		k.elAt( \sl, 0 ).action.disable( \sl1Sound2 );
		k.elAt( \sl, 1 ).action.disable( \sl2Sound2 );
	}{
		k.elAt( \sl, 0 ).action.disable( \sl1Sound1 );
		k.elAt( \sl, 1 ).action.disable( \sl2Sound1 );
		k.elAt( \sl, 0 ).action.enable( \sl1Sound2 );
		k.elAt( \sl, 1 ).action.enable( \sl2Sound2 );
	};
} );

// starting condition - as if pressed
k.elAt( \sl, 0 ).action.enable( \sl1Sound1 );
k.elAt( \sl, 1 ).action.enable( \sl2Sound1 );
k.elAt( \sl, 0 ).action.disable( \sl1Sound2 );
k.elAt( \sl, 1 ).action.disable( \sl2Sound2 );

/* ////  or initialize this by using the action:
  k.elAt( \sl, 0 ).doAction;
*/

);




// feel free to extend with more controls!