\loadCl.eval;

(
PR(\abstractLiveCode).chuck(BP(\beep), nil, (
	userprep: {
		~buf = Buffer.read(
			s,
			Platform.resourceDir +/+ "sounds/a11wlk01.wav",
			4982, 10320
		);
		SynthDef(\buf1, { |out, bufnum, pan, time = 0.1, amp|
			var sig = PlayBuf.ar(1, bufnum),
			eg = EnvGen.kr(
				Env.linen(0.02, time - 0.02, 0.02),
				doneAction: 2
			);
			Out.ar(out, Pan2.ar(sig, pan, amp * eg));
		}).add;
		~defaults[\bufnum] = ~buf;
	},
	userfree: {
		~buf.free;
	},
	defaultParm: \amp,
	parmMap: (
		amp: ($.: 0.1, $-: 0.4, $^: 0.8),
		pan: (
			$<: -0.9, $>: 0.9,
			$(: -0.4, $): 0.4,
			$-: 0
		)
	),
	defaults: (instrument: \buf1),
	postDefaults: Pbind(
		\time, (Pkey(\dur) * 0.6 / Pfunc { ~clock.tempo }).clip(0.04, 0.2)
	)
));
TempoClock.default => BP(\beep);
)

TempoClock.tempo = 2;

/beep = "^|.. .| .- | .  ";
/beep+;

/beep..pan = "<><><><>";

/beep-;

/beep(free);



// acid bassline
(
SynthDef(\sqrbass, { |out, freq = 110, gate = 1, freqMul = 1.006, amp = 0.1,
	filtMul = 3, filtDecay = 0.12, ffreq = 2000, rq = 0.1,
	lagTime = 0.1|
	var sig = Mix(Pulse.ar(Lag.kr(freq, lagTime) * [1, freqMul], 0.5)) * amp,
	filtEg = EnvGen.kr(Env([filtMul, filtMul, 1], [0.005, filtDecay], \exp), gate),
	ampEg = EnvGen.kr(Env.adsr(0.01, 0.08, 0.5, 0.1), gate, doneAction: 2);
	sig = RLPF.ar(sig, (ffreq * filtEg).clip(20, 20000), rq);
	Out.ar(out, (sig * ampEg).dup);
}).add;
)

(instrument: \sqrbass, midinote: 36, ffreq: 300, rq: 0.2, filtMul: 8).play;

(
BP(\acid).free;
PR(\abstractLiveCode).chuck(BP(\acid), nil, (
	event: (eventKey: \default),
	alwaysReset: true,
	defaultParm: \degree,
	parmMap: (
		degree: (isPitch: true),
	),
	defaults: (
		ffreq: 300, filtMul: 8, rq: 0.2,
		octave: 3, root: 6, scale: Scale.locrian.semitones
	),
	postDefaults: PmonoArtic(\sqrbass,
		\dummy, 1
	)
));
)

/acid(defaults.octave = 2);
/acid(defaults.root = 6);
/acid(defaults.scale = Scale.locrian.semitones);

/acid = "1.|1.|1_  1.|";

/acid = "1~5~1~9~";

/acid = "1_  1.|5~3_9.4.|7.2~4_5'.|5_8. 2.";

/acid = "1_  1.|5~3_9.4.|7.2~4_5'.|5_8~2_4.";

/acid+;
/acid-;

/acid(reset);

8.degreeToKey(Scale.locrian.semitones, 12);
4.degreeToKey(Scale.locrian.semitones, 12);

92.4986.cpsmidi
42.asMIDINote

TempoClock.tempo = 132/60;



(
(
defaultName: \demo,
make: { |name|
	PR(\abstractLiveCode).chuck(BP(name), nil, (
		event: (eventKey: \default),
		defaultParm: \degree,
		parmMap: (degree: (isPitch: true))
	));
}, type: \bp) => Fact(\demoBP);
)

/make(demoBP);

/demo = "1353427,5,";
/demo+;
/demo-;

/demo(free);

/make(demoBP:dm);

/dm = "1353427,5,";
/dm+;
/dm-;

/dm(free);



// importing


(
BP(\y).free;
PR(\abstractLiveCode).chuck(BP(\y), nil, (
	event: (eventKey: \default),
	defaultParm: \degree,
	parmMap: (degree: (isPitch: true))
));
)

TempoClock.tempo = 140/60;

/y = "\seq("** *| * *| ** |**", "12456", "*")";

/y+

/y = "12 4| 5 6| 12 |45";

/y = "\seq("** *| * *| ** |**", "12456", "*")";

/y = "\ins(\seq("** *| * *| ** |**", "12456", "*"), "*", 7, 0.25)";

/y = "\seq(\ins(\seq("** *| * *| ** |**", "12456", "*"), "*", 7, 0.25), "6,214", "*")";

/y = "\seq("** *| * *| ** |**", "12456", "*")::\ins("", "*", 7, 0.25)::\seq("", "6,214", "*")";

/y = "\seq("** *| * *| ** |**", "12456", "*")::\ins(, "*", 7, 0.25)::\seq(, "6,214", "*")";

/y-;







// 1. \rand starts on the downbeat and occupies the whole bar.
/y = "\rand(\ins("1,", "*", 3, 0.5), "13467", "*")";

/y+;

// 2. \rand starts on beat 2
/y = "1,|\rand(\ins("6,", "*", 3, 0.5), "13467", "*")||";

// 3. \rand starts on the 2nd 16th-note of beat 2
/y = "1,|6,\rand(\ins("", "*", 3, 0.5), "13467", "*")  ||";

// 4. \rand starts on the 2nd 16th-note of beat 2
// and stops on the 'and' of 4
/y = "1,|6,\rand(\ins("", "*", 3, 0.5), "13467", "*")  || x";

/y-;
