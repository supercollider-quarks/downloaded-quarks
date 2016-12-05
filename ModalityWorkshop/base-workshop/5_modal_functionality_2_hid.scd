// to create a modal control, we will use MFunc from the adclib.

// our simple use case:
// we have two gamepad joystick axes that we use for continuous control of different synths.
// the buttons on the shoulders of the gamepad select which synths the controls go to.
// two other buttons start and stop the synths


// prepare the sound sources:
s.boot;

(
Ndef( \sound1, { var freq = \freq.kr(200) * LFNoise0.kr(ExpRand(3, 8), 0.02, 1);
	var snd = RLPF.ar(Pulse.ar(freq, 0.2), \rfreq.kr(2500), 0.3);
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
	// which param should slider set?
Ndef( \sound1 ).addHalo(\xParam, \freq);
Ndef( \sound1 ).addHalo(\yParam, \rfreq);
	// range of that param
Ndef( \sound1 ).addSpec(\freq, [60, 600, \exp]);
Ndef( \sound1 ).addSpec(\rfreq, [1500, 6000, \exp]);

Ndef( \sound2, {
	var dens = \dens.kr(30);
	var snd = Ringz.ar(Dust2.ar(dens, 0.8), [ \freq1.kr(400), \freq2.kr(500)], 0.03).sum;
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
Ndef( \sound2 ).addHalo(\xParam, \dens);
Ndef( \sound2 ).addHalo(\yParam, \freq1);
Ndef( \sound2 ).addHalo(\xParam1, \freq2);
Ndef( \sound2 ).addSpec(\dens, [3, 3000, \exp]);
Ndef( \sound2 ).addSpec(\freq1, [300, 2500, \exp]);
Ndef( \sound2 ).addSpec(\freq2, [1500, 3500, \exp]);

Ndef( \sound1 ).gui;
Ndef( \sound2 ).gui;

);

// the MKtl:
k = MKtl( 'myGP', "*drive");

// setting the continuous controls:
(
k.elAt( \joy, \l, \x ).action = MFunc.new;
k.elAt( \joy, \l, \x ).action.add( \trace, { |elem| "joy,x,l, ".post; elem.value.postln; } );
k.elAt( \joy, \l, \x ).action.add( \xSound1, { |elem|
	var ctlName = Ndef( \sound1 ).getHalo(\xParam);
	Ndef( \sound1 ).setUni( ctlName, elem.value );
});
k.elAt( \joy, \l, \x ).action.add( \xSound2, { |elem|
	var ctlName = Ndef( \sound2 ).getHalo(\xParam);
	Ndef( \sound2 ).setUni( ctlName, elem.value );
});
);

(
k.elAt( \joy, \l, \y ).action = MFunc.new;
k.elAt( \joy, \l, \y ).action.add( \trace, { |elem| "joy,y,l, ".post; elem.value.postln; } );
k.elAt( \joy, \l, \y ).action.add( \ySound1, { |elem|
	var ctlName = Ndef( \sound1 ).getHalo(\yParam);
	Ndef( \sound1 ).setUni( ctlName, elem.value );
});
k.elAt( \joy, \l, \y ).action.add( \ySound2, { |elem|
	var ctlName = Ndef( \sound2 ).getHalo(\yParam);
	Ndef( \sound2 ).setUni( ctlName, elem.value );
});

);

// starting and stopping the ndefs:
(
k.elAt( \bt, \7 ).action = MFunc.new;
k.elAt( \bt, \7 ).action.add( \trace, { |elem| "b7 ".post; elem.value.postln; } );
k.elAt( \bt, \7 ).action.add( \togglePlay, { |elem|
	if ( elem.value > 0 ){ // only when pressed
		if ( Ndef( \sound1 ).monitor.isPlaying ){
			Ndef( \sound1 ).stop;
			// disable controls
			k.elAt( \joy, \l, \x ).action.disable( \xSound1 );
			k.elAt( \joy, \l, \y ).action.disable( \ySound1 );
		}{ Ndef( \sound1 ).play }
	};
} );

k.elAt( \bt, \8 ).action = MFunc.new;
k.elAt( \bt, \8 ).action.add( \trace, { |elem| "b8 ".post; elem.value.postln; } );
k.elAt( \bt, \8 ).action.add( \togglePlay, { |elem|
	if ( elem.value == 1 ){ // only when pressed
		if ( Ndef( \sound2 ).monitor.isPlaying ){
			Ndef( \sound2 ).stop;
			// disable controls
			k.elAt( \joy, \l, \x ).action.disable( \xSound2 );
			k.elAt( \joy, \l, \y ).action.disable( \ySound2 );
		}{ Ndef( \sound2 ).play }
	};
} );
);

// just enable controls when button pressed:
(
k.elAt( \bt, \5 ).action = MFunc.new;
k.elAt( \bt, \5 ).action.add( \trace, { |elem| "b5 ".post; elem.value.postln; } );
k.elAt( \bt, \5 ).action.add( \togglePlay, { |elem|
	if ( Ndef( \sound1 ).monitor.isPlaying ){
		if ( elem.value == 1 ){ // only when pressed
			k.elAt( \joy, \l, \x ).action.enable( \xSound1 );
			k.elAt( \joy, \l, \y ).action.enable( \ySound1 );
		}{
			// disable controls
			k.elAt( \joy, \l, \x ).action.disable( \xSound1 );
			k.elAt( \joy, \l, \y ).action.disable( \ySound1 );
		};
	};
} );
);

(
k.elAt( \bt, \6 ).action = MFunc.new;
k.elAt( \bt, \6 ).action.add( \trace, { |elem| "b6 ".post; elem.value.postln; } );
k.elAt( \bt, \6 ).action.add( \togglePlay, { |elem|
	if ( Ndef( \sound2 ).monitor.isPlaying ){
		if ( elem.value == 1 ){ // only when pressed
			k.elAt( \joy, \l, \x ).action.enable( \xSound2 );
			k.elAt( \joy, \l, \y ).action.enable( \ySound2 );
		}{
			// disable controls
			k.elAt( \joy, \l, \x ).action.disable( \xSound2 );
			k.elAt( \joy, \l, \y ).action.disable( \ySound2 );
		};
	};
} );
);

(
// after testing, disable the trace action:
k.elAt( \bt, \5 ).action.disable( \trace );
k.elAt( \bt, \6 ).action.disable( \trace );
k.elAt( \bt, \7 ).action.disable( \trace );
k.elAt( \bt, \8 ).action.disable( \trace );
k.elAt( \joy, \l, \x ).action.disable( \trace );
k.elAt( \joy, \l, \y ).action.disable( \trace );
);

// only play sound when buttons 7 or 8 are pressed
(
k.elAt( \bt, \7 ).action = MFunc.new;
k.elAt( \bt, \7 ).action.add( \trace, { |elem| "b1, 0 ".post; elem.value.postln; } );
k.elAt( \bt, \7 ).action.add( \togglePlay, { |elem|
	if ( elem.value > 0 ){
		Ndef( \sound1 ).play
	}{
		Ndef( \sound1 ).stop;
	}
} );

k.elAt( \bt, \8 ).action = MFunc.new;
k.elAt( \bt, \8 ).action.add( \trace, { |elem| "b1,1 ".post;
elem.value.postln; } );
k.elAt( \bt, \8 ).action.add( \togglePlay, { |elem|
	if ( elem.value > 0 ){
		Ndef( \sound2 ).play
	}{
		Ndef( \sound2 ).stop;
	}
} );

k.elAt( \bt, \9 ).action = MFunc.new;
k.elAt( \bt, \9 ).action.add( \trace, { |elem| "b1,2 ".post;
elem.value.postln; } );
k.elAt( \bt, \9 ).action.add( \toggleSliders, { |elem|
	if ( elem.value > 0 ){
		k.elAt( \joy, \l, \x ).action.enable( \xSound1 );
		k.elAt( \joy, \l, \y ).action.enable( \ySound1 );
		k.elAt( \joy, \l, \x ).action.disable( \xSound2 );
		k.elAt( \joy, \l, \y ).action.disable( \ySound2 );
	}{
		k.elAt( \joy, \l, \x ).action.disable( \xSound1 );
		k.elAt( \joy, \l, \y ).action.disable( \ySound1 );
		k.elAt( \joy, \l, \x ).action.enable( \xSound2 );
		k.elAt( \joy, \l, \y ).action.enable( \ySound2 );
	};
} );

k.elAt( \joy, \l, \x ).action.enable( \xSound1 );
k.elAt( \joy, \l, \y ).action.enable( \ySound1 );
k.elAt( \joy, \l, \x ).action.disable( \xSound2 );
k.elAt( \joy, \l, \y ).action.disable( \ySound2 );

);



// feel free to extend with more controls!