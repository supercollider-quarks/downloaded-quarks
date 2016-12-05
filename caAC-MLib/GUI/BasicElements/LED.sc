
LED {
	var <>view, <value, <bounds;
	var <>colorBlinkOff, <>colorBlinkOn;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect);
	}

	init { |parent, argBounds|

		bounds = argBounds;

		view = UserView(parent,bounds);
		view.background = Color.clear;

		colorBlinkOff = Color.red(0.2, 0.3);
		colorBlinkOn = Color.yellow(0.8);

		view.drawFunc={|uview|

			var width = bounds.width;
			var height = bounds.height;
			var smallestRadius = if (width < height) { width } { height };

			Pen.translate(width * 0.5, height * 0.5);

			Pen.color = if (value > 0) { colorBlinkOn } { colorBlinkOff };

			Pen.addAnnularWedge(0,
				smallestRadius * 0.5,
				smallestRadius * 0.2,
				0,
				2pi
			);
			Pen.perform(\fill);
		};

		view.refresh;
	}

	bounds_ { |argBounds|
		bounds = argBounds;
		view.bounds = argBounds;
		view.refresh;
	}

	value_ {  |argValue|
		value = argValue;
		view.refresh;
	}

	resize { |param = 0| "here".postln; view.resize = param }

	remove { view.remove }
}