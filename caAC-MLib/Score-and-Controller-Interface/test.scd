
(
l = MLemurGui();
l.connect("192.10.1.2",8002,8000);
l.resetAll;
m = ScoreMixer(l);
m.makeGui;
)

// Script field header aanpassen naar de parameter te veranderen.



s.boot;

SynthDef(\basicFM, {|out = 0, gate = 1, amp = 1, carFreq = 1000, modFreq = 100, modAmount = 2000, clipAmount = 0.1, pan = 0, atk = 0.02, rel = 1|
	var mod = SinOsc.ar(modFreq) * modAmount;
	var car = SinOsc.ar(carFreq + mod);
	var ampEnv = EnvGen.ar(Env.adsr(atk, 0, 0.7, rel, peakLevel: amp), gate, doneAction: 2);
	Out.ar(out, Pan2.ar(car * ampEnv,pan));
}).add;




s.reboot;

a.release(1); a = { Splay.ar(SinOsc.ar(postln({exprand(30,80)}!3)),0,1).clip(-0.2,0.2) * 0.2 }.play
a.free;

e.release(1); e = { Splay.ar(SinOsc.ar({exprand(13000,18000)}!8),0,1).clip(-0.2,0.2) * LFNoise1.kr({1/7}!8,0.2,0.02).sqrt  }.play
e.free;

s.boot;
s.record


{ { var freq = midiratio(round(rrand(20,110.0),0.5)).postln; RLPF.ar(VarSaw.ar(freq, SinOsc.ar(1/3)),freq * SinOsc.ar(1/rrand(3,7)).rangeExp(0.24,4), 0.34,0.1)!2 }.play; } ! 5;

s.boot;


(
b.free;
b = {
	Splay.ar(
		Mix.fill(8,
			{
				VarSaw.ar(
					midicps(rrand(30,80.0).round(0.25)), SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,687.2),0,0.1),
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! 5)
				* { LFNoise2.kr(1/3,1.5) }!5
				% { LFNoise2.kr(1/5) }!5
		})
	) * 0.02
}.play
);
b.free;


(
b.free; b = {
	Splay.ar(
		Mix.fill(8,
			{
				VarSaw.ar(
					midicps(rrand(30,80.0).round(0.25)), SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,687.2),0,0.1),
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! 5)
				* { LFNoise2.kr(1/3,2) }!5
				% { LFNoise2.kr(1/5, LFNoise2.kr(1/8)) }!5
		})
	) * 0.1
}.play
);

(
b.free; b = {
	Splay.ar(
		Mix.fill(8,
			{
				VarSaw.ar(
					midicps(rrand(30,80.0).round(0.25) + LFNoise1.kr(1/11, 4)), SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,687.2),0,0.1),
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! 5)
				* { LFNoise2.kr(1/3,2) }!5
				% { LFNoise2.kr(1/5, LFNoise2.kr(1/8)) }!5
		})
	) * 0.1
}.play
);

(
b = {
	var n = 5;
	Splay.ar(
		Mix.fill(n,
			{
				VarSaw.ar(
					midicps(rrand(30,110.0).round(0.25) + LFNoise1.kr(1/13, 6)), SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,1),0,0.5),
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! n)
				* { LFNoise2.kr(1/3) } ! n
				* { LFNoise2.kr(1/4, LFNoise2.kr(1/13)) }!n
		})
	) * 0.2
}.play
);

(
b = {
	var n = 3;
	Splay.ar(
		Mix.fill(n,
			{
				VarSaw.ar(
					midicps(rrand(30,80.0).round(0.25)), { SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,687.2),0,0.5) } ! n,
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! n)
				* { LFNoise2.kr(1/3,2) }!n
				% { LFNoise2.kr(1/5, LFNoise2.kr(1/8)) }!n
		})
	) * 0.1
}.play
);

(
c.free; c = {
	var sig = In.ar(0, 2) + DelayC.ar(LocalIn.ar(2), 2, { LFNoise2.kr(1/11).range(0,2.0) } ! 2);
	var chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.
		// A simple reverb
		chain = PitchShift.ar(LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,350, 1), 0.2, {0.19.rand+0.01}!2, 3)), 0.1, LFNoise1.ar(1/5).rangeExp(0.25,4));

	};
	sig = Limiter.ar(sig+chain);    // dry + wet
	sig = RLPF.ar(sig, 13500, 0.7);
	sig = LeakDC.ar(sig);
	LocalOut.ar(sig * 0.8);
	Out.ar(0, sig.softclip)
}.play(s, addAction: \addToTail)
);

(
b.free; b = {
	Splay.ar(
		Mix.fill(2,
			{
				VarSaw.ar(
					midicps(rrand(20,110).round(0.25)), SinOsc.ar(LFNoise2.kr(1/11).rangeExp(0.34,687.2),0,0.1),
					{ LFNoise2.kr(1/3).rangeExp(0.01,0.5) } ! 5)
				* { LFNoise2.kr(1/3,1.5) }!5
				% { LFNoise2.kr(1/5) }!5
		})
	) * 0.01
}.play(s, addAction: \addToHead)
);

b.free;
s.record

(
c = {

	var sig = In.ar(0, 2) + DelayC.ar(LocalIn.ar(2), 2, { LFNoise2.kr(1/11).range(0,2.0) } ! 2) + HPF.ar(EnvGen.kr(Env([1,0],0.2,-4))*WhiteNoise.ar(0.001),4000,12).distort;
	var chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.
		// A simple reverb
		chain = PitchShift.ar(LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,13500, 1), 0.2, {0.19.rand+0.01}!2, 3)), 0.1, LFNoise1.ar(1/5).rangeExp(0.25,4));

	};
	sig = Limiter.ar(sig+chain);    // dry + wet
	sig = RLPF.ar(sig, LFNoise1.ar(1/3).rangeExp(2000,13500),0.8);
	sig = LeakDC.ar(sig);
	LocalOut.ar(sig * 0.7);
	Out.ar(0, sig.softclip * 0.4)
}.play(s, addAction: \addToTail)
);


c.release(8)
c.free
// make een synthese model van dit!!

a.release(4); a = { Splay.ar(SinOscFB.ar((100,200..400), 2, Saw.ar(10,0.5,0.5) )) * 0.1  }.play

a.release(0.01); a = { Splay.ar(SinOscFB.ar((100,200..400), 2, Saw.ar((1,3..7),0.5,-0.5) )) * 0.1  }.play
a.release(0.01); a = { Splay.ar(SinOscFB.ar((100,200..400), 2, Saw.ar((1,3..7).reciprocal,0.5,-0.5) )) * 0.1  }.play

a.release(0.01); a = { Splay.ar(SinOscFB.ar((100,200..400).scramble, 2, Saw.ar((1,3..7).scramble,0.5,0.5) )) * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((100,200..400), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7),0.5,0.5) )) * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((100,500..2900), 2, Saw.ar(scramble((1,2..8)*3),0.5,0.5))) * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((100,200..400), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7),0.5,0.5) )) * Decay2.ar(Impulse.ar({exprand(0.1,2)}!5),0.004,3).sqrt * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((100,200..3500), LFNoise1.kr(1/11,0.5,0.5), Saw.ar((0.1,0.3..2),0.5,0.5) )) * Decay2.ar(Impulse.ar({exprand(0.1,2)}!5),0.004,3).sqrt * 0.1  }.play

a.release(7); a = { Splay.ar(Saw.ar((1,5..29)*midicps(1), Saw.ar(scramble((1,2..8)*3),0.5,0.5))) * 0.1  }.play

a.release(0.2); a = { Splay.ar(SinOscFB.ar((1,2..4)*midicps(rrand(30,50)), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7)*midiratio(rrand(-18,18)),0.5,0.5) )) * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((100,200..400), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7),0.5,0.5) )) * Decay2.ar(Impulse.ar({exprand(0.34,2)}!5),0.004,3).sqrt * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((1,2..4) * midicps(rrand(20,80)), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7),0.5,0.5) )) * Decay2.ar(Impulse.ar({exprand(0.34,2)}!5),0.004,3).sqrt * 0.1  }.play

a.release(7); a = { Splay.ar(SinOscFB.ar((1,2..8) * midicps(rrand(20,80)), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7)*midiratio(24),0.5,0.5) )) * Decay2.ar(Impulse.ar({exprand(3,11)}!5),0.004,0.2).sqrt * 0.1  }.play



(
a = {
	EnvGen.kr(Env([0,1,0],[34,13],[3,7]),doneAction: 2) *
	Splay.ar(SinOscFB.ar((1,2..8) * midicps(rrand(20,20)), { LFNoise1.kr(exprand(0.64,13)).range(0,4) } ! 5, Saw.ar((1,3..7).scramble*midiratio(exprand(0.125,8)),0.5,0.5) )) * Decay2.ar(Impulse.ar(sort({exprand(0.34,18.2)}!5)),0.004,exprand(0.1,4)).sqrt * 0.1  }.play
)
(
fork { loop {
	{ EnvGen.kr(Env([0,1,0],[3,8],[3,7]),doneAction: 2) *
		Splay.ar(SinOscFB.ar((1,2..4)*midicps(rrand(30,50)), LFNoise1.kr(1/5,1,1), Saw.ar((1,3..7)*midiratio(rrand(-18,18)),0.5,0.5))) * 0.1  }.play;
	exprand(0.1,13).wait;
}
}.play
)



a.release(0.02); a = { Splay.ar(SinOscFB.ar((1,2..4), 2, Saw.ar(10,0.5,0.5) )) * 0.1  }.play

a.release(0.02); a = { Splay.ar(SinOscFB.ar((1,2..4) * midicps(60 + choose(Scale.minor.semitones - 12 ++ Scale.minor.semitones)), 2, Saw.ar((1,3..7),0.5,0.5) )) * 0.1  }.play




{ SinOsc.ar(100) }.plot(0.01)
s.scope

s.reboot;

(
l = MLemurGui();
l.connect("192.10.1.2",8002,8000);
l.resetAll;
m = ScoreMixer(l);
m.makeGui;
)

m.makeGui;



s.reboot;
// add parallelization!!

b = m.scores.first.eventStream;

c = Ppar({b}!6)

e = c.play;

b = Ppar({m.scores.first.eventStream}!5).trace.play
b.stop


EZNumber()


e.free;
s.boot;

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/49798__davehammond__spoonslurps-2.wav");
a.play;

b = Buffer.loadCollection(s, Env.sine.discretize(1024));
b.play;

{ SinOsc.ar(43.06640625) }.play

44100 / 1024

	.plot


b.stop

s.reboot;

b = m.scores.first.setValueFunction[\envirTextField].value("~a = 1")



Environment.make( { ~a = 1; ~b = 2 })[\a]


// How to rebuild a closed window
s.boot;

a = Buffer.readDir(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/")

h = Buffer.read(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/95964__gunnbladez__100-dertill-zynth-03.wav");
(
f = fork {
	inf do: {
		{ PlayBuf.ar(2, h, midiratio(6 * rand(5,21)), startPos: h.numFrames.rand, doneAction: 2) * 0.4
		}.play(s, addAction: \addToHead);
		exprand(0.1,5).wait;
	}
}
)
f.stop
s.boot;
play{a=15;b=15;c=100;x=In.ar([0,1]);y=x;a.do{|i|y=y+(CombL.ar(x,3,(i+1)/b,3)/(a-i))};y*SinOsc.ar(c)} //FX For BassGuitar #Supercollider


b = "/Users/MarinusKlaassen/Desktop/SOUNDS/".getPathsInDirectory collect: { |path| Buffer.read(s, path) };
b.choose.play

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/119491__ingudios__sheet-metal-7.wav",  channels: 0);

s.boot;
a.play
a.bufnum
(
~lm = MLemurGui.new;
~lm.connect("192.10.1.2",8002,8000);
~lm.resetAll;
m = ScoreMixer.new(~lm);
m.makeGui;
)
b = m.scores.first.eventStream.trace.play
b.stop

(
SynthDef(\test, { |freq = 4000, vibrato = 34, vibAmount = 8000, amp = 0.1, pan = 0, gate = 1, out, dur = 4|
	var env = EnvGen.kr(Env.perc(0.003,dur - 0.003, 1, -4), gate, doneAction: 2);
	var mod = SinOsc.ar(vibrato, mul: vibAmount).softclip * vibAmount;
	var sig = SinOsc.ar(freq + mod).softclip;
	Out.ar(out, Pan2.ar(sig, pan) * 0.1 * env * amp)
}).add;
)
s.quit
s.reboot;

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/77168__7by7__spoons-along-radiator.aif", channels: 0)

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/25737__freqman__metal-pipe-1.wav", channels: 0);

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/15806__melack__esquell-loop2.wav", channels: 0);

a = Buffer.readChannel(s, "/Users/MarinusKlaassen/Desktop/SOUNDS/198396__ani-music__ani-spoons-clicked-4a.wav", channels: 0)
a.play
b = Buffer.loadCollection(s, Env.perc(0.1,1).discretize(2048));
b = Buffer.loadCollection(s, Env.sine.discretize(2048));
b.plot
b = Buffer.loadCollection(s, Env.perc(0.2,1,curve: -4).discretize(2048))

b = Buffer.loadCollection(s, Env.perc(0.1,1).discretize(2048));

(
SynthDef(\Grain, {
	var n = 5, chain;

	var fd = LocalIn.ar(2);
	var rate = MouseY.kr(3,4000, 1);
	var trigger = { Dust.ar(rate) } ! n;
	var sig = sum({ |i|
		GrainBuf.ar(2, trigger[i], rate.first.reciprocal, a, midiratio(i*6), MouseX.kr(0,1.0) + LFNoise1.kr(1/3, 0.05) % 1, envbufnum: b, pan: Latch.ar(LFNoise1.ar(0.4),trigger))

	} ! n).distort;

	sig = Splay.ar(sig);
	chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.
		//chain = PitchShift.ar(chain, 0.2, LFNoise1.ar(0.3).rangeExp(0.25,4));
		// A simple reverb
		chain = LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,50, 1), 0.2, {0.19.rand+0.01}!2, 3));

	};

	sig = Limiter.ar(sig+chain);    // dry + wet
	sig = RLPF.ar(sig, 13500, 0.34);
	LocalOut.ar(sig);
	Out.ar(0, sig.softclip)
}).play
)
e.free;

(
e = SynthDef(\Grain, {
	var n = 5, chain;

	var fd = LocalIn.ar(2);
	var rate = MouseY.kr(3,13000, 1);
	var trigger = { Dust.ar(rate) } ! n;
	var sig = sum({ |i|
		GrainBuf.ar(2, trigger[i], rate.first.reciprocal, a, midiratio(i*6-18), MouseX.kr(0,1.0) + LFNoise1.kr(1/3, 0.05) % 1, envbufnum: b, pan: Latch.ar(LFNoise1.ar(0.4),trigger))

	} ! n).distort;

	sig = Splay.ar(sig);
	chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.

		chain = PitchShift.ar(LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,13500, 0.84), 0.2, {0.19.rand+0.01}!2, 3)), 0.1,LFNoise1.kr(1/2).rangeExp(0.25,4), 0.1, 0.1) + chain;

	};

	sig = Limiter.ar(sig+(dbamp(-18) * chain));    // dry + wet
	sig = RLPF.ar(sig, 13000, 0.34);
	LocalOut.ar(sig);
	Out.ar(0, sig.softclip)
}).play
)
s.boot;

(
e = SynthDef(\Grain, {
	var n = 6, chain;

	var fd = LocalIn.ar(2);
	var rate = MouseY.kr(3,13000, 1);
	var trigger = { Dust.ar(rate) } ! n;
	var sig = sum({ |i|
		GrainBuf.ar(2, trigger[i], rate.first.reciprocal, a, midiratio(i*6), MouseX.kr(0,1.0) + LFNoise1.kr(1/3, 0.05) % 1, envbufnum: b, pan: Latch.ar(LFNoise1.ar(0.4),trigger))

	} ! n).distort;

	sig = Splay.ar(sig);
	chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.

		chain = PitchShift.ar(LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,13500, 0.84), 0.2, {0.19.rand+0.01}!2, 3)), 0.1,LFNoise1.kr(1/2).rangeExp(0.25,4), 0.1, 0.1) + chain;

	};

	sig = Limiter.ar(sig+(dbamp(-18) * chain));    // dry + wet
	sig = RLPF.ar(sig, 13000, 0.34);
	LocalOut.ar(sig);
	Out.ar(0, sig.softclip)
}).play
)

a.plot
e.free;


(
{
	var env, speed = MouseX.kr(0.34,130, 1, 0.02);
	env = LFPar.ar([0.5,0.8] * speed.reciprocal,add:1);
	Mix.new([Saw.ar(speed)*env,Saw.ar(3*speed)*env]).sin*Saw.ar(7*speed)*0.5;
}.play;
)



({ |f| 1 + f * 3 } ! 8)
(
SynthDef(\Grain, {
	var n = 3, chain;

	var fd = LocalIn.ar(2);
	var rate = { |i| MouseY.kr(3,634, 1)  / (1 + i * midiratio(2)) } ! n;
	var trigger = Impulse.ar(rate);
	var sig = sum({ |i|
		GrainBuf.ar(2, trigger[i], rate.first.reciprocal, a, midiratio(i*3), MouseX.kr(0,1.0) + LFNoise1.kr(1/3, 0.05) % 1, envbufnum: b, pan: Latch.ar(LFNoise1.ar(0.4),trigger))

	} ! n).distort;

	sig = Splay.ar(sig);
	chain = sig;    // Start with the original signal
	8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.
		//chain = PitchShift.ar(chain, 0.2, LFNoise1.ar(0.3).rangeExp(0.25,4));
		// A simple reverb
		chain = LeakDC.ar(AllpassL.ar(RLPF.ar(chain*0.9,350, 1), 0.2, {0.19.rand+0.01}!2, 3));

	};

	sig = Limiter.ar(sig+chain);    // dry + wet
	sig = RLPF.ar(sig, 3500 * 2, 0.34);
	LocalOut.ar(sig);
	Out.ar(0, sig.softclip)
}).play
)
s.scope
// (1,3..9)
s.boot;
()
(
var winenv;

// b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");
// a custom envelope
winenv = Env([0, 1, 0], [0.5, 0.5], [8, -8]);
z = Buffer.sendCollection(s, winenv.discretize, 1);

SynthDef(\buf_grain_test, {arg gate = 1, amp = 1, sndbuf, envbuf;
    var pan, env, freqdev;
    // use mouse x to control panning
    pan = MouseX.kr(-1, 1);
    env = EnvGen.kr(
        Env([0, 1, 0], [1, 1], \sin, 1),
        gate,
        levelScale: amp,
        doneAction: 2);
    Out.ar(0,
        GrainBuf.ar(1, Impulse.kr(10), 0.1, sndbuf, LFNoise1.kr.range(0.5, 2),
            LFNoise2.kr(0.1).range(0, 1), 2, pan, envbuf) * env)
    }).send(s);

)

// use built-in env
x = Synth(\buf_grain_test, [\sndbuf, b, \envbuf, -1])

// switch to the custom env
x.set(\envbuf, z)
x.set(\envbuf, -1);

x.set(\gate, 0);

b = { Trig.ar(PlayBuf.ar(2, a, 1, loop: 1, rate: MouseX.kr(1,100))) }.play


b = { PlayBuf.ar(2, a, 1, loop: 1, rate: MouseX.kr(1,100)) * 0.1 }.play

(
// mixing two delays together
// you hear a phasing sound but the sound is still flat.
{ var x; x = { BrownNoise.ar(0.2) } !2;
    x = Mix.ar([DelayC.ar(x,0.01,0.01),DelayC.ar(x,0.02,MouseX.kr(0,0.02))]);
    x * 0.1
}.play;
)



(
play {
    var sig, chain;

    // The original sound source
    // sig = sum({ SinOsc.ar(rrand(50,6000),0,2*Decay.ar(Dust2.ar(1),0.1)).tanh } ! 7);
	sig =	Trig.ar(PlayBuf.ar(1, a, 1, loop: 1, rate: MouseX.kr(1,100))) * Impulse.ar(MouseX.kr(34,83));
	sig =  sig + PlayBuf.ar(2, a, 1, loop: 1, rate: MouseX.kr(1,100)) * 0.1;
	sig = LeakDC.ar(RHPF.ar(sig, MouseY.kr(40,13000), 0.34)) * VarSaw.ar(MouseY.kr(40,13000,1.4)) * 2;
	sig = distort(12*sig);
	chain = sig;    // Start with the original signal
    8.do {|i|       // Loop 8 times. For each loop, connect the signal through something.

        // A simple reverb
        chain = LeakDC.ar(AllpassL.ar(LPF.ar(chain*0.9,3000), 0.2, {0.19.rand+0.01}!2, 3));
    };

    Limiter.ar(sig+chain);    // dry + wet
}
)

{ var alpha,beta; alpha = rrand(99,399); beta = alpha = rrand(99,399);Pan2.ar(LeakDC.ar(Normalizer.ar(BPeakEQ.ar(BPeakEQ.ar(BPeakEQ.ar(BPeakEQ.ar(
FreeVerb.ar(Formant.ar(TRand.kr(alpha,alpha+(rrand(5,20)),Impulse.kr(rrand(10,100))), TRand.kr(beta,beta+(rrand(5,20)),Impulse.kr(rrand(10,100))), EnvGen.kr(Env.adsr,Impulse.kr(rrand(100,666)))*rrand(666,3333),0.125),
rrand(0.5,1.0),rrand(0.5,1.0),rrand(0.5,1.0))
,100,6.0,12),1000,6.0,0),5000,6.0,0),10000,6.0,12),0.6,0.1)),0)
}.play;

s.boot;

(
SynthDef("lazerdrone", {
	var in, freq, hasFreq;


	in = SinOsc.ar(440 * LFSaw.kr(100, 1000));

	# freq, hasFreq = Pitch.kr(
					in,
					ampThreshold: 0.09,
					median: 1);
	freq = freq * hasFreq;
	//freq.poll;

	SinOsc.kr(freq).poll;

		Out.ar(0, in +
		SinOsc.ar(440,
			0,
			Clip.kr(
				SinOsc.kr(freq),
				0)
		)
	);
}).play;
)
(
{ var x;
    x = Klank.ar(`[[200, 671, 1153, 1723], nil, [1, 1, 1, 1]], PinkNoise.ar(7e-3));
    [x,x]
}.scope(2))
// not correlated
{ Klank.ar(`[[200, 671, 1153, 1723], nil, [1, 1, 1, 1]], PinkNoise.ar([7e-3,7e-3])) }.scope(2);

// two waves mixed together coming out both speakers
{ var x; x = Mix.ar(VarSaw.ar([100,101], 0, 0.1, 0.2)); [x,x] }.scope(2);
// two waves coming out each speaker independantly
{ VarSaw.ar([100,101], 0, 0.1, 0.2 * 1.414) }.scope(2); // * 1.414 to compensate for power

// delays as cues to direction
// mono
{ var x; x = LFTri.ar(1000,0,Decay2.ar(Impulse.ar(4,0,0.2),0.004,0.2)); [x,x]}.scope(2);

(
// inter-speaker delays
{ var x; x = LFTri.ar(8000,0,Decay2.ar(Impulse.ar(8,0,0.2),0.004,0.2));
    [DelayC.ar(x,0.01,0.01),DelayC.ar(x,0.02,MouseX.kr(0.02, 0))]
}.scope(2);







(
fork{
	SynthDef(\basicFM, {|out = 0, gate = 1, amp = 1, carFreq = 1000, modFreq = 100, modAmount = 2000, clipAmount = 0.1|
		var modEnv = EnvGen.ar(Env.adsr(0.5, 0.5, 0.7, 0.1, peakLevel: modAmount), gate);
		var mod = SinOsc.ar(modFreq) * modEnv;
		var car = SinOsc.ar(carFreq + mod);
		var ampEnv = EnvGen.ar(Env.adsr(0.1, 0.3, 0.7, 0.2, peakLevel: amp), gate, doneAction: 2);
		var clip = clipAmount * 500;
		Out.ar(out, (car * ampEnv * clip).clip(-0.7, 0.7) * 0.1);
	}).add;

	s.sync;

	Pbind(*[
		instrument: \basicFM,
		out: [0, 1],
		dur: Pseq([1/7, 1/3] / 1.2, inf),
		sustain: Pkey(\dur),
		amp: 1/4,
		carFreq: Pseq([1, 1, 1, 2] * 350, inf),
		modFreq: Pkey(\carFreq) * (1/8),
		modAmount: Pseq([2, 1, 0.5, 2, 1, 0.5, 2, 1, 2, 1, 0.5, 2, 1, 0.5, 2, 4] * 500, inf),
		clipAmount: Pseq([Pn(0.1, 32), Pn(1, 32)], inf)
	]).play;
}
)



(
{ | dur = 0.15, atk = 0.005, amp = 1 |
	SinOsc.ar(
		EnvGen.ar( Env(
			NamedControl.kr(\freq_l, [2960, 70, 90]),
			NamedControl.kr(\freq_d, [0.07, 0.2]),
			NamedControl.kr(\freq_c, [-13, -1]),
		) ),
		mul:amp * EnvGen.ar(
			Env.perc( atk, dur - atk, curve: NamedControl.kr(\amp_c, [-1, 6])),
			doneAction: 2
		);
	) ! 2;
}.asSynthDef.name_("kick").add;

{ | atk = 0.01, dur = 0.15, freq = 50, amp=0.8 |
	BPF.ar(LFSaw.ar(freq), freq, 2, mul: EnvGen.kr( Env.perc( atk, dur-atk, amp, 6 ), doneAction: 2 )) ! 2;
}.asSynthDef.name_("bass").add;

{ | dur = 0.15, freq = 50, amp = 0.8, index = 10 |
	PMOsc.ar(freq, freq + 5, index, mul: amp * EnvGen.kr( Env.triangle( dur ), doneAction: 2 )) ! 2;
}.asSynthDef.name_("bazz").add;
)


(
Ppar([
	Pbind(*[
		instrument: \kick,
		delta: 1,
		dur: Pfunc({ thisThread.clock.beatDur }) / 4,
		amp: Pseq([
			Pseq([1], 16),
			Pseq([0.8, 1], 8),
			Pseq([1], 16),
			Pseq([0.8, 0.8, 0.9, 1], 8)
		], inf) * 0.8,
		freq_l: Pstutter(Prand(2.pow((1..4)),inf), Pseq([
			`[2960,70,90],
			`[1260,60,110],
			`[4360,60,120]
		],inf))
	]),
	Pbind(*[
		instrument: \bass,
		delta: 1/4,
		dur: Pkey(\delta) * Pfunc({ thisThread.clock.beatDur }),
		freq: Pseq([\r, 50,60,70], inf),
		amp:0.6
	]),
	Pbind(*[
		instrument: \bazz,
		delta: 1/4,
		dur: Pkey(\delta) * Pfunc({ thisThread.clock.beatDur }),
		freq: Pseq([\r, 30,35,40], inf),
		amp:0.2
	])
]).play(TempoClock(156/60), quant:[1])
)

// kick from above?
(
Pbind(*[
	instrument: \kick,
	delta: Pseq(Array.geom(540, 1, 1.015).reciprocal.reverse),
	dur: Pkey(\delta) * Pfunc({ thisThread.clock.beatDur }),
	atk: Pkey(\dur) / 6,
        amp: 0.8
]).play;
)




(
{
	var env;
	env = LFPar.ar([0.5,0.8],add:1);
	Mix.new([Saw.ar(200)*env,Saw.ar(40)*env])*Saw.ar(80)*0.5;
}.play;
)

(
{
	var env;
	env = LFPar.ar([0.1,0.08],add:1);
	Mix.new([Saw.ar(2000)*env,Saw.ar(200)*env])*Saw.ar(80)*0.1;
}.play;
)

(
{
	var env;
	env = LFPar.ar([0.1,0.08],add:1);
	Mix.new([Saw.ar(VarSaw.ar(1/13,0,0.3,1000+1000))*env,Saw.ar(VarSaw.ar(1/13,0,0.7,2000+2000))*env])*Saw.ar(VarSaw.ar(1/13,0,0.7,2000+2000))*0.1;
}.play;
)



{
var delay1, delay2, source, shift;

//k from -1 to 1

//in samples
delay1= 100;
delay2= 40;

//	source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]));


	source = Dust.ar(50);
	shift = TwoTube.ar(source,1,0.99,delay1,delay2);
	//FreqShift.ar(shift, 400);
	PitchShift.ar(shift, pitchRatio: 0.1);
}.play


(
{
	var env;
	env = LFPar.ar(0.3,add:1);
	Mix.new([SinOsc.ar(80)*env,SinOsc.ar(40)*env])*Saw.ar(80)*0.5;
}.play;


// Adding dust.

{
var delay1, delay2, source, shift;

//k from -1 to 1

//in samples
delay1= 100;
delay2= 40;

	//source= Dust.ar(300)*4*EnvGen.ar(Env([1,1,0],[(delay1+delay2)/SampleRate.ir,0.0]),);
		//Impulse.kr(MouseY.kr(1,4))););

	source = Dust.ar(5);
	shift = TwoTube.ar(source,1,0.99,delay1,delay2);
	//FreqShift.ar(shift, 400);
	PitchShift.ar(shift, pitchRatio: 0.1);
}.play
)


/*
Schemawound track for Waxen Wing's "Give Me A Sine" compilation.

Compilation Description: All songs written using ONLY sine waves in their creation. All oscillations, modulations, lfo's, envelopes, etc, use only sine waves. No samples or outside source audio were permitted on this releases, unless of course the samples were of pure sine waves. Download includes full 8 panel artwork and extensive liner notes on each piece written by each artist.

Download the free compilation here: http://waxenwings.bandcamp.com/album/give-me-a-sine

Blog post about the creation of this track: http://schemawound.tumblr.com/post/24520532915/sinusoid
*/

(
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Sinusoid -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	SynthDef(\Sinusoid, {
		|
			out = 0,					gate = 1,					amp = 1,					freqArrayMult = 1,
			mod1FreqRLfoFreq = 0.1,		mod1FreqRLfoDepth = 100,	mod1FreqRLfoOffset = 100,
			mod2Freq = 50,				combDelay = 0.7, 			combDecay = 9,
			attack = 0.001 				release = 0.5
		|
		//
		var combMaxDelay = 10;
		//Many Sines
		var freqArray = (1..50) * freqArrayMult;
		var manySines = Mix(SinOsc.ar(freqArray));
		//Mod1
		var mod1FreqL = SinOsc.kr(150, 0, 20);
		var mod1FreqRLfo = SinOsc.kr(mod1FreqRLfoFreq, 0, mod1FreqRLfoDepth, mod1FreqRLfoOffset);
		var mod1FreqR = SinOsc.kr(mod1FreqRLfo, 0, 37);
		var mod1 = SinOsc.ar([mod1FreqL, mod1FreqR]);
		//Mod2
		var mod2 = SinOsc.ar(mod2Freq);
		//Sum and FX
		var sinSum = manySines * mod1 * mod2;
		var comb = sinSum; //+ CombC.ar(sinSum, combMaxDelay, combDelay, combDecay);
		var dist = comb.tanh;
		var env = dist * Linen.kr(gate, attack, amp, release, doneAction: 2);
		//Output
		Out.ar(out, env);
	}).add;

//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- ELEKTRO KICK -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	SynthDef(\ElektroKick, {
		|
			out = 0,		gate = 1,		amp = 1,			freqArrayMult = 1,
			basefreq = 50,	envratio = 3, 	freqdecay = 0.02, 	ampdecay = 0.5
		|
		var fenv = EnvGen.ar(Env([envratio, 1], [freqdecay], \exp), 1) * basefreq;
		var aenv = EnvGen.ar(Env.perc(0.005, ampdecay), 1, doneAction:2);
		var output = SinOsc.ar(fenv, 0.5pi, aenv) * amp;
		Out.ar(out, output!2);
	}).add;

//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- VERB -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	SynthDef(\Verb, {
		arg 	out = 0,	in,
			mix = 0.25,	room = 0.15,	damp = 0.5;

		var input, verb;

		input = In.ar(in);
		verb = FreeVerb.ar(input, mix, room, damp);
		Out.ar(out, verb!2);
	}).add;
)

(
	//Groups and Busses
	var sourceGroup = Group.new;
	var fxGroup = Group.after(~sourceGroup);
	var verbBus = Bus.audio(s, 2);
	var mainOut = 0;
	var verb = Synth.tail(~fxGroup, \Verb, [\in, verbBus, \out, mainOut, \mix, 1, \room, 1]);

	//Song Variables
	var bar = 0.94;
	var qNote = bar/4;
	var eNote = bar/8;

	//Mix
	var finalAmp 	= 0.1;
	var hatAmp 		= 0.6 * finalAmp;
	var bassAmp 	= 0.8 * finalAmp;
	var loToneAmp 	= 0.8 * finalAmp;
	var hiWhineAmp 	= 0.7 * finalAmp;
	var eKickAmp 	= 2.1 * finalAmp;

	//Basic Patterns
	var hat = {|beatsPerMeasure = 9, freqArrayMult = 1|
		Pbind(*[instrument: \Sinusoid, amp: hatAmp, group: sourceGroup,
			dur:				Pseq([bar / beatsPerMeasure], beatsPerMeasure),
			freqArrayMult:		Pxrand((1..12), inf),
			mod2Freq:			Pwhite(60, 6000, inf),
			mod1FreqRLfoFreq:	0.01,
			mod1FreqRLfoDepth:	Pwhite(1000, 3000, inf),
			mod1FreqRLfoOffset:	Pwhite(10, 300, inf),
			release:			Pkey(\dur)
		])
	};
	var bass = {|beatsPerMeasure|
		Pbind(*[instrument: \Sinusoid, amp: bassAmp, group: sourceGroup,
			dur:				Pseq([bar / beatsPerMeasure], beatsPerMeasure),
			mod2Freq:			Pwhite(30, 300, inf),
			mod1FreqRLfoFreq:	Pwhite(0.1, 0.3, inf),
			mod1FreqRLfoDepth:	Pwhite(10, 300, inf),
			mod1FreqRLfoOffset:	Pwhite(10, 300, inf),
			release:			0.001
		])
	};
	var lowtone = {|beatsPerMeasure = 1, attack = 0.001, out = 0|
		Pbind(*[instrument: \Sinusoid, amp: loToneAmp, group: sourceGroup,
			dur:				Pseq([bar / beatsPerMeasure], beatsPerMeasure),
			mod2Freq:			Pwhite(30, 400, inf),
			mod1FreqRLfoFreq:	Pwhite(0.1, 0.3, inf),
			mod1FreqRLfoDepth:	Pwhite(10, 300, inf),
			mod1FreqRLfoOffset:	Pwhite(10, 300, inf),
			attack:				attack,
			release:			Pkey(\dur),
			out:				out
		])
	};
	var lowtoneLong = Pbind(*[instrument: \Sinusoid, amp: loToneAmp, group: sourceGroup,
		dur:				Pseq([bar*8], 1),
		freqArrayMult:		3,
		mod2Freq:			50,
		mod1FreqRLfoFreq:	0.1,
		mod1FreqRLfoDepth:	100,
		mod1FreqRLfoOffset:	100,
		release:			3
	]);
	var hiWhine = {|out = 0|
		Pbind(*[instrument: \Sinusoid, amp: hiWhineAmp, group: sourceGroup,
			dur:				Pseq([bar], 1),
			mod2Freq:			2000,
			mod1FreqRLfoFreq:	Pwhite(0.1, 0.3, inf),
			mod1FreqRLfoDepth:	100,
			mod1FreqRLfoOffset:	100,
			release:			Pkey(\dur),
			out:				out
		])
	};
	var elektroKick = {|beatsPerMeasure = 1|
		Pbind(*[instrument: \ElektroKick, amp: eKickAmp, group: sourceGroup,
			dur:				Pseq([bar / beatsPerMeasure], beatsPerMeasure),
			basefreq:			Pwhite(70, 75),
			ampdecay:			2,
			envratio:			1,
			freqdecay:			1
		])
	};

	//8 Bar Patterns
	var loTonePat = [
		Pn(lowtone.(1), 8),					//loTone pattern 0 - 8 bars
		Pn(lowtone.(1, bar), 8),				//loTone pattern 1 - 8 bars
		Pn(lowtone.(1, out:verbBus), 8)	//loTone pattern 2 - 8 bars
	];

	var hiWhinePat = [
		Pn(hiWhine.(verbBus), 8),	//hiWhine pattern 0 - 8 bars
		Pn(hiWhine.(mainOut), 8)	//hiWhine pattern 0 - 8 bars
	];

	var hatPat = [
		Pseq([//hat pattern 0 - 8 bars
			Pn(hat.(9), 7),		hat.(11)
		]),
		Pseq([//hat pattern 1 - 8 bars
			hat.(8), 			hat.(9,(1..12)),	hat.(9), 			hat.(7,(1..12)),
			hat.(6,(1..12)),	hat.(12,(1..12)),	hat.(6,(1..12)),	hat.(24,(1..12))
		]),
		Pseq([//hat pattern 2 - 8 bars
			hat.(8), 			hat.(3),			hat.(6),			hat.(9),
			hat.(8,(1..12)),	hat.(3,(1..12)),	hat.(6,(1..12)),	hat.(12,(1..12))
		]),
		Pseq([//hat pattern 3 - 8 bars
			hat.(9), 			hat.(8),			hat.(7),			hat.(8),
			hat.(9,(1..12)),	hat.(8,(1..12)),	hat.(16,(1..12)),	hat.(32,(1..12))
		])
	];

	var bassPat = [
		Pn(bass.(3), 8),	//bass pattern 0 - 8 bars
		Pseq([				//bass pattern 1 - 8 bars
			bass.(4),	Pn(bass.(3),3)
		], 2),
		Pseq([				//bass pattern 2 - 8 bars
			bass.(4),	Pn(bass.(3),3),
			bass.(4),	Pn(bass.(3),2), 	bass.(5)
		]),
		Pseq([				//bass pattern 3 - 8 bars
			bass.(4), 	bass.(3.5), 		bass.(3),		bass.(3.5),
			bass.(4), 	bass.(3), 			bass.(6),		bass.(7)
		]),
		Pseq([				//bass pattern 4 - 8 bars
			bass.(4),	Pn(bass.(3), 7)
		]),
	];

	var kickPat = [
		Pn(elektroKick.(1), 8),		//kick pattern 0 - 8 bars
		Pn(elektroKick.(2), 8)	//kick pattern 1 - 8 bars
	];

	var drop = [
		Pn(Ppar([lowtone.(1), hiWhine.(verbBus)]), 2),										//Drop Pattern 0 - 2 bars
		Pn(Ppar([lowtone.(1), hiWhine.(verbBus), elektroKick.(1)]), 2),						//Drop Pattern 1 - 2 bars
		Pn(Ppar([lowtone.(1), hiWhine.(verbBus), elektroKick.(1), hiWhine.(mainOut)]), 2),	//Drop Pattern 2 - 2 bars
	];

	//Song
	var song = Pseq([
								loTonePat[0],
		Ppar([	bassPat[0], 	loTonePat[0]																				]),
		Ppar([	bassPat[0], 	loTonePat[0],					hatPat[0]													]),
		drop[0],
		Ppar([	bassPat[0], 	loTonePat[0], 								kickPat[0]										]),
		Ppar([	bassPat[0], 	loTonePat[0],					hatPat[0], 	kickPat[0]										]),
		drop[1],
		Ppar([	bassPat[1], 									hatPat[2], 	kickPat[0]										]),
		Ppar([	bassPat[2], 	loTonePat[0], 					hatPat[2], 	kickPat[0]										]),
		drop[2],
		Ppar([	bassPat[1], 									hatPat[2], 	kickPat[0],		hiWhinePat[0]					]),
		Ppar([	bassPat[2], 	loTonePat[0], 					hatPat[2], 	kickPat[0]										]),
		Ppar([	bassPat[1], 					loTonePat[2],	hatPat[2], 	kickPat[0],		hiWhinePat[0],	hiWhinePat[1]	]),
		Ppar([	bassPat[2], 	loTonePat[0],	loTonePat[2],	hatPat[2], 	kickPat[0],						hiWhinePat[1]	]),
		drop[0],
								loTonePat[2]
	]);

	song.play;
)

s.boot;
18/25*10

(
g.free; g ={
	var freq = exprand(50,130);
	var env;
	env = LFPar.ar([0.5,0.8],add:1);
	RLPF.ar(Mix.new([Saw.ar(2 * freq)*env,Saw.ar(freq)*env])*Saw.ar(freq)*0.5, freq * MouseY.kr(0.25,8, 1), 0.34).distort;
}.play;
)

g.free;

(
// weiweiwei ++ no comment //
SynthDef("allpassDelay", { | out=0 |
	var input ;
	input = In.ar(0, 2);
	8.do({ input = AllpassC.ar(input, 0.04, { Rand(0.001,0.04) }.dup, Rand(0.5, 3.0) )});
	ReplaceOut.ar(out, input);
       }
).play;

SynthDef("weiweiwei", { | out=0, pitch=300, pan=0, decay=1, mix=0.3, room=0.7, damp=0.4, dur=5 |
	var signal, input;
	input = Amplitude.kr(In.ar(0))*0.8;
	signal = Pan2.ar(
	signal = Klank.ar(`[[pitch*1.2, pitch*1.5, pitch*2.9, pitch*2.2], [0.13, 0.08, 0.05, 0.11],[1.2, 1, 1.2, 0.5]], BrownNoise.ar(input)), 0);
	signal = PitchShift.ar(signal, 0.3, LinLin.kr(input, 0.001, 0.025, 0.5, 6), 0.5, 0.004);
	signal = FreeVerb.ar(signal, mix, room, damp);
	signal = SplayAz.ar(2, signal, spread: 0.5, center: (LFSaw.ar(dur.reciprocal * Rand(-1, 1).sign, Rand(0, 2)).range(0, 2)));
	Out.ar(out, Limiter.ar(signal, 0.7, 0.01))
}).play;
)


(
{
	var env, speed = MouseX.kr(0.34,130, 1, 0.02);
	env = LFPar.ar([0.5,0.8],add:1);
	Mix.new([Saw.ar(speed)*env,Saw.ar(4*speed)*env])*Saw.ar(8*speed)*0.5;
}.play;
)
2