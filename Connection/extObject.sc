+Object {
	valueSlot {
		|setter=\value|
		^ValueSlot(this, setter.asSetter)
	}

	methodSlot {
		|method ...argOrder|
		^MethodSlot(this, method, *argOrder)
	}

	methodSlots {
		|...methods|
		^methods.collect(this.methodSlot(_))
	}

	connectTo {
		|...dependants|
		var autoConnect = if (dependants.last.isKindOf(Boolean)) { dependants.pop() } { true };
		if (dependants.size == 1) {
			^Connection(this, dependants[0], autoConnect);
		} {
			^ConnectionList.newFrom(dependants.collect {
				|dependant|
				Connection(this, dependant, autoConnect)
			})
		}
	}

	mapToSlots {
		|...associations|
		^ConnectionList.makeWith {
			associations.do {
				|assoc|
				assoc.key.connectTo(this.methodSlot(assoc.value));
			}
		}
	}

	signal {
		|keyOrFunc|
		if (keyOrFunc.isNil) {
			^this
		} {
			if (keyOrFunc.isKindOf(Symbol)) {
				^UpdateDispatcher(this).at(keyOrFunc);
			} {
				^this.connectTo(UpdateFilter(keyOrFunc));
			}
		}
	}

	signals {
		|...keyOrFuncs|
		^keyOrFuncs.collect(this.signal(_));
	}

	connectionTraceString {
		^"%(%)".format(this.class, this.identityHash)
	}

	connectionFreed {}
}