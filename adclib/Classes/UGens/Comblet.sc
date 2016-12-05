CombletN {
	*combClass { ^CombN }

	*ar { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2,
		decaytime = 1.0, risetime = 0.1, mul = 1.0, add = 0.0;

		var combs = [decaytime, risetime].collect { |decay|
			this.combClass.ar(in, maxdelaytime, delaytime, decay)
		};
		^(combs[0] - combs[1]).madd(mul, add);
	}
	*kr { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2,
		decaytime = 1.0, risetime = 0.1, mul = 1.0, add = 0.0;

		var combs = [decaytime, risetime].collect { |decay|
			this.combClass.kr(in, maxdelaytime, delaytime, decay)
		};
		^(combs[0] - combs[1]).madd(mul, add);
	}
}

CombletL : CombletN {
	*combClass { ^CombL }
}

CombletC : CombletN {
	*combClass { ^CombC }
}