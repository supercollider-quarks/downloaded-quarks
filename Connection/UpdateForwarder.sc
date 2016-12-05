UpdateForwarder {
	var <dependants;

	*new {
		^super.new.prInitDependants;
	}

	prInitDependants{
		dependants = IdentitySet();
	}

	changed { arg what ... moreArgs;
		dependants.do({ arg item;
			item.update(this, what, *moreArgs);
		});
	}

	addDependant { arg dependant;
		if (dependants.isNil) {
			dependants = IdentitySet();
			this.onDependantsNotEmpty
		};

		dependants.add(dependant);
	}

	removeDependant { arg dependant;
		dependants.remove(dependant);
		if (dependants.size == 0) {
			dependants = nil;
			this.onDependantsEmpty;
		};
	}

	release {
		this.releaseDependants();
	}

	releaseDependants {
		dependants.clear();
		this.onDependantsEmpty();
	}

	onDependantsEmpty {}

	onDependantsNotEmpty {}

	update {
		|object, what ...args|
		dependants.do {
			|item|
			item.update(object, what, *args);
		}
	}
}

UpdateFilter : UpdateForwarder {
	var <>func;

	*new {
		|func|
		^super.new.func_(func)
	}

	update {
		|object, what ...args|
		if (func.value(object, what, *args)) {
			super.update(object, what, *args);
		}
	}
}

UpdateTransform : UpdateForwarder {
	var <>func;

	*new {
		|func|
		^super.new.func_(func)
	}

	update {
		|object, what ...args|
		var argsArray = func.value(object, what, *args);
		if (argsArray.notNil) {
			argsArray = argsArray[0..1] ++ argsArray[2];
			super.update(*argsArray);
		}
	}
}

UpdateKeyFilter : UpdateFilter {
	var <>key;

	*new {
		|key|
		var func = "{ |obj, inKey| % == inKey }".format("\\" ++ key).interpret;
		^super.new(func).key_(key);
	}

	connectionTraceString {
		^"%(%)".format(this.class, "\\" ++ key)
	}
}

DeferredUpdater : UpdateForwarder {
	classvar immediateDeferFunc, immediateDeferList;
	var clock, force, delta;

	*new {
		|delta=0, clock=(AppClock), force=true|
		^super.new.init(delta, clock, force)
	}

	init {
		|inDelta, inClock, inForce|
		clock = inClock;
		force = inForce;
		delta = inDelta;
	}

	update {
		|object, what ...args|
		if ((thisThread.clock == clock) && force.not) {
			super.update(object, what, *args);
		} {
			clock.sched(delta, {
				super.update(object, what, *args);
			})
		}
	}
}

OneShotUpdater : UpdateForwarder {
	var <>connection, <>shouldFree;

	*new {
		|connection, shouldFree=false|
		^super.new.connection_(connection).shouldFree_(shouldFree)
	}

	update {
		|object, what ...args|
		protect {
			super.update(object, what, *args);
		} {
			if (shouldFree) {
				connection.free();
				connection = nil;
			} {
				connection.disconnect();
			}
		}
	}
}

CollapsedUpdater : UpdateForwarder {
	var clock, force, delta;
	var deferredUpdate;
	var holdUpdates=false;

	*new {
		|delta=0, clock=(AppClock), force=true|
		^super.new.init(delta, clock, force)
	}

	init {
		|inDelta, inClock, inForce|
		clock = inClock;
		force = inForce;
		delta = inDelta;
	}

	deferIfNeeded {
		|func|
		if ((thisThread.clock == clock) && force.not) {
			func.value
		} {
			clock.sched(0, func);
		}
	}

	update {
		|object, what ...args|
		if (holdUpdates) {
			deferredUpdate = [object, what, args];
		} {
			holdUpdates = true;

			this.deferIfNeeded {
				super.update(object, what, *args);
			};

			clock.sched(delta, {
				holdUpdates = false;
				if (deferredUpdate.notNil) {
					var tmpdeferredUpdate = deferredUpdate;
					deferredUpdate = nil;
					this.update(tmpdeferredUpdate[0], tmpdeferredUpdate[1], *tmpdeferredUpdate[2]);
				};
			})
		}
	}
}
