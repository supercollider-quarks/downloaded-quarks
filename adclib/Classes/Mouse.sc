

Mouse {
	classvar <all, <task, <screenSize, pos, <prevPos;
	var <name, <>action, <>xspec, <>yspec;

	*initClass {
		all = ();
		screenSize = Window.screenBounds.extent;
	}

	*pos {
		if (task.isPlaying) { ^pos };
		^pos = QtGUI.cursorPosition;
	}

	*initTask {
		task ?? {

			task = SkipJack({
				pos = QtGUI.cursorPosition;
				if (pos != prevPos) {
					this.doActions;
				};
				prevPos = pos;
			}, 0.02, false, "Mouse", AppClock)
		}
	}

	*doActions {
		all.do { |mouse|
			try {
				mouse.doAction
			} {
				"%.action failed.\n".postf(mouse)
			}
		}
	}

	*free {
		task.stop;
		task = nil;
	}

	*start { this.initTask; task.play }
	*stop { task.stop }

	*x { ^this.pos.x }
	*y { ^this.pos.y }

	*xuni { ^(pos.x / screenSize.x) }
	*yuni { ^(pos.y / screenSize.y) }

	*new { |name, action, xspec, yspec|
		var res;
		this.initTask; // lazy init task here

		res = all[name];
		if (res.notNil) {
			action !? { res.action = action };
			xspec !? { res.xspec = xspec.asSpec };
			yspec !? { res.yspec = yspec.asSpec };
			^res
		};

		^super.newCopyArgs(name, action, xspec, yspec).init.prAdd;
	}
	init {
		xspec !? { xspec = xspec.asSpec };
		yspec !? { yspec = yspec.asSpec };
	}
	prAdd { all.put(this.name, this) }

	free { action = nil; all.removeAt(name) }

	xval {
		var xval = Mouse.xuni;
		xspec !? { xval = xspec.map(xval) };
		^xval
	}
	yval {
		var yval = Mouse.xuni;
		yspec !? { yval = yspec.map(yval) };
		^yval
	}

	doAction {
		action.value(this, this.xval, this.yval, Mouse.x, Mouse.y);
	}

	storeArgs { ^[name] }
	printOn { |str| this.storeOn(str) }
}