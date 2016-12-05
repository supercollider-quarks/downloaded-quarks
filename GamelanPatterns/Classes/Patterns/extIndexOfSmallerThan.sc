+ SequenceableCollection {
	indexOfSmallerThan { arg val;
		var i = this.indexOfGreaterThan(val);
		if(i.isNil) { ^this.size - 1 };
		if(i == 0) { ^nil }
		^i - 1
	}

}