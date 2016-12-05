/*
2013
Marinus Klaassen
rotterdamruis.nl
*/


MUI {
	classvar settings;

	*settings {
		^(
			font: Font("Menlo", 12, true, true, true),

			backgroundColor: Color.new255(* ({ 150 }!3 ++ 230)),
			specSettingColor: nil,
			controlSettinColor: nil,

			chOffset: 240,
			chHeight: 30,
			chGap: 5,
			xName: 10,
			widthName: 70,
			xLayers: 80,
			widthLayers: 415,
			xButtons: 500,
			widthButtons: 140,
			xRemove: 640,
			widthRemove: 15,
		)
	}

}


ProjectSaveAndLoad {
	var <canvas, <gui, >readAction, >storeAction;

	*new { ^super.new }

	makeGui { |argParent, argBounds|
		var width = argBounds.width, height = argBounds.height;
		canvas = CompositeView(argParent, argBounds)
		.background_(Color.clear);

		gui = { |i|
			Button(canvas, Rect(i * 0.5 * width, 0, width * 0.5, height))
			.states_([[["all: open file", "all: save to file"][i], Color.black, Color.red]])
			.action_({ var func = [{this.read}, {this.store}][i]; func.value; })
			.font_(Font("Menlo",12)) } ! 2;
	}

	store {
		Dialog.savePanel({ arg path; var temp;
			"The project state is saved to file.".postln;
			if (storeAction.notNil) {
				temp = storeAction.value;
				temp.writeArchive(path.postln);
		}},{ "cancelled".postln; });
	}

	read {  Dialog.getPaths({ arg paths;
		paths do: { arg p;
			if (readAction.notNil) {
				var temp = Object.readArchive(p.value.postln);
				readAction.value(temp);
			}

		}},{ "cancelled".postln; });
	}
}


ScoreWidget {
	var parent, <model, <setValueFunction, dependants, <gui, <canvas, widgets, <bounds, <>action;

	*new { ^super.new.init }

	init { }

	makeGui { }

	getState { ^model.copy }

	loadState { |preset| setValueFunction.value(preset) }

	closeGui { canvas.remove; gui do: (_.remove); gui = nil; }

	close {
		this.closeGui;
		model.dependants do: { |i| model.removeDependant(i) };
	}

	bounds_ { canvas.bounds = bounds }
}


ParamScript : ScoreWidget {
	var <string, popupParent;

	string_ { |argString|
		setValueFunction.value(argString);
	}

	loadState { |preset| this.string = preset[\script].asString }

	init {  |argString = ""|
		string = argString;
		model = (script: string);
		setValueFunction = { |script|
			model[\script] = script;
			model.changed(\script, script);
			model.postln;
		};
		dependants = ();
		dependants[\action] = {|theChanger, what, argString|
			string = argString;
			if(action.notNil) { action.value(string) };
		};
		model.addDependant(dependants[\action]);

	}

	makeGui { |argParent, argBounds|
		parent = argParent;
		bounds = argBounds.asRect;
		canvas = CompositeView(parent,bounds);
		canvas.background = Color.white.alpha_(0.5);
		widgets = ();
		widgets[\scriptField] = TextView(canvas,canvas.bounds.extent)
		.background_(Color.white.alpha_(0.5))
		.string_(model[\script])
		.keyDownAction_({| ... args|
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { setValueFunction.value(widgets[\scriptField].string) };
		})
		.enterInterpretsSelection_(false)
		.hasVerticalScroller_(false);
		dependants[\scriptField] = {|theChanger, what, argString|
			widgets[\scriptField].string_(argString);

		};
		model.addDependant(dependants[\scriptField]);

	}

	makePopupGui {
		if (popupParent.isNil) {
			popupParent = Window.new("SCRIPTFIELD")
			.onClose_({
				model.removeDependant(dependants[\popupWindow]);
				popupParent= nil;
				widgets[\popupScriptField] = nil;
			})
			.front;
			widgets[\popupScriptField] = TextView(popupParent, popupParent.bounds.extent)
			.background_(Color.white.alpha_(0.5))
			.string_(model[\script])
			.keyDownAction_({| ... args|
				var bool = args[2] == 524288;
				bool = args[1].ascii == 13 && bool;
				if (bool) { setValueFunction.value(widgets[\popupScriptField].string) };
			})
			.enterInterpretsSelection_(false);
			dependants[\popupWindow] = {|theChanger, what, argString|
				widgets[\popupScriptField].string_(argString);
			}
		} {
			popupParent.front
		};
	}
}


ParamController : ScoreWidget {
	var >rangeAction, >faderAction, <>spec, <lemur, <>ezLemurInstance, tempDependant, tempLemurDependant, <name;
	var <pagename, <objectReferenceName, <setValueFunction;

	loadState { |preset|
		setValueFunction[\fader].value(preset[\fader]);
		setValueFunction[\range].value(preset[\range]);
	}

	init {
		spec = ControlSpec();

		model = (fader: 0, range: [0, 1]);

		setValueFunction = ();
		setValueFunction[\fader] = { |value|
			model[\fader] = value;
			model.changed(\fader, value);
		};
		setValueFunction[\range] = { |value|
			model[\range] = value;
			model.changed(\range, value);
		};

		dependants = ();

		dependants[\faderAction] = {|theChanger, what, val|
			if (what == \fader) {
				if (faderAction.notNil) { faderAction.value(spec.map(val)) };
			};
		};

		dependants[\rangeAction] = {|theChanger, what, val|
			if (what == \range) {
				if (rangeAction.notNil) { rangeAction.value(spec.map(val)) };
			};
		};


		model.addDependant(dependants[\faderAction]);
		model.addDependant(dependants[\rangeAction]);
	}

	selectElement { |objectType|

		gui do: { |widget| widget.remove; gui[widget] = nil };
		if (tempDependant.notNil) { model.removeDependant(tempDependant); tempDependant = nil; };
		if (tempLemurDependant.notNil) { model.removeDependant(tempLemurDependant) };
		if (ezLemurInstance.notNil) { ezLemurInstance.remove };

		case { objectType == "Fader" } {
			if (gui.notNil) {
				gui[\fader] = Slider(canvas, bounds.extent)
				.value_(model[\fader])
				.action_({|val|setValueFunction[\fader].value(val.value)});
				tempDependant = {|theChanger, what, val|
					if (what == \fader) { { gui[\fader].value = val }.defer;  };
				};
				model.addDependant(tempDependant);
			};
			if (ezLemurInstance.notNil) {

				ezLemurInstance.makeGui(objectType, model[\fader]);
				ezLemurInstance.action = {|val| setValueFunction[\fader].value(val.first) };
				tempLemurDependant = {|theChanger, what, val|

					if (what == \fader) { ezLemurInstance.value = val; };
				};

				model.addDependant(tempLemurDependant);
			};
		} { objectType =="Range" } {
			if (gui.notNil) {
				gui[\range] = RangeSlider(canvas, bounds.extent)
				.lo_(model[\range][0]).hi_(model[\range][1])
				.action_({|val|setValueFunction[\range].value([val.lo,val.hi])});
				tempDependant = {|theChanger, what, val|
					if (what == \range) { { gui[\range].lo_(val[0]).hi_(model[\range][1]) }.defer; };
				};
				model.addDependant(tempDependant);
			};
			if (ezLemurInstance.notNil) {
				ezLemurInstance.makeGui(objectType, model[\range]);
				ezLemurInstance.action_({|val|setValueFunction[\range].value(val)});
				tempLemurDependant = {|theChanger, what, val|
					if (what == \range) { ezLemurInstance.value = val };
				};
				model.addDependant(tempLemurDependant);
			};
		};
	}

	makeGui {  |argParent, argBounds |
		parent = argParent;
		bounds = argBounds.asRect;
		gui = ();
		canvas = CompositeView(parent,bounds)
		.background_(Color.blue.alpha_(0.5));
	}

	close {
		this.closeLemur;
		this.closeGui;
		model.dependants do: { |i| model.removeDependant(i) };
	}

	closeGui {
		canvas.remove;
		gui do: (_.remove); gui = nil;
		if (tempDependant.notNil) { model.removeDependant(tempDependant); tempDependant = nil; };
	}

	restoreGui {  |argParent, argBounds, objectType |
		parent = argParent;
		bounds = argBounds.asRect;
		gui = ();
		canvas = CompositeView(parent,bounds)
		.background_(Color.blue.alpha_(0.5));

		case { objectType == "Fader" } {
			if (gui.notNil) {
				gui[\fader] = Slider(canvas, bounds.extent)
				.value_(model[\fader])
				.action_({|val|setValueFunction[\fader].value(val.value)});
				tempDependant = {|theChanger, what, val|
					if (what.postln == \fader) { { gui[\fader].value = val }.defer;  };
				};
				model.addDependant(tempDependant);
			};

		} { objectType =="Range" } {
			if (gui.notNil) {
				gui[\range] = RangeSlider(canvas, bounds.extent)
				.lo_(model[\range][0]).hi_(model[\range][1])
				.action_({|val|setValueFunction[\range].value([val.lo,val.hi])});
				tempDependant = {|theChanger, what, val|
					if (what == \range) { { gui[\range].lo_(val[0]).hi_(model[\range][1]) }.defer; };
				};
				model.addDependant(tempDependant);
			};

		};
	}

	moveLemur { |argxOffset| ezLemurInstance.move(argxOffset); }

	pagename_ { |argPagename|
		pagename = argPagename;
		if (ezLemurInstance.notNil) { ezLemurInstance.pagename = argPagename; };
	}

	objectReferenceName_ { |argObjectReferenceName|
		objectReferenceName = argObjectReferenceName;
		if (ezLemurInstance.notNil) { ezLemurInstance.objectReferenceName = objectReferenceName; };
	}

	name_ { |argName|
		name = argName;
		if (ezLemurInstance.notNil) { ezLemurInstance.name = name; };
	}

	initLemur {  |argLemur, xoffset|
		if (argLemur.notNil) { lemur = argLemur; };
		ezLemurInstance = EZLemurGui.new;
		ezLemurInstance.lemur = lemur;
		ezLemurInstance.pagename = pagename;
		ezLemurInstance.name = name;
		ezLemurInstance.objectReferenceName = objectReferenceName;
		ezLemurInstance.xOffset = xoffset;


	}

	closeLemur { ezLemurInstance.remove }

	lemur_ { |arglemur|
		lemur = arglemur;
		if (ezLemurInstance.isNil) { this.initLemur };
	}


}


ParamSpec : ScoreWidget {
	var <spec;

	init {
		model = (
			controlSpec: ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, ""),
			gui: nil
		);
		setValueFunction = {| argSpec|
			var controlSpec,code;
			if (argSpec.isKindOf(ControlSpec))
			{ controlSpec = argSpec }
			{ controlSpec = argSpec.asSpec; };

			code = controlSpec.asCode;
			"paramSpec setValueFunction".postln;
			controlSpec.class.postln;
			controlSpec.postln;
			code.class.postln;
			code.postln;



			model[\controlSpec] = controlSpec;
			model.changed(\controlSpec, controlSpec);
			model[\gui] = code;
			model.changed(\gui, code);
		};
		dependants = ();
		dependants[\action] = {|theChanger, what, argSpec|
			if(what == \controlSpec, {
				spec = argSpec;
				if (action.notNil) { action.value(argSpec) };
			});
		};
		model.addDependant(dependants[\action]);
	}

	spec_ { |argSpec| setValueFunction.value(argSpec) }

	makeGui { |argParent, argBounds|
		parent = argParent;
		bounds = argBounds.asRect;

		gui = ();

		canvas = CompositeView(parent, bounds)
		.background_(Color.yellow.alpha_(0.9));

		gui[\specText] = TextField(canvas,canvas.bounds.extent)
		.background_(Color.rand.alpha_(0))
		.string_(model[\gui])
		.action_({ |getSpec|
			var returnInterprettedCode = getSpec.value.interpret;
			if (returnInterprettedCode.notNil)
			{ "New ControlSpec asssigned".postln; setValueFunction.value(returnInterprettedCode) }
			{ "Spec is not assigned because of a writing mistake!".postln };
		});

		dependants[\gui] = {|theChanger, what, val|
			if(what == \gui, {
				gui[\specText].string_(val);
			});
		};
		model.addDependant(dependants[\gui]);

	}

	closeGui { model.removeDependant(dependants[\gui]) }

	loadState { |preset|
		setValueFunction.value(preset)
	}
}


EZButtons4  {
	var <action, <gui;

	*new { ^super.new.init; }

	init { action = IdentityDictionary.new; }

	makeGui { |parent, argBounds, gaps|
		var bounds = argBounds.asRect;
		var jumpWidth = bounds.width * 0.25;
		var height = bounds.height;
		gui = Array.new;
		[\red,\blue,\yellow,\black] do: { |color, i|
			gui = gui.(
				Button(parent,
					Rect(i * jumpWidth + bounds.left, bounds.top, jumpWidth - gaps, bounds.height)
				)
				.states_([[""] ++ color.asColor.dup(2)])
				.action_({ if (action[i].notNil) { action[i].value }})
			);
		};
	}
}


ParamChannel : ScoreWidget {
	var <pScript, <paramController, <pSpec, ez4Buttons, <name, <>currentLayerIndex, <>currentWidgetType, <>currentWidgetIndex, previousLayer;
	var <>nameAction, <>removeAction, <>index, <>paramProxy, <>controllerProxies, <>scriptFunc;

	*new { |argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|
		^super.newCopyArgs.init(argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName)
	}

	init { |argName, argIndex, argLemur, argLemurXoffset, argPageName, argObjectReferenceName|

		name = argName;
		scriptFunc = {};
		currentLayerIndex = 0;
		currentWidgetType = "Empty";
		currentWidgetIndex = argIndex;
		index = argIndex;
		pScript = ParamScript.new;
		paramController = ParamController.new;
		paramController.lemur = argLemur;
		paramController.pagename = argPageName;
		paramController.objectReferenceName = argObjectReferenceName;
		paramController.name = argName.asString;
		paramController.initLemur(argLemur, argLemurXoffset);
		paramController.spec = ControlSpec(); // to be initialize

		pSpec = ParamSpec.new;
		pSpec.action = { |argSpec|
			"a new spec:".postln;
			paramController.spec = argSpec.postln;
			"is added".postln;
		};
		pSpec.spec = ControlSpec();

		ez4Buttons = EZButtons4.new;

		ez4Buttons.action[0] = { pScript.makePopupGui };

		ez4Buttons.action[1] = {
			var showLayer, objectType;
			currentWidgetIndex = currentWidgetIndex + 1 % 3;
			currentLayerIndex = 1;
			if (currentWidgetIndex == 0) {

				showLayer = pScript.canvas;
			} {
				showLayer = paramController.canvas;
			};
			previousLayer.visible = false;
			showLayer.visible = true;
			currentWidgetType = ["Empty", "Fader", "Range"][currentWidgetIndex];
			paramController.selectElement(currentWidgetType);
			previousLayer = showLayer;
		};

		ez4Buttons.action[2] = {
			var showLayer;
			currentLayerIndex = currentLayerIndex + 1 % 3;
			// if (currentLayerIndex == 1) { currentLayerIndex = currentLayerIndex + 1 };
			showLayer = [pScript.canvas, paramController.canvas, pSpec.canvas][currentLayerIndex];
			previousLayer.visible = false;
			showLayer.visible = true;
			previousLayer = showLayer;
		};

		ez4Buttons.action[3] = { "button action to be implemented".postln; };
	}

	moveBounds { |x = 0, y = 0| canvas.moveTo(x,y) }

	moveLemur { |argxOffset| paramController.moveLemur(argxOffset); }

	name_  { |argName|
		name = argName.asString;
		if (nameAction.notNil) { nameAction.value(name) };
		paramController.name = name;
		"what is this??".postln;
		if (gui.notNil) { gui[\name].string = if (name.isKindOf(String)) { name } { name.asCode }; };
	}

	makeGui { |parent, bounds|
		var layerBounds;

		canvas = CompositeView(parent, bounds)
		.background_(Color.red.alpha_(0.4));

		gui = ();
		gui[\name] = TextField(canvas, Rect(
			MUI.settings[\xName],
			MUI.settings[\chGap],
			MUI.settings[\widthName],
			MUI.settings[\chHeight]
			)
		).string_(name.asString)
		.action_({|argName|
			name = argName.string.asString;
			// name = interpret(argName.string);
			if (nameAction.notNil) { nameAction.value(name) };
			paramController.name = name;
		});

		layerBounds = Rect(
			MUI.settings[\xLayers],
			MUI.settings[\chGap],
			MUI.settings[\widthLayers],
			MUI.settings[\chHeight]);

		pScript.makeGui(canvas, layerBounds);

		previousLayer = pScript.canvas;

		paramController.makeGui(canvas, layerBounds);

		if (currentWidgetType == "Fader" || (currentWidgetType == "Range")) {
			previousLayer.visible_(false);
			previousLayer = paramController.canvas.visible_(true);
			paramController.restoreGui(canvas, layerBounds, currentWidgetType);
		} { paramController.canvas.visible_(false); };

		pSpec.makeGui(canvas, layerBounds);
		pSpec.canvas.visible_(false);

		ez4Buttons.makeGui(canvas, Rect(
			MUI.settings[\xButtons],
			MUI.settings[\chGap],
			MUI.settings[\widthButtons],
			MUI.settings[\chHeight]), 5);

		gui[\remove] = MButtonV(canvas, Rect(
			MUI.settings[\xRemove],
			MUI.settings[\chGap],
			MUI.settings[\widthRemove],
			MUI.settings[\widthRemove]))
		.action_({ if (removeAction.notNil) { removeAction.value(index) } });
	}

	closeGui {
		canvas.remove; gui do: (_.remove); gui = nil;
		pScript.closeGui;
		paramController.closeGui;
		pSpec.closeGui;
	}

	closeLemur { paramController.closeLemur; }

	loadState { |aPreset|
		this.name = aPreset[\name];
		pSpec.loadState(aPreset[\spec]);
		pScript.loadState(aPreset[\script]);
		paramController.loadState(aPreset[\paramController]);
		paramController.selectElement(aPreset[\paramControllerCurrentWidget]);
	}

	getState {
		var preset = Dictionary.new;
		preset[\name] = name.copy;
		preset[\script] = pScript.getState;
		preset[\paramController] = paramController.getState.copy;
		preset[\paramControllerCurrentWidget] = currentWidgetType.copy;
		preset[\spec] = pSpec.spec.copy;
		^preset;
	}
}
