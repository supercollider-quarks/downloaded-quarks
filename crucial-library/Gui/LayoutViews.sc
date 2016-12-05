
VertLayoutView : SCViewHolder {

	// a CompositeView with a VertLayout as its decorator

	*viewClass { ^CompositeView }
	decoratorClass {
		^VertLayout
	}

	*new { arg parent, bounds, gap;
		^super.new.init(parent, bounds, gap);
	}
	init { arg parent, bounds, margin, gap;
		var w, parentView;
		parentView = parent.asView;
		if(bounds.notNil, {
			bounds = bounds.asRect;
		}, {
			bounds = parentView.bounds.moveTo(0, 0);
		});
		this.view = this.class.viewClass.new(parentView, bounds);

		// parent has placed me, now get my bounds
		bounds = view.bounds.moveTo(0, 0);

		view.decorator = this.decoratorClass.new(bounds, gap, false);
		view.decorator.owner = this;
	}
	add { |child|
		view.add(child);
	}

	innerBounds { ^this.decorator.innerBounds }
	bounds_ { arg b;
		if(b != view.bounds, {
			view.bounds = b;
			if(this.decorator.notNil, {
				this.decorator.bounds = b.moveTo(0, 0);
			})
		});
	}

	remove {
		view.notClosed.if({
			view.remove;
		});
	}
	viewDidClose {
		view.tryPerform(\viewDidClose);
	}

	children { ^view.children }
	decorator { ^view.decorator }
	decorator_ { |dec| view.decorator = dec }
	removeAll {
		view.removeAll;
		this.decorator.clear;
	}
}


HorzLayoutView : VertLayoutView {

  // a CompositeView with a HorzLayout as its decorator

	decoratorClass {
		^HorzLayout
	}
}
