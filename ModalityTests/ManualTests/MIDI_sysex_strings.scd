(
~el = (
	argTemplate: [ 240, 71, 127, 21, 24, 0, 69, 0 ] ++ [ nil ] ++ 247,
	type: \asciiString,
	length: \68,
	\ioType: \out,
	\midiMsgType: \sysex
);
);



(
~val = "hello";
if ( ~el[\type] == \asciiString ){
	var sysexMsg;
	var argTemplate;
	var fullascii;
	var ascii = ~val.ascii;
	if ( ~el[\length].notNil ){
		fullascii = Array.fill( 68, 0 );
		ascii.do{ |it,i| fullascii.put( i, it ); };
	}{
		fullascii = ascii;
	};
	argTemplate = ~el[\argTemplate].copy;
	if ( argTemplate.includes( nil ) ){
		argTemplate.put( argTemplate.indexOf( nil ), fullascii );
	}{
		argTemplate = argTemplate ++ fullascii;
	};
	sysexMsg = argTemplate.flatten;
};
);
);


(
~el = (
	argTemplate: [ 240, 71, 127, 21, 24, 0, 69, 0 ] ++ [ nil ] ++ 247,
	type: \asciiString,
	// length: \68,
	\ioType: \out,
	\midiMsgType: \sysex
);
);



(
~val = "hello";
if ( ~el[\type] == \asciiString ){
	var sysexMsg;
	var argTemplate;
	var fullascii;
	var ascii = ~val.ascii;
	if ( ~el[\length].notNil ){
		fullascii = Array.fill( 68, 0 );
		ascii.do{ |it,i| fullascii.put( i, it ); };
	}{
		fullascii = ascii;
	};
	argTemplate = ~el[\argTemplate].copy;
	if ( argTemplate.includes( nil ) ){
		argTemplate.put( argTemplate.indexOf( nil ), fullascii );
	}{
		argTemplate = argTemplate ++ fullascii;
	};
	sysexMsg = argTemplate.flatten;
};
);
);
