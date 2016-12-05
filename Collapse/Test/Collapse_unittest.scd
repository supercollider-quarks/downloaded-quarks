// UnitTestScript("Collapse", thisProcess.nowExecutingPath).runScript;

{
	|test|
	var collapseValues = List();
	var collapse;

	collapse = Collapse({ |v| collapseValues.add(v) }, 0.1);

	collapseValues.clear;

	collapse.value(1);
	test.assert(collapseValues.isEmpty, "Collapse doesn't exec immediately");

	1.wait;
	test.assertEquals(collapseValues.array, [1], "Collapse execs eventually");

	collapseValues.clear;
	[1, 2, 3, 4, "last"].do { |i| collapse.(i); 0.05.wait };
	1.wait;
	test.assertEquals(collapseValues.array, ["last"], "Collapse only execs last value.");

	collapseValues.clear;
	["first", "second", "last"].do { |i| collapse.(i); 0.2.wait };
	test.assertEquals(collapseValues.array, ["first", "second", "last"], "Collapse execs all values.")

};
