MButtonV {
	var <>view, <>action, state;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect);
	}

	init { |parent, bounds|
		state = false;
		view = UserView(parent,bounds);
		view.background = Color.white;
		view.drawFunc = {
			var radius = min(bounds.width, bounds.height) * 0.7;
			var widthLine = min(bounds.width, bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ (radius * 0.5).neg,
				(radius * 0.5).neg @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5),
				(radius * 0.5) @ (radius * 0.5).neg
			];


			Pen.smoothing = true;
			Pen.translate(bounds.width * 0.5, bounds.height * 0.5);
			Pen.width = widthLine;
			// draw cross.
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.stroke;

			if (state, {
				Pen.translate(neg(bounds.width * 0.5),neg(bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = Color.red;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});
		};

		view.refresh;
		view.mouseDownAction = { state = true; view.refresh; };
		view.mouseUpAction = { state = false; view.refresh; if (action.notNil, { action.value }) };
	}

	resize { |param = 0| "here".postln; view.resize = param }

	remove { view.remove }
}



/*
w = Window.new.front; a = MButtonV(w,Rect(0,0,50,50)).action_({ "release function".postln; }); a.resize = 3
*/

MButtonP {
	var <>view, <>action, state;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect);
	}

	init { |parent, bounds|
		state = false;
		view = UserView(parent,bounds);
		view.background = Color.new(0.8,0.8,0.8);
		view.drawFunc = {
			var radius = min(bounds.width, bounds.height) * 0.75;
			var widthLine = min(bounds.width, bounds.height) * 0.1;
			var cp = [
				(radius * 0.5).neg @ 0,
				0 @ (radius * 0.5),
				(radius * 0.5) @ 0,
				0 @(radius * 0.5).neg
			];

			Pen.strokeColor = Color.new(0.2,0.2,0.2);
			Pen.smoothing = true;
			Pen.translate(bounds.width * 0.5, bounds.height * 0.5);
			Pen.width = widthLine;
			// draw plus
			Pen.line(cp[0],cp[2]);
			Pen.line(cp[1],cp[3]);
			Pen.fillStroke;

			if (state, {
				Pen.translate(neg(bounds.width * 0.5),neg(bounds.height * 0.5));
				Pen.width = widthLine * 0.5;
				Pen.strokeColor = Color.red;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});
		};

		view.refresh;
		view.mouseDownAction = { state = true; view.refresh; };
		view.mouseUpAction = { state = false; view.refresh; if (action.notNil, { action.value }) };
	}

	remove { view.remove }

}

/*
w = Window.new.front; a = MButtonP(w,Rect(0,0,80,80)).action_({ "release function".postln; });
a.remove; w.front;
*/

MToggle4 {
	var <>view, <>action, <>state, <>value, <>quadColor, <>offBalance;

	*new { |parent, bounds|
		^super.newCopyArgs.init(parent,bounds.asRect).quadColor_(Color.black).offBalance_(0).state_(false);
	}

	init { |parent, bounds|
		view = UserView(parent,bounds);
		view.background = Color.new(0.8,0.8,0.8).alpha_(0);
		value = 0;
		view.drawFunc = {
			Pen.smoothing = true;

			if (state, {
				Pen.width = if (bounds.width < bounds.height) { bounds.width } { bounds.height } * 0.05;
				Pen.strokeColor = Color.red;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			},
			{
				Pen.width = if (bounds.width < bounds.height) { bounds.width } { bounds.height } * 0.05;
				Pen.strokeColor = Color.black;
				Pen.moveTo(0@ 0);
				Pen.lineTo(0@ bounds.height);
				Pen.lineTo(bounds.width @ bounds.height);
				Pen.lineTo(bounds.width @ 0);
				Pen.lineTo(0@0);
				Pen.stroke;
			});


			Pen.color = quadColor;
			Pen.width = if (bounds.width < bounds.height) { bounds.width } { bounds.height } * 0.1;
			if (value < 3) { Pen.rotate(pi * offBalance.rand2); };
			Pen.addRect(
			Rect(
					bounds.width * #[ 0, 0.15, 0.35, 0.45][value],
					bounds.height * #[ 0, 0.15, 0.35, 0.45][value],
					bounds.width * #[ 0, 0.7, 0.3, 0.1][value],
					bounds.height * #[ 0, 0.7, 0.3, 0.1][value]
				)
			);
			Pen.perform(\fill);


		};

		view.refresh;

		view.mouseDownAction = {
			state = true;
			view.refresh;
			value = value + 1 % 4;
			if (action.notNil, { action.value(value) })
		};

		view.mouseUpAction = {
			state = false;
			view.refresh;

		};
	}

	remove { view.remove }

}



