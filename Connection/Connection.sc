Connection {
	classvar <collectionStack;
	classvar <tracing, <>traceAll=false;

	var <object, <dependant;

	var connected = false;
	var <traceConnection;

	*initClass {
		tracing = List();
	}

	*traceWith {
		|func|
		var collected, wasTracingAll;
		wasTracingAll = traceAll;
		traceAll = true;

		protect({
			collected = Connection.collect(func);
		}, {
			if (wasTracingAll.not) {
				collected.do(_.trace(false));
			};
			traceAll = wasTracingAll;
		})
	}

	*prBeforeCollect {
		collectionStack = collectionStack.add(List(20));
	}

	*prAfterCollect {
		^ConnectionList.newFrom(collectionStack.pop());
	}

	*basicNew {
		|object, dependant, connected|
		^super.newCopyArgs(object, dependant, connected).trace(traceAll).prCollect()
	}

	*new {
		|object, dependant, autoConnect=true|
		^super.newCopyArgs(object, dependant).connected_(autoConnect).trace(traceAll).prCollect()
	}

	*untraceAll {
		tracing.copy.do(_.trace(false));
	}

	prCollect {
		if (collectionStack.size > 0) {
			collectionStack.last.add(this);
		}
	}

	connected {
		traceConnection.notNil.if {
			^traceConnection.connected
		} {
			^object.dependants !? { |d| d.includes(dependant) } ?? false;
		}
	}

	connected_{
		|connect|
		if (traceConnection.isNil) {
			if (connect != this.connected) {
				connected = connect;
				if (connect) {
					object.addDependant(dependant);
				} {
					object.removeDependant(dependant);
				}
			}
		} {
			traceConnection.connected = connect;
		}
	}

	connect {
		this.connected_(true)
	}

	disconnect {
		this.connected_(false)
	}

	connectionFreed {
		this.free();
	}

	free {
		this.trace(false);
		this.disconnect();
		object.connectionFreed(this);
		object = dependant = nil;
	}

	disconnectWith {
		|func|
		var wasConnected = this.connected;

		this.disconnect();

		^func.protect({
			if (wasConnected) {
				this.connect();
			}
		});
	}

	onTrace {
		|obj, what ...values|
		var from, to, connectedSym;
		from = object.isKindOf(Connection).if({ object.dependant }, { object });
		to = dependant.isKindOf(UpdateTracer).if({ dependant.wrappedObject }, { dependant });
		connectedSym = this.connected.if("⋯", "⋰");

		"% %.signal(%) → %\t =[%]".format(
			connectedSym++connectedSym,
			from.connectionTraceString(obj, what),
			"\\" ++ what,
			to.connectionTraceString(obj, what),
			(values.collect(_.asCompileString)).join(","),
		).postln
	}

	trace {
		|shouldTrace=true|
		if (shouldTrace) {
			traceConnection ?? {
				traceConnection = UpdateTracer(object, dependant, this);
				object.addDependant(traceConnection);
				object.removeDependant(dependant);
				tracing.add(this);
			}
		} {
			traceConnection !? {
				var tempTrace = traceConnection;
				traceConnection = nil;
				object.removeDependant(tempTrace);
				this.connected = tempTrace.connected;
				tracing.remove(this);
			}
		}
	}

	traceWith {
		|func|
		var wasTracing = traceConnection.notNil;
		this.trace(true);
		protect(func, {
			this.trace(wasTracing);
		});
	}

	dependants {
		^dependant.dependants
	}

	addDependant {
		|dep|
		if (dependant.dependants.size == 0) {
			this.connect();
		};

		dependant.addDependant(dep);
	}

	removeDependant {
		|dep|
		dependant.removeDependant(dep);

		if (dependant.dependants.size == 0) {
			this.disconnect();
		}
	}

	releaseDependants {
		dependant.releaseDependants();
		this.disconnect();
	}

	connectTo {
		|nextDependant|
		^Connection(this, nextDependant);
	}

	chain {
		|newDependant|
		var newConnection, wasTracing = traceConnection.notNil;

		// We want to insert newDependant in between our current object and dependant.
		// I.e.: this.object -> newDependant -> this.dependant
		// The current (this) connection will represent the [newDependant -> this.dependant]
		// portion, and we construct and return a new connection for [this.object -> newDependant].
		this.trace(false);

		newConnection = object.connectTo(newDependant);
		this.disconnect();
		object = newConnection;
		this.connect();

		this.trace(wasTracing);
	}

	filter {
		|filter|
		if (filter.isKindOf(Symbol)) {
			this.chain(UpdateKeyFilter(filter))
		} {
			this.chain(UpdateFilter(filter))
		}
	}

	transform {
		|func|
		this.chain(UpdateTransform(func))
	}

	defer {
		|delta=0, clock=(AppClock), force=false|
		this.chain(DeferredUpdater(delta, clock, force));
	}

	collapse {
		|delta=0, clock=(AppClock), force=true|
		this.chain(CollapsedUpdater(delta, clock, force))
	}

	oneShot {
		|shouldFree=false|
		this.chain(OneShotUpdater(this, shouldFree));
	}
}

ConnectionList : List {
	*makeWith {
		|func|
		var result;

		Connection.prBeforeCollect();
		protect {
			func.value()
		} {
			result = Connection.prAfterCollect();
		};
		^result
	}

	connected_{
		|connect|
		this.do(_.connected_(connect));
	}

	connect {
		this.do(_.connect);
	}

	disconnect {
		this.do(_.disconnect);
	}

	connectionFreed {
		this.free;
	}

	free {
		this.do(_.free);
		this.clear();
	}

	disconnectWith {
		|func|
		var wasConnected = this.select(_.connected);

		this.disconnect();

		^func.protect({
			wasConnected.do(_.connect)
		});
	}

	trace {
		|shouldTrace=true|
		this.do(_.trace(shouldTrace));
	}

	dependants {
		^this.collect({ |o| o.dependants.asList }).flatten;
	}

	addDependant {
		|dep|
		this.do(_.addDependant(dep));
	}

	removeDependant {
		|dep|
		this.do(_.removeDependant(dep));
	}

	releaseDependants {
		this.do(_.releaseDependants());
	}

	connectTo {
		|nextDependant, autoConnect=true|
		^Connection(this, nextDependant, autoConnect);
	}

	chain {
		|newDependant|
		this.do(_.chain(newDependant));
	}

	filter {
		|filter|
		this.do(_.filter(filter));
	}

	transform {
		|func|
		this.do(_.transform(func))
	}

	defer {
		|delta=0, clock=(AppClock), force=false|
		this.do(_.defer(delta, clock, force))
	}

	collapse {
		|delta=0, clock=(AppClock), force=true|
		this.do(_.collapse(delta, clock, force))
	}

	oneShot {
		|shouldFree=false|
		this.do(_.oneShot(shouldFree))
	}
}
