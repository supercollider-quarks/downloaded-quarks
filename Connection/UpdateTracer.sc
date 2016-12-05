UpdateTracer {
	var <upstream, <wrappedObject, <connection;
	var <>connected;

	*new {
		|upstream, wrappedObject, connection|
		^super.newCopyArgs(upstream, wrappedObject, connection).init
	}

	init {
		connected = upstream.dependants !? (_.includes(wrappedObject)) ?? false;
	}

	update {
		|object, what ...args|
		connection.onTrace(object, what, *args);
		if (connected) {
			wrappedObject.update(object, what, *args);
		}
	}

	addDependant {
		|dependant|
		wrappedObject.addDependant(dependant);
	}

	removeDependant {
		|dependant|
		wrappedObject.removeDependant(dependant);
	}

	release {
		wrappedObject.release();
	}

	releaseDependants {
		wrappedObject.releaseDependants();
	}
}