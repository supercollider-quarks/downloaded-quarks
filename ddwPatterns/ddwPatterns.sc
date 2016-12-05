
	// some new patterns - jamshark70@dewdrop-world.net

Pwxrand : Pwrand {
	embedInStream { |inval|
		var item,
			weightStream = weights.asStream,
			currentWeights,
			index = weightStream.next(inval).windex,
			totalweight, rnd, runningsum;
		repeats.value(inval).do({ |i|
			item = list.at(index);
			inval = item.embedInStream(inval);

			currentWeights = weightStream.next(inval);
			if(currentWeights.isNil) { ^inval };

			totalweight = 1.0 - currentWeights[index];
			rnd = totalweight.rand;
			runningsum = 0;
			while {
				index = (index + 1) % currentWeights.size;
				runningsum = runningsum + currentWeights[index];
				runningsum < rnd
			};
		});
	}
}

Pwshuf : ListPattern {
	var <>weights, <>mask;
	*new { |list, weights, repeats, mask|
		^super.new(list, repeats).weights_(weights).mask_(mask)
	}
	embedInStream { |inval|
		var localWeights, i, maskOffset;
		if(repeats == inf and: { weights.every(_ <= 0) }) {
			"Pwshuf: No positive weights and infinite repeats, aborting.".warn;
			^inval
		};
		repeats.do {
			localWeights = weights.normalizeSum;  // returns a new array -- clever, no copy
			while { localWeights.any(_ > 0) } {
				i = localWeights.windex;
				inval = list.wrapAt(i).embedInStream(inval);
				if(mask.size == 0) {
					localWeights = localWeights.wrapPut(i, localWeights.wrapAt(i) * (mask ? 0)).normalizeSum;
				} {
					maskOffset = mask.size div: 2;
					mask.do { |factor, j|
						j = j + i - maskOffset;
						if(j >= 0 and: { j < localWeights.size }) {
							localWeights.wrapPut(i, localWeights.wrapAt(i) * (factor ? 0));
						};
					};
					localWeights = localWeights.normalizeSum;
				};
			};
		};
		^inval
	}
}

// deprecated; Pslide now has a wrap flag
PslideNoWrap : Pslide {
		// false = do not wrap at end
	*new { |list, repeats, len, step, start|
		^super.new(list, repeats, len, step, start, false)
	}
}

Pslide1 : Pslide {
    embedInStream { arg inval;
    	var pos, item, lenStream, stepStream, lenn, stepp;
    	pos = start;
    	lenStream = len.asStream;
    	stepStream = step.asStream;
    	repeats.value(inval).do({
    			// nil protection -- stop immediately if lenStream or stepStream return nil
    		(lenn = lenStream.next(inval)).notNil.if({
	    		lenn.do({ arg j;
	    			item = list.wrapAt(pos + j);
	    			inval = item.embedInStream(inval);
	    		});
	    		(stepp = stepStream.next(inval)).notNil.if({ pos = pos + stepp },
	    			{ ^inval });
	    	}, { ^inval });
    	});
	     ^inval;
    }
}

PseqFunc : Pseq {
		// executes a function on the list item before embedding in stream
		// if func is nil, the item is used as is
	var	<func;

	*new { |list, repeats = 1, offset = 0, func|
		^super.new(list, repeats, offset).func_(func)
	}

	func_ { |f|
		f.isNil.if({ func = { |x| x } }, { func = f });
	}

	embedInStream {  arg inval;
		var item, offsetValue;
		offsetValue = offset.value;
		if (inval.eventAt('reverse') == true, {
			repeats.value(inval).do({ arg j;
				list.size.reverseDo({ arg i;
					item = func.value(list.wrapAt(i + offsetValue));
					inval = item.embedInStream(inval);
				});
			});
		},{
			repeats.value(inval).do({ arg j;
				list.size.do({ arg i;
					item = func.value(list.wrapAt(i + offsetValue));
					inval = item.embedInStream(inval);
				});
			});
		});
		^inval;
	}
}

PserFunc : PseqFunc {
	embedInStream {  arg inval;
		var item, offsetValue;
		offsetValue = offset.value;
		if (inval.eventAt('reverse') == true, {
			repeats.value(inval).reverseDo({ arg i;
				item = func.value(list.wrapAt(i + offsetValue));
				inval = item.embedInStream(inval);
			});
		},{
			repeats.value(inval).do({ arg i;
				item = func.value(list.wrapAt(i + offsetValue));
				inval = item.embedInStream(inval);
			});
		});
		^inval;
	}
}


// 1/f noise

Pvoss : Pattern {
	var	<>lo, <>hi, <>generators, <>length;
	*new { |lo = 0, hi = 1, generators = 8, length = inf|
		^super.newCopyArgs(lo, hi, generators, length)
	}
	embedInStream { |inval|
		var	localGenerators = generators.value;
		^Pfin(length.value,
			Pn(PstepNadd(*(Pwhite(0.0, 1.0, 2) ! localGenerators)), inf)
				/ localGenerators * (hi - lo) + lo
		).embedInStream(inval);
	}
}

Pmcvoss : Pvoss {
	embedInStream { |inval|
		var	counter = 1,
			localGenerators = generators.value,
			maxCounter = 1 << (localGenerators-1),
			gens = { 1.0.rand } ! localGenerators,
			total = gens.sum,
			i, new;

		length.value(inval).do {
			inval = ((total / localGenerators) * (hi - lo) + lo).yield;

			i = counter.trailingZeroes;
			new = 1.0.rand;
			total = total - gens[i] + new;
			gens[i] = new;

			counter = (counter + 1).wrap(1, maxCounter);
		};
		^inval
	}
}

Ptempo : Pattern {
	asStream { ^FuncStream({ thisThread.clock.tryPerform(\tempo) ?? { 1 } }) }
}


// Like Pstep, but assumes an event as input (with delta) -- does not refer to clock time
// caveat: 'dur' or 'delta' *must* be set in the inval before this pattern gets called:
// Pbind(\dur, ..., \step, PstepDur(...)) = OK
// Pbind(\step, PstepDur(...), \dur, ...) = not OK

PstepDur : Pstep {
	var <>tolerance;

	*new { |levels, durs = 1, repeats = 1, tolerance = 0.001|
		^super.newCopyArgs(levels, durs, repeats).init.tolerance_(tolerance);
	}

	embedInStream { |inval|
		var itemStream, durStream, item, dur, nextChange = 0, elapsed = 0;
		repeats.value(inval).do {
			itemStream = list.asStream;
			durStream = durs.asStream;
			while {
				item = itemStream.next(inval);
				item.notNil and: {
					dur = durStream.next(inval);
					dur.notNil
				}
			} {
				nextChange = nextChange + dur;
				// 'elapsed' increments, so nextChange - elapsed will get smaller
				// when this drops below 'tolerance' it's time to move on
				while { (nextChange - elapsed) >= tolerance } {
					elapsed = elapsed + inval.delta;
					inval = item.embedInStream(inval);
				};
			};
		};
		^inval
	}
}