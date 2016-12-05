/*
UnitTest.reset; UnitTestScript("Connection", thisProcess.nowExecutingPath).runScript;
*/

{
	|test|
	var object, connection;
	var recordFunc = {
		|object, changed ...args|
		~object = object;
		~changed = changed;
		~args = args;
	};
	var collectFunc = {
		|object, changed ...args|
		~object = ~object.add(object);
		~changed = ~changed.add(changed);
		~args = ~args.add(args);
	};
	var lastMessageEquals = {
		|object, changed, args, description|
		test.assertEquals(object, ~object, description ++ " (object correct)");
		test.assertEquals(changed, ~changed, description ++ " (changed correct)");
		test.assertArrayEquals(args, ~args, description ++ " (args correct)");
	};
	var clear = { ~object = ~changed = ~args = nil };
	test.addUniqueMethod(\assertArrayEquals, {
		|test, a, b, message|
		test.assert(
			((a.size + b.size) == 0) or: {
				(a.size == b.size) and: {
					a.every { |item, i| item == b[i] } }
			}, message ++ " (% == %".format(a, b));
	});


	////////////////////////////////////////////////////////////
	// Basic connection
	object = ();
	connection = object.connectTo(recordFunc);

	object.changed(\testA);
	lastMessageEquals.(object, \testA, [],
		"Update sent with no args.");

	object.changed(\testB, "foo");
	lastMessageEquals.(object, \testB, ["foo"],
		"Update sent with one arg.");

	object.changed(\testC, "foo", "bar");
	lastMessageEquals.(object, \testC, ["foo", "bar"],
		"Update sent with two args.");

	clear.();
	connection.disconnect();
	object.changed(\testC, "foo", "bar");
	lastMessageEquals.(nil, nil, nil,
		"Update not sent when disconnected."
	);

	connection.connect();
	object.changed(\test);
	lastMessageEquals.(object, \test, nil,
		"Update sent when reconnected."
	);


	////////////////////////////////////////////////////////////
	// Connection list, makeWith
	object = [(), ()];
	connection = ConnectionList.makeWith {
		object[0].connectTo(collectFunc);
		object[1].connectTo(collectFunc);
	};

	test.assertEquals(connection.size, 2, "ConnectionList.makeWith collects properly.");

	clear.();
	connection.disconnect();
	object.do(_.changed(\test));
	lastMessageEquals.(nil, nil, nil,
		"ConnectionList:disconnect works.");

	connection.connect();
	object.do(_.changed(\test, 1));
	lastMessageEquals.(object, [\test, \test], [[1], [1]],
		"ConnectionList:connect works.");


	////////////////////////////////////////////////////////////
	// disconnectwith
	clear.();
	connection.disconnectWith {
		object.do(_.changed(\shouldmiss));
	};
	object.do(_.changed(\shouldget, 1));
	lastMessageEquals.(object, [\shouldget, \shouldget], [[1], [1]],
		"disconnectWith works");


	////////////////////////////////////////////////////////////
	// Signals
	object = ();
	~connA = object.signal(\foo).connectTo { ~recvFoo = true };
	~connB = object.signal(\bar).connectTo { ~recvBar = true };

	object.changed(\foo);
	object.changed(\bar);
	test.assert(~recvFoo, "Signal \\foo works");
	test.assert(~recvBar, "Signal \\bar works");

	~recvFoo = ~recvBar = false;
	~connA.disconnect; ~connB.disconnect;
	object.changed(\foo);
	object.changed(\bar);
	test.assert(~recvFoo.not, "Disconnecting \\foo works");
	test.assert(~recvBar.not, "Disconnecting \\bar works");

	test.assert(UpdateDispatcher.dispatcherDict.isEmpty,
		"UpdateDispatcher.dispatcherDict was cleaned up."
	);

	////////////////////////////////////////////////////////////
	// Slots
	~target = (
		value_: { |self ...args| ~slotResult = [\value_] ++ args },
		foo: { |self ...args| ~slotResult = [\foo] ++ args },
	);

	~slotResult = nil;
	object = ();
	~conn = object.connectTo(~target.valueSlot);
	object.changed(\something, 1234);
	test.assertArrayEquals(~slotResult, [\value_, 1234],
		"valueSlot works");

	~slotResult = nil;
	object = ();
	~conn = object.connectTo(~target.methodSlot("foo('symbol', 1234, value, object, changed, *args)"));
	object.changed(\something, \theValue, 1, 2);
	test.assertArrayEquals(~slotResult, [\foo, \symbol, 1234, \theValue, object, \something, \theValue, 1, 2],
		"methodSlot argument specification works");

	////////////////////////////////////////////////////////////
	// Chaining
	object = ();
	clear.();
	~conn = object.connectTo(collectFunc).filter({
		|object, changed| (changed == \bar)
	});
	object.changed(\foo, 1);
	object.changed(\bar, 1);
	lastMessageEquals.([object], [\bar], [[1]],
		"Filtering chain works.");

	clear.();
	~conn.disconnect();
	object.changed(\bar, 1);
	lastMessageEquals.(nil, nil, nil, "Chained connection disconnect works");

	clear.();
	~conn.connect();
	object.changed(\bar, 1);
	lastMessageEquals.([object], [\bar], [[1]], "Chained connection reconnect works");

	object = (); clear.();
	~conn = object.connectTo(collectFunc).transform({
		|object, changed, value| [object, \transformed, [value * 2]]
	});
	object.changed(\shouldTransform, 10);
	lastMessageEquals.([object], [\transformed], [[20]], "Chained transformation works.");


	object = (); clear.();
	~conn = object.connectTo(recordFunc).defer(0.5);
	object.changed(\foo, 1);
	test.wait({ ~object.notNil }, "Defered action executed.");
	lastMessageEquals.(object, \foo, [1]);


	object = (); clear.();
	~conn = object.connectTo(collectFunc).collapse(0.2);
	object.changed(\shouldRecieve, 1);
	object.changed(\shouldNot, 2);
	object.changed(\shouldNot, 3);
	object.changed(\shouldNot, 4);
	object.changed(\done, 5);
	test.wait({ ~object.size == 2 },
		"Deferred action executed");
	lastMessageEquals.([object, object], [\shouldRecieve, \done], [[1], [5]]);


	object = (); clear.();
	~conn = (object.connectTo(collectFunc)
		.filter({ |object, what| what == \allowed })
		.transform({ |object, what ...args| [object, \transformed, args] })
		.defer(0.2));
	object.changed(\allowed, 1);
	object.changed(\notAllowed, 2);
	~done = false; { ~done = true; }.defer(0.3);
	test.wait({ ~done },
		"Deferred action executed.");
	lastMessageEquals.([object], [\transformed], [[1]]);

}
