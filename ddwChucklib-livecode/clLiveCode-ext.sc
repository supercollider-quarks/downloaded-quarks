// "decodes" pairs of [value, delta] into step sequences, for non-default parameters

PstepDurPair : Pstep {
	var <>tolerance;

	*new { |pairs, repeats = 1, tolerance = 0.001|
		^super.newCopyArgs(pairs, nil, repeats).init.tolerance_(tolerance);
	}

	embedInStream { |inval|
		var itemStream, durStream, pair, item, dur, nextChange = 0, elapsed = 0;
		repeats.value(inval).do {
			itemStream = list.asStream;
			while {
				pair = itemStream.next(inval);
				if(pair.notNil) {
					#item, dur = pair;
					item.notNil and: { dur.notNil }
				} { false }  // terminate if stream was nil
			} {
				nextChange = nextChange + dur;
				// 'elapsed' increments, so nextChange - elapsed will get smaller
				// when this drops below 'tolerance' it's time to move on
				while { (nextChange - elapsed) >= tolerance } {
					elapsed = elapsed + inval.delta;
					inval = item.embedInStream(inval);
				};
			};
		};
		^inval
	}

	// not properly part of a pattern, but I need to install hooks to load the environment
	// then you can do e.g. \loadCl.eval
	*initClass {
		Class.initClassTree(AbstractChuckArray);
		Class.initClassTree(Library);
		Library.put(\cl, \path, this.filenameSymbol.asString.dirname);
		Library.put(\cl, \files, ["preprocessor.scd", "preprocessor-generators.scd", "helper-funcs.scd"]);
		Library.put(\cl, \extras, ["edit-gui.scd", "mobile-objects.scd"]);

		{ |files|
			var dir = Library.at(\cl, \path);
			files.do { |name| (dir +/+ name).loadPath };
		} => Func(\loadClFiles);

		{ \loadClFiles.eval(Library.at(\cl, \files)) } => Func(\loadCl);
		{ \loadClFiles.eval(Library.at(\cl, \extras)) } => Func(\loadClExtras);
		{ #[loadCl, loadClExtras].do(_.eval) } => Func(\loadAllCl);
	}
}


// used in clGenSeq for matching items

+ Rest {
	== { |that|
		if(that.isKindOf(Rest).not) { ^false };
		^(dur == that.dur)
	}
}
