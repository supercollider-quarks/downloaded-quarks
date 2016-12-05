
// integrates polyphony.
// we collect all the voices together and flop them so that
// we get events with arrays as values.
// is used only in VGScoreReader to apply string conversion

PvgFlop : FilterPattern {
	var <>maxVoices = 8;

	embedInStream { arg inval;
		var outval, memory, stream, polyphon, insidePolyphon = false;
		var voiceCount = 0, closing;
		
		
		stream = pattern.asStream;
		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			polyphon = outval.at(\polyphon);
			if(polyphon === 'open') {
				if(insidePolyphon) { Error("cannot open polyphony when already open.").throw };
				insidePolyphon = true;
				outval = outval.copy;
				outval.put(\polyphon, nil);
			};
			closing = polyphon === 'close' or: {voiceCount >= maxVoices};
			if(closing) {
				if(insidePolyphon.not) { 
					Error("cannot close polyphony when already closed.").throw 
				};
				insidePolyphon = false;
				outval = outval.copy;
				outval.put(\polyphon, nil);
				memory = memory.add(outval); // elephant rein ..
				
				// giraffen wieder raus..
				
				if(memory.notNil) {
					memory.flopEvents;
					inval = memory.flopEvents.yield;
					memory = nil;
				};
				voiceCount = 0;
			} {
				if(insidePolyphon) {
					memory = memory.add(outval.copy); // elephanten rein ..
					voiceCount = voiceCount + 1;
				} {
					inval = outval.yield;
				};
			};
		};
		if(insidePolyphon) {
			Error("forgot to close polyphony").throw;
		};
		memory.do { |event|
			inval = event.yield;
		}
		^inval
		
	}

}

