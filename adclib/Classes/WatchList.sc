WatchList {
	classvar <getFuncs;
	var <object, <>getFunc, <>keepGone;
	var <list, <skip;

	*initClass {
		// lookup for getFuncs
		getFuncs = ();
	}

	*new { |object, getFunc, keepGone = true, skip = true, halo = true|
		^super.newCopyArgs(object, getFunc, keepGone)
		.init(skip, halo);
	}

	init { |skipFlag, haloFlag|
		list = List[];
		// dont know if a list of watchlists is sufficient
		if (haloFlag) { this.addToHalo(object) };
		if (skipFlag) { this.makeSkip };
		this.update;
	}

	object_ { |obj|
		object.getHalo(\wl).remove(this);
		object = obj;
		list.clear;
		this.addToHalo(obj);
	}

	addToHalo { |obj|
		if (obj.notNil) {
			obj.addHalo(\wl, obj.getHalo(\wl).add(this));
		}
	}

	// proxy space
	*envir { |envir, keepGone = true, skip = true, halo = true|
		^this.new(envir, { |ob| ob.keys(Array).sort }, keepGone, skip, halo);
	}

	*spaceAr { |space, keepGone = true, skip = true, halo = true|
		^this.new(space, { |ob| ob.arProxyNames }, keepGone, skip, halo);
	}
	*spaceKr { |space, keepGone = true, skip = true, halo = true|
		^this.new(space, { |ob| ob.krProxyNames }, keepGone, skip, halo);
	}

	*proxy { |proxy, keepGone = true, skip = true, halo = true|
		^this.new(proxy, { |ob| ob.controlKeys }, keepGone, skip, halo);
	}

	update {
		var newItems;
		if (object.isNil) { ^this };
		newItems = getFunc.value(object).removeAll(list);
		newItems.do { |item|
			if (list.includes(item).not) { list.add(item) };
		};
		// to do:
		// if (keepGone.not) { .. filter removed names }
	}
	makeSkip { |dt=0.2|
		skip = skip ?? { SkipJack({ this.update }, dt) };
	}

	clear { |update = true|
		list.clear;
		if (update) { this.update };
	}
}