MethodSlot {
	var <updateFunc, <reciever, <methodName;

	*new {
		|obj, method ...argOrder|
		if (obj.isKindOf(View)) {
			^MethodSlotUI.prNew().init(obj, method, argOrder);
		} {
			^MethodSlot.prNew().init(obj, method, argOrder);
		};
	}

	*prNew { ^super.new }

	init {
		|inObject, inMethodName, argOrder|
		reciever = inObject;
		methodName = inMethodName;
		updateFunc = MethodSlot.makeUpdateFunc(reciever, methodName, argOrder);
	}

	*makeUpdateFunc {
		|reciever, methodName, argOrder|
		var argString, callString;
		var possibleArgs = ['object', 'changed', '*args', 'args', 'value'];

		if (methodName.isKindOf(String) && argOrder.isEmpty) {
			// Should be of the form e.g. "someMethod(value, arg[0])"
			callString = methodName;
			methodName = methodName.split($()[0].asSymbol; // guess the method name - used later for validation
		} {
			argOrder.do {
				|a|
				if (a.isInteger.not && possibleArgs.includes(a).not) {
					Error("Can't handle arg '%' - must be one of: %.".format(a, possibleArgs.join(", "))).throw
				}
			};

			if (argOrder.isEmpty) {
				argOrder = ['object', 'changed', '*args'];
			};

			argString = argOrder.collect({
				|a|
				if (a.isInteger) {
					"args[%]".format(a)
				} {
					a.asString
				}
			}).join(", ");
			callString = "%(%)".format(methodName, argString);
		};

		if (reciever.respondsTo(methodName).not && reciever.tryPerform(\know).asBoolean.not) {
			Exception("Object of type % doesn't respond to %.".format(reciever.class, methodName)).throw;
		};

		^"{ |reciever, object, changed, args| var value = args[0]; reciever.% }".format(callString).interpret;
	}

	update {
		|object, changed ...args|
		updateFunc.value(reciever, object, changed, args);
	}

	connectionTraceString {
		|what|
		^"%(%(%).%)".format(this.class, reciever.class, reciever.identityHash, methodName)
	}
}

MethodSlotUI : MethodSlot {
	classvar deferList, deferFunc;

	*initClass {
		deferList = List();
	}

	*doDeferred {
		var tmpList = deferList;
		deferList = List(tmpList.size);
		deferFunc = nil;

		tmpList.do {
			|argsList|
			argsList[0].value(*argsList[1]);
		}
	}

	*deferUpdate {
		|updateFunc, args|
		deferList.add([updateFunc, args]);
		deferFunc ?? {
			deferFunc = { MethodSlotUI.doDeferred }.defer
		}
	}

	*prNew { ^super.prNew }

	update {
		|object, changed ...args|
		if (this.canCallOS) {
			updateFunc.value(reciever, object, changed, args);
		} {
			this.class.deferUpdate(updateFunc, [reciever, object, changed, args])
		}
	}
}

ValueSlot : MethodSlot {
	*new {
		|obj, setter=\value_|
		^super.new(obj, setter, \value)
	}
}

SynthArgSlot {
	var <synth, <>argName, synthConn;

	*new {
		|synth, argName|
		^super.newCopyArgs(synth, argName).connectSynth
	}

	connectSynth {
		synth.register;
		synthConn = synth.signal(\n_end).connectTo(this.methodSlot(\disconnectSynth))
	}

	disconnectSynth {
		synthConn.free();
		synth = argName = synthConn = nil;
	}

	update {
		|obj, what, value|
		if (synth.notNil) {
			synth.set(argName, value);
		}
	}
}