// String type that:
// 1. can represent an extended set of ascci characters (http://en.wikipedia.org/wiki/UTF-8)
// 2. responds to the collection methods, so is more practical in many cases.
// 3. can represent nested stings
// ("Strang" is German for string/strand)


Strang : List {

	*newFrom { |string|
		^string.asStrang
	}

	species { ^this.class }

	addAll { |string|
		^super.addAll(string.asStrang)
	}

	find { |string, ignoreCase = false, offset = 0|
		var index = offset, last;
		var i0 = 0;
		string = string.asStrang;
		last = string.size - 1;
		if(ignoreCase) { Error("ignoreCase not yet implemented").throw };
		while {
			index < this.size
		} {
			if(this.at(index + i0) == string.at(i0)) {
				if(i0 < last) {
					i0 = i0 + 1;
				} {
					^index
				}
			} {
				index = index + i0 + 1;
				i0 = 0;
			}
		};
		^nil
	}

	// conversion

	normalize {
		^this.collect { |elem|
			if(elem.isKindOf(Char)) { elem } { elem.asStrang }
		}
	}

	asStrang {
		^this
	}

	asString {
		^array.join
	}

	ascii {
		^array.ascii.flat
	}

	// printing

	printOn { arg stream;
		this.asString.printOn(stream)
	}

	deepFormat { |delim1 = "(", delim2 = ")"|
		var str = "";
		str = str ++ delim1;
		this.do { |x|
			if(x.isKindOf(Strang)) {
				str = str ++ x.deepFormat(delim1, delim2);
			} {
				str = str ++ x.asString;
			}
		};
		str = str ++ delim2;
		^str
	}

	*fromFormat { |string, delim1 = "(", delim2 = ")"|
		var new = Strang.new;
		/*

		*/
		^new
	}

	// some methods from String:

	compare { | aString, ignoreCase=false |
		^this.asString.compare(aString, ignoreCase)
	}
	< { arg aString; ^this.asString < aString.asString }
	<= { arg aString; ^this.asString <= aString.asString }
	> { arg aString; ^this.asString > aString.asString }
	>= { arg aString; ^this.asString >= aString.asString }

	split { |separator=$/, includeEmpty = true|
		var res = Array.new;
		var curr = this.class.new;
		array.do { |char|
			if(char == separator) {
				if(includeEmpty or: { curr.notEmpty }) {
					res = res.add(curr)
				};
				curr = this.class.new;
			} {
				curr.add(char)
			}
		};
		if(curr.notEmpty) { res = res.add(curr) };
		^res
	}

	+ { |string|
		^this ++ " " ++ string
	}


}


BigChar : Char {
	var chars;

	*new { |chars|
		^super.newCopyArgs(chars)
	}

	asAscii {
		^chars
	}

	ascii {
		^chars.ascii
	}

	printOn { arg stream;
		chars.do { |each| stream.put(each) }
	}

	== { arg obj;
		^this.compareObject(obj, #[\chars])
	}

	hash {
		^this.instVarHash(#[\chars])
	}

}

+ Object {
	asStrang {
		^this.asString.asStrang
	}
}

+ String {
	asStrang {
		var curr, res, counter, nonAscii;
		res = Strang.new(this.size);
		this.size.do { |i|
			// thanks to Laureano López:
			var character = this[i];
			var ascii = character.ascii;
			if (ascii < 0) {
				case
				{ (ascii >= -64) && (ascii < -32) } { counter = 2 }
				{ (ascii >= -32) && (ascii < -16) } { counter = 3 }
				{ ascii >= -16 } { counter = 4 };
				nonAscii = nonAscii.add(character);
				counter = counter - 1;
				if (counter == 0) {
					if(nonAscii.isNil) { Error("can't encode string").throw };
					// this could be Strang, then we can skip BigChar completely.
					// But semantics?
					res.add(BigChar(nonAscii));
					nonAscii = nil;
				};
			} {
				res = res.add(character);
			};
		};
		^res
	}
}

+ Integer {
	ascii {
		^this
	}
}

+ SequenceableCollection {
	asAscii {
		var res = String.new(this.size);
		this.do { |item| res.addAll(item.asAscii) };
		^res
	}
}
