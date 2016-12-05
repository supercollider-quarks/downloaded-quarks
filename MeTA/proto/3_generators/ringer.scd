m.gens[\ringer].isNil.if{
	"init %".format(\ringer).postln;
};
m.gens[\ringer] = Ndef(\ringer);


(
Ndef(\ringer).ar(2);

Ndef(\ringer).addSpec(\freq, [2800, 3200, \exp]);
Ndef(\ringer).addSpec(\mof, [50, 220, \exp]);
Ndef(\ringer).addSpec(\movement, [50, 220, \exp]);
Ndef(\ringer).addSpec(\chopWidth, [0.001, 1, \exp]);
Ndef(\ringer).addSpec(\chopLPF, [100, 1000, \exp]);
Ndef(\ringer).addSpec(\chopFreq, [1, 100, \exp]);
Ndef(\ringer).addSpec(\amp, [0, 1]);

// on/off mechanism for m.selectGenerator
Ndef(\ringer).addSpec(\on, [0, 1, \lin, 1]);

Ndef(\ringer, {
	var snd, chopper;

	snd = LFPar.ar(\freq.kr(3100)+[0, 0.7]) *
	LFPar.ar(LFTri.ar(\mof.kr(120) + [0, 0.7]).range(0, \movement.kr(100)));


	chopper = LPF.ar(
		LFPulse.ar(\chopFreq.kr(20) + [0, 1/pi], width: \chopWidth.kr(0.5)),
		\chopLPF.kr(800)
	);

	(chopper * snd).tanh * \amp.kr(1) * \on.kr(0, 0.2);


}).play;

);


////////////// MAPPING ///////////////////////
Ndef(\ringer).addHalo(\offFunc, {
	Ndef(\ringer).set(\on, 0);
	// Ndef(\ringer).stop;
});

Ndef(\ringer).addHalo(\onFunc, {
	Ndef(\ringer).set(\on, 1);
});


/////// additional mapping (to your controllers) ///////////


////// assign functions to keyplayer

// Ndef(\ringer).addHalo(\kpAction_a, {
// 	"%: a pressed.".format(\ringer).inform;
// });

/////////// ALWAYS RETURN Ndef //////////////

Ndef(\ringer); // return value


//////////////// TESTS /////////////

/*

m.ctls.pp.capNormVals
m.ctls.pp.imuDict
m.ctls.pp.encoderDict


*/