+Node {
	argSlot {
		|argName|
		^SynthArgSlot(this, argName)
	}

	argSlots {
		|...argNames|
		^argNames.collect(SynthArgSlot(this, _));
	}

	mapToArgs {
		|...associations|
		^ConnectionList.makeWith {
			associations.do {
				|assoc|
				assoc.key.signal(\value).connectTo(this.argSlot(assoc.value));
			}
		}
	}
}
