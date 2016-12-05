
a = BufferPool();
a.makeGui;
a.closeGui;
a.bufData;
a.bufData;


"path".pathExists; // if path exits continue the addition procedure of a buffer to the bufferpool


p = BPdata();
p.bufferpool

(
w = Window.new("", Rect(200, 800, 604, 500));
w.background = Color.grey(0.2, 0.9);
w.front;

a = BPBankView();
a.makeGui(w, Rect(5, 5, 180, 300)) ;
a.addAction = { |...args|
	var soundfileView = BPSoundfileView();
	soundfileView.makeGui(w, Rect(200, 5, 400, 300));

};
a.selectAction = { |...args| args.postln;
	~currentBankName;
};
a.removeAction = { |...args| args.postln; };
a.selectAction = { |...args| args.postln; };

t = BPTransporter();
t.makeGui(w, Rect(5, 420, 350, 60));
t.editAction = { |buttonValue|
	if (buttonValue > 0) {
		a do: (_.edit(true));
		// k do: (_.edit(true));
	} {
		a do: (_.edit(false));
		// k do: (_.edit(false));
	}
};

~soundfileBanks = IdentityDictionary();
)





(
w = Window.new("", Rect(200,800,604,500)).front;
w.background = Color.grey(0.2, 0.9);

// Bank Selector object!
h = ScrollView(w, Rect(5, 5, 180, 300));
h.background = Color.grey;

a = { |i|

	var temp = BPDragAndDropElement(i, "bank", "bank" ++ i);
	temp.makeGui(h, Rect(0,35 * i,175,30));

	temp.beginDragAction = { |what, index, string, extra|
		~startDragArray = [what, index, string, extra];
	};

	temp.endDragAction = { |what, index, string, extra|
		var tempObject;
		var currentDrag = View.currentDrag;

		case { currentDrag.isString } {
			"Append Soundfile to this bank: %\n".postf(temp.string);
		} { currentDrag.isArray } {
			if (currentDrag.at(0) == "bank") {

				a[~startDragArray[1].postln].string = a[temp.index.postln].string.copy;
				a[temp.index].string = ~startDragArray[2];
			};
			if (~startDragArray[3] != temp.string) {
				"Append soundfile to %\n".postf(temp.string);
				"Peform remove action from previous arrays".postln;
				"Update GUI".postln;
			};
		} { currentDrag.value == \add } {
			"Perform add action".postln
		};
	};
	temp;
} ! 4;


// One Bank Object
j = ScrollView(w, Rect(200, 5, 400, 300));
j.background = Color.grey;
j.visible = true;
k = { |x|
	var currentBank = "bank1".postln;
	var temp = BPDragAndDropElement(x, "soundfile", "soundfile" ++ x);
	temp.makeGui(j, Rect(3,35 * x,j.bounds.width - 10,30));

	temp.beginDragAction = { |what, index, string, extra|
		~startDragArray = [what, index, string, currentBank];
	};

	temp.endDragAction = { |what, index, string, extra|
		var tempObject;
		var currentDrag = View.currentDrag;

		case { currentDrag.isString } {
			"Append Soundfile after this index: % in %\n".postf(temp.index, currentBank);
			"Peform add Buffer action".postln;
		} { currentDrag.isArray } {
			if (currentDrag.at(0) == "soundfile") {

				k[~startDragArray[1].postln].string = k[temp.index.postln].string.copy;
				k[temp.index].string = ~startDragArray[2];

				"Swap buffer array action".postln;
				[\bankSlot, \index1, \index2].postln;
			};

		} { currentDrag.value == \add } {
			"Perform ADD action".postln;
		};

	};
	temp;


} ! 8;

// Operator Object
l = CompositeView(w, Rect(5, 420, 350, 60));
l.background = Color.grey;
// Verander dit naar static text!!
m = DragBoth(l,Rect(5, 5, 50, 50)).align_(\10)
.background_(Color.yellow(0.8))
.object_(\trash)
.align_(\center)
.stringColor_(Color.black)
.receiveDragHandler = { "remove this object.postln".postln; View.currentDrag.postln };
n = Button(l, Rect(60, 5, 50, 50))
.states_([
	["edit", Color.yellow, Color.black],
	["edit off", Color.yellow, Color.blue]
])
.action_({
	if (n.value > 0) {
		a do: (_.edit(true));
		k do: (_.edit(true));
	} {
		a do: (_.edit(false));
		k do: (_.edit(false));
	}
});

o = DragBoth(l,Rect(115, 5, 50, 50)).align_(\10)
.background_(Color.black.alpha_(0.99))
.object_(\add)
.align_(\center)
.stringColor_(Color.yellow);

p = Button(l, Rect(170, 5, 50, 50))
.states_([
	["REC", Color.red, Color.black],
	["REC", Color.black, Color.red]
])
.action_({ "record audio".postln; "getCurrentSelectBankName".postln; "append buffer to currentbank".postln; "update gui".postln; });

q = Button(l, Rect(225, 5, 50, 50))
.states_([
	["WAV", Color.yellow, Color.black],
	["WAV", Color.yellow, Color.blue]
]);

r = Button(l, Rect(280, 5, 50, 50))
.states_([
	["PLAY", Color.yellow, Color.black],
	["PLAY", Color.black, Color.yellow]
])
)






a = Prand(BPdata(0).list, inf).asStream;
Prand((0,1..9),inf).asStream.nextN(10)

