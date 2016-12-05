VGMulTree {

	var <>argNames, <>mulBus, <>branches, <>relBus, <>baseBus, <>metaData;
	var <>parent;
		
	*newFromDict { | dict, server, argNames |
		^this.new.initWithDict(dict, server, argNames);
	}
	
	branchNames { 
		^branches.keys.asArray.sort
	}
	
	asDict { | addArgNames = true|
	
		^(
			specs: this.values,
			relSpecs: this.relValues,
			baseSpecs: this.baseValues,
			branches: branches.collect(_.asDict(false)),
			metaData: metaData,
			argNames: if(addArgNames) { argNames }
		);
		
	}
	
	
	initWithDict { | dict, server, inArgNames |
		var specs, relSpecs, baseSpecs; 
		
		dict = dict.deepCopy;
		
		argNames = inArgNames ? dict[\argNames];
		specs = dict[\specs]; 
		relSpecs = dict[\relSpecs]; 
		baseSpecs = dict[\baseSpecs]; 
		
		if(argNames.isNil) {
			Error("VGMulTree dict got no argNames.").throw; 
		};
		if (specs.isNil) { 
			Error("VGMulTree dict got no specs.").throw; 
		}; 
		if (specs.size != argNames.size) { 
			Error("VGMulTree dict has wrong spec size.").throw; 
		};
		
		mulBus = KeyBus(argNames, dict[\specs], server);
		
		branches = dict[\branches].collect { |subdict|
			this.class.newFromDict(subdict, server, argNames).parent_(this)
		};
		
		baseSpecs = dict[\baseSpecs];
		relSpecs = dict[\relSpecs];
		if (baseSpecs.notNil and: relSpecs.notNil) { 
			if (baseSpecs.size != argNames.size) { 
				Error("VGMulTree has wrong baseSpecs size.").throw; 
			}; 
			if (relSpecs.size != argNames.size) { 
				Error("VGMulTree has wrong relSpecs size.").throw; 
			}; 
			relBus = KeyBus(argNames, relSpecs, server);
			baseBus = KeyBus(argNames, baseSpecs, server) 
		};
		metaData = dict[\metaData] ? ();
	}
	
	setDict { |dict|
		var specs, relSpecs, baseSpecs;
		
		// does not test for consistency of argNames.
		
		if(dict.isNil) {Ê^this };
		
		dict = dict.deepCopy;
		
		specs = dict[\specs]; 
		relSpecs = dict[\relSpecs]; 
		baseSpecs = dict[\baseSpecs]; 
		
		if (specs.isNil) { 
			Error("VGMulTree dict got no specs.").throw; 
		}; 
		if (specs.size != argNames.size) { 
			Error("VGMulTree dict has wrong spec size.").throw; 
		};
		
		this.set(*[argNames, specs].flop.flatten(1)); 
		
		dict[\branches] !? {
			dict[\branches].keysValuesDo { |key, subdict|
				var tree = branches[key];
				tree !? { tree.setDict(subdict) };
			};
		};
		
		if (baseSpecs.notNil and: relSpecs.notNil) { 
			if (baseSpecs.size != argNames.size) { 
				Error("VGMulTree has wrong baseSpecs size.").throw; 
			}; 
			if (relSpecs.size != argNames.size) { 
				Error("VGMulTree has wrong relSpecs size.").throw; 
			}; 
			this.setRel(*[argNames, relSpecs].flop.flatten(1)); 
			this.setBase(*[argNames, baseSpecs].flop.flatten(1));
		};
		metaData = dict[\metaData] ? metaData;
	}
	
	values {
		^mulBus.values
	}
	
	relValues {
		^relBus !? { relBus.values }
	}
	baseValues {
		^baseBus !? { baseBus.values }
	}
	
	index { ^mulBus.index }
	relIndex { ^relBus !? { relBus.index } }
	baseIndex { ^baseBus !? { baseBus.index } }
	
	isLeaf {
		^baseBus.notNil
	}

	isSonator {
		^this.isLeaf
	}

	set { |...keysVals| 
		mulBus.set(*keysVals)
	}
	
	setBase { |... keysVals|
		baseBus.set(*keysVals)
	}
	
	setRel { |... keysVals|
		relBus.set(*keysVals)
	}

	nodeAt { |...keys|
		var nextKey, node;
		
		if(keys.notEmpty and: { branches.notNil }) { 
			nextKey = keys.removeAt(0);
			node = branches[nextKey];
			if(node.isNil) { 
				warn("no such branch: %.\n".format(nextKey));
				^nil 
			};
			^node.nodeAt(*keys);
		} { 
			^this 
		};
	}
	
	calcAt { |...keys|
		^this.collectNodes(keys).collect(_.values).product;
	}
	
	calcAtLeaf {|...keys|
		var nodes = this.collectNodes(keys), allSpecs;
		if(nodes.last.isLeaf.not) { Error("no leaf at this path").throw };
		allSpecs = nodes.collect(_.values)
			.add(nodes.last.relValues)
			.add(nodes.last.baseValues);
			
		^allSpecs.product
	}
		
	asSynthParams { |...keys| 
		^[this.argNames, this.calcAtLeaf(keys)].flop.flatten(1);
	}
	
	bussesFor { |keys, busNames, addBase=true| 
		var lastBusses ;
		var nodes = this.collectNodes(keys);
		var busses = nodes.collect { |node| node.mulBus.bus };

		if (addBase) { 
			lastBusses = [nodes.last.relBus.bus, nodes.last.baseBus.bus]; 
			if (lastBusses.includes(nil)) { Error("VGMulTree: no baseIndex found!").throw };
			busses = busses.addAll(lastBusses);
		};

		if (busNames.size != busses.size) { 
			 Error("VGMulTree: busNames.size and busses.size do not match!").throw 
		};

		^[busNames, busses].flop.flat;
	}
	
	collectSubNodes { |keys, result|
		var nextKey, node;
		^if(keys.notEmpty and: { branches.notNil }) {
			nextKey = keys.removeAt(0);	
			node = branches[nextKey];
			if(node.isNil) { Error("VGMulTree: no such branch: %.\n".format(nextKey)).throw };
			result = result.add(node);
			node.collectNodes(keys, result) 
		}
	}
	
	collectParentNodes {
		var result;
		^if(parent.isNil) { nil } { parent.collectParentNodes.add(this) }
	}
	
	collectNodes { |keys, result|
		^[this] ++ this.collectSubNodes(keys)
	}
//	
//	storeArgs {
//		var args = [specs, branches];
//		baseSpecs !? { args = args.add(baseSpecs) };
//		^args
//	}
	
	printOn { |stream|
		stream << this.class.name << "(" << this.values << ", " 
			<< this.relValues << ", " << this.baseValues << ")"
	}
}




