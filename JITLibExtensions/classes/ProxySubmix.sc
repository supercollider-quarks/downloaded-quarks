// temporary solution for postfader submixing
// when/if proxies use volume busses, this will change.

ProxySubmix : Ndef {

	var <skipjack, collection;

	addLevel { |lev_ALL = 1, masterNodeID = 1001|
		// if needed, init with default numChannels from NodeProxy
		if (this.isNeutral) { this.ar };

		this.put(masterNodeID, {
			ReplaceOut.ar(bus,
				bus.ar * \lev_ALL.kr(lev_ALL).lag(0.05)
			);
		});
	}

	addMix { |proxy, sendLevel = 0.25, postVol = true, mono = false|

		var index, item, sendName, volBus;
		this.checkInit(proxy);

		if (collection.includes(proxy)) { ^this };

		index = collection.indexOf(nil) ?? { collection.size };
		collection = collection.extend(max(collection.size, index + 1));
		sendName = ("snd_" ++ proxy.key).asSymbol;
		item = (proxy: proxy, name: sendName);
		collection[index] = item;
		this.addSpec(sendName, \amp);

		if (postVol) {
			if (skipjack.isNil) { this.makeSkip };
			volBus = Bus.control(server, 1);
			item[\volBus] = volBus;
		};

		this.put(index + 1, {
			var source, levelCtl;
			source = NumChannels.ar(proxy.ar,
				if(mono) { 1 } { this.numChannels }
			);
			levelCtl = sendName.kr(sendLevel);
			if (postVol) {
				levelCtl = levelCtl * volBus.kr;
			};
			source * levelCtl.lag(0.05);
		});
		proxy.addDependant(this);
	}
	removeMix { |proxy|
		var i = collection.detectIndex { |item| item[\proxy] === proxy };
		if(i.notNil) {
			collection[i][\proxy].removeDependant(this);
			collection[i][\volBus].free;
			collection[i] = nil;
			this.put(i+1, nil);
		};
	}

	checkInit { |proxy|
		if (this.isNeutral) { this.ar(proxy.numChannels) };

		if (collection.isNil) {
			collection = [];
			this.addSpec(\lev_ALL, [0, 4, \amp]);
		};
	}

	makeSkip {
		skipjack = SkipJack({ this.updateVols; }, 0.05);
	}

	updateVols {
		// collect all setmessages and send as one bundle
		// to reduce osc traffic
		server.bind {
			collection.do { |item, i|
				var volBus;
				if(item.notNil) {
					volBus = item[\volBus];
					if (volBus.notNil) {
						volBus.set(
							item[\proxy].vol
							* item[\proxy].monitor.isPlaying.binaryValue
						)
					};
				};
			};
		};
	}

	clear {
		collection.do { |item|
			if(item.notNil) {
				item[\proxy].removeDependant(this);
				item[\volBus].free;
			};
		};
		collection.clear;

		skipjack.stop;
		skipjack = nil;
		^super.clear;
	}

	proxies { ^collection.collect { |item| item[\proxy] } }
	sendNames { ^collection.collect { |item| item[\name] } }
}