
VGRatioEditor {
	var <refVal=1, <baseVal=3, <extDetune=1.0, <localDetune=1.0, <totalDetune=1.0, <currVal;
	var <>sendFunc;
	var <comp, <boxes, <>names, width, <slSpec, slider;

	*new { |refVal, baseVal, totalDetune, localDetune, names, parent, top=true, slide=false|
		var defaults = this.defaultVals;
		refVal = refVal ? defaults[0];
		baseVal = baseVal ? defaults[1];
		totalDetune = totalDetune ? defaults[2];
		localDetune = localDetune ? defaults[3];

		^super.newCopyArgs(refVal, baseVal, totalDetune, localDetune)
			.init(parent, top, slide);
	}
	*defaultNames {
		^[
			[\refVal, \baseVal, \extDetune],
			[\currVal, \totalDetune, \localDetune]
		]
	}
	*defaultVals { ^[1, 2, 1, 1] }

	visible_ { |flag| comp.visible_(flag) }
	visible { ^comp.visible }

	init { |parent, top, slide|
		names = names ?? { this.class.defaultNames };
		this.baseCalc;
		this.altCalc;

		width = names.first.size * 70;
		this.gui(parent, top, slide);
		this.updateBoxes;
	}

	baseCalc {
		totalDetune = extDetune * localDetune;
		currVal = baseVal * totalDetune;
	}
	altCalc {
		// use in subclasses for secondary representations
		sendFunc.(localDetune);
	}

	gui { |parent, top, slide|
		var 	gap = 2@2;
		var unitWidth = 70, unitHeight=22;
		var height = 2 + (top.binaryValue * 32) +
			(2 * unitHeight) + (slide.binaryValue * unitHeight);
		width = unitWidth * names.first.size;

		parent = parent ?? {
			parent = Window(this.class.asString, Rect(440,800, width + 4 , 200)).front;
			parent.view.decorator = FlowLayout(parent.bounds.moveTo(0,0), gap, gap);
			parent;
		};
		comp = comp ?? {
			comp = CompositeView(parent, Rect(0,0, width, height))
				.background_(Color.gray(0.9));
			comp.decorator = FlowLayout(comp.bounds, gap, gap);
			comp;
		};

		if (top) { this.topLine(comp) };
		this.makeBoxes(comp);
		if (slide) { this.makeSlide(comp) };
	}

	topLine { |comp|

		names.flat.do { |paramName|
			StaticText(comp, Rect(0, 0, width / names.first.size - 3, 16))
			//	.align_(1)
				.background_(Color.gray(0.9))
				.string_(paramName)
				.font_(Font(*["Helvetica", 11]));
		  };
	}

	makeBoxes { |comp|
		boxes = [
			{|box| this.refVal_(box.value) },
			{|box| this.baseVal_(box.value) },
			{|box| this.extDetune_(box.value) },

			{|box| this.currVal_(box.value) },
			{|box| this.totalDetune_(box.value) },
			{|box| this.localDetune_(box.value);
				sendFunc.(localDetune)
			}

		].collect { |func, i|
			NumberBox(comp, Rect(0,0, width / names.first.size - 3, 20))
			     .decimals_(3)
			     .action_(func);
		};
		boxes[[0, 1, 2]].do { |box| box.enabled_(false).scroll_(false) };
	}
	makeSlide { |comp|
		slSpec = slSpec ?? { [0.0625,16,\exp].asSpec };
		slider = Slider(comp, Rect(0,0,comp.bounds.width - 4,18))
			.action_{ |sl| this.localDetune_(slSpec.map(sl.value)) }
	}
	allVals {
		^[	refVal, baseVal,  extDetune,
			currVal, totalDetune, localDetune]
	}
	updateBoxes {
		if (comp.isClosed.not) {
			this.allVals.do { |val, i| boxes[i].value_(val.round(0.0001)) };
			if (slider.notNil) { slider.value_(slSpec.unmap(localDetune)) };
		}
	}

		// simple forward calcs:
	refVal_ { |inval|
		refVal = inval;
		this.baseCalc;
		this.altCalc;
		this.updateBoxes;
	}

	baseVal_ { |inval|
		baseVal = inval;
		this.baseCalc;
		this.altCalc;
		this.updateBoxes;
	}

	extDetune_ { |inval|
		extDetune = inval;
		this.baseCalc;
		this.altCalc;
		this.updateBoxes;
	}

	localDetune_ { |inval|
		localDetune = inval;
		this.baseCalc;
		this.altCalc;
		this.updateBoxes;
	}

		// indirect calculations:
	totalDetune_ { |inval|
		totalDetune = inval;
		localDetune = totalDetune / extDetune;
		currVal = baseVal * totalDetune;
		this.altCalc;
		this.updateBoxes;
	}

	currVal_ { |inval|
		currVal = inval;
		totalDetune = currVal / baseVal;
		localDetune = totalDetune / extDetune;
		this.altCalc;
		this.updateBoxes;
	}
}

VGTimeEditor : VGRatioEditor {
	*defaultNames {
		^[ 	[\refTime, \baseTime, \extDetune],
			[\currTime, \totalDetune, \localDetune]
		]
	}
}