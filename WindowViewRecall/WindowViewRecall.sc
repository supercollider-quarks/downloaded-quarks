WindowViewRecall {
	classvar updatePosition;

	*initClass {
		Archive.read();
		updatePosition = Collapse({
			|bounds, addr|
			WindowViewRecall.rememberPosition(bounds, *addr);
		}, 1);
	}

	*save {
		Archive.write();
	}

	*autoRememberPosition {
		| that ...addr |

		var viewFunc = {
			|v|
			var bounds;
			if (v.isClosed.not) {
				bounds = v.bounds;
				if (bounds.isKindOf(Rect)) { updatePosition.(bounds, addr) }
			}
		};
		var windowFunc = {
			|w|
			var bounds;
			if (w.isClosed.not) {
				bounds = w.findWindow.bounds;
				if (bounds.isKindOf(Rect)) { updatePosition.(bounds, addr) }
			}
		};

		if (that.isKindOf(Window)) {
			that.view.onMove = that.view.onMove.addFunc(windowFunc);
			that.view.onResize = that.view.onResize.addFunc(windowFunc);
		} {
			that.onMove = that.onMove.addFunc(viewFunc);
			that.onResize = that.onResize.addFunc(viewFunc);
		};

		that.recallPosition(*addr);
	}

	*rememberPosition {
		| bounds ...addr |
		Archive.global.put(*([\WindowPositions] ++ addr ++ [ bounds ]));
		Archive.write();
	}

	*recallPosition {
		| that ...addr |
		var bounds = Archive.global.at(*([\WindowPositions] ++ addr));
		if (bounds.notNil) {
			that.bounds = bounds;
		}
	}

	*resetWindowPositions {
		| ...addrs |
		if (addrs.isEmpty) {
			Archive.global.put(\WindowPositions, nil);
		} {
			addrs.do({
				| addr |
				Archive.global.put(*([\WindowPositions] ++ addr ++ [nil]));
			});
		};
		Archive.write();
	}
}

+ Window {
	autoRememberPosition {
		| ...addr |
		WindowViewRecall.autoRememberPosition(this, *addr);
	}

	rememberPosition {
		| ...addr |
		if (this.isClosed.not) {
			WindowViewRecall.rememberPosition(this.bounds, *addr);
		}
	}

	recallPosition {
		| ...addr |
		WindowViewRecall.recallPosition(this, *addr);
	}

	resetPosition {
		| ...addr |
		WindowViewRecall.resetWindowPositions(addr);
	}
}

+ View {
	autoRememberPosition {
		| ...addr |
		WindowViewRecall.autoRememberPosition(this, *addr);
	}

	rememberPosition {
		| ...addr |
		if (this.isClosed.not) {
			WindowViewRecall.rememberPosition(this.bounds, *addr);
		}
	}

	recallPosition {
		| ...addr |
		WindowViewRecall.recallPosition(this, *addr);
	}

	resetPosition {
		| ...addr |
		WindowViewRecall.resetWindowPositions(addr);
	}
}