
NanoKtl2 : MIDIKtl {
	classvar <>verbose = false;

	var <pxmixer, <pxEditor, <pxOffset = 0, <parOffset = 0;
	var <>softWithin = 0.05, <lastVals;

	*initClass {
		this.makeDefaults;
	}

	init {
		super.init;
		ctlNames = defaults[this.class];
		orderedCtlNames = ["kn", "sl", "bu", "bm", "bd"]
		.collect { |str, i|
			(1..8).collect { |j| (str ++ j).asSymbol }
		};

		lastVals = ();

		^this
	}

	mapToEnvirGui { |gui, scene, indices|
		var elementKeys;
		indices = indices ? (1..8);

		elementKeys = orderedCtlNames[indices - 1].postcs;

		elementKeys.do { |key, i|
			this.mapCC( key,
				{ |ccval|
					var envir = gui.envir;
					var parKey =  gui.editKeys[i];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (envir.notNil and: { parKey.notNil } ) {
						envir.softSet(parKey, normVal, softWithin,
							false, lastVal, gui.getSpec(parKey))
					};
					lastVals.put(key, normVal) ;
				}
			)
		};
	}
	//
	// map directly to a proxy's params. no Gui needed.
	mapToProxyParams { |proxy ... pairs|
		pairs.do { |pair|
			var ctlName, paramName, spec;
			#ctlName, paramName = pair;
			spec = paramName.asSpec;

			this.mapCC(ctlName,
				{  |ccval|
					proxy.softSet(paramName,
						ccval / 127, softWithin,
						false, lastVals[ctlName], spec)
				}
			);
		};
	}


	mapToNdefGui { |gui, volPause = true|
		var kns = orderedCtlNames[0];
		pxEditor = gui;

		// map first 7 knobs to params - can be shifted
		kns.drop(-1).do { |key, i|

			this.mapCC( key,
				{ |ccval|
					var proxy = pxEditor.proxy;
					var parKey =  pxEditor.editKeys[i + parOffset];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) {
						proxy.softSet(parKey, normVal, softWithin, mapped: false, lastVal: lastVal)
					};
					lastVals.put(key, normVal);
				}
			)
		};
		// and use 9th knob for proxy volume
		this.mapCC( kns.last, { |ccval|
			var lastKnName = kns.last;
			var lastVal = lastVals[lastKnName];
			var mappedVol = \amp.asSpec.map(ccval / 127);
			var proxy = pxEditor.proxy;
			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };
			if (proxy.notNil) {
				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal)
			};
			lastVals[lastKnName] = mappedVol;
		} );
	}

	mapToMixer { |mixer, scene = 1|

		var server, mastaFunc, spec;
		pxmixer = mixer;
		server = mixer.proxyspace.server;

		//	mastaFunc = Volume.softMasterVol(0.05, server, \midi.asSpec);

		spec = Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
		this.mapCC( \sl8, { |val| server.volume.volume_(spec.map(val/127)) });

		// map first 8 volumes to sliders
		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7].do { |key, i|
			this.mapCC( key,
				{ |ccval|
					var lastVal = lastVals[key];
					var ampSpec = \amp.asSpec;
					var normVal = ccval / 127;
					var mappedVal = ampSpec.map(normVal);
					var lastVol = if (lastVal.notNil) { ampSpec.map(lastVal) };

					try {

						pxmixer.arGuis[i + pxOffset].proxy
						.softVol_( mappedVal, softWithin, true, lastVol, ampSpec );
					};

					lastVals[key] =  normVal;
				};
			)
		};
		// upper buttons: send to editor
		[\bu1, \bu2, \bu3, \bu4, \bu5, \bu6, \bu7, \bu8].do { |key, i|
			this.mapCC( key, { |ccval|
				defer { if (ccval > 0) {
					pxmixer.arGuis[i + pxOffset].edBut.doAction
				} };
			})
		};

		// lower buttons: toggle play/stop
		[\bd1, \bd2, \bd3, \bd4, \bd5, \bd6, \bd7].do { |key, i|
			this.mapCC( key,
				{ |ccval| defer {

					var pxGui = pxmixer.arGuis[i + pxOffset];
					var playBut = pxGui.monitorGui.playBut;
					var proxy = pxGui.proxy;


					if (proxy.notNil) {
						if ( ctlNames['mode'] == 'push' ){
							if (ccval == 127) {
								playBut.valueAction_(1 - playBut.value); // toggle on pushing
							};
						};
						if ( ctlNames['mode'] == 'toggle' ) {
							playBut.valueAction_(ccval.sign);
						};
					};
				};
			});
		};

		this.mapCC( \bu8, { |ccval| if (ccval > 0) { this.pxShift(1, scene) } });
		//	this.mapCC( \bd8, { |ccval| if (ccval > 0) { this.paramShift(1, scene) } });

		this.pxShift(0);
		this.mapToNdefGui(mixer.editGui, true);
		//	this.paramShift(0, scene);
	}

	pxShift { |step = 1, scene=1|

		var numActive = pxmixer.arGuis.count { |mon| mon.proxy.notNil };
		var maxOff = (numActive - 8).max(0);
		var pxOffset = (pxOffset + step).wrap(0, maxOff);
		pxOffset = pxOffset;

		pxmixer.highlightSlots(pxOffset, 8);
	}

	paramShift { |step = 1, scene=1|

		var numActive = pxEditor.editKeys.size;
		var maxOff = (numActive - 8).max(0);
		var parOffset = (parOffset + step).wrap(0, maxOff);
		pxEditor.highlightParams(parOffset, 8);
	}

	*makeDefaults {
		var dict = (
			rew: '0_43',	play: '0_41', fwd: '0_44',
			loop: '0_46', stop: '0_42', rec: '0_45',
			mset: '0_60', mleft: '0_61', mright: '0_62',
			tleft: '0_58', tright: '0_59'
		);

		[ ["sl", (1..8)],
			["kn", (16..23)],
			["bu", (32..39)],
			["bm", (48..55)],
			["bd", (64..71)]
		].do { |pair|
			var name, midiNums, key;
			#name, midiNums = pair;
			midiNums.do { |num, i|
				key = (name ++ (i+1)).asSymbol;
				dict.put(key, ("0_" ++ num).asSymbol);
			};
		};

		defaults.put(this, dict)
	}
}
