/*
compatibility with the Steno Quark
*/

+ Strang {

	stenoStripLineComments {
		^this.split(Char.nl).collect { |line|
			var i = line.find("//");
			if(i.notNil) { line[..i-1] } { line }
		}.join(Char.nl)
	}


}