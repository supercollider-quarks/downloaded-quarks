/*

if obj is a list,
	actually a list of associations,
		check each key, if it is recognised, repeat procedure with its value,
		until something is not nil.
	actually a list of objects,
		try to recognise it. if it can be recognised, return it, otherwise, return nil

if obj is an association,
	try to recognise key, if it matches, return this (the assoc), otherwise return nil

if obj is an obj,
	try to recognise it, if it matches, return this (the object), otherwise return nil

*/





+ SequenceableCollection {

	deepFindRule { arg list, index=0;
		//"searching in (%) List %\n\n\n".postf(
		//	if(this.containsAssociations) { "Assoc" } { "" }, this);
		if(this.containsAssociations) {
			this.do { |item|
				var res = item.deepFindRule(list, index);
				res !? { ^res }
			}
		} {
			this.recognise(list, index)
		};
		^nil
	}
	// check one level below, so that only lists of associations are deepsearched
	
	containsAssociations {
		^this.at(0).isAssociation
	}
	
	// recursively iteration over all rules
	
	deepDoAssoc { arg func, allLevels=true;
		if(this.containsAssociations) {
			this.do { |each|
				each.deepDoAssoc(func, allLevels)
			}
		}
	}
	
	deepCollectAssoc { arg func;
		^if(this.containsAssociations) {
			this.collect { |each|
				each.deepCollectAssoc(func)
			}
		} {
			this.copy
		}
	}
	
	
	findMaximalDropSize {
		var size = 0;
		this.deepDoAssoc { |assoc| size = max(size, assoc.keyDropSize) };
		^size
	}
	
	findMaximalKeySize {
		var size = 0;
		this.deepDoAssoc { |assoc| size = max(size, assoc.keySize) };
		^size
	}
	
}

// set is for random choice.
+ Set {

	deepFindRule { arg list, index=0;
		var randomElements = this.select { |assoc| 
			assoc.recognise(list, index) 
		};
		"randomElements: %\n".postvg(randomElements);
		^randomElements.choose
	}
	
	containsAssociations {
		^this.any(_.isAssociation)
	}

}

+ Association {

	isAssociation {
		^true
	}
	
	containsAssociations {
		^this.value.containsAssociations
	}
	
	deepFindRule { arg list, index=0;
		//"searching in % Association %\n\n\n".postf( if(this.containsAssociations) 
		//	{ "Association" } { "" }, this);
		if(this.containsAssociations) {
			if(this.recognise(list, index)) {
				^value.deepFindRule(list, index);
			}
		} {
			if(this.recognise(list, index)) {
				^this
			}
		};
		^nil
	}
	
	deepDoAssoc { arg func, allLevels=true;
		var deeper = this.containsAssociations;
		if(allLevels or: { deeper.not }) { func.value(this) };
		if(deeper) {
			this.value.deepDoAssoc(func, allLevels);
		}
	}
	deepCollectAssoc { arg func;
		^if(this.containsAssociations) {
			this.class.new(
				this.key,
				this.value.deepCollectAssoc(func)
			)
		} {
			func.value(this)
		}
	}
	
	prefixSize { ^0 }
	
	keyDropSize { ^key.keySize }
	
	keySize { ^key.keySize }
	
	keyDropDuration { ^key.eventDuration }
	
	

}


+ Object {

	isAssociation {
		^false
	}
	
	containsAssociations {
		^false
	}
	
	deepFindRule { arg list, index=0, verbose=true;
		// ("deepFindRule: %\n\n\n").postf(this);
		^if(this.recognise(list, index)) { this } { nil }
	}
	deepDoAssoc { arg func, allLevels;
		 ^this
	}
	deepCollectAssoc { arg func;
		^this
	}
	
	vgTestRule { arg list;
		^this.deepFindRule(list, 0).tranform(list)
	}
	
	keySize {
		^this.size.max(1)
	}
}

+ Dictionary {
	keySize { ^1 }
}


+ Tafsiran {
	deepFindRule { arg list, index=0;
		^rules.deepFindRule(list, index)
	}
}
