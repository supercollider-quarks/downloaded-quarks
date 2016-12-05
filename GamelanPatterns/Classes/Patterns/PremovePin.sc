PremovePin : FilterPattern {

	embedInStream { arg inval;
		var outval, prev, stream;
		stream = pattern.asStream;
		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			if(outval.at(\degree) === '?') {
				prev = prev.copy;
				prev.put(\dur, prev.at(\dur) + outval.at(\dur));
			} {
				if(prev.notNil) {Êinval = prev.yield }; // elephant rein ..
				prev = outval;
			};
		};
		inval = prev.yield; // giraffe wieder raus..
		^inval
		
	}

}
