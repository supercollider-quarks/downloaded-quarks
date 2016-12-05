
IBdef : InfluxBase {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	// inNames = outNames
	*new { |key, inNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.new(inNames, inValDict);
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}

IMdef : InfluxMix {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	// inNames = outNames
	*new { |key, inNames|
		var res = this.at(key);
		if (res.notNil) {
			if (inNames.notNil) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.new(inNames);
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}

ISdef : InfluxSpread {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	*new { |key, inNames, outNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, outNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.new(inNames, outNames, inValDict);
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}
Idef : Influx {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	*new { |key, inNames, outNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, outNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.new(inNames, outNames, inValDict);
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}
