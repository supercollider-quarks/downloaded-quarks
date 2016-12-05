

PselectSegment : Psym {
	*new { arg pattern, dict;
		^super.new(pattern).dict_(dict)
	}
	lookUp { arg key;
		^dict.at(key).collect { |event| event.put(\segment, key); event };
	}
	
}



Psegment : Pattern {
	var <>axiom, <>rules, <>pattern;
	var <>verbose = false;
	
	*new { arg axiom, rules , pattern;
		^super.newCopyArgs(axiom ? \default, rules ? [() -> [\default -> \default]], pattern)
	}
	
	embedInStream { arg inval;
		var current = axiom;
		var outval, sigStream = pattern.asStream, currentSignal;
		var ruleStream = rules.asStream;
		"returned axiom".postvg;
		inval = current.yield; // return axiom once
		while {
			currentSignal = sigStream.next(inval ? ());
			currentSignal.postvg;
			current.notNil and: { currentSignal.notNil }
		} {
			current = this.nextSegment(current, currentSignal, ruleStream);
			inval = current.yield;
		};
		^inval
	}
	
	nextSegment { arg key, signalEvent, ruleStream;
		var which = this.findRule(key, signalEvent, ruleStream);
		if(verbose) { 
			postvg("Psegment: calling next segment with: key (%) signalEvent (%). found: %\n", 
				key, signalEvent, which) 
		};
		if(which.isNil) { 
			"Psegment found no matching rule for::\n%\n".postvg(signalEvent);
			^nil
		} {
			which.do { |assoc|
				if(assoc.key.matchAttributes(key)) {
					if(verbose) { "Psegment: %\n".postvg(assoc) };
					^assoc.value.value(signalEvent, key)
				}
			}
		
		}
		
	}
	
	findRule { arg key, signalEvent, ruleStream;
		ruleStream.next(signalEvent).do { |assoc|
			if(assoc.recognise(signalEvent)) {
				if(verbose) { "Psegment found matching set of rules for %\n".postvg(assoc.key) };
				^assoc.transform(signalEvent, key)
			}
		};
		^nil
	}

}
