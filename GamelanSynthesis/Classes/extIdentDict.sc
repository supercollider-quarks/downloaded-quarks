+ IdentityDictionary {

	atKeys { |keys| 
		inform("atKeys is deprecated, use atAll.");
		
		^if (keys.isSequenceableCollection) { 
			keys.collect { |key| this.at(key) } 
		} { 
			this.at(keys) 
		}
	}
	
	atDefinedKeys { |keys| 
		^if (keys.isSequenceableCollection) { 
			keys.collect { |key|
				var val = this.at(key);
				if(val.isNil) { ^nil };
				val
			} 
		} { 
			this.at(keys) 
		}
	}
}