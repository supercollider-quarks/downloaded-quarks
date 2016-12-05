
// extensions for easy layouts
// these overwrite and fix the broken extensions
// in Common/Gui/Base/viewExtensionsQt.sc


+ Window {

	asFlowView { arg bounds;
		^FlowView(this, bounds)
	}
	flow { arg func, bounds;
		var f;
		f = FlowView(this, bounds ?? { this.bounds.moveTo(0, 0) });
		func.value(f);
		if(bounds.isNil, {
			f.resizeToFit
		});
		^f
	}
	comp { arg func, bounds;
		var f;
		f = View(this, bounds ?? { this.bounds.moveTo(0, 0) });
		func.value(f);
		^f
	}
}


+ View {

	asFlowView { arg bounds;
		^FlowView(this, bounds ?? {this.bounds})
	}
	deepDo { arg function;
		// includes self
		function.value(this);
		this.children.do({arg child;
			child.deepDo(function);
		});
	}
	allChildren {
		// includes self
		var all;
		all = Array.new;
		this.deepDo({ arg child; all = all.add(child) });
		^all
	}
	flow { arg func, bounds;
		var f, comp;
		f = FlowView(this, bounds); // flow view intellegently calc defaults bounds
		func.value(f);
		if(bounds.isNil, {
			f.resizeToFit
		});
		^f
	}
	horz { arg func, bounds;
		var comp;
		comp = HorzLayoutView(this, bounds ?? { this.bounds });
		func.value(comp);
		^comp
	}
	vert { arg func, bounds;
		var comp;
		comp = VertLayoutView(this, bounds ?? { this.indentedRemaining });
		func.value(comp);
		^comp
	}
	comp { arg func, bounds;
		var comp;
		comp = View(this, bounds ?? { this.bounds });
		func.value(comp);
		^comp
	}
	scroll { arg func, bounds, autohidesScrollers=true, autoScrolls=true,
					hasHorizontalScroller=true, hasVerticalScroller=true;
		var comp;
		comp = ScrollView(this, bounds ?? { this.bounds });
		comp.autohidesScrollers = autohidesScrollers;
		comp.hasHorizontalScroller = hasHorizontalScroller;
		comp.hasVerticalScroller = hasVerticalScroller;
		func.value(comp);
		^comp
	}
}

+ FlowView {

	// place a FlowView on this FlowView
	flow { arg func, bounds;
		var f, consumed, b;
		if(bounds.notNil, {
			f = FlowView(this, bounds);
			func.value(f);
			^f
		});
		f = FlowView(this, this.allocateRemaining);
		func.value(f);
		consumed = f.resizeToFit;
		// did we exceed ?
		if(this.decorator.bounds.containsRect(consumed).not, {
			// yes
			// pretend I just consumed nothing
			this.didUseAllocated(consumed.resizeTo(0, 0));

			// goto the next line
			this.decorator.nextLine; // don't put a StartRow in there, the decorator should auto-flow on resize
			// take everything
			b = this.allocateRemaining;
			// and if its too big for that then it will just have to jutt or scroll over
			// that's what you asked for.
			// move the last object there
			f.bounds = b;
			// reflow the sub view
			f.reflowAll.resizeToFit;
			this.didUseAllocated(f.bounds);
		}, {
			this.didUseAllocated(consumed);
		});
		^f
	}

	vert { arg func, bounds;
		var comp;
		comp = VertLayoutView(this, bounds ?? { this.indentedRemaining });
		func.value(comp);
		^comp
	}
	horz { arg func, bounds;
		var comp;
		comp = HorzLayoutView(this, bounds ?? { this.bounds });
		func.value(comp);
		^comp
	}

	comp { arg func, bounds;
		var comp, b;
		b = bounds ?? { this.indentedRemaining };
		b = b.asRect;

		comp = CompositeView.new(this, b);
		func.value(comp);
		^comp
	}
	scroll { arg func, bounds,
				autohidesScrollers=true, autoScrolls=true,
				hasHorizontalScroller=true, hasVerticalScroller=true;
		var comp, b;
		b = bounds ?? { this.bounds };
		b = b.asRect;

		comp = ScrollView.new(this, b);
		comp.autohidesScrollers = autohidesScrollers;
		comp.hasHorizontalScroller = hasHorizontalScroller;
		comp.hasVerticalScroller = hasVerticalScroller;
		func.value(comp);
		^comp
	}
}
