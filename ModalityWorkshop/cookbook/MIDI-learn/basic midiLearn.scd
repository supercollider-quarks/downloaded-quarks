// midi learn: make two functions that
// control a synth process
//  1. start and stop
//  2. set frequency
// These features should be assigned to the two controllers that are moved next.



s.boot;
s.latency = nil;

m = MKtl('nk2', "korg-nanokontrol2"); // the MKtl


q = (); // a place to put stuff

(
// freq action to assign to e.g. to one of the sliders

q[\setFreq] = {|el|
	[el, el.value].postln;
	q.mySynth.notNil.if{
		q.freq = el.value.linexp(0, 1, 200, 2000);
		q.mySynth.set(\freq, q.freq);
	};
};

// synth creation action to assign, to one of the buttons
q[\synthCtl] = {|el|
	[el, el.value].postln;
	(el.value > 0).if({
		q.mySynth = Synth(\default, [\freq, q.freq]);
	}, {
		q.mySynth.notNil.if{
			q.mySynth.release;
			q.mySynth = nil;
		};
	})
};
)


// group of elements from which the element is selected from,
// get them from the nanoKontrol 2 :
q.sliders = m.elAt(\sl);
q.buttons = m.elAt(\bt);

// this action goes to the group of elements
// and will add the action to the first element activated:

q.makeLearnAction = {|q, group, action, callback|

	var learnAction = {|el|
		el.addAction(action);
		"Added func to element %\n".postf(el);
		// set the element to a variable
		callback.value(el);
		// after one element learned it, stop learning
		group.removeAction(learnAction);
		"learnAction removed from group %.\n".postf(el);
	};

	group.addAction(learnAction);
};

// now put them there
q.makeLearnAction(q.sliders, q[ \setFreq], {|el| q. freqCtlElem = el});
q.makeLearnAction(q.buttons, q[\synthCtl], {|el| q.synthCtlElem = el});

q.freq = 100;
)

// hit one of the buttons to have it learn:
// -> on the next push and release, it will turn a synth on and off.

// next, while holding down button to play the sound,
// move one of the sliders to have it learn:
// -> it will now change the sound's freq parameter.


// remove actions again from the elements who learned them:
q. freqCtlElem.removeAction(q[ \setFreq]);
q.synthCtlElem.removeAction(q[\synthCtl]);

