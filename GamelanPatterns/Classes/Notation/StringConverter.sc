
StringConverter {
	classvar >current;

	*current { ^current ? BalunganReader }
	*makeCurrent { current = this }

	*use { arg function;
		var result, saveEnvir;

		saveEnvir = current;
		currentEnvironment = this;
		protect {
			result = function.value;
		}{
			current = saveEnvir;
		};
		^result
	}

	*convert { arg str, addScore=true;
		^this.current.convert(str)
	}
	*unconvert { arg list;
		^this.current.unconvert(list)
	}
	*error { arg string, index;
		Error("conversion failed: " ++ string.insert(index, Char.bullet).quote).throw;
	}

}

// a very simple string converter

EventScoreReader : StringConverter {

	*addScoreToEvent { arg event, score;
		var resource = event.at(\resource);
		score = score.replace("[", "").replace("]", ""); // remove ugly brackets
		if(resource.isNil) { resource = (); event.put(\resource, resource) };
		resource.put(\score, score);
	}

	*getScoreFromEvent { arg event;
		^event.at(\resources).eventAt(\score) ?? { event.at(\resources).eventAt(\varname) }
	}

	*convert { arg str, addScore=true;
		var res, event;
		str.do { |char, i|
			var x = char.asString;
			if(char.isSpace.not) {
				if(x == this.tagAnythingGoes) {
					event = this.genericEvent
				} {
					if(x.every { |x| x.isDecDigit }) {
						event = (degree: x.asInteger);
					} {
						this.error(str, i)
					}
				};
				if(addScore) { this.addScoreToEvent(event, x) };
				res = res.add(event)
			}
		}
		^res
	}

	*unconvert { arg list;
		var res = "";
		list.do { |event|
			var score = this.getScoreFromEvent(event);
				if(score.notNil) {
					res = res ++ score
				} {
					if(this.isGeneric(event)) {
						res = res ++ this.tagAnythingGoes
					} {
						res = res ++ event[\degree]
					}
				}
		};
		^res

	}

	*isGeneric { arg event;
		^event == this.genericEvent
	}

	*tagAnythingGoes { ^"*" }
	*genericEvent {�^() }

}


BalunganReader : EventScoreReader {

	classvar <events; // lookup table for event generation
	classvar <acceptInsteadOfNote;
	classvar <>diacritica; // an array of arrays
	classvar <>reverseDiacritica; // dito

	*initClass {
		var rules, pairs, events, reverseRules;
		events = [
				"*" : { () }, // wildcard
				"?" : { (degree: '?', dur: 0.0) } // elongation
		];
		// remove them later..
		// accept variuable names
		// resource key is ignored by pattern matching.
		"abcdefghijklmnopqrstuvwxyz".do { |char|
			events = events.add(char.asString);
			events = events.add({
				().addResource(\varname, char.asSymbol)
			})
		};
		this.events = events;

		pairs = [ // basis ist immer 1.0 !
			"|", 1,  // * 1 (explicit dur of one.)
			"-", 2,	// * 1 + 1 ??
			"--", 3, // * 1 + 2
			"---", 4, // * 1 + 3
			"----", 5,  // * 1 + 4
			"-_", 1.5, // * 1 + 0.5
			"--_", 2.5, // * 1 + 1.5
			"---_", 3.5,
			"_-=", 0.75, // problem: < 1.0 with elongation at segment border
			"=", 0.25,
			"_-_", 1, // * 0.5 + 0.5
			"_-", 1.5,
			"=-=", 0.5,
			"_", 0.5

		];

		// important: sort them by string size:
		pairs = pairs.clump(2).sort { |x, y| x[0].size > y[0].size }; 		pairs.do { |x|
			var str = x[0], time = x[1];

			rules = rules.add(str).add({ |event|
				event.put(\dur, time);
			});
		};
		// for reverse translating, reverse sorting: short to long
		pairs.reverseDo { |x|
			var str = x[0], time = x[1];
				reverseRules = reverseRules.add(
					this.reverseTranslationFunction(\dur, time, str)
				)
		};
		reverseRules = reverseRules.addFirst(
			this.reverseTranslationFunction(\dur, 1.0, "") // default dur = 1.0
		);

		// out of each group (an array) one member is selected only.
		diacritica = [
			rules,
			[
				"'", { |event| event.put(\octave, 6) },
				"''", { |event| event.put(\octave, 7) },
				".", { |event| event.put(\octave, 4) },
				"..", { |event| event.put(\octave, 3) },
				":", { |event| event.put(\octave, 5) }
			],
			[
				"<", { |event| event.addResource(\context, \open) },
				">", { |event| event.addResource(\context, \close) }
			],
			[
				"[",  { |event| event.put(\polyphon, \open) },
				"]",  { |event| event.put(\polyphon, \close) },

			],
			[
				/* here more groups of rules can be inserted ..*/
			]
		];

		reverseDiacritica = [
			[
				this.reverseTranslationFunction(\octave, 6, "'"),
				this.reverseTranslationFunction(\octave, 4, ".")
			],
			reverseRules


		];

	}

	*reverseTranslationFunction { |key, value, str|
			^{ |event|
				if(event[key] == value) { str }
			}
	}

	*events_ { arg pairs;
		events = pairs;
		acceptInsteadOfNote = events[0,2..];
	}

	// find the event for a given string
	*getEvent { arg str;
		var res;
		events.pairsDo {|key, val|
			if(str == key) {
				^val.value
			};
		};
		^if(str.every(_.isDecDigit)) {
				(degree: str.asInteger, dur: 1.0)
		}
	}

	// apply a diacriticum (a string) to an event
	*applyDiacritica { arg str, event, index;
		diacritica.at(index).pairsDo {|key, val|
			if(str.find(key.value).notNil) {
				val.value(event);
				//"found diakritikum: %. event: % \n".postf( key, event );
				^this
			};
		};
	}

	*isDiakritikum { arg str;
		diacritica.do { arg each;
			each.pairsDo {|key, val|
				if(str.find(key).notNil) {
					^true
				}
			}
		};
		^false
	}

	*isSeparator { arg char;
		var str;
		if(char.isDecDigit) { ^true };
		str = char.asString;
		acceptInsteadOfNote.do { |key| if(key == str) { ^true } };
		^false
	}

	*removeDuplicateSpaces { arg str;
		^str.separate { |x,y| x.isSpace != y.isSpace }
		.collect { |x| if(x[0].isSpace) { " " } { x } }
		.join
	}

	// while we want to write opening brackets before what they bracket,
	// it is easier to convert them when we treat them like diacritica
	// here we move them after the first character they stand before

	*moveBracketsIntoSuffixPosition { arg str;
		var indicesOfPrefixBrackets, lastIndex;

		str = if(str.endsWith(">")) { str.drop(-1) } { str.copy };
		str = str.copy;
		indicesOfPrefixBrackets = str.findAll("[");
		indicesOfPrefixBrackets = indicesOfPrefixBrackets.addAll(str.findAll("<"));
		indicesOfPrefixBrackets = indicesOfPrefixBrackets.addAll(str.findAll(">"));
		lastIndex = str.size - 1;



		indicesOfPrefixBrackets.do { |index|
			if(index < lastIndex) { str.swap(index, index + 1) }
		};
		^str
	}


	*convert  { arg str, addScore=true, postDur=true;
		var list, pairs, totalDuration = 0, res, numPolyEvents;

		// empty string is valid, returns the empty array
		if(str == "") { ^[] };

		// prepare string
		str = this.removeDuplicateSpaces(str);
		str = this.moveBracketsIntoSuffixPosition(str);

		// beginning of score, if a degree note is not given,
		// interpret as elongation tone: "?"
		if(this.isDiakritikum(str[0].asString)) { str =  "?" ++ str };

		// separate the string into segments
		list = str.separate { |x, y|
				this.isSeparator(y)
		};
		pairs = list.collect { |each|
				each.separate { |x, y|
					this.isSeparator(x)
					or: { this.isSeparator(y) }
				}
		};

		// posting here returns the separated strings
		// pairs.postln;

		res = pairs.collect {|each, i|
			var event, numericalPart, extraPart;

			// a segment has a numerical part (usually the note number or the variable)
			// and a suffix (usually the duration)
			#numericalPart, extraPart = each;

			event = this.getEvent(numericalPart);

			if(event.isNil) {
				this.error(numericalPart.addAll(extraPart), numericalPart.size)
			};

			if(extraPart.notNil) {
				diacritica.size.do { |i|
					this.applyDiacritica(extraPart, event, i)
				}
			};

			// the score can be useful for debugging or display. This is the score
			// that generated the event which we return.
			if(addScore) {
				this.addScoreToEvent(event, list[i]);
			};

			// treat the special sign for elongation.
			if(event[\degree] == '?') {
				if(event[\dur] > 1.0) {
					event[\dur]  = event[\dur] - 1
				} {
					event.postln;
					Error("this rythmical succession (dur: %)
					currently not defined at gongan border.".format(event[\dur])).throw;
				}
			};

			// add the event duration to the total duration of all events
			totalDuration = totalDuration + (event[\dur] ? 1.0);
			event
		};

		// now find the events which have polyphony
		// NOTE:::perhaps we should calculate this down below, after the poly events.
		// needs testing, so  I won't touch it now!
		numPolyEvents = res.selectPolyEvents.flatten(1).size;
		totalDuration = totalDuration - numPolyEvents;
		if(postDur) { ("totalDuration: " + totalDuration).postvg };

		// parallelise polyphonic series
		res = Pevent(PvgFlop(Pseq(res)), ()).asStream.all;


		^res
	}


	*unconvert { arg eventList;
		var str = "";

		eventList.do { |event|
			str = str ++(event[\degree] ? "*");
			reverseDiacritica.do { |list|
				var match;
				list.detect { |func| match = func.value(event); match.notNil };
				match !? {
					str = str ++ match;
				};
			}
		};

		^str
	}
}

VGScoreReader : BalunganReader {}

