(
MKtl.addSpec(\pwm8bit, [0, 255, \linear, 1]);
~desc = (
	device: "exampleled",
	protocol: \osc,
	port: NetAddr.langPort, // if messages are sent from a specific port
	description: (
		'led': (
			oscPath: '/output',
			argTemplate: [ 3 ], // message will be: "/minibee/output" 3 <val>
			'type': 'led', spec: \pwm8bit,
			\ioType: \out
		);
	);
);
);

(
MKtl.addSpec(\pwm8bit, [0, 255, \linear, 1]);
~desc = (
	idInfo: ( ipAddress: "127.0.0.1", destPort: NetAddr.langPort ),
	protocol: \osc,
	description: (
		'led': (
			oscPath: '/output',
			argTemplate: [ 3, nil, 2 ], // message will be: "/minibee/output" 3 <val>
			'type': 'led', spec: \pwm8bit,
			\ioType: \out
		);
	);
);
)

MKtl.find; // starts osc exploration in the backend

m = MKtl( \example, ~desc );

// we need to send a test message to get a key to the host (this is what is still under development):
n = NetAddr.new( "127.0.0.1", NetAddr.langPort );
n.sendMsg( \led, 0 );

// check name of the new device:
MKtlDevice.allAvailable
// and use it as the device:
m.openDevice( \host0_57120 );
// create a gui:
m.gui;

OSCFunc.trace( true ); // trace what is coming in

// set a value:
m.elementAt( \led ).value_( 0.6 );
m.elementAt( \led ).value_( 1.0.rand );


// rebuild when changing spec:
m.rebuildFrom( ~desc );
// reopen gui afterwards:
m.gui;
