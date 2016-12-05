
// methods for recursive translation from score (string) to event lists.
// and for applying pathet.


+ String {
	fromScoreToEvents { arg converter, addResource=true;
		^converter.convert(this, addResource);
	}
	
}

+ Object {

	fromScoreToEvents { arg converter, addResource=true;
		^this
	}

//	balungan { // remove later
//		"don't use balungan, use vg_convert!".postln;
//		^this.vg_convert
//	}

	vg_convert { arg addResource=true;
		^this.fromScoreToEvents(BalunganReader, addResource)
	}

//	kendhangan { // remove later
//		"don't use kendhangan, use vg_kdhconvert!".postln;
//		^this.vg_kdhconvert
//	}
		
	vg_kdhconvert { arg addResource=true;
		^this.fromScoreToEvents(KendhanganReader, addResource)
	}

//	unconvert { // remove later
//		"don't use unconvert, use vg_unconvert!".postln;
//		^this.vg_unconvert
//	} 
	
	vg_unconvert { arg converter = \BalunganReader;
		^this
	}
	
	applyPathet { arg pathetName;
		^this
	}
	
}

+ Association {

	fromScoreToEvents { arg converter, addResource=true;
		this.key = this.key.fromScoreToEvents(converter, false);
		this.value = this.value.fromScoreToEvents(converter, addResource);
	}
		
	applyPathet { arg pathetName;
		key = key.applyPathet(pathetName);
		value = value.applyPathet(pathetName);
	}
}

+ Set {
	fromScoreToEvents { arg converter, addResource=true;
		^this.collect(_.fromScoreToEvents(converter, addResource))
	}
}

+ SequenceableCollection {
	
	fromScoreToEvents { arg converter, addResource=true;
		this.deepDoAssoc { |assoc|
			assoc.fromScoreToEvents(converter, addResource)
		}
	}
	
	vg_unconvert { arg converter;
		^(converter ? BalunganReader).unconvert(this)
	}
	
	applyPathet { arg pathetName;
		// doesn't work yet!
		this.deepDoAssoc { |assoc|
			assoc.applyPathet(pathetName)
		}
	}

}

+ Event {
	applyPathet { arg pathetName;
		// in place conversion for now.
		this.putAll(Pathet(pathetName).mapEvent(this))
	}
}

+ Nil {
	getScoreFromEvents { ^"" }
}


// functions create a mapping function that is applied when they are evaluated
// this has turned out to be a little dangerous, because it may be applied to functions in Preact
/*
+ Function {

	fromScoreToEvents { arg converter, addResource=true;
		^{ |...args| converter.convert(this.valueArray(args), addResource) };
	}

	applyPathet { arg pathetName;
		^{ |...args| Pathet(pathetName).mapEvent(this.valueArray(args)) };
	}

}
*/


