
Dsinemap : DUGen {
	*new { arg r = 0.2, length = inf;
		^this.multiNew('demand', length, r)
	}
}

Dsine2map : DUGen {
	*new { arg r1 = 0.2, r2 = 0.8, length = inf;
		^this.multiNew('demand', length, r1, r2)
	}
}
