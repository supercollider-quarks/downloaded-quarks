PeriodicUpdater {
	var <object, <method;
	var <freq, <>name;
	var <process, <lastVal;

	*new {
		|object, method=\value, freq=0.1|
		^super.newCopyArgs(object, method).freq_(freq).name_(method);
	}

	freq_{
		|inFreq|
		freq = inFreq;
		process.stop();
		process = SkipJack(this.pull(_), freq, name:"PeriodicUpdater_" ++ this.identityHash.asString);
	}

	start {
		process.start();
	}

	stop {
		process.stop();
	}

	pull {
		var val = object.perform(method);
		if (val != lastVal) {
			lastVal = val;
			this.changed(\value, val)
		};
	}
}

BusUpdater : PeriodicUpdater {
	*new {
		|bus, freq=0.1|
		^super.new(bus, \getSynchronous, freq);
	}
}