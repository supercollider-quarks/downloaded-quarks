
VertLayout : Decorator {

	var <>bounds, <>gap;
	var <>current;
	var <>owner;

	*new { arg bounds, gap;
		^super.newCopyArgs(bounds, gap ? 4).init
	}
	init {
		this.reset;
	}
	clear { this.reset; }
	reset {
		current = bounds.top;
	}
	place { arg view;
		var height;
		height = view.bounds.height;
		view.bounds = Rect(bounds.left, current, bounds.width, height);
		current = current + height + gap;
	}
	remove { }
	innerBounds {
		^bounds
	}
}


HorzLayout : VertLayout {

	reset {
		current = bounds.left;
	}
	place { arg view;
		var width;
		width = view.bounds.width;
		view.bounds = Rect(current, bounds.top, width, bounds.height);
		current = current + width + gap;
	}
}
