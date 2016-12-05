
VG_State {
	classvar <>all, <>global;
	var <name, <dict;
	
	*initClass {
		all = ();
		global = this.new(\global).add;
	}
	
	*new { arg name;
		^super.newCopyArgs(name).init.add
	}
	
	*get { arg name, key;
		var state = all.at(name);
		^if(state.isNil) { nil } { state.dict.at(key) }
	}
	
	*set { arg name, key, value;
		var state = all.at(name);
		^if(state.isNil) { "no state found".error; } { state.dict.put(key, value) }
	}
	
	init {
		dict = LazyEnvir.new;
		dict.know = true;
		dict.proxyClass = PatternProxy;
	}
	
	add {
		all.put(name, this)
	}
	
	remove {
		all.removeAt(name)
	}
}