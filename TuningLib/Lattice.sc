// This code by Charles Celeste Hutchins

Lattice {

	// coming soon

		*adjustOctave { arg ratio, base =2;
	/*@
	desc: For a given ratio, does octave transpositions and returns a ratio between 1 and 2
		ratio: the ratio to adjust
		base: the octave multiplier. Usually this should be left as 2, for for some tuning systems, like Bohlen-Pierce, it might be another number.
	ex:
	Diamond.adjustOctave(9/2) // returns 1.125, which is 9/8
	@*/
		/*
		{ratio < 1}.while ({
			ratio = ratio * 2;
		});
		{ratio > 2}.while ( {
			ratio = ratio / 2;
			//ratio.postln;
		});

		//ratio.postln;
		*/

		ratio = ratio / (ratio.previousPowerOf(base))

		^ratio;
	}

}