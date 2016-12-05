
//////////////////  attributes (dict of properties) ///////////////////

+ Object {

	matchAttributes { arg dict;
		^this == dict
	}
	concattribute { arg obj, override=false;
		^if(override) {Êobj } { this }
	}
	deattribute { arg obj;
		^if(this == obj) { nil } { this }
	}
}

+ Nil { // nil matches everything

	matchAttributes { arg dict;
		^true
	}
	
	concattribute { arg obj;
		^obj
	}
	
	deattribute { arg dict;
		^this
	}
}

+ Dictionary {
	
	matchAttributes { arg dict;
		if(dict.isKindOf(Dictionary)) {
			this.pairsDo { |key, val|
				if(val.isKindOf(AbstractFunction)) { 
					
					^val.value(dict[key].value(this), dict)
					
				};
				if(
						key != \resource // omit resource
						and: { dict[key].value(this) != val }
				) { ^false } 
			};
			^true
		} {
			^false
		}
	}
	
	concattribute { arg dict, override=false;
		if(dict.respondsTo(\pairsDo)) {
			dict.pairsDo { |key, val|
				if(override or: { this[key].isNil }) {Ê
						//"warning: override attribute %: % by %\n".postf(key, attr[key], val);
					this[key]Ê= val
				};
			}
		} {
			^[this, dict]
		}
	}
	
	deattribute { arg dict;
		if(dict.respondsTo(\pairsDo)) {
			dict.pairsDo { |key, val|
					if(this[key] == val) { // maybe match item?
						this.removeAt(key)
					};
			}
		}
	}
}

+ Collection {

	matchAttributes { arg dict;
		^this.any(_.matchAttributes(dict))
	}
	
	concattribute { arg obj;
		if(this.indexOfEqual(obj).isNil) {
			^this.add(obj)
		}
	}
	// ?
	deattribute { arg obj;
		^this.reject(_ == obj)
	}
}


+ AbstractFunction {

	matchAttributes { arg dict;
		^this.value(dict)
	}
	
	concattribute { arg obj;
		^[this, obj]
	}
	
}
+ UnaryOpFunction {
	matchAttributes { arg dict;
		^a.matchAttributes(dict).perform(selector)
	}


}

+ BinaryOpFunction {
	matchAttributes { arg dict;
		^a.matchAttributes(dict).perform(selector, b.matchAttributes(dict), adverb)
	}
}
