// complex-copy-paste in jit-MFunc style

// Jeff Carey's MPD18 use case,
// done with JITLib elements (NodeProxy)
// JITLib extensions (Halo, getSpec)
// and modal use of functions (MFunc, ATM in adclib quark)



/*

( You can use this example in two modes :-)

*** IMPATIENT MODE
- if needed, install Quarks, recompile, boot,
- select all and evaluate
- play
- maybe read later

*** DETAIL MODE
- if needed, install Quarks, recompile, boot,

Read sections, run code segments,
do all the tests one at a time,
in order to understand options offered by the classes used,
implementation details, various design decisions, etc.
This should also give you an idea of the writing process
of an example of this complexity.


// first install quarks needed:

Quarks.checkoutAll;
// ... wait ... wait ... some more ... then:
Quarks.install("JITLibExtensions");
Quarks.install("adclib");

// then recompile:
thisProcess.recompile;

// then boot server
s.boot;



*/


// make 16 sound locations with proxies

(
// 3 source locations, 12 destinations, .
// each can have its own sound, keeps its settings,
// and knows which param will be set from the MPD18 slider
// each can be started, held, released from a button.

q = q ? ();

	// make 16 sound locations, for 16 pads
q.allSnds = 16.collect { NodeProxy.audio(s, 2); };

	// add a flag keep track whether sound is on or not
	// (could also use proxy.monitor.isPlaying,
	// but that would leave sound processes running on silently)
q.allSnds.do { |px| px.addHalo(\isOn, false); };
// mark them as off when hitting cmd-.
CmdPeriod.add({ q.allSnds.do { |px| px.addHalo(\isOn, false); }; });

// take last 12 proxies as destinations to copy to
q.dests = q.allSnds.drop(4);


// take first 3 as sources which cant be overwritten
q.sources = q.allSnds.keep(3);

// prepare the sources silently, with three different sounds
// later sounds could have tweak inputs for adsr, amp, etc

q.sources[0].prime({
	var freq = \freq.kr(200) * LFNoise0.kr(ExpRand(3, 8), 0.02, 1);
	var snd = RLPF.ar(Pulse.ar(freq, 0.2), 2500, 0.3);
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
	// which param should slider set?
q.sources[0].addHalo(\sliderParam, \freq);
	// range of that param
q.sources[0].addSpec(\freq, [60, 600, \exp]);

q.sources[1].prime({
	var dens = \dens.kr(30);
	var snd = Ringz.ar(Dust2.ar(dens, 0.8), [[300, 890], [567, 2500]], 0.03).sum;
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
q.sources[1].addHalo(\sliderParam, \dens);
q.sources[1].addSpec(\dens, [3, 3000, \exp]);

q.sources[2].prime({
	var noodle = LFNoise0.kr(\noodle.kr(30) * [1, 1.618], 0.5, 1);
	var snd = SinOsc.ar(1000 * noodle);
	var env = EnvGen.kr(Env.adsr, \gate.kr(1), doneAction: 2);
	snd * env;
});
q.sources[2].addHalo(\sliderParam, \noodle);
q.sources[2].addSpec(\noodle, [1, 100, \exp]);
);


/* // halo has spec, isOn, and sliderParam
q.sources[0].getHalo;
	// test toggling on/off - direct play/release
q.sources[0].send.play;
q.sources[0].release;

*/
// first function - toggle a sound on/off
// return whether is was turned on or off
(
q.srcTogPlay = { |q, index, value|
	var proxy = q.allSnds[index];
	var willBeOn = if (value.notNil) {
		value > 0
	} {
		proxy.getHalo(\isOn).not;
	};
	[\srcTogPlay, index, value].postln;
	if (willBeOn) { proxy.send.play; } { proxy.release; };
	proxy.addHalo(\isOn, willBeOn);
	willBeOn
};
// panic button - turn them all off
q.quiet = { q.allSnds.do { |px, i| q.srcTogPlay(i, 0); } };

);
/*
	// // test - srcTogPlay
q.srcTogPlay(0, 1);  // force on
q.srcTogPlay(0, 0);  // force off
q.srcTogPlay(0);	// toggle current state
q.srcTogPlay(0);

q.srcTogPlay(1, 1);
q.srcTogPlay(1, 0);
q.srcTogPlay(2, 1);
q.srcTogPlay(2, 0);
q.allSnds[0].play;

*/

(
// second function - slider should set one param of all active sounds
// set all currently active sounds by their own specs
q.sliderSet = { |q, normValue|
	var actives = q.getActives;
	actives.do { |src, i|
		var specName = src.getHalo(\sliderParam);
		// [specName, src.getUni(specName)].postln;
		if (specName.notNil) {
			SoftSet.uni(src, specName, normValue, 0.04, q.lastSlVal);
		};
	};
	q.lastSlVal = normValue;
};

// needs helper functions:
// get currently active proxies ...
q.getActives = { q.allSnds.select { |x| x.getHalo(\isOn) == true } };
//  or their indices
q.getActiveIndices = { var res = [];
	q.allSnds.do { |x, i| if (x.getHalo(\isOn)) { res = res.add(i) } };
	res
};

// and a little window to test that slider control works as intended
// use 3 NdefGui to see changes on the sources
q.pxwin = Window("sources", Rect(10,100, 400, 250)).front;
q.pxwin.addFlowLayout;
EZSlider(q.pxwin, 380@30, \sliderSet, [0, 1], { |sl| q.sliderSet(sl.value) }, );
q.pxwin.view.decorator.nextLine.shift(0, 10);
q.eds = q.sources.collect { |src, i| NdefGui(src, 1, q.pxwin).name_("src" + i) };
);

/*
// test helper functions:
q.getActives;
q.getActiveIndices;

// when slider is moved:
// it "catches" an active ndef's param when the value gets close enough;
// after that it always sets that control input.

q.srcTogPlay(0, 1); // move till it catches, then it sets src 0 / freq
q.srcTogPlay(1, 1); // same for src 1, controls both now
q.getActiveIndices; // test helper function
q.getActives; 		// test helper function

q.srcTogPlay(0, 0); // stop src 0 -> slider just sets src 1
q.srcTogPlay(2, 1); // add src 2, so src 1 and 2 are controlled
q.srcTogPlay(1, 0); // stop 1, only src2 remains controlled
q.srcTogPlay(2, 0); // stop 2, slider moves nothing
q.getActiveIndices;

q.quiet;
*/


// third function - copying a source to a destination,
// with params and play state
(
// copy snd from one loc to another
q.copySnd = { |q, srcIndex, destIndex, isOn|
	var src = q.allSnds[srcIndex];
	var dest = q.allSnds[destIndex];
	var srcFunc = src.source;
	var srcSettings = src.getKeysValues.flatten;
	isOn = isOn ?? { src.getHalo(\isOn).postln };

	if (destIndex < 4) {
		"can't copy to first four sounds!".postln;
	} {
		"copying src % to dest %\n".postf(srcIndex, destIndex);
		// source might be nil - catch that if so?
		"src.source: %\n".postf(srcFunc);
		dest.prime(srcFunc);
		"copy source settings: %\n".postf(srcSettings);
		dest.set(*srcSettings);
		 // debug
		if (isOn) {
			"copy and play!".postln;
			q.srcTogPlay(destIndex, 1);
		} {
			"silent copy.".postln;
		};
		dest.addHalo(\sliderParam, src.getHalo(\sliderParam));
		dest.addHalo(\spec, src.getSpec.copy);
	};
};
);

/* tests:
// all sounds off at first
q.quiet;

// copy src0 to dest 0
q.srcTogPlay(0, 1);    // src 0 plays
q.sources[0].getHalo(\isOn);  // checking
q.copySnd(0, 4);      // copy to 4, should also play
q.srcTogPlay(0, 0);
q.srcTogPlay(4, 0);
q.allSnds[0].getSpec;   // should copy specs as well
q.allSnds[4].getSpec;


q.srcTogPlay(1, 0);    // src 1 is off
q.copySnd(1, 5); // copy to snd 5, mute flag
q.allSnds[5].getSpec;  // spec copied
q.srcTogPlay(5, 1); // now src 5 can play that sound
q.srcTogPlay(5, 0); //  and off again

// all sounds off
 q.quiet;

*/

// ATTACHING TO CONTROLLERS:
// To attach this basic layer of functions to an MPD18, one can:
// 1. Make an MPD18 MIDIMKtl:
/*
//
m = MKtl('mpd', "*mpd18");  // Akai MPD18
*/
// if you don't have one, this makes a virtual one by gui, or
// 2. Make a GUI that resembles an MPD18 and has all the
// elements required for the use case, and use that.
(
// gui adapted from Miguel's example,
// do only layout and labels at first
q = q ? ();
q.mpdwin = Window("MPD18 use case JIT/MFunc (JNCv2)", Rect(10, 500, 400, 350)).front;

// 16 buttons, button indices start bottom left
// buttons have two states, to allow for simulated holding down
// (button polyphony) when playing with just a mouse.
q.buts = 4.collect { |i|
	4.collect {|j|
		Button(q.mpdwin,
				Rect(i * 80 + 5, 240 - (j * 80) + 5, 75, 75))
		.states_([
			["dest" + (i + (j * 4)), Color.black],
			["down" + (i + (j * 4)), Color.black, Color.green]

		])
		.action_(MFunc());
	}
}.flop.flat;

// a single slider on the right
q.sl = Slider(q.mpdwin, Rect(340, 25, 40, 280));

// of the 16 buttons, the bottom right is a shift button
q.shifter = q.buts[3];
// and it gets two states, so one can simulate holding that pad down
q.shifter.states_([["shift", Color.black], ["SHIFT", Color.black, Color(1.0, 0.75)]]);

// the bottom 3 from left are source buttons
q.srcButs = q.buts.keep(3);
q.srcButs.do { |bt, i| bt.states_([["src" + i], ["down" + i, Color.black, Color.green]]) };

// and the others are destination buttons.
q.memButs = q.buts.drop(4);
// put them in single var for convenience
q.sndButs = q.srcButs ++ q.memButs;
);


// add basic layer of functions :
(
// slider will set all active sounds, as tested above
q.sl.action = { |sl| q.sliderSet(sl.value) };

// On GUIs, button action happens on mouseUp, not like pads when tapping them.
// also, a mouse can only hold a single button, so to clicking a button simulates
// both tap-hold and pad-release. holding down is shown by button state "down".
q.srcButs.do { |bt, i|  bt.action = { |bt| q.srcTogPlay(i, bt.value) } };

// sketch the shift button - only posts for now
q.shifter.action = { |b|
	if (b.value > 0) { \enterCopyMode.postcs; } { \basicPlayMode.postcs; }
};
);

// tests:
// - slider sets a param on every active source
// - src buttons toggle their sources on and off
// shift button posts info only
// dest buttons do nothing so far.



/*** Concepts and names for the three modes needed for the use case:

*** basic play mode:
///  use case
+ [Sound Buttons] Buttons 1-3 are mapped to adsr enveloped sound sources.
  + By pushing them down sound turns on; releasing: sound off.
///
-> basicPlay mode: src and memory buttons turn sounds on/off

*** copyGet and copyPut modes:
//// use case
+ [Memory Slots] Buttons 5-16 represent 'memory' positions (initially not mapped)
  + if sound is assigned (see below), sound is played when button depressed.

+ [Shift Button] Button 4 is a 'shift key'. When depressed
  1. Sound Buttons don't trigger any sound but select the active slot. This can be followed by
  2. depressing a Memory Slot button, which assigns the selected sound to that pad.
  3. if you release the shift key before assignment, nothing happens.
  4. assigning a copy to an already assigned memory slot replaces existing
  5. mute copy
////
-> copyGet mode: waiting for next button to determine copy source
-> copyPut mode: there is a valid copy source, next button sets destination

*** loud or silent copy
//// use case
+[Sound Button then Shift button]
  1. Sound Button triggers sound
  2. depress Memory Slot button, assigning the sound to the pad, with sound

-> when a sound is on, and copyGet mode is entered,
  -> sound becomes copy source, and copyPut mode is entered

*/


// USING MODAL FUNCTIONS:
// first, put an MFunc as action into every modal gui element,
// then add debug functions as examples of multiple funcs
// then add the functions as used above with simple names.
(
q.sl.action = MFunc.new;
q.sl.action.add(\debug, { |sl| "// sliderSet: %\n".postf(sl.value) });
q.sl.action.add(\sliderSet, { |sl| q.sliderSet(sl.value) });

// turn off posting
q.sl.action.disable(\debug);

// same with source buttons:
q.srcButs.do { |bt, i|
	bt.action = MFunc.new;
	bt.action.add(\debug, { "// bt % on: %\n".postf(i, bt.action.activeNames) });
	bt.action.add(\toggle, { |bt| q.srcTogPlay(i) });
};

q.shifter.action = MFunc.new;
q.shifter.action.add(\debug, { |bt|"// shifter: % \n".postf(bt.value) });
// will be replaced with mode-switch function later
q.shifter.action.add(\copyMode, { |b|
	if (b.value > 0) {
		\enterCopyMode.postcs;
	} {
		\basicPlayMode.postcs;
	}
});
);

// tests:
// - buttons, slider, shifter post debug info now
// - other functions work as above
// - debug info can be turned on and off easily with
// xyz.action.disable(\debug);


// now build the second mode - copyGet
// in this mode, the next src or dest button selects
// which sound will be copied. sound is assumed off.
(
q.srcButs.do { |bt, i|
	bt.action.disable(\toggle);
	bt.action.add(\copyGet, { |bt|
		q.srcIndexToCopy = i;
		[\srcIndexToCopy, q.srcIndexToCopy].postln;
		bt.value = 1 - bt.value; // dont change button state
	});
};

// make memButs behave like srcButs now:
// they can toggle their respective sounds,
// and they can be selected as copySource.
q.memButs.do { |bt, i|
	i = i + 4;  // offset for 16 sounds

	// same funcs as srcButs already had:
	bt.action = MFunc.new;
	bt.action.add(\debug, { "// bt % on: %\n".postf(i, bt.action.activeNames) });
	bt.action.add(\toggle, { |bt| q.srcTogPlay(i) });

	// new ones srcButs just got:
	bt.action.disable(\toggle);
	bt.action.add(\copyGet, { |bt|
		q.srcIndexToCopy = i;
		[\srcIndexToCopy, q.srcIndexToCopy].postln;
		bt.value = 1 - bt.value; // dont change button state
	});
};
);

// next, add copyPut mode to q.memButs
// this action happens only when the next button pressed is a dest button:
// that proxy is then selected as copy destination,
// and the sound from copy source is copied to destination.
(
q.memButs.do { |bt, i|
	bt.action.disable([\toggle, \copyGet]);
	bt.action.add(\copyPut, { |bt|
		q.copySnd(q.srcIndexToCopy, i + 4);
	});
};

// can't copy to a srcBut, so put an info copyPut func there.
q.srcButs.do { |bt, i|
	bt.action.add(\copyPut, { "can't copy to src button...".inform });
};
);

/// then, switch between modes:
// declare them as modes first in all the MFuncs,
// then switch between them in a coordinated way.

// basicPlay, copyGet and copyPut are mutually exclusive;
// so when one of them is active, the others should be off.
// basicPlay:
// srcButs and memButs only have \toggle on
// copyGet mode:
// srcButs and memButs only have \copyGet on
// copyPut mode:
// memButs only have \copyPut on, srcButs do nothing

(
// declare all modes for source buttons and memory buttons:
q.sndButs.do { |bt, i|
 	bt.action.modeLists.put(\ex3, [\toggle, \copyGet, \copyPut]);
	bt.action.modes.put(\basicPlay, (on: \toggle, off: \ex3));
	bt.action.modes.put(\copyGet, (on: \copyGet, off: \ex3));
	bt.action.modes.put(\copyPut, (on: \copyPut, off: \ex3));
};
);
// test modes on srcButs:
/*
q.srcButs.do { |bt| bt.action.mode_(\basicPlay); };
q.srcButs[0].doAction;
q.srcButs[0].doAction;
q.srcButs.do { |bt| bt.action.mode_(\copyGet); };
q.srcButs[1].doAction;
q.srcButs.do { |bt| bt.action.mode_(\copyPut); };
q.srcButs[2].doAction;

// test modes on memButs:
q.memButs.do { |bt| bt.action.mode_(\basicPlay); };
q.srcButs[0].doAction;
q.srcButs[0].doAction;
q.memButs.do { |bt| bt.action.mode_(\copyGet); };
q.srcButs[0].doAction;
q.memButs.do { |bt| bt.action.mode_(\copyPut); };
q.srcButs[0].doAction;
*/

// now, a function to go into copy mode:
(
q.enterCopyMode = {
	if (q.getActives.size < 1) {
		"mute copy mode - next snd becomes copy src.".postln;
		\copyGet.postln;
		q.sndButs.do { |but| but.action.mode_(\copyGet) };
	} {
		q.srcIndexToCopy = q.getActiveIndices.first;
		"loud copy mode - will copy snd % to next destBut.\n".postf(q.srcIndexToCopy);
		\copyPut.postln;
		q.sndButs.do { |but| but.action.mode_(\copyPut) };
	};
};
);
/*
// test silent copy first
q.quiet;

q.enterCopyMode;
q.sndButs[1].doAction;
q.sndButs.do { |but| but.action.mode_(\copyPut) };

q.sndButs[10].doAction; // copyPut
q.srcTogPlay(11);  // snd 11 now has sound from 1, play
q.srcTogPlay(11);  // stop

// now go back to basic play mode:
q.sndButs.do { |but| but.action.mode_(\basicPlay) };

// test loud copy:
q.quiet;
q.srcTogPlay(2, 1);

q.enterCopyMode; // goes to copyPut directly
q.sndButs[0].doAction; // cant copy to source but
q.sndButs[7].doAction; // move to dest 8
q.srcTogPlay(2, 0);
q.srcTogPlay(8, 0);

// now go back to basic play mode:
q.sndButs.do { |but| but.action.mode_(\basicPlay) };
q.sndButs[7].doAction;

*/

// now use the shift button to control the modes:
// give the buttons their behavior repertoires :
// shift button

(
q.basicPlayMode = {
	"basicPlayMode".postln;
	q.sndButs.do { |but| but.action.mode_(\basicPlay) };
	q.shifter.value_(0);
};

q.shifter.action.add(\copyMode, { |b|
	if (b.value > 0) {
		q.enterCopyMode;
	} {
		q.basicPlayMode;
	}
});

// finally change copyPut and copyGet to move to next modes:

// after copyPut finishes, go back to basicPlay
// (defer to avoid inconsistency when activeFuncs list
// modifies itself while iterating)
q.memButs.do { |bt, i|
	bt.action.add(\copyPut, { |bt|
		var butVal = q.getActives.size.sign;
		q.copySnd(q.srcIndexToCopy, i + 4);
		bt.value = butVal;
		defer ({ q.basicPlayMode }, 0.02);
	});
};

// after copyGet finishes, go on to copyPut
q.sndButs.do { |bt, i|
	bt.action.add(\copyGet, { |bt|
		q.srcIndexToCopy = i;
		[\srcIndexToCopy, q.srcIndexToCopy].postln;
		bt.value = 1 - bt.value; // dont change button state
		defer ({
			q.sndButs.do { |bt| bt.action.mode_(\copyPut); };
		}, 0.02);
	});
};
);

// NOW ready to test that everything works as described in use case!

q.basicPlayMode;

