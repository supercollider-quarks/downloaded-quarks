// osc message of type: /pad <row> <column> <value>
(
~desc = (
	idInfo: ( srcPort: 57121, ipAddress: "127.0.0.1" ),
	protocol: \osc,
	description: (
		'pad': (
				oscPath: '/pad',
				argTemplate: [ 1,2 ], // at row 1 and column 2
				valueAt: 3, // valueindex of osc message
				'type': 'pad', spec: \midiCC,
				\ioType: \in
		)
	)
)
);

(
~desc = (
	idInfo: ( srcPort: 57121, ipAddress: "127.0.0.1" ),
	protocol: \osc,
	description: (
		'pad': (
				oscPath: '/pad',
			// argTemplate: [ 1,2 ], // at row 1 and column 2
			// valueAt: 3, // valueindex of osc message
				'type': 'pad', spec: \midiCC,
				\ioType: \in
		)
	)
)
);

MKtl.find; // starts osc exploration in the backend

m = MKtl( \example, ~desc );

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

n = NetAddr.new( "127.0.0.1", NetAddr.langPort );
n.sendMsg( "/pad", 2, 3, 63 );

// send messages:
n.sendMsg( '/pad', 1, 2, 63 );

n.sendMsg( '/pad', 1, 2, 35 );

n.sendMsg( '/pad', 34 );


// rebuild when changing spec:
m.rebuildFrom( ~desc ); // just updates the elements
// reopen gui afterwards:
m.gui;
