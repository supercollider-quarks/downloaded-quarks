
// common helper funcs
Library.put(\clLivecode, \setupbars, \addToDoc, Main.versionAtLeast(3, 7) and: { Platform.ideName == "scqt" });

{ |key, n(4), prefix("m")|
	var phrases = [[prefix], (0 .. n-1)].flop.collect(_.join),
	stepForward, pos;
	phrases.do { |phrase, i|
		\chucklibLiveCode.eval("/%.% = \"\";".format(key, phrase)).interpret;
	};
	if(Library.at(\clLivecode, \setupbars, \addToDoc) ? false) {
		stepForward = PR(\clPatternToDoc)[\stepForward];
		if(stepForward.isNil) {
			stepForward = PR(\clPatternToDoc).checkStepForward;
		};

		pos = 'Document'.asClass.current.selectionStart + 'Document'.asClass.current.selectionSize;
		if(stepForward.not) { pos = pos + 1 };
		'Document'.asClass.current.string_(
			"\n" ++ (phrases.collect { |phr| "/%.% = \"\";\n".format(key, phr) }.join),
			pos, 0
		);
	} {
		"Added " ++ phrases
	}
} => Func(\setupbars);

{ |key, n(4), prefix("m"), phrases|
	var argPhrases = phrases, str;
	phrases ?? {
		phrases = [[prefix], (0 .. n-1)].flop.collect(_.join);
	};
	str = "/% = (%)".format(
		key,
		// '^prefix' supports easy variations:
		// /proc.m00 = ...
		// /proc.m01 = ... and so on
		phrases.collect { |phr| "'^%'".format(phr) }.join($.),
	);
	if(argPhrases.isNil) { str = "%(%)".format(str, n.neg) };  // set BP's quant
	\chucklibLiveCode.eval(str).interpret;
} => Func(\setm);

{ |key, n(4), prefix("m")|
	\setm.eval(key, n, prefix);
	\setupbars.eval(key, n, prefix);
} => Func(\bars);

{ |vcKey, bpKey|
	if(bpKey.notNil) {
		if(BP.exists(bpKey)) {
			VC(vcKey) => BP(bpKey);
		} {
			"VC(%) should go into BP(%), but it doesn't exist. Remember to => manually"
			.format(
				vcKey.asCompileString,
				bpKey.asCompileString
			)
			.warn;
		};
	};
} => Func(\vcToDefaultBP);


// apply a voicer to the default event prototype
{ |ev|
	var mixer;
	if(ev[\voicer].notNil) {
		ev.put(\instrument, ev[\voicer].nodes[0].defname);
		mixer = ev[\voicer].bus.asMixer;
		if(mixer.notNil) {
			ev.put(\group, mixer.synthgroup)
			.put(\out, mixer.inbus.index);
		};
		ev[\voicer].nodes[0].initArgs.pairsDo { |key, value|
			// difficult. If the event has a default, the event's default takes precedence.
			if(ev[key].isNil) { ev.put(key, value) };
		};
		ev[\voicer].globalControls.keysValuesDo { |key, value|
			ev.put(key, value.asMap);
		};
	};
	ev
} => Func(\voicer);
