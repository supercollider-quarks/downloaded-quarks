WindowHandleView : UserView {
	var dragging, dragStart, <>win, isWin;

	*qtClass { ^'QcCustomPainted' }

	*new { arg parent, bounds;
		^super.new(parent, bounds ?? {this.sizeHint} ).init;
	}

	init {
		this.canFocus = false;
		this.setDefaultBackground();
		this.drawFunc = this.drawBorder(_);
		this.mouseDownAction_(this.mouseDown(_,_,_));
		this.mouseMoveAction_(this.mouseMove(_,_,_));
		this.mouseUpAction_(this.mouseUp(_,_,_));
	}

	mouseDown {
		|x, y|
		var parents = this.parents;
		isWin = false;
		win = parents !? { parents.last };
		if (win.respondsTo(\findWindow)) {
			win = win.findWindow();
			isWin = true;
		} {
			win = win ?? this;
		};
		dragStart = x@y;
	}

	mouseMove {
		|x, y|
		var moved, newPos = x@y;
		moved = newPos - dragStart;
		if (isWin) { moved.y = moved.y.neg };
		win.bounds = win.bounds.moveBy(moved.x, moved.y);
	}

	mouseUp {
		|v, x, y|
	}

	setBackgroundImage{
		arg image, tileMode=5, alpha=1.0, fromRect; // default file mode differs from base class
		super.setBackgroundImage(image, tileMode, alpha, fromRect);
	}

	drawBorder {
		var b, w, h;
		b = this.bounds.moveTo(0, 0);
		w = b.width;
		h = b.height;
		Pen.strokeColor = Color.grey(0.1, 0.3);
		Pen.line(0@h, 0@0);
		Pen.line(0@0, w@0);
		Pen.stroke();

		Pen.strokeColor = Color.grey(0.5, 0.2);
		Pen.line(w@0, w@h);
		Pen.line(w@h, 0@h);
		Pen.stroke();
	}

	setDefaultBackground {
		|v|
		var image, w = 63, h = 63, onW=4, offW=3, color, height;
		image = Image.newEmpty(w, h);
		image.draw({
			var x;
			(w / onW + (h / onW)).do {
				| x |
				Pen.width = 1;

				x = x * (onW + offW);
				Pen.strokeColor = Color.grey(0.5, 0.6);
				Pen.line(x@0, (x-h)@h);
				Pen.stroke();

				x = x + onW;
				Pen.strokeColor = Color.grey(0.1, 0.4);
				Pen.line(x@0, (x-h)@h);
				Pen.stroke();
			};
		});
		this.setBackgroundImage(image);
	}
}