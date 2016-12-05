
CompassView {
	var <value = 0, <>skin, <>action;
	var <nameView, <zone, <buttons;

	*new { |parent, bounds|
		^super.new.init(parent, bounds);
	}

	value_ 	{ |val|
		value = val;
		this.buttonReset(value);
	}

	valueAction_ 	{ |val|
		this.value_(val);
		action.value(this);
	}

	buttonReset { |val|
		buttons.do { |but, i|
			if (i == val) { but.value_(1) } { but.value_(0) }
		};
	}

	init { |parent, bounds|
		var center = bounds.extent * 0.5;
		zone = CompositeView(parent, bounds)
			.background_(Color.grey(0.8, 0.5));
		skin = GUI.skins.jit;

		nameView = StaticText(zone, Rect.aboutPoint(center, 30, 10))
			// .string_(el.name)

			.align_(\center);

		buttons = 9.collect { |i|
			var radius = 35 * i.sign;
			var angle = (i + 4 / 8 * -2pi );
			var butcent = center + Polar(radius, angle).asPoint;
			Button(zone, Rect.aboutPoint(butcent, 10, 10))
				.states_([[i.asString, skin.fontColor, skin.offColor ],
					[i.asString, skin.fontColor, skin.onColor ]])
				.action_({ |but|
					this.valueAction_(i);
				});
		}
	}
}