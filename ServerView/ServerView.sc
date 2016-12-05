ServerView : Singleton {
	classvar panelTypes, <>widgets, <actions, <>border=true,
	showOnBoot=true, serverViewShown=false;
	var <>view, <window, widgetLayout, <server, <>widgets, containers;

	*initClass {
		panelTypes = IdentityDictionary();
		widgets = List[ServerStatusWidget, ScopeWidget, HistoryWidget, VolumeWidget, RecordWidget];
		actions = IdentityDictionary();

		ServerBoot.add(this);
	}

	*doOnServerBoot {
		|server|
		if (showOnBoot && serverViewShown.not) {
			serverViewShown = true;
			server.makeGui;
		}
	}

	*default {
		^Server.default.name;
	}

	set {}

	close {
		{ window.close() }.defer;
	}

	front {
		if (server.isNil) {
			server = Server.default;
		};
		if (view.isNil) {
			this.createView();
		};
		view.front;
	}

	createView {
		window = Window(bounds:Rect(0, 0, 265, 200), resizable:true, border:border);
		window.autoRememberPosition(\ServerView);
		view = window.view.minWidth_(265).minHeight_(200);
		view.keyDownAction_({
			|v, char|
			actions[char.asSymbol].value(this);
		});
		if (border.not) {
			view.layout_(HLayout(
				WindowHandleView().maxWidth_(10).minWidth_(10),
				widgetLayout = VLayout()
			).spacing_(0).margins_(2));
		}{
			view.layout_(HLayout(
				widgetLayout = VLayout()
			).spacing_(0).margins_(2));
		};

		widgetLayout.margins = 3;
		widgetLayout.spacing = 1;
		view.onClose_(this.onClose(_));
		this.populateView();
	}

	populateView {
		containers = List();
		widgets = List();

		([ServerSelectorWidget] ++ this.class.widgets).do {
			|wClass, i|
			var container, widget;
			widget = wClass.new(server, this).view();
			widgets.add(widget);
			container = View().layout_(
				VLayout(
					widget,
					if (i != this.class.widgets.size()) {
						UserView().fixedHeight_(10).drawFunc_({
							|v|
							var y = (v.bounds.height / 2).round + 0.5;
							Pen.strokeColor = Color.grey(0.5, 0.2);
							Pen.width = 1;
							Pen.line(2@y, (v.bounds.width - 2)@y);
							Pen.stroke();
						})
					}
				).spacing_(1).margins_(0)
			).canFocus_(true);
			containers.add(container);
		};

		containers.do({
			|w|
			widgetLayout.add(w)
		});

		widgetLayout.add(nil);
	}

	onClose {
		|v|
		window = widgetLayout = view = nil;
		actions.clear;
	}

	clearView {
		if (view.notNil) {
			containers.do(_.remove);
		}
	}

	registerKeyAction {
		| key, action |
		if (actions[key].notNil) {
			"ServerView - Overriding an existing action for key %".format(key).warn;
		};
		actions[key] = action;
	}

	server_{
		|newServer|
		if (server != newServer) {
			{
				server = newServer;
				if (view.notNil) {
					this.clearView();
					this.populateView();
				}
			}.defer();
		}
	}
}

ServerWidgetBase {
	var server, parent, buttonColor, faintGreen, faintRed, faintYellow, faintBlue,
	brightBlue, brightGreen, brightRed, highlightColor, fontface;

	*new {
		|server, parent|
		^super.newCopyArgs(server, parent).init();
	}

	*brightenColor {
		|inColor, amt|
		var color = Color.fromArray(inColor.asArray);
		color.red = (color.red + amt).linlin(0,1,0,1);
		color.green = (color.green + amt).linlin(0,1,0,1);
		color.blue = (color.blue + amt).linlin(0,1,0,1);
		^color
	}

	init {
		var mod = if (QtGUI.palette.window.asHSV[2] > 0.5) {-0.2} {0.2};
		buttonColor = QtGUI.palette.button;
		faintGreen = ServerWidgetBase.brightenColor(buttonColor.blend(Color.green, 0.2), mod);
		faintRed = ServerWidgetBase.brightenColor(buttonColor.blend(Color.red, 0.25), mod);
		faintYellow = ServerWidgetBase.brightenColor(buttonColor.blend(Color.yellow, 0.25), mod);
		faintBlue = ServerWidgetBase.brightenColor(buttonColor.blend(Color.blue, 0.25), mod);

		brightBlue = Color.hsv(0.555, 1, 0.6 + mod);
		brightGreen = Color.hsv(0.277, 1, 0.6 + mod);
		brightRed = Color.hsv(0.01, 1, 0.6 + mod);

		highlightColor = Color.grey(0.7 + mod);

		this.actions.do({
			|action|
			parent.registerKeyAction(action.key, action.action);
		});
	}

	font {
		| size=12, bold=false |
		if (fontface.isNil) {
			var fonts = Font.availableFonts;
			if (fonts.includesEqual("M+ 2c")) {
				fontface = "M+ 2c";
			} {
				if (fonts.includesEqual("Helvetica Neue")) {
					fontface = "Helvetica Neue"
				} {
					fontface = Font.defaultSansFace
				}
			};
		}
		^Font(fontface, size, bold);
	}

	actions { ^[] }

	view { this.subclassResponsibility('view') }
}

ServerViewAction {
	var <name, <key, <action;
	*new { |name, key, action| ^super.newCopyArgs(name, key, action) }
	value { |...args| action.value(*args) }
	asString { ^name }
}

ServerSelectorWidget : ServerWidgetBase {
	var serverList, view, list, runningText, bootButton, defaultButton, optionsMenu, optionsView, controller, serverQuitView;

	actions {
		var actions = [
			ServerViewAction("Options", 'o', {
				if (optionsView.isNil) {
					optionsView = ServerOptionsGui(server);
					optionsView.parent.autoRememberPosition(\ServerOptionsGui);
					optionsView.parent.onClose_({ optionsView = nil })
				};
			}),

			ServerViewAction("Query nodes", 'n',
				{ server.queryAllNodes(true) }),

			ServerViewAction("Level meters", 'l',
				{ server.tryPerform(\meter) }),

			ServerViewAction("Tree view", 'p',
				{ if(server.serverRunning) { TreeSnapshotView(server).autoUpdate().front() } }),

			ServerViewAction("Scope", 's',
				{
					var scopeWin = server.scope(server.options.numOutputBusChannels);
					scopeWin.scopeView.waveColors = 10.collect {
						| i |
						var h, s, v, a;
						#h, s, v, a = brightBlue.asHSV();
						h = (h + (i * 0.68)).mod(1).min(1).max(0);
						Color.hsv(h, s, v, a);
					};
				}
			),

			ServerViewAction("Frequency scope", 'f',
				{ server.freqscope }),

			ServerViewAction("Dump OSC", 'd',
				{ if (server.isLocal or: { server.inProcess }) { server.dumpOSC((server.dumpMode + 1) % 2) } }),

			ServerViewAction("Mute", 'm',
				{ if (server.volume.isMuted) { server.unmute } { server.mute } }),

			ServerViewAction("Reset volume", '0',
				{ server.volume = 0 }),
		];

		actions = [
			ServerViewAction("Boot", ' ',
				{ if(server.serverRunning.not) { server.boot } }),

			ServerViewAction("Quit", ' ',
				{ if(server.serverRunning) { server.quit } }),
		] ++ actions;

		actions = actions ++ [
			ServerViewAction("Kill all servers", ' ',
				{ Server.killAll }),
			ServerViewAction("Close Server View", 27.asAscii.asSymbol,
				{ parent.window.rememberPosition(\ServerView); parent.close() }),

		];

		^actions
	}

	view {
		controller = (SimpleController(server)
			.put(\serverRunning, this.onRunning(_,_))
			.put(\default, this.onDefault(_,_))
		);

		serverList = [Server.internal, Server.local];
		view = View();
		list = PopUpMenu();
		list.items = serverList.collect({
			| s |
			s.name;
		});
		list.value = serverList.indexOf(server);
		list.action = {
			|v|
			parent.server = serverList[v.value];
		};
		list.maxHeight_(18);
		list.font = this.font(12, false);
		view.layout_(
			HLayout(
				//StaticText().string_("SUPERCOLLIDER").font_(this.font(16, true)),
				bootButton = (StaticText()
					.minWidth_(180)
					.font_(this.font(15, true))
					//					.maxSize_(22, 22)
					.mouseUpAction_(this.bootAction(_))
				),
				//runningText = StaticText().font_(this.font(16, true)).align_(\center),
				nil,
				defaultButton = (Button()
					.action_(this.defaultAction(_))
					.maxHeight_(18).maxWidth_(22)
					.canFocus_(false)
					.font_(this.font(12, true))
				),
				[list, \align: \right],
				[optionsMenu = PopUpMenu().maxHeight_(18).maxWidth_(22), \align: \right]
			).margins_(0).spacing_(0)
		);

		this.onDefault;

		optionsMenu.allowsReselection = true;
		optionsMenu.items = [""] ++ this.actions.collectAs(_.name, Array);
		optionsMenu.action_({
			|v|
			var name = v.items[v.value];
			this.actions.detect({ |action| action.name == name }).value(server)
		});

		this.onRunning();
		this.onDefault();

		view.onClose_({ controller.remove() });

		^view;
	}

	bootAction {
		if (server.serverRunning.not) {
			server.boot;
		}
	}

	defaultAction {
		Server.default = server;
	}

	killAction {
		Server.killAll;
	}

	onRunning {
		if (server.serverRunning) {
			bootButton.string = "◉ RUNNING";
			bootButton.stringColor = faintGreen;
			// runningText.string = "running";
			// runningText.stringColor = faintGreen;
		} {
			bootButton.string = "◎ INACTIVE";
			bootButton.stringColor = Color.grey;
			// runningText.string = "inactive";
			// runningText.stringColor = Color.grey;
		}
	}

	onDefault {
		if (Server.default == server) {
			defaultButton
			.states_([["D", nil, faintGreen]])
			.font_(this.font(12, true))
		} {
			defaultButton
			.states_([["D"]])
			.font_(this.font(12, false))
		}
	}

}

ServerStatusWidget : ServerWidgetBase {
	classvar <allCounters;
	var view, controller, columns=3, trackedCounts, counters;

	*initClass {
		allCounters = List[\numSynths, \numGroups, \numUGens, \avgCPU, \numSynthDefs, \peakCPU, \sampleRate, \actualSampleRate];
	}

	view {
		var counterViews;
		controller = (SimpleController(server)
			.put(\counts, this.onCounts(_,_))
		);

		counters = IdentityDictionary();
		trackedCounts = [\numSynths, \numGroups, \avgCPU, \numUGens, \numSynthDefs, \peakCPU];
		trackedCounts.do {
			|countName|
			counters[countName] = this.perform( ("create_" ++ countName).asSymbol);
		};
		counterViews = List();
		trackedCounts.do({|c|
			counters[c].view.background_(ServerWidgetBase.brightenColor(QtGUI.palette.window, -0.02));
			counterViews.add([ counters[c].view, columns:counters[c].span ]);
			(counters[c].span - 1).do { counterViews.add(nil) }
		});
		counterViews = counterViews.clump(columns);

		view = View().layout_(
			GridLayout.rows(*counterViews).spacing_(1).margins_(0)
		);

		view.onClose_({ controller.remove() });

		^view;
	}

	onCounts {
		allCounters.do {
			|counterName|
			counters[counterName] !? { counters[counterName].value = server.perform(counterName) };
		}
	}

	create_numUGens {
		^GraphCounter("UGENS", "", this.font(8), brightBlue.alpha_(0.6), 0);
	}

	create_numSynths {
		^GraphCounter("SYNTHS", "", this.font(8), brightBlue.alpha_(0.6), 0);
	}

	create_numGroups {
		^NumberCounter("GROUPS", "", this.font(8));
	}

	create_numSynthDefs {
		^NumberCounter("SYNTHDEFS", "", this.font(8));
	}

	create_avgCPU {
		^GraphCounter("AVG CPU", "%", this.font(8), brightGreen, 0, 100, brightRed.alpha_(0.6));
	}

	create_peakCPU {
		^GraphCounter("PEAK CPU", "%", this.font(8), brightGreen, 0, 100, brightRed.alpha_(0.6));
	}
}

NumberCounter {
	var name, units, font, <view, heading, number, <>span=1;

	*new {
		|name, units, font|
		^super.newCopyArgs(name, units, font).init;
	}

	init {
		var mod = if (QtGUI.palette.window.asHSV[2] > 0.5) {-0.5} {0.3};
		view = View().layout_(VLayout(
			heading = (StaticText()
				.string_(name)
				.font_(font)
				.stringColor_(Color.grey)
				.align_(\center)
			),
			number = (StaticText()
				.font_(font.boldVariant.size_(font.size + 2))
				.stringColor_(Color.grey(0.7 + mod))
				.align_(\center)
			)
		).margins_(0).spacing_(0));
	}

	value_{
		|val|
		number.string = (val.round(0.1).asString + units);
	}
}

GraphCounter {
	var name, units, font, color, <>min, <>max, maxColor, <>minFixed=false, <>maxFixed=false, historySize, reverse,
	<view, heading, number, history, <>span=1, <>round=0.1
	;

	*new {
		|name, units, font, color, min, max, maxColor, historySize=20, reverse=false|
		^super.newCopyArgs(name, units, font, color, min, max, maxColor, min.notNil, max.notNil, historySize, reverse)
		.init;
	}

	init {
		var mod = if (QtGUI.palette.window.asHSV[2] > 0.5) {-0.5} {0.3};
		history = LinkedList.newFrom(0 ! historySize);
		view = UserView().layout_(VLayout(
			heading = (StaticText()
				.string_(name)
				.font_(font)
				.stringColor_(Color.grey)
				.align_(\center)
			),
			number = (StaticText()
				.font_(font.boldVariant.size_(font.size + 2))
				.stringColor_(Color.grey(0.7 + mod))
				.align_(\center)
				.visible_(units.notNil)
			)
		).margins_(0).spacing_(0))
		.drawFunc_({
			|v|
			var b = v.bounds, size;
			size = history.size()- 1;
			Pen.push();

			Pen.width = 0;
			if (reverse) {
				Pen.scale(b.width, b.height.neg);
				Pen.translate(0, -1);
				Pen.moveTo(0@0);
			} {
				Pen.scale(b.width.neg, b.height.neg);
				Pen.translate(-1, -1);
				Pen.moveTo(0@0);
			};

			history.do {
				|val, i|
				Pen.lineTo(
					(i / size) @ (val.linlin(min, max, 0, 1)).min(1).max(0)
				);
			};
			Pen.lineTo(1@0);
			Pen.lineTo(0@0);
			if (maxColor.notNil && (history.first.linlin(min, max, 0, 1) > 0.9)) {
				Pen.fillColor = maxColor.alpha_(0.3);
				Pen.strokeColor = maxColor.blend(Color.white, 0.1).alpha_(0.8);
			} {
				Pen.fillColor = color.alpha_(0.3);
				Pen.strokeColor = color.blend(Color.white, 0.1).alpha_(0.8);
			};
			Pen.draw(3);

			Pen.pop();
		})
		.mouseDownAction_(this.resetMinMax(_));
		this.resetMinMax();
	}

	resetMinMax {
		if (maxFixed.not) { max = -9999999 };
		if (minFixed.not) { min = 9999999 };
		view.refresh();
	}

	value_{
		|val|
		history.pop();
		history.addFirst(val);

		if ((val < min) && minFixed.not) {
			min = val;
		};
		if ((val > max) && maxFixed.not) {
			max = val;
		};

		number.string = (val.round(round).asString + units);
		view.refresh();
	}
}

VolumeWidget : ServerWidgetBase {
	var view, controller, volumeNum, volumeSlider, muteButton, volSpec, <>height=18;

	view {
		controller = SimpleController(server.volume)
		.put(\amp, {|changer, what, vol|
			{
				volumeNum.value_(vol.round(0.01));
				volumeSlider.value_(volSpec.unmap(vol));
			}.defer
		})
		.put(\mute, {|changer, what, flag|
			{
				muteButton.value_(flag.binaryValue);
			}.defer
		})
		.put(\ampRange, {|changer, what, min, max|
			volSpec = [min, max, \db].asSpec;
			volumeSlider.value_(volSpec.unmap(server.volume.volume));
		});
		volSpec = [server.volume.min, server.volume.max, \db].asSpec;

		view = View().layout_(
			VLayout(
				HLayout(
					StaticText().font_(this.font(9)).string_("VOL:  "),
					volumeSlider = (Slider()
						.action_({ |v| server.volume_(volSpec.map(v.value).round(0.1)) })
						.orientation_(\horizontal)
						.maxHeight_(height)
						.thumbSize_(height - 5)
						.value_(volSpec.unmap(server.volume.volume))
					),
					volumeNum = (NumberBox()
						.action_({ |v| server.volume_(v.value.round(0.1)) })
						.font_(this.font().boldVariant)
						.stringColor_(QtGUI.palette.windowText).normalColor_(QtGUI.palette.windowText)
						.maxWidth_(48)
						.maxHeight_(height - 2)
						.value_(server.volume.volume)
					),
					muteButton = (Button()
						.action_({ |v| (v.value == 1).if({ server.mute }, {server.unmute}) })
						.font_(this.font().boldVariant)
						.maxHeight_(height - 2).maxWidth_(22)
						.canFocus_(false)
						.states_([
							["M"],
							["M", nil, faintRed]
						])
					)
				).margins_(0).spacing_(0),

			).margins_(0).spacing_(0)
		);
		view.onClose_({ controller.remove() });

		^view;
	}
}

RecordWidget : ServerWidgetBase {
	var view, pathString, <>recPath, timeString, timeRoutine;

	isRecording {
		^server.recordNode.notNil
	}

	drawRecording {
		| size |
		Pen.stringCenteredIn("▨", Rect(0,1,size,size),
			font: Font(size:14),
			color: QtGUI.palette.windowText
		);
	}

	drawStopped {
		| size |
		Pen.stringCenteredIn("●", Rect(0,1,size,size),
			font: Font(size:18),
			color: Color.hsv(0, 0.8, 0.9)
		);
	}

	buttonClicked {
		if (this.isRecording) {
			server.stopRecording();
			timeRoutine.stop();
			timeString.string = "";
		} {
			server.record();

			Routine({
				var i = 0;
				while { i < 100 && this.isRecording.not } {
					i = i - 1;
					0.1.wait;
				};

				view.parent.refresh;
			}).play(AppClock);

			timeRoutine = Routine({
				var startTime = thisThread.seconds;
				inf.do {
					timeString.string = (thisThread.seconds - startTime).asTimeString(maxDays:0)[3..];
					0.1.wait;
				}
			}).play(AppClock)
		};

		view.refresh;
	}

	view {
		var recButton, pathString, label, path, openButton, showButton;

		label = StaticText().string_("REC:").font_(this.font(9));

		pathString = (DragSink()
			.font_(this.font(8))
			.stringColor_(QtGUI.palette.windowText.alpha_(0.7))
			.background_(Color.grey(0.5, 0.8))
			.fixedHeight_(20)
		);
		pathString.string = " ..." +/+ PathName(thisProcess.platform.recordingsDir).folderName +/+ PathName(thisProcess.platform.recordingsDir).fileName;
		pathString.mouseUpAction = {
			if (thisProcess.platformClass == OSXPlatform) {
				"open '%'".format(thisProcess.platform.recordingsDir).unixCmdGetStdOut();
			}
		};
		pathString.canReceiveDragHandler = { View.currentDrag.asString.pathExists == \folder };
		pathString.receiveDragHandler = {
			thisProcess.platform.recordingsDir = View.currentDrag.asString;
			pathString.string = " ..." +/+ PathName(thisProcess.platform.recordingsDir).folderName +/+ PathName(thisProcess.platform.recordingsDir).fileName;
		};

		timeString = (TextField()
			.font_(this.font(11, true))
			.align_(\right)
			.stringColor_(QtGUI.palette.windowText)
			.background_(Color.clear)
			.fixedWidth_(75)
			.fixedHeight_(20)
		);

		recButton = UserView().fixedHeight_(22).fixedWidth_(22);
		recButton.drawFunc = {
			|v|
			if (this.isRecording) {
				this.drawRecording(20)
			} {
				this.drawStopped(20)
			}
		};

		recButton.mouseUpAction = this.buttonClicked(_);

		view = View().layout_(HLayout(
			label, pathString,
			5, timeString, recButton
		).margins_(0).spacing_(9));

		^view
	}
}

ScopeWidget : ServerWidgetBase {
	var view, <scopeView, <meters, <synth, levelSynth, levelsName, outresp, bus, rate=\audio, inChannels=2, outChannels=2, index=0,
	updateFreq=18, cycle=2048, dBLow = -80, numRMSSamps;

	*new {
		|...args|
		var me;
		me = super.new(*args);
		^me
	}

	view {
		var subviews;
		levelsName = (server.name ++ "OutputWidgetLevels");

		inChannels = server.options.numInputBusChannels;
		outChannels = server.options.numOutputBusChannels;

		meters = (inChannels + outChannels).collect {
			(LevelIndicator()
				.fixedWidth_(8 - (inChannels + outChannels).linlin(4, 16, 0, 6).round(1))
				.minWidth_(2)
				.stepWidth_(1)
				.style_(1)
				.drawsPeak_(true)
			)
		};
		subviews = (meters[0..(inChannels-1)]
			++ [[scopeView = ScopeView(), stretch:2]]
			++ meters[inChannels..]);

		view = View().layout_(HLayout(
			*subviews
		).margins_(0).spacing_(1));

		scopeView.server = server;
		scopeView.canFocus = true;
		scopeView.background = QtGUI.palette.window;
		ServerTree.add(this, server);
		ServerQuit.add(this, server);
		if (server.serverRunning) {
			this.startSynth();
		};

		scopeView.mouseWheelAction_({ |...args| this.mouseWheelAction(*args) });
		scopeView.mouseUpAction_({ scopeView.style = (scopeView.style + 1) % 3 });

		view.onClose = {
			this.stopSynth();
		};

		^view;
	}

	mouseWheelAction {
		| v, x, y, mods, xScroll, yScroll |
		if (scopeView.notNil) {
			if (xScroll != 0) {
				cycle = (cycle + (xScroll * 1)).linlin(100, 2048, 100, 2048);
				synth.setCycle(cycle);
			};
			if (yScroll != 0) {
				scopeView.yZoom = (scopeView.yZoom + (yScroll * 0.01)).linlin(1, 100, 1, 100);
			};
		}
	}

	playLevelSynth {
		if (levelSynth.notNil) {
			levelSynth.free;
		};
		if (outresp.notNil) {
			outresp;
		};

		numRMSSamps = server.sampleRate / updateFreq;

		levelSynth = SynthDef(levelsName, {
			var sig, imp;
			sig = [
				SoundIn.ar((0..inChannels-1)),
				In.ar(0, outChannels)
			].flatten;

			imp = Impulse.ar(updateFreq);
			SendReply.ar(imp, "/" ++ levelsName,
				// do the mean and sqrt clientside to save CPU
				[
					RunningSum.ar(sig.squared, numRMSSamps),
					Peak.ar(sig, Delay1.ar(imp)).lag(0, 3)
				].flop.flat
			);
		}).play(RootNode(server), nil, \addToTail);

		outresp = OSCdef(\widgetLevels, {
			|msg, t|
			{
				msg = msg[3..];
				msg.pairsDo({
					|val, peak, i|
					var meter;
					meter = meters[i * 0.5];
					meter.value = (val.max(0.0) * numRMSSamps.reciprocal).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
					meter.peakLevel = peak.ampdb.linlin(dBLow, 0, 0, 1);
				})
			}.defer;
		}, ("/" ++ levelsName).asSymbol, server.addr).fix;
	}

	startSynth {
		if (synth.isNil) {
			synth = BusScopeSynth(server);
		};
		if (bus.isNil) {
			bus = Bus(rate, index, outChannels, server);
		};
		if (synth.isPlaying.not) {
			scopeView.stop();

			scopeView.server = server;
			scopeView.bufnum = bus.index;
			scopeView.style = 1;
			scopeView.yZoom = 0.9;
			scopeView.waveColors = outChannels.collect {
				| i |
				var h, s, v, a;
				#h, s, v, a = brightBlue.asHSV();
				h = (h + (i * 0.68)).mod(1).min(1).max(0);
				Color.hsv(h, s, v, a);
			};

			scopeView.start();
			synth.play(2048, bus, cycle);
			this.playLevelSynth();
		}
	}

	stopSynth {
		scopeView.stop();
		synth.free; bus.free; levelSynth.free;
		levelSynth = synth = bus = nil;
	}

	doOnServerTree {
		this.startSynth;
	}

	doOnServerQuit {
		this.stopSynth;
	}

	cmdPeriod {
		this.stopSynth();
		this.startSynth();
	}
}

HistoryWidget : ServerWidgetBase {
	var historyResponder, historySynth, counter;
	var cmdName = '/HistoryWidget';

	doOnServerTree {
		|bootingServer|
		if (bootingServer == server) {
			this.start();
		}
	}

	doOnServerQuit {
		|bootingServer|
		if (bootingServer == server) {
			this.stop;
		}
	}

	start {
		var channels = server.options.numOutputBusChannels ?? 2;

		historySynth = SynthDef("ampHistory_%".format(channels).asSymbol, {
			SendPeakRMS.kr(InFeedback.ar(0, channels), 2, 1, cmdName:cmdName);
		}).play(RootNode(server), addAction:\addToTail);

		historyResponder = OSCFunc({
			|msg|
			var peak, rms;
			msg = msg[3..].clump(2).flop;

			peak = msg[0];
			rms = msg[1];

			{ counter.value = peak.maxItem.ampdb.linlin(-60, 0, 0, 1) }.defer;
		}, cmdName);

		historySynth.onFree({
			|freed|
			if (freed == historySynth) {
				historySynth = nil;
			}
		})
	}

	stop {
		historySynth !? { historySynth.free };
		historySynth = nil;

		historyResponder !? { historyResponder.free };
		historyResponder = nil;
	}

	view {
		var view;

		counter = GraphCounter("PEAK DB", nil, this.font(8), brightGreen, 0.0, 1.0, historySize:160);
		view = counter.view;
		view.minHeight_(40);

		if (server.serverBooting.not && server.serverRunning) {
			this.start();
		};

		ServerTree.add(this);
		ServerQuit.add(this);

		view.onClose = { this.stop() };

		^view
	}
}
