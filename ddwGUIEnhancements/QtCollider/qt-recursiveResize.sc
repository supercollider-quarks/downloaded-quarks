
// resize everything in a view's chain

+ View {
	recursiveResize {
		var owner;
		if((owner = this.decorator.tryPerform(\owner)).notNil) {
			owner.recursiveResize;
		};
	}

	findRightBottom {
		var maxpt;
		maxpt = this.bounds.rightBottom;
		if(decorator.notNil) {
			maxpt = maxpt + decorator.margin;
		};
		^maxpt
	}

	isActive { ^this.isClosed.not }

	isView { ^true }
}

+ DragView {
	silentObject_ { |obj| object = obj }
}
