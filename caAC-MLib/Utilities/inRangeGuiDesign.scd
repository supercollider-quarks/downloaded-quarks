
w = Window();
w.acceptsMouseOver_(true).front;
b = CompositeView(w, Rect(50,150,300,50));
b.background = Color.red;
b.mouseOverAction_({
	b.background = Color.red;

});

w.view.mouseMoveAction_({ |object,x,y|
	var range = b.bounds.asArray;
	var xRange = x > range[0] && (x < (range[0] + range[2]));

	var yRange = postln(y > range[1]) && (y < (range[1] + range[3]));
	if (xRange && yRange) { b.background = Color.red; } { b.background = Color.clear };


});

b.mouseEnterAction_({
	b.background = Color.red;

});

b.mouseLeaveAction_({
	b.background = Color.clear;

})

BF(\bufBank);

// Wanneer een buffer is opgenomen informeer de buffer classes zodat deze kan worden upgedate! 