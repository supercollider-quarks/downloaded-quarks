// UnitTestScript("WindowViewRecall", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var win, view;

	WindowViewRecall.resetWindowPositions();

	win = Window("test", bounds:Rect(500, 500, 123, 456)).front;
	win.autoRememberPosition(\windowviewrecall_unit_test_window);
	win.bounds = win.bounds.moveTo(100, 100);
	2.wait;
	win.close();

	win = Window("test", bounds:300@300).front;
	win.autoRememberPosition(\windowviewrecall_unit_test_window);

	test.assertEquals(win.bounds, Rect(100, 100, 123, 456));
	win.close();


	view = View(bounds:Rect(500, 500, 123, 456)).front;
	view.autoRememberPosition(\windowviewrecall_unit_test_window);
	view.bounds = view.bounds.moveTo(100, 100);
	2.wait;
	view.close();

	view = View(bounds:300@300).front;
	view.autoRememberPosition(\windowviewrecall_unit_test_window);

	test.assertEquals(view.bounds, Rect(100, 100, 123, 456));
	view.close();

}
