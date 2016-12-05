// UnitTestScript("Singleton", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var object, oldObject;

	Singleton.know = true;

	object = Singleton(\one);
	test.assertEquals(object, Singleton(\one), "Constructor fetches correct object.");
	test.assertEquals(object, Singleton.one, "Dot notation fetches correct object.");
	test.assert(Singleton.all[Singleton].includes(Singleton(\one)), "Internal dictionary has correct object.");

	test.assert(Singleton(\one) !== Singleton(\two), "Different singletons are different.");

	oldObject = Singleton(\one);
	Singleton(\one).clear;
	test.assert(Singleton(\one) !== oldObject, "Clearing results in new object.");

	object = Singleton.dotnotation;
	test.assertEquals(object, Singleton(\dotnotation), "Dot notation can construct an object.");
}
