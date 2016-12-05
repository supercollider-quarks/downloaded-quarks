VGScale { 
	classvar <>scales, <refOct = 5, <refNote = 6, <refInst = \SaronA; 
	
	*refNoteKey { ^(refOct.asString ++ refNote).asSymbol }
	
	*initClass { 
		VGScale.scales = ();
			// by ear/reading from slendro, SaronA, ref = \56. 
		VGScale.scales.put(
			\pelog, (\51: 610.61, \52: 662.49, \53: 710.85, \54: 827.21, 
				\55: 896.9, \56: 959.01, \57: 1062.01)
		); 
		
			// slendro: stretch between [46, 56] is +1.97 Hz, between [51, 61] is + 1.47 Hz...
		VGScale.scales.put(
			\slendro, (\46: 478.09, \51: 550.39, \52: 635.43, \53: 726.93, 
				\55: 835.39, \56: 958.15, \61: 1102.25)
		);
			// initialise once.
		this.calcFullScale(\slendro);
		this.calcFullScale(\pelog);
		
	}
	*calcFullScale { arg laras=\slendro, stretchCent=0, stretchHz=0;
	
//			"calculate scale for full ambitus by: 
//			*rootScale (from SaronA for now, later from Gender)
//			*stretchCent (cents/octave)
//			*stretchHz (Hz/octave).".postln;
		
		var myScale = VGScale.scales[laras];
		var octs = (1..7); 
		var notes = (pelog: [1,2,3,4,5,6,7], slendro: [1,2,3,5,6])[laras]; 
		var refScaleKeys = notes.collect({ |note| ("5" ++ note).asSymbol });
		var refScale = myScale.atAll (refScaleKeys); 

		octs.do { |oct| 
			notes.do { |note, i| 
				var noteSymbol, noteFreq, octOffset;
				noteSymbol = (oct.asString ++ note).asSymbol;
				
				octOffset = (oct - refOct);
				noteFreq = refScale[i] * (2 ** octOffset); 
				noteFreq = noteFreq * (stretchCent * 0.01 * octOffset).midiratio;
				noteFreq = noteFreq + (stretchHz * octOffset);

				// [noteSymbol, noteFreq].postcs;
				myScale.put(noteSymbol, noteFreq);
			};
		};		
	}
	
	*analyseData { 
		"find longest/strongest partials near the predicted root freq of each sonator, 
		assume this is rootFreq.".postln;
	}	
}

VGScaleGraph {
	classvar w, <>color;
	classvar <>scale = \pelog;
	
	*isOpen {
		^w.notNil and: { w.isClosed.not }
	}
	
	*refresh {
		if(this.isOpen) { w.refresh }
	}
	
	*initClass {
		color = (semitone: Color.blue(0.5), scale: Color.red, insts: Color.green);
	}
	
	*new {
		var drawLine, instNames, btns, showInst, tuningNode, skippy, comp;
		w = Window("Scale Graph", Rect(100, 0, 1300, 800)).front;
		w.view.background_(Color.black);

		instNames = VGTuning.mulTrees[scale].branches.keys.asArray.sort; 
		
		comp = CompositeView(w, Rect(0,0,80,800))
			.background_(Color.grey(0.1));
		comp.decorator = FlowLayout(comp.bounds); 
		
		PopUpMenu(comp, Rect(4, 4, 72, 30))
			.items_([\pelog, \slendro])
			.background_(Color.grey)
			.action_{ |pop| this.scale_(pop.items[pop.value]).refresh };
		
		comp.decorator.nextLine.shift(0, 20);
		
		EZNumber(comp, 72@45, \stretchCent, [-20, 20, \lin, 0.1], { |box| 
			VGScale.calcFullScale(VGScaleGraph.scale, stretchCent: box.value); 
		}, 0, true, 72, 72, layout: \line2).labelView.background_(Color.grey(0.7)).align_(0);

		EZNumber(comp, 72@45, \stretchHz, [-20, 20, \lin, 0.1], { |box| 
			VGScale.calcFullScale(VGScaleGraph.scale, stretchHz: box.value); 
		}, 0, true, 72, 72, layout: \line2).labelView.background_(Color.grey(0.7)).align_(0);
	
		comp.decorator.nextLine.shift(0, 20);

		btns = instNames.collect { |name, i| 
			var isRef = (name == VGScale.refInst); 
			Button(comp, Rect(4, i + 1.67 * 30, 72, 25))
				.states_([[name, Color.black, Color.grey(0.5, 0.5)], [name, Color.black, Color.grey]])
				.action_{ w.refresh }
				.value_(isRef.binaryValue);
		};	
		
		showInst = { |instKey| 
			try { 
			var instTree = tuningNode.branches[instKey];
			var noteTree = instTree.branches; 
			Pen.color = color.insts;
			
				instTree.branches.keys.asArray.sort.do { |noteKey|
					
					var index, node, freq, pitch, octave, midinote; 
					#index, node = VGSound.findRefPartialFor(scale, instKey, noteKey);
					freq = tuningNode.calcAtLeaf(instKey, noteKey).first[index];
	
					// freq = node.baseValues.flop[index].first;
					pitch = freq.cpsmidi; 
					pitch = pitch;		// shift reference for visual display
					octave = pitch div: 12 - 1; 
					midinote = pitch % 12; 
					
					drawLine.(midinote, octave);
				};	
			Pen.stroke;
			};
		};
		
		drawLine = { |pitch=0, octave=1, length=0.92, str| 
			var bounds = w.view.bounds;
			var unit = bounds.width / 13;
			var x = (pitch * unit); 
			var y1 = (unit * 7 - (octave - 1 * unit));
			var y2 = y1 - (length * unit);
			Pen.line(x @ y1, x @ y2);
			if (str.notNil) { Pen.stringAtPoint(str, x@y1 + (-3@4)) };
		};
		
		w.drawHook = {
			tuningNode = VGTuning.mulTrees[scale];
			Pen.translate(80, 50);
					// semitone grid
			Pen.color = color.semitone;
			Pen.width_(0.5);
			["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B", "C"].do { |str, i| 						drawLine.(i, 1, 7, str) 
			};
			Pen.stroke;
			
					// draw fullScale
			
			Pen.color = color.scale;
		
			VGScale.scales[scale].keys.asArray.sort.do { |key, i| 
				var oct, notename, pitch, str; 
				var octave, midinote;
				#oct, notename = key.asString.clump(1).collect(_.asInteger);
				pitch = VGScale.scales[scale][key].cpsmidi; 
				pitch = pitch - 0.5;		// shift reference for visual display
				octave = pitch div: 12; 
				midinote = pitch % 12 + 0.5; 
				[octave, midinote].round(0.001);
				if (octave == 2) { str = notename.asString };
				drawLine.(midinote, octave - 1, str: str);
			};
			Pen.stroke;
		
					// draw all instrument notes! 
			btns.do { |btn| 
				if (btn.value == 1) { 
					showInst.(btn.states.first.first.asSymbol);
				}
			};
		};
		w.refresh;
		
		skippy = SkipJack({ this.refresh }, 0.2, 
			name: \scaleGraph, stopTest: { this.isOpen.not }
		);
	}		
}