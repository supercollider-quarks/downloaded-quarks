/***
This file shows how to connect to a nanoKontrol 2 to a MasterFX instance.

Not finished yet, ends with some open code design questions.

***/

q = q ? ();

Ndef(\in, { SoundIn.ar([0, 1]) }).play;

// Make the Master FX Chain first

(
// The order in slotNames determines order of execution of the efx.
MasterFX.clear;
try { q.masterFXGui.close };
q.masterFX = MasterFX.new(server: s,
   numChannels: 4,
   slotNames: [\leakDC, \rollClip, \krush, \ampFin],
   busIndex: 0
);
q.masterFXGui = q.masterFX.gui; // a GUI

q.masterFXGui.skipjack.dt = 0.05;
q.masterFXGui.editGui.skipjack.dt = 0.05;
)

q.nk = MKtl(\nk2, "korg-nanokontrol2");
q.nkgui = q.nk.gui;





(
// some FX from MasterFX.openHelpFile;
ProxyChain.add(
    \leakDC,  \filter -> { |in, leak=0.99| LeakDC.ar(in, leak) },
    \rollClip, \filter -> { |in, clip=0.99, pole=0.2|
        OnePole.ar(in.clip2(clip), pole ** 0.7);
    },
    \krush, \filter -> { |in, bits=16, downsamp=2|
        var down;
        in = in.round(0.5 ** bits);
        down = Latch.ar(in, Impulse.ar(SampleRate.ir / downsamp.max(2)));
        // below 1/2 downsamp, do xfade:
        blend(in, down, (downsamp - 1).clip(0, 1));
    },
    \ampFin, \filter -> { |in, limDrive=1, ampLimit=0.8, postAmp=1 |
        Limiter.ar(in * limDrive, ampLimit) * postAmp;
    }
);

// and specs for them (e.g. for gui control)
Spec.add(\leak, [0.5, 0.999, \exp]);
Spec.add(\clip, [0.0, 1, \amp]);
Spec.add(\pole, [0, 0.95, \lin]);
Spec.add(\bits, [16, 1]);
Spec.add(\downsamp, [1, 100, \exp]);
Spec.add(\limDrive, \ampx4);
Spec.add(\ampLimit, \amp);
Spec.add(\postAmp, \ampx4);
)

// each channel-group of elements belongs to one effect,
// in their order in MasterFX.

// top row button S de/activates the effect
// slider controls wet/dry balance of each effect
// middle buttons assigns knobs to the params of that effect
// (and further on down from there)

// top row button S de/activates the effect

q.nk.elementAt(\bt, 0).do { |bt, i|
	bt.action = { |bt|
		var guibut = q.masterFXGui.buttons[i];
		if (bt.value > 0) {
			defer { guibut.valueAction_(1 - guibut.value) };
		};
	};
};

// slider controls wet/dry balance of each effect
q.nk.elementAt(\sl).do { |sl, i|
	sl.action = { |sl|
		var pxChain, slotName, slotObj, slotNumber, ctlName;
		pxChain = q.masterFX.pxChain;
		slotNumber = pxChain.slotNames.indices[i];
		slotObj = pxChain.proxy.objects[slotNumber];
		if (slotObj.notNil) {
			ctlName = slotObj.controlNames[3].name;
			pxChain.proxy.softSet(ctlName, sl.value,
				lastVal: sl.prevValue);
		};
	};
};


// assign elements from a specific control on,
// so one can jump to all controls from an effect
q.mapElementsToParamsFrom = { |q, elemgroup, editGui, indexOffset = 0|
	elemgroup.do { |elem, i|
		elem.action = { |el|
			var proxy, paramKey, spec;
			proxy = editGui.proxy;
			paramKey = editGui.editKeys[i + indexOffset];
			if (paramKey.notNil) {
				spec = proxy.getSpec(paramKey);
				spec = spec ?? { editGui.paramGui.specs[paramKey] };
				proxy.softSet(paramKey,
					spec.map(el.value),
					within: 0.04,
					lastVal: spec.map(el.prevValue));
			};
		}
	};
};

// kick some in, then begin from 3 (e.g. if krush is first):
q.mapElementsToParamsFrom(
	q.nk.elementAt(\kn),
	q.masterFXGui.editGui,
	3
);

// now button M should jump knobs to the controls of that effect...
// hmmm, that only works when that effect is on!
// how best to write that?

// check if that effect is on, and if it is,
// get offset of its control in editKeys?
// or maybe better - get names from the synthcontrol object,
// and map t the names directly? hmmm, intransparent.
// 'wet40'.matchOSCAddressPattern('wet*');
// still to be considered how to do that best ...


// use rightmost slider for global server volume control :
Spec.add(\mastaVol, [s.volume.min, s.volume.max, \db]);
q.nk.at(\sl).last.action = { |el|
	s.volume.volume_(\mastaVol.asSpec.map(el.value));
};
