/*
Example made by Tijs Ham
NOTE - There are a few preparations to be made first:
Go to the KORG EDITOR

Switch the 'Control -> Common -> LED Mode' to External
All of the 'S' and 'M' buttons should operate as 'toggle'
All of the other buttons should operate as 'momentary'

Execute the code below

Press the 'Play' button to start the sound
Press the 'Stop' button to stop the sound

The track arrows increase or decrease the bpm
The marker arrows increase or decrease the lag

The knobs influence frequency and duration
The faders influence modulation and filter cutoff

When the 'S' buttons are 'on' the frequency is send through
When the 'M' buttons are 'on' the modulation is send through

Hold the 'R' buttons while changing the knobs to change duration
Hold the 'R' buttons while changing the faders to change filter cutoff
*/

s.waitForBoot {
	{
		// find devices
		MKtl.find;
		// open the nanoKontrol2
		n = MKtl('kn2', "*nanokontrol2");
		// reset all the actions
		n.resetActions;
		// syncing the server
		s.sync;
		// setting global variables
		~bpm = 600;
		~freq = Array.fill(8, {220});
		~phfreq = Array.fill(8, {8});
		~dur = Array.fill(8, {0.1});
		~cut = Array.fill(8, {1200});
		~lag = 0.005;

		// keep states here in 2d array
		~onOff = Array.fill2D(3,8,{0});

		// using an Ndef to prepare a sound
		Ndef(\sound, {|freq=200, phfreq=20, lagg=0.01, dur=0.1, t_trig=0, cut=1200|
			GVerb.ar(BLowPass.ar(SinOsc.ar([freq.lag(lagg), freq.lag(lagg)+SinOsc.ar(2)],
				LFSaw.ar([phfreq.lag(lagg) + SinOsc.ar(2.01),phfreq.lag(lagg)], 0, 2pi, 2pi),
				EnvGen.ar(Env.perc(0.001, dur, 0.5, -8), t_trig)), cut.lag(0.1), 0.2),
			50, 4, 1, 0.1, 80, 1, 0.1, 0.1 ,mul: 0.1)
		});

		//storing the state of the top rows of buttons in a 2d array
		3.do {|i| 8.do {|j|
			n.elAt( \bt, i, j ).action = {|el|
				//	n.elAt( \bt, i, j ).value_( el.value );
				~onOff[i][j] = el.value;
			};
		}};
		// Tdef that is the main algorithm for the sequenser
		Tdef(\seq, {
			// looping this
			inf.do{|i|
				//setting the lagtime and triggering the envelope
				Ndef(\sound).set(\lagg, ~lag);
				Ndef(\sound).set(\t_trig, 1);
				case(
					// actions at the first step
					{i%8 < 1},
					{
						n.elAt( \bt, 2, 0 ).value_( 1 );
						n.elAt( \bt, 2, 7 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[0]);
						Ndef(\sound).set(\cut, ~cut[0]);
						if(~onOff[0][0] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[0]);
						});
						if(~onOff[1][0] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[0]);
						});
					},
					// actions at the second step
					{i%8 < 2},
					{
						n.elAt( \bt, 2, 1 ).value_( 1 );
						n.elAt( \bt, 2, 0 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[1]);
						Ndef(\sound).set(\cut, ~cut[1]);
						if(~onOff[0][1] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[1]);
						});
						if(~onOff[1][1] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[1]);
						});
					},
					// actions at the third step
					{i%8 < 3},
					{
						n.elAt( \bt, 2, 2 ).value_( 1 );
						n.elAt( \bt, 2, 1 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[2]);
						Ndef(\sound).set(\cut, ~cut[2]);
						if(~onOff[0][2] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[2]);
						});
						if(~onOff[1][2] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[2]);
						});
					},
					// actions at the fourth step
					{i%8 < 4},
					{
						n.elAt( \bt, 2, 3 ).value_( 1 );
						n.elAt( \bt, 2, 2 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[3]);
						Ndef(\sound).set(\cut, ~cut[3]);
						if(~onOff[0][3] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[3]);
						});
						if(~onOff[1][3] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[3]);
						});
					},
					// actions at the fifth step
					{i%8 < 5},
					{
						n.elAt( \bt, 2, 4 ).value_( 1 );
						n.elAt( \bt, 2, 3 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[4]);
						Ndef(\sound).set(\cut, ~cut[4]);
						if(~onOff[0][4] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[4]);
						});
						if(~onOff[1][4] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[4]);
						});
					},
					// actions at the sixth step
					{i%8 < 6},
					{
						n.elAt( \bt, 2, 5 ).value_( 1 );
						n.elAt( \bt, 2, 4 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[5]);
						Ndef(\sound).set(\cut, ~cut[5]);
						if(~onOff[0][5] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[5]);
						});
						if(~onOff[1][5] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[5]);
						});
					},
					// actions at the seventh step
					{i%8 < 7},
					{
						n.elAt( \bt, 2, 6 ).value_( 1 );
						n.elAt( \bt, 2, 5 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[6]);
						Ndef(\sound).set(\cut, ~cut[6]);
						if(~onOff[0][6] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[6]);
						});
						if(~onOff[1][6] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[6]);
						});
					},
					// actions at the eights step
					{i%8 < 8},
					{
						n.elAt( \bt, 2, 7 ).value_( 1 );
						n.elAt( \bt, 2, 6 ).value_( 0 );
						Ndef(\sound).set(\dur, ~dur[7]);
						Ndef(\sound).set(\cut, ~cut[7]);
						if(~onOff[0][7] == 1,
							{
								Ndef(\sound).set(\freq, ~freq[7]);
						});
						if(~onOff[1][7] == 1,
							{
								Ndef(\sound).set(\phfreq, ~phfreq[7]);
						});
					},
				);
				// wait inbetween the steps
				(60/~bpm).wait;
			}
		});

		// assigning actions for the knobs
		n.elAt( \kn, (0..7)).do { |knob, i|
			knob.action = {|el|
				~freq[i] = el.value.linlin(0,1.0, 30, 2500);
				if(~onOff[2][i] > 0.5, {
					~dur[i] = el.value.linlin(0,1.0, 0.001, 15);
				});
			};
		};
		// assigning actions for the sliders
		n.elAt( \sl, (0..7)).do { |sl, i|
			sl.action = {|el|
				~phfreq[i] = el.value.linlin(0,1.0, 6, 250);
				if(~onOff[2][i] > 0.5, {~cut[i] = el.value.linlin(0,1.0, 200, 2000);});
			}
		};
		// assigning actions for the control buttons
		n.elAt( \tr, \tleft ).action = {(~bpm = ~bpm - 5).clip(40, 1000)};
		n.elAt( \tr, \tright ).action = {(~bpm = ~bpm + 5).clip(40, 1000)};
		n.elAt( \tr, \mleft ).action = {~lag = (~lag - 0.01).clip(0.01, 2);};
		n.elAt( \tr, \mright ).action = {~lag = (~lag + 0.01).clip(0.01, 2);};

		n.elAt( \tr, \play ).action = {|el|
			if(el.value > 0.5, {
				Tdef(\seq).play;
				Ndef(\sound).play;
				n.elAt( \tr, \play ).value_(1);
				n.elAt( \tr, \stop ).value_(0);
			});
		};

		n.elAt( \tr, \stop ).action = {|el|
			if(el.value > 0.5, {
				Tdef(\seq).stop;
				Ndef(\sound).stop;
				n.elAt( \tr, \play ).value_(0);
				n.elAt( \tr, \stop ).value_(1);
			});
		};

	}.fork;
};
/*
Server.default.makeGui;
*/