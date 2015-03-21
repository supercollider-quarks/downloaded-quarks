
// SC 3 version 0.3, August 2005

// (first version 0.2, July 2001)
// (c) julian rohrhuber

// distributed under the terms of the GNU General Public License
// full notice in ProteinBioSynthesis main folder




// use this to stream the letters of the bases themselves (like Pseq)


GenomePattern : ProteinBioSynthesis {
	var  <>repeats, <>offset;

	*new { arg genome, repeats=1, offset=0;
		^super.new(genome).repeats_(repeats).offset_(offset)
	}


	asStream {
		^Routine.new({ arg inval;
			var item, offsetValue;
			offsetValue = offset.value;

				repeats.value.do({ arg j;
					this.size.do({ arg i;
						item = genome @@ (i + offsetValue);
						inval = item.embedInStream(inval);
					});
				});
			})

	}


}


// use this to stream triplets (codons)
CodonPattern : GenomePattern {
	asStream {
		^Routine { | inval |
			var inStream, codon = String.new(3), char, size = 0;
			inStream = super.asStream;
			while {
				char = inStream.next(inval);
				char.notNil
			} {
				codon = codon ++ char.toLower;
				size = size + 1;
				if(size == 3) {
					inval = codon.asSymbol.yield;
					codon = String.new(3);
					size = 0;
				}
			}
		}
	}

}

// use this to stream the index numbers of the aminoacids (from -1 to 19)
AminoacidPattern : CodonPattern {
	asStream {
		^super.asStream.collect({ arg item;
			this.translate(item)
		})
	}
}


// use this to stream the names of the aminoacids
AminoacidNames : ProteinBioSynthesis {
	var <>pattern;
	*new { arg pattern;
		^super.new.pattern_(pattern);
	}

	asStream {
		^pattern.asStream.collect({ arg item;
			aminoacids.at(item)
		})
	}
}



// test this, not reliable.

Ptranscribe : AminoacidPattern {
	var <>gap = -1;
	asStream {
		^Routine {
			var inStream, codon, mode = \skip;
			inStream = super.asStream;
			loop {
				codon = inStream.next;
				case
				{ codon == startcodon and:  { mode == \skip } } { mode = \write }
				{ codon == stopcodon and: { mode == \write } } { mode = \skip }
				{ mode == \write } { codon.yield }
				{ gap.yield }
			}
		}
	}


}




