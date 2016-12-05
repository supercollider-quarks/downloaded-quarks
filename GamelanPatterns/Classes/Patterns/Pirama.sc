
Pirama : FilterPattern {

	var <>threshUp, <>threshDown, <>stepsize;
	var <>verbose = true;
	classvar <>iramaSymbols = #[\rangkep3, \ir3, \ir2, \ir1, \ir0];
	classvar <>bukaSymbol = \buka;

	*new { arg pattern,
		// hypothetical values
		threshUp = #[0, 0.25, 0.5, 0.95, 1.9],
		threshDown=#[0, 0.2, 0.33, 0.73, 1.5],
		stepsize = 0.001;
		^super.newCopyArgs(pattern, threshUp, threshDown, stepsize)
	}

	embedInStream { arg inval;
		var stream = pattern.asStream, outval, clock;
		var tempo, prevTempo = 0.0, irama, irsym = Ref(\ir1), upward = false;
		while {
			//inval = inval.copy.put(\irama, irsym); // add irama both upstream is a problem
			outval = stream.next(inval);
			outval.notNil
		} {
			clock = thisThread.clock;
			if(clock === SystemClock) {
				tempo = 1.0;
				"warning: Pirama uses SystemClock, which cannot change tempo.".postln;
			} {
				tempo = clock.tempo
			};
			if(absdif(tempo, prevTempo) >= stepsize) {
				upward = tempo > prevTempo;
				if(verbose) {
					postvg("Pirama: clock tempo changed: % irama: % direction: % \n",
						tempo.round(0.0001), irsym.value,
						if(upward) { "upward" } { "downward" });
				};
			};
			if(upward) {
				irama = threshUp.indexOfSmallerThan(tempo) ? 0
			} {
				irama = threshDown.indexOfSmallerThan(tempo) ? 0
			};

			if((outval.at(\doSuwukInNextGongan) !== true).and(outval.at(\doSuwuk) !== true)) { // in suwuk tamban, current irama is kept
				irsym.value = iramaSymbols.clipAt(irama);

			};

			if(outval.at(\segment) === \buka) { // in buka there is no real irama defined
				outval.put(\irama, bukaSymbol);
			} {
				outval.put(\irama, irsym);
			};
			prevTempo = tempo;

			// [\Pirama, irama].postln;

			inval = outval.yield;

		}
		^inval
	}

}



PiramaOffset {
	*new { arg leveldict;
		^Pfunc { |event|
					leveldict[event[\irama].value] ?? { leveldict[\default] }
		}
	}

}

/*PiramaOffset2 : Pcollect {
	*new { arg pattern, leveldict;
		^super.new(pattern, { |event|
				var lag = leveldict[event[\irama].value] ?? { leveldict[\default] };
				event[\lag] = event[\lag] + lag;
				event[\gonganOffset] = lag.asInteger;
		})
	}

}
*/


PtempoChanged {


}

