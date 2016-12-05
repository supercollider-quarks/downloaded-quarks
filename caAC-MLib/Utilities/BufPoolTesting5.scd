/*
Belangrijk!!

Creeer een load file and save file knop zodat een project opgeslagen kan worden
en weer kan worden opgeroepen in een later stadium.

Bij elke actie schrijf naar een default locatie op schijf
Implementeer de BufRecord objecxt
methodes die aanwezen moeten zijn:
- na opname sla buffer data als geluids bestand in de default locatie op schijf.
- methodes/parameters die nodig zijn:
= record
= doneAction
= makeGui
= recordSettings
= channels
= duration
= metadata

Verder wanneer een bank verandert is, update de onderliggende objecten die de buffers
gebruiken in hun processen. Dit om realtime sampling and processing mogelijk te maken.

Maak een bank slider/selector zodat de EmbedGui objecten en the Score & Controller interface ook
gebruik kan maken van deze buffers.
4
*/

"killall scsynth".unixCmd;

/* bufferGui Control Model */


/* Als er op de stop edit Gaat string actie gaat af. Waarom? */


s.boot;


h = BufferPool();
h.makeGui;


~iBank = 3;

/* Responder function */
(
w = Window(); w.front;
b = SBufControl();
b.gui(w, Rect(0, 100, 400, 28));

b.tempActionFunc = { |oModelIP, cWhatIP, tArgsIP|

    cWhatIP.postln; tArgsIP.postln;

};
)










/* In de werk logica de dependants toevoegen*/
~model.addDependant({      }
    if (owhat == \iBank) {

        currentBank = // get bankname
        create a control spec !! //
        // Update sndfile list gui!!!

    }









(

a = { |i|
	var temp = SBufControl();

} ! 20;
)

// BufferCM.changed(\bankUpdate, ["Test");




    (
a  = {
    |...args|
    args.postln;
};
BufferCM.model.addDependant(a);
)

BufferCM.model.removeDependant(a);


  BufferDict.buffers



// The Control Model generates the lists from the BufferPool objects.
// These lists are passed to the BufferChannels

// The buffer channel control object properties
- The current bank
- The current soundfile index
- The midi cc controller assigned to it
- The if the sampler mode is on or off
- The if cc learn mode is enable
- The invertion of the incoming cc


- If the sampler mode is turned on the passed buffer is instantly assigned to the synth buffer key
- When a
- Also the bank is changed to the current sampling

- make gui method
- getState
- loadState

- Buffers en ControlSpecs are requested via the control model. This pool is static en nested in the model

// In the BufferPool dictionary a ControlSpec should be assign to a dedicated slot.
~spec = Dictionary();


~specs[thisBank.string] = ControlSpec(0, bufferDataBase[thisBank.string], step: 1);













// Setup GUI

// Setup BufferPoolData class which holds all the Buffer Mappings etc.

Window.new.front;

// Add an env editor!!

(
// use shift-click to keep a node selected
w = Window("envelope", Rect(150 , Window.screenBounds.height - 250, 250, 100)).front;
w.view.decorator = FlowLayout(w.view.bounds);

b = EnvelopeView(w, Rect(0, 0, 230, 80))
    .drawLines_(true)
    .selectionColor_(Color.red)
    .drawRects_(true)
    .resize_(5)
    .step_(0.05)
    .action_({arg b; [b.index, b.value].postln})
    .thumbSize_(5)
    .value_([[0.0, 0.1, 0.5, 1.0],[0.1,1.0,0.8,0.0]]);
w.front;
)

"mix" + 6 /* concat */

// A knop with snap options!!

(
var model, controlSpec, window, knopView, valueFunction, actionDependant, viewDependant;

model = (value: 0);

controlSpec = ControlSpec(1, 20, \lin, 1);

window = Window.new;
window.front;

knopView = Knob(window, window.bounds.extent);
knopView.value = model[\value];
knopView.action = { |knop| valueFunction.value(knop.value) };

valueFunction = { |argValue|
	argValue = controlSpec.map(argValue);
	model[\value] = argValue;
	model.changed(\value, argValue)
};

actionDependant = { |argModel|
	argModel[\value].postln;
};

model.addDependant(actionDependant);

viewDependant = { |argModel|
	knopView.value = controlSpec.unmap(argModel[\value]).postln;
};

model.addDependant(viewDependant);
)

(
w = Window.new.front;
v = LED(w, w.bounds.extent);
v.value = 1;
)

w.front; v.value = 1; v.bounds = Rect(0, 0, 200, 300);

// Double quote in string and next line.
"po\"\nhello\ndn\"dfl".postln;


(
{
var buf = h.bufferDataBase["test"].choose;
var nChannels = buf.numChannels;
[nChannels, buf].postln
} ! 10;
)
// Add an quick drop buffer option!!

// Make a autogui buffer element!!
(
q = Font("Menlo", 11, true);
w = Window();
c = PopUpMenu(w, Rect(0, 0, 100, 30));
c.items = h.soundFileViews.keys.asArray;
c.action = { |b| b.value.postln };
c.font_(q);
d = PopUpMenu(w, Rect(100, 0, 100, 30));
d.items = h.soundFileViews["test"].units.collect (_.string);
d.action = { |b| b.value.postln };
d.font_(q);
e = NumberBox(w, Rect(200, 0, 50, 30));
e.clipLo = 0;
e.clipHi = d.items.size - 1;
e.action = { |b| b.value.postln };
e.background = Color.clear;
e.font_(q);
f = Button(w, Rect(300, 0, 90, 30))
.states_([
	            ["sampling on", Color.black, Color.red(0.8)],
	            ["sampling off", Color.black, Color.clear],
        ]);
f.font_(q);
f.action = { |b| b.value.postln; };
w.front;
)


\











(
~saveDialog = File.saveDialog(successFunc: {
	|path| var file;
	path = PathName(path.standardizePath).extension_("xml").fullPath;
	file = File(path, "w");

	~retrieveXMLSTringAndSaveBuffersToFiles.value(path);
	~doneAction = { |xmlFileString|
		file.putString(xmlFileString);
		file.close;
	}
});

~retrieveXMLSTringAndSaveBuffersToFiles = { |argPath|
	var audio_file_bank_settings, audiofile_path;
	var newDOMDocument = DOMDocument.new;
	var root = newDOMDocument.createElement("bufferPool");
	root.setAttribute( "selected_bank", ~bufPool.currentBankName);
	newDOMDocument.appendChild(root);

	audio_file_bank_settings = newDOMDocument.createElement("audiofile_bank_settings");
	root.appendChild(audio_file_bank_settings);

	audiofile_path = PathName(argPath).pathOnly ++ "audio/";
	if (audiofile_path.pathExists != \folder) {

		"mkdir %".format(audiofile_path).unixCmd(postOutput: false)
	};

	fork {
		0.05.wait;

		~bufPool.soundFileViews keysValuesDo: { |bankName, soundFileView|
			var bank = newDOMDocument.createElement("bank");
			bank.setAttribute("name", bankName);
			audio_file_bank_settings.appendChild(bank);

			soundFileView.units do: { |bufferUnit|
				var bufferElement = newDOMDocument.createElement("file");

				var pathName = bufferUnit.string;
				if (pathName.pathExists != \file) {
					pathName = audiofile_path ++ bufferUnit.string ++ ".aiff";
					~bufPool.bufferDataBase[bankName][bufferUnit.index].write(pathName)
				};

				bufferElement.setAttribute( "pathname", pathName);
				bank.appendChild(bufferElement);
			}
		};
		~doneAction.value(newDOMDocument.format);
	}
};
)

(
File.openDialog(successFunc: {
	|path| var file, xmlString;
	path.standardizePath;
	file = File(path, "r");
	xmlString = String.readNew(file);
	file.close;
	~loadStateFromXML.value(xmlString);
});

~loadStateFromXML = { |xmlString|
	var root, audio_file_bank_settings, banks;
	var newDOMDocument = DOMDocument.new;
	newDOMDocument.parseXML(xmlString);

	root = newDOMDocument.getChildNodes;
	audio_file_bank_settings = root.first.getChildNodes.first;
	banks = audio_file_bank_settings.getChildNodes;
	banks do: { |bank, index|
		var bankName = bank.first.getAttribute("name");
		~bufPool.bankView.add(argName: bankName);

		bank.first.getChildNodes do: { |file|
			var pathName = file.getAttribute("pathname");
			~bufPool.soundFileViews[bankName].addFileToBuffer(pathName);
		};
	};
	"\n\n\n\n".postf;
}
)


SynthDef(\BufPlay, {
	arg out = 0, amp = 0, bufnum = 0, pitch = 0, start = 0, loop = 0, pan = 0, env = 0, gate = 0;
	var aenv, sig;

	aenv	= EnvGen.kr(Env.asr(0.005,1,0.05,-2), gate: gate, doneAction: 2);
	sig	= PlayBuf.ar(1, bufnum, pitch.midiratio * BufRateScale.kr(bufnum), 1, start, loop);
	sig	= Pan2.ar(sig,pan, aenv * amp);

	Out.ar(out,sig);
}).add;
s.reboot;






//In this example code, a sound file can be made to loop at any selection in the graphical display


//load soundfile onto Server
b = Buffer.read(s,Platform.resourceDir +/+ "sounds/a11wlk01.wav");


//SynthDef (making Synth straight away) which has arguments for the loop points
c= SynthDef(\loopbuffer, {arg start=0, end=10000; Out.ar(0,Pan2.ar(BufRd.ar(1, 0, Phasor.ar(0, BufRateScale.kr(b.bufnum), start, end),0.0)))}).play(s);
//*BufFrames.ir(b.bufnum) //this isn't needed since the GUI gives us positions directly in samples



(		// make a simple SCSoundFileView
w = Window("soundfiles looper", Rect(10, 700, 750, 100));
w.front;
a = SoundFileView(w, Rect(20,20, 700, 60));

f = SoundFile.new;
f.openRead(Platform.resourceDir +/+ "sounds/a11wlk01.wav");

a.soundfile = f;				// set soundfile
a.read(0, f.numFrames);		// read in the entire file.
a.refresh;							// refresh to display the file.

//set a function which is called when the mouse is let go, i.e. just after dragging out a selection in the window
a.mouseUpAction_({arg view;
	var where;

	where= (view.selections[0]); //get the latest selection (assuming no other selections going on)

	where.postln; //post where - start sample and length in samples of selection

	c.set(\start, where[0], \end, where[0]+where[1]); //convert to start and end samples for loop area
});
)



~looper = Synth.new(\looper, [\bufnum,b])


//this loads into a buffer the default sound that comes with SuperCollider
//.read brings in the whole sound at once
b = ~bufPool.bufferDataBase["default bank 0"].first;

{
	arg rate=1, bufnum = b, phasBuf, pulse=1;
	var out, pos, gate;
	gate = Impulse.kr(pulse);
	pos = K2A.ar(Linen.kr(gate, In.kr(phasBuf)/s.sampleRate, In.kr(phasBuf), 0));
	out = BufRd.ar(1, bufnum, pos);
	Out.ar(0, out!2);
}





// allocate a buffer with a sound file
b = Buffer.read(s, "sounds/a11wlk01.wav");

// simple playback (more examples: see BufRd)
// Start and end here are defined as 0 and the number of frames in the buffer.
// This means that the Phasor will output values from 0 to numFrames - 1 before looping,
// which is perfect for driving BufRd. (See note above)
{ BufRd.ar(1, b.bufnum, Phasor.ar(0, BufRateScale.kr(b.bufnum), 0, BufFrames.kr(b.bufnum))) }.play;


// two phasors control two sound file positions: mouse y controls resetPos of the second
(
{ var trig, rate, framesInBuffer;
	rate = MouseX.kr(0.1, 100, 1);
	trig = Impulse.ar(rate);
	framesInBuffer = BufFrames.kr(b.bufnum);
	x = Phasor.ar(trig, BufRateScale.kr(b.bufnum), 0, framesInBuffer,
		[0, MouseY.kr(0, framesInBuffer)]);
	BufRd.ar(1, b.bufnum, x);
}.play;
)





//using Mouse to scrub through- X position is normalised position 0 to 1 phase through the source file
SynthDef("bufrd",{ arg out=0,bufnum=0;
	Out.ar(out,
		Pan2.ar(BufRd.ar(1, b, K2A.ar(BufFrames.ir(b.bufnum)*MouseX.kr(0.0,1.0)).lag(MouseY.kr(0.0,1.0))),0.0)
	)
}).play(s);
)

// now for a twist
(
a = Signal.fill(256, { |i|
	var t = i/255.0;
	// t + (0.1 * (max(t, 0.1) - 0.1) * sin(2pi * t * 80 + sin(2pi * 25.6 * t)))
	t;
})
);

a.plot
a.copy.reverse.neg
a = (a.copy.reverse.neg) ++(Signal[0]) ++ a;

d.plot

d.size	//must be buffer size/2 + 1, so 513 is fine

b.sendCollection(d.asWavetableNoWrap);  // may also use loadCollection here

x = 513.rand; b = Buffer.loadCollection(s, Array.fill(513, { |i| i + x / (512 * 0.5) % 2 - 1 }));
x = 513.rand; b.sendCollection(Array.fill(513, { |i| neg(i + x / (512 * 0.5) % 2 - 1) + 0.1.rand2 }))
b.plot // wavetable format!


// Maak hier een feedback instrument van!

(
{
	arg modFreq = 80, detune = 1, clip = 10;
	var tri = Impulse.ar(0);
	var in = LocalIn.ar(2) + tri;
	var bank = Mix.new(BPF.ar(in, {{exprand(50,1300)}!3}!2, 1));
	var sig = Shaper.ar(b, Mix.new(DelayN.ar(bank,0.1,{0.1.rand}!rrand(3,11))));
	sig = LeakDC.ar(sig);
	// sig = RLPF.ar(sig, 200, 0.34);
	sig = sig * SinOsc.ar([modFreq,modFreq * midiratio(detune)], 0, clip).distort;
	LocalOut.ar(sig * 100);
	sig*0.2;
}.play
)
s.scope;

Phasor



// definition of the looper
SynthDef(\looper, {
    // need a buffer to listen to and an input for the loop trigger
    arg bufnum, t_reset;

    // variables for the existing signal in the loop, the new input,
    // the output signal and the recording head position
    var inputSig, outputSig, existingSig, recHead;

    // get the input signal
    inputSig = In.ar(0);

    // generate the recording (also playback) position
    recHead = Phasor.ar(t_reset, BufRateScale.kr(bufnum), 0, BufFrames.kr(0));

    // read the existing signal from the loop
    existingSig = BufRd.ar(1, bufnum, recHead);

    // put the existing signal plus the new signal into the loop
    BufWr.ar(inputSig + existingSig, bufnum, recHead);

    // play back signal we got from the loop before the writing operation
    Out.ar(0, existingSig);
}).add;

// a crappy instrument to test with
SynthDef(\ping, {
    arg freq;
    var sig;
    sig = SinOsc.ar(freq) * 0.5;
    sig = EnvGen.kr(Env.perc(0.05, 2), doneAction:2) * sig;
    Out.ar(0, sig);
}).add;

// create a big empty buffer (20 secs is enough for me)
b = Buffer.alloc(s, 20 * s.sampleRate, 1)

// listen for a an OSC message indicating that we've reached the loop point
p = OSCresponderNode(nil, '/newbar', {|t, r, msg| ~looper.set(\t_reset, 1)}).add;

// listen for OSC triggering the test instrument
o = OSCresponderNode(nil, '/newnote', {
    |t, r, msg| msg[1].postln; Synth.new(\ping, [\freq, msg[1].midicps])
}).add;

// create an instance of the looper
~looper = Synth.new(\looper, [\bufnum, b.bufnum])
Now the Processing:

// import the OSC libraries (see http://www.sojamo.de/libraries/oscP5/)
import oscP5.*;
import netP5.*;

// the OSC handler and target address
OscP5 oscP5;
NetAddress remote;

// where in the bar are we
int beatCount;

// lock to a scale so the test instrument sounds a bit nicer
int[] cMinor = {48, 50, 52, 53, 55, 57, 59, 60, 62, 64, 65, 67, 69};

void setup() {
  size(260, 200);
  frameRate(30);

  // draw a keyboard to test with
  for(int i = 1; i <= 12; i++) {
    line(20 * i, 0, 20 * i, 100);
  }

  // instantiate the OSC handler
  oscP5 = new OscP5(this, 10000);

  // Supercollider is assumed to be listening on
  // localhost at the default sclang port
  remote = new NetAddress("127.0.0.1", 57120);
}

void draw() {
  // pulse in time with beat
  fill(255 / ((frameCount % 15) + 1));
  rect(10, 110, 40, 40);

  // one beat every 15 frames
  if(frameCount % 15 == 0) {
    beatCount++;
  }

  // at the end of the 16 beat bar, trigger the loop back to the beginning
  if(beatCount == 16) {
    OscMessage msg = new OscMessage("/newbar");

    oscP5.send(msg, remote);
    beatCount = 0;
  }

  // pulse in time with the bar
  fill(255 / (beatCount + 1));
  rect(60, 110, 40, 40);
}

// use the mouse to play the test instrument
void mousePressed() {
  int noteNum = int(mouseX / 20);
  OscMessage msg = new OscMessage("/newnote");
  msg.add(cMinor[noteNum]);
  oscP5.send(msg, remote);
}






// initial buffer
~buf = Buffer.alloc(s, s.sampleRate * 2.0, 1);

// bus to keep track of the current length of the buffer which has been overwritten/recorded
~phasBuf = Bus.control(s, 1);

// parameter which controls sensitivity of the mic
~minsig = Bus.control(s, 1);


// this synthdef records to ~buf and keeps overwriting it when it detects non-silence
(
SynthDef(\bufwr, {
	arg rate=1, bufnum, phasBuf;
	var in, avg, gate, phase;
	in = SoundIn.ar(0);
	avg = Amplitude.kr(in, 10, 10);
	gate = (avg > In.kr(~minsig));
	gate.poll(20).postln;
	phase = Phasor.ar(gate, BufRateScale.kr(bufnum) * gate * rate, 0, BufFrames.kr(bufnum));
	BufWr.ar(in, bufnum, phase);
	Out.kr(phasBuf, phase);
	0.0
}).add;
)

// original version -- this doesn't work well because i cant trigger it at will
(
SynthDef(\bufrd, {	arg rate=1, bufnum, phasBuf;
	var out, phase;
	phase = Phasor.ar(1, BufRateScale.kr(bufnum) * rate, 0, In.kr(phasBuf));
	out = BufRd.ar(1, bufnum, phase);
	Out.ar(0, out!2);
}).add;
)

// second version using Linen to control the position of the buffer. can trigger at will
(
SynthDef(\bufrd2, {
	arg rate=1, bufnum, phasBuf, pulse=1;
	var out, pos, gate;
	gate = Impulse.kr(pulse);
	pos = K2A.ar(Linen.kr(gate, In.kr(phasBuf)/s.sampleRate, In.kr(phasBuf), 0));
	out = BufRd.ar(1, bufnum, pos);
	Out.ar(0, out!2);
}).add;
)

// third version. uses Playbuf and an envelope as long as the buffer position
(
SynthDef(\bufrd3, {
	arg rate=1, bufnum, phasBuf;
	var env, out;
	env = EnvGen.kr(Env([0, 1, 1, 0], [0.05, In.kr(phasBuf)/s.sampleRate - 0.1, 0.05]), doneAction: 2);
	out = env * PlayBuf.ar(1, bufnum);
	Out.ar(0, out!2);
}).add;

// second version using Linen to control the position of the buffer. can trigger at will
(
SynthDef(\bufrd2, {
	arg rate=1, bufnum, phasBuf, pulse=1;
	var out, pos, gate;
	gate = Impulse.kr(pulse);
	pos = K2A.ar(Linen.kr(gate, In.kr(phasBuf)/s.sampleRate, In.kr(phasBuf), 0));
	out = BufRd.ar(1, bufnum, pos);
	Out.ar(0, out!2);
}).add;
)

// To add

// Waveform view and Signal Feature Extraction Tools.

Buffer().as(SoundFile)

s.boot;
SoundFile.asBuffer

s.boot;

// Show a soundfile!!
(
w = Window().front;
b = SoundFileView(w, w.bounds.extent);
b.gridOn = false;
c = Buffer.read(s,"/Users/MarinusKlaassen/Desktop/Novation Heritage Soundpack novationmusic.com/Bass Station/Loops/NOVHTG 120 A - BassStation I - Epic Saw.wav"
);
c.loadToFloatArray(action: { arg array; a = array; {b.data = array; }.defer; "done".postln;});
)


r = CompositeView(w, Rect(5, 310, 594, 105));
r.background = Color.grey;
t = SoundFileView(r, r.bounds.extent);
t.gridOn = false;
u = Buffer.read(s, "/Users/MarinusKlaassen/Desktop/Novation Heritage Soundpack novationmusic.com/UltraNova/Loops/NOVHTG 110 F - Ultranova - Evil Clown.wav"

);
u.loadToFloatArray(action: { arg array; {t.data = array; }.defer; "done".postln;});
