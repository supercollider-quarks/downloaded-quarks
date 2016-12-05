
AbstractEventMatch : AbstractFunction {
	classvar <>debug = false; 
	matchAttributes { arg event;
		if (debug.value(event)) { 
			"AbstractEventMatch:matchAttributes : ".post; event.postcs;
		};
		^this.value(event)
	}
	
	keySize { ^1 }

}

GonganMatch : AbstractEventMatch {
}


InNthGongan : GonganMatch {
	var <>n;
	*new { arg n;
		^super.new.n_(n)
	}
	value { arg event;
		^n == event.eventAt(\gonganCount)
	}
	storeArgs { ^[n] }
}

AfterNthGongan : GonganMatch {
	var <>n;
	*new { arg n;
		^super.new.n_(n)
	}
	value { arg event;
		var count = event.eventAt(\gonganCount);
		^count.notNil and: { count > n }
	}
	storeArgs { ^[n] }
}

BeforeNthGongan : GonganMatch {
	var <>n;
	*new { arg n;
		^super.new.n_(n)
	}
	value { arg event;
		var count = event.eventAt(\gonganCount);
		^count.notNil and: { count < n }
	}
	storeArgs { ^[n] }
}

AtGonganDivision : GonganMatch {
	var <>mod, <>offset;
	
	*new { arg mod, offset=0;
		^super.new.mod_(mod).offset_(offset);
	}
	value { arg event;
		var sabet = event.eventAt(\sabet);
		var gonganOffset = event.eventAt(\gonganOffset) ? 0.0;
		^sabet.notNil and: { sabet + gonganOffset % mod == offset }
	}
	storeArgs { ^[mod, offset] }
}

AtGonganFraction : GonganMatch {
	var <>frac;
	
	*new { arg frac;
		^super.new.frac_(frac);
	}
	value { arg event;
		var sabet = event.eventAt(\sabet);
		var dur = event.eventAt(\gonganDur);
		var gonganOffset = event.eventAt(\gonganOffset) ? 0.0;
		^sabet.notNil and: { dur.notNil } and: { sabet + gonganOffset == (dur * frac).round }
	}
	storeArgs { ^[frac] }
}



AtGonganPhase : GonganMatch {
	var <>phases;
	*new { arg phases;
		^super.new.phases_(phases.asArray.collect(_.asFloat).sort)
	}
	value { arg event;
		var phase = event.eventAt(\gonganPhase);
		var offset = event.eventAt(\gonganOffset) ? 0.0;
		var dur = event.eventAt(\gonganDur);
		^phase.notNil and: { dur.notNil } and: { 
			this.includesPhase(phase + offset, dur) 
		}
	}
	includesPhase { arg phase, gonganDur;
		var lastPhase = gonganDur - 1;
		phases.do { |x|
			if((x % lastPhase) == phase) { ^true }
		};
		^false
	}
	storeArgs { ^[phases] }
}

AtSabet : GonganMatch {
	var <>sabets;
	*new { arg sabets;
		^super.new.sabets_(sabets.asArray.collect(_.asFloat).sort)
	}
	value { arg event;
		var phase = event.eventAt(\gonganPhase);
		var offset = event.eventAt(\gonganOffset) ? 0.0;
		var dur = event.eventAt(\gonganDur);
		^phase.notNil and: { dur.notNil } and: { 
			this.includesPhase(phase + offset, dur) 
		}
	}
	includesPhase { arg phase, gonganDur;
		sabets.do { |sabet|
			if((sabet - 1 % gonganDur) == phase) { ^true }
		};
		^false
	}
	storeArgs { ^[sabets] }

}

BeforeGong : GonganMatch {
	var <>n;
	*new { arg n;
		^super.new.n_(n)
	}
	value { arg event;
		var dur = event.eventAt(\gonganDur);
		var sabet = event.eventAt(\sabet);
		var offset = event.eventAt(\gonganOffset) ? 0.0;		^dur.notNil and: { sabet.notNil } and: { sabet + offset >= (dur - n)  }
	}
	storeArgs { ^[n] }

}

AfterGong : GonganMatch {
	var <>n;
	*new { arg n;
		^super.new.n_(n)
	}
	value { arg event;
		var sabet = event.eventAt(\sabet);
		var offset = event.eventAt(\gonganOffset) ? 0.0;
		^sabet.notNil and: { sabet + offset >= n }
	}
	storeArgs { ^[n] }

}

MatchTag : AbstractEventMatch {
	var <>tag;
	*new { arg tag;
		^super.new.tag_(tag)
	}
	value { arg event;
		^event.eventAt(tag) == true
	}
	storeArgs { ^[tag] }
}


