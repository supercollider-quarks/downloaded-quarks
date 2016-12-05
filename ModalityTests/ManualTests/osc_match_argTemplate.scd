n = NetAddr.new( "127.0.0.1", NetAddr.langPort );

OSCdef( \test, { |msg| msg.postln; }, "/test", argTemplate: [ 0 ] );

n.sendMsg( "/test", 0, 2 );
n.sendMsg( "/test", 1, 1 );

OSCdef( \test, { |msg| msg.postln; }, "/test", argTemplate: [ "hello" ] );

n.sendMsg( "/test", "hello", 2 );
n.sendMsg( "/test", 1, 1 );

OSCdef( \test, { |msg| msg.postln; }, "/minibee/data", argTemplate: [ 7 ] );

n.sendMsg( "/minibee/data", 7, 0, 0 ,0 ); // matches
n.sendMsg( "/minibee/data", 6, 0, 0 ,0 ); // doesn't match


OSCdef( \test, { |msg| msg.postln; }, "/grid", argTemplate: [ 2, 3 ] );

n.sendMsg( "/grid", 2, 3, 0.4 ); // matches
n.sendMsg( "/grid", 1, 2, 0.2 ); // doesn't match


OSCdef( \test, { |msg| msg.postln; }, "/second", argTemplate: [ nil, [3,2] ] );

n.sendMsg( "/second", 2, 3, 0.4 ); // matches
n.sendMsg( "/second", 2, 2, 0.4 ); // matches
n.sendMsg( "/second", 1, 4, 0.2 ); // doesn't match

