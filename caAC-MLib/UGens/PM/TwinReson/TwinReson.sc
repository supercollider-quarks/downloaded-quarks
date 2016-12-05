
// decay2 in milli seconds??
TwinReson : UGen {
	*ar { arg in, freq1 = 440, decay1 = 10, freq2 = 110, decay2 = 10 , nonlinear = 0.1;
		^this.multiNew('audio', in, freq1,decay1,freq2,decay2,nonlinear)
	}
}


