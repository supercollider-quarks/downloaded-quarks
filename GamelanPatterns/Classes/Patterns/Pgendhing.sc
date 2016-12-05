Pgendhing : Pattern {

	var tagProxy;
	var pathetProxy;

	var segmentPattern;
	var balunganChoice;
	var pattern;


	*new { arg axiom, tag, segmentRules, balunganDict, pathet;
		^super.new.init(axiom, tag, segmentRules, balunganDict, pathet)
	}

	init { arg axiom, tag, segmentRules, balunganDict, pathet;

		tagProxy = PatternProxy(tag);
		pathetProxy = PatternProxy(pathet);

		segmentPattern = Psegment(axiom, segmentRules, tagProxy);
		balunganChoice = PselectSegment(segmentPattern, balunganDict);
		pattern = Ppathet(pathetProxy, balunganChoice);
		pattern = PremovePin(pattern);
	}

	embedInStream { arg inval;
		^pattern.embedInStream(inval)
	}

	// convenient getters and setters

	tag_ { arg event;
		tagProxy.source = event;
	}
	tag {
		^tagProxy.source
	}

	segmentRules_ { arg rules;
		segmentPattern.rules = rules;
	}

	segmentRules {
		^segmentPattern.rules
	}
	balunganDict_ { arg dict;
		balunganChoice.dict = dict;
	}

	balunganDict {
		^balunganChoice.dict
	}
	pathet_ { arg pathet;
		pathetProxy.source = pathet;
	}
	pathet {
		^pathetProxy.source
	}
}



Pbalungan : Pgendhing {}


PgendhingTime {

	*new { arg balungan, gonganName, gonganDuration, iphase=1.0;// gonganDuration = Formzahl

		// cut in pieces
		var pat = ProtoBalungan(balungan);

		// add gongan info
		pat = Pgongan(pat, gonganName, gonganDuration, iphase);
		pat = PgonganCount(pat);

		// add irama
		pat = Pirama(pat);

		^pat
	}


}


PaddTag : Pattern {
	var <>pattern, <>tag;

	*new { arg pattern, tag;
		^super.new.tag_(tag ? ()).pattern_(pattern ? ())
	}

	embedInStream { arg inval;
		var str = pattern.asStream, outval;
		while {
			outval = str.next(inval ? ());
			outval.notNil
		} {
			tag !? { outval.putAll(tag) };
			inval = outval.yield;
		}
		^inval
	}
}

// embeds references.

PaddTagRef : PaddTag {
	tag_ { arg event;
		if(tag.isNil) { tag = LazyEnvir.new.know_(true) } { tag.clear };
		event !? { tag.putAll(event) };
	}
}


// for instruments that are silent for a while in buka segment
// needs gongan info (from Pgongan)


PsilentInBuka : FilterPattern {
	var <>startSabet, <>checkForBuka;
	*new { arg pattern, startSabet, checkForBuka = true;
		^super.new(pattern).startSabet_(startSabet).checkForBuka_(checkForBuka)
	}
	embedInStream { arg inval;
		var outval, stream = pattern.asStream, sabet;
		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			//"sabet: %, segment: %\n".postf(outval.at(\sabet), outval.at(\segment));
			if(checkForBuka.not or: { outval.at(\segment) === \buka }) {
				sabet = outval.at(\sabet);
				if(sabet.notNil and: { sabet < startSabet }) {
					outval.put(\degree, \rest);
				};
			};
			inval = outval.yield;
		}
	}
}

Ppenunggu : Pattern {
	var <>pattern, <>tagPattern, <>conditionPattern;
	var <>verbose = false;

	*new { arg pattern, tagPattern, conditionPattern;
		^super.newCopyArgs(pattern, tagPattern, conditionPattern)
	}
	embedInStream { arg inval;
		var stream = pattern.asStream;
		var tagStream = tagPattern.asStream;
		var conditionStream = conditionPattern.asStream;
		var outval, tag = ();
		var prevCondition = false;
		var condition = conditionStream.next(inval) ? false;
		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			if(condition.matchAttributes(outval)) {
				tag = tagStream.next(inval) ? ();
				prevCondition = condition;
				condition = conditionStream.next(inval) ? false;
			};
			if(prevCondition.matchAttributes(outval)) {
				if(verbose) { "penunggu embedding tag: %\n".postf(tag) };
				outval.putAll(tag);
			};
			inval = outval.yield;
		};
		^inval
	}

}

