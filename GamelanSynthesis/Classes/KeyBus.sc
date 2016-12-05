KeyBus { 
	var <keys, <values, <bus;
	var indexDict;
		// for KeyBus with (freq: [200, 300, 400], amp: [3,2,1]);
	var <isPoly, <isMono;
	
	*new { |keys, values, server| 
		^super.newCopyArgs(keys).init(values ? [], server);
	}
	
	argNames { ^keys }	// temp fix: behave like VGMulTree
	
	index { ^bus.index }
	
	init { |invals, server| 
		var firstSize;
		if (invals.size > keys.size) { 
			("KeyBus has more values than keys: " + invals + keys).warn;
		};
		values = invals.extend(keys.size, 0);
		bus = Bus.control(server, values.flat.size); 
		
			// all single values
		isMono = values.every{ |val| val.size == 0 }; 
		
			// all values have equal sizes, e.g. for VGG
		firstSize = values.first.size; 
		isPoly = values.every { |val| val.size == firstSize };
		
		this.set;
	}
	
	indexOf { |key|
		var res =  keys.indexOf(key);
		res ? { Error("KeyBus: % not found.".format(key)).throw };
		^res	
	}
		// get to set multiple single values in a polyphonic bus
		// shuld eventually support busses with unequal bus sizes too, 
		// but not now.
	setByIndex { |setIndex ... keysVals| 
		var newvals;
		if (isPoly.not) { 
			("KeyBus: cannot set index for non-poly bus. use .set(* %).\n\n"
				.format(keysVals)).warn;
			^this
		};
		if (values.first[setIndex].isNil) { 
			("KeyBus: setIndex is out of range. values: %, setIndex: %.\n\n"
				.format(values, setIndex)).warn;
			^this
		};
		
		newvals = values.deepCopy;
		keysVals.pairsDo { |key, val|
			var index = this.indexOf(key);
			var oldval = newvals.at(index);
			newvals[index].put(setIndex, val);	
		};
		values = newvals;
		bus.setn(newvals.flat); // always writes all values.
	}

	set { |... keysVals| 
		var newvals = values.deepCopy;
		keysVals.pairsDo { |key, val|
			var index = this.indexOf(key);
			var oldval = newvals.at(index);

			if(oldval.size != val.size) { 
				Error("KeyBus: values do not have the right shape.").throw;
			} {
				newvals.put(index, val);	
			};
		};
		values = newvals;
		bus.setn(newvals.flat); // always writes all values.
	}
	
	get { |... keys|
		^keys.collect { |k| values[this.indexOf(k)] }
	}
	
	printOn { |stream| 
		stream << this.class.name << "(" << bus << ")"
	}	
	
	free { bus.free }
	
		// get values from bus 
	poll { |inKeys, action|
		var busvals;
		inKeys = inKeys ? keys; 
		bus.getn(bus.numChannels, { |arr|
			busvals = arr.reshapeLike(values);
			[keys, busvals].flop.do(_.postcs);
			// ... 
		});
	}

	sync { // poll and set values from bus 
	
	}
}


NamedBus : KeyBus { 
	classvar <all;
	var <name; 
	
	*initClass { this.clear }
	*clear { 
		all.do(_.free);
		all = ();
	}

	*new { arg name, keys, values, server;
		var res = this.at(name);

		if (res.isNil) { ^res = super.new(keys, values, server).prAdd(name) };
		if (keys.isNil) { ^res };
			
		if(res.keys.size == keys.size and: { server.isNil or: { server === res.bus.server } })
			{
				res.set(*[keys, values].flop);
			} {
				Error("could not set NamedBus (key: %)".format(name)).throw;
			};
		^res
	
	}
	remove {
		all.removeAt(name);
		this.free;
	}
	
	*at { arg name;
		^this.all.at(name)
	}

	prAdd { arg argName;
		name = argName;
		all.put(argName, this);
	}

}
	
