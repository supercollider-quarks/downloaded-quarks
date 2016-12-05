+SequenceableCollection {
	connectAll {
		|dependant|
		^ConnectionList.newFrom(this.collect(_.connectTo(dependant)))
	}

	connectEach {
		|dependantList, signalName, method|
		if (this.size != dependantList.size) {
			Error("connectEach requires collections of equal size (this.size = %, other.size = %)".format(this.size, dependantList.size)).throw;
		};

		^this.collectAs({
			|object, i|
			var dependant = dependantList[i];

			if (method.notNil) {
				dependant = dependant.methodSlot(method);
			};

			if (signalName.notNil) {
				object = object.signal(signalName);
			};

			object.connectTo(dependant);
		}, ConnectionList)
	}
}