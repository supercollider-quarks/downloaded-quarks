VGScaleRetune {
		var <w;
		var <>laras, <>refKey, <>refKeyFreq, <>scaleKeys, <>refScaleIntervals, <>refScaleFreqs, <>intervalsBetweenScale,
			trips, compSCT, compSCTWidth, unit, boxwidth, newScaleFreqs, newScaleIntervals, newScaleIntervalsBS, newFqNumBoxes, newIntervalBoxes;

	*new { |w, laras|
		^super.new.init(w, laras);
	}

	init { |parent, inLaras|
		w = parent ?? {�Window("single scale-key tuning", Rect(100, 100, 1200, 300)).front; };
		laras = inLaras ? VGScaleGraph.scale;

		compSCTWidth = 1200;

		/////////////////////SCALE TUNING//////////////////////////////////////

		compSCT = CompositeView(w, Rect(0 ,0, compSCTWidth, 300));
		compSCT.decorator = FlowLayout(compSCT.bounds);



			// get RefFreqs and convert them

		refKey = VGScale.refNoteKey;
		refKeyFreq = [refKey, VGScale.refScales[laras][refKey]];

		scaleKeys = VGScale.refScaleKeys(laras);
		refScaleIntervals = (scaleKeys.collect (VGScale. getRefDegreeInterval(laras, _)) * 100).round(0.001);
		refScaleFreqs = (scaleKeys.collect (VGScale. getRefDegreeFreq(laras, _))).round(0.001);
		intervalsBetweenScale = refScaleIntervals.differentiate.drop(1);


		newScaleFreqs =(scaleKeys + (VGScale.refOct * 10)).collect {|key| VGScale.workScales[laras][key.asSymbol]};
		newScaleIntervals = ((newScaleFreqs / refKeyFreq[1]).ratiomidi * 100).round(0.001);
		newScaleIntervalsBS = newScaleIntervals.differentiate.drop(1);

		unit = compSCTWidth / (scaleKeys.size +2.5);
		boxwidth = 100;


			// top lines is fixed, tuning of gamKUG
		this.drawTopRefLine;

		this.drawEditingZone;

	}

	updateEditingZone { |box, i|


		newFqNumBoxes[i].string = ((refKeyFreq[1].cpsmidi + (box.value * 0.01)).midicps.round(0.001) ).asString ++" Hz";
		newScaleFreqs[i] = (refKeyFreq[1].cpsmidi + (box.value * 0.01)).midicps;

		newScaleIntervalsBS = (newScaleFreqs.cpsmidi.differentiate.drop(1) * 100);

		newScaleIntervalsBS. do {|newInt, index|
				newIntervalBoxes[index].string =  newInt.round(0.001).asString ++ " Cent";
		};


	}

	/*recalcGuiState {


		refKey = VGScale.refNoteKey;
		refKeyFreq = [refKey, VGScale.refScales[laras][refKey]];

		scaleKeys = VGScale.refScaleKeys(laras);
		refScaleIntervals = (scaleKeys.collect (VGScale. getRefDegreeInterval(laras, _)) * 100).round(0.001);
		refScaleFreqs = (scaleKeys.collect (VGScale. getRefDegreeFreq(laras, _))).round(0.001);
		intervalsBetweenScale = refScaleIntervals.differentiate.drop(1);


		newScaleFreqs =(scaleKeys + (VGScale.refOct * 10)).collect {|key| VGScale.workScales[laras][key.asSymbol]};
		newScaleIntervals = ((newScaleFreqs / refKeyFreq[1]).ratiomidi * 100).round(0.001);
		newScaleIntervalsBS = newScaleIntervals.differentiate.drop(1);


		/*VGScale.workScales[laras].keys.asArray.sort.do{|key, i|

			newFqNumBoxes[i].string = ((refKeyFreq[1].cpsmidi + (box.value * 0.01)).midicps.round(0.001) ).asString ++" Hz";
			newScaleFreqs[i] = (refKeyFreq[1].cpsmidi + (box.value * 0.01)).midicps;

			newScaleIntervalsBS = (newScaleFreqs.cpsmidi.differentiate.drop(1) * 100);

			newScaleIntervalsBS. do {|newInt, index|
				newIntervalBoxes[index].string =  newInt.round(0.001).asString ++ " Cent";
			};
		}*/


	}
*/



	drawEditingZone {

		newFqNumBoxes = (); newIntervalBoxes = ();

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
		            .string = " virtual working scale:";



		newScaleFreqs.collect {|freq, i|
			newFqNumBoxes[i] = StaticText(compSCT, Rect(0, 0, boxwidth + 26, 16))
					.stringColor_(Color.grey)
					.string = freq.round(0.001).asString ++" Hz";
			compSCT.decorator.shift(unit - (boxwidth + 26) -4 , 0);
		};

		compSCT.decorator.nextLine;
		compSCT.decorator.nextLine.shift(unit *0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
					.string = " intervals to ref note key:";


		//set new scale with key tuning. tune scale keyall instruments  at once. e.g. pelog 3 or slendro 2
		newScaleIntervals.collect { |interval, i|
			NumberBox(compSCT, Rect(0,0, boxwidth -30, 20))
				.value_(interval.round(0.001))
				.decimals_(3)
			    .action_({ |box|

						// this writes into VGScale.workScales
					VGScale.setStepCent(*[laras, scaleKeys[i], box.value]);

						// this writes into MulTrees - temp only!!!
						// workScales / fullScales should go into mulTrees eventually.
					VGScale. setDegreeCent(*[laras, scaleKeys[i], box.value]);

					this.updateEditingZone(box, i);
					VGScale.calcFullScale(laras);
				});
			StaticText(compSCT, Rect(0, 0, 30, 14)).stringColor_(Color.grey).string = "Cent";
			compSCT.decorator.shift(unit - (boxwidth) -8 , 0);
		};

		//reset to original vgg scale
		/*Button(compSCT, Rect(10,0, 125,16))
				.states_([["reset to VGG scale", Color.black, Color.grey(0.7)]])
				.action_({

						VGScale.workScales[laras] = VGScale.refScales[laras].copy;
						VGScale.refScaleKeys(laras).do{|key, i| VGTuning.retuneDegree(laras, key, 1.0)};
						VGScale.calcFullScale(laras);
						//ugly ?!?
						{ try {
								(GUI.window.allWindows.detect{|win| win.name == "Scale Graph"}).close;
								{ VGScaleGraph.new }.defer(0.1);
							  };
						}.defer(0.1);

				});
*/

		compSCT.decorator.nextLine.shift(unit * 0.75 + (boxwidth * 2), 0);

		//just for orientation +   =
		intervalsBetweenScale.size.do{|i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 20, 20))
					.stringColor_(Color.grey)
					.string = " |_________| ";
			compSCT.decorator.shift(unit - (boxwidth + 20) -4 , 0);
			};

		//compSCT.decorator.shift(10,0);

		compSCT.decorator.nextLine.shift(unit * 0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
					.string = " intervals of virtual scale:";

		compSCT.decorator.shift(unit * 0.5, 0);

		//show intervals between reference scale note keys in cent
		newScaleIntervalsBS.collect{|interval, i|
			newIntervalBoxes[i] = StaticText(compSCT, Rect(0, 0, boxwidth + 30, 20))
				.stringColor_(Color.grey)
				.string = interval.round(0.001).asString ++ " Cent";
			compSCT.decorator.shift(unit - (boxwidth + 30) -4 , 0);
		};

		compSCT.decorator.nextLine.shift(unit * 0.25, 5);

		Button(compSCT, Rect(0, 0, 160, 25))
			.states_([["save workscale"]])
			.action_({
				Dialog.savePanel { |path|
							var file = File.new(path, "w");
							var dict = VGScale.workScales;
							protect {
								file.write(dict.asCompileString)
							} {
								file.close;
							}
						}
			});
		Button(compSCT, Rect(0, 0, 160, 25))
		    .states_([["load workscale"]])
			.action_({Dialog.getPaths {|paths|
		                var events = paths[0].load;
					    if(events.notNil) {
							VGScale.workScales.keys.do{|laras|

						           var ev = events[laras];
						           ev.keysValuesDo {|key, fq| VGScale.workScales[laras][key] = fq; };

						           VGScale.calcFullScale(\slendro);
						           VGScale.calcFullScale(\pelog);

						//ugly ?!?
						{ try {
							var newWin, currWin = w, currBounds = w.bounds;
							(GUI.window.allWindows.detect{|win| win.name == currWin.name}).close;
							{
								newWin = Window("single scale-key tuning", currBounds);
								newWin.front;
								VGScaleRetune(newWin);

							}.defer(0.1);
							};
						}.defer(0.1);

			                       };
							} {
								"workscale - File load error".warn;
							};

						}
			});

		Button(compSCT, Rect(0,0, 160,25))
				.states_([["reset to VGG scale"]])
				.action_({

						VGScale.workScales[laras] = VGScale.refScales[laras].copy;
						VGScale.refScaleKeys(laras).do{|key, i| VGTuning.retuneDegree(laras, key, 1.0)};
						VGScale.calcFullScale(laras);
						//ugly ?!?
						{ try {
								var newWin, currWin = w, currBounds = w.bounds;
				                (GUI.window.allWindows.detect{|win| win.name == currWin.name}).close;
								{
					            newWin = Window("single scale-key tuning", currBounds);
			                    newWin.front;
                                VGScaleRetune(newWin);

				                }.defer(0.1);
							  };
						}.defer(0.1);

				});

	}

	drawTopRefLine {
		////head line
//		compSCT.decorator.shift(10, 0);
//		StaticText(compSCT, Rect(0, 0, 750, 16)).string =
//				" reference scale: gamelan graz  " ++ laras.asString ++
//				"       reference key is: " ++ refKey.asString ++
//				"      ref. freq is: " ++ refKeyFreq[1].asString ++ " Hz";
//		compSCT.decorator.nextLine.shift(unit *0.5 +10, 4);

		compSCT.decorator.shift(unit *0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
					.string = " scale key:";

		compSCT.decorator.shift(unit *0.25, 0);

		//show scale keys
		scaleKeys.collect{|key, i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 16, 20))
					.stringColor_(Color.grey)
					.string =  key.asString;
			compSCT.decorator.shift(unit - (boxwidth + 16) -4 , 0);
		};

		compSCT.decorator.nextLine.shift(0,15);
		compSCT.decorator.nextLine.shift(unit *0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
		            .string = " reference scale (Graz):";

		//show ref scal in Hz
		refScaleFreqs.collect{|freq, i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 26, 20))
					.stringColor_(Color.grey)
					.string = freq.round(0.001).asString ++" Hz";
			compSCT.decorator.shift(unit - (boxwidth + 26) -4 , 0);
		};

		compSCT.decorator.nextLine.shift(unit *0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
					.string = " intervals to ref note key:";

		//show intervals to reference note key in cent
		refScaleIntervals.collect{|interval, i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 40, 20))
					.stringColor_(Color.grey)
					.string = interval.round(0.001).asString ++ " Cent";
			compSCT.decorator.shift(unit - (boxwidth + 40) -4 , 0);
		};
		compSCT.decorator.nextLine.shift(unit * 0.75 + (boxwidth * 2), 0);


		//just for orientation
		intervalsBetweenScale.size.do{|i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 40, 20))
					.stringColor_(Color.grey)
					.string = " |_________|  ";
			compSCT.decorator.shift(unit - (boxwidth + 40) -4 , 0);
			};

		compSCT.decorator.nextLine.shift(unit * 0.25, 0);

		StaticText(compSCT, Rect(0, 0, boxwidth * 2, 16))
		            .stringColor_(Color.grey)
					.string = " intervals of reference scale:";

		compSCT.decorator.shift(unit * 0.5, 0);

		//show intervals between reference scale note keys in cent
		intervalsBetweenScale.collect{|interval, i|
			StaticText(compSCT, Rect(0, 0, boxwidth + 30, 20))
				.stringColor_(Color.grey)
				.string = interval.round(0.001).asString ++ " Cent";
			compSCT.decorator.shift(unit - (boxwidth + 30) -4 , 0);
		};

		compSCT.decorator.nextLine.shift(0,10);
		compSCT.decorator.nextLine.shift(unit *0.25, 5);

	}

		/////////////////////////////////////////////////////////////////////////
}