//0
(
"DEMO Score";
//the score must be named q.scd and located in the same directory of the class
//it must contain valid SC codes
//in addition to the queue numbers following the slashes //
~dur = 60/40;
~scale = Scale.major;
~amp = -6.dbamp;
~ampSines = ~amp * 0.7;
)
//1
(
"sine tone";
a = Synth(\default, [fade:~dur * 2, freq:~scale.d2f(1), amp:~ampSines/6]);
NodeWatcher.register(a, true);
fork { (~dur * 2.1).wait; "test".postln; a.release(~dur * 4); };
)
//2
(
if (a.isPlaying, { a.release(0.1); });
b = List[];
[-1,0,1,2,3,4,5,6].do {|item|
	b.add(
		Synth(\default, [
		fade: ~dur * 10.5, 
		freq: ~scale.d2f(item), 
		amp: ~ampSines/8
		])
	);
};
)
//3
nil
//4
b.do(_.release(0.05));
//5