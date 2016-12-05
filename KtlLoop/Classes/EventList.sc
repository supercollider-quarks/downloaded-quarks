
EventList : List {
	var <totalDur = 0, <playingDur = 0;

	print { |keys, postRest = true|
		var postKeys;
		if (postRest.not) {
			postKeys = keys;
		} {
			postKeys = this[1].keys.asArray.sort;
			if (keys.notNil) {
				postKeys = (keys ++ postKeys.removeAll(keys));
			};
		};
		this.do { |ev|
			var ev2 = ev.copy;
			postKeys.do { |key|
				"%: %, ".postf(key, ev2.removeAt(key));
			};
			if (ev2.size > 0) {
				".. %\n".postf(ev2);
			} {
				"".postln;
			};
		}
	}

	start { |absTime = 0|
		this.add((absTime: absTime, type: \start, relDur: 0));
	}

	addEvent { |ev|
		if (array.size == 0) { this.start(ev[\absTime]) };
		super.add(ev);
		this.setRelDurInPrev(ev, this.lastIndex);
	}

	calcRelDurs {
		this.doAdjacentPairs { |prev, next|
			var newRelDur = next[\absTime] - prev[\absTime];
			prev.put(\relDur, newRelDur);
			prev.put(\dur, newRelDur);
		};
		this.last.put(\relDur, 0).put(\dur, 0);
	}

	finish { |absTime|
		this.addEvent((absTime: absTime, type: \end, relDur: 0));
		totalDur = absTime - this.first[\absTime];
		playingDur = totalDur;
		this.setPlayDursToRelDur;
	}

	setRelDurInPrev { |newEvent, newIndex|
		var prevEvent;
		newIndex = newIndex ?? { array.indexOf(newEvent) };
		prevEvent = array[newIndex - 1];

		if (prevEvent.notNil) {
			prevEvent[\relDur] = newEvent[\absTime] - prevEvent[\absTime];
		};
	}

	setPlayDurs { |func| this.do { |ev| ev.put(\playDur, func.value(ev)) } }

	setPlayDursToRelDur { this.setPlayDurs({ |ev| ev[\relDur] }); }

	quantizeDurs { |quant = 0.25, fullDur|
		var durScaler = 1;
		fullDur !? {
			playingDur = fullDur;
			durScaler = fullDur / totalDur;
		};

		this.doAdjacentPairs({ |ev1, ev2|
			var absNow = (ev2[\absTime] * durScaler).round(quant);
			var absPrev = (ev1[\absTime] * durScaler).round(quant);
			ev1.put(\playDur, (absNow - absPrev));
		});
		// leaves end event untouched.
	}

	restoreDurs {
		this.setPlayDursToRelDur;
	}
}
