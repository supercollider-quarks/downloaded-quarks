+ VGMulTree {
	
	deepDo { |function, level = inf, key|
		function.value(this, level, 0, key);
		if(level > 0) {
			branches.do { |branch, key, i|
				branch.deepDo(function, level - 1, i, key);
			}
		};
	}
	
	selectNodes { |function, level = inf|
		var res;
		this.deepDo { |node, level, index, key|
			if(function.value(node, level, index, key)) { res = res.add(node) };
		};
		^res
	}
	
	resetAll { |val = 1|
		this.deepDo { |node|Ê
			node.values = node.values.collect(val);
			if(node.isLeaf) { 
				node.relValues = node.relValues.collect(val)
			};
		};
	}

	values_ { |values|
		^mulBus.values = values;
	}
	
	relValues_ { |values|
		^relBus !? { relBus.values = values }
	}
	
	baseValues_ { |values|
		^baseBus !? { baseBus.values = values }
	}
}

+ KeyBus {

	values_ { |vals|
		if(vals.flat.size != bus.numChannels) {
			Error("values do not have the right shape.").throw;
		};
		values = vals;
		bus.setn(values.flat);
	}

}