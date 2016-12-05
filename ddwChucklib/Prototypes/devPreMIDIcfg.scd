
// runs before initializing default midi responders
// must make sure the right device is set up as the first port

var	index;

MIDIClient.initialized.not.if({
	// restricting connections breaks MIDIPort if there are more sources than given here
	// so I'm removing the restriction. Initialize to the number of reported devices.
	MIDIClient.init/*(MIDIPort.numPorts, MIDIPort.numPorts)*/;

	if(Library.at(\chucklib, \midiSearches).isNil) {
		Library.put(\chucklib, \midiSearches, #["ultralite", "828", "remote"]);
	};

	block { |break|
		Library.at(\chucklib, \midiSearches).do({ |string|
			index = MIDIClient.sources.detectIndex({ |item|
				item.device.find(string, true).notNil
			});
			if(index.notNil) { break.(index) };
		});
	};

	index.notNil.if({
		~srcInports = [index];
	}, {
		~srcInports = nil;
	});

	~srcInports.debug("Index of default MIDI device");
}, {
	"MIDIClient already initialized.".postln;
});

~srcInports ?? { [] };