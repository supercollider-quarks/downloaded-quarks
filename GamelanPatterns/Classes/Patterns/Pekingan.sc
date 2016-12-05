// simple version

Ppekingan : Pattern {
	var <>balungan, <>irama;
	*new { arg balungan, irama=1;
		^super.newCopyArgs(balungan, irama)
	}

	embedInStream { arg inval;
		var str, outval, anticipation, n=2, count=0, res;

		str = balungan.asStream;

		while {
			outval = str.next(inval);
			outval.notNil
		} {
			anticipation = anticipation.add(outval);
			count = count + 1;
			if(count != 0 and: { count % 2 == 0 }) {
				if(anticipation[0][\degree] != anticipation[1][\degree]) {
					res = anticipation.stutter;
				} {
					res = [anticipation[0].copy, anticipation[1]];
					res[0].use { ~degree = ~degree + 1 };
					res = res.stutter;
				};
				res.do { |val|
					val = val.copy;
					val[\dur] = val[\dur] * 0.5;
					inval = val.yield
				};
				anticipation = [];
			};
		}
		^inval

	}
}


ProtoBalungan : FilterPattern {

	embedInStream { arg inval;
		var outval, stream, dur;
		stream = pattern.asStream;
		loop {
			outval = stream.next(inval);
			if(outval.isNil) {nil.alwaysYield; ^inval };
			dur = outval.at(\dur);
			if(dur.frac != 0) { Error("pekingan for this balungan type not defined.").throw };
			if(dur > 1) { // for now split only whole dur
				dur.do {
					inval = outval.copy.put(\dur, 1.0).yield;
				}
			} {
				if(dur < 1) { "could not divide up balungan. values smaller 1.0 currently"
								" not  supported".warn };
				inval = outval.yield;
			}

		}
	}

}


PgamelanAmbitus {
	// wissen:
	// instrument (instr -> ambitus)
	// pathet


}
