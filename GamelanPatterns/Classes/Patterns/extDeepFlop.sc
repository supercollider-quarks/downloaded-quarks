+ SequenceableCollection {
	flopEvents { arg defaultEvent;
		var keys = IdentitySet.new;
		var values, res;
		defaultEvent = defaultEvent ? Event.default;
		this.do { |x| keys.addAll(x.keys) };
		keys = keys.asArray;
		
		keys.do { |key|
			var all = [];
			this.do { |x|
				var val = x.at(key);
				if(val.isNil) {
					val = defaultEvent.at(key);
				};
				if(val.notNil) {
					all = all.add(val);
				}
			};
			values = values.add(all);
		};
		res = ();
		
		keys.do { |key, i|
			if(key === \dur
				or: {key === \resource}
				or: {key === \instrument}
				or: {key === \delta} 
				or: {key === \legato} 
				or: {key === \sustain}
			) 
			{
				res.put(key, values.at(i).at(0)) // cannot multichannel expand these.
			} {
				res.put(key, values.at(i).unbubble)
			}
		};
		^res
	}

}