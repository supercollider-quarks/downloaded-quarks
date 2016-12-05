// UnitTestScript("ServerView", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var win;

	ServerView(\ServerViewUnitTest).front;
	0.1.wait;
	test.assert(ServerView(\ServerViewUnitTest).window.notNil, "Made a window.");
	test.assert(ServerView(\ServerViewUnitTest).window.visible, "Window is visible.");

	win = ServerView(\ServerViewUnitTest).window;
	ServerView(\ServerViewUnitTest).close;
	0.1.wait;
	test.assert(win.isClosed, "Window not visible after close.");
	test.assert(ServerView(\ServerViewUnitTest).window.isNil, "Window is nil'd'.");
	test.assert(ServerView(\ServerViewUnitTest).view.isNil, "View is nil'd'.");

}

