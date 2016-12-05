Ptafsiran : Pattern {

	var <>pattern, <rules, <>transparent, <>maxContextSize, <maxKeySize;
	var <>verbose = true;

	*new { arg pattern, rules, transparent = false, maxContextSize = 30;
		if(rules.isNil) { Error("need rules for Ptafisran!").throw };
		^super.new.pattern_(pattern).rules_(rules)
			.transparent_(transparent).maxContextSize_(maxContextSize)
	}

	rules_ { arg list;
		rules = list;
		maxKeySize = rules.findMaximalKeySize;
	}


	embedInStream { arg inval;
		var list, stream, outval, match;
		var rule, dropSize;
		var offset = 0;
		var stateDictionary = Event.new;

		stream = pattern.asStream;
		dropSize = maxKeySize;

		//"dropSize: %\n".postf(maxKeySize);

		list = Array.new(maxContextSize * 2);

		loop {

			// fill up the list
			dropSize.do {
				outval = stream.next(inval);
				outval !? { list = list.add(outval) };
				// overridden in subclass. does nothing here:
				inval = this.stepDropValue(outval, inval); 			};
			if(verbose) {
				postvg("list: %\ndrop size: %\n", list[offset..].vg_unconvert, dropSize);
			};
			// forget somethings
			if(list.size > (maxContextSize * 2)) {
			 	list = list.drop(maxContextSize);
			 	offset = offset - maxContextSize;
			};

			if(offset >= list.size) {
			 	nil.alwaysYield;
			 	^inval
			};

			// find and apply the rule to get a match
			stateDictionary.use {
				match = rules.deepFindRule(list, offset);
			};

			// embed the match in the stream
			// and get the increment for the event list
			#inval, dropSize = this.embedResultInStream(inval, match, list, offset, outval);

			match = nil;

			// drop the corresponding pattern part
			// list = list.drop(dropSize);
			offset = offset + dropSize;


		}
	}

	stepDropValue { arg outval, inval;
		^inval
	}


	// returns the inval and the drop size: [inval, dropSize]

	embedResultInStream { arg inval, match, list, offset=0, outval;
			// derive the eventarray from the match
			var transformedValue, dropSize;

			if(verbose) {
				"for current events:\n%\n\n".postvg([list[..offset-1], list[offset..]]
												.collect(_.vg_unconvert));
				"applying rule to string: %\n%\n".postvg(list[offset..].vg_unconvert, match)
			};

			if(match.isNil) {
				if(transparent) {
					dropSize = 1;
					if(verbose) { "no rule applied.\n\n".postvg };
					inval = list[offset].yield;
				} {
					"no match found for input pattern.".postvg;
					rules.rules.postvgcs;
					list[offset..].vg_unconvert.postvgcs;
					ListeningClock.stopAll; // halt everything.
					^nil.alwaysYield;
				}
			} {
				dropSize = match.keyDropSize;
			};

			transformedValue = this.getTransformedValue(inval, match, list, offset);
			// correct the gongan phases
			transformedValue.do { |event|
					outval = list[offset].copy.putAll(event); // easy way.
					inval = outval.yield;
					if(inval.isNil) {
						nil.alwaysYield;
					};
			};

			^[inval, dropSize]

	}

	getTransformedValue { arg inval, match, list, offset=0;
			var duration;
			var matchValue = match.transform(list, offset);

			if(matchValue.isNil) {
				 ^nil
			};

			duration  = match.keyDropDuration;

			if(duration.notNil) {
				matchValue = matchValue.copy.asArray
					.scaleEventDuration(duration)
					.addGonganPhases(list[offset]);

				if(verbose) { "scaling time: % (n=%) by duration: %\n"
					.postvg(matchValue.eventDuration, matchValue.size, duration)

				};
			} {
				postvg("no key drop duration avaliable: %\n", match);
			};

			^matchValue
	}


}



Preact : Ptafsiran {

	*new { arg pattern, rules;
		^super.new(pattern, rules, true)
	}

	stepDropValue { arg outval, inval;
		^outval.yield
	}

	embedResultInStream { arg inval, match, list, offset=0, outval;
			var dropSize;
			// instead of embedding the value, just evaluate the function in the value
			if(match.isNil) {
				dropSize = 1;
			} {
				match.value.value(list, offset, inval);
				dropSize = match.keyDropSize.max(1);
			};
			^[inval, dropSize];
	}


}





PtafsiranPar : Ptafsiran {
	var <parallel;

	embedInStream { arg inval;
		^parallel.embedInStream(inval)
	}

	rules_ { arg list;
		var allValues, i = 0, indexPattern, patternList;

		super.rules_(list);

		rules.rules.deepDoAssoc({|assoc|
			if(assoc.value.isSequenceableCollection.not and: {assoc.value.isKindOf(Pattern).not})
			{
			Error("PparTafsiran requires written out sequences or patterns as values !").throw;
			};

			allValues = allValues.add(assoc.value.asArray);
			assoc.value = [(index: i)];
		/*	assoc.value = { |list, offset|
				list[offset].copy.put(\index, i).postln;
				[(index: i)]
			};
		*/
			i = i + 1;

		}, false);

		patternList = allValues.collect(Pseq(_, inf));
		indexPattern = Prout({|inval| super.embedInStream(inval) }).collect { |x| x.at(\index) };
		parallel = PparSwitch(patternList, indexPattern);
	}

	getTransformedValue { arg inval, match, list, offset=0;
			^match.transform(list, offset).postvg;
	}
}

// takes a stream of events instead of indices
// used to embed the tafsiran input stream into the output pattern

PtafsiranParSwitch : PparSwitch {

	embedInStream { arg inval;

		var outval, patterns, stream, key, ended;
		var whichStr, whichEvent, index, delta = 0.0;

		key = UniqueID.next;
		patterns = list.collect { |pat, i|
			pat.collect { |event| event.put(key, i) }
		};

		stream = Ppar(patterns).asStream;
		whichStr = which.asStream;

		loop {
			whichEvent = whichStr.next(inval);
			outval = stream.next(inval);
			index = whichEvent.eventAt(\index);
			if(outval.isNil or: { index.isNil }) {
				nil.yield; ^inval
			};

			if(index != outval.at(key)) {
				if(accum) {
				delta = delta + outval.delta;
				if(delta > 1.0) { // return at least one event per accum period.
					inval = Event.silent(delta).yield;
				}
				} {
					inval = outval.put(\degree, \rest).yield;
				}
			} {
				if(delta > 0) {
					inval = Event.silent(delta).yield;
				};
				outval.put(key, nil);
				outval.putAll(whichEvent); // add the input choice event here
				delta = 0.0;
				inval = outval.yield;
			};
		}

	}
}