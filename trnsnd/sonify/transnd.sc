
Transnd {
	classvar version = 4.0;
	classvar <data, <buf, soundFile, pathName;// single session

	*new { |path, rawArray|
		^super.new.init(path, rawArray).storeDefs;
	}
	storeDefs {
		SynthDef(\transnd_resample_1, { |buf, rate=1, amp=1, ipol=2|
			var sig, phr;
			phr = Phasor.ar(0, BufRateScale.kr(buf) * rate, 0, BufFrames.kr(buf));
			sig = BufRd.ar(1, buf, phr, 1, ipol);
			sig = sig * amp;
			Out.ar(0, sig);
		}).writeDefFile;
		// mono
		SynthDef(\transnd_playbuf_0, { |buf, rate=1, amp=1, ipol=2|
			var sig, phr;
			phr = Phasor.ar(0, BufRateScale.kr(buf) * rate, 0, BufFrames.kr(buf));
			sig = BufRd.ar(1, buf, phr, 1, ipol);
			Out.ar(0, sig);
		}).writeDefFile;
		// mono->stereo
		SynthDef(\transnd_playbuf_1, { |buf, rate=1, amp=1, ipol=2|
			var sig, phr;
			phr = Phasor.ar(0, BufRateScale.kr(buf) * rate, 0, BufFrames.kr(buf));
			// phr = EnvGen.ar(Env([0, 0, 1], [0, 1]), 1, BufFrames.kr(buf), 0, BufDur.kr(buf) * rate)
			sig = BufRd.ar(1, buf, phr, 1, ipol);
			sig = Pan2.ar(sig, 0, amp);
			Out.ar(0, sig);
		}).writeDefFile;
		// interleaved power of 2
		[2,4,8,16,32].do({ |item|
			SynthDef((\transnd_playbuf_ ++ item).asSymbol, { |buf, rate=1, amp=1, ipol=2|
				var sig, phr;
				phr = Phasor.ar(0, BufRateScale.kr(buf) * rate, 0, BufFrames.kr(buf));
				sig = BufRd.ar(item, buf, phr, 1, ipol);
				sig = sig * amp;
				Out.ar(0, sig);
			}).writeDefFile;
		});
	}
	init { |path, rawArray|
		var raw, file, div;
		pathName = PathName(path);
		if(rawArray.isNil, { rawArray = Int8Array });
		// read raw data
		file = File(pathName.fullPath, "rb");
		raw  = file.read(rawArray.asClass.newClear(file.length));
		file.close;
		// convert to float if needed
		if(rawArray.asString[0] == "I"[0], {
			rawArray.switch(
				Int8Array,  { div = 2 ** 8  / 2 },
				Int16Array, { div = 2 ** 16 / 2 },
				Int32Array, { div = 2 ** 32 / 2 }
			);
			data = FloatArray.newClear(raw.size);
			raw.do({ |item,i| data[i] = item / div });
		}, { data = raw });
	}
	openFolder {
		unixCmd("open " ++ pathName.pathOnly);
	}
	send { |chan=1|
		// single session
		// loadCollection bug!
		buf.free;
		buf = Buffer.sendCollection(Server.default, data, chan);
	}
	play { |rate=1, amp=1, ipol=2|
		var defname = \transnd_playbuf_ ++ buf.numChannels;
		Synth(defname, [buf: buf, rate: rate, amp: amp, ipol: ipol]);
	}
	writeSoundFile { |path, header="WAV", format="int16", sampleRate=44100|
		var sfName;
		var extension = "." ++ header.toLower;
		if(path.isNil, { sfName = pathName.fileName });
		soundFile = SoundFile.new
		.headerFormat_(header)
		.sampleFormat_(format)
		.sampleRate_(sampleRate)
		.numChannels_(buf.numChannels)
		;
		if(soundFile.openWrite(pathName.pathOnly +/+ sfName ++ "@" ++ sampleRate ++ extension), {
			soundFile.writeData(data);
			soundFile.close;
		}, { Error("Could not open.\n").throw });
	}
	*resample { |path, ipol=2, header="WAV", format="int16", sampleRate=44100|
		var pathName, soundFile, defname, bufnum, score, oscfp;
		var extension = "." ++ header.toLower;
		pathName = PathName(path);
		bufnum = UniqueID.next;
		oscfp = PathName.tmp +/+ "osc" ++ bufnum;
		soundFile = SoundFile.openRead(path);
		soundFile.close;
		soundFile.numChannels.switch(
			1, { defname = \transnd_resample_1 },
			2, { defname = \transnd_playbuf_2 }
		);
		score = Score([
			[0, ["/b_allocRead", bufnum, pathName.fullPath, 0, -1]],
			[0, [\s_new, defname, Server.default.nextNodeID, 0, 1, \buf, bufnum, \ipol, ipol]],
			[soundFile.duration, [\c_set, 0, 0]]
		]);
		score.recordNRT(
			oscfp,
			pathName.pathOnly +/+
			pathName.fileNameWithoutExtension ++
			"rsmp@" ++
			sampleRate ++ "ipol" ++ ipol ++ extension,
			nil,
			sampleRate,
			header,
			format,
			ServerOptions.new
			.numOutputBusChannels_(soundFile.numChannels)
			.sampleRate_(sampleRate)
			,
			//doesn't like nil
			"",
			soundFile.duration
			{ unixCmd("rm " + oscfp) }
		);
	}
}

+ String {
	resample { |ipol=2, header="WAV", format="int16", sampleRate=44100|
		Transnd.resample(this, ipol, header, format, sampleRate);
	}
}