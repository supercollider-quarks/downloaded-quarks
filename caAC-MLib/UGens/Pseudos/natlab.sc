
// TEST

NatLab {
	*ar {
		|in, gain = -1.01, val = #[0,0.02,0.03,0.08,0.4], dur = #[0.01,0.02,0.05,0.1], mul = 1, add = 0|
		var rmp = DemandEnvGen.ar(Drand(val,inf),Drand(dur,inf),1);
		var sig = DelayC.ar(in,0.4,rmp).tanh * gain;
	/*	sig = tanh((3*sig**3) + (2*sig**2) - 0.5) * gain;*/
		^sig.madd(mul,add);
	}
}



