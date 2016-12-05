Peuclid : Pattern {
	var <>numHits, <>numBeats, <>repeats;

	*new { |numHits = 7, numBeats = 12, repeats = 1|
		^super.newCopyArgs(numHits, numBeats, repeats)
	}

	embedInStream { |inval|
		var currval = 0, lastval = -1;
		var repStream = repeats.asStream;
		var numHitStream = numHits.asStream;
		var numBeatStream = numBeats.asStream;

		repStream.next(inval).do { arg j;
			var numBeatVal = numBeatStream.next(inval);
			var numHitVal = numHitStream.next(inval);
			var increment;

			if (numBeatVal.notNil and: numHitVal.notNil) {
				// cant have more hits than beats
				numHitVal = min(numBeatVal, numHitVal);
				increment = (numHitVal / numBeatVal).postln;

				numBeatVal.do { arg i;
					var isHit;
					isHit = (currval - lastval).round(1e-10) >= 1;
					[currval, lastval, isHit].postln;
					if (isHit) { lastval = currval.round(1e-10).trunc };
					inval = isHit.binaryValue.embedInStream(inval);
					currval = (currval + increment);
				};
			}
		};
		^inval;
	}

	storeArgs { ^[ numHits, numBeats, repeats ] }
}