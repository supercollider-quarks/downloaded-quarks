+View {
	updateOnAction {
		|should=true|
		if (should) {
			ViewActionUpdater.enable(this);
		} {
			ViewActionUpdater.disable(this);
		}
	}

	signal {
		|key|
		this.updateOnAction(); // automatically update on action if we connect to a View
		^super.signal(key);
	}
}
