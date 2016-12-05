/* todo Influx:

* implement clip, fold, wrap, compression for outvals
(as in Nudge)

* write a method for making skewed diagonals

* flexify weights
  * allow using weights presets:
  * make basic named ones once when called, then lookup
  * distinguish between known and added new ones;
  * on demand, save new weights to disk.

* implement a crossfade task:
*  xfade to new set of weights,
*  xfade to new offsets
*  xfade to multi-offsets,
*   e.g. locate them at (0.5@0.5), (-0.5 @ -0.5)
* same for 3dim controls

* Examples with Tdef, Pdef
* Example with multitouchpad:
*  new finger gets next ndef/tdef, 3 params (vol, x, y)

* PresetZone - a dense field of lots of presets, morph by distance
* PresetGrid - a grid with presets at each intersection

*/

InfluxBase {
	classvar <outNameOrder;

	var <inNames, <inValDict, <outValDict;
	var <outNames;
	var <>action;

	var <shape, <smallDim, <bigDim;

	*initClass {
		outNameOrder = [23, 24, 25] ++ (22, 21 .. 0);
	}

	*outNameFor { |index|
		// infinite default inNames
		// a b c ... x y z A B C ... X Y Z aa bb cc ... xx yy zz AA BB CC ...
		var charIndex = index % 26;
		var num = index div: 52 + 1;
		var charCase = index div: 26 % 2;

		var char = (charIndex + [97, 65][charCase]).asAscii;
		// [\charIndex, charIndex, \num, num, \charCase, charCase].postln;
		// char.postcs;
		^String.fill(num, char).asSymbol;
	}

	*inNameFor { |index|
		// like outNames, but reordered :
		// x y z w ... b a X Y Z W ... B A xx yy zz ... BB AA XX YY ZZ
		var charIndex = index % 26;
		index = index - charIndex + outNameOrder[charIndex];
		^this.outNameFor(index);
	}

	*makeInNames { |ins|
		// make inNames from number
		if (ins.isKindOf(SimpleNumber)) {
			ins = ins.collect(this.inNameFor(_));
		};
		^ins
	}

	*makeOutNames { |outs|
		// replace with x, y, z, w, v, u ... and a, b, c, ...
		if (outs.isKindOf(SimpleNumber)) {
			outs = outs.collect(this.outNameFor(_));
		};
		^outs
	}

	*new { |inNames = 2, inValDict|
		inNames = this.makeInNames(inNames);
		^super.newCopyArgs(inNames, inValDict)
			.initBase
			.initOuts(inNames)
			.calcOutVals;
	}

		printOn { |receiver, stream|
		^this.storeOn(receiver, stream);
	}

	// needed for influence method -
	// x.putHalo(\key, <myName>);
	key { ^this.getHalo(\key) ? 'anonIB' }

	resetInvals {
		this.set(*inNames.collect([_, 0]).flat);
	}

	prepInvals {
		inValDict = inValDict ?? { () };
		inNames.do { |name|
			if (inValDict[name].isNil) {
				inValDict[name] = 0;
			}
		};
	}

	initBase {
		this.prepInvals;
		action = MFunc.new;
	}

		// overwrite in subclasses
	initOuts { |argOutNames|
		outNames = argOutNames; // here, innames
		outValDict = ();
	}

	doAction { action.value(this) }

		// set input params - ignore unknowns.
	set { |...keyValPairs|
		var doIt = false;
		keyValPairs.pairsDo { |key, val|
			if (inNames.includes(key)) {
				inValDict.put(key, val);
				doIt = true;
			};
		};
		if (doIt) {
			this.calcOutVals;
			this.doAction;
		};
	}

	calcOutVals {
		// just copy them over here;
		// modifications in subclasses
		inValDict.keysValuesDo { |key, val|
			outValDict.put(key, val);
		};
	}

	// basic interface to MFunc,
	// for more complex ordering, use inph.action.addAfter etc.
	add { |name, func| action.add(name, func) }
	remove { |name| action.remove(name) }

	// create simple funcnames based on relevant object
	funcName { |str, obj|
		var objname = if (obj.respondsTo(\key)) {
			obj.key } { action.funcDict.size };
		^(str ++ "_" ++ objname).asSymbol;
	}

	// attach objects directly (i.e. without mapping)
	attachSet { |object, funcName|
		funcName = funcName ?? { this.funcName("set", object) };
		this.add(funcName, { object.set(*this.outValDict.asKeyValuePairs) });
	}

	attachPut { |object, funcName|
		funcName = funcName ?? { this.funcName("put", object) };
		this.add(funcName, { object.putAll(outValDict); });
	}

	attachInfl { |object, funcName|
		funcName = funcName ?? { this.funcName("infl", object) };
		this.add(funcName, {
			object.influence(this.key, *this.outValDict.asKeyValuePairs);
		});
	}

	detach { |name| this.remove(name); }


		// convenience methods //
	    // prettyprint values
	postv { |round = 0.001|
		var str = "\n// " + this + "\n";
		[   ["inVals", inNames, inValDict],
			["outVals", outNames, outValDict]
		].do { |trip|
			var valName, names, vals;
			#valName, names, vals = trip;
			if (names.notNil and: { vals.size > 0}) {
				str = str ++ "\n// x.%: \n(\n".format(valName);
				names.do { |name|
					var val = vals[name];
					if (val.isNumber) { val = val.round(round) };
					if (val.notNil) {
						str = str ++
						"\t%: %,\n".format(name, val)
					};
				};
				str = str ++ ");\n";
			};
		}
		^str
	}

}

Influx :InfluxBase {
	classvar <outFilters;

	var <weights, <presets;
	var <outOffsets, <>inScaler = 1;
	var <outProcs;

	// *initClass {
	// 	outFilters = (
	// 		tanh: _.tanh,
	// 		fold: _.fold2
	// 	)[\tanh].postcs;
	// }

	*new { |ins = 2, outs = 8, vals, weights|
		ins = this.makeInNames(ins);
		outs = this.makeOutNames(outs);

		^super.newCopyArgs(ins, vals)
			.initBase
			.initOuts(outs)
			.initWeights(weights)
			.makePresets
			.calcOutVals;
	}

	init { |outs|
		this.prepInvals;
		action = MFunc.new;
		outNames = this.class.makeOutNames(outs);
		this.calcOutVals;
	}

	initOuts { |outs|
		outNames = outs;
		outValDict = ();
		outNames.do (outValDict.put(_, 0));
		outProcs = ();
		outOffsets = ();
	}

	initWeights { |argWeights|
		if (argWeights.isNil) {
			weights = argWeights ?? { { 0 ! inNames.size } ! outNames.size };
			this.rand;
		} {
			// add size check here
		//	if (weights.shape == [inNames.size, outNames.size])
				weights = argWeights
		};
	}

	calcOutVals {
		weights.do { |line, i|
			var outVal = line.sum({ |weight, j|
				weight * (inValDict[inNames[j]] ? 0) * inScaler;
			});
			outVal = outProcs[\base].value(outVal) ? outVal;
			outValDict.put(outNames[i], outVal);
		};
	}

	deltaFor { |...inDeltas|
		^inDeltas.keep(inValDict.size).collect { |indelta, i|
			weights.collect { |line| line[i] * indelta };
		}.sum
	}

	addProc { |name, func| outProcs.put(name, func); }

	makePresets {

		shape = weights.shape;
		smallDim = shape.minItem;
		bigDim = shape.maxItem;

		presets = ();
		// diagonals
		presets.put(\diagL, weights.collect { |inner, j|
			inner.collect { |el, i|
				if ( (i % smallDim) == (j % smallDim) ) { 1 } { 0 };
			}
		});
		// reverse diag
		presets.put(\diagR, weights.collect { |inner, j|
			inner.collect { |el, i|
				if ( ((i % smallDim) + (j % smallDim)) == (smallDim - 1) ) { 1 } { 0 };
			}
		});

		// skewed diags TBD later, like these:
		// 3 to 5 skewed diagonal
		// [
		// 	[1, 0, 0],
		// 	[0.5, 0.5, 0],
		// 	[0, 1, 0],
		// 	[0, 0.5, 0.5],
		// 	[0, 0, 1]
		// ]
		//
		// [
		// 	[ 1, 0.5, 0, 0, 0 ],
		// 	[ 0, 0.5, 1, 0.5, 0 ],
		// 	[ 0, 0, 0, 0.5, 1 ]
		// ]
	}

	// prettyprint weights
	postw { |round = 0.001|
		var str = "// x.weights:\n[\n";
		weights.do { |line| str = str ++ Char.tab ++ line.round(round) ++ ",\n" };
		str = str ++ "]";
		^str
	}

		// prettyprint presets
	postp { |round = 0.001|
		var str = "// x.presets:\n(\n";
		presets.keysValuesDo { |key, pre|
			str = str ++ key ++ ":";
			pre.do { |line| str = str ++ Char.tab ++ line.round(round) ++ ",\n" };
			str = str ++ "],\n";
		};
		str = str ++ ");\n";
		^str
	}

	// make a plotter that can display and edit weights
	plot { |name, bounds, parent, makeSkip = true, options=#[]|
		^InfluxPlot(this, inNames.size, parent, bounds, makeSkip, options)
		.name_(name);
	}

	center {
		this.set(*inNames.collect([_, 0]).flat);
	}

	// create new random weights
	rand { |maxval = 1.0|
		weights = weights.collect { |row|
			row.collect { maxval.rand2.fold2(1.0) }
		}
	}

	blend { |other, blend = 0.5|
		// any array will be made to fit:
		if (other.shape != shape) { other = other.reshapeLike(weights); };
		weights = weights.collect { |row, j|
			row.collect { |val, i|
				blend(val, other[j][i], blend).fold2(1.0) }
		};
	}

	// modify existing ones:
	entangle { |drift = 1.0|
		weights = weights.collect { |row|
			row.collect { |val, i| (val + drift.rand2).fold2(1.0) }
		}
	}

	disentangle { |blend, presetName|
		var pres = presets[presetName] ? presets[\diagL];
		this.blend(pres, blend);
	}

	setw { | arrays |
		if (arrays.shape == weights.shape) {
			weights = arrays;
		} {
			warn("Influx - new weights have wrong shape: %.\n"
				.format(weights.shape))
		}
	}

	setwPre { |name|
		var pre = presets[name];
		if (pre.notNil) { this.setw(pre) };
	}

	setOffsets { |key, newOffs|
		var insize = newOffs.size;
		var offsize = outOffsets[key].size;
		if (insize < offsize) {
			newOffs = newOffs ++ 0.dup(offsize - insize);
		} {
			if (insize > offsize) {
				newOffs = newOffs.keep(offsize);
			};
		};
		outOffsets.put(key, newOffs);
	}

	offsetsFromProxy { |proxy|
		var setting = proxy.getKeysValues;
		var normVals = setting.collect { |pair|
			proxy.getSpec(pair[0]).unmap(pair[1]);
		};
		this.setOffsets(proxy.key, normVals.unibi);
		^outOffsets;
	}

	offsetsFromPreset { |preset, setName|
		var setting = preset.getSet(setName);
		var normVals = setting.value.collect { |pair|
			preset.proxy.getSpec(pair[0]).unmap(pair[1]);
		};
		this.setOffsets(preset.proxy.key, normVals.unibi);
		^outOffsets;
	}

	attachMapped { |object, funcName, paramNames, specs, proc|
		var mappedKeyValList;
		specs = specs ?? { object.getSpec; };
		funcName = funcName ?? { object.key };
		paramNames = paramNames
		?? { object.getHalo(\orderedNames); }
		?? { object.controlKeys; };

		outOffsets.put(funcName, 0 ! outNames.size);
		outProcs.put(funcName, proc);

		action.addLast(funcName, {
			var myOffsets = outOffsets[funcName];
			var myProc = outProcs[funcName];
			mappedKeyValList = paramNames.collect { |extParName, i|
				var inflOutName = outNames[i];
				var inflVal = outValDict[inflOutName];
				var mappedVal;

				if (inflVal.notNil) {
					inflVal = inflVal + myOffsets[i];
					inflVal = myProc.value(inflVal) ? inflVal;
					mappedVal = specs[extParName].map(inflVal + 1 * 0.5);
					[extParName, mappedVal];
				} { [] }
			};
			object.set(*mappedKeyValList.flat);
		});
	}

	removeMapped { |funcName|
		action.disable(funcName);
	}

	attachNudge { |object, funcName, paramNames, specs, proc|

	}
}
