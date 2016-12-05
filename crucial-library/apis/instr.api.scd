var cb = { arg reply, name, args, atTime, targetStyle, target;
	var b = MixedBundle.new, synth, s = Server.default;

	synth = Instr(name).prMakeSynth(targetStyle, target, args, b, atTime);
	b.addFunction({ reply.value(synth.nodeID) });
	b.sendAtTime(s, atTime);
};


(
	loadAll: { arg reply;
		Instr.loadAll;
		reply.value
	},

	detail: { arg reply, name;
		var instr, args, outSpec;
		name = name.asString;
		instr = Instr.at(name) ?? {Instr.load(name)};
		if(instr.isNil, {
			Error("Instr not found" + name).throw;
		});
		outSpec = instr.outSpec;
		args = instr.argNames.collect({ arg argName, i;
							var spec = instr.specs.at(i);
							(
								name: argName,
								spec: [spec.class, spec.rate] ++ spec.storeArgs,
								default: instr.defArgAt(i) // ? spec.default
							)
						});
		reply.value((
			name: name,
			path: instr.path,
			code: instr.funcDef.sourceCode,
			args: args,
			outSpec: [outSpec.class, outSpec.rate] ++ outSpec.storeArgs,
			rate: instr.rate
		));
	},
	play: { arg reply, name ... args;
		cb.value(reply, name, args, nil, \addToTailMsg, nil);
	},
	after: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addAfterMsg, nil);
	},
	before: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addBeforeMsg, nil);
	},
	head: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addToHeadMsg, nil);
	},
	tail: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addToTailMsg, nil);
	},
	replace: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addReplaceMsg, nil);
	},

	// spawnEvent

	list: { arg reply;
		reply.value(Instr.leaves.collect(_.dotNotation))
	},
	listBySpec: { arg reply, outSpec;
		reply.value(Instr.selectBySpec(outSpec.asSymbol.asSpec).collect(_.dotNotation))
	},

	addSynthDesc: { arg reply, name, args;
		// for patterns
		Instr(name).add(args);
		reply.value  // not properly synced
	}

);
