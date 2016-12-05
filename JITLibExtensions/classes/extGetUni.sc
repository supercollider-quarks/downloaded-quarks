
+ NodeProxy {

	mapPairs { |pairs, bipolar = false|
		var mappedPairs = [];
		pairs.pairsDo { |param, val, i|
			var spec = this.getSpec(param);
			if (spec.notNil) {
				if(bipolar) { val = val.biuni };
				mappedPairs = mappedPairs ++ [param, spec.map(val)];
			} {
				(this.asString + thisMethod.asString + ":\n"
					+ "no spec found for %.\n").postf(\key);
			};
		};
		^mappedPairs;
	}

	setUni { |...args| this.set(*this.mapPairs(args)); }
	setBi { |...args| this.set(*this.mapPairs(args, bipolar: true)); }


	getUni { |name, val|
		var normVal;
		var spec = this.getSpec(name);
		val = val ?? { this.get(name) };
		if (val.notNil and: { spec.notNil }) {
			normVal = spec.unmap(val);
		};

		^normVal
	}

	getUnis { |names| ^names.collect (this.getUni(_)).unbubble }

}

+ PatternProxy {

	mapPairs { |pairs, bipolar = false|
		var mappedPairs = [];
		pairs.pairsDo { |param, val, i|
			var spec = this.getSpec(param);
			if (spec.notNil) {
				if(bipolar) { val = val.biuni };
				mappedPairs = mappedPairs ++ [param, spec.map(val)];
			} {
				(this.asString + thisMethod.asString + ":\n"
					+ "no spec found for %.\n").postf(\key);
			};
		};
		^mappedPairs;
	}

	setUni { |...args| this.set(*this.mapPairs(args)); }
	setBi { |...args| this.set(*this.mapPairs(args, bipolar: true)); }

	getUni { |name, val|
		var normVal;
		var spec = this.getSpec(name);
		val = val ?? { this.get(name) };
		if (val.notNil and: { spec.notNil }) {
			normVal = spec.unmap(val);
		};

		^normVal
	}
	getUnis { |names| ^names.collect (this.getUni(_)).unbubble }

}

+ NPVoicer {
	setUniAt { | index ... args |
		proxy.setAt(index, *proxy.mapPairs(args));
 	}
}
