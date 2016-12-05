// basic example on how to swap one action from one control element to another.

k = MKtl(\nk2, "*nanokontrol2");
k.gui;

(
// function that should be linked to some element
var setFunc = {|el ... args|
	"(%) % [%].\n".postf(el.parent.elemIndexOf(el), el, el.value.round(0.001))
};
var activeElement;

// how switching works :
var switchTo = {|newActive|
    "switch control: % >> %\n".postf(activeElement, newActive);
	activeElement !? { activeElement.action = nil };
    newActive.action = setFunc;
    activeElement = newActive;
};

// the first two sliders are the two elements between which the function is switched
var controlElements = k.elAt(\sl, [0, 1]);

// bt 0 0 (bt_1_S) : press to switch
k.elAt(\bt, 0, 0).action = {|el|
    (el.value == 1).if({
		switchTo.(controlElements[0]);
    },{
        switchTo.(controlElements[1]);
    })
};
// initialize activeElement
k.elAt(\bt, 0, 0).doAction;
)