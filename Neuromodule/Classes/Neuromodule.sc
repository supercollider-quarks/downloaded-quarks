Neuromodule : Stream {

	/*
	x: one-dimensional array, with init values
	theta: one-dimensional array, with inputs
	weight: two-dimensional array with weights from ... to
	outs: if not nil, select those indices as a return for calling the next method
	*/

	var <>x, <>theta, <>weights, <>outs;

	*new { |x, theta, weights, outs|
		^super.newCopyArgs(x.asArray, theta.asArray, weights.asArray, outs)
	}

	fillMissing {
		var n = this.numCells;
		theta = theta.extend(n, 0.0);
		weights = weights.flatten(1).extend(n.squared, 0.0).reshape(n, n).postcs
	}

	numCells {
		^x.size
	}

	// this is the most important method, calculating the next step and returning the x values
	next { |inval|
		var n = this.numCells;
		x = (0..n-1).collect { |i|
			theta.wrapAt(i).value(inval) + (0..n-1).sum { |j|
				weights.wrapAt(i).wrapAt(j).value(inval) * tanh(x.at(j))
			}
		};
		^if(outs.notNil) { x[outs] } { x }
	}

	// make an independent copy of the module
	copy {
		^this.class.new(x, theta, weights, outs)
	}

	// combine two modules without any rewiring

	merge { |neuromodule|
		^this.copy.addAll(neuromodule)
	}

	addAll { |neuromodule|
		var n1 = this.numCells;
		var n2 = neuromodule.numCells;
		x = x.addAll(neuromodule.x);
		theta = theta.addAll(neuromodule.theta);
		weights = weights.collect { |x| x ++ (0.0).dup(n2) };
		weights = weights ++ neuromodule.weights.collect { |x| (0.0).dup(n1) ++ x };
		if(neuromodule.outs.notNil) {
			outs = outs ?? { (0..n1-1) };
			outs = outs.asArray ++ (neuromodule.outs + n1);
		}
	}

	rewire { |triples, func, postWarnings = true| // triples: e.g. [[4, 5, 0.3], [5, 4, -0.2]]
		triples.do { |triple|
			var from, to, weight, node;
			if(triple.size !=3) { triples.postln; "triples should be an array of triples.".error; };
			#from, to, weight = triple;
			node = weights[from];
			if(node.notNil and: { to <= node.lastIndex }) {
				node[to] = if(func.isNil) {  weight } { func.(node[to], weight, from, to) };
			} {
				if(postWarnings) {
					"couldn't rewire neuromodule, dimension mismatch:".warn;
					triple.postln;
					this.postln;
				}
			}
		}
	}


	// for printing the source code of a module, we need to know what arguments it takes when created.
	// try with module.postcs
	storeArgs {
		var args = [x, theta, weights];
		if(outs.notNil) { args = args.add(outs) };
		^args
	}

	// always post fullcompile string
	printOn { |stream|
		stream <<< this
	}

	dotStructure {
		var str = "";
		str = str ++ "digraph G {\n";
		x.size.do { |i|
			str = str ++ "\t% [label=\"% (θ: %, x: %)\"];\n".format(i, i, theta[i], x[i]);
			weights.at(i).do { |w, j|
				if(w != 0.0) {
					str = str ++ "\t\t% -> % [label=\"%\"];\n".format(i, j, w)
				};
			};

		};
		str = str ++ "}";
		^str

	}

}

Pneuromodule : Pattern {
	var <>x, <>theta, <>weights, <>outs;

	*new { |x, theta, weights, outs|
		^super.newCopyArgs(x.asArray, theta.asArray, weights.asArray, outs)
	}

	numCells {
		^x.size
	}

	embedInStream { |inval|
		var thetaStr = theta.collect { |x| x.asStream };
		var weightsStr = theta.collect{ |x| x.asArray.collect { |y| y.asStream } };
		var n;
		var xVal = x;
		loop {
			n = this.numCells;
			xVal = (0..n-1).collect { |i|
				var thetaVal = thetaStr.wrapAt(i).next(inval);
				if(thetaVal.isNil) { nil.alwaysYield };
				thetaVal + (0..n-1).sum { |j|
					var weightsVal = weightsStr.wrapAt(i).wrapAt(j).next(inval);
					if(weightsVal.isNil) { nil.alwaysYield };
					weightsVal * tanh(xVal.at(j))
				}
			};
			if(outs.notNil) {
				inval = xVal.at(outs).yield;
			} {
				inval = xVal.yield
			}
		}
	}

}


/*

a = Neuromodule([0, 0], [2, -1], [[-3, 2], [-2, 0]]);
a.nextN(8);

a = Pneuromodule([0, 0], [2, -1], [[-3, 2], [-2, 0]]).asStream;

a.nextN(8)
*/