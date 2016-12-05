NudgeGroup {
	var <object, <params, <nudgeList, <nudgeDict;

	*new { |object, params|
		^super.newCopyArgs(object, params).init;
	}

	init {
		params = params ?? { object.controlKeys };
		nudgeDict = ();
		nudgeList = params.collect { |paramKey|
			var nudge = Nudge(object, paramKey);
			nudgeDict.put(paramKey, nudge);
			nudge;
		};
	}

	at { |keyOrIndex|
		^nudgeDict[keyOrIndex] ?? { nudgeList[keyOrIndex] };
	}

	nudge { |...deltas|
		deltas.keep(nudgeList.size).do { |delta, i|
			nudgeList[i].nudge(delta);
		}
	}
	nudgeAt { |keyOrIndex, delta|
		var nudge = this.at(keyOrIndex);
		if (nudge.notNil) { nudge.nudge(delta) };
	}

	nudgePairs { |... keysDeltas|
		keysDeltas.pairsDo { |key, delta|
			this.nudgeAt(key, delta);
		}
	}

	pullToward { |values, maxDelta, exp = 0.5|
		var dists, distScaler;
		dists = nudgeList.collect { |n, i| n.distFromUni(values[i]) };
		if (exp >= 0) {
			distScaler = dists.maxItem(_.abs)
		} {
			// arrived ones are zero, so need to filter them
			distScaler = 1;
			dists.do { |dist|
				dist = dist.abs;
				if (dist > 0) {
					distScaler = min(distScaler, dist)
				};
			};
		};
		distScaler = distScaler.reciprocal;

		nudgeList.do { |nudge, i|
			nudge.pullToward(values[i],
				maxDelta * (dists[i] * distScaler ** exp)
			);
		}
	}
}

Nudge {
	var <>object, <>key, <>getFunc, <>setFunc;
	var <>map2BiFunc, <>nudgeFunc, <>borderFunc, <>unmapFunc;
	var <>state, <unival, <bival;

	*new { |obj, key|
		^super.newCopyArgs(obj, key).init;
	}

	storeArgs { ^[object, key] }
	printOn { |stream| ^this.storeOn(stream) }

	init {
		state = (dir: 1, border: 1.0);
		getFunc = { |obj, key| obj.getUni(key) };
		setFunc = { |obj, key, val| obj.setUni(key, val) };
		nudgeFunc = { |val, delta, state|
			val + (delta * state[\border] * state[\dir])
		};

		this.useLin;
		this.useFold;
	}

	nudge { |delta|
		unival = getFunc.(object, key);
		bival = map2BiFunc.(unival, state);
		bival = nudgeFunc.(bival, delta, state);
		bival = borderFunc.(bival, state) ? bival;
		unival = unmapFunc.(bival, state);
		setFunc.value(object, key, unival);
	}

	getUni { ^unival = getFunc.(object, key); }
	setUni { |val| setFunc.value(object, key, val); }
	distFromUni { |val| ^val - this.getUni; }

	pullToward { |target, maxDelta = 0.02|
		var targ2, dist, sign, delta;
		unival = getFunc.(object, key);
		bival = map2BiFunc.(unival, state);
		targ2 = map2BiFunc.(target, state);
		dist = targ2 - bival;

		if (dist == 0) { ^this };

		delta = min(maxDelta, dist.abs) * dist.sign;
		bival = bival + delta;
		unival = unmapFunc.(bival, state);
		setFunc.value(object, key, unival);
	}

	useClip {
		borderFunc = { |val, state| val.clip2(state[\border]) };
	}
	useWrap {
		borderFunc = { |val, state| val.wrap2(state[\border]) };
	}

	useFold {
		borderFunc = { |val, state|
			if (val.abs >= state[\border]) {
				state[\dir] = state[\dir].neg;
				val = val.fold2(state[\border]);
			} { val };
		};
	}

	useLin {
		state[\border] = 1.0;
		map2BiFunc = { |val| val.unibi };
		borderFunc = { |val, state| val.clip2(state[\border]) };
		unmapFunc = { |val| val.biuni };
	}

	useTan { |drive = 5|
		state[\border] = drive;
		state[\tanGain] = drive.atan;
		map2BiFunc = { |val, state| (val.unibi * state[\tanGain]).tan };
		unmapFunc = { |val, state| (val.atan / state[\tanGain]).biuni };
	}

	useSin {
		state[\border] = 0.5pi;
		state[\tanGain] = 1;
		map2BiFunc = { |val| val.unibi.asin };
		unmapFunc = { |val| val.sin.biuni };
	}

	useBipow { |exp = 0.5|
		state[\border] = 1;
		state[\exp] = exp;
		map2BiFunc = { |val, state| val.unibi.bipow(state[\exp]) };
		unmapFunc = { |val, state| val.bipow(state[\exp].reciprocal).biuni };
	}

}