// UnitTestScript("Require", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var r1Result, r2Result, assertIdentical, assertNotIdentical;

	//////////////////////////
	// Hack: UnitTestScript doens't properly set nowExecutingPath, so we have to do it manually.
	var oldPath = thisProcess.nowExecutingPath;
	thisProcess.nowExecutingPath = PathName(Require.filenameSymbol.asString).pathOnly +/+ "Test/Test_unittest.scd";
	//////////////////////////

	assertIdentical = {
		|a, b, msg=""|
		test.assert(a === b, "% (% === %)".format(msg, a.asString, b.asString));
	};

	assertNotIdentical = {
		|a, b, msg=""|
		test.assert(a !== b, "% (% !== %)".format(msg, a.asString, b.asString));
	};

	protect({
		Require.reset();

		~required1_was_run = ~required2_was_run = false;
		test.assertEquals(Require("Required1"), "Required1",
			"Require returns correct value");
		test.assert(~required1_was_run,
			"Relative require works");
		test.assertEquals(Require("Subdir/Required2"), "Required2",
			"Require subfolder returns correct value");
		test.assert(~required2_was_run,
			"Relative require in subfolder works");

		~required1_was_run = ~required2_was_run = false;
		assertIdentical.(Require("Required1"), Require("Required1"),
			"Cached require returns correct value.");
		assertIdentical.(Require("Subdir/Required2"), Require("Subdir/Required2"),
			"Cached require in subfolder returns correct value.");
		test.assert(~required1_was_run.not && ~required2_was_run.not,
			"Cached requires were not re-executed.");

		Require.reset();
		~required1_was_run = ~required2_was_run = false;
		Require("Required1");
		Require("Subdir/Required2");
		test.assert(~required1_was_run && ~required2_was_run,
			"Requires were re-run after reset.");

		~required1_was_run = ~required2_was_run = false;
		assertNotIdentical.(
			Require("Required1", always:true),
			Require("Required1", always:true),
			"Require1 returned non-identival value with always:true");
		assertNotIdentical.(
			Require("Subdir/Required2", always:true),
			Require("Subdir/Required2", always:true),
			"Require2 returned non-identical value with always:true");
		test.assert(~required1_was_run && ~required2_was_run,
			"Requires were re-run with always parameter.");

		assertIdentical.(Require("Required1"), Require("Required1.scd"),
			"Cache hit on 'Required1' vs 'Required1.scd'");
		assertIdentical.(Require("Required1"), Require(thisProcess.nowExecutingPath.dirname +/+ "Required1.scd"),
			"Cache hit on 'Required1' vs absolute path");
		assertIdentical.(Require("Required1"), Require("Re????ed*"),
			"Cache hit on 'Required1' vs wildcard match");

		assertIdentical.(Require("Subdir/Required2"), Require("Subdir/Required2.scd"),
			"Cache hit on 'Required2' vs 'Required2.scd'");
		assertIdentical.(Require("Subdir/Required2"), Require(thisProcess.nowExecutingPath.dirname +/+ "Subdir/Required2.scd"),
			"Cache hit on 'Required2' vs absolute path");
		assertIdentical.(Require("Subdir/Required2"), Require("Subdir/Re????ed*"),
			"Cache hit on 'Required2' vs wildcard match");

		~required1_was_run = ~required2_was_run = false;
		"touch \"%\"".format(thisProcess.nowExecutingPath.dirname +/+ "Required1.scd").unixCmdGetStdOut;
		"touch \"%\"".format(thisProcess.nowExecutingPath.dirname +/+ "Subdir/Required2.scd").unixCmdGetStdOut;
		Require("Required1"); Require("Subdir/Required2");
		test.assert(~required1_was_run && ~required2_was_run,
			"Require re-runs in case of file update.");

		~required1_was_run = ~required2_was_run = false;
		Require("Required1"); Require("Subdir/Required2");
		test.assert(~required1_was_run.not && ~required2_was_run.not,
			"New file mod time is properly cached.");

	}, { thisProcess.nowExecutingPath = oldPath })
}

