+ SequenceableCollection {

	addAttribute { arg dict, override=false;
		dict = dict.asAttribute;
		this.size.do { |i| 
			this[i] = this[i].concattribute(dict, override)
		} 
	}
	
	removeAttribute { arg dict;
		dict = dict.asAttribute;
		this.size.do { |i| 
			this[i] = this[i].deattribute(dict)
		} 
	}
	
	addConstraint { arg dict, override=false;
		this.do { |assoc| assoc.addConstraint(dict, override) }
	}
	
	removeConstraint { arg dict;
		this.do { |assoc| assoc.removeConstraint(dict) }
	}
	
	addProperty {Êarg dict, override=false;
		this.do { |assoc| assoc.addProperty(dict, override) }
	}

}

+ Association {

	addConstraint { arg dict, override=false;
		key.addAttribute(dict, override)
	}
	
	removeConstraint { arg dict;
		key.removeAttribute(dict)
	}
	
	
	addProperty {Êarg dict, override=false;
		value.addAttribute(dict, override)
	}
}

+ Dictionary {
	asAttribute {
		^this
	}
	addResource { arg ... pairs;
		var resource = this[\resource];
		if(resource.isNil) { 
			resource = this.class.new; 
			this[\resource] = resource 
		};
		pairs.pairsDo { |key, val|
			resource[key] = val;
		}
	}
}

+ Object {

	asAttribute {
		^().put(this, true)
	}
	
}