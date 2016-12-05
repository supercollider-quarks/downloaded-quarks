/********
Server.killAll;
*********/

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
(
///////////    UTILS	  ///////////////////////
m = MeTA(path: thisProcess.nowExecutingPath.dirname, server: s);

/*
m.openFiles("configs",   ["3_userConfig"], warn: true);
*/

///////////    CONFIG	  ///////////////////////
// configuration of your performance. give general information such as number of channels, server options etc.

m.config.numChans = 2;

m.loadFiles("configs", [
	"1_serverConfig",
	"2_networkConfig",
	"3_userConfig"
]);


// boot server and load generators

m.server.waitForBoot({
/*
m.server.boot;
*/
	//	m.server.scope(q.numChans).window.bounds_(Rect(700, 5, 261, 250));


///////////   HELPER NDEFS  ///////////////////////
	"loading HELPERS...".inform;
	m.loadFiles("helperNdefs", ["amplitudeTracker"]);


	///////////    GENERATORS	  ///////////////////////
	"loading GENS...".inform;
	m.loadGen("ringer" , 1);
	m.loadGen("buzz"   , 2);
	// m.loadGen("yourGen", 3);



	// /////  play all Ndefs
	// 0.5.wait;
	// "play GENS...".inform;
	// q.utils.getNdefs.do {|ndef|
	// 	ndef.play;
	// };
	// 0.5.wait;
	/*
	q.utils.openFiles("gen", ["*"]); // */



	///////////   AUX-EFFECTS   ///////////////////////
	"loading AUX EFX...".inform;
	m.loadAux(
		name:          "reverbAux",
		numChans:      m.config.numChans,
		defaultInGain: 0.3
	);

	///////////   MASTER-EFFECTS   ///////////////////////
	// q.utils.loadFiles("efx", ["*fx*", "*master*"]);

});
);

(
///////////    CONTROLLERS	  ///////////////////////
"loading CONTROLLERS...".inform;

m.loadFiles(\control, [
	"NdefMixer",
	"pushPullOSC",
	"soundSelector-KeyPlayer"
]);

/*
m.openFiles(\control, [
"NdefMixer",
"pushPullOSC",
"soundSelector-KeyPlayer"
]);
*/


///////////    MAPPING   	  ///////////////////////
"loading MAPPING...".inform;
(
	m.loadFiles("map", [
		"keyPlayerMapping"
	]);
);

{m.utils.generatorSelectAction(m.getNdefGens.first.key);}.defer;


);


(

// Ndef('reverb').set('t60', 3.5432325262237, 'damp', 0.6, 'size', 0.72222222222222, 'earlydiff', 0.59444444444444, 'in', ProxySubmix('reverbAux'), 'mdepth', 44.166666666667, 'mfreq', 0.055555555555556);

Ndef('reverb').set('t60', 1.9, 'damp', 0.2, 'size', 0.72222222222222, 'earlydiff', 0.59444444444444, 'in', ProxySubmix('reverbAux'), 'mdepth', 44.166666666667, 'mfreq', 0.055555555555556);


)

