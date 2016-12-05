
SortedNList : SortedList {
	var <>maxSize = 8;

	*new { |maxSize=8, function|
		^super.new(maxSize, function).maxSize_(maxSize)
	}

	add { |item|
		// just add
		if (this.size < maxSize) {
			^super.add(item)
		};
		// when maxsize, only add if better than last element,

		if (function.value(item, array.last)) {
			array.pop;
			^super.add(item);
		};
		// else do nothing, i.e. just drop item
	}
	// always add them one by one to maintain maxSize
	addAll { |aColl|
		aColl.do { |elem| this.add(elem) }
	}
	// not efficient but simple
	sort {
		super.sort;
		array = array.keep(maxSize);
	}
}
