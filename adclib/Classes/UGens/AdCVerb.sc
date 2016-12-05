/* 	Simple reverb class, based on MoorerLoyReverb as given in Pope, Sc1 Tutorial.
input is converted to mono and filtered,
dense reverb is done with a bank of comb filters with prime ratio delaytimes;
hfDamping uses side CombL side effect for frequency dependent decay.
defaultCombTimes are handpicked from random prime number ratios,
allpass times are random based on prime numbers, and can be posted to keep magical ones.
*/
AdCVerb {

	classvar <>verbose = false, <>maxTime = 0.2;

	// Initialize comb table for longer reverberations
	//	 "// combs: ".post;
	// try creating good prime number based delayTimes with e.g. :
	//	combTimes = ({ rrand(100, 400).nthPrime } ! numCombs).sort.postln / 40000;
	classvar <defaultCombTimes = #[
		0.0797949, 			// new prime Numbers
		0.060825,
		0.0475902,
		0.0854197,
		0.0486931,
		0.0654572,
		0.0717437,
		0.0826624,
		0.0707511,
		0.0579574,
		0.0634719,
		0.0662292
	];

	*ar { arg in, revTime = 3, hfDamping = 0.1, nOuts = 2, predelay = 0.02,
		numCombs = 8, numAllpasses = 4, inFilter = 0.6, leakCoeff = 0.995,
		combScale = 1, apScale = 1, allpassPrimes;

		var monoIn = this.makeMonoIn(in, inFilter, leakCoeff, predelay);
		var combTimes = this.makeCombTimes(numCombs);

		var combsOut = if (numCombs > 0) {
			this.makeCombs(monoIn, combTimes, revTime, hfDamping, numCombs, combScale)
		} {
			// if no combs, use monoIn for allpasses
			monoIn
		};

		var allpasses = this.makeAllpasses(combsOut, revTime, nOuts,
			numAllpasses, apScale, allpassPrimes);

		^allpasses
	}

	// mix input down to mono if needed, block DC, roundoff and pre-delay reverb input.
	*makeMonoIn { |in, inFilter, leakCoeff, predelay|
		if (in.size > 1) { in = in.asArray.sum };
		if (leakCoeff != 1) { in = LeakDC.ar(in, leakCoeff) };
		if (inFilter != 0) { in = OnePole.ar(in, inFilter) };
		if (predelay != 0) { in = DelayN.ar(in, maxTime, predelay) };
		^in
	}

	*makeCombTimes { |numCombs|
		^defaultCombTimes.copyRange(0, numCombs - 1);
	}

	*makeCombs { |monoIn, combTimes, revTime, hfDamping, numCombs, combScale|
		// used for comb average-filtering;
		var timeOneSample = SampleDur.ir;
		var combsOut;

		// Create an array of combs, with a special trick to make treble decay faster than lows:
		combsOut = CombL.ar(monoIn, maxTime,

				(combTimes * combScale)
				.round(timeOneSample)	// round delay times to integer samples
				+ 						// and add up to half a sample to them:
				// linear interpolation between samples loses
				// high freq energy, with the maximum at 0.5.
				(timeOneSample * 0.5 * hfDamping),
				revTime
			).sum;
		^combsOut
	}

	*makeAllpasses { |combsIn, revTime, nOuts, numAllpasses, apScale, allpassPrimes|
		var allpassTimes, primeRange, apDecay;

		allpassPrimes = allpassPrimes ?? {
			primeRange = 250 div: numAllpasses;
			{
				{ |i| rrand(i + 1 * primeRange, i + 2 * primeRange).nthPrime } ! numAllpasses
			} ! nOuts
		};

		allpassTimes = allpassPrimes * (1/44100); // scale into a good time range.

		if (verbose) {
			"// AdCVerb - allpassPrimes are: \n    %\n\n".postf(allpassPrimes);
		};

		// allpass decay always is < 1 and shorter than combs decay
		apDecay = 1.min(revTime * 0.6);

		// Put the output through nOuts parallel chains of allpass delays
		^allpassTimes.collect({ |timesCh|
			// was: combsIn + monoIn;
			var out = combsIn;
			timesCh.do { |time| out = AllpassN.ar(out, maxTime, time * apScale, apDecay) };
			out;
		});
	}
}

/// same as AdCVerb, but can have random animation on the combTimes - animRate, animDepth.

AdCVerb2 : AdCVerb {

	*ar { arg in, revTime = 3, animRate = 0.1, animDepth = 0.3,
		hfDamping = 0.1, nOuts = 2, predelay = 0.02,
		numCombs = 8, numAllpasses = 4, inFilter = 0.6, leakCoeff = 0.995,
		combScale = 1, apScale = 1, allpassPrimes;

		var monoIn = this.makeMonoIn(in, inFilter, leakCoeff, predelay);
		var combTimes = this.makeCombTimes(numCombs, animRate, animDepth);

			// if no combs, use monoIn
		var combsOut = if (numCombs > 0) {
			this.makeCombs(monoIn, combTimes, revTime, hfDamping, numCombs, combScale)
		} {
			monoIn
		};

		var allpasses = this.makeAllpasses(combsOut, revTime, nOuts,
			numAllpasses, apScale, allpassPrimes);

		^allpasses
	}

	*makeCombTimes { |numCombs, animRate, animDepth|
		// Table of combtimes to use
		var combTimes = defaultCombTimes.copyRange(0, numCombs - 1);
		var combDrifts = LFDNoise3.kr(animRate ! numCombs)
		.range(combTimes.minItem, combTimes.maxItem);

		^combTimes.blend(combDrifts, animDepth);
	}
}
