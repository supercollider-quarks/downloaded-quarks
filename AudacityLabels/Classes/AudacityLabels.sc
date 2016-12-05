LabelsDictionary : IdentityDictionary {

	var <>rejectDuplicates = false;

	*new { |rejectDuplicates = false|
		^super.new.rejectDuplicates_(rejectDuplicates)
	}

	defaultEvent {
		^(type: \rest, dur: 0)
	}

	addAsArray { |wort ... events|
		var old = this[wort];
		if(old.isNil) { super[wort] = events } {
			if(rejectDuplicates and: { this[wort].notNil }) {
				"Duplicate Labels not allowed, overwriting previous label '%'".format(wort).warn;
				super[wort] = events.unbubble
			} {
				super[wort] = super[wort] ++ events
			}
		}
	}

	addProperties { |propertiesDict|
		this.do { |item|
			if(item.isArray) {
				item.do { |event| event.putAll(propertiesDict) }
			} {
				item.putAll(propertiesDict)
			}
		}
	}

	put { |wort, event|
		this.addAsArray(wort, event)
	}

	at { |wort|
		^this.get(wort)
	}

	get { |wort, choiceFunc|
		var value = super.at(wort.asSymbol);
		if(wort.isSequenceableCollection) {
			^wort.collect { |each| this.get(each, choiceFunc) };
		};
		^if(value.isNil) { nil } {
			if(value.isArray) {
				if(choiceFunc.notNil) {
					choiceFunc.(value)
				} {
					value.choose
				}
			} {
				value
			}.copy
		}
	}

	choose {
		^super.choose.asArray.choose;
	}

	all { |choiceFunc, sortFunc|
		var values;
		values = this.get(this.keys.asArray, choiceFunc);
		sortFunc = sortFunc ? { |a, b| a[\t0] < b[\t0] };
		^values.sort(sortFunc)
	}

	putEvent { |wort, event| // rename later.
		var newEvent = (
			wort: wort,
			t0: event[\t0] ? 0,
			t1: event[\t1] ? 1,
			rate: 1.0,
			gap: 0.0,
			finish: {
				var dt = ~t1 - ~t0;
				~start !? { ~t0 = ~t0 + (dt * ~start) };
				~end !? { ~t1 = ~t0 + (dt * ~end) };
				~dur = abs((~t1 - ~t0) / ~rate) + ~gap;
			}
		);
		this.addAsArray(wort, newEvent)
	}


}

AbstractAudacityLabels {

	var <rejectDuplicates = false;
	var <dict, <>verbose = true;
	var numberWord = 0;

	*new { |rejectDuplicates|
		^super.newCopyArgs(rejectDuplicates).clear
	}

	clear {
		dict = LabelsDictionary.new(rejectDuplicates);
	}

	get { |wort, choiceFunc|
		^dict.get(wort, choiceFunc)
	}

	at { |wort|
		^dict.get(wort).copy // make a copy by default.
	}

	put { |wort, event|
		dict.put(wort, event)
	}

	all { |choiceFunc, sortFunc|
		^dict.all(choiceFunc, sortFunc)
	}

}


AudacityLabels : AbstractAudacityLabels {


	*read { |labelPath|
		^this.new.read(labelPath)
	}

	read { |labelPath|
		var string = File.use(labelPath, "r", { |file| file.readAllString });
		this.parseAndAddLabels(string, labelPath);
	}

	parseAndAddLabels { |string, labelPath|
		var zeilen = string.split(Char.nl);
		if(verbose) { "Reading labels from: %\n\n".postf(labelPath) };
		zeilen.do({ |zeile|
			var daten, t0, t1, wort, event;
			daten = zeile.split(Char.tab);
			if(daten.size >= 3) {
				t0 = daten[0].replace(",", ".").asFloat; // account for format bug in audacity: convert to dot.
				t1 = daten[1].replace(",", ".").asFloat;
				wort = daten[2];
				if(wort.isEmpty or: { wort.every { |char| char.isSpace }}) {
					wort = numberWord;
					numberWord = numberWord + 1;
				} {
					wort = wort.asSymbol
				};
				dict.putEvent(wort, (t0:t0, t1:t1));
				if(verbose) { wort.post; " ".post; }
			};
		});
		if(verbose) { "\n".post };
	}

}

LabeledSoundFile : AbstractAudacityLabels {

	var <buffers;

	*new { |rejectDuplicates = false|
		^super.newCopyArgs(rejectDuplicates).clear
	}

	clear {
		buffers.do { |x| x.free };
		buffers = [];
		dict = LabelsDictionary.new(rejectDuplicates);
	}


	*read { |soundFilePath, labelPath, server|
		^this.new.read(soundFilePath, labelPath, server)
	}

	read { |soundFilePath, labelPath, server, finishFunc|
		var labels;
		server = server ? Server.default;
		if(server.serverRunning.not) { "Server not running!".warn; ^this };

		fork {
			var defName, bufevent;
			var buffer = this.getBuffer(server, soundFilePath);
			server.sync;
			defName = if(buffer.numChannels == 2) { \labelPlayer_2 } { \labelPlayer_1 };
			bufevent = (server: server, buffer: buffer, instrument: defName);
			labels = AudacityLabels(rejectDuplicates).verbose_(verbose);
			labels.read(labelPath);
			labels.dict.addProperties(bufevent);
			labels.dict.keysValuesDo { |wort, event|
				dict.addAsArray(wort, *event);
			};
			finishFunc.value(this);
		}
	}

	putEvent { |wort, event| // assumes that buffer is in \buffer key.
		var buffer = event[\buffer];
		if(buffer.notNil) {
			if({ buffers.includesEqual(buffer).not }) {
				buffers = buffers.add(buffer);
			};
			event.use {
				if(~instrument.isNil) {
					~instrument = if(buffer.numChannels == 2) { \labelPlayer_2 } { \labelPlayer_1 };
				};
				~t0 = ~t0 ? 0.0;
				~t1 = ~t1 ? buffer.duration ? 1.0;
			};
		};
		event[\wort] = wort;
		dict.addAsArray(wort, event);

	}

	getBuffer { |server, path|
		var buffer = buffers.detect { |buf| buf.path == path and: { buf.server == server } };
		if(buffer.isNil) {
			buffer = Buffer.read(server, path);
			buffers = buffers.add(buffer)
		};
		^buffer
	}

	maxWordSize {
		var max = 0;
		dict.keysDo { |name| max = max(max, name.asString.size) };
		^max
	}

	*initClass {

		Class.initClassTree(SynthDescLib);

		SynthDef(\labelPlayer_1, { |out = 0, rate = 1, t0, t1, buffer, pan, amp = 0.1|
			var sustain;
			var ton, env, channels;
			sustain = abs((t1 - t0) / rate); // Gesamtdauer
			env = EnvGen.kr(
				Env.linen(0.001, sustain, 0.01, (amp * 10)),
				doneAction:2 // die Hüllkurve beendet den Synth
			);

			ton = env * PlayBuf.ar(
				1, // mono
				buffer, // der buffer,
				rate * BufRateScale.kr(buffer), // Abspielrate mit Ausgleich
				startPos:BufSampleRate.kr(buffer) * t0, // Startposition
				loop: 1 // falls wir versehentlich über das Ende hinausgehen
			);
			OffsetOut.ar(out, Pan2.ar(ton, pan))
		}).add;

		SynthDef(\labelPlayer_2, { |out = 0, rate = 1, t0, t1, buffer, pan, amp = 0.1|
			var sustain;
			var ton, env, channels;

			sustain = abs((t1 - t0) / rate); // Gesamtdauer
			env = EnvGen.kr(
				Env.linen(0.001, sustain, 0.01, (amp * 10)),
				doneAction:2 // die Hüllkurve beendet den Synth
			);

			ton = env * PlayBuf.ar(
				2, // stereo
				buffer, // der buffer,
				rate * BufRateScale.kr(buffer), // Abspielrate mit Ausgleich
				startPos:BufSampleRate.kr(buffer) * t0, // Startposition
				loop: 1 // falls wir versehentlich über das Ende hinausgehen
			);
			OffsetOut.ar(out, Balance2.ar(ton[0], ton[1], pan))
		}).add;


	}

}



