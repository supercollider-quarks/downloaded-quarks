Pshutter : FilterPattern {
	var <>quant;
	*new { arg pattern, quant = 1.0;
		^super.new(pattern).quant_(quant)
	}

	embedInStream { arg inval;
		var outval, str, split, delta; 
		str = pattern.asStream;
		loop {
			outval = str.next(inval);
			if(outval.isNil) { nil.yield; ^inval };
			delta = outval.delta;
			
			if(delta > quant) {
				split = delta div: quant;
				outval.use {
					~sustain = ~sustain.value; // fix sustain of first event
					~dur = quant;
				};
				inval = outval.yield;
				(split - 1).do { 
					inval = Event.silent(quant).yield 
				};
				
			} {
				outval[\delta] = quant;
				inval = outval.yield // assumes delta == quant now.
			} 
		}
	}

}