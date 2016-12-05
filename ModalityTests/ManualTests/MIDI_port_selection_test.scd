MKtl.find;

MIDIClient.sources

MKtl('bltn0');  // [ "Ableton Push", "Ableton Push MIDI 1" ]
MKtl('bltn1');  // [ "Ableton Push", "Ableton Push MIDI 2" ]


(
~desc = (
	// idInfo: "Ableton Push",
	idInfo: ( name: "Ableton Push", sourcePortIndex: 1, destinationPortIndex: 1 ),
	protocol: \midi,
	status: "untested",
	description: (// display still missing because SysEx and different MIDI port...
		//------ pads -------------
		\pad: (
			\on: (36..99).collect{|num|
				('midiMsgType': \noteOn, \type: \pad, \midiChan: 0, \midiNum: num, \spec: \midiNote, \ioType: \inout)
			}.clump(8),
			\off: (36..99).collect{|num|
				('midiMsgType': \noteOff, \type: \pad, \midiChan: 0, \midiNum: num, \spec: \midiNote, \ioType: \inout)
			}.clump(8)
		),
		\poly: (36..99).collect{|num|
			('midiMsgType': 'polyTouch', 'type': 'keytouch', 'midiChan': 0, 'midiNum': num,'spec': 'midiCC', \ioType: \in)
		}.clump(8)
	)
);
);

MKtl('test', ~desc );  // [ "Ableton Push", "Ableton Push MIDI 2" ]
MKtl('test').trace;

(
~myName = "Ableton Push";
~foundSources = List.new;
MIDIMKtlDevice.sourceDeviceDict.keysValuesDo{ |key,endpoint|
	if ( endpoint.device == ~myName ){ ~foundSources.add( endpoint ) }
};
~foundSources.sort( { |a,b| a.name < b.name } ).at( 1 );
);
	if ( endpoint.device == rawDeviceName ){
		if ( rawPortName.isNil ){
			devKey = key;
		}{
			if ( endpoint.name == rawPortName ){
				devKey = key;
			}
		}
	};
};
);