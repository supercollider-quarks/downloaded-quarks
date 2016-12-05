AbstractControlValue {
	var value, <spec, specConnection, specConnection, <>updateOnConnect=true, <>holdUpdates=false;
	var prIsChanging=false;

	*defaultSpec { this.subclassResponsibility(thisMethod) }

	*new {
		|initialValue, spec|
		^super.new.init(initialValue, spec);
	}

	init {
		|initialValue, inSpec|
		spec = inSpec 			?? { this.class.defaultSpec.deepCopy };
		value = initialValue;
	}

	value {
		^(value ?? { spec.default })
	}

	value_{
		|inVal|
		value = spec.constrain(inVal);
		this.changed(\value, value);
	}

	input_{
		|inVal|
		this.value = spec.map(inVal);
	}

	input {
		^spec.unmap(this.value);
	}

	addDependant {
		|dependant|
		super.addDependant(dependant);
		if (updateOnConnect) {
			dependant.update(this, \value, this.value);
		}
	}

	spec_{
		|inSpec|
		spec.setFrom(inSpec);

		this.constrain();
	}

	constrain {
		var newValue;
		if (value.notNil) {
			newValue = spec.constrain(value);
			if (value != newValue) { this.value = newValue }
		} {
			this.changed(\value, this.value);
		}
	}

	changed {
		arg what ... moreArgs;
		if (holdUpdates.not && prIsChanging.not) {
			prIsChanging = true;
			protect {
				super.changed(what, *moreArgs);
			} {
				prIsChanging = false;
			}
		}
	}

	signal {
		|key|
		if (key == \value) {
			// We only use the \value signal - so just return the object itself as an optimization.
			// This also allows us to detect when dependants are added, and report the current value.
			^this
		} {
			^super.signal(key);
		}
	}

	// Do not override this method in subclasses - instead, override prSetFrom
	setFrom {
		|other|
		if (this.class != other.class) {
			Error("Trying to set a ControlValue of type '%' from one of type '%'.".format(this.class, other.class)).throw
		} {
			this.holdUpdates = true;
			protect { this.prSetFrom(other) } {
				this.holdUpdates = false;
			};
			this.changed(\value, value);
		}
	}

	prSetFrom {
		|other|
		this.updateOnConnect = other.updateOnConnect;
		this.spec = other.spec;
		this.value = other.value;
	}

	asControlInput {
		^this.value
	}

	asStream {
		^Routine { loop { this.asControlInput.yield } }
	}
}

NumericControlValue : AbstractControlValue {
	*defaultSpec { ^\unipolar.asSpec }
}

IndexedControlValue : AbstractControlValue {
	*defaultSpec { ^ItemsSpec([]) }
}

BusControlValue : NumericControlValue {
	var bus, <>server;

	init {
		|initialValue, inSpec|
		super.init(initialValue, inSpec);

		server = Server.default;

		ServerTree.add(this);
		ServerQuit.add(this);
		ServerBoot.add(this);

		if (Server.default.serverRunning) {
			this.send();
		}
	}

	bus {
		if (bus.isNil && server.serverRunning) {
			this.send();
		};
		^bus;
	}

	doOnServerTree {}

	doOnServerBoot {
		this.send();
	}

	doOnServerQuit {
		this.free();
	}

	value_{
		|inValue|
		super.value_(inValue);
		bus.set(value);
	}

	constrain {
		if (value.isNil) {
			bus.set(this.value)
		};
		^super.constrain();
	}

	send {
		if (bus.isNil) {
			bus = Bus.control(server, 1);
			bus.set(this.value);
		}
	}

	free {
		if (bus.notNil) {
			bus.free; bus = nil;
		}
	}

	asMap { ^this.bus.asMap }
}

OnOffControlValue : AbstractControlValue {
	var value, onSig, offSig;

	*defaultSpec { ^ControlSpec(0, 1, step:1) }

	init {
		|initialValue|
		onSig = UpdateForwarder();
		offSig = UpdateForwarder();
		^super.init(initialValue);
	}

	on {
		this.value = \on;
	}

	off {
		this.value = \off;
	}

	toggle {
		this.value = (value == \on).if(\off, \on);
	}

	value_{
		|inVal|
		if ((inVal == \on) || (inVal == \off)) {
			if (inVal != value) {
				value = inVal;
				this.changed(\value, value);
				if (value == \on) {
					onSig.changed(\on)
				} {
					offSig.changed(\on)
				};
			}
		} {
			Error("Value must be \off or \on").throw
		}
	}

	input_{
		|inputVal|
		this.value = (inputVal > 0.5).if(\on, \off);
	}

	input {
		^switch (value,
			{ \off }, { 0 },
			{ \on }, { 1 }
		)
	}

	signal {
		|name|
		if (name == \on) { ^onSig };
		if (name == \off) { ^offSig };
		^super.signal(name);
	}

	constrain {}
}

MIDIControlValue : NumericControlValue {
	var <>inputSpec, <isOwned=false;
	var func, <midiFunc;

	*defaultInputSpec { ^ControlSpec(0, 127); }

	cc_{
		| ccNumOrFunc, chan, srcID, argTemplate, dispatcher |
		inputSpec = inputSpec ?? { this.class.defaultInputSpec };

		func = func ? {
			|val|
			this.input = inputSpec.unmap(val);
		};

		this.clearMIDIFunc();

		if (ccNumOrFunc.notNil) {
			if (ccNumOrFunc.isKindOf(MIDIFunc)) {
				isOwned = false;
				midiFunc = ccNumOrFunc;
				midiFunc.add(func);
			} {
				isOwned = true;
				midiFunc = MIDIFunc.cc(func, ccNumOrFunc, chan, srcID, argTemplate, dispatcher)
			}
		}
	}

	prSetFrom {
		|other|
		super.prSetFrom(other);
		this.inputSpec = other.inputSpec;
		if (other.midiFunc.notNil) {
			this.cc_(
				other.midiFunc.msgNum,
				other.midiFunc.chan,
				other.midiFunc.srcID,
				other.midiFunc.argTemplate,
				other.midiFunc.dispatcher
			)
		}
	}

	free {
		this.clearMIDIFunc();
	}

	clearMIDIFunc {
		if (midiFunc.notNil) {
			midiFunc.remove(func);
			if (isOwned) {
				midiFunc.free;
			};
			midiFunc = nil;
		}
	}
}

ControlValueEnvir : EnvironmentRedirect {
	var <default, redirect;
	var envir;
	var <allowCreate=true;

	*new {
		|default=(NumericControlValue)|
		var envir = Environment();
		^super.new().default_(default).know_(true)
	}

	default_{
		|inDefault|
		if (inDefault.isKindOf(Class)) {
			default = { inDefault.new() }
		} {
			default = inDefault
		}
	}

	at {
		|key|
		var control = super.at(key);

		if(control.isNil && allowCreate) {
			control = default.value(key);
			super.put(key, control);
		};
		^control
	}

	put {
		|key, value|
		var control = super.at(key);

		if (control.isNil || value.isNil) {
			super.put(key, value);
		} {
			control.setFrom(value);
		}
	}
}

