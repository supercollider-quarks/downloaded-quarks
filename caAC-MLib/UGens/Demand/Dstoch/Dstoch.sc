

Dcoin : DUGen {
	*new { arg weight = 0.5, length = inf;
		^this.multiNew('demand', length, weight)
	}
}


Dcoin2 : DUGen {
	*new { arg weight = 0.5, length = inf;
		^this.multiNew('demand', length, weight)
	}
}


Dexpon : DUGen {
	*new { arg lo = 0.0, hi = 1.0, length = inf;
		^this.multiNew('demand', length, lo, hi)
	}
}


Dexponential : DUGen {
	*new { arg threshold = 0, gamma, upperLimit, length = inf;
		^this.multiNew('demand', length, threshold, gamma, upperLimit)
	}
}


Dsumnrand : DUGen {
	*new { arg lo = 0.0, hi = 1.0, n = 1, length = inf;
		^this.multiNew('demand', length, lo, hi, n)
	}
}


Dlinear : DUGen {
	*new { arg lo = 0.0, hi = 1.0, favor = 0, length = inf;
		^this.multiNew('demand', length, lo, hi, favor)
	}
}


Dbeta : DUGen {
	*new { arg lo = 0.0, hi = 1.0, a = 0.2, b = 0.2, length = inf;
		^this.multiNew('demand', length, lo, hi, a, b)
	}
}


Dlogist : DUGen {
	*new { arg lo = 0.0, hi = 1.0, lambda = 3.449449, x = 0.001, length = inf;
		^this.multiNew('demand', length, lo, hi, lambda, x)
	}
}


Dsine : DUGen {
	*new { arg lo = 0.0, hi = 1.0, length = inf;
		^this.multiNew('demand', length, lo, hi)
	}
}


Dgamma : DUGen {
	*new { arg lo = 0.0, hi = 1.0, nu = 2, length = inf;
		^this.multiNew('demand', length, lo, hi, nu)
	}
}


Dcauchy : DUGen {
	*new { arg lo = 0.0, hi = 1.0, param = 1, length = inf;
		^this.multiNew('demand', length, lo, hi, param)
	}
}


Dlaplace : DUGen {
	*new { arg lo = 0, hi = 1, tau = 0.1, length = inf;
		^this.multiNew('demand', length, lo, hi, tau)
	}
}
