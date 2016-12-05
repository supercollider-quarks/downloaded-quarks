/***
This file shows how to connect to proxies via their guis,
which can have several advantages:

* visual feedback on which controller maps to which param
* flexibility by putting different proxies into the same gui.
* JITLib overview guis like the NdefMixer already provide
access to all running proxies of one kind; attaching a
controller to such a gui allows access to all processes
by physical control.


***/

q = q ? ();

// first, make an Ndef with 9 params, same as in prev. file
(
Ndef(\test, { |freq=250, intv=19, timescale=1, curve=0, loopnode=0, formfact=2, widfact=0.5, spread=1, amp=0.1|
		var env = EnvGen.kr(
			Env(
				{ 1.0.rand2 }!11,
				{1.0.rand}!10 * timescale,
				curve,
				releaseNode: 9,
				loopNode: loopnode)
		);
		var pitch = (env * [1, 0.33, -1] * intv).midiratio * freq;
		Splay.ar(
			Formant.ar(pitch, pitch.scramble * formfact, pitch.scramble * widfact), spread) * amp;
});
	// add specs for all the parameter ranges - required!
	Spec.add(\intv, [0, 36, \lin]);
	Spec.add(\timescale, [0.001, 10, \exp]);
	Spec.add(\curve, [-10, 10]);
	Spec.add(\loopnode, [0, 7, \lin, 1]);
	Spec.add(\formfact, [0.1, 10, \exp]);
	Spec.add(\widfact, [0.1, 10, \exp]);
	Spec.add(\spread, \unipolar);
);

q.nk = MKtl(\nk2, "*trol2");
q.nkg = q.nk.gui;

q.ndgui = NdefGui(Ndef(\test), 10);

// map group of mktl elements to the NdefGui's proxy and editKeys
// - note that this depends on the gui!

q.mapElemGroupToNdefGui = { |q, group, ndefGui|
	var numGuiSliders = ndefGui.paramGui.numItems;
	group.keep(numGuiSliders).do { |el, i|
		el.action = { |el|
			var proxy, paramKey;
			proxy = ndefGui.proxy;
			if (proxy.notNil) {
				paramKey = ndefGui.editKeys[i];
				proxy.softSet(
					paramKey,
					el.value,
					within: 0.05,
					mapped: false,
					lastVal: el.prevValue,
					spec: paramKey.asSpec
				)
			};
		};
	}
};

q.mapElemGroupToNdefGui(
	q.nk.elAt(\kn).keep(7),
	q.ndgui
);

// use last knob for volume control of current ndgui proxy:
q.nk.elAt(\kn).last.action = { |el|
	q.ndgui.proxy.softVol_(\amp.asSpec.map(el.value));
};
// add play and stop buttons
q.nk.at(\tr, \play).action = { |el| q.ndgui.proxy.play; };
q.nk.at(\tr, \stop).action = { |el| q.ndgui.proxy.stop; };


// now stick nil into NdefGui - does nothing
q.ndgui.proxy_(nil); // does nothing

// make the second proxy, same as in proxies1:
(
Spec.add(\slope, [0.1, 1, \exp]);
Spec.add(\formfreq, [50, 5000, \exp]);
Spec.add(\reso, [1, 10, \exp]);

Ndef(\test2, { |freq = 2000, slope = 0.8,
	formfreq = 5000, reso = 10, amp = 0.5|

	var freqscale = (slope ** (0..5));
	var snd = { LFDNoise0.ar(freq * freqscale).product } ! 2;
	RLPF.ar(snd, formfreq, 1/reso) * amp;
}).play;
)

// ... and set it again now - all controls should be connected.
q.ndgui.proxy_(Ndef(\test2));

// and back to first proxy:
q.ndgui.proxy_(Ndef(\test));

// now assign rew and fwd buttons to switch between \test and \test2
q.nk.elAt(\tr, \rew).action = { |el| q.ndgui.proxy_(Ndef(\test)); };
q.nk.elAt(\tr, \fwd).action = { |el| q.ndgui.proxy_(Ndef(\test2)); };



// should you close the gui by mistake,
q.ndgui.close;
// you can fully recover by running its code again:

q.ndgui = NdefGui(Ndef(\test), 8);
q.ndgui.skipjack.dt = 0.05;

// mapElemGroupToNdefGui method was not smart enough yet
// to only reference q.ndgui, therefore must also run again:
q.mapElemGroupToNdefGui(
	q.nk.at(\kn).keep(7),
	q.ndgui
);


// SCALING this up to an overview gui, the NdefMixer:

q.ndmix = NdefMixer(s, 8);

q.ndmix.switchSize(2);
q.ndmix.editGui.proxy_(Ndef(\test));

// map all controls used so far to the editGui:
// map knobs to param controls
q.mapElemGroupToNdefGui(
	q.nk.elAt(\kn).keep(7),
	q.ndmix.editGui
);
// use last knob for volume control of current ndgui proxy:
q.nk.elAt(\kn).last.action = { |el|
	q.ndmix.editGui.proxy.softVol_(\amp.asSpec.map(el.value));
};
// add play and stop buttons
q.nk.elAt(\tr, \play).action = { |el| q.ndmix.editGui.proxy.play; };
q.nk.elAt(\tr, \stop).action = { |el| q.ndmix.editGui.proxy.stop; };

// now, map the nk sliders to the volume controls:
// (nested checks for nil to avoid setting empty ones)
q.nk.elAt(\sl).do { |sl, i|
	sl.action = { |el|
		var argui, proxy;
		argui = q.ndmix.arGuis[i];
		if (argui.notNil) {
			proxy = argui.object;
			if (proxy.notNil) {
				proxy.softVol_(
					\amp.asSpec.map(el.value),
					lastVal: \amp.asSpec.map(el.prevValue)
				);
			}
		}
	};
};

// now, map the nk top row of buttons
// to send that proxy to the editGui:
q.nk.elAt(\bt).at(0).do { |bt, i|
	bt.action = { |el|
		var argui, proxy;
		argui = q.ndmix.arGuis[i];
		if (argui.notNil) {
			proxy = argui.object;
			if (proxy.notNil) {
				// has to be deferred because MIDI is
				// in realtime thread and Qt is not
				defer { q.ndmix.editGui.proxy_(proxy) };
			}
		}
	};
};

// to do - enable keysRotation on larger mixers and NdefGuis,
// so one can shift around on larger gui slider groups.

