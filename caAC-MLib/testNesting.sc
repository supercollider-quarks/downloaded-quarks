


SecondObject {
	var object;

	*new{ |argObject| ^super.new.init(argObject) }

	init {|argObject|
		object = argObject;
	}

	postValueFromOtherObject {
		object.value.postln;
	}
}

FirstObject {
	classvar <>value;

	*new { value = 100; }
}


/*
b = FirstObject()
b.value = 1000;

c = SecondObject(b);
c.postValueFromOtherObject;
*/