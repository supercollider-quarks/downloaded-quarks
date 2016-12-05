
+ SequenceableCollection {

	addGonganPhases { arg initialEvent;
		var phase = initialEvent.eventAt(\gonganPhase);
		var gonganDur = initialEvent.eventAt(\gonganDur);
		var gonganName = initialEvent.eventAt(\gongan);
		var gonganCount = initialEvent.eventAt(\gonganCount);

		if(gonganDur.notNil and: { phase.notNil }) {
			this.do { |event|
				event.use {
					~gonganPhase = phase;
					~sabet = phase + 1; // "beat"
					~gonganDur = gonganDur;
					~gongan = gonganName;
					~gonganCount = ~gonganCount ? gonganCount;
					phase = phase + (~dur ? 1.0) % gonganDur;
					if(phase == 0 and: { gonganCount.notNil }) {
						gonganCount = gonganCount + 1
					};
				}
			}
		} {
			"could not add gongan phase".warn;
			this.postln;
		}
	}
	// used in string converter

	selectPolyEvents { arg events;
		var isOpen = false, res = [], curr;
		this.do { |event|
			if(event.at(\polyphon) == \open) {
				if(isOpen) { Error("no closing bracket found").throw };
				isOpen = true;
			} {
				if(event.at(\polyphon) == \close) {
					if(isOpen.not) { Error("no opening bracket found").throw };
					isOpen = false;
					res = res.add(curr);
				};
			};
			if(isOpen) { curr = curr.add(event) };
		};
		if(isOpen) { Error("left polyphonic context open").throw };
		if(res.size > 0) { "number of polyphonic events: %\n".postvg(res.size) };
		^res
	}
}