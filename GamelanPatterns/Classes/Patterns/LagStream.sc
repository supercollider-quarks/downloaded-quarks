
LinLagStream : Stream {
	var <>lag, <end;
	var lastTime, current;
	var <thread;
	
	*new { arg lag = 1.0, value = 0.0, end;
		^super.newCopyArgs(lag).init(value, end)
	}
	
	init { arg value, endVal;
		thread = thisThread;
		this.end = endVal ? value;
		current = value;
	}
	
	next {
		^this.phase.linlin(0.0, 1.0, current, end);
	}

	end_ { arg val;
		lastTime = this.beats;
		if(this.phase == 1.0) { current = end };
		end = val;
	}
		
	phase {
		var currentTime = this.beats;
		^if(lastTime + lag > currentTime) {
			currentTime.linlin(lastTime, lastTime + lag, 0.0, 1.0);
		} {
			1.0
		}
	}
	
	beats {
		// ^thread.beats // doesn't work: thread seems to refer always to thisThread
		 ^Main.elapsedTime
		//^thread.clock.secs2beats(Main.elapsedTime)
	}

}

LagStream : LinLagStream {
	var <>curve = 0.001;
	
	phase {
		var currentTime = this.beats;
		^if(lastTime + lag > currentTime) {
			currentTime.linexp(lastTime, lastTime + lag, curve, curve + 1) - curve;
		} {
			1.0
		}
	}
			

}


