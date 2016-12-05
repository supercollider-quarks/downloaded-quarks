var current;

Proto {
	~begin = nil;
	~end = nil;
	~selectionStart = { ~begin };
	~selectionSize = { ~end - ~begin + 1 };
	~siblings = {
		if(~parentNode.notNil) { ~parentNode.children };  // else nil
	};
	~index = {
		var sibs = ~siblings.();
		if(sibs.notNil) { sibs.indexOf(currentEnvironment) };  // else nil
	};
	~nearbySib = { |incr = 1|
		var sibs = ~siblings.(), i;
		if(sibs.notNil) {
			i = sibs.indexOf(currentEnvironment);
			if(i.notNil) {
				sibs[i + incr]  // may be nil
			}
		};  // else nil
	};
	~setLastSelected = {
		var i = ~index.();
		if(i.notNil) { ~parentNode.tryPerform(\lastSelected_, i) };
		currentEnvironment
	};

	~prep = { |stream, parentNode|
		~parentNode = parentNode;
		~children = Array.new;
		// if(stream.peek == $/) { stream.next };
		~begin = stream.pos;
		~parse.(stream);
		if(~end.isNil) { ~end = stream.pos - 1 };
		if(~string.isNil) {
			if(~end >= ~begin) {
				~string = stream.collection[~begin .. ~end]
			} {
				~string = String.new;
			};
		};
		currentEnvironment
	};

	// utility function
	~unquoteString = { |str, pos = 0, delimiter = $", ignoreInParens(false)|
		var i = str.indexOf(delimiter), j, escaped = false, parenCount = 0;
		if(i.isNil) {
			str
		} {
			j = i;
			while {
				j = j + 1;
				j < str.size and: {
					escaped or: { str[j] != delimiter }
				}
			} {
				switch(str[j])
				{ $\\ } { escaped = escaped.not }
				{ $( } {
					if(ignoreInParens) {
						parenCount = parenCount + 1;
						escaped = true;
					} {
						escaped = false;
					};
				}
				{ $) } {
					if(ignoreInParens) {
						parenCount = parenCount - 1;
						if(parenCount < 0) {
							"unquoteString: paren mismatch in '%'".format(str).warn;
						} {
							escaped = parenCount > 0;
						};
					} {
						escaped = false;
					};
				}
				{
					if(ignoreInParens.not or: { parenCount <= 0 }) {
						escaped = false;
					};
				}
				// if(str[j] == $\\) { escaped = escaped.not } { escaped = false };
			};
			if(j - i <= 1) {
				String.new  // special case: two adjacent quotes = empty string
			} {
				str[i + 1 .. j - 1];
			};
		};
	};

	~idAllowed = "_";
	~getID = { |stream, skipSpaces(true)|
		var str = String.new, ch, begin;
		if(skipSpaces) { ~skipSpaces.(stream) };
		begin = stream.pos;
		while {
			ch = stream.next;
			ch.notNil and: { ch.isAlphaNum or: { ~idAllowed.includes(ch) } }
		} {
			str = str.add(ch);
		};
		if(ch.notNil) { stream.pos = stream.pos - 1 };
		PR(\clStringNode).copy
		.put(\parentNode, currentEnvironment)
		.put(\string, str)
		.put(\begin, begin)
		.put(\end, begin + str.size - 1);
	};
	~skipSpaces = { |stream|
		var ch;
		while {
			ch = stream.next;
			ch.notNil and: { ch.isSpace }
		};
		if(ch.notNil) { stream.pos = stream.pos - 1 };
	};

	~streamCode = { |stream| stream << ~string; currentEnvironment };
	~setTime = { |onset(0), dur(4)|
		~time = onset;
		~dur = dur;
		currentEnvironment
	};
	~isSpacer = false;
} => PR(\abstractClParseNode);

PR(\abstractClParseNode).clone {
	// assumes you've skipped the opening delimiter
	~parse = { |stream|
		var str = String.new, ch;
		while {
			ch = stream.next;
			ch.notNil and: { ~endTest.(ch).not }
		} {
			str = str.add(ch);
		};
		if(ch.notNil) { stream.pos = stream.pos - 1 };
		str
	};
	~endTest = { |ch| ch == $\" };
	~symbol = { ~string.asSymbol };
	~openDelimiter = "";
	~closeDelimiter = "";
	~streamCode = { |stream|
		stream << ~openDelimiter << ~string << ~closeDelimiter;
		currentEnvironment
	};
	~isSpacer = { ~string.every(_.isSpace) };
} => PR(\clStringNode);

PR(\clStringNode).clone {
	~streamCode = { |stream|
		stream << "(time: " << ~time << ", dur: " << ~dur << ", item: ";
		if(~isPitch ?? { false }) {
			stream <<< ~decodePitch.(~string);
		} {
			if(~string.size == 1) {
				stream <<< ~string[0];
			} {
				stream <<< ~string;
			};
		};
		stream << ")";
		currentEnvironment
	};
}.import((clPatternSet: #[decodePitch])) => PR(\clEventStringNode);

PR(\abstractClParseNode).clone {
	~types = [
		Float -> "[\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?",
		Integer -> "(-[0-9]+|[0-9]+)"
	];
	~parse = { |stream|
		// hack into CollStream -- will fail with other IO streams
		var match,
		type = ~types.detect { |assn|
			match = stream.collection.findRegexpAt(assn.value, stream.pos);
			match.notNil
		};
		if(match.notNil) {
			~string = stream.nextN(match[1]);  // match[1] = length of match
			~value = ~string.perform(("as" ++ type.key.name).asSymbol);
		};  // else leave state variables nil
	};
} => PR(\clNumberNode);

PR(\abstractClParseNode).clone {
	~isPitch = false;
	~endChars = "|\"";
	~parse = { |stream|
		var ch;
		// stream.collection[stream.pos .. stream.pos + 10].debug(">> clDividerNode");
		~items = Array.new;
		while {
			ch = stream.next;
			ch.notNil and: { ~endChars.includes(ch).not }
		} {
			~items = ~items.add(~parseItem.(stream, ch));
		};
		if(ch.notNil) { stream.pos = stream.pos - 1 };
		// stream.collection[~begin .. ~begin + 10].debug("<< clDividerNode");
	};

	~parseItem = { |stream, ch|
		var new, begin, colonCount, rewindPos;
		// [ch.asCompileString, stream.collection[stream.pos .. stream.pos + 10]].debug(">> parseItem");
		if(ch.isNil) { ch = stream.next };
		begin = stream.pos - 1;
		case
		{ ch == $\\ } {
			stream.pos = stream.pos - 1;
			new = PR(\clGeneratorNode).copy
			.putAll((bpKey: ~bpKey, isPitch: ~isPitch, isMain: ~isMain, parm: ~parm))
			.prep(stream, currentEnvironment);
			if(stream.peek == $:) {
				colonCount = 0;
				rewindPos = stream.pos;
				while { stream.next == $: } { colonCount = colonCount + 1 };
				if(colonCount == 2) {
					stream.pos = stream.pos - 1;  // need next to be the backslash
					new = PR(\clChainNode).copy
					.putAll((bpKey: ~bpKey, isPitch: ~isPitch, isMain: ~isMain, parm: ~parm))
					.prep(stream, currentEnvironment, new);  // 3rd arg is first item in the chain
				} {
					Error("'::' syntax, wrong number of colons").throw;
				};
			};
			~children = ~children.add(new);
			new//.debug("<< parseItem");
		}
		{ ~isPitch } {
			if(ch.isDecDigit) {
				new = String.with(ch);
				while { (ch = stream.next).notNil and: { "+-',~_.>".includes(ch) } } {
					new = new.add(ch);
				};
				if(ch.notNil) { stream.pos = stream.pos - 1 };
				~children = ~children.add(
					PR(\clEventStringNode).copy
					.put(\parentNode, currentEnvironment)
					.put(\string, new)
					.put(\isPitch, ~isPitch)
					.put(\begin, begin)
					.put(\end, begin + new.size - 1);
				);
				new//.debug("<< parseItem");
			} {
				new = String.with(ch);
				~children = ~children.add(
					PR(\clEventStringNode).copy
					.put(\parentNode, currentEnvironment)
					.put(\string, new)
					.put(\isPitch, ~isPitch)
					.put(\begin, begin)
					.put(\end, begin);  // one char only
				);
				new//.debug("<< parseItem");
			};
		} {
			new = String.with(ch);
			~children = ~children.add(
				PR(\clEventStringNode).copy
				.put(\parentNode, currentEnvironment)
				.put(\string, new)
				.put(\isPitch, ~isPitch)
				.put(\begin, begin)
				.put(\end, begin);
			);
			new//.debug("<< parseItem");
		};
	};

	~streamCode = { |stream|
		var needComma = false;
		if(~children.notEmpty) {
			if(~children[0][\time].isNil) { ~setTime.(~time, ~dur) };
			// no array brackets: divider delimiters are for humans, not machines
			~children.do { |item, i|
				if(item.isSpacer.not) {
					if(needComma) { stream << ", " };
					item.streamCode(stream);
					needComma = true;
				};
			};
		};
		currentEnvironment
	};
	~setTime = { |onset(0), dur(4), extraDur(0)|
		var itemDur = dur / max(~children.size, 1), durs, lastI;
		~time = onset;
		~dur = dur;
		// children should be clStringNodes or clGeneratorNodes
		durs = Array(~children.size);
		~children.do { |item, i|
			if(item.isSpacer and: { lastI.notNil }) {
				durs[lastI] = durs[lastI] + itemDur;
			} {
				lastI = i;
			};
			durs.add(itemDur);
		};
		if(lastI.notNil) {
			durs[lastI] = durs[lastI] + extraDur;
		};
		~children.do { |item, i|
			item.setTime(onset + (i * itemDur), durs[i]);
		};
	};
} => PR(\clDividerNode);

PR(\abstractClParseNode).clone {
	~isPitch = false;
	~types = [
		(type: \clRhythmGenNode, regexp: "^:[a-zA-Z0-9_]+\\(.*\\)"),
		(type: \clGeneratorNode, regexp: "^\\\\[a-zA-Z0-9_]+\\(.*\\)"),
		// more specific "" test must come first
		(type: \clStringNode, regexp: "^\"[._~]+\"",
			match: { ~isPitch ?? { false } },
			endTest: { |ch| "~_.\"".includes(ch).not }
		),
		(type: \clPatStringNode, regexp: "^\".*\""),
		(type: \clNumberNode, regexp: "^-?[0-9]"),
		(type: \clStringNode, regexp: "^`[A-Za-z0-9_]+",
			endTest: { |ch| not(ch.isAlphaNum or: { "_`".includes(ch) }) },
			openDelimiter: $', closeDelimiter: $',
			pre: { |stream| stream.next }  // drop ` intro
		),
		(type: \clStringNode, regexp: "^,",  // empty arg
			endTest: { |ch| ch == $, }
		)
	];
	~extras = #[endTest, openDelimiter, closeDelimiter];
	~parse = { |stream|
		var name, ch;
		// stream.collection[stream.pos .. stream.pos + 10].debug(">> clGeneratorNode");
		if(stream.peek == $\\) { stream.next };
		name = ~getID.(stream);
		~children = ~children.add(name);
		~name = name.string;
		ch = stream.next;
		if(ch == $() {
			while {
				ch = stream.next;
				ch.notNil and: { ch != $) }
			} {
				stream.pos = stream.pos - 1;
				~children = ~children.add(~parseArg.(stream));
			};
			// if(ch.notNil) { stream.pos = stream.pos - 1 };
		} {
			// gen refs
			Error("Generator '%' has no argument list".format(~name)).throw;
		};
		// stream.collection[~begin .. ~begin + 10].debug("<< clGeneratorNode");
	};
	~parseArg = { |stream|
		var type, ch, new;
		// stream.collection[stream.pos .. stream.pos + 10].debug(">> parseArg");
		type = ~types.detect { |entry|
			(entry[\match].value ?? { true }) and: {
				entry[\regexp].matchRegexp(stream.collection, stream.pos);
			}
		};
		if(type/*.debug("type")*/.notNil) {
			type[\pre].value(stream);
			new = PR(type.type).copy
			.putAll((bpKey: ~bpKey, isPitch: ~isPitch, isMain: ~isMain, parm: ~parm));
			~extras.do { |key|
				if(type[key].notNil) {
					new.put(key, type[key]);
				};
			};
			new.prep(stream, currentEnvironment);
		} {
			Error("Syntax error in % arg list".format(~name)).throw;
		};
		ch = stream.next;
		if(ch == $,) { ~skipSpaces.(stream) } {
			if(ch.notNil) { stream.pos = stream.pos - 1 };
		};
		// new.listVars; "<< parseArg".debug;
		new
	};
//PR('clGenTest').copy.putAll((bpKey: 'x', args: [[$., $., $., $.]], dur: 4.0, isPitch: false, isMain: true, parm: 'a')).prep

	~streamCode = { |stream|
		var name = ~children[0].string;
		if(~children[1].isNil or: { ~children[1][\time].isNil }) { ~setTime.(~time, ~dur) };
		stream << "PR(\\clGen";
		if(name.size > 0) {
			stream << name[0].toUpper << name[1..];
		};
		stream << ").copy.putAll((";
		stream << "bpKey: " <<< ~bpKey;
		stream << ", args: [ ";
		forBy(1, ~children.size - 1, 1) { |i|
			if(i > 1) { stream << ", " };
			if(~children[i].string == "") {
				stream << "nil"
			} {
				~children[i].streamCode(stream);
			};
		};
		stream << " ], dur: " << ~dur << ", time: " << ~time;
		stream << ", isPitch: " << (~isPitch ?? { false });
		stream << ", isMain: " << (~isMain ?? { false });
		stream << ", parm: " <<< ~parm;
		stream << ")).prep";
		currentEnvironment
	};
	~setTime = { |onset(0), dur(4)|
		var itemDur = dur / max(~children.size, 1);
		~time = onset;
		~dur = dur;
		forBy(1, ~children.size - 1, 1) { |i|
			~children[i].setTime(onset, dur);  // meaningful for gens and patstrings
		};
		currentEnvironment
	};
} => PR(\clGeneratorNode);

PR(\abstractClParseNode).clone {
	// special constructor -- you know what kind of node you're creating
	// assumes stream.next will be the second generator
	~prep = { |stream, parentNode, leftNode|
		if(stream.peek != $\\) {
			Error("'::' syntax is valid between two generators only").throw;
		};
		~parentNode = parentNode;
		leftNode.parentNode = currentEnvironment;
		~children = [leftNode];
		// if(stream.peek == $/) { stream.next };
		~begin = leftNode.begin; // stream.pos;
		~parse.(stream);
		if(~end.isNil) { ~end = stream.pos - 1 };
		if(~string.isNil) {
			if(~end >= ~begin) {
				~string = stream.collection[~begin .. ~end]
			} {
				~string = String.new;
			};
		};
		currentEnvironment
	};

	~parse = { |stream|
		var new, continue = true, rewindPos, colonCount;
		// stream.collection[stream.pos .. stream.pos + 10].debug(">> clChainNode");
		while { continue } {
			if(stream.peek == $\\) {
				new = PR(\clGeneratorNode).copy
				.putAll((bpKey: ~bpKey, isPitch: ~isPitch, isMain: ~isMain, parm: ~parm))
				.prep(stream, currentEnvironment);
				~children = ~children.add(new);
				if(stream.peek == $:) {
					rewindPos = stream.pos;
					colonCount = 0;
					while { stream.next == $: } { colonCount = colonCount + 1 };
					switch(colonCount)
					{ 2 } {
						stream.pos = stream.pos - 1;
					}
					{ 0 } {
						stream.pos = rewindPos;
						continue = false;
					}
					{ Error("'::' syntax, wrong number of colons").throw };
					if(stream.peek != $\\) {
						Error("'::' syntax is valid between two generators only").throw;
					};
				} {
					continue = false;
				};
			} {
				continue = false;
			}
		};
		// stream.collection[stream.pos .. stream.pos + 10].debug("<< clChainNode");
	};
	~streamCode = { |stream|
		if(~children[0][\time].isNil) { ~setTime.(~time, ~dur) };
		stream << "PR(\\clGenChain).copy.putAll((";
		stream << "bpKey: " <<< ~bpKey;
		stream << ", args: [ ";
		~children.do { |child, i|
			if(i > 0) { stream << ", " };
			child.streamCode(stream);
		};
		stream << " ], dur: " << ~dur << ", time: " << ~time;
		stream << ", isPitch: " << (~isPitch ?? { false });
		stream << ", isMain: " << (~isMain ?? { false });
		stream << ", parm: " <<< ~parm;
		stream << ")).prep";
		currentEnvironment
	};
	~setTime = { |onset(0), dur(4)|
		var itemDur = dur / max(~children.size, 1), i, extraDur = 0, first;
		~time = onset;
		~dur = dur;
		~children.do { |item| item.setTime(onset, dur) };
		currentEnvironment
	};
} => PR(\clChainNode);

PR(\clGeneratorNode).clone {
	~superParse = ~parse;
	~parse = { |stream|
		if(stream.peek == $:) { stream.next };
		~superParse.(stream);
	};
} => PR(\clRhythmGenNode);

PR(\abstractClParseNode).clone {
	~isPitch = false;
	~parse = { |stream|
		var str = String.new, ch, didOpenQuote = false;
		// stream.collection[stream.pos .. stream.pos + 10].debug(">> clPatStringNode");
		while {
			ch = stream.next;
			ch.notNil and: { didOpenQuote.not or: { ch != $\" } }
		} {
			~children = ~children.add(
				PR(\clDividerNode).copy
				.putAll((bpKey: ~bpKey, isPitch: ~isPitch, isMain: ~isMain, parm: ~parm))
				.prep(stream, currentEnvironment)
			);
			didOpenQuote = true;
		};
		// stream.collection[~begin .. ~begin + 10].debug("<< clPatStringNode");
	};
	~streamCode = { |stream|
		var needComma = false;
		if(~children[0][\time].isNil) { ~setTime.(~time, ~dur) };
		stream << "[ ";
		~children.do { |item, i|
			if(item.children.size > 0) {
				if(needComma) { stream << ", " };
				item.streamCode(stream);
				needComma = true;
			};
		};
		stream << " ]";
		currentEnvironment
	};
	~setTime = { |onset(0), dur(4)|
		var itemDur = dur / max(~children.size, 1), i, extraDur = 0, first;
		~time = onset;
		~dur = dur;
		// all children should be divider nodes
		// what about items spanning a division? reverse order
		i = ~children.size - 1;
		~children.reverseDo { |item|
			var itemOnset = onset + (i * itemDur);
			item.setTime(itemOnset, itemDur, extraDur);
			if(item.children.size == 0) {
				extraDur = extraDur + itemDur;
			} {
				first = item.children.detect { |ch| ch.isSpacer.not };
				if(first.notNil) {
					extraDur = first.time - itemOnset;
				} {
					extraDur = extraDur + itemDur;
				}
			};
			i = i - 1;
		};
		currentEnvironment
	};
} => PR(\clPatStringNode);

PR(\abstractClParseNode).clone {
	~clClass = BP;
	~objExists = false;
	~phrase = \main;
	~parm = nil;  // filled in in 'parse'

	~idAllowed = "_*";
	~parse = { |stream|
		var i, ids, test, sym, temp, broke;

		broke = block { |break|
			// class (I expect this won't be used often)
			test = ~getID.(stream);
			~children = ~children.add(test);
			if(test.string.first.isUpper) {
				~clClass = test.symbol.asClass;
				if(stream.next != $.) { break.(true) };
				test = ~getID.(stream);
				~children = ~children.add(test);
			};

			// chucklib object key
			~objKey = test.symbol;  // really? what about array types?
			if(~clClass.exists(~objKey)) {
				~objExists = true;
			};
			if(stream.next != $.) { break.(true) };
			test = ~getID.(stream);
			~children = ~children.add(test);

			// phrase name
			test = test.string;
			if(test.size == 0) {
				~phrase = \main;
			} {
				i = test.indexOf($*);
				if(i.notNil) {
					temp = test[i+1 .. ];
					if(temp.notEmpty and: temp.every(_.isDecDigit)) {
						~numToApply = temp.asInteger;
					} {
						"%: Invalid apply number".format(test).warn;
					};
					~phrase = test[ .. i-1].asSymbol;
				} {
					~phrase = test.asSymbol;  // really? what about array types?
				};
			};
			if(stream.next != $.) { break.(true) };
			test = ~getID.(stream);
			~children = ~children.add(test);

			// parameter name
			~parm = test.string;
			false;
		};
		if(~parm.size == 0) {
			if(~objExists) {
				~parm = ~clClass.new(~objKey)[\defaultParm] ?? { \main };
			};
		} {
			~parm = ~parm.asSymbol;
		};
		if(broke) { stream.pos = stream.pos - 1 };
		currentEnvironment
	};

	~streamCode = { |stream|
		"clIDNode:streamCode not yet implemented".warn;
		stream << "IDNode";
		currentEnvironment
	};
} => PR(\clIDNode);

PR(\abstractClParseNode).clone {
	~parse = { |stream|
		var str = String.new, ch;
		while {
			ch = stream.next;
			ch.notNil and: { ch != $" }
		} {
			str = str.add(ch);
		};
		if(ch.notNil) { stream.pos = stream.pos - 1 };
		~string = str;
		~additiveRhythm = str[0] == $+;
		~quant = str[~additiveRhythm.asInteger ..].interpret;
	};
} => PR(\clPatStringQuantNode);

PR(\abstractClParseNode).clone {
	~isPitch = false;
	~hasQuant = false;
	// getters, because ~children positions may vary
	~idNode = { ~children[0] };
	~quantNode = {
		if(~hasQuant) { ~children[1] } { nil };
	};
	~patStringNode = {
		if(~hasQuant) { ~children[2] } { ~children[1] };
	};
	// caller needs to wrap the outermost patStringNode in a generatorNode: need setter
	~patStringNode_ = { |node|
		if(~hasQuant) {
			~children[2] = node;
		} {
			~children[1] = node;
		};
		currentEnvironment
	};
	~parse = { |stream|
		var id, obj;
		if(stream.peek == $/) { stream.next };
		id = PR(\clIDNode).copy.prep(stream, currentEnvironment);
		~children = ~children.add(id);
		~skipSpaces.(stream);
		if(stream.peek == $=) {
			stream.next;
		} {
			Error("clPatternSet must have '='").throw;
		};
		~skipSpaces.(stream);
		if(stream.peek == $() {
			Error("Composite patterns not refactored yet").throw;
		};
		if(stream.peek.isDecDigit or: { stream.peek == $+ }) {
			~children = ~children.add(PR(\clPatStringQuantNode).copy.prep(stream));
			~hasQuant = true;
		};
		if(stream.peek == $\") {
			if(id.clClass.exists(id.objKey)) {
				obj = id.clClass.new(id.objKey);
				try {
					~isPitch = obj.parmIsPitch(id.parm) ?? { false };
					~isMain = id.parm == obj.defaultParm ?? { false };
				};
			};
			~children = ~children.add(
				PR(\clPatStringNode).copy
				.putAll((bpKey: id.objKey, isPitch: ~isPitch, isMain: ~isMain, parm: id.parm))
				.prep(stream, currentEnvironment)
			);
		}
	};

	// ~setPattern = { |phrase, parm, inParm, pattern, inString, newQuant| };

	// maybe caller should be responsible for this
	~streamCode = { |stream|
		// var id = ~children[0];
		// stream << id.clClass.name << "(" <<< id.name << ").setPattern(";
		// stream <<< id.phrase << ", "
		currentEnvironment
	};
	~setTime = { |onset(0), dur(4)|
		~time = onset;
		~dur = dur;
		~children[1].setTime(onset, dur);  // [1] is the patstring
		currentEnvironment
	};
} => PR(\clPatternSetNode);



// set nodeType tags
current = thisProcess.nowExecutingPath.basename;
PR.all.do { |pr|
	if(pr.path.basename == current) {
		pr.v[\nodeType] = pr.collIndex;
	};
};
