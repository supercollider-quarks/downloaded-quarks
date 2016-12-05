
/*
s.boot;
a = AudioRecorder()

w = Window.new; a.makeGui(w, Rect(100, 100, 200, 200)); w.front; a.doneAction_({ |buffer| buffer.play; b = buffer });
*/


AudioRecorder {
	var widget, widgetDependant, model, valueFunction, actionDependant;
	var <>busIn, <>channels, <>maxRecordTime, <>sampleRate, <>recTime, tempBuffer, recordSynth;
	var >doneAction, <>monitorBus;

	*new { |argBusIn = 2, argChannels = 1, argMaxRecordTime = 60, argSampleRate = 44100|
		^super.newCopyArgs.init(argBusIn, argChannels, argMaxRecordTime, argSampleRate)
	}

	stopRecordSynthAndCreateNewBuffer {
		var buffer;
		if(recordSynth.isPlaying == true) {
			recordSynth.free;
			recordSynth = thisThread.seconds - recTime;

			buffer = Buffer.alloc(numFrames: sampleRate * recTime, numChannels: channels);
			tempBuffer.copyData(buffer, numSamples: sampleRate * recTime);

			if (doneAction.notNil) { doneAction.value(buffer) }
		};
	}

	init { |argBusIn, argChannels, argMaxRecordTime, argSampleRate|
		busIn = argBusIn;
		channels = argChannels;
		maxRecordTime = argMaxRecordTime;
		sampleRate = argSampleRate;
		monitorBus = 127;
		tempBuffer = Buffer.alloc(numFrames: sampleRate * maxRecordTime, numChannels: channels);

		model = (recordState: 0);

		valueFunction = { |argValue|
			model[\recordState] = argValue;
			model.changed(\recordState, argValue);
		};

		actionDependant = { |theChanger, what, argValue|
			"record %\n".postf(argValue);
			if (argValue > 0) {
				this.stopRecordSynthAndCreateNewBuffer;
				// Register the runned time of the Server thread:
				recTime = thisThread.seconds;
				recordSynth = { |in, bufnum|
					Out.ar(monitorBus, RecordBuf.ar(SoundIn.ar(busIn, channels),tempBuffer));
				}.play(addAction: 'addToTail');
				recordSynth.track;
			} {
				this.stopRecordSynthAndCreateNewBuffer;
			};
		};

		model.addDependant(actionDependant);

	}

	makeGui { |parent, bounds|
		var widgetDependant;

		widget = Button(parent, bounds)
		.states_([
			["REC", Color.red, Color.black],
			["REC", Color.black, Color.red]
		])
		.value_(model[\recordState])
		.action_({ valueFunction.value(widget.value) });

		widgetDependant = { |theChanger, what, argValue|
			widget.value = argValue;
		};
		model.addDependant(widgetDependant);

	}

	closeGui {
		widget.remove; model.removeDependant(widgetDependant)
	}
}



	