+Function {
	doAfter {
		|obj|
		if (obj.respondsTo(\addDoAfter)) {
			obj.addDoAfter(this);
		} {
			Error("Target (%) cannot register objects for freeAfter/doAfter.".format(obj)).throw;
		}
	}
}

+Object {
	freeAfter {
		|obj|
		{ this.free }.doAfter(obj)
	}

	closeAfter {
		|obj|
		if (this.respondsTo(\close).not) {
			Error("Object (%) does not respond to .close".format(this)).throw;
		};
		{ this.close }.doAfter(obj);
	}
}

+View {
	closeAfter {
		|obj|
		{ { this.close }.defer }.doAfter(obj);
	}
}

+Window {
	closeAfter {
		|obj|
		{ { this.close }.defer }.doAfter(obj);
	}
}

+Number {
	addDoAfter {
		|func|
		func.defer(this);
	}
}

+Condition {
	addDoAfter {
		|func|
		fork {
			this.wait;
			func.();
		}
	}
}

+CmdPeriod {
	*addDoAfter {
		|func|
		CmdPeriod.doOnce(func)
	}
}

+Node {
	addDoAfter {
		|func|
		this.onFree(func)
	}
}

+Server {
	addDoAfter {
		|func|
		var oneTimeFunc = {
			this.onQuitRemove(oneTimeFunc);
			func.();
		};
		this.onQuitAdd(oneTimeFunc);
	}
}

+PauseStream {
	addDoAfter {
		|func|
		var oneTimeFunc = {
			|who, what|
			if (what == \stopped) {
				this.removeDependant(oneTimeFunc);
				func.();
			}
		};

		this.addDependant(oneTimeFunc);
	}
}

+View {
	addDoAfter {
		|func|
		this.onClose = this.onClose.addFunc(func);
	}
}

+Window {
	addDoAfter {
		|func|
		this.onClose = this.onClose.addFunc(func);
	}
}