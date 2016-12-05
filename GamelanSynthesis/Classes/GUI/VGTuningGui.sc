VGTuningGui {
	var <currLaras, <currInstKey, <currNoteKey, <currDepth = 0, <currPartialIndex = 0;
	var <currNode, <currTopName, <currBranchNames;

	var <currShowIndex=0;

	var <w, <topSlider, <tunePop, <topEditor, <specEditors, <scrolly, <noteDocument;
	var <playKeys = "QKJHGFDSA";

	*new { |laras = \pelog, instrument = \SaronA, note = \56, depth = 0|
		^super.new.init(laras, instrument, note, depth);
	}

	defaultRefs {
		^[\freq, VGScale.refScales[currLaras][VGScale.refNoteKey], \ringtime, 2.0, \amp, 0.2, \attack, 0.01];
	}

	allSpecs { ^[currLaras, currInstKey, currNoteKey, currDepth, currPartialIndex] }


	getNodeFor { |laras, instKey, noteKey, depth|
		var tuningTree;

		laras = laras ? currLaras;
		instKey = instKey ? currInstKey;
		noteKey = noteKey ? currNoteKey;
		depth = (depth ? currDepth).clip(0, 2);

		tuningTree = VGTuning.mulTrees[laras];

		if(tuningTree.isNil) {
			"VGTuningGui: no tuning called %.\n\n".postf(laras);
			^nil
		};

		^tuningTree.nodeAt(*[instKey, noteKey].keep(depth));
	}

	getRefFor {  |laras, instKey, noteKey, depth|
		var node = this.getNodeFor(laras, instKey, noteKey, depth);
		if (depth == 1) {
			^node.baseValues.flop[node.metaData.refIndex];
		};
			// what should this be???
//		if (depth == 1) {
//			^node.baseValues.flop[node.metaData.refIndex];
//		};
	}

	navigateTo { |laras, instKey, noteKey, depth=0, startIndex=0|

		var newTopNode, newTopName, branches, branchKeys, showPartials = false;

		newTopNode = this.getNodeFor(laras, instKey, noteKey, depth, startIndex);

		if (newTopNode.isNil) {
			"VGTuningGui: no tuning node found for: % - so trying to move up.\n"
				.postf([laras, instKey, noteKey, depth, startIndex]);
			if (depth > 0) {
				^this.navigateTo(laras, instKey, noteKey, depth - 1, startIndex);
			} {
				"VGTuningGui: no change for keys: %.\n"
					.postf([laras, instKey, noteKey, depth, startIndex]);
				^this
			}
		};

			// should do safety checking on this
		currShowIndex = startIndex;

		currLaras = laras ? currLaras;
		currInstKey = instKey ? currInstKey;
		currNoteKey = noteKey ? currNoteKey;
		currDepth = depth ? currDepth;
		currNode = newTopNode;
		currTopName = [currLaras, currInstKey, currNoteKey][depth];

	//	"navigateTo : laras: % instKey: % noteKey: % depth: %\n".postf(currLaras, currInstKey, currNoteKey, currDepth);

		if (currDepth == 2) {
			"VGTuningGui: node has % partials. Showing leaves.\n\n".postf(currNode.relValues.first.size);
			this.showLeaves;
		} {
			this.showBranches;
		};

		this.updateViews(currShowIndex);
	}

	showBranches {
		var branchKeys = currNode.branches.keys.asArray.sort;
		if (currDepth == 0) { branchKeys = VGNames.instNames.drop(1) };
			// high notes or partials go up;
		if (branchKeys.first.asString.first.isDecDigit) { branchKeys = branchKeys.reverse };
		currBranchNames = branchKeys;

	}

	showLeaves { // test only.
		var numPartials = currNode.relValues.first.size;
		currBranchNames = (numPartials - 1 .. 0).collect(_.asSymbol);
	//	[\numPartials, numPartials, \currBranchNames, currBranchNames].postcs;
	}

	updateViews { |startIndex=0, waittime = 0.1|

			var currScale = VGScale.fullScales[currLaras];
			var topRefFreq, topBaseFreq, refNoteKey;
			var notenames, partialIndex, polyNode;

			var popNames = [currLaras,
				VGNames.instNameScGraph(currInstKey),
				"note" + (currNoteKey ? "-")
			];

//			"VGTuningGui:updateViews : % --- now at item: '%' startIndex: %.\n\n".postf(this.allSpecs,
//				([currLaras, currInstKey, currNoteKey]).at(currDepth), startIndex);

			tunePop.value_([\pelog, \slendro].indexOf(currLaras));
			topEditor.popup.items_(popNames).value_(currDepth);
				// prefix for notes only
			topEditor.setMulTree(currNode).name_(currTopName);

				// cosmetics
			topEditor.indexBox.visible_(false);

			if (currDepth == 0) { // instruments
				topRefFreq = currScale[VGScale.refNoteKey];
				topBaseFreq = topRefFreq;
			};

			if (currDepth == 1) { // notes of one instrument

				notenames = currNode.branches.keys.asArray.sort.reverse;
				refNoteKey = notenames.detect { |key| key.asString.endsWith("6") } ? notenames.first;
				partialIndex = currNode.branches[refNoteKey].metaData[\reference];

				topRefFreq = currScale[refNoteKey];
				topBaseFreq = currNode.branches[refNoteKey].baseValues.first[partialIndex];
			};

			if (currDepth == 2) { // partials of one note
				topRefFreq = currScale[currNoteKey];
				partialIndex = currNode.metaData[\reference];
				topBaseFreq = currNode.baseValues.first[partialIndex];

				polyNode = currNode;
			};

			topEditor.setRef(\freq, topRefFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);
			topEditor.setBase(\freq, topBaseFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);

			topEditor.updateExtDetune;

			specEditors.do { |editor, i|
				var newName = currBranchNames[startIndex + i];
				var nodeToShow = polyNode ?? { currNode.branches[newName] };
				var setIndex = currBranchNames.size - 1 - startIndex - i;

				var refFreq, baseFreq;

								 	// newName isNil when out of partials
				if (nodeToShow.notNil and: newName.notNil) {
					editor.name_(newName);
					editor.visible_(true);

				//	[\VGTuningGui, nodeToShow.values].postcs;

					if (currDepth == 0) { // instruments
						refFreq = topRefFreq;

						notenames = nodeToShow.branches.keys.asArray.sort.reverse;
						refNoteKey = notenames.detect { |key| key.asString.endsWith("6") } ? notenames.first;
						baseFreq = currScale[refNoteKey];

						editor.indexToSet_(nil);
						editor.setMulTree(nodeToShow);

					    /*
					    editor.setRef(\freq, topRefFreq, \ringtime, 5, \amp, 0.1, \attack, 0.01);
						editor.setBase(\freq, baseFreq, \ringtime, 5, \amp, 0.1, \attack, 0.01);
					    why was ringtime here at 5? -> set to 1
					    */

					    editor.setRef(\freq, topRefFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);
						editor.setBase(\freq, baseFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);

						editor.nameBut.states_([[VGNames.instNameScGraph(newName)]]);
					};

					if (currDepth == 1) { // notes of one instrument
						refFreq = currScale[newName];
						partialIndex = nodeToShow.metaData[\reference];
						baseFreq = nodeToShow.baseValues.first[partialIndex];
						editor.indexToSet_(nil);
						editor.setMulTree(nodeToShow);
						editor.setRef(\freq, topRefFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);
						editor.setBase(\freq, baseFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);

						editor.name_(newName, prefix: "note");
					};


					if (currDepth == 2) { //
						refFreq = currScale[currNoteKey];

						editor.setMulTree(*[nodeToShow, false, setIndex]);
						editor.setBaseByIndex(setIndex, currBranchNames[i]);
						editor.setRef(\freq, topRefFreq, \ringtime, 1, \amp, 0.1, \attack, 0.01);

						editor.name_(newName, prefix: "partial");
					};

					editor.updateExtDetune;
				} {
				//	"no node at index %.\n\n".postln;
					editor.visible_(false);
				};
			};

			scrolly.value_(startIndex).numItems_(currBranchNames.size);
			scrolly.visible_(scrolly.numItems > scrolly.maxItems);

			VGScaleGraph.refresh;

	}

	init { |argLaras, argInstKey, argNoteKey, depth|

		var bigfont, midfont, smallfont;
		var buttonComp, topButComp, editsComp, gap = 2@2;
		var numEditors = 8;
		var prevScrollIndex = 0;
			// init only

		//skipjack for updates test
		var skippy;

		#bigfont, midfont, smallfont = [16, 14, 12].collect(Font("Helvetica", _));

		w = Window("Tuning Editor", Rect(300,20, 1120, 755), false);
		w.view.decorator = FlowLayout(w.bounds.moveTo(2, 2));

		w.view.keyDownAction = { |topview, char|
			var playCharIndex = playKeys.indexOf(char.toUpper);
			var whichEdit;
			if (playCharIndex.notNil) {
				whichEdit = ([topEditor] ++ specEditors)[playCharIndex];
				whichEdit.funcBut.doAction;
			}
		};

		currDepth = depth;
		currLaras = argLaras;
		currInstKey = argInstKey;
		currNoteKey = argNoteKey;

			// topLine
		tunePop = PopUpMenu(w, Rect(0, 0, 120, 40))
			.items_([\pelog, \slendro])
			.font_(bigfont)
			.action_ { |pop|
				this.navigateTo(pop.items[pop.value])
			};

		w.view.decorator.shift(30, 0);

		Button(w, Rect(0, 0, 80, 40))
			.states_([["Take Notes"]])
			.action_({
				var name = [currInstKey, currNoteKey].asString;
				var note = (currNode.metaData[\notes] ?? {�"Notes for" + name + ":\n\n" });
				noteDocument = Document(name, note)
					.promptToSave_(false)
					.onClose_({
						currNode.metaData[\notes] = noteDocument.string;
					});
			});

		Button(w, Rect(0, 0, 60, 40))
			.states_([["Save"]])
			.action_({
				Dialog.savePanel { |path|
							var file = File.new(path, "w");
							var dicts = VGTuning.mulTrees.collect(_.asDict);
							protect {
								file.write(dicts.asCompileString)
							} {
								file.close;
							}
						}
			});
		Button(w, Rect(0, 0, 60, 40))
			.states_([["Load"]])
			.action_({
				Dialog.getPaths { |paths|
							var dicts = paths[0].load;
							if(dicts.notNil) {
								VGTuning.mulTrees.keysValuesDo { |key, tree|
									var dict = dicts[key]; // only use if one exists
									dict !? { tree.setDict(dicts[key]) }; 								};
								this.updateViews;
							} {
								"VGTuningGui - File load error".warn;
							};

						}
			});

		Button(w, Rect(0, 0, 60, 40))
			.states_([["Edit \nSound"]])
			.action_({
				if (w.bounds.width > 800) {
					w.bounds_(w.bounds.setExtent(440, 755));
				} {
					w.bounds_(w.bounds.setExtent(1110, 755));
				}
			});
		/*

		topSlider = Slider(w, Rect(0,0,860,30))
			.action_ { "will do fine tuning - not active yet!".postln };
		*/
			// labels
		w.view.decorator.nextLine;

		([""] ++ VGTuning.busKeys).collect { |label, i|
			StaticText(w, Rect(0, 0, #[130, 300, 180, 300, 170][i], 30))
				.string_(label)
				.font_(bigfont);
		};


		editsComp = CompositeView(w, Rect(0,0, 970 + 130, 745));
		editsComp.decorator = FlowLayout(editsComp.bounds, gap, gap);

		editsComp.decorator.shift(8, 0); // offset to align visually with lower editors
			///////////
		topEditor = VGSpecEditor.new(nil,
			parent: editsComp, top: true, slide: true,
			buttons: true, pop: true, buttonsWidth: 120);
		topEditor.name_('___');
		topEditor.nameBut.font_(midfont);
		topEditor.nameBut.action_({
			"VGTuningGui: what should happen here in topEditor?".postln;
		});
		topEditor.indexBox.enabled_(false).scroll_(false);

		topEditor.popup.font_(midfont)
			.items_([currLaras, currInstKey, currNoteKey])
			.action_({ |pop|
					// get rid of "note" prefix:
				var navigItems = pop.items
					.put(1, VGNames.instNameByGraphName(pop.items[1]))
					.put(2, pop.items.last.keep(-2).asSymbol);
				this.navigateTo(*(navigItems ++ pop.value.asInteger));
			});

		topEditor.funcBut.states_([["play - Q"]]);
		topEditor.playFunc_ {
			var synthDef;
			var event = (laras: currLaras).putPairs(
				[[\instKey, currInstKey],
				[\noteKey, currNoteKey]]
					.keep((currDepth)).flat
			);
			if (currDepth == 2) { synthDef = \vgAddiAr };
			VGSound.play(currLaras, synthDef, event.postcs);
		};

		editsComp.decorator.nextLine.shift(0, 8);

		currBranchNames = (1..20).collect(_.asSymbol);

		scrolly = EZScroller(editsComp, Rect(100, 0, 16, 550), numEditors, currBranchNames.size,
			{ |sc|
				var startIndex = sc.value.max(0).asInteger;
				if(startIndex != prevScrollIndex and: { sc.numItems >= sc.maxItems }) {
					currShowIndex = startIndex;
					this.updateViews(startIndex);
					prevScrollIndex = startIndex;
			};
		});
				// make space for EZScroller.
		editsComp.decorator.bounds.left_(26);
		editsComp.decorator.nextLine.shift(0, -550);

		specEditors = currBranchNames.keep(numEditors).collect { |label, i|

			var edi = VGSpecEditor.new(parent: editsComp, slide: true, top: false, buttons: true);
				edi.name_(label);
				edi.nameFunc_({ |btn|
					var nodekey = edi.name;
					if (currDepth == 0) {  currInstKey = nodekey };
					if (currDepth == 1) { currNoteKey = nodekey };
					if (currDepth == 2) { currPartialIndex = nodekey.asInteger };

					this.navigateTo(depth: (currDepth + 1).min(2));
				});
				edi.funcBut.states_([["play -" + playKeys.drop(1)[i]]]);

					// one less visual distraction �
				edi.indexBox.visible_(false);

				edi.playFunc_ {
				 	var myName = edi.name;  // WISIWYG!
				 	var event = (), myPartial;

				 	if (currDepth == 0) {
				 		currInstKey = myName;
				 		event.put(\instKey, currInstKey);
				 		VGSound.play(currLaras, event: event);
				 	};
				 	if (currDepth == 1) {
				 		currNoteKey = myName;
				 		event.putPairs([\instKey, currInstKey,
				 			\noteKey, currNoteKey]);
				 		VGSound.play(currLaras, event: event);
				 	};

				 	if (currDepth == 2) {
				 		myPartial = myName.asString.split($_).last.asInteger;

						event.putPairs([
							\flattenLevel, 0.5,
							\laras, currLaras,
							\instKey, currInstKey,
							\noteKey, currNoteKey,
							\partIndex, myPartial,
							\synthDef, \vgPartial
						]);
						event = VGSound.eventFor(event.postcs);
						event.putAll((\instrument: \vgPartial)).play
					};

				};
		};
		w.bounds_(w.bounds.setExtent(440, 755)).front;
		this.navigateTo(currLaras, currInstKey, currNoteKey, currDepth, currShowIndex);
	//	this.updateViews;

	//skipjack for updates test
	skippy = SkipJack({ this.updateViews(currShowIndex) }, 0.2, name: \tuningGui, stopTest: { w.isClosed });

	}
}
