LayoutValueSlider : SCViewHolder {
	var <value, <spec, number, slider, <action;

	*new { |parent, bounds, initValue = 0, spec, numberWidth = 50|
		^super.new.init(parent, bounds, initValue, spec, numberWidth)
	}

	value_ { |new|
		value = spec.constrain(new);
		number.value = value;
		slider.value = spec.unmap(value);
	}
	activeValue_ { |new|
		this.value_(new);
		action.value(this, value);
	}

	spec_ { |argSpec|
		spec = argSpec.asSpec;
		this.value_(value);
	}

	init { |parent, bounds, initValue, argSpec, numberWidth|
		spec = argSpec.asSpec;
		value = spec.constrain(initValue);

		view = CompositeView(parent, bounds);
		view.layout = HLayout(
			number = NumberBox().maxWidth_(numberWidth),
			slider = Slider().orientation_(\horizontal)
		).margins_(0).spacing_(2);

		number.value_(value)
		.action_({ |view|
			value = view.value;
			slider.value = spec.unmap(value);
			action.(this, value);
		});

		slider.value_(spec.unmap(value))
		.action_({ |view|
			value = spec.map(view.value);
			number.value = value;
			action.(this, value);
		});
	}

	action_ { |func|
		action = func;  // must override superclass action_
	}
}