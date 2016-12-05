+ControlSpec {

	// a hasZeroCrossing variant that also detects if minval or maxval is 0
	safeHasZeroCrossing {
		var thisMinSign, thisMaxSign;
		#thisMinSign, thisMaxSign = [minval, maxval].collect{ |val|
			if(val.isArray) { val.sign.mean } { val.sign }
		};
		^thisMinSign != thisMaxSign or:{ (thisMinSign == 0).and(thisMaxSign == 0) };
	}

	// for correct display of zero crossing sliders, knobs, etc.
	excludingZeroCrossing {
		if(minval != 0 and:{ maxval != 0 }) {
			^this.hasZeroCrossing
		}
		^false
	}

}