


a = MindReadingMachine.new;

z = a <> Pfunc { [1, 0].choose }.asStream;

z.next;

(
a = MindReadingMachine.new.verbose_(false);
b = MindReadingMachine.new.verbose_(false);
c = 0;
d = 0;
)

(
fork {
	loop {
		y = a.next(x ? [0, 1].choose);
		if(x == y) { c = c + 1 };
		x = b.next(y);
		x.postln;
		if(x == y) { d = d + 1 };
		y.postln;
		0.2.wait;
		x = y;
	}
}
)

// sonified
(
fork {
	loop {
		y = a.next(x ? [0, 1].choose);
		if(x == y) { c = c + 1 };
		x = b.next(y);
		x.postln;
		if(x == 1) { (note: 6, sustain: 0.1).play };
		if(x == y) { d = d + 1 };
		y.postln;
		if(y == 1) { (note: 17, sustain: 0.1).play };
		0.2.wait;
		x = y;
	}
}
)

(
fork {
	loop {
		y = a.next(x ? [0, 1].choose);
		if(x == y) { c = c + 1; (note: 0, out: 0, sustain: 0.1, ctranspose:c-d).play };
		x = b.next(y);
		x.postln;
		0.2.wait;
		if(x == y) { d = d + 1; (note: 0, out: 1, sustain: 0.1, ctranspose:d-c).play  };
		y.postln;
		0.2.wait;
		x = y;
	}
}
)

y = 0;



a.verbose = true;
b.verbose = true;

a.verbose = false;
b.verbose = false;

c
d

a.prose;
b.prose;




a.prose;
b.prose;


z = a <> b <> Pfunc { [1, 0].choose }.asStream;

z.next;

a.next(0);
a.table;
a.stack;

a;


a.oppositeValue(1)
a.oppositeValue(0)
a.oppositeValue(1.2)

a.playToken(1, 0);
a.playToken(1, 1);

a.resultToken(1, 0);
a.resultToken(1, 1);

a.findPlayerStrategy([\wins, \same, \wins])
a.table
a.next(1);


a.class;
a.inspect;

a.value;



a = Pseq([Pfuncn({ |event| event.copy.put(\val, 1.0.rand) }, 2), Pfuncn({ |event| event.copy.put(\val, event[\max].rand) }, 2)], inf);

x = a.asStream;

x.next((max: 100, zz: 8));

a = Pmindread(Pseq([1, 0, 1, 1, 0, 1], inf));
x = a.asStream;

x.nextN(6);

Pdefn(\mind, Pseq([1, 0, 1, 1, 0, 1], inf));

(
Pdef(\x,
	Pbind(
		\amp, Pmindread(Pdefn(\mind)) * 0.1,
		\note, -1,
		\dur, 0.2
	)
).play
)
(
Pdef(\y,
	Pbind(
		\amp, Pmindread(Pdefn(\mind)) * 0.1,
		\note, 7,
		\dur, 0.2
	)
).play
)


Pdefn(\mind, Pseq([1, 0], inf))
Pdefn(\mind, Pseq([0, 1, 1, 0], inf))
Pdefn(\mind, Pseq([0, 1, 0, 1, 0, 1, 0, 1, Prand([1, 0], 1)], inf))
Pdefn(\mind, Pseq([1, 0, 1, 1, 1, 1, 0], inf))
Pdefn(\mind, Pmindread(Pseq([1, 0, 1, 1, 0], inf)))



(
SynthDef(\default, { arg out=0, freq=440, amp=0.1, pan=0, gate=1;
	var z;
	z = RLPF.ar(
		Mix.new(Pulse.ar(freq + [0, Rand(-0.4,0.0), Rand(0.0,0.4)] / 4, 0.3, 0.3)),
		XLine.kr(Rand(4000, 500), Rand(2500, 200), 1),
		0.02
	) * Linen.kr(gate, 0.01, 0.7, 0.03, 2);
	OffsetOut.ar(out, Pan2.ar(z, pan, amp));
}, [\ir]).add;
)


Event.makeDefaultSynthDef;




Pdefn(\mind, Pmindread(Pseq([1, 0, 1, 1, 0], inf), Pseq([0, 1], inf)))
Pdefn(\mind, Pmindread(Pseq([1, 0, 1, 1, 0], inf), 1, 3))
x = Pdefn(\mind).asStream;
x.next(1);
Pdefn(\mind).source.




