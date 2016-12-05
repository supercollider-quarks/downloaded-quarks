Ptiming : Pattern {
	var <>offset, <>jitter, <>drift, <>driftRatio, <>range, <>length, <>startVal;
	*new { arg offset=0.0, jitter=0.003, drift=0.01, driftRatio=0.1, range, length=inf, startVal;
		^super.newCopyArgs(offset, jitter, drift, driftRatio, range, length)
	}
	storeArgs { ^[offset, jitter, drift, driftRatio, range, length] }
	embedInStream { arg inval; 
	
		var brown = startVal ?? { (drift * driftRatio).xrand2 };
		var outval;
		length.do({
			brown = (brown + (drift * driftRatio).xrand2).fold2(drift);
			outval = brown + jitter.bilinrand + offset;
			if (range.notNil) { outval = outval.fold(*range) };
			inval = outval.yield;				
		});
		^inval;
	}
}