m = MIDIValueMatcher.new( [64], { |v| v.postln } );

m.value( 64 );
m.value( 43 );

m = MIDIValueMatcher.new( { |v| v<64 }, { |v| v.postln } );

m.value( 64 );
m.value( 43 );

n = MIDIFunc.noteOn({arg ...args; args.postln}, argTemplate: [43] ); // match any note
n = MIDIFunc.noteOn({arg ...args; args.postln} ); // match any note

MIDIIn.doNoteOnAction(1, 1, 64, 64); // spoof a note on

MIDIIn.doNoteOnAction(1, 1, 64, 43); // spoof a note on

