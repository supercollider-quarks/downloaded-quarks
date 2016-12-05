+ Stream {
	generateAll { arg inval;
		// don't do this on infinite streams.
		var array;
		this.generate({|item| array = array.add(item); }, inval);
		^array
	}

}