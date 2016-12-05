
DualBandLim {
	*ar { |in, xfreq = 300, lflim = 0.7, hflim = 0.3, lfgain = 1, hfgain = 1|
		var lf = LPF.ar(in, xfreq);
		var hf = in - lf;

		lf = Limiter.ar(lf * lfgain, lflim);
		hf = Limiter.ar(hf * hfgain, hflim);
		^lf + hf
	}
}

// - take loudest chan and compress all chans to its level.
// maybe conceptually better? should keep panning intact.

DualBandComp {
	*ar { |in, xfreq = 300, lflim = 0.7, hflim = 0.3, lfgain = 1, hfgain = 1|
		var lo = LPF.ar(in, xfreq);
		var hi = in - lo;
		var loCtl = 0, hiCtl = 0;
		lo = lo * lfgain;
		hi = hi * hfgain;
		// take peaks of loudest chan as control signals:
		lo.do { |ch| loCtl = max(loCtl, PeakFollower.ar(ch, 0.99)) };
		hi.do { |ch| hiCtl = max(hiCtl, PeakFollower.ar(ch, 0.99)) };
		lo = Compander.ar(lo, loCtl, lflim, 1, 0.1);
		hi = Compander.ar(hi, hiCtl, hflim, 1, 0.1);
		^lo + hi
	}
}

