// BASIC METHODS AND ARTIBUTES TOP-LEVEL PSEUDO-CLASS
~scoreAndController = ();

~scoreAndController.font = Font("Menlo", 12, true, true, true);

~scoreAndController.guiSettings = (
	background: Color.new255(* ({ 150 }!3 ++ 230)),
	specSettingColor: nil,
	controlSettinColor: nil
);

~scoreAndController.posSettings = (
	channelOffset: 240,
	channelHeight: 30,
	channelGap: 5,
	xName: 10,
	widthName: 60,
	xWorkSpace: 80,
	widthWorkSpace: 415,
	xEnlarge: 505,
	widthEnlarge: 30,
	xselectWidget: 545,
	widthSelectWidget: 30,
	xSpec: 585,
	widthSpec: 30,
	xWidgetControl: 625,
	widthWidgetControl: 30,
	xWidgetRemove: 665,
	widthWidgetRemove: 10,
);

~scoreAndController.channelHeight = ~scoreAndController.posSettings[\channelGap] * 2 + ~scoreAndController.posSettings[\channelHeight];

~scoreAndController.addNamesToScore = { |object, parent|
	object.nameViews = ();
	[\nameView, \typeView, \cntrView] do: { |nameView, i|
		object.nameViews[nameView] = StaticText(parent,
			Rect(
				object.posSettings[[\xName, \xWorkSpace, \xEnlarge][i]],
				object.posSettings[\channelOffset] - object.posSettings[\channelHeight],
				[70, 180, 70][i],
				object.posSettings[\channelHeight])
		).font_(object.font).string = ["NAME", "SCRIPT OR WIDGET?", "CONTROLS"][i];
	};
};

// SCRIPT INTERFACE PSEUDO-CLASS
~scriptWidget = ();

~scriptWidget.loadPreset = { |object, presetDictionary|
	object.setValueFunction(presetDictionary[\script]);
};

~scriptWidget.getPreset = { |object|
	object.model;
};

~scriptWidget.newInstance = { |object|

	object.model = (script: "Specify Pattern script or a value");
	object.setValueFunction = {| object, value|
		object.model[\script] = value;
		object.model.changed(\script, value);
		object.model.postln;
	};
	object.modelDependants = ();

	object.modelDependants[\updaterScript] = {|theChanger, what, script|
		if(what == \script, {
			object.scriptTextField.string_(script);
			if(object.action.notNil) { object.action(script) };
		});

	};

	object.modelDependants[\enlargedInterface] = {|theChanger, what, script|
		if(what == \script, {
				object.enlargedTextField.string_(script);
			});
		};
};

~scriptWidget.makeGui = { |object, parent, bounds|
	object.view = CompositeView(parent,bounds);
	object.view.background = Color.white.alpha_(0.5);
	object.scriptTextField = TextView(object.view,object.view.bounds.extent)
	.background_(Color.white.alpha_(0.5))
	.string_(object.model[\script])
	.keyDownAction_({| ... args|
		var bool = args[2] == 524288;
		bool = args[1].ascii == 13 && bool;
		if (bool) { object.setValueFunction(object.scriptTextField.string) };
	})
	.enterInterpretsSelection_(false)
	.hasVerticalScroller_(false);

	object.model.addDependant(object.modelDependants[\updaterScript]);
};

~scriptWidget.enlargedInterface = { |object|
	if (object.enlargedWindow.isNil) {
		object.enlargedWindow = Window.new("enlarged script")
		.onClose_({
			object.model.removeDependant(object.modelDependants[\enlargedInterface]);
			object.enlargedWindow = nil })
		.front;
		object.enlargedTextField = TextView(object.enlargedWindow ,object.enlargedWindow.bounds.extent)
		.background_(Color.white.alpha_(0.5))
		.string_(object.model[\script])
		.keyDownAction_({| ... args|
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { object.setValueFunction(object.enlargedTextField.string) };
		})
		.enterInterpretsSelection = false;
		object.model.addDependant(object.modelDependants[\enlargedInterface]);
	} { object.enlargedWindow.front };

};


// WIDGET SELECTOR PSEUDO-CLASS
~selectWidget = ();

~selectWidget.loadPreset = { |object, presetDict|
		object.setFaderFunction(presetDict[\fader]);
		object.setRangeFunction(presetDict[\range]);
};

~selectWidget.getPreset = { |object|
	"get widgtet preset".postln;
	(fader: object.modelFader[\widgetValue], range: object.modelRange[\widgetValue]).postln;
};

~selectWidget.newInstance = { |object|

	object.proxies = ();
	[\fader, \rangeLo, \rangeHi] do: { |key| object.proxies[key] = PatternProxy.new; };

	object.modelFader = (widgetValue: 0);
	object.modelRange = (widgetValue: [0,1.0]);

	object.setFaderFunction = {|object, value|
		object.modelFader[\widgetValue] = value;
		if (object.widgetIndex == 1) { object.modelFader.changed(\widgetValue, value); };
	};
	object.setRangeFunction = {|object, value|
		object.modelRange[\widgetValue] = value;
		if (object.widgetIndex == 2) { object.modelRange.changed(\widgetValue, value); };
	};

	object.modelDependants = ();

	object.modelDependants[\lemurFader] = {|theChanger, what, val|
		if (object.lemurFlag) { object.lemur.oscaddr.sendMsg("/" ++ object.pageID ++ object.nameID ++ "/x", val) };
	};

	object.modelDependants[\widgetFader] = {|theChanger, what, val|
		{ object.widget.value = val.postln; }.defer;
	};

	object.modelDependants[\mapFader] = {|theChanger, what, val|
	object.proxies[\fader].source = object.spec.map(val);

	};

	object.modelDependants[\lemurRange] = {|theChanger, what, val|
		if (object.lemurFlag) { object.lemur.oscaddr.sendMsg("/" ++ object.pageID ++ object.nameID ++ "/x", val[0],val[1]) };
	};

	object.modelDependants[\widgetRange] = {|theChanger, what,val|
		{ object.widget.lo_(val[0]).hi_(val[1]) }.defer;
	};

	object.modelDependants[\mapRange] = {|theChanger, what, val|
	object.proxies[\rangeLo].source = object.spec.map(val.first);
	object.proxies[\rangeHi].source = object.spec.map(val.last);
	};

	object.widgetIndex = 0;

	object.modelFader.addDependant(object.modelDependants[\mapFader]);
	object.modelRange.addDependant(object.modelDependants[\mapRange]);
};

~selectWidget.navigateWidgets = { |object| object.selectWidget(object.widgetIndex + 1 % 3) };

~selectWidget.lemurLoad = { |object, lemurClass, pageID = "page 1", nameID = "default", xOffset = 4|
	object.lemur = lemurClass;
	object.xOffset = xOffset;
	object.modelFader.addDependant(object.modelDependants[\lemurFader]);
	object.modelRange.addDependant(object.modelDependants[\lemurRange]);
	object.nameID = nameID;
	object.pageID = pageID;
};

~selectWidget.lemurRemove = { |object, lemurClass|
	object.modelFader.removeDependant(object.modelDependants[\lemurFader]);
	object.modelRange.removeDependant(object.modelDependants[\lemurRange]);
	object.lemur = nil;
	object.freeResp;
};

~selectWidget.freeResp = { |object| if (object.lemurResp.notNil) { object.lemurResp.free; object.lemurResp = nil };  };

~selectWidget.switchView = { |object, newView|

	if (object.widget.notNil) { object.widget.remove; };
	object.widget = nil;
	object.getPrevView.visible = false;
	newView.visible = true;
	object.assignPrevView = newView;

};

~selectWidget.updateResp { |object| object.selectWidget(object.widgetIndex) };



~selectWidget.selectWidget = { |object, index|
		object.widgetIndex = index.postln % 3;
		case
		{ index == 0 } {
			object.switchView(object.returnView);
			object.view.visible = false;
			if (object.lemur.notNil) {
				object.freeResp;
				object.lemur.removeRange(object.pageID, object.pageID ++ object.nameID);
				object.lemur.removeFader(object.pageID, object.pageID ++ object.nameID);
				object.lemur.removeText(object.pageID, object.pageID ++ object.nameID);

			};
		} { index == 1 } {
			object.switchView(object.view);
			if (object.lemur.notNil) {
				object.lemur.removeRange(object.pageID, object.pageID ++ object.nameID);
				object.lemur.removeFader(object.pageID, object.pageID ++ object.nameID);
				object.freeResp;
				object.lemur.fader(object.pageID, object.pageID ++ object.nameID, color: Color.blue, x: object.xOffset);
				object.lemur.removeText(object.pageID, object.pageID ++ object.nameID);
				object.lemur.text(object.pageID, object.pageID ++ object.nameID, object.getName, color: Color.blue, x: object.xOffset, y: 100);

				fork { 0.02.wait; object.lemur.oscaddr.sendMsg("/" ++ object.pageID ++ object.nameID ++ "/x", object.modelFader[\widgetValue]) };
				object.lemurResp = OSCFunc({|msg|object.setFaderFunction(msg[1])}, "/" ++ object.pageID ++ object.nameID ++ "/x");
			};
			object.widget = Slider(object.view,object.view.bounds.extent)
			.value_(object.modelFader[\widgetValue])
			.action_({|val|object.setFaderFunction(val.value);});
		} { index == 2 } {
			object.switchView(object.view);
			if (object.lemur.notNil) {
				object.freeResp;
				object.lemur.removeRange(object.pageID, object.pageID ++ object.nameID);
				object.lemur.removeFader(object.pageID, object.pageID ++ object.nameID);
				object.lemur.range(object.pageID, object.pageID ++ object.nameID, color: Color.blue, x: object.xOffset);
				object.lemur.removeText(object.pageID, object.pageID ++ object.nameID);
				object.lemur.text(object.pageID, object.pageID ++ object.nameID, object.getName, color: Color.blue, x: object.xOffset, y: 100);
				fork { 0.02.wait; object.lemur.oscaddr.sendMsg(*(["/" ++ object.pageID ++ object.nameID ++ "/x"] ++ object.modelRange[\widgetValue])) };
				object.lemurResp = OSCFunc({ |msg| object.setRangeFunction([msg[1],msg[2]])}, "/" ++ object.pageID ++ object.nameID ++ "/x");

			};
			object.widget = RangeSlider(object.view,object.view.bounds.extent)
			.lo_(object.modelRange[\widgetValue][0]).hi_(object.modelRange[\widgetValue][1])
			.action_({|val|object.setRangeFunction([val.lo,val.hi]); })
		};
	};
~selectWidget.makeGui = { |object, parent, bounds|
	object.view = CompositeView(parent,bounds);
	object.view.background = Color.blue.alpha_(0.5);
	object.modelFader.addDependant(object.modelDependants[\widgetFader]);
	object.modelRange.addDependant(object.modelDependants[\widgetRange]);
	object.selectWidget(object.widgetIndex);

};

~selectWidget.closeView = { |object|
	object.modelFader.removeDependant(object.modelDependants[\widgetFader]);
	object.modelRange.removeDependant(object.modelDependants[\widgetRange]);

	if (object.widget.notNil) { object.widget.remove; object.widget = nil; };

};

// SPEC WIDGET PSEUDO CLASS
~specWidget = ();

~specWidget.loadPreset = { |object, presetDictionary|
	object.setValueFunction(presetDictionary[\viewSpec]);
	};

~specWidget.getPreset = { |object| object.model; };

~specWidget.newInstance = { |object|

	object.model = (controlSpec: [0,1.0,\lin].asSpec, viewSpec: [0,1.0,\lin])     ;
	object.setValueFunction = {| object, value|
		var controlSpec = value.asSpec;
		object.model[\controlSpec] = controlSpec;
		object.model.changed(\controlSpec, controlSpec);
		object.model[\viewSpec] = value;
		object.model.changed(\viewSpec, value);
		object.model.postln;
	};

	object.modelDependants = ();

	object.modelDependants[\updaterView] = {|theChanger, what, val|
		if(what == \viewSpec, {
			object.specField.string_(val);
		});

	};

	object.modelDependants[\outputFunc] = {|theChanger, what, val|
		if(what == \controlSpec, {
			if (object.action.notNil) { object.action(val) };
			});
	};

	object.model.addDependant(object.modelDependants[\outputFunc]);
};

~specWidget.makeGui = { |object, parent, bounds|
	object.view = CompositeView(parent, bounds)
	.background = Color.yellow.alpha_(0.9);
	object.specField = TextField(object.view,object.view.bounds.extent)
	.background_(Color.rand.alpha_(0))
	.string_(object.model[\viewSpec].asCode)
	.action_({ |getSpec|
		var returnInterprettedCode = getSpec.value.interpret;
		if (returnInterprettedCode.notNil)
		{ "New ControlSpec asssigned".postln; object.setValueFunction(returnInterprettedCode) }
		{ "Spec is not assigned to the parameter channel because it needs to be debugged!".postln };
	});
	object.model.addDependant(object.modelDependants[\updaterView]);
};

~specWidget.closeView = { |object|
	object.model.removeDependant(object.modelDependants[\updaterView]);
};

// CHANNEL CONTROL PSEUDO-CLASS
~paramWidget = ();

~paramWidget.getState = { |object|
	var preset = ();
	preset[\widgetIndex] = object.widgetIndex;
	preset[\paramName] = object.paramName;
	preset[\specWidget] = object.specWidget.getPreset;
	preset[\selectWidget] = object.selectWidget.getPreset;
	preset[\scriptWidget] = object.scriptWidget.getPreset;
	preset.copy;
};

~paramWidget.loadState = { |object, preset|
	preset = preset;
	object.widgetIndex = preset[\widgetIndex];
	object.paramName = preset[\paramName];
	object.views[\paramNameText].string = preset[\paramName];
	object.specWidget.loadPreset(preset[\specWidget]);
	object.selectWidget.loadPreset(preset[\selectWidget]);
	object.scriptWidget.loadPreset(preset[\scriptWidget]);
	object.nameAction;
};

// ENVIRONMENT INITIALIZATION
~paramWidget.newInstance = { |object, paramName = "default"|
	object.paramName = paramName;

	object.scriptWidget = ~scriptWidget.copy; // Copying pseudo object to create a new instance of the pseudo object
	object.scriptWidget.newInstance; //
	object.selectWidget = ~selectWidget.copy;
	object.selectWidget.newInstance;
	object.selectWidget.getName = { object.paramName.asString.postln; };
	object.specWidget = ~specWidget.copy;
	object.specWidget.newInstance;
	object.selectWidget.spec = [0,1.0,\lin].asSpec;
	object.specWidget.action = { |specObject, spec|
	object.selectWidget.spec = spec.asSpec
	};
	object.defineButtonOutputFuncs;
};

// CHANNEL GUI CODE
~paramWidget.buildControlButtons = {|object, posSettings|
	[\enlargeButton, \selectWidgetButton, \specButton, \controlButton] do: { |buttonName, i|
		object.views[buttonName] = Button(object.views[\channelView],
			Rect(
				posSettings[[\xEnlarge, \xselectWidget, \xSpec, \xWidgetControl][i]],
				posSettings[\channelGap],
				posSettings[[\widthEnlarge, \widthSelectWidget,\widthSpec,\widthWidgetControl][i]],
				posSettings[\channelHeight]))
		.states_([[""] ++ [\red,\blue,\yellow,\black][i].asColor.dup(2)])
		.action_({object.buttonPerformFuncs[buttonName].value});
	};
};

~paramWidget.buidNameRemove = { |object, posSettings|

	object.views[\paramNameText] = TextField(object.views[\channelView],
		Rect(
			posSettings[\xName],
			posSettings[\channelGap],
			posSettings[\widthName],
			posSettings[\channelHeight]));
	object.views[\paramNameText].string = "param";
	object.views[\paramNameText].action_({|paramName|

		object.paramName = interpret(paramName.string);
		object.nameAction;
		object.selectWidget.updateResp;
	});

	object.views[\removeView] =  MButtonV(object.views[\channelView],
		Rect(
			posSettings[\xWidgetRemove],
			posSettings[\channelGap],
			posSettings[\widthWidgetRemove],
			posSettings[\widthWidgetRemove]))
	.action_({ object.buttonPerformFuncs[\removeButton].value });
};

~paramWidget.setName = { |object, name|
	object.views[\paramNameText].string = if (name.isKindOf(String)) { name } { name.asCode };

};

~paramWidget.makeGui = { |object, parent, bounds, posSettings|
	var worksSpaceBounds = Rect(
		posSettings[\xWorkSpace], posSettings[\channelGap],
		posSettings[\widthWorkSpace], posSettings[\channelHeight]);
	object.views = ();
	object.views[\channelView] = CompositeView(parent, bounds);

	object.views[\channelView].background = Color.red.alpha_(0.4);

	object.buildControlButtons(posSettings);
	object.buidNameRemove(posSettings);

	object.scriptWidget.makeGui(object.views[\channelView], worksSpaceBounds);

	object.workSpacePrevView = object.scriptWidget.view.postln;

	object.selectWidget.getPrevView = { object.workSpacePrevView};
	object.selectWidget.assignPrevView = { |object, view| object.workSpacePrevView = view; };
	object.selectWidget.returnView = object.scriptWidget.view;
	object.selectWidget.makeGui(object.views[\channelView], worksSpaceBounds);

	if (object.selectWidget.widgetIndex == 0) { object.selectWidget.view.visible = false; };

	object.specWidget.makeGui(object.views[\channelView], worksSpaceBounds);
	object.specWidget.view.visible = false;

	object.setName(object.paramName)
};

~paramWidget.updateResp = { |object|
	object.selectWidget.updateResp;
};
~paramWidget.moveLayer = { |object,verticalOffset| object.views[\channelView].moveTo(0,verticalOffset) };

~paramWidget.defineButtonOutputFuncs = { |object|
	var countSpec = 0, countControl = 0, controlPrev;
	object.buttonPerformFuncs = ();
	object.buttonPerformFuncs[\enlargeButton] = { object.scriptWidget.enlargedInterface; };

	object.buttonPerformFuncs[\selectWidgetButton] = { object.selectWidget.navigateWidgets;};
	// optimaliseren. 2 x.
	object.buttonPerformFuncs[\specButton] = {
		var newView,key;
		countSpec = countSpec + 1 % 2;
		newView = [object.scriptWidget.view, object.specWidget.view][countSpec];
		object.workSpacePrevView.visible = false;
		newView.class.postln;
		newView.visible = true;
		object.workSpacePrevView = newView;

	};

	object.buttonPerformFuncs[\controlButton] = {
		var newView;
		countControl = countControl + 1 % 1;
		newView = [object.scriptWidget.view][countControl];
		object.workSpacePrevView.visible = false;
		newView.class.postln;
		newView.visible = true;
		object.workSpacePrevView = newView;
		};

	object.buttonPerformFuncs[\removeButton] = {
		object.removeWidgetFunc;
	};

};

~paramWidget.closeView = { |object|
	object.selectWidget.closeView;
	object.scriptWidget.closeView;
	object.specWidget.closeView;
};

~paramWidget.getLayoutPreset = { |object|
	var layoutPreset = ();
	layoutPreset[\scriptContent] = object.scriptWidget.getPreset;
	layoutPreset[\whichWidget] = object.selectWidget.widgetIndex;
	layoutPreset[\keyName] = object.paramName;
	layoutPreset[\controlSpec] = object.specWidget.getPreset;
};









~presetTop = ();

~presetTop.makeInstance = { |object|
	object.storedEvents = ();
	object.refEvent = ();
};

~presetTop.makeGui = { |object, parent, bounds|
	var width = bounds.width, height = bounds.height,
	canvas = CompositeView(parent, bounds);
	canvas.background = Color.rand.alpha_(0);
	object.storeButtons = { |i| Button(canvas, Rect(i * 0.5 * width,0,1 - i - 4 + (width * 0.5),height))
		.states_([[["all: open file","all: save to file"][i], Color.black, Color.red]])
		.action_({ var func = [{object.read}, {object.store}][i]; func.value; })
		.font_(Font("Menlo",12)) } ! 2;
};

~presetTop.store = { |object|  Dialog.savePanel({ arg path; var temp;
	"presets saved to file".postln;
	temp = object.getPresets;
	temp.writeArchive(path.postln)
	},{"cancelled".postln;});
};

~presetTop.read = {  |object|	Dialog.getPaths({ arg paths;
	paths.do({ arg p;
		object.storedEvents = Object.readArchive(p.value.postln);
		r { 	0.1.wait;
			object.loadPresets(object.storedEvents);
			nil}.play(AppClock); })
	},{"cancelled".postln;});
};


~scoreAndMixer = ();

~scoreAndMixer.loadPreset = { "to be specified".postln; };

~scoreAndMixer.getPreset = { "to be specified".postln; };

~scoreAndMixer.reorderMixer = { |object|

	object.scoreArray do: { |score, i|
		score.scoreIndex = i;
		score.mixerView.moveTo(0, i * 50 + object.mixerVerticalOffset);

	};
	object.addView.moveTo(10,object.scoreArray.size * 50 + object.mixerVerticalOffset + 5);
};

// Make a new instance.
~scoreAndMixer.newInstance = { |object|

	object.allScoresPreset = ~presetTop.copy;
	object.allScoresPreset.makeInstance;

	object.allScoresPreset.getPresets = {
		var preset = IdentityDictionary.new;
		object.scoreArray do: { |aScore, i|
			preset[i] = aScore.getState.copy;
		};
		preset.copy;
	};

	object.allScoresPreset.loadPresets = { |other, preset|
		object.scoreArray do: { |score|
			score.mixerView.remove;
			if (score.scoreViewIsOpenFlag == false) { score.view.close };
		};
		object.scoreArray = [];
		preset.keys.asArray.sort do: { |i|
			object.setAddScoreFunction;
			object.scoreArray[i].view.close;
			object.scoreArray[i].loadState(preset[i]);
		};


	};

	object.mixerVerticalOffset = 55;
	object.scoreArray = [];

	object.setAddScoreFunction = { |object|

		var score = ~scoreController.copy;
		score.newInstance(~lm);
		"inMixer".postln;
		score.lemurFlag = object.lemurFlag.postln;
		score.setScoreNameFunction("Score" ++ (object.scoreArray.size + 1));
		score.makeGui(Rect(200,400,700,800), 40, ~scoreAndController);
		score.mixerchannelView(object.view, object.scoreArray.size * 50 + object.mixerVerticalOffset);
		score.scoreIndex = object.scoreArray.size.postln;
		score.removeScoreFunc = { |objectScore|

			objectScore.mixerView.remove;
			if (objectScore.scoreViewIsOpenFlag == false) { objectScore.view.close };
			object.scoreArray.removeAt(objectScore.scoreIndex.postln);
			object.reorderMixer;
		};
		object.scoreArray = object.scoreArray.add(score);

		object.addView.moveTo(10,object.scoreArray.size * 50 + object.mixerVerticalOffset + 5)
	}

};

~scoreAndMixer.makeGui = { |object, parent, bounds|
	object.view = Window.new("Score Mixer", Rect(1000,300,400,500), false, true)
	.background_(Color.new255(* ({ 150 }!3 ++ 230)));

	object.allScoresPreset.makeGui(object.view,Rect(0,0,object.view.bounds.width + 4,20));
	object.balk = CompositeView(object.view, Rect(0,20,object.view.bounds.width,30))
	.background_(Color.black.alpha_(0.4));

	object.addView = CompositeView(object.view,
		Rect(
			10,
			object.scoreArray.size * 50 + object.mixerVerticalOffset + 5,
			40,
			20));
	object.addButton = MButtonP(object.addView,object.addView.bounds.extent)
	.action_({ object.setAddScoreFunction; });

	object.scoreArray do: { |score, i| score.mixerchannelView(object.view, i * 50 + object.mixerVerticalOffset); };

	object.view.front;
};

~scoreAndMixer.closeView = { |object| };

~scoreAndMixer.cleanup = { |object| }; // cleanup responders etc.


// PRESET OBJECT
~presetInterface = ();

~presetInterface.loadPreset = { |object, preset|
	var temp = [];
	preset = preset.copy;
	object.indexValueFunction(preset[\selectIndex]);
	preset[\presets] do: { |i| temp = temp.add(i.first) };

	object.updateNamesFunction(temp);
	object.modelPresets[\presets] = preset[\presets];
	"is something changed".postln;
	object.modelPresets.postln;
};

~presetInterface.getPreset = { |object| object.modelPresets.copy; };

~presetInterface.makeTypePresetNameWindow = { |object, what|
	var tmp,field,text = "Default name";
	tmp = Window.new("Enter Preset",Rect(600,400,200,100),false);
	field = TextField(tmp, Rect(15, 60, 170, 20))
	.string_(text);
	Button(tmp, Rect(15,20,80,30))
	.states_([["store", Color.black, Color.red]])
	.action_({
		text = field.string;
		[what,text].postln;
		case
		{ what == \add }
		{ object.addPresetFunction(text) }
		{ what == \replace }
		{ object.replacePresetFunction(text) };
	        tmp.close;
	});
	Button(tmp, Rect(105, 20, 80,30))
	.states_([["decline", Color.black, Color.red]])
	.action_({  tmp.close;
	});
	tmp.front;
};

~presetInterface.newInstance = { |object|

	object.modelPresets = (presets: [], selectIndex: 0, names: []);

	object.indexValueFunction = {| object, selectIndex|
		object.modelPresets[\selectIndex] = selectIndex;
		object.modelPresets.changed;
		object.modelPresets.postln;
	};

	object.addPresetFunction = {|object, presetName|

		object.modelPresets[\presets] = object.modelPresets[\presets].insert(object.modelPresets[\selectIndex], [presetName, if (object.getPresetFunc.notNil) { object.getPresetFunc.copy }]);
		object.modelPresets.changed(\names);
		object.modelPresets.postln;
	};

	object.replacePresetFunction = { |object,presetName|
		object.modelPresets[\presets] = object.modelPresets[\presets][object.modelPresets[\selectIndex]] = [presetName, if (object.getPresetFunc.notNil) { object.getPresetFunc }];
		object.modelPresets.changed(\names);
		object.modelPresets.postln;
	};

	object.updateNamesFunction = { |object,names|
		object.modelPresets[\names] = names;
		object.modelPresets.changed(\names);
		object.modelPresets.postln;
	};

	object.removePresetFunction = {|object|
		object.modelPresets[\presets] = object.modelPresets[\presets].takeAt(object.modelPresets[\selectIndex]);
		if (object.modelPresets[\selectIndex] >= object.modelPresets[\presets].size) { object.modelPresets[\selectIndex] = object.modelPresets.model[\selectIndex] - 1 };
		object.modelPresets.changed(\names);
		object.modelPresets.postln;
	};

	object.getPresetFunction = { |object|
		var getPreset;
		getPreset = object.modelPresets[\presets][object.modelPresets[\selectIndex]].last;
		object.outputFunc(getPreset);
	};

	object.modelDependants = ();

	object.modelDependants[\updateIndex] = { // always update when an element is changed within the model
			object.nameField.value = object.modelPresets[\selectIndex];
	};

	object.modelDependants[\updateNames] = {|theChanger, what|
		if(what == \names) {
			object.nameField.items = object.modelPresets[\presets] collect: (_.first);
			object.nameField.value = object.modelPresets[\selectIndex];
		};
	};

};

~presetInterface.makeGui = { |object, parent, bounds|
	var width,height; bounds = if(bounds.isKindOf(Point)) { bounds.asRect } { bounds };

	width = bounds.width - 20; height = bounds.height - 8;
	object.view = CompositeView(parent, bounds)
	.background_(Color.new255(0,0,0,0));
	object.view.decorator = FlowLayout(object.view.bounds);

	object.nameField = PopUpMenu(object.view, 37/160 * width @ height)
	.background_(Color.black.alpha_(0.8))
	.stringColor_(Color.red)
	.items_(object.modelPresets[\names].postln)
	.action_({|index| object.indexValueFunction(index.value)});

	object.controlViews = ["+", "-", "recall", "store", "replace","delete"] collect: { |name, i|
		Button(object.view, if(i < 2, { 10 / 160 * width }, { 25 / 160 * width }) @ height)
		.states_([[name,Color.red, if(i < 2, { Color.new255(189, 183, 107) }, { Color.black.alpha_(0.8) })]])
		.action_(
			[
				{ 	var newIndex = object.modelPresets[\selectIndex] - 1;
					newIndex = newIndex % object.modelPresets[\presets].size;
					object.indexValueFunction(newIndex ); },
				{ 	var newIndex = object.modelPresets[\selectIndex] + 1;
					newIndex = newIndex % object.modelPresets[\presets].size;
					object.indexValueFunction(newIndex ); },
				{       object.getPresetFunction; },
				{       object.makeTypePresetNameWindow(\add) },
				{       object.makeTypePresetNameWindow(\replace) },
				{       object.removePresetFunction } ][i]);
	};

	object.modelPresets.addDependant(object.modelDependants[\updateIndex]);
	object.modelPresets.addDependant(object.modelDependants[\updateNames]);
};

// Close View function is a function to be assigned to .onClose method
// Removes dependant that perform GUI specific action.
~presetInterface.closeView = { |object|
	object.model.removeDependant(object.modelDependants[\updateIndex]);
	object.model.removeDependant(object.modelDependants[\updateNames]);

};

~presetInterface.cleanup = { |object| }; // cleanup responders etc.


// SCORE & CONTROLLER INTERFACE

~scoreController = ();

~scoreController.loadPreset = { "to be specified".postln; };

~scoreController.getPreset = { "to be specified".postln; };

~scoreController.getState = { |object|
	var preset = ();

	preset[\channelStates] = IdentityDictionary.new;
	object.channelArray do: { |channel, i|
		preset[\channelStates][i] = channel.getState;
	};

	preset[\textField] = object.model;
	preset[\levelScore] = object.modelLevel;
	preset[\scoreName] = object.scoreNameModel;
	preset[\presets] = object.presetInterface.getPreset;
	preset.copy;
};

~scoreController.loadState = { |object, preset|
	object.channelArray do: { |channel| channel.views[\channelView].remove };
	object.channelArray = [];

	preset[\channelStates].size do: { object.setAddChannelFunction };

	preset[\channelStates] do: { |channelPreset, i | object.channelArray[i].loadState(channelPreset) };

	object.setEnvirFieldFunction(preset[\textField][\textField]);

	object.setScoreNameFunction(preset[\scoreName][\scoreName].asString.postln);
	r { 2 do: { 0.1.wait; object.presetInterface.loadPreset(preset[\presets].postln); }}.play(AppClock)
};

~scoreController.reorderChannels = { |object, chDistance, posSettings|

	object.channelArray do: { |channel, i|
		channel.widgetIndex = i;
		channel.views[\channelView].moveTo(0, i * chDistance + posSettings.channelOffset);
		channel.nameAction; // reassign proxies to Pbind.
	};
	object.addView.moveTo(
		posSettings.xWorkSpace,
		object.channelArray.size * chDistance + (chDistance * 0.3) + posSettings.channelOffset
	);
};

~scoreController.randomize = { |object|
	object.channelArray do: { |channel| channel.selectWidget.loadPreset((fader: 1.0.rand, range: [1.0.rand,1.0.rand].sort)) };

};

// Make a new instance.
~scoreController.newInstance = { |object, lemur|

	object.scoreViewIsOpenFlag = true;
	object.lemur = lemur;
	object.presetInterface = ~presetInterface.copy;
	object.presetInterface.newInstance;
	object.presetInterface.getPresetFunc = {
		var preset = ();
		object.channelArray do: { |channel|
			preset[channel.paramName] = channel.selectWidget.getPreset.copy;
		};
		preset;
	};
	object.presetInterface.outputFunc = { |something, preset|
		var names = [];

		object.channelArray do: { |channel| names = names.add(channel.paramName); };

		preset.keys do: { |key|
			var getIndex = names.indexOf(key);
			if (getIndex.notNil) { object.channelArray[getIndex].selectWidget.loadPreset(preset[key]) };
		}
	};

	object.ampProxy = PatternProxy.new;
	object.ampProxy.source = 1;

	object.sndStreamProxy = PatternProxy.new;
	object.sndStreamProxy.source = Pbind(\dur, 0.1);

	object.sndStream = Pmul(\amp, object.ampProxy, object.sndStreamProxy);

	object.modelDependants = ();

	// ENVIR TEXT FIELD MODEL AND CONTROLLERS
	object.model = (textField: "");
	object.setEnvirFieldFunction = {| object, code|
		object.model[\textField] = code;
		object.model.changed(\textField, code);
		object.model.postln;
	};

	object.modelLevel = (level: 0);
	object.setLevelSliderFunction = {| object, lvl|
		object.modelLevel[\level] = lvl;
		object.levelSlider = lvl;
		object.ampProxy.source = lvl.dbamp
	};

	object.modelDependants[\updateEnvirTextField] = {|theChanger, what, code|
		object.environmentField.string = code;
	};
	object.modelDependants[\theInterpretter] = {|theChanger, what, code|
		code = "Environment.make({" ++ code ++ "})";
		code.postln;
		code = interpret(code);
		if (code.isNil) {
			"Debug envirField".postln;
		} {
			"make function!".postln; code.postln;
		};
	};

	// SCORE NAME MODEL AND CONTROLLER
	object.scoreNameModel = (scoreName: nil);



	object.setScoreNameFunction = {| object, name|
		if (object.scoreNameModel[\scoreName].isNil) { object.scoreNameModel[\scoreName] = name };
		object.lemurFlag.postln;
		if (object.lemurFlag) { object.lemur.renamePage(object.scoreNameModel[\scoreName], name) };
		object.scoreNameModel[\scoreName] = name;
		object.scoreNameModel.changed(\scoreName, name);
		object.scoreNameModel.postln;

		object.channelArray do: { |channel|
			if (object.lemurFlag) {

				channel.pageID = name;
				channel.updateResp;
			};
		};
	};

	object.modelDependants[\scoreNameModelDependant] = { |theChanger, what, name|
		object.nameView.string = name;
	};

	object.modelDependants[\scoreMixerNameModelDepedant] = { |theChanger, what, name|
		"is this empty".postln;
		object.levelSlider.labelView.postln;

		object.levelSlider.labelView.string = name.asString;
	};

	// PLAY SCORE
	object.playPatternModel = (state: 0);
	object.playPatternFunction = {| object, state|
		object.playPatternModel[\state] = state;
		object.playButton1.value = state;
		object.playButton2.value = state;
		if (state > 0) { object.tempPlay = object.sndStream.play } { object.tempPlay.stop };
	};

	object.modelDependants[\scoreMixerNameModelDepedant] = { |theChanger, what, name|
		"is this empty".postln;
		object.levelSlider.labelView.postln;

		object.levelSlider.labelView.string = name.asString;
	};

	object.channelArray = [];

	object.keyAndPatternPairs = IdentityDictionary.new;

	object.setAddChannelFunction = { |object|
		var currentIndex = object.channelArray.size;
		var paramWidget = ~paramWidget.copy;
		var parName = "\Param" ++ currentIndex;
		var scoreSettings = ~scoreAndController;

		paramWidget.newInstance(parName);
		paramWidget.widgetIndex = currentIndex;
		paramWidget.makeGui(
			object.view,
			Rect(
				0,
				currentIndex * object.chDistance + scoreSettings.posSettings.channelOffset,
				object.view.bounds.width,
				object.chDistance),
			scoreSettings.posSettings);
		paramWidget.selectWidget.lemurLoad(lemur, object.scoreNameModel[\scoreName].postln, parName.postln, currentIndex * 100 + 4);

		paramWidget.nameAction = {
			if (paramWidget.proxy.isNil) { paramWidget.proxy = PatternProxy.new };
			object.channelArray.postln;
			object.channelArray do: { |chKeyProxy|
				object.keyAndPatternPairs[chKeyProxy.paramName] = chKeyProxy.proxy;
			};

			object.sndStreamProxy.source = Pbind(*object.keyAndPatternPairs.getPairs);
		};

		paramWidget.scriptWidget.action = { |scriptObject, code|
			code = "{ |fader, rangeLo, rangeHi, env| " ++ code ++ "}";
			code = interpret(code);
			if (paramWidget.proxy.isNil) { paramWidget.proxy = PatternProxy.new };
			paramWidget.proxy.source = code.value(
				paramWidget.selectWidget.proxies[\fader],
				paramWidget.selectWidget.proxies[\rangeLo],
				paramWidget.selectWidget.proxies[\rangeHi]);
		};

		paramWidget.removeWidgetFunc = { |channelObject|
			channelObject.views[\channelView].remove;
			object.channelArray.removeAt(channelObject.widgetIndex);
			object.reorderChannels(object.chDistance, scoreSettings.posSettings);
		};

		paramWidget.selectWidget.lemurFlag = object.lemurFlag;

		object.channelArray = object.channelArray.add(paramWidget);
		object.addView.moveTo(
			scoreSettings.posSettings.xWorkSpace,
			object.channelArray.size * object.chDistance + (object.chDistance * 0.3) + scoreSettings.posSettings.channelOffset
		);
		paramWidget;
	};

	object.model.addDependant(object.modelDependants[\theInterpretter]);

};

// Make View
~scoreController.makeGui = { |object, bounds, chDistance = 40, scoreSettings|

	object.scoreViewIsOpenFlag = false;
	object.chDistance = chDistance;
	object.view = Window.new("", bounds)
	.background = Color.new255(* ({ 150 }!3 ++ 230));

	object.nameView = TextField(object.view, Rect(10,10,110,30))
	.string_(object.scoreNameModel[\scoreName])
	.action_({|nv| object.setScoreNameFunction(nv.string)});

	object.scoreNameModel.addDependant(object.modelDependants[\scoreNameModelDependant]);

	object.playButton1 = Button(object.view, Rect(object.view.bounds.width - 60,10,50,30))
	.font_(scoreSettings.font)
	.states_([["PLAY", Color.red, Color.black],["STOP", Color.black, Color.red]])
	.action_({ |b| object.playPatternFunction(b.value) });

	object.randomButton = Button(object.view, Rect(220,10,80,30))
	.font_(scoreSettings.font)
	.states_([["RANDOMIZE", Color.red, Color.black]])
	.action_({ object.randomize });

	object.presetInterface.makeGui(object.view, Rect(6, 45, object.view.bounds.width - 14, 30));

	object.environmentField = TextView(object.view, Rect(10, 80, object.view.bounds.width - 20, 130))
	.background_(Color.white.alpha_(0.5))
	.string_(object.model[\textField].postln)
	.keyDownAction_({| ... args|
		var bool = args[2] == 131072;
		bool = postln(args[1].ascii == 13) && bool.postln;
		if (bool) { object.setEnvirFieldFunction(object.environmentField.string) };
	});

	object.model.addDependant(object.modelDependants[\updateEnvirTextField]);

	scoreSettings.addNamesToScore(object.view);

	object.channelArray do: { |chan, i|
		chan.makeGui(
			object.view,
			Rect(
				0,
				i * chDistance + scoreSettings.posSettings.channelOffset,
				object.view.bounds.width,
				chDistance),
			scoreSettings.posSettings);
	};

	object.addView = CompositeView(object.view,
		Rect(
			scoreSettings.posSettings.xWorkSpace,
			object.channelArray.size * chDistance + (chDistance * 0.3) + scoreSettings.posSettings.channelOffset,
			40,
			20));
	object.addButton = MButtonP(object.addView,object.addView.bounds.extent)
	.action_({ object.setAddChannelFunction });
	object.view.front;
	object.view.onClose = object.closeView;
};

~scoreController.mixerchannelView = { |object, parent, verticalOffset = 200|
	object.mixerView = CompositeView(parent, Rect(0,verticalOffset, parent.bounds.width,50))
	.background_(Color.black.alpha_(0.2));

	object.font = Font("Menlo", 14);

	object.playButton2 = Button(object.mixerView,Rect(0,0,40,40))
	.font_(object.font)
	.states_([
		["PLAY", Color.red.alpha_(0.8), Color.black],
		["STOP", Color.black,Color.red.alpha_(0.8)]])
	.action_({ |b| object.playPatternFunction(b.value) });

	object.levelSlider = EZSlider(object.mixerView, Rect(50,0,300,40), "", \db.asSpec.step_(0.01),unitWidth:30, numberWidth:60,layout: \line2, margin: nil)
	.setColors(Color.black.alpha_(0),Color.black, Color.black.alpha_(0),Color.black.alpha_(0), Color.red,Color.black.alpha_(1),nil,nil, Color.black.alpha_(0))
	.action_({ |v| object.setLevelSliderFunction(v.value) });

	object.levelSlider.labelView.string = object.scoreNameModel[\scoreName];
	object.levelSlider.value = object.modelLevel[\level];
	object.openScore = Button(object.mixerView,Rect(350,0,50,40))
	.font_(object.font)
	.states_([["SCORE", Color.red.alpha_(0.8), Color.black]])
	.action_({
		if (object.scoreViewIsOpenFlag.postln) {
			object.makeGui(Rect(200,400,700,800), 40, ~scoreAndController);
			} { object.view.front }; });

	object.scoreNameModel.addDependant(object.modelDependants[\scoreMixerNameModelDepedant]);

	object.removeScoreButton = MButtonV(object.mixerView, Rect(350,42,6,6))
	.action_({ |v| object.removeScoreFunc; });
};

~scoreController.mixerchannelCloseView = { |object|
	object.scoreNameModel.removeDependant(object.scoreMixerNameModelDepedant);
};

~scoreController.closeView = { |object|
	{
	object.model.removeDependant(object.modelDependants[\updateEnvirTextField]);
	object.scoreNameModel.removeDependant(object.scoreNameModelDependant);
	object.scoreViewIsOpenFlag = true;
	}
};



// (
// ~scoreAndMixer.scoreArray do: { |score|
// 	score.channelArray do: { |channel|
// 		channel.selectWidget.lemurFlag = true;
// 	}
// }
// )



~removeWhiteSpace = { |string|
	var temp = "";
	string do: { |char|
		if (postln(char.ascii != " ".ascii.first))
		{ temp = temp ++ char }
	};
	temp;
}

