//  How to store and load a project??
//  How to store and read states
//  Implement a way to use sliders mapped from another channel.
//  How to minimize the usage of controllers and its assignment?
//  Implement the whole Score&Controller interface as classes.


Facebook


// FIRST CONNECT LEMUR
~lm = MLemurGui.new;
// Connect with Lemur client by setting IP of the ipad or iphone
~lm.connect("192.10.1.2",8002,8000);
// Sets target on the Lemur to send OSC to the IP of this computer
// Change address if nescessary
~lm.set_osctarget(0,"192.10.1.16", NetAddr.langPort);

// RESET LEMUR
~lm.resetAll;

// BOOT SERVER
s.boot;
s.reboot
(
// MAKE A NEW MIXER INSTANCE
~scoreAndMixer.newInstance;
// Set lemur flag on true
~scoreAndMixer.lemurFlag = true;
// MAKE A NEW MIXER VIEW
~scoreAndMixer.makeGui;
)

// View to front
~scoreAndMixer.makeGui;

// CLEANUP STUF
~lm.disconnect;


// How to change a parameter??
osc_message="/test/x", osc_message="/%/z"

OSCFunc.trace(true);

(
SynthDef(\FM, { arg freq = 200, hr = 4, md = 24, dur = 5,curve = -4, amp = 0.1, out, pan = 0;
	var env = EnvGen.kr(Env.perc(0.001,dur,amp,curve),1,doneAction: 2);
	var modFreq = (freq * hr.reciprocal);
	var sig = PMOsc.ar(freq, modFreq, md,0);
	OffsetOut.ar(out, PanAz.ar(4, sig, pan, env));
}, \ir!8).add;
)

(
// MAKE A NEW MIXER INSTANCE
~scoreAndMixer.newInstance;
// Set lemur flag on true
~scoreAndMixer.lemurFlag = false;
// MAKE A NEW MIXER VIEW
~scoreAndMixer.makeGui;
)





