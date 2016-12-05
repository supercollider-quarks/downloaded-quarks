+ControlSpec {
	setFrom {
		|otherSpec|
		this.minval 	= otherSpec.minval;
		this.maxval 	= otherSpec.maxval;
		this.warp 		= otherSpec.warp;
		this.step 		= otherSpec.step;
		this.default 	= otherSpec.default;
		this.units 		= otherSpec.units;
		this.grid 		= otherSpec.grid;
	}

	warp_{
		|w|
		warp = w.copy;
		w.spec = this;
		this.changed(\warp);
	}
}
