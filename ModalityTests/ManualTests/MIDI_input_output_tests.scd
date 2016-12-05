/*
Note: bug in SC where MIDI noteOn messages with velocity 0 are always parsed as noteOff messages.

There is a pull request to SuperCollider for this, which makes this an option rather than a default:


MIDIIn.noteOnZeroAsNoteOff = false;

See: https://github.com/supercollider/supercollider/issues/1483
and: https://github.com/supercollider/supercollider/pull/1488

*/


// find all MIDI devices (and do MIDI initialisation)
MKtl.find(\midi);

// make a MIDIOut for the tests in the examples

//Linux:
// link::Classes/MIDIOut#Linux specific: Connecting and disconnecting ports::
~midiOut= MIDIOut.new(0); // = SC's first midi output
~midiOut.connect(1); // = connect to SC's first midi input

// for OSX use IAC bus for these tests:
link::Classes/MIDIOut#OS X specific: Sending MIDI to other applications::


// midiMsgType: \noteOnOff,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	protocol: \midi,
	description: (
		'bt': (
			type: 'button',
			midiMsgType: \noteOnOff,
			midiChan: 0,
			midiNum: 64,
			spec: \midiVel,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.noteOn(0, 64, 127 );
~midiOut.noteOn(0, 64, 0 ); // creates a noteOff message (see note)
~midiOut.noteOff(0, 64, 0 );

// midiMsgType: \noteOn,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	protocol: \midi,
	    description: (
		'key': (
			type: 'pianoKey',
			midiMsgType: \noteOn,
			midiChan: 0,
			midiNum: 64, // note number
			spec: \midiVel,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.noteOn(0, 64, 127 );
~midiOut.noteOn(0, 64, 24 );
~midiOut.noteOn(0, 64, 0 ); // this is a bug - see note

// midiMsgType: \noteOff,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'key': (
			type: 'pianoKey',
			midiMsgType: \noteOff,
			midiChan: 0,
			midiNum: 64, // note number
			spec: \midiVel,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.noteOff(0, 64, 127 );
~midiOut.noteOff(0, 64, 24 );
~midiOut.noteOff(0, 64, 0 );



// midiMsgType: \cc - knob
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'kn': (
			type: 'knob',
			midiMsgType: \cc,
			midiChan: 0,
			midiNum: 8, // control number
			spec: \midiCC,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.control(0, 8, 127 );
~midiOut.control(0, 8, 24 );
~midiOut.control(0, 8, 0 );


// midiMsgType: \cc - button
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	protocol: \midi,
	    description: (
		'bt': (
			type: 'button',
			midiMsgType: \cc,
			midiChan: 0,
			midiNum: 8, // control number
			spec: \midiBut,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.control(0, 8, 127 );
~midiOut.control(0, 8, 0 );


// midiMsgType: \bend,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: "SuperCollider", // Linux: this may need to be different on OSX
	    protocol: \midi,
	    description: (
		'bend': (
			type: 'bender',
			midiMsgType: \bend, // bend needs midiChan only
			midiChan: 0,
			spec: \midiBend,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.bend(0, 128*128-1 ); // 14-bit value
~midiOut.bend(0, 128*64 );
~midiOut.bend(0, 0 );


// midiMsgType: \polyTouch,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	protocol: \midi,
	description: (
		'touch': (
			type: 'touch',
			midiMsgType: \polyTouch,
			midiChan: 0,
			midiNum: 2,
			spec: \midiTouch,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.polyTouch(0, 2, 127 );
~midiOut.polyTouch( 0, 2, 63 );
~midiOut.polyTouch(0, 2, 0 );


// midiMsgType: \touch,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	protocol: \midi,
	    description: (
		'touch': (
			type: 'touch',
			midiMsgType: \touch, // touch does not need \midiNum
			midiChan: 0,
			spec: \midiTouch,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.touch(0, 127 );
~midiOut.touch(0, 63 );
~midiOut.touch(0, 0 );


// midiMsgType: \program,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'bt': (
			type: 'button',
			midiMsgType: \program, // program does not need \midiNum
			midiChan: 0,
			spec: \midiProgram,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.program(0, 127 );
~midiOut.program(0, 63 );
~midiOut.program(0, 0 );

/*** this does not work in SC's MIDI implementation yet:

// midiMsgType: \allNotesOff,
(
~descInput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: "SuperCollider", // Linux: this may need to be different on OSX
	    protocol: \midi,
	    description: (
		'key': (
			type: 'button',
			midiMsgType: \allNotesOff, // allNotesOff does not need \midiNum
			midiChan: 0,
			spec: \midiCC,
			ioType: \in
		)
	)
)
);

m = MKtl( \testMIDI, ~descInput );
m.rebuildFrom( ~descInput ); // updating it

m.trace;

// make some messages:
~midiOut.allNotesOff(0);
~midiOut.allNotesOff(0);
***/

////========= output =========

(
// connect all midi ports
MIDIIn.connectAll;

// trace all incoming midi
MIDIFunc.trace;
);



// midiMsgType: \noteOn
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'bt': (
			type: 'button',
			midiMsgType: \noteOn,
			midiChan: 0,
			midiNum: 2,
			spec: \midiVel,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \bt ).value_( 0 ); // see note above on noteOn message with velocity zero; may show up in the trace as a noteOff
m.elementAt( \bt ).value_( 1 );

m.gui; // and press the button


// midiMsgType: \noteOff
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'bt': (
			type: 'button',
			midiMsgType: \noteOff,
			midiChan: 0,
			midiNum: 2,
			spec: \midiVel,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \bt ).value_( 0 );
m.elementAt( \bt ).value_( 1 );

m.gui; // and press the button



// midiMsgType: \cc - slider
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'sl': (
			type: 'slider',
			midiMsgType: \cc,
			midiChan: 0,
			midiNum: 2,
			spec: \midiCC,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \sl ).value_( 0 );
m.elementAt( \sl ).value_( 0.4 );
m.elementAt( \sl ).value_( 1 );

m.gui; // and move the slider



// midiMsgType: \bend
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'bend': (
			type: 'slider',
			midiMsgType: \bend, // bend has no midiNum
			midiChan: 0,
			spec: \midiBend,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \bend ).value_( 0 );
m.elementAt( \bend ).value_( 0.4 );
m.elementAt( \bend ).value_( 1 );



// midiMsgType: \polyTouch
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'touch': (
			type: 'slider',
			midiMsgType: \polyTouch,
			midiChan: 0,
			midiNum: 2,
			spec: \midiTouch,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \touch ).value_( 0 );
m.elementAt( \touch ).value_( 0.4 );
m.elementAt( \touch ).value_( 1 );




// midiMsgType: \touch
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'key': (
			type: 'slider',
			midiMsgType: \touch, // touch does not need \midiNum
			midiChan: 0,
			spec: \midiTouch,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \key ).value_( 0 );
m.elementAt( \key ).value_( 0.4 );
m.elementAt( \key ).value_( 1 );



// midiMsgType: \program,
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
	// idInfo: ( linux: "SuperCollider", osx: "IAC-bus" ), // Linux: this may need to be different on OSX
	    idInfo: ( name: "SuperCollider", sourcePortIndex: 0 ),
	    protocol: \midi,
	    description: (
		'key': (
			type: 'slider',
			midiMsgType: \program, // program does not need \midiNum
			midiChan: 0,
			spec: \midiProgram,
			ioType: \out
		)
	)
)
);

m = MKtl( \testMIDI, ~descOutput );
m.rebuildFrom( ~descOutput ); // updating it

m.elementAt( \key ).value_( 0 );
m.elementAt( \key ).value_( 0.4 );
m.elementAt( \key ).value_( 1 );


/// exploring

MKtl.find( \midi );

a = MKtl('lnch0');

a.explore;

MIDIExplorer.verbose = true;
MIDIExplorer.verbose = false;

a.createDescriptionFile;

a.explore( false );

MIDIExplorer.compile

