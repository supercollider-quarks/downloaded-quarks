
PoffsetGong : FilterPattern {
	var <>minOffset, <>maxOffset, <>end;

	// maxOffset for gong should be shorter than minOffset for the other instruments.

	*new { arg pattern, minOffset = 0.5, maxOffset = 1.0, end=true;
		^super.new(pattern).minOffset_(minOffset).maxOffset_(maxOffset).end_(end)
	}

	embedInStream { arg inval;

		var distanceFromGong, gonganDur;
		var clock, outval;
		var stream = pattern.asStream;

		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			if(outval.at(\doSuwuk) === true) {
				gonganDur = outval.at(\gonganDur);
				distanceFromGong = gonganDur - outval.at(\sabet);
				postf("Psuwuk - distanceFromGong: %\n", distanceFromGong);
				if(distanceFromGong == 0) {
					outval[\timingOffset] = outval[\timingOffset] + rrand(minOffset, maxOffset);
					if(end) {
						"ending with PlagGong, in % beats\n".postf(outval.delta);
						inval = outval.yield;
						nil.alwaysYield;
						^inval
					};
				};

			};
			inval = outval.yield;

		};
		^inval

	}

}

/*Psuwuk : FilterPattern {
	embedInStream { arg inval;

		var warp = \cos.asWarp, distanceFromGong, newTempo;
		var clock, task, outval, stream = pattern.asStream;

		while {
			outval = stream.next(inval);
			outval.notNil
		} {
			if(outval.at(\segment) === \suwuk) {
				clock = thisThread.clock;
				if(clock.isKindOf(GamelanClock).not) {
					Error("warning: Psuwuk needs GamelanClock!").throw;
				};
				distanceFromGong =  outval.at(\gonganDur) - outval.at(\gonganPhase);
				distanceFromGong.postln;
				if(distanceFromGong == 0) {
					clock.hold = true;
					clock.pause;
					task = clock.queue[1]; // read task from the queue
					clock.clear; // remove all tasks
					clock.fadeTempo(1.0, rrand(0.3, 3)); // tweak here
					clock.sched(1.0, { clock.hold = false; "clock hold off".postln; nil });

				} {
					newTempo = blend(clock.tempo, 0, 1 - warp.map(distanceFromGong));
					clock.fadeTempo(newTempo, outval.delta / clock.tempo);
				}

			};
			inval = outval.yield;

		}

	}

}*/

