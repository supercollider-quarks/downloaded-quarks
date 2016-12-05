RandomOrbit {
	var <>seed, <>func, <>useScale, <>scaler = 1.0, <>sign = 1;

	*new { |seed, func, useScale = true|
		seed = seed ?? { Date.seed };
		func = func ?? { { |n = 1| n.collect ({ 1.0.rand2 }).unbubble } };
		^super.newCopyArgs(seed, func, useScale);
	}
	stepSeed { |inc = 1|
		seed = seed + inc;
	}
	value { |inc ... args|
		var res;
		this.stepSeed(inc ? 0);
		res = { func.value(*args) }.valueSeed(seed);
		^if (useScale) { res * scaler * sign } { res };
	}

	// pattern support
	next { |inval|
		var res;
		{ |inc| this.stepSeed(inc ? 0) }.valueWithEnvir(inval);
		{ res = func.valueWithEnvir(inval); }.valueSeed(seed);
		^if (useScale) { res * scaler * sign } { res };
	}

	asStream {
		^Routine({ |inval| loop { this.next(inval).yield }; })
	}

	embedInStream { |stream|
		this.asStream.embedInStream(stream)
	}
}