VGSpecEditor {
	var <keyBus, <name, <indexToSet, <mulTree;

	var <parent, <comp, <editors;
	var <popup, <nameBut, <funcBut, <indexBox;

	var <>nameFunc, <>playFunc;

	*new { |keyBus, parent, top=false, slide=false, buttons=false, pop=false,
		indexToSet, buttonsWidth=100|
		^super.new.init(keyBus, parent, top, slide, buttons, pop, indexToSet, buttonsWidth);
	}

	init { |inKeyBus, inparent, top, slide, buttons, pop, argIndexToSet, buttonsWidth|
		var width = 1140;
		var height = 50;
		var gap = 2@2;

		if (slide, { height = height + 20 });
		if (top, { height = height + 32 });
 		if (buttons or: pop) {
 			buttonsWidth = buttonsWidth ? 100;
 			width = width + buttonsWidth
 		};

		parent = inparent ?? {
			parent = Window(this.class.asString,
				Rect(40, 700, width + 8, height + 8)
			).front;
			parent.view.decorator = FlowLayout(parent.bounds.moveTo(0,0), 2@2, 2@2);
			parent;
		};

		comp = CompositeView(parent, Rect(0, 0, width, height + 8));
		comp.decorator = FlowLayout(comp.bounds, gap, gap);

		if (buttons) { this.makeButtons(pop, buttonsWidth, height) };

		editors = VGTuning.busKeys.collect { |specName, i|
			var editclass = [VGFreqEditor, VGTimeEditor, VGAmpEditor, VGTimeEditor][i];
				editclass.new(parent: comp, top: top, slide: slide);
		};

		this.setBus(inKeyBus, indexToSet);
	}

	makeButtons {|pop, buttonsWidth, height|

		var popHeight = 0, nameButHeight = 0.55, funcButHeight = 0.34, funcWidth2;

//		if (pop) { popHeight = 0.55 };
//		#popHeight, nameButHeight, funcButHeight =
//		([popHeight, nameButHeight, funcButHeight].normalizeSum * 0.95
//			* height).asInteger;
//
//		if (pop) {
//			popup = PopUpMenu(comp, Rect(0, 0, buttonsWidth, popHeight))
//				.items_([\a, \b, \c])
//				.action_ { |pop|
//					"pop action: value %, item %".format
//						(pop.value, pop.items[pop.value]).postln;
//				};
//				comp.decorator.nextLine;
//		};

			// if popup, drop the button
		if (pop) { popHeight = 0.55 };
		#popHeight, nameButHeight, funcButHeight =
		([popHeight, nameButHeight, funcButHeight].normalizeSum * 0.95
			* height).asInteger;

		if (pop) {
			popup = PopUpMenu(comp, Rect(0, 0, buttonsWidth, popHeight + nameButHeight))
				.items_([\a, \b, \c])
				.action_ { |pop|
					"pop action: value %, item %".format
						(pop.value, pop.items[pop.value]).postln;
				};
			comp.decorator.nextLine;
			nameButHeight = 0;
		};

		nameBut = Button(comp, Rect(0, 0, buttonsWidth, nameButHeight))
			.states_([['..keybus..']])
			.action_({ nameFunc.value(nameBut) });
		comp.decorator.nextLine;

		funcWidth2 = buttonsWidth - 4 * 0.8;

		funcBut = Button(comp, Rect(0, 0, funcWidth2 ? buttonsWidth, funcButHeight))
			.states_([[\play]])
			.action_ ({ playFunc.value(funcBut) });

			// always make index box, don't show when indexToSet = nil
		indexBox = NumberBox(comp,
			Rect(0, 0, (buttonsWidth - 4 * 0.2), funcButHeight)
		).string_("-").enabled_(false);

		comp.decorator.reset.shift(buttonsWidth + 8);
	}

	setBus { |inBus, inIndex|

		if (inBus.isNil) {
			"VGSpecEditor: keyBus is nil.".inform;
			keyBus = nil;
			editors.do {|ed|
				ed.boxes.do(_.string_(""));
				ed.comp.enabled_(false);
			};
			^this
		};

		editors.do {|ed| ed.comp.enabled_(true) };

			// sloppy but maybe useful
		if (inBus.isKindOf(VGMulTree)) {
			this.setMulTree(inBus, inIndex.isNil, inIndex);
			^this
		};

			// single arguments bus
		if (inBus.isMono) {
		//	"KeyBus: single values bus.".postln;
			if (inIndex.notNil) {
				"KeyBus: cannot set index for a single-values bus. ignoring indexToSet.".warn
			};
			indexToSet = nil;
			this.monoBus_(inBus);
			^this
		};

			// poly-values bus
		if (inBus.isPoly) {
		//	"KeyBus: poly-values bus.".postln;
			if (inIndex.isNil) {
			//	"KeyBus: assuming index for poly bus is 0.".inform;
			};
			inIndex = (inIndex ? 0).clip(0, inBus.values.first.size - 1);
			this.polyBus_(inBus, inIndex);
			^this
		};
	}

	setMulTree { |tree, setBase = true, inIndex|
		if (tree.isNil) {
			mulTree = nil;
			this.setBus(nil);
			^this
		};
		mulTree = tree;

		if (setBase) {
			this.setBus(mulTree.mulBus);
		} {
			this.setBus(mulTree.relBus, inIndex);
		}
	}
		// have checked already
	monoBus_ { |inBus|

		keyBus = inBus;
		inBus.values.do { |val, i|
			var edi = editors[i];
			edi.sendFunc_({ |edival|
			//	"VGSpecEditor: normal, mulBus".postln;
				keyBus.set(VGTuning.busKeys[i], edival)
			});
			edi.localDetune_(val);
		};
		indexBox !? { indexBox.string_("-") };
	}

		// have checked already
	polyBus_ { |inBus, inIndex|

		keyBus = inBus;
		indexToSet = inIndex;
		keyBus.values.do { |val, i|
			var edi = editors[i];
			edi.sendFunc_({ |edival|
			//	"VGSpecEditor: sending single index-value for a poly bus".postln;
				keyBus.setByIndex(indexToSet, VGTuning.busKeys[i], edival)
			});
			edi.localDetune_(val[indexToSet]);
		};
		indexBox !? { indexBox.value = indexToSet };
	}

	indexToSet_ { |val|
		if (keyBus.notNil and: { keyBus.isPoly }) { this.setBus(keyBus, val) };
	//	try { this.setBaseIndex(indexToSet, rename: false) };
	}

	name_ { |inname, prefix|
		var butName = if (prefix.notNil) {( prefix + inname); } { inname };
		name = inname;
		if (nameBut.notNil) { nameBut.states_([[butName]]).refresh };
	}

	setRef { |...keysVals|
		keysVals.pairsDo { |key, val|
			editors[VGTuning.busKeys.indexOf(key)].refVal_(val);
		}
	}

	setBase { |...keysVals|
		keysVals.pairsDo { |key, val|
		//	[key, val].postcs;
			editors[VGTuning.busKeys.indexOf(key)].baseVal_(val);
		}
	}

	visible_ { |flag| comp.visible_(flag) }

	visible { ^comp.visible }

	setRefByIndex { |index|
		var basevals = mulTree.baseValues.flop;
		var refvals = basevals[index];
		this.setRef(*[VGTuning.busKeys, refvals].flop.flat);
	}

	setBaseByIndex { |index, name|
		var basevals = mulTree.baseValues.flop;
		var refvals = basevals[index];
		if (name.notNil) { this.name = (name).asSymbol };
		this.setBase(*[VGTuning.busKeys, refvals].flop.flat);
	}

	updateExtDetune {
		var extDetuneVals;
		if (mulTree.notNil and: { mulTree.parent.notNil }) {
			extDetuneVals = mulTree.calcExtDetune;
			editors.do { |ed, i| ed.extDetune_(extDetuneVals[i]) };
		};
	}


}
