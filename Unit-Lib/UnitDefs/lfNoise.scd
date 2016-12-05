/*
\lfNoise

A sample-and-hold noise generator. Generates random values at frequency rate.

freq: frequency (2-20000Hz)
amp: amplitude (0-1) of the noise
type: the type of noise:
	0: step or sample-and-hold noise; hard jumps at each value change
	1: linear interpolated noise
	2: cubic interpolated noise
	3: clip noise
	- settings in between these types are also possible, effectively crossfading between them.
seed: random seed (positive whole number). The same seed will always result in exactly the same noise on any computer. If you want decorrelated noise from multiple sources, change the seed on each source to a different number.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \lfNoise, { |freq = 440, amp = 0.1, type = 0, seed = 12345|
	var sig;
	URandSeed.ir(seed);
	sig = [
		LFDNoise0.ar( freq, amp ),
		LFDNoise1.ar( freq, amp ),
		LFDNoise3.ar( freq, amp ),
		LFDClipNoise.ar( freq, amp ),
	];
	sig = SelectX.ar( type, sig );
	UMixOut.ar( 0, sig, 0, true )
} ).category_( \noise )
	.setSpec( \type, [0,2,\lin,0,0].asSpec )
	.setSpec( \freq, FreqSpec(2, 20000, default: 440) )
	.setSpec( \seed, PositiveIntegerSpec(12345) )