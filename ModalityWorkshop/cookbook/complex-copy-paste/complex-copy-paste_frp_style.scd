// complex-copy-paste in FRP style.
// requires FPLib https://github.com/miguel-negrao/FPLib

(
q = Namespace();

q.n = 15;

//Declare synthdefs
q.sources = [
	{
		var snd = RLPF.ar(Pulse.ar(\freq.kr(200), 0.2), 2500, 0.8) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	},
	{
		var snd = SinOsc.ar(\freq.kr(200)) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	},
	{
		var snd = Saw.ar(\freq.kr(200)) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	}
];

q.names = [\def1, \def2, \def3];

[q.sources, q.names].flopWith{ |func, name|
	SynthDef(name, func).add
};

//array to store running synths
q.synths = 15.collect{ [] };

//GUIS
q.mpdwin = Window("MPD18 use case (JNCv2)").front;
q.butvals = 0!4!4;

q.buts = 4.collect { |i|
	4.collect {|j|
		Button(q.mpdwin, Rect(i * 80 + 5, 240 - (j * 80) + 5, 75, 75))
		.states_([["up" + (i + 1 + (j * 4)), Color.black], ["DOWN", Color.black, Color.green]]);
	}
}.flop;

q.playButs = q.buts[0][..2];
q.memButs = q.buts[1..].flatten;

q.sl = Slider(q.mpdwin, Rect(340, 25, 40, 280));

q.shifter = q.buts [0][3];
q.shifter.states_([["shift", Color.black], ["SHIFT", Color.black, Color.green]]);

CmdPeriod.add({ q.mpdwin !? _.close });

//IO actions
//creating, setting frequency and stopping synths

//IO{ } is essentially the same as { } and it is only used
//to delay execution of the action until reaching the end
//of the event graph
//If an event stream is carrying IO but is not actually registered for output
//using .enOut, then that action will not be performed. This is also useful
//for dynamic event switching where we might want to use an event stream only at
//some times

q.startSynths = { |i, freqs, sources|
	IO{
		q.synths[i] = [sources[i], freqs].flopWith{ |j, freq|
			Synth(q.names[j], [\freq, freq.linlin(0.0,1.0,300,2000)])
		};
	}
};

q.stopSynths = { |i|
	IO{ q.synths[i].do(_.release); q.synths[i] = [] }
};

q.setFreq = { |i, freqs|
	IO{ [q.synths[i], freqs].flopWith{ |s,v| s.set(\freq,v.linlin(0.0,1.0,300,2000)) } }
};

//soft set

//takes an array of event streams and a value for the distance
//necessary for accepting an incoming event
//based on the algorithm on the SoftSet
//uses a recursive relation since 'checked' depends on 'outSig',
//'outES' depends on 'checked', and 'outSig' depends on 'outES'.
//the recursive relation allows us to use the last outputted value
//to determine the next value.
q.softset = { |es, delta = 0.1|
	var outSig;
	var checked =  { |e|
		var eSig = e.hold(0.0);
		({ |last, new|
			var current = outSig.now;
			if( (absdif(current, last) < delta) || (absdif(current, new) < delta) ){Some(new)}{None()}
		} <%> eSig <@> e).selectSome
	};
	var outES = es.collect(checked).mreduce;
	outSig = outES.hold(0.0);
	outES
};

//FRP
/*
Notes:

The logic is essentially separated into two parts, the copy logic and the logic for each pad.
Both of them can essentially be tested separatedelly from everything else

The logic for each pad is essentially also separate from one pad to another, with the exception
that there is a recursive relationship to the last outputted frequencies of each pad
since the copy mechanism needs access to those when copying.

The last value of the freqs for each pad
is stored in a variable freqArraySig, which is declared up front, so that it can be used
half-way through the pad function before it is event assigned to anything (assignment of that
variable is last thing done in the FRP code).

ENdef works like Ndef for FRP.

ENdef(\x, {  }) //set frp network
ENdef(\x).start //start processing events
ENdef(\x).stop //stop processing events
ENdef(\x).clear //remove all actions

*/
ENdef(\x,{

	var freqArraySig; //declare now for recursive use

	//*** declare all the input event streams (and signals) ***
	//button value are converted to boolean since we will be doing checks on their value mostly
	var flatPads = q.buts.flat;
	var allButtonsESs = (flatPads[0..2]++flatPads[4..]).collect{ |x| x.enInES.collect(_.booleanValue) };

	//the first 3 buttons are the play buttons
	var playButsESs = allButtonsESs[..2];
	//the last 12 are the mem buttons
	var memButsESs = allButtonsESs[3..];

	var shiftES = q.shifter.enInES.collect(_.booleanValue);
	var shiftSig = shiftES.hold(false);
	//pads are only interested on when shift is not pressed so we create that signal here
	//for convenince
	var shiftSigNot = shiftSig.collect(_.not);

	var sliderES = q.sl.enInES;
	var sliderSig = sliderES.hold(0);

	//*** copying logic ***

	// copyFromTo is constructed by running a state function
	// each button press is associated with a specific function
	// to alter the current state.

	// The is state is T( [Int], Option Int)

	// Option Int can be Some(4) or Nothing()

    // The state means copy settings from play buttons with
	// indexes in array in first element of tuple
	// to membutton index stored in Option ( Some(index) ).
	// the data is only ready when whe get a Some on the second element
	// of the tuple.

	var copyFromTo =
	// merge all button pressing events
	// associating them with state function
	// only let them through when shift is pressed
	(shiftSig.when(                  //only let through pad down presses
		                             //(I.d is the identity function
		                             //so select only let's through true values of pad
		(playButsESs.collect({ |es,i| es.select(I.d).collect{
			//here we declare the state function for this play pad press
			{|state|
				//already made assignment so clean state
				if(state.at2.isDefined) {
					T([i],None())
				}
				//collecting things to assign
				{
					if(state.at1.includes(i).not){
						T(state.at1++[i],None())
					}{
						state
					}
				}
		} } }) ++
		memButsESs.collect({ |es,i| es.select(I.d).collect{
			//here we declare the state function for this mem pad press
			{|state|
				//if we already have a Some in the second element
				if(state.at2.isDefined) {
					//then it means two mem button were pressed in sequence
					//copy description is complete, do nothing
					state
				} {
					//otherwise store the mem button to copy to
					//mem buttons start at index 4
					T(state.at1, Some(i+3) )
				}
	} } })).mreduce )
	//shift is pressed, clean state and start over
	| shiftES.collect{ { T([],None()) } })
	//injectF is what actually runs the state functions
	//first argumetn is initial state
	.injectF( T([],None()) )
	//transform
	//    T([x,y,z],None())  into None()
	//and T([x,y,z],Some(v)) into Some( T([x,y,z], v ) )
	.collect{ |tup|
		tup.at2.collect{ |x| tup.at2_(x) }
	}
	//.enDebug("state")
	//only let through when we have a Some( T([x,y,z], v ) )
	//this means we have a complete copy description
	//selectSome only let's through Some(..) values and unwraps
	//them from the Some()
	.selectSome;
	//.enDebug("swap");
	//copying the settings causes pickup mode

	//sources stores which synthdefs each button is using
	//each button has an array associated since it can
	//play multiple synthdefs at the same time
	var sources = copyFromTo.collect{ |tup|
		{ |state| state[tup.at2] = tup.at1; state }
	}.injectFSig( (0..14).collect({ |x| [x.mod(3)] }) );
	//.enDebug("sources");

	//*** pad press logic ***

	//we can write the logic for each button presss separate from the other buttons
	//we just need to use a recursive definition on freqArraySig, the last value
	//of the frequencies for each pad
	//this is the logic for pad i
	var processOnePad = { |padES, shiftSigNot, sliderES, copyFromTo, sources, i|

		var padSig = padES.hold(false);
		var padWhenShiftOff = when( shiftSigNot, padES);

		//only take values from slider if pad is pressed and shift is not pressed
		//soft set prevents jumps from slider repositiong while pad not pressed.
		var sliderWhenPadOnAndShiftOff = q.softset.( [when( (_&&_).lift.(shiftSigNot, padSig ) , sliderES )] );

		//frequency can be determined by copying from another pad or by setting from slider

		//check from the output of copy logic if the last description was to copy into this pad
		//if so, copy frequencies from the indexes in first element of the copyFromTo tupple
		//only take the first frequency from each array
		//note the recursive dependency on freqArraySig
		var copy = copyFromTo.select{ |t| t.at2 == i}.collect{ |t|
			var currentFreqs = freqArraySig.now;
			currentFreqs[t.at1].collect(_.first)
		};

		//moving slider while pad playing causes all synths in the pad to use same frequency
		//take value from slider and make an array with enough values for the number of synths
		var fromSlider = { |xs, v| v.dup(xs[i].size) } <%> sources <@> sliderWhenPadOnAndShiftOff;
		//there are only two things changing frequency, the slider and the copying, so merge them:
		var freqs = copy | fromSlider;
		var freqsSig = freqs.hold([0.0]);

		//*** outputs: Perform actions ***

		//debug statements for convenience
		//padSig.enDebug("pad "++i);
		//sliderWhenPadOnAndShiftOff.enDebug("sl "++i);
		//freqsSig.enDebug("freq "++i);

		//freq changed set new freq to synth
		freqs.collect{ |fs| q.setFreq.(i,fs) }
		//.enDebug("freq "++i)
		.enOut;

		//pad on -> synth start
		({ |fs, sources, play| q.startSynths.(i, fs, sources) } <%> freqsSig <*> sources <@> padWhenShiftOff.select(I.d) )
		//.enDebug("start "++i)
		.enOut;

		//pad off -> synth stop
		padES.select(_.not).collect{ q.stopSynths.(i) }
		//.enDebug("stop "+i)
		.enOut;

		//return the freqSig to be used by other pads
		freqsSig
	};

	//create array of signals with frequencies for each pad by declaring the
	//event graph for each separate pad
	var freqSigs = allButtonsESs.collect{ |pad, i|
		processOnePad.(pad, shiftSigNot, sliderES, copyFromTo, sources, i)
	};

	//transform array of signals into signal of arrays
	//it's just automatic merging of signals with the
	//array function
	// [ FPSignal [Float] ] -> FPSignal [ [ Float ] ]
	// this is the signal that will be used recursivelly
	// to determine itself since freqArraySig depends on
	// each 'freqSig' of each pad, which depends on 'copy'
	// which depends again on 'freqArraySig', creating a cycle.
	freqArraySig = freqSigs.sequence;

}).start
)

//close gui and remove all actions
(
q.mpdwin.close;
ENdef(\x).clear;
)