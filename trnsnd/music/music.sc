
AbstractMusic {
	//class
	var path, argv;
	//audio
	var <s, <scOptions, <buflist, <buslist, <sflist;
	//music
	var <t;
	//gui
	var que, score, qwin, qText, qFont, tv1, tv2;//qwin should classvar to avoid multiple instances?

	*new { |...args| ^super.new.initAbstractMusic(args); }

	initAbstractMusic {| args |
		path = this.class.filenameSymbol.asString.dirname ++ "/";
		buflist = List[];
		buslist = List[];
		sflist  = List[];
		argv = args;
		s = Server.default;
		scOptions = ServerOptions.new;
		this.setServerOptions; //should not be in the routine for subclasses
		Routine({
			this.killall;
			"killing scsynth before rebooting...".warn;
			3.wait;
			s.options = scOptions;
			s.volume.setVolumeRange(-90, 12);
			this.rebootServer;
		}).play;
		t = TempoClock.default;
	}
	killall { Server.killAll }

	// override if needed
	setServerOptions {
		scOptions
		.sampleRate_(44100)
		.numInputBusChannels_(2)
		.numOutputBusChannels_(2)
		;
	}
	rebootServer {
		s.waitForReboot({
			this.addBuflist;
			this.addBuslist;
			this.sendSynthDefs;
			this.readScore;
		});
	}
	end {
		this.freeBuflist; buflist = nil;
		this.freeBuslist; buslist = nil;
		sflist = nil;
		this.freeResponders;
		currentEnvironment.clear;
		s.quit;
		s = nil;
		t = nil;
	}
	test {| out=0, tone='pulse', amp=0.1 |
		{
			Out.ar(out,
				tone.switch(
					\pulse, { Impulse.ar(10, mul: amp) },
					\pink, { PinkNoise.ar(amp) },
					\sine, { SinOsc.ar(mul: amp) },
					\woof, { SinOsc.ar(30, mul: amp) },
					{ SinOsc.ar(mul: amp) }
				)
			);
		}.play;
	}
	rek {
		var i = s.inputBus;
		var o = s.ouputBus;
		^[i, o];
	}
	stoprek {}
	freeBuflist { buflist.do(_.free) }
	freeBuslist { buslist.do(_.free) }

	//override
	addBuflist {}//buflist.add(Buffer.read(s, path ++ ))
	addBuslist {}//buslist.add(Bus.audio(s, 1))
	sendSynthDefs {}
	addResponders {
//		MIDIClient.init;
//		MIDIIn.connectAll;
	}
	freeResponders {
//		MIDIClient.disposeClient;
//		MIDIdef.all.clear;
	}
	readScore {
		var file;
		if(File.exists(path ++ "q.scd"), {
			file = File(path ++ "q.scd", "r");
			score = file.readAllString;
			file.close;
		});
	}
	openScoreFile {
		("open " ++ (path ++ "q.scd").escapeChar($ )).unixCmd;
	}
	showQueInView {| q, v, interpret=false |
		var start, end, queue;
		start = score.find("//" ++  q.asString);
		end   = score.find("//" ++ (q + 1).asString);
		if(end.isNil.not, {
			queue = score.copyRange(start, end - 1);
			if(interpret, { queue.interpret });
			v.string_(queue);
		}, {
			v.string_("end of file");
		});
	}
	forwardQ {
		Routine({
			qText.string_(que);
			qText.stringColor_(Color.white);
			qText.background_(Color.green);
			// catch error
			{ this.showQueInView(que, tv1, true) }.try({ |error|
				tv1.string = error.errorString
			});
			que = que + 1;
			this.showQueInView(que, tv2, false);//prepare
			0.3.wait;
			qText.stringColor_(Color.black);
			qText.background_(Color.white);
		}).play(AppClock);
	}
	rewindQ {
		s.freeAll;
		Routine({
			que = que - 1;
			qText.string_(que);
			qText.stringColor_(Color.white);
			qText.background_(Color.yellow(1, 0.2));
			tv1.string_("");//clear tv1
			this.showQueInView(que, tv2, false);//prepare
			0.3.wait;
			qText.stringColor_(Color.black);
			qText.background_(Color.white);
		}).play(AppClock);
	}
	resetQ {
		s.freeAll;
		Routine({
			qText.string_("Q");
			qText.stringColor_(Color.white);
			qText.background_(Color.blue(1, 0.2));
			tv1.string_("");//clear tv1
			que = 0;
			this.showQueInView(que, tv2, false);//prepare
			0.3.wait;
			qText.stringColor_(Color.black);
			qText.background_(Color.white);
		}).play(AppClock);
	}
	jump2Q {|num|
		s.freeAll;
		que = num;
		Routine({
			qText.string_(que);
			qText.stringColor_(Color.white);
			qText.background_(Color.green);
			// catch error
			{ this.showQueInView(que, tv1, true) }.try({ |error|
				tv1.string = error.errorString
			});
			que = que + 1;
			this.showQueInView(que, tv2, false);
			0.3.wait;
			qText.stringColor_(Color.black);
			qText.background_(Color.white);
		}).play(AppClock);
	}
	gui {
		// gui addition
		var h, w;
		// meters
		var iLevels, oLevels, levels, iFunc, oFunc, iSynth, oSynth, synthFunc;
		var numIns = s.options.numInputBusChannels;
		var numOuts = s.options.numOutputBusChannels;
		var separator;

		// gui
		if((GUI.id == 'qt').not, { GUI.qt });
		h = Window.screenBounds.height;
		w = Window.screenBounds.width;
		qFont = Font("Monaco", 9);

		// window
		qwin = Window(this.class.asString, Rect(100, 100, w * 0.7, h * 0.5));
		qwin.alwaysOnTop = false;

		// meters
		separator = UserView();
		separator.drawFunc = {|v|
			Pen.strokeColor = Color.black;
			Pen.line(0@0, 0@v.bounds.height);
			Pen.stroke;
		};
		iLevels = Array.fill(numIns, {
			LevelIndicator()
			.drawsPeak_(true)
			.warning_(0.8)
			.numTicks_(9)
			.numMajorTicks_(3)
			;
		});
		oLevels = Array.fill(numOuts, {
			LevelIndicator()
			.drawsPeak_(true)
			.warning_(0.8)
			.numTicks_(9)
			.numMajorTicks_(3)
			;
		});
		levels = iLevels ++ [separator] ++ oLevels;

		iFunc = OSCFunc({| msg |
			{
				iLevels.do({|item, i|
					item.value     = msg[i * 2 + 4].ampdb.linlin(-40, 0, 0, 1);
					item.peakLevel = msg[i * 2 + 3].ampdb.linlin(-40, 0, 0, 1);
				});
			}.defer;
		}, '/i_reply', s.addr);
		iFunc.fix;

		oFunc = OSCFunc({| msg |
			{
				oLevels.do({|item, i|
					item.value     = msg[i * 2 + 4].ampdb.linlin(-40, 0, 0, 1);
					item.peakLevel = msg[i * 2 + 3].ampdb.linlin(-40, 0, 0, 1);
				});
			}.defer;
		}, '/o_reply', s.addr);
		oFunc.fix;

		synthFunc = {
			s.bind({
				iSynth = SynthDef('iLevels', {
					var sig;
					sig = In.ar(NumOutputBuses.ir, numIns);
					SendPeakRMS.kr(sig, 15, 1.5, '/i_reply');
				}).play(RootNode(s), nil, \addToHead);

				oSynth = SynthDef('oLevels', {
					var sig;
					sig = In.ar(0, numOuts);
					SendPeakRMS.kr(sig, 15, 1.5, '/o_reply');
				}).play(RootNode(s), nil, \addToTail);
			});
		};
		ServerTree.add(synthFunc, s);
		if(s.serverRunning, synthFunc);

		qwin.onClose = {
			iFunc.free;
			oFunc.free;
			iSynth.free;
			oSynth.free;
			ServerTree.remove(synthFunc, s);
		};

		// gui
		qwin.layout = HLayout(
			VLayout(
				StaticText()
				.string_("queue moves on the RELEASE of keys")
				,
				StaticText()
				.background_(Color.green(1, 0.2))
				.string_("forward: Space")
				,
				StaticText()
				.background_(Color.yellow(1, 0.2))
				.string_("rewind: Shift + Space")
				,
				StaticText()
				.background_(Color.blue(1, 0.2))
				.string_("reset: Shift + r")
				,
				StaticText()
				.string_("fired queue:")
				,
				tv1 = TextView()
				.editable_(false)
				.font_(qFont)
				.canFocus_(false)
				,
				StaticText()
				.string_("next queue:")
				,
				tv2 = TextView()
				.editable_(false)
				.font_(qFont)
				.canFocus_(false)
				,
				HLayout(
					StaticText().string_("jump to queue: "),
					NumberBox()
					.value_(0)
					.action_({|nb|
						this.jump2Q(nb.value);
					})
				)
			),
			[
				qText = StaticText()
				.font_(Font("Monaco", w * 0.15))
				.stringColor_(Color.black)
				.string_("Q")
				.align_('center')
				.minWidth_(w * 0.3)
				,
				stretch: 1
			],
			*levels
		);
		qwin.view.keyUpAction_({|v,c,m,u,k|
			if(u==32, {
				if(m.isShift, { this.rewindQ }, { this.forwardQ });
			});
			if(u==82, { this.resetQ });
		});
		this.resetQ;
		qwin.front;
	}
}