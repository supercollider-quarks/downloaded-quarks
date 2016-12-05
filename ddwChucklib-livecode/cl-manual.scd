/**************
 Listing 1. A simple cll process.
 **************/

(
PR(\abstractLiveCode).chuck(BP(\beep), nil, (
   userprep: {
      ~buf = Buffer.read(
         s, Platform.resourceDir +/+ "sounds/a11wlk01.wav",
         4982, 10320
      );
      ~defaults[\bufnum] = ~buf;
      SynthDef(\buf1, { |out, bufnum, pan, amp, time = 1|
         var sig = PlayBuf.ar(1, bufnum),
         eg = EnvGen.kr(
            Env.linen(0.02,
               min(time, BufDur.ir(bufnum) - 0.04), 0.02),
            doneAction: 2
         );
         Out.ar(out, Pan2.ar(sig, pan, amp * eg));
      }).add;
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
)

// Use it, with cll statements:
TempoClock.tempo = 2;

/beep = "^|.. .| .- | .  ";  // "Set pattern"
/beep+;  // start it

/beep..pan = "<><><><>";

/beep-;

/beep(free);


/**************
 Listing 2. Template for the parameter map.
 **************/

parmMap: (
   parmName: (
      char: value,
      char: value,
      char: value...
   ),
   parmName: (...)
)


/**************
 Listing 3. How to write arrays in the parameter map.
 **************/

parmMap: (
   freqs: (
      $2: [200, 300, 400],
   ),
   parmName: (...)
)


/**************
 Listing 4. Arrays for multiple-parameter setting using one cll parameter.
 **************/

parmMap: (
   filt: (
      alias: [\ffreq, \rq],
      $x: [2000, 0.05]
   )
)


/**************
 Listing 5. Cll statements, one by one or as a batch.
 **************/

// run one at a time
/kick.fotf = "----";
/snare.bt24 = " - -";

// or as a batch
/kick.fotf = "----"; /snare.bt24 = " - -";


/**************
 Listing 6. Syntax template for the Set pattern statement.
 **************/

/proc.phrase.parm = quant"string";


/**************
 Listing 7. Multiple parameters with different timing.
 **************/

/x = "--";
/x.filt = "ab c";  // "c" is not heard

/x = "-|-  -";  // now "c" is heard on beat 4.5


/**************
 Listing 8. A retro acid-house bassline, demonstrating pitch notation.
 **************/

(
SynthDef(\sqrbass, { |out, freq = 110, gate = 1,
   freqMul = 1.006, amp = 0.1,
   filtMul = 3, filtDecay = 0.12, ffreq = 2000, rq = 0.1,
   lagTime = 0.1|
   var sig = Mix(
      Pulse.ar(
         Lag.kr(freq, lagTime) * [1, freqMul],
         0.5
      )
   ) * amp,
   filtEg = EnvGen.kr(
      Env([filtMul, filtMul, 1], [0.005, filtDecay], \exp),
      gate
   ),
   ampEg = EnvGen.kr(
      Env.adsr(0.01, 0.08, 0.5, 0.1),
      gate, doneAction: 2
   );
   sig = RLPF.ar(sig, (ffreq * filtEg).clip(20, 20000), rq);
   Out.ar(out, (sig * ampEg).dup);
}).add;

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

TempoClock.tempo = 132/60;
)

/acid = "1_  1.|5~3_9.4.|7.2~4_5'.|5_8~2_4.";

/acid+;
/acid-;


/**************
 Listing 9. Syntax template for "Set pattern" phrase selection.
 **************/

/proc = (group...);


/**************
 Listing 10. Nested phrase-selection groups.
 **************/

((a%4|b)*4.(a|b%4)*2)


/**************
 Listing 11. Syntax template for randomizer statement.
 **************/

/proc.prefix.parm *n +ki %q "string"


/**************
 Listing 12. Examples of randomizer statements.
 **************/

// assuming BP(\snr) defines:
// "-" (normal note)
// "." (softer note)
// Produces strong notes on 2 and 4, and one note elsewhere
/snr.a *10 +1. "|-||-";
/snr = ('^a');  // randomly choose one variation for each bar

// "-" = open, "." = closed
/hh = "..|..|..|..";  // all closed at first

// add an open HH on any empty 16th
/hh.a *10 +1- main;  // "main" refers to the above
/hh = ('^a');

// totally random HH rhythm (probably sounds stupid)
{ "-.".wchoose(#[0.16, 0.84]) } => Func(\randHH);
/hh.b *10 +9\randHH "|||";

// or random notes on 8ths
/hh.b *10 +5\randHH %0.5 "|||";

// or, random notes, but don't allow two "-" in a row
(
{ |prev|
   if(prev == $-) { $. } {
      "-.".wchoose(#[0.16, 0.84])
   }
} => Func(\randHH);
)

/hh.b *10 +9\randHH "|||";


/**************
 Listing 13. Syntax template for make statements.
 **************/

/make(factory0:targetName0/factory1:targetName1/...);


/**************
 Listing 14. Example of the make statement.
 **************/

(
// THIS PART IN THE INIT FILE
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

// DO THIS IN PERFORMANCE
/make(demoBP:dm);  // :dm overrides defaultName

/dm = "1353427,5,";
/dm+;
/dm-;

/dm(free);


/**************
 Listing 15. Syntax template for passthrough statements.
 **************/

// This...
/snr(clock = ~myTempoClock);

// ... is the same as running:
BP(\snr).clock = ~myTempoClock;

// Or...
/VC.bass(releaseAll);  // VC(\bass).releaseAll;


/**************
 Listing 16. Syntax template for Chuck statements.
 **************/

// This...
/snr => MCG(0);

// ... is the same as running:
BP(\snr) => MCG(0);

// Or...
/VC.keys => MCG(0);  // VC(\keys) => MCG(0);


/**************
 Listing 17. Syntax template for func-call statements.
 **************/

/func.(arguments);

// e.g.:
/bars.(\proc, 2, \a);


/**************
 Listing 18. Syntax template for copy/transfer statements.
 **************/

/proc.phrase*n -> newPhrase;  // copy

/proc.phrase*n ->> newPhrase;  // transfer


/**************
 Listing 19. Demonstration of "Show pattern" statements.
 **************/

/snr.a = " - -";

/snr.a -> b;

/snr.b   // now hit ctrl-return at the end of this line

// the line magically changes to
/snr.b = " - -";


/**************
 Listing 20. Common initialization sequence, using helper functions.
 **************/

/make(kick);
/bars.(\kick, 2, \a);

// the following lines are automatically inserted
/kick.a0 = "";
/kick.a1 = "";


/**************
 Listing 21. Isorhythmic cycles with generators.
 **************/

(
BP(\y).free;
PR(\abstractLiveCode).chuck(BP(\y), nil, (
	event: (eventKey: \default),
	defaultParm: \degree,
	parmMap: (degree: (isPitch: true))
));
)

TempoClock.tempo = 140/60;

/y = "12 4| 5 6| 12 |45";

/y+;

/y = "\seq("** *| * *| ** |**", "12456", "*")";

/y = "\ins(\seq("** *| * *| ** |**", "12456", "*"), "*", 7, 0.25)";

/y = "\seq(\ins(\seq("** *| * *| ** |**", "12456", "*"), "*", 7, 0.25), "6,214", "*")";

/y = "\seq("** *| * *| ** |**", "12456", "*")::\ins(, "*", 7, 0.25)::\seq(, "6,214", "*")";

/y-;


/**************
 Listing 22. Interaction between generator syntax and "set pattern" rhythmic notation.
 **************/

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


/**************
 Listing 23. Cll statement regular expression templates.
 **************/

   ~statements = [
      \clMake -> "^ *make\\(.*\\)",
      \clFuncCall -> "^ *`id\\.\\(.*\\)",
      \clPassThru -> "^ *([A-Z][A-Za-z0-9_]*\\.)?`id\\(.*\\)",
      \clChuck -> "^ *([A-Z][A-Za-z0-9_]*\\.)?`id *=>.*",
      \clPatternSet -> "^ *`id(\\.|`id|`id\\*[0-9]+)* = .*",
      \clGenerator -> "^ *`id(\\.|`id)* \\*.*",
      // harder match should come first
      \clXferPattern -> "^ *`id(\\.`id)?(\\*`int)? ->>",
      \clCopyPattern -> "^ *`id(\\.`id)?(\\*`int)? ->",
      \clStartStop -> "^([/`spc]*`id)+[`spc]*[+-]",
      \clPatternToDoc -> "^ *`id(\\.|`id)*[`spc]*$"
   ];


/**************
 Listing 24. Regular expression macros for SC language tokens.
 **************/

   ~tokens = (
      al: "A-Za-z",
      dig: "0-9",
      id: "[A-Za-z][A-Za-z0-9_]*",
      int: "(-[0-9]+|[0-9]+)",
      // http://www.regular-expressions.info/floatingpoint.html
      float: "[\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?",
      spc: "    "  // space, tab, return
   );


/**************
 Listing 25. Template for cll statement handlers.
 **************/

Proto {
   ~process = { |code|
      // parse 'code' and build the SC language statement(s)...
      translatedStatement  // return value
   };
} => PR(\clMyNewStatement);


/**************
 Listing 26. Adding a function into PR(\chucklibLiveCode) for simple statement types.
 **************/

PR(\chucklibLiveCode).clMyNewStatement = { |code|
   // parse 'code' and build the SC language statement(s)...
   translatedStatement  // return value
};


