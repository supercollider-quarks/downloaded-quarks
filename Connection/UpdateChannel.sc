UpdateChannel : Singleton {
	update {
		|object, what ...args|
		this.changed(what, *args)
	}
}

UpdateBroadcaster : Singleton {
	// simply rebroadcast
	update {
		|object, what ...args|
		dependantsDictionary.at(this).copy.do({ arg item;
			item.update(object, what, *args);
		});
	}
}