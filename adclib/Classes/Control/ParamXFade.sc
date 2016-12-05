ParamXFade {
	classvar <getSetDict;

	var <obj, <>fadeTime, <>dt, <>getSetType;
	var <>getFunc, <>setFunc, <>startFunc, <>endFunc;
	var <task, <fadeIndex, <remainTime;
	var <dest, <destNames, <destVals, <currVals;

	*initClass {
		getSetDict = (
			uni: (
				get: { |obj, keys| obj.getUnis(keys) },
				set: { |obj, keysVals| obj.setUni(*keysVals) }
			)
		);
	}

	*new { |obj, fadeTime = 5, dt = (1/32), getSetType = \uni|
		^super.newCopyArgs(obj, fadeTime, dt, getSetType).init;
	}

	init {
		this.initFuncs;
		this.initTask;
	}

	initFuncs {
		var funcs = getSetDict[getSetType];
		if (funcs.notNil) {
			getFunc = funcs[\get]; setFunc = funcs[\set]
		} {
			"%: please specify getFunc and setFunc.\n".postf(this);
		};
		startFunc = { "% starts xfade to %.".postf(this, dest.cs) };
		endFunc = { "% ended xfade to %.".postf(this, dest.cs) };
	}

	// should be able to change dest while fading?
	dest_ { |indest|
		// dest is a flat list of [key1, val1, ... keyN, valN]
		// check dest for integrity first...
		// then:
		dest = indest;
		#destNames, destVals = dest.clump(2).flop;
	}

	initTask {
		task = TaskProxy {
			var nextVals, oldIndex, fadeStep, blendVal;
			startFunc.value;
			fadeIndex = 1;
			remainTime = fadeTime;
			// var increment =
			while { remainTime > (dt * 0.5) } {
				currVals = getFunc.(obj, destNames);
				oldIndex = fadeIndex;
				fadeIndex = remainTime / fadeTime;
				blendVal = oldIndex - fadeIndex / fadeIndex;

				// hmm, how to introduce xfade warp curve here?

				// calc values to set to here:
				[fadeIndex, remainTime];
				nextVals = blend(currVals, destVals, blendVal);
				setFunc.(obj, [destNames, nextVals].flop.flat);

				dt.wait;
				remainTime = (remainTime - dt);
			};
			endFunc.value;
		};
	}

	fadeTo { |indest, time|
		if (indest.notNil) {
			fadeTime = time ? fadeTime;
			this.dest = indest;
			task.play;
		}
	}
}