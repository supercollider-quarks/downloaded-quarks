
Pca : ListPattern {

	var <>rule, <>ruleAsBinary;//base
	var <>win;//gui

	*new {| list, rule, repeats=inf |
		^super.new(list, repeats).rule_(rule);
	}
	//for subclass
	makerule {}
	//for subclass
	evolve {}

	embedInStream {|inval|
		var items = list.copy;
		var ruleStrm = rule.asStream, ruleVal;
		repeats.do {
			ruleVal = ruleStrm.next(inval);
			if (ruleVal.isNil, { ^inval });
			this.makerule(ruleVal);
			items = this.evolve(items);
			inval = items.yield;
		}
		^inval;
	}
}

Pca1 : Pca {

	makerule {|char|
		ruleAsBinary = Array.fill(8, {|i| 16r01 & (char >> i) });
	}
	evolve {|prev|
		var next = Array.newClear(prev.size);
		prev.size.do {|i|
			next[i] = ruleAsBinary[(prev.wrapAt(i-1)*4) + (prev[i]*2) + prev.wrapAt(i+1)]
		};
		^next;
	}
}

Pca2 : Pca {

	makerule {|hex|
		ruleAsBinary = Array.newClear(8);
		hex.do {|item,i| ruleAsBinary[i] = ("16r" ++ item).interpret.asBinaryDigits(4) };
		ruleAsBinary = ruleAsBinary.flat.reverse;
	}
	evolve {|prev|
		var next = Array.newClear(prev.size);
		prev.size.do {|i|
			next[i] = ruleAsBinary[
				(prev.wrapAt(i-2) * 16) +
				(prev.wrapAt(i-1) *  8) +
				(prev.at(i)       *  4) +
				(prev.wrapAt(i+1) *  2) +
				(prev.wrapAt(i+2) *  1)
			];
		};
		^next;
	}
}

+ Pca {
	*r1 {
		//some rules of ca1
		//Original, Mirrored, Complemental, Mirrored-Complemental
		^"
		triangle1 -> [18, 146] // amphirical
		triangle2 -> [26, 82, 154, 210]
		triangle3 -> [22, 151, 129, 161]
		triangle4 -> [166, 167, 180, 181, 183]
		triangle5 -> [90, 165]// amphirical
		gestalt   -> [105, 150] // survive smpwd
		universal -> [110, 124, 137, 193]
		harmonic1 -> [60, 102, 195, 153]
		harmonic2 -> [106, 120, 169, 225] // 120 and 225 survive
		noise1    -> [30, 86, 135, 149], // survive smpwd if init rand
		noise2    -> [122, 126, 146, 182, 192], // wider smpwd conditionally with rand init
		colored   -> [45, 75, 89, 101], // wider smpwd init rand
		pulseflux1-> [54, 147], // amphirical, no mirror
		pulseflux2-> [62, 118, 131, 145],
		motion RL 2, 6, 10, 14, 34, 38, 42, 46, 74, 106
		motion LR 16, 20, 24, 82, 120
		straight 4, 12, 13, 36";
	}
	*r2 {
		//some rules of ca2
		//reference, Mirek's MJCell
		//http://psoup.math.wisc.edu/mcell/mjcell/mjcell.html
		^[
		"BC82271C", //bermuda triangle
		"AD9C7232", //chaotic gliders
		"89ED7106", //compound glider
		"1C2A4798",
		"5C6A4D98",
		"5F0C9AD8",
		"B51E9CE8",
		"6C1E53A8",
		"360A96F9",
		"BF8A5CD8",
		"6EA8CD14",
		"4668ED14",
		"D28F022C",
		"6EEAED14",
		"BF8A18C8",
		"BF8A58C8",
		"3CC66B84",
		"3EEE6B84",
		"1D041AC8",
		"5F2A9CC8",
		"1D265EC8",
		"2F8A1858",
		"1D065AD8",
		"BDA258C8",
		"9D041AC8",
		"7E8696DE",
		"978ECEE4",
		"E0897801",
		"8F0C1A48"
		];
	}
	plot { |n=500|
		var rct, cell, stream = this.asStream, strVal;
		rct = Rect(0, 0, list.size, n);
		win = Window("rule " ++ rule ++ " size " ++ list.size ++ "x" ++ n, rct, false);
		win.view.background = Color.black;
		win.drawFunc = {
			Pen.fillColor = Color.white;
			n.do {|i|
				strVal = stream.next;
				strVal.do {|item,j|
					if (item == 1, { Pen.fillRect(Rect(j, i, 1, 1)) });
				};
			};

		};
		win.front;
		CmdPeriod.doOnce { win.close };
	}
}