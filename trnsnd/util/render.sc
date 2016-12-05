// server needs not boot
// use load for sysnthdef

+ Score {
	*render { |chan=1 dur=1 header="WAV" format="int16" sampleRate=44100 filename defname args buf debug=false|
		var server = Server.default;
		var oscfp = PathName.tmp +/+ UniqueID.next;
		var path;
		var score;
		var bufScore;

		chan = chan ? 1;
		dur = dur ? 1;
		header = header ? "WAV";
		format = format ? "int16";
		sampleRate = sampleRate ? 44100;
		filename = filename ? Date.localtime.format("%y-%m-%d_%H-%m-%S");
		defname = defname ? \default;
		header.switch(
			"AIFF", { path = "~/Desktop/".standardizePath ++ filename ++ ".aif" },
			"WAV", { path = "~/Desktop/".standardizePath ++ filename ++ ".wav" }
		);
		if(buf.notNil, {
			if(buf.isKindOf(SequenceableCollection), {
				bufScore = [];
				buf.do({ |item,i|
					bufScore = bufScore ++ [[0, ["/b_allocRead", item.bufnum, item.path, 0, -1, nil]]];
				});
			}, {
				if(buf.path.isNil, {
					// it's a tmp buffer
					// bufScore = [[0, ["/b_alloc", buf.bufnum, buf.numFrames, buf.numChannels]]];
					// bufScore = bufScore ++ [[0, ["/b_setn", buf.bufnum, 0, buf.numFrames] ++ ]];
				}, {
					bufScore = [[0, ["/b_allocRead", buf.bufnum, buf.path, 0, -1, nil]]];
				});
			});
		});
		score = [
			// nodeID can proceed without server running
			[0,   [\s_new, defname, server.nextNodeID, 0, 1] ++ args],
			[dur, [\c_set, 0, 0]]
		];
		if(bufScore.notNil, { score = bufScore ++ score });

		this.recordNRT(
			score,
			oscfp,
			path,
			if(buf.isNil, { nil }, { buf.path }),
			sampleRate,
			header,
			format,
			ServerOptions.new
			.verbosity_(-1)
			.numOutputBusChannels_(chan)
			.sampleRate_(sampleRate)
			,
			action: { File.delete(oscfp); File.delete(SynthDef.synthDefDir+/+defname++".scsyndef") }
		);
		if(debug, { this.saveToFile("~/".standardizePath ++ "debug.osc") });
	}
}
+ Synth {
	*render { |defname args chan dur header format sampleRate filename buf|
		Score.render(chan, dur, header, format, sampleRate, filename, defname, args, buf);
	}
}
+ Function {
	render { |chan dur header format sampleRate filename buf debug|
		var def, server;
		def = this.asSynthDef(fadeTime: 0);
		server = Server.default;
		Routine {
			// complition Msg does not work!
			def.load(server);
			1.wait;
			// render
			Synth.render(def.name, nil, chan, dur, header, format, sampleRate, filename, buf, debug);
		}.play;
	}
}


+ Pattern {
	render { |chan=1 dur=1 header="WAV" format="int16" sampleRate=44100 filename buf opt debug=false|
		var server = Server.default;
		var oscfp = PathName.tmp +/+ UniqueID.next;
		var path;
		var score, patternAsScore, bufScore;
		filename = filename ? Date.localtime.format("%y-%m-%d_%H-%m-%S");
		header.switch(
			"AIFF", { path = "~/Desktop/".standardizePath ++ filename ++ ".aif" },
			"WAV", { path = "~/Desktop/".standardizePath ++ filename ++ ".wav" }
		);
		if(opt.isNil, {
			// minimum option
			opt = ServerOptions.new.numOutputBusChannels_(chan);
		}, {
			// append chan option
			opt = opt.numOutputBusChannels_(chan);
		});
		// do anyway
		opt.sampleRate = sampleRate;
		if(buf.notNil, {
			if(buf.isKindOf(Array), {
				bufScore = [];
				buf.do({ |item i|
					bufScore = bufScore ++ [[0, ["/b_allocRead", item.bufnum, item.path, 0, -1, nil]]];
				});
			}, {
				bufScore = [[0, ["/b_allocRead", buf.bufnum, buf.path, 0, -1, nil]]];
			});
		});
		score = this.asScore(dur);
		patternAsScore = score.score;
		if(bufScore.notNil, { patternAsScore = bufScore ++ patternAsScore });
		if(debug, {
			var f;
			f = File.new("~/Desktop/".standardizePath ++ "debug.osc", "w");
			f.putString("[ // SuperCollider Score output " ++ Date.getDate ++ "\n");
			patternAsScore.do({ arg me;
				f.putString((me).asCompileString ++ "\n\n");
			});
			f.putString("]");
			f.close;
			}, {
				Score.recordNRT(
					patternAsScore,
					oscfp,
					path,
					if(buf.isNil, { nil }, { buf.path }),
					sampleRate,
					header,
					format,
					opt,
					action: {
						File.delete(oscfp);
						// hackish... rely on the pattern.asScore message structure
						// to find the synthdef name
						// does not work when buffer alloc message is appended
						File.delete(SynthDef.synthDefDir+/+patternAsScore[1][1][1]++".scsyndef")
					}
				);
				"wait ...".inform;
		});
	}
}