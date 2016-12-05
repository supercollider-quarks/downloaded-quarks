/*
{ { 100.rand }.value; }!5	     // random, different every time
{ { 100.rand }.valueSeed; }!5    // detto when seed is nil
{ { 100.rand }.valueSeed(123); }!5 // same number with same seed

(
100.rand.postln;  // random
({ { 100.rand }.valueSeed(123); }!5).postln; // samesame with seed
100.rand.postln; // random again
)
*/
+ Function {
	valueSeed { |seed|
		var hasSeed = seed.notNil;
		var oldData, result;
		if (seed.isNil) {
			^this.value;
		};

		oldData = thisThread.randData;
		if(seed.isKindOf(Integer)){
			thisThread.randSeed = seed;
		}{
			thisThread.randSeed = seed.hash;
		};
		result = this.value;
		thisThread.randData_(oldData);
		^result
	}
}
