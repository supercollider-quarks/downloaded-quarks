VGMulTreeGui {
	var <tree, <name, <parent;
	var <>refValues;

	classvar <paramUnits, <unitSpecs, <relativeUnitSpecs;

	*initClass {

		paramUnits = (
			\freq: [ \ratio, \cent, \Hz ],
			\amp: [\ratio, \db, \amp],
			\attack: [\ratio, \sec],
			\ringtime: [\ratio, \sec]
		);
		unitSpecs = (
			\Hz: \freq.asSpec,
			\sec: [-1200, 1200, \lin, 0, 0, "cent"].asSpec //was -4800, 4800
		);

		relativeUnitSpecs = (
			\ratio: [0.0625, 16, \exp, 0, 1, "rel"].asSpec,
			\cent: [-1200, 1200, \lin, 0, 0, "cent"].asSpec, //was -4800, 4800
			\db: [-80, 20, \lin, 0, 0, "dB"].asSpec,
			\amp: [0, 10, \amp, \lin, 0, 1, "rel"].asSpec
		);

	}

	*new { |tree, name, parent, bounds|
		^super.newCopyArgs(tree).init(name, parent, bounds).initRefValues;
	}

	initRefValues {
		refValues = (
			freq: 440,
			amp: 0.1,
			attack: 0.01,
			ringtime: 1
		);
	}

	/*convert { |val, key, unit|
		var isAbsolute = [\Hz, \sec, \amp].includes(key);
		var unitNames = paramUnits[key];
		var specs = unitSpecs.atKeys(unitNames);
		var origSpec = unitSpecs[unit];
		var refVal = refValues[key];
		var relVal = if (isAbsolute) { val / refValues[key] } { val };
		var normRelVal = origSpec.unmap(val);

		// = origSpec.unmap(val);

		// ^specs.collect(_.map(normVal));

	}*/

	init { |inName, bounds|

		var totalWidth = 720;
		var unitWidth = 60, unitHeight = 40, gap = 2@2;
		var w = Window("VG Tuning Editor", Rect(400,800,totalWidth, 700)).front;

		var argNames = tree.argNames;
		var editorNames = tree.branchNames;
		var comp, makeEditor;


		comp = CompositeView(w, Rect(4,4, 98, 700))
				.background_(Color.gray(0.9));
			comp	.decorator_(FlowLayout(comp.bounds, gap * 2, gap * 2));

		name = inName;

		Button(comp, Rect(2, 2, 90, unitHeight))
			.states_([[name.asString]])
			.font_(Font(*["Helvetica", 24]));

		editorNames.do { |name|
			Button(comp, Rect(0,0,90,unitHeight))
				.states_([[name]])
				.font_(Font(*["Helvetica", 16]));
		};

		w.view.decorator = FlowLayout(w.bounds.moveTo(100,0), gap * 2, gap * 2);

			// topLine
		argNames.do { |paramName|
			var mywidth, comp;
			var unitNames = paramUnits[paramName];
			var initVals = tree.values;

			mywidth = unitNames.size * unitWidth;

			comp = CompositeView(w, Rect(0,0, mywidth, unitHeight + 4))
				.background_(Color.gray(0.9));
			comp	.decorator_(FlowLayout(comp.bounds, 0@0, 0@0));

			StaticText(comp, Rect(0, 0, unitNames.size * unitWidth - 5, 20))
				.align_(1)
				.background_(Color.gray(0.9))
				.string_(paramName)
				.font_(Font(*["Helvetica", 16]));

				unitNames.do { |name, j|
					StaticText(comp, Rect(0, 0, unitWidth - 4, 20))
					.string_(name);
				};
		};

		editorNames.do { |name|
			argNames.do { |paramName, i|
				var mywidth, comp;
				var unitNames = paramUnits[paramName];
				var initVals = tree.values;
				mywidth = unitNames.size * unitWidth;

				comp = CompositeView(w, Rect(0,0, mywidth, unitHeight))
					.background_(Color.gray(0.9));
				comp	.decorator_(FlowLayout(comp.bounds, 2@0, 2@4));
				unitNames.do { |unitName, j|

					var box = NumberBox(comp, Rect(0, 0, unitWidth - 4, 18))
						.value_(initVals[j]);
					if(unitName == \ratio) {
						box.value = tree.nodeAt(name).mulBus.get(paramName).unbubble;
						box.action = {
							tree.nodeAt(name).set(paramName, box.value);
							"setting % to %\n".postf(paramName, box.value);
						}
					} {
						 box.enabled = false
					};
				};
				Slider(comp, Rect(0, 0, mywidth - 4, 18))
			};
		};
	}


}