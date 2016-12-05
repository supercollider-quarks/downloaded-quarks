m.gens[\buzz].isNil.if{
	"init %".format(\buzz).postln;
};
m.gens[\buzz] = Ndef(\buzz);


(
Ndef(\buzz).ar(2);

Ndef(\buzz).addSpec(\freq, [200, 800, \exp]);
Ndef(\buzz).addSpec(\mof, [50, 220, \exp]);
Ndef(\buzz).addSpec(\movement, [50, 220, \exp]);
Ndef(\buzz).addSpec(\chopHarm, [1, 1000, \exp]);
Ndef(\buzz).addSpec(\chopFreq, [0.5, 20, \exp]);
Ndef(\buzz).addSpec(\amp, [0, 1]);

// on/off mechanism for m.selectGenerator
Ndef(\buzz).addSpec(\on, [0, 1, \lin, 1]);

Ndef(\buzz, {
	var snd, chopper;

	snd = LFPar.ar(\freq.kr(3100)+[0, 0.7]) *
	LFPar.ar(LFTri.ar(\mof.kr(120) + [0, 0.7]).range(0, \movement.kr(100)));


	chopper = Blip.ar(\chopFreq.kr(20) + [0, 1/pi], \chopHarm.kr(1));

	(chopper * snd).tanh * \amp.kr(1) * \on.kr(0, 0.2);


}).play;

);


////////////// MAPPING ///////////////////////
Ndef(\buzz).addHalo(\offFunc, {
	Ndef(\buzz).set(\on, 0);
	// Ndef(\buzz).stop;
});

Ndef(\buzz).addHalo(\onFunc, {
	Ndef(\buzz).set(\on, 1);
	// Ndef(\buzz).play;
});


/////// additional mapping (to your controllers) ///////////


////// assign functions to keyplayer

// Ndef(\buzz).addHalo(\kpAction_a, {
// 	"%: a pressed.".format(\buzz).inform;
// });

/////////// ALWAYS RETURN Ndef //////////////

Ndef(\buzz); // return value


//////////////// TESTS /////////////

/*

m.ctls.pp.capNormVals
m.ctls.pp.imuDict
m.ctls.pp.encoderDict


*/