// input message format:
// /minibee/data <id> <val1> <val2> <acc x> <acc y> <acc z>
// default:
// /minibee/data 7 0 0 0.5 0.5 0.5

(
MKtl.addSpec(\minibeeData, [0, 1]);
MKtl.addSpec(\minibeeDigital, [0, 1, \linear, 1]);

~desc = (
	idInfo: ( ipAddress: "127.0.0.1" ),
	protocol: \osc,
	collectives: (
		data: (
			// would fire the do something with the elements inside
			oscPath: '/minibee/data',
			// argTemplate: [ 7 ],
			argTemplate: [ 7, nil, 2 ], // default arguments to send
			valueAt: [ 4, 5, 6, 7, 8],
			elements: [ [\bt,0], [\bt,1], [\accelero,\x], [\accelero,\y], [\accelero,\z] ],
			ioType: \in
		)
	),
	description: (
		'bt': [
			( 'type': 'button', spec: \minibeeDigital, \ioType: \collectiveIn ),
			( 'type': 'button', spec: \minibeeDigital, \ioType: \collectiveIn )
		],
		'accelero': (
			x: ( 'type': 'accelerationAxis', spec: \minibeeData, \ioType: \collectiveIn ),
			y: ( 'type': 'accelerationAxis', spec: \minibeeData, \ioType: \collectiveIn ),
			z: ( 'type': 'accelerationAxis', spec: \minibeeData, \ioType: \collectiveIn ),
		)
	);
);
);

m = MKtl( \example, ~desc );

m.trace;
n = NetAddr.new( "127.0.0.1", NetAddr.langPort );
n.sendMsg( "/minibee/data", 7, "blah".scramble, 2,  2.rand, 2.rand, 0.5.rand, 0.5.rand, 0.5.rand );

n.sendMsg( "/minibee/data", 7, 0, 1, 0.2, 0.3, 0.5 );


m.elementAt( \accelero )

m.mktlDevice.srcDevice;
m.mktlDevice.destDevice;
m.mktlDevice.recvPort;

MKtlDevice.allAvailable // now also shows the opened 'osc device'

// close the device to open a new one:
m.closeDevice;
m.openDevice( devDesc: ( protocol: \osc, device: ( recvPort: 57121, destPort: 57121, srcPort: 57121, ipAddress: "127.0.0.1" ) ) );

// OSCMKtlDevice.sourceDeviceDict

// create a gui:
m.gui;

m.trace;

m.deviceInfoDict
m.collectivesDict

m.mktlDevice.oscFuncDictionary

n = NetAddr.new( "127.0.0.1", NetAddr.langPort );
n.sendMsg( "/minibee/data", 7, 0, 0, 0.5, 0.5, 0.5 );

n.sendMsg( "/minibee/data", 7, 0, 1, 0.2, 0.3, 0.5 );

n.sendMsg( "/minibee/data", 7, 1, 1, 0.2, 0.3, 0.5 );

(
Tdef( \autoaction, {
	loop{
		n.sendMsg( "/minibee/data", 7, 2.rand, 2.rand, 1.0.rand, 1.0.rand, 1.0.rand );
		1.0.wait;
	};
}).play;
);
OSCFunc.trace( true );

m.elementAt( \bt, 0 ).action = { "button!!!".postln; };

OSCFunc.trace( false );


// send messages:
n.sendMsg( '/pad', 1, 2, 63 );

n.sendMsg( '/pad', 1, 2, 35 );

n.sendMsg( '/pad', 1, 2, 34 );


// rebuild when changing spec:
m.rebuildFrom( ~desc ); // just updates the elements
// reopen gui afterwards:
m.gui;
