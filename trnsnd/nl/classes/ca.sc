CAUGen : UGen {
	*categories {^#["UGens>transnd"] }
}

CA1 : CAUGen {
	*ar { |smprt=22050 smpwd=60 rule=18 seed=1 trig=0 mul=1.0, add=0.0|
		^this.multiNew(\audio, smprt, smpwd, rule, seed, trig).madd(mul, add);
	}
}

CA2 : CAUGen {
	*ar { |smprt=22050 smpwd=60 rule="1C2A4798" seed=1 trig=0 mul=1.0 add=0.0|
		var ruleArray = Array.newClear(8);
		rule.do {|item, i| ruleArray[i] = ("16r" ++ item).interpret.asBinaryDigits(4) };
		ruleArray = ruleArray.flat.reverse;
		^this.multiNewList([\audio, smprt, smpwd, seed, trig] ++ ruleArray).madd(mul, add);
	}
}