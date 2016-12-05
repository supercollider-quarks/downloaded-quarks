Pvgamelan : Pattern {
	var <>pattern, <>instKey; // gamelan is synthDef
	classvar <convertDegree;
	
	*initClass {
		convertDegree = { arg degree=(0), octave=(5);
			 "%%".format(octave, degree.floor).asSymbol;
		}.flop;
	}
	
	*new { arg pattern, instKey;
		 ^super.newCopyArgs(pattern, instKey)
	}
	
	embedInStream { arg inval;
		var outval, stream, instrEvent;
		var octave, degree, laras, noteKey;
		
		stream = pattern.asStream;
		
		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			if(outval.at(\degree).isKindOf(Symbol).not) {
				
				outval.use {
					~instKey = instKey ? ~instKey;
					if(~laras.isNil) { // ~tuning will be removed later..
						~tuning = ~laras = 
							if(~pathet.isNil or: { ~pathet.asString.at(0) == $s }) 
						{ \slendro } { \pelog }
					};
				
					if(~noteKey.isNil) {
						if(~degree.isNil or: { ~degree == 0 }) {
							"WARN - Pvgamelan: degree in % is wrong: %\nevent: %"
								.format(instKey, ~degree, outval).postvg;
						};
						~noteKey = convertDegree.value(~degree, ~octave);
					};

				};
				outval = VGSynth.eventFor(outval);
			};
	
			inval = outval.yield;
		}
		^inval
		
	}
	
}
