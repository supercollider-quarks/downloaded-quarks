+ Char {
	isClean {
		^this.isPrint or: { this.ascii.inclusivelyBetween(9, 13) }
	}
}

+ String {

	countGremlins { ^this.findGremlins.size }

	findGremlins {
		var indices = [];
		this.do { |ch, i|
			if (ch.isClean.not) { indices = indices.add(i) }
		};
		^indices
	}

	clean { |space=true|	// strip all non-crossplatform ascii chars.
		^if (space)		// either add blankspaces
		{ this.collect { |ch| if (ch.isClean) { ch } { $ }; } }
		// or not.
		{ this.select(_.isClean) }
	}
}

+ Document {
	clean { |space=true|
		var cleanedString;
		cleanedString = this.string.clean(space);
		if (cleanedString != this.string, {
			this.string = cleanedString;
			"" ++ this.class + this.title + "swapped for clean string.".postln;
		});
	}
	*makeCleanerWindow {
		var w;
		w = Window("cleaner", Rect(0,0,140, 60)).front;
		w.view.decorator = FlowLayout(w.bounds.copy.left_(0).top_(0));
		Button(w, Rect(0,0,120,20))
		.states_([["clean current doc"]])
		.action_ { Document.current.clean(false) };
		Button(w, Rect(0,0,120,20))
		.states_([["current doc gremlins"]])
		.action_ { var gr; gr = Document.current.string.findGremlins;
			if (gr.notEmpty,
				{ Document.current.selectRange(gr[0], 1).front },
				{ Document.current.selectRange(nil, 0).front; "None!".postln; }
			);
		};
	}
}

+ Class {
	*showGremlins {
		var scfiles = Set.new;
		var file, string, badIndices, count;
		Class.allClasses.do { |cl|
			scfiles.add(cl.filenameSymbol);
			cl.methods.do { |mth|
				var file = mth.filenameSymbol;
				if (file.notNil) { scfiles.add(file) };
			};
		};

		scfiles.as(Array).do { |name|
			file = File(name.asString, "r");
			if (file.isOpen, {
				badIndices = file.contents.findGremlins;
				count = badIndices.size;
				if (count > 0, { ("" + name + ":" + count + "at:" + badIndices).postln });
				file.close;
				}, {
					("could not open file: " + name).postln;
			});
		};
	}
}
