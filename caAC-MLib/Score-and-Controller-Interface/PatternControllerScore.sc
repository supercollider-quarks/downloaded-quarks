/*
2013
Marinus Klaassen
rotterdamruis.nl
*/


PatternControllerScore : ScoreWidget {
	var isOpen, <>lemur, <scorePresetMenu, <controllers, <scoreName, <playingStream, <keyAndPatternPairs;
	var mixerAmpProxy, eventStreamProxy, <eventStream, controllerProxies, eventParProxy;
	var <parent, scoreGui, mixerGui, <mixerCanvas, >removeAction;
	var <>index, >closeAction;

	*new { |argLemur, argIndex|
		^super.newCopyArgs.init(argLemur, argIndex);
	}

	initProxies {
		mixerAmpProxy = PatternProxy.new;
		mixerAmpProxy.source = 1;
		eventStreamProxy = PatternProxy.new;
		eventStreamProxy.source = Pbind(\dur, 1);
		eventParProxy = PatternProxy.new;
		eventParProxy.source = Ppar([eventStreamProxy]);
		eventStream = Pmul(\amp, mixerAmpProxy, eventParProxy);

	}

	init { |argLemur, argIndex|

		isOpen = false;
		lemur = argLemur;

		index = argIndex;

		controllerProxies = IdentityDictionary.new;

		scorePresetMenu = ScorePresetMenu.new;
		scorePresetMenu.storeAction = {
			var preset = Dictionary.new;
			controllers do: { |channel|
				preset[channel.name.asSymbol] = channel.paramController.getState;
			};
			preset.copy;
		};

		scorePresetMenu.action = { |aPreset|
			var nameArray = [];
			controllers do: { |channel| nameArray = nameArray.add(channel.name.asSymbol); };
			aPreset.keys do: { |key|
				var nameIndex = nameArray.indexOf(key);
				if (nameIndex.notNil) {
					controllers[nameIndex].paramController.loadState(aPreset[key])
				};
			};
		};

		this.initProxies;

		scoreName = "Score" ++ argIndex;

		model = (
			scoreName: scoreName,
			envirTextField: "",
			environment: Environment[],
			mixerAmpFader: 1,
			playButton: 0
		);
		dependants = ();
		setValueFunction = ();
		[\envirTextField, \mixerAmpFader, \scoreName, \playButton] do: { |key|
			setValueFunction[key] = { |inArg|
				model[key] = inArg;
				model.changed(key, inArg);
			};
		};

		dependants[\interpresetEnvirTextField] = {|theChanger, what, environment|
			if (what == \envirTextField) {
				environment =  "Environment.make({" ++ environment ++ "})".postln;
				environment = interpret(environment);
				if (environment.notNil) {
					///////////////////////////////////////// TEXTFIELD ENVIRONMENT DEPEDANT FUNCTION
					model[\environment] = environment.postln;

					controllers do: { |aCon|
						aCon.paramProxy.source = aCon.scriptFunc.value(
						aCon.controllerProxies['fader'],
						aCon.controllerProxies['rangeLo'],
						aCon.controllerProxies['rangeHi'],
						model[\environment]
						)
					};
				} {
					"Debug envirField!!".postln;
				};
			};
		};
		model.addDependant(dependants[\interpresetEnvirTextField]);
		dependants[\scoreName] = {|theChanger, what, argScoreName|
			if (what == \scoreName) {
				controllers do: { |i|
					i.paramController.pagename = argScoreName;

				};
				lemur.removePage(scoreName);
				scoreName = argScoreName;
			}
		};
		model.addDependant(dependants[\scoreName]);
		dependants[\playButton] =  {|theChanger, what, value|
			if (what == \playButton) {
				if (value > 0) {
					playingStream = eventStream.play } { playingStream.stop };
			};
		};
		model.addDependant(dependants[\playButton]);

		dependants[\mixerAmpFader] =  {|theChanger, what, value|
			if (what == \mixerAmpFader) {
				mixerAmpProxy.source = value;
			};
		};
		model.addDependant(dependants[\mixerAmpFader]);

		controllers = Array.new;

		keyAndPatternPairs = IdentityDictionary.new;

	}

	makeScoreGui {

		isOpen = true;
		parent = Window.new("", Rect(200,400,700,800))
		.background_(Color.new255(* ({ 150 }!3 ++ 230)));

		scoreGui = ();
		scoreGui[\scoreNameField] = TextField(parent, Rect(10,10,110,30))
		.string_(model[\scoreName])
		.action_({|v| setValueFunction[\scoreName].value(v.string)});
		dependants[\scoreNameScoreGui] = {|theChanger, what, argScoreName|
			if (what == \scoreName) {
				scoreGui[\scoreNameField].string = argScoreName;
			}
		};
		model.addDependant(dependants[\scoreNameScoreGui]);

		scoreGui[\playButton] = Button(parent, Rect(parent.bounds.width - 62,10,50,30))
		.value_(model[\playButton])
		.font_(MUI.settings[\font])
		.states_([["PLAY", Color.red, Color.black],["STOP", Color.black, Color.red]])
		.action_({|b| setValueFunction[\playButton].value(b.value)});
		dependants[\playButtonScoreGui] =  {|theChanger, what, value|
			if (what == \playButton) {
				scoreGui[\playButton].value = value;
			};
		};
		model.addDependant(dependants[\playButtonScoreGui]);

		scoreGui[\randomizeButton] = Button(parent, Rect(220,10,80,30))
		.font_(MUI.settings[\font])
		.states_([["RANDOMIZE", Color.red, Color.black]])
		.action_({ this.randomize; });


		scorePresetMenu.makeGui(parent, Rect(10, 44, parent.bounds.width - 12, 30));

		scoreGui[\envirTextField] = TextView(parent, Rect(10, 80, parent.bounds.width - 20, 130))
		.background_(Color.white.alpha_(0.5))
		.string_(model[\envirTextField])
		.keyDownAction_({| ... args|
			var bool = args[2] == 524288;
			bool = args[1].ascii == 13 && bool;
			if (bool) { setValueFunction[\envirTextField].value(scoreGui[\envirTextField].string) };
		});

		dependants[\envirTextFieldScoreGui] = {|theChanger, what, script|
			if (what == \envirTextField) {
				scoreGui[\envirTextField].string = script;
			};
		};
		model.addDependant(dependants[\envirTextFieldScoreGui]);

		[\nameView, \typeView, \cntrView] do: { |key, i|
			scoreGui[key] = StaticText(parent,
				Rect(
					MUI.settings[[\xName, \xLayers, \xButtons][i]],
					MUI.settings[\chOffset] - MUI.settings[\chHeight],
					[70, 180, 70][i],
					MUI.settings[\chHeight])
			)
			.font_(MUI.settings[\font])
			.string = ["NAME", "SCRIPT OR WIDGET?", "CONTROLS"][i];
		};

		controllers do: { |con, i|
			con.makeGui(
				parent,
				Rect(
					0,
					i * 42 + MUI.settings[\chOffset],
					parent.bounds.width,
					40));
		};

		scoreGui[\addChannelView] = CompositeView(parent, Rect(
			MUI.settings[\xLayers],
			controllers.size * 40 + MUI.settings[\chOffset],
			40,
			30)).background_(Color.clear);

		scoreGui[\addChannelButton] = MButtonP(scoreGui[\addChannelView],scoreGui[\addChannelView].bounds.extent)
		.action_({ this.addChannel });


		scoreGui[\LayerView] = EZNumber(parent,
				Rect(0, parent.bounds.height - 30, 88, 20),
				"LAYERS", ControlSpec(1,20, \lin, 1),
				{ |ez| this.layerAction(ez.value.postln) },
			1, false, 60);
		scoreGui[\LayerView].setColors(Color.grey,Color.white);
		scoreGui[\LayerView].labelView.align_(\center);

		parent.onClose = { this.closeScoreGui; };
		parent.front;

	}

	makeScoreMixerChannelGui { |parent, yOffset = 200, height = 50|
		var font = Font("Menlo", 14);
		mixerCanvas = CompositeView(parent, Rect(0, yOffset, parent.bounds.width,height))
		.background_(Color.black.alpha_(0.2));

		mixerGui = ();
		mixerGui[\mixerScorePlay] = Button(mixerCanvas,Rect(0,0,40,40))
		.font_(font)
		.states_([
			["PLAY", Color.red.alpha_(0.8), Color.black],
			["STOP", Color.black,Color.red.alpha_(0.8)]])
		.action_({|b| setValueFunction[\playButton].value(b.value)});
		dependants[\mixerScorePlay] =  {|theChanger, what, value|
			if (what == \playButton) {
				mixerGui[\mixerScorePlay].value = value;
			};
		};
		model.addDependant(dependants[\mixerScorePlay]);

		mixerGui[\mixerAmpFader] = EZSlider(
			mixerCanvas,
			Rect(50,0,300,40),
			"",
			\db.asSpec.step_(0.01),
			unitWidth:30,
			numberWidth:60,
			layout: \line2,
			margin: nil)
		.setColors(Color.black.alpha_(0),Color.black, Color.black.alpha_(0),Color.black.alpha_(0), Color.red,Color.black.alpha_(1),nil,nil, Color.black.alpha_(0))
		.value_(model[\mixerAmpFader])
		.action_({ |v| setValueFunction[\mixerAmpFader].value(v.value.dbamp.postln) });

		mixerGui[\mixerAmpFader].labelView.string_(model[\scoreName]);

		dependants[\mixerAmpFaderGui] =  {|theChanger, what, value|
			if (what == \mixerAmpFader) {
				mixerGui[\mixerAmpFader].value = value.ampdb;
			};
		};
		model.addDependant(dependants[\mixerAmpFaderGui]);

		dependants[\mixerLabelView] =  {|theChanger, what, value|
			if (what == \scoreName) {
				mixerGui[\mixerAmpFader].labelView.string_(value);
			};
		};
		model.addDependant(dependants[\mixerLabelView]);

		mixerGui[\popupScore] = Button(mixerCanvas,Rect(350,0,50,40))
		.font_(font)
		.states_([["SCORE", Color.red.alpha_(0.8), Color.black]])
		.action_({ if (isOpen) {
			parent.front
			} {
				this.makeScoreGui;
		}});
		mixerGui[\removeScore] = MButtonV(mixerCanvas, Rect(350,42,6,6))
		.action_({ this.close; });
	}

	addChannel {
		var currentIndex = controllers.size;
		var paramName = "Param" ++ currentIndex;
		var paramChannel = ParamChannel.new(
			argName: paramName,
			argIndex: currentIndex,
			argLemur: lemur,
			argLemurXoffset: currentIndex * 110,
			argPageName: model[\scoreName],
			argObjectReferenceName: "Object" ++ currentIndex
		);

		paramChannel.controllerProxies = (
			fader: PatternProxy.new(0),
			rangeLo: PatternProxy.new(0),
			rangeHi: PatternProxy.new(1)
		);

		paramChannel.paramController.rangeAction = { | val |
			paramChannel.controllerProxies[\rangeLo].source = val[0].postln;
			paramChannel.controllerProxies[\rangeHi].source = val[1].postln;
		};

		paramChannel.paramProxy = PatternProxy.new(1);

		paramChannel.paramController.faderAction = { | val |
			paramChannel.controllerProxies[\fader].source = val.postln
		};

		paramChannel.nameAction = { |argName|
			argName.postln;
			keyAndPatternPairs = Dictionary.new;
			if (paramChannel.paramProxy.isNil) { paramChannel.paramProxy = PatternProxy.new(1) };
			controllers do: { |aChannel|
				keyAndPatternPairs[aChannel.name.asSymbol] = aChannel.paramProxy;
			};
			eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs.postln);
		};

		paramChannel.pScript.action = { |code|
			var func = interpret("{ |fader, rangeLo, rangeHi, env| " ++ code ++ "}");
			paramChannel.scriptFunc = func;

			paramChannel.paramProxy.source = func.value(
				paramChannel.controllerProxies['fader'],
				paramChannel.controllerProxies['rangeLo'],
				paramChannel.controllerProxies['rangeHi'],
				model[\environment]
			);
		};

		paramChannel.removeAction = { |index|
			var tempChannel = controllers.removeAt(index);
			tempChannel.closeGui;
			tempChannel.closeLemur;
			this.positionChannels;
			keyAndPatternPairs = IdentityDictionary.new;
			controllers do: { |i|
				keyAndPatternPairs[i.name.asSymbol] = i.paramProxy;
			};
			if (keyAndPatternPairs.size >= 2) {
				eventStreamProxy.source = Pbind(*keyAndPatternPairs.getPairs);
			} {
				eventStreamProxy.source = Pbind();
			}
		};

		controllers = controllers.add(paramChannel);

		if (isOpen != false) { paramChannel.makeGui(parent,
			Rect(
				0,
				currentIndex * 42 + MUI.settings[\chOffset],
				parent.bounds.width,
				40));
			scoreGui[\addChannelView].moveTo(
				MUI.settings[\xLayers],
				controllers.size * 42 + MUI.settings[\chOffset],
				40,
				30);
		};

		paramChannel;

	}

	randomize {
		controllers do: { |i|
			i.paramController.loadState((fader: 1.0.rand, range: sort({1.0.rand}!2)))
		}
	}

	positionChannels {
		controllers do: { |instance, index|
			instance.index = index;
			instance.moveBounds(0,index * 42 + MUI.settings[\chOffset]);
			instance.moveLemur(index * 110);

		};
		scoreGui[\addChannelView].moveTo(
			MUI.settings[\xLayers],
			controllers.size * 42 + MUI.settings[\chOffset],
			40,
			30);
	}

	layerAction { |argLayers|
		eventParProxy.source = Ppar({eventStreamProxy}!argLayers);
		argLayers.postln;
	}

	close {
		if (closeAction.notNil) { closeAction.value(index) };
		this.closeGui;
		model.dependants do: { |i| model.removeDependant(i) };
	}

	closeGui {
		this.closeMixerChannelGui;
		this.closeScoreGui;
	}

	closeMixerChannelGui {
		mixerCanvas.remove;
		mixerGui do: (_.remove);
		model.removeDependant(dependants[\mixerScorePlay]);
		model.removeDependant(dependants[\mixerAmpFaderGui]);
		model.removeDependant(dependants[\mixerLabelView]);
	}

	closeScoreGui {
		parent.close;
		scoreGui do: (_.remove);
		isOpen = false;
		model.removeDependant(dependants[\scoreNameScoreGui]);
		model.removeDependant(dependants[\envirTextFieldScoreGui]);
		model.removeDependant(dependants[\playButtonScoreGui]);
	}

	getState {
		var scoreState = Dictionary.new;
		scoreState[\scoreName] = model[\scoreName].copy;
		scoreState[\presetMenu] = scorePresetMenu.getState.postln;
		scoreState[\envirTextField] = model[\envirTextField].copy;
		scoreState[\mixerAmpFader] = model[\mixerAmpFader].copy;

		scoreState[\controllers] = Dictionary.new;
		controllers do: { |conCh, i|
			scoreState[\controllers][i.asSymbol] = conCh.getState;
		};
		^scoreState;
	}

	loadState { |argPreset|
		setValueFunction[\envirTextField].value(argPreset[\envirTextField]);
		setValueFunction[\scoreName].value(argPreset[\scoreName]);
		setValueFunction[\mixerAmpFader].value(argPreset[\mixerAmpFader]);
		scorePresetMenu.loadState(argPreset[\presetMenu].postln);
		argPreset[\controllers].size do: { |i|
			if (controllers[i].isNil) { this.addChannel; };
		};
		controllers do: { |aScore,i |
			aScore.loadState(argPreset[\controllers][i.asSymbol])
		};
	}
}

