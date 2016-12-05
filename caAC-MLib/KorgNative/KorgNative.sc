KorgToggleMatrix {
	var padObjects, <korgNative, >onAction, >offAction;

	*new {
		^super.new.init;
	}

	init {
		korgNative = KorgNative();
		padObjects = Array.fill(16, { |i|
			var ktp = KorgTogglePad(i, korgNative);
			ktp.onAction = { onAction.value(i) };
			ktp.offAction = { onAction.value(i) };
			ktp;
		});
	}
}



KorgTogglePad {
	var responder, >onAction, >offAction, <index;

	*new { |noteNumber, korgNative|
		^super.newCopyArgs.init(noteNumber, korgNative)
	}

	init { |noteNumber, korgNative|
		var state = 0;
		responder = NoteOnResponder.new(
			{
				state = state + 1 % 2;
				if (state == 1) {
					if (onAction.notNil) { onAction.value(noteNumber) };
				} {
					if (offAction.notNil) { offAction.value(noteNumber) };
				};
				korgNative.padLight(noteNumber, state * 32);
			},
			num: noteNumber + 1
		);
	}
}




KorgNative {
	var <>toKorg;

	*new { ^super.new.init }

	setupControls {
		toKorg.sysex(
			Int8Array[
				0xF0,0x42,0x40,0x6E,0x08,0x00,0x00,0x01,0xF7,0xF0,0x42,0x40,0x6E,
				0x08,0x3F,0x2A,0x00,0x00,0x05,0x05,0x05,0x7F,0x7E,0x7F,0x7F,0x03,
				0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,
				0x0A,0x0A,0x0A,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,
				0x0B,0x0C,0x0d,0x0E,0x0F,0x10,0xF7,0xF0,0x42,0x40,0x6E,0x08,0x3F,
				0x0A,0x01,0x7F,0x7F,0x7F,0x7F,0x7F,0x00,0x38,0x38,0x38,0xF7
			]
		);
		toKorg.sysex(
			Int8Array[
				0xF0,0x42,0x40,0x6E,0x08,0x00,0x00,0x01,0xF7,0xF0,0x42,0x40,0x6E,
				0x08,0x3F,0x2A,0x00,0x00,0x05,0x05,0x05,0x7F,0x7E,0x7F,0x7F,0x03,
				0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,0x0A,
				0x0A,0x0A,0x0A,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,
				0x0B,0x0C,0x0d,0x0E,0x0F,0x10,0xF7,0xF0,0x42,0x40,0x6E,0x08,0x3F,
				0x0A,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x29,0x29,0x29,0xF7
		]
		);
	}

	init {
		MIDIClient.init;
		toKorg = MIDIOut.newByName("padKONTROL", "CTRL");
		toKorg.sysex(Int8Array[0xf0,0x42,0x40,0x6e,0x08,0x00,0x00,0x01,0xF7]); // enable native mode
		this.setupControls;
		MIDIIn.connectAll;
		MIDIIn.findPort("padKONTROL", "PORT A").tryPerform(\uid);
	}

	padLight { |padNumber = 0, padState = 32|
		toKorg.sysex(Int8Array[0xf0, 0x42, 0x40, 0x6e, 0x08, 0x01, padNumber, padState, 0xf7])
	}

}


// The padLight doesn't work without having to blink all the lights first by using the code:
// Search what sysex string needs to be send inorder to make this work!
// Create with easy array lookup an easy mapping tool instead of using an case construction!
/*

a = KorgToggleMatrix()
a.onAction = { |argIndex| ["on", argIndex].postln; };
a.offAction = { |argIndex| ["off", argIndex].postln; };

a = KorgNative()
a.padLight(0, 32)

- Create a pad object. This object will keep track its own state. This object will work and behave as an
- GUI element. An array of objects will represent the padKontrol Matrix.
- Options are oneShot play, toggle play
- assign a sysex responder and divide the functionality over several function.
MIDIIn.connectAll;
		1.wait;
MIDIIn.findPort("padKONTROL", "PORT A").tryPerform(\uid);
~midiSysex = MIDIIn.sysex = { |scr, sysex|

		sysex = sysex.drop(5).drop(-1);
                "a".postln;
		sysex.postln;

};
~midiSysex = MIDIIn.sysex = { |scr, sysex|
		sysex = sysex.drop(5).drop(-1);
                "b".postln;
		sysex.postln;

};

~midiSysex = MIDIIn.sysex = {};
~state = 0;
MIDIFunc.noteOn({arg ...args;
~state = ~state + 1 % 2;
a.padLight(0, postln(~state * 32));
args.postln}, 1); // match any noteOn
*/




