// TESTING MLemurGui

// filmpje maken.
~lm = MLemurGui.new;

// makes a tcp and udp connection
~lm.connect("192.10.1.2",8002,8000);

// sets lemur to send osc messages to the sc-lang. (57120)
~lm.set_osctarget(0,"192.10.1.16",NetAddr.langPort);

// sets lemur to send osc messages to the sc-lang. (57120) on another device
~lm.set_osctarget(1,"192.10.1.12",NetAddr.langPort);

// start with a nice fresh and empty lemur interface:
// when the screen flash it is also a sign that there is an connection.
~lm.resetAll;

// disconnect class.
~lm.disconnect;

// Add and remove works!
~lm.addPage("Thing")
~lm.removePage("Thing")

~lm.fader("Thing");
// move a fader!!
~lm.fader("Thing", x: 500, color: Color.rand);

~lm.removeFader("Thing");

~lm.range("Thing");
// move a fader!!
~lm.range("Thing", x: 200);

~lm.removeRange("Thing");

~lm.text("Thing");
// move a fader!!
~lm.text("Thing",  x: 600.rand, y: 800.rand, width: 200, content: "stockhausen".scramble, color: Color.rand);

~lm.removeText("Thing");

~lm.text("Default", "SomethingElse");
// move a fader!!
~lm.text("Default", "SomethingElse", x: 600.rand, y: 800.rand, content: "sono-boys".scramble);

~lm.removeText("Default", "Something");

~lm.oscaddr.sendMsg("/interface", "klang")
~lm.oscaddr.sendMsg("/interface", "Thing");

// generatively change font size!!

a = Array.fill(100, { |i|
	var name = "camper" ++ i, x = Pbrown(0,1023,30).asStream, y = Pbrown(0,768,30).asStream;
	[name, r { inf do: { |i|
		~lm.text(
			"Thing", name, x: x.next, y: y.next, width: 120,
			content: "stockhausen".scramble, color: Color.rand,
			fontSize: 16.rand + 8);
		exprand(0.1,0.2).wait;
		}}.play
	]
});

~lm.text

a do: { |slot| ~lm.removeText("Thing",slot.first); slot.last.stop };



s.boot;
~lm.addPage("multisine")
~lm.removePage("multisine")

30 do: { |i| var name = \sine ++ i; ~lm.fader("multisine", name, 984.rand, 300.rand, 40, 400) };

30 do: { |i| var name = \sine ++ i; ~lm.removeFader("multisine", name) };

OSCFunc.trace(true)
s.boot;
~synths = Array.fill(30, { { arg amp = 0; Pan2.ar(SinOsc.ar(exprand(100,16000),mul: Ramp.kr(amp * 0.1, exprand(3,18))),rrand(-1.0,1)) }.play });
~synths do: (_.set(\amp, 1 - 1.0.linrand.postln))
~synths do: (_.set(\amp, 0))
~synths do: (_.release(exprand(2,13)))
s.record
r { inf do: {
	var amp =  1.0.linrand, index = 30.rand;
	~lm.oscaddr.sendMsg('/sine' ++ index ++ '/x', amp);
		~synths[index].set(\amp,amp); exprand(0.01,0.4).wait } }.play

~resp = Array.fill(30, { |i| OSCFunc({|msg| ~synths[i].set(\amp,msg[1].squared).postln }, '/sine' ++ i ++ '/x') })
OSCFunc.trace(true)
b = OSCresponder(~lm.oscaddr,nil
	, { |msg| msg.postln; }).add

/sine11/x
OSCresponderNode(nil, '/sine0', { |t, r, msg| msg.postln }).add;

// faders
30 do: {|i|~lm.fader("klang",\sine++i,i%15*65+25,asInt(i/15%2,1)*350,65,350)};
30 do: {|i|~lm.oscaddr.sendMsg('/sine'++i++'/x',1.0.linrand)};
30 do: {|i|~lm.removeFader("klang",\sine++i)};


// another example of a additve synth.
~lm = MLemurGui.new;
~lm.connect("192.10.1.2",8002,8000);
~lm.set_osctarget(0,"192.10.1.16", NetAddr.langPort);
~lm.resetAll;

// faders
30 do: {|i|~lm.fader("klang",\sine++i,i%15*65+25,asInt(i/15%2,1)*350,65,350,Color.rand)};
30 do: {|i|~lm.oscaddr.sendMsg('/sine'++i++'/x',1.0.linrand)};

OSCFunc.trace(true);

30 do: {|i|~lm.removeFader("klang",\sine++i)};

~lm.disconnect;
// set array with klang!

(
~color = Color.green;
~colVal = { |r,g,b| }.value(*~color);
~lm.fader("Thing", color: ~colVal);
)


~lm.fader("Thing", color: Color.rand);
~lm.range("Thing", color: Color.rand, x: 400);
~lm.text("Thing", color: Color.rand, x: 400, y: 100, height: 40, fontSize: 24)

~lm.resetAll;


// pads and switches.
(
~lm.pads(x: 512, y: 100, width: 512,height: 512, column: 5, row: 5, colorOff: Color.blue, colorOn: Color.red);
~lm.switches(x: 0, y: 100, width: 512,height: 512, column: 5, row: 5, colorOff: Color.red, colorOn: Color.black);
)
~lm.removeSwitches;~lm.removePads;

	