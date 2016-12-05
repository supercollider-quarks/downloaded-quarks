
NLUGen : UGen {
	*categories {^#["UGens>transnd"] }
}

Logist : NLUGen {
	*ar {| freq=22050, r=1.5, xi=0.001, mul=1, add=0 |
		^this.multiNew(\audio, freq, r, xi).madd(mul, add);
	}
}
CML : NLUGen {
	*ar {| freq=22050, r=1.5, g=0.5, xi=0.001, mul=1.0, add=0.0 |
		^this.multiNew(\audio, freq, r, g, xi).madd(mul, add);
	}
}
GCM : NLUGen {
	*ar {| freq=22050, r=1.5, g=0.5, mul=1.0, add=0.0 |
		^this.multiNew(\audio, freq, r, g).madd(mul, add);
	}
}
HCM : NLUGen {
	*ar {| freq=22050, r=1.5, g=0.5, mul=1.0, add=0.0 |
		^this.multiNew(\audio, freq, r, g).madd(mul, add);
	}
}
Nagumo : NLUGen {
	*ar {| uh=0.01, vh=0.01, pulse=0 mul=1, add=0 |
		^this.multiNew(\audio, uh, vh, pulse).madd(mul, add);
	}
}
FIS : NLUGen {
	*ar {| r=3.5, xi=0.1, n=3, mul=1, add=0 |
		^this.multiNew(\audio, r, xi, n).madd(mul, add);
	}
}
TLogist : NLUGen {
	*kr {| r=1.5, xi=0.1, trg=0, mul=1, add=0 |
		^this.multiNew(\control, r, xi, trg).madd(mul, add)
	}
}

Below2 : UGen {
	*ar {| a=0 b=1 |
		var above, neg;
		above = a.thresh(b);
		neg = a.neg.thresh(b);
		^(a - above + neg)
	}
}

// shortcut
+ UGen {
	below2 {| b=1 |
		^Below2.ar(this, b);
	}
}

// multichannel expansion support
+ SequenceableCollection {
	below2 {| b=1 | ^this.collect { |item| item.below2(b) } }
}
