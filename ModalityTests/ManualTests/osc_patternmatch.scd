n = NetAddr("127.0.0.1", NetAddr.langPort); // local machine

// o = OSCFunc.newMatching({|msg, time, addr, recvPort| msg.post; \matching.postln}, '/button/*', n); // path matching

p = OSCMessageInversePatternDispatcher.new;
o = OSCFunc.new({|msg, time, addr, recvPort| msg.post; \matching.postln}, '/button/*', n, dispatcher: p ); // inverse path matching

n.sendMsg( "/button/off" );
n.sendMsg( "/button/on" );

t = "/button/*";
t.includes( $* )
t = "/button";
t.includes( $* )


t = "/button/*/morestuff";
i = t.split( $/ ).indexOfEqual( "*" );

t = "/button/hello/morestuff";
t.split( $/ ).at( i );

v = StringMap.new( [ "off", "on" ] );


(
~descInput = (
	specs: ( onoffString: ItemsSpec.new( [ "off", "on" ] ); ),
	    idInfo: ( srcPort: NetAddr.langPort, ipAddress: "127.0.0.1" ),
	    protocol: \osc,
	    description: (
		        'bt': (
			            oscPath: '/button/*',
			            type: 'button',
			            spec: \onoffString,
			            ioType: \in
		        )
	    )
)
);

m = MKtl( \exampleOSCInput, ~descInput );
MKtlDevice.allAvailable // now also shows the opened 'osc device'

// create a gui for the osc device:
m.gui;

// post incoming data
m.trace;

n = NetAddr.new( "127.0.0.1", NetAddr.langPort );

// send messages:
n.sendMsg( "/button/off" );
n.sendMsg( "/button/on" );


n.sendMsg( '/knob', 1, 2, 63 );

n.sendMsg( '/knob', 1, 2, 35 );

n.sendMsg( '/knob', 1, 2, 34 );

// close the device again
m.closeDevice;




/// triggers:


(
~descInput = (
	idInfo: ( srcPort: NetAddr.langPort, ipAddress: "127.0.0.1" ),
	protocol: \osc,
	description: (
		'tr': (
			oscPath: '/trigger',
			type: 'trigger',
			argTemplate: [1,2],
			ioType: \in
		)
	)
)
);

m = MKtl( \exampleOSCInput, ~descInput );
MKtlDevice.allAvailable // now also shows the opened 'osc device'

// create a gui for the osc device:
m.gui;

// post incoming data
m.trace;

n = NetAddr.new( "127.0.0.1", NetAddr.langPort );

// send messages:
n.sendMsg( "/trigger", 1, 2, 3 );
n.sendMsg( "/trigger", 1, 2 );
n.sendMsg( "/trigger", 2, 3 );
n.sendMsg( "/trigger", 1 );
n.sendMsg( "/trigger" );

// close the device again
m.closeDevice;

m.mktlDevice.oscFuncDictionary.at( \tr ).dump
m.mktlDevice.

OSCMKtlDevice.messageSizeDispatcher

AbstractResponderFunc.allFuncProxies


n = NetAddr.new( "127.0.0.1", NetAddr.langPort );

OSCdef( \test, { |msg| msg.postln; }, "/test", argTemplate: [ 0 ] );

n.sendMsg( "/test", 0, 2 );
n.sendMsg( "/test", 1, 1 );
n.sendMsg( "/test", 1 );
n.sendMsg( "/test" );

OSCdef( \test, { |msg| msg.postln; }, "/test", argTemplate: [ "hello" ] );

n.sendMsg( "/test", "hello", 2 );
n.sendMsg( "/test", 1, 1 );

OSCdef( \test, { |msg| msg.postln; }, "/minibee/data", argTemplate: [ 7 ] );

n.sendMsg( "/minibee/data", 7, 0, 0 ,0 ); // matches
n.sendMsg( "/minibee/data", 6, 0, 0 ,0 ); // doesn't match


OSCdef( \test, { |msg| msg.postln; }, "/grid", argTemplate: [ 2, 3 ] );

n.sendMsg( "/grid", 2, 3, 0.4 ); // matches
n.sendMsg( "/grid", 1, 2, 0.2 ); // doesn't match

n.sendMsg( "/grid", 2 ); // matches??
n.sendMsg( "/grid" ); // matches??

OSCdef( \test, { |msg| msg.postln; }, "/second", argTemplate: [ nil, [3,2] ] );

n.sendMsg( "/second", 2, 3, 0.4 ); // matches
n.sendMsg( "/second", 2, 2, 0.4 ); // matches
n.sendMsg( "/second", 1, 4, 0.2 ); // doesn't match