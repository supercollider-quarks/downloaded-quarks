/*
Example by Tijs Ham

Load an audio file (no mp3!)
Look at the LED's on the nanoKontrol2!
*/

s.waitForBoot{

	MKtl.find;
	n = MKtl('nk2', "*trol2");
	n.resetActions;
	~nrBands = 8;
	~nrLights = 8;
	~lowBand = 50;
	Dialog.openPanel(
		{|path|
			{
				~buf = Buffer.readChannel(s, path, channels:[0]);

				s.sync;

				SynthDef(\band, {|frlow, frhi, id|
					var in, filter, amplitude;
					in = PlayBuf.ar(1, ~buf.bufnum, loop:1);
					filter = BLowPass4.ar(BHiPass4.ar(in, frlow), frhi);
					amplitude = Amplitude.kr(filter);
					SendTrig.kr(Impulse.kr(12), id, amplitude);
				}).add;

				s.sync;

				~bands = Array.geom(~nrBands+1, ~lowBand, (5000/~lowBand).pow(~nrBands.reciprocal));
				~nrBands.collect({|i| Synth(\band, [\frlow, ~bands[i], \frhi, ~bands[i+1], \id, i]);});
				{PlayBuf.ar(1, ~buf.bufnum, loop:1)!2;}.play;
				~threshholds = 	(~nrLights+1).collect({|i| (i+1).pow(4).reciprocal});

				s.sync;

				OSCFunc({|...args|

					case(
						{args[0][2] < 1},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 0 ).value_(0);
									n.elementAt( \bt, 1, 0 ).value_(0);
									n.elementAt( \bt, 0, 0 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 0 ).value_(1);
									n.elementAt( \bt, 1, 0 ).value_(0);
									n.elementAt( \bt, 0, 0 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 0 ).value_(1);
									n.elementAt( \bt, 1, 0 ).value_(1);
									n.elementAt( \bt, 0, 0 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 0 ).value_(1);
									n.elementAt( \bt, 1, 0 ).value_(1);
									n.elementAt( \bt, 0, 0 ).value_(1);
								}
							);
						},
						{args[0][2] < 2},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 1 ).value_(0);
									n.elementAt( \bt, 1, 1 ).value_(0);
									n.elementAt( \bt, 0, 1 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 1 ).value_(1);
									n.elementAt( \bt, 1, 1 ).value_(0);
									n.elementAt( \bt, 0, 1 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 1 ).value_(1);
									n.elementAt( \bt, 1, 1 ).value_(1);
									n.elementAt( \bt, 0, 1 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 1 ).value_(1);
									n.elementAt( \bt, 1, 1 ).value_(1);
									n.elementAt( \bt, 0, 1 ).value_(1);
								}
							);
						},
						{args[0][2] < 3},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 2 ).value_(0);
									n.elementAt( \bt, 1, 2 ).value_(0);
									n.elementAt( \bt, 0, 2 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 2 ).value_(1);
									n.elementAt( \bt, 1, 2 ).value_(0);
									n.elementAt( \bt, 0, 2 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 2 ).value_(1);
									n.elementAt( \bt, 1, 2 ).value_(1);
									n.elementAt( \bt, 0, 2 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 2 ).value_(1);
									n.elementAt( \bt, 1, 2 ).value_(1);
									n.elementAt( \bt, 0, 2 ).value_(1);
								}
							);
						},
						{args[0][2] < 4},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 3 ).value_(0);
									n.elementAt( \bt, 1, 3 ).value_(0);
									n.elementAt( \bt, 0, 3 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 3 ).value_(1);
									n.elementAt( \bt, 1, 3 ).value_(0);
									n.elementAt( \bt, 0, 3 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 3 ).value_(1);
									n.elementAt( \bt, 1, 3 ).value_(1);
									n.elementAt( \bt, 0, 3 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 3 ).value_(1);
									n.elementAt( \bt, 1, 3 ).value_(1);
									n.elementAt( \bt, 0, 3 ).value_(1);
								}
							);
						},
						{args[0][2] < 5},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 4 ).value_(0);
									n.elementAt( \bt, 1, 4 ).value_(0);
									n.elementAt( \bt, 0, 4 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 4 ).value_(1);
									n.elementAt( \bt, 1, 4 ).value_(0);
									n.elementAt( \bt, 0, 4 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 4 ).value_(1);
									n.elementAt( \bt, 1, 4 ).value_(1);
									n.elementAt( \bt, 0, 4 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 4 ).value_(1);
									n.elementAt( \bt, 1, 4 ).value_(1);
									n.elementAt( \bt, 0, 4 ).value_(1);
								}
							);
						},
						{args[0][2] < 6},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 5 ).value_(0);
									n.elementAt( \bt, 1, 5 ).value_(0);
									n.elementAt( \bt, 0, 5 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 5 ).value_(1);
									n.elementAt( \bt, 1, 5 ).value_(0);
									n.elementAt( \bt, 0, 5 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 5 ).value_(1);
									n.elementAt( \bt, 1, 5 ).value_(1);
									n.elementAt( \bt, 0, 5 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 5 ).value_(1);
									n.elementAt( \bt, 1, 5 ).value_(1);
									n.elementAt( \bt, 0, 5 ).value_(1);
								}
							);
						},
						{args[0][2] < 7},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 6 ).value_(0);
									n.elementAt( \bt, 1, 6 ).value_(0);
									n.elementAt( \bt, 0, 6 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 6 ).value_(1);
									n.elementAt( \bt, 1, 6 ).value_(0);
									n.elementAt( \bt, 0, 6 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 6 ).value_(1);
									n.elementAt( \bt, 1, 6 ).value_(1);
									n.elementAt( \bt, 0, 6 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 6 ).value_(1);
									n.elementAt( \bt, 1, 6 ).value_(1);
									n.elementAt( \bt, 0, 6 ).value_(1);
								}
							);
						},
						{args[0][2] < 8},
						{
							case(
								{args[0][3] < 0.001},
								{n.elementAt( \bt, 2, 7 ).value_(0);
									n.elementAt( \bt, 1, 7 ).value_(0);
									n.elementAt( \bt, 0, 7 ).value_(0);
								},
								{args[0][3] < 0.01},
								{n.elementAt( \bt, 2, 7 ).value_(1);
									n.elementAt( \bt, 1, 7 ).value_(0);
									n.elementAt( \bt, 0, 7 ).value_(0);
								},
								{args[0][3] < 0.1},
								{n.elementAt( \bt, 2, 7 ).value_(1);
									n.elementAt( \bt, 1, 7 ).value_(1);
									n.elementAt( \bt, 0, 7 ).value_(0);
								},
								{args[0][3] < 1},
								{n.elementAt( \bt, 2, 7 ).value_(1);
									n.elementAt( \bt, 1, 7 ).value_(1);
									n.elementAt( \bt, 0, 7 ).value_(1);
								}
							);
						}
					);

				},'\tr');

			}.fork;
	},{"cancelled".postln;});
};
