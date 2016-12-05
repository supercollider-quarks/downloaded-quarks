// UnitTest.reset; UnitTestScript("Log", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var logString = "This is a test.";

	Log(\unittestlog).level = \debug;

	Log(\unittestlog).debug(logString);
	test.assertEquals(Log(\unittestlog).lines.last.string, logString);
	test.assertEquals(Log(\unittestlog).lines.last.level, \debug);

	Log(\unittestlog).info(logString);
	test.assertEquals(Log(\unittestlog).lines.last.string, logString);
	test.assertEquals(Log(\unittestlog).lines.last.level, \info);

	Log(\unittestlog).warning(logString);
	test.assertEquals(Log(\unittestlog).lines.last.string, logString);
	test.assertEquals(Log(\unittestlog).lines.last.level, \warning);

	Log(\unittestlog).error(logString);
	test.assertEquals(Log(\unittestlog).lines.last.string, logString);
	test.assertEquals(Log(\unittestlog).lines.last.level, \error);

	Log(\unittestlog).critical(logString);
	test.assertEquals(Log(\unittestlog).lines.last.string, logString);
	test.assertEquals(Log(\unittestlog).lines.last.level, \critical);

	~formatterHit = false;
	Log(\unittestlog).formatter = { ~formatterHit = true };
	Log(\unittestlog).info(logString);

	test.assert(~formatterHit, "formatter was hit.");

	~actionHit = false;
	Log(\unittestlog).actions.add({
		~actionHit = true;
	});
	Log(\unittestlog).info(logString);
	test.assert(~actionHit, "action func was hit.");

	~actionHit = false;
	Log(\unittestlog).actions.clear();
	Log(\unittestlog).info(logString);
	test.assert(~actionHit.not, "action func NOT was hit after clear.");

	Log(\unittestlog).level = \warning;
	Log(\unittestlog).warning(logString);
	Log(\unittestlog).info(logString);
	test.assert(Log(\unittestlog).lines.last.level != \info, "Log level warning posts \warning but not \info events.");


}
