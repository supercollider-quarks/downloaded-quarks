// output message format:
// /minibee/output <id> <led0> <led1> <led2> <motor>
// default:
// /minibee/output 7 0 0 0
(
~desc = (
	idInfo: ( ipAddress: "127.0.0.1", destPort: NetAddr.langPort ),
	protocol: \osc,
	'collectives': (
		output: (
			oscPath: '/minibee/output',
			argTemplate: [ 7 ], // default arguments to send
			elements: [ [\led,0], [\led, 1], [\led, 2], \motor ], // elements that make up the group in order that they should appear in the output message
			// more complicate argTemplate:
			// argTemplate: [ nil, 7, nil, 9, 10 ], // default arguments to send (elements take up position 0, 2 and 5 and up
			ioType: \out
		)
	),
	description: (
		'led': [
			(
				default: 0,
				type: 'led', spec: \minibeePWM,
				ioType: \collectiveOut // use a group method to create the output
			),
			(
				default: 0,
				type: 'led', spec: \minibeePWM,
				ioType: \collectiveOut // use a group method to create the output
			),
			(
				default: 0,
				type: 'led', spec: \minibeePWM,
				ioType: \collectiveOut // use a group method to create the output
			)
		],
		'motor': (
			default: 0,
			type: 'led', spec: \minibeePWM,
			ioType: \collectiveOut // use a group method to create the output
		)
	);
);
);

m = MKtl( \example, ~desc );
m.mktlDevice.destDevice

OSCFunc.trace( true );

m.elementAt( \motor ).value_( 1 );

m.elementAt( \led, 0 ).value_(0.3)
m.elements
m.elementAt( \led, 0 ).value_( 0.5 );

m.elementAt( \led, 1 ).value_( 0.25 );

m.elementAt( \led, 1 ).value_( 1 );

(
Tdef( \autoaction, {
	loop{
		m.elementAt( \motor ).value_( 1.0.rand );
		1.0.wait;

		m.elementAt( \led, 0 ).value_( 1.0.rand );
		1.0.wait;

		m.elementAt( \led, 1 ).value_( 1.0.rand );
		1.0.wait;

		m.elementAt( \led, 2 ).value_( 1.0.rand );
		1.0.wait;
	};
}).play;
);

m.gui;