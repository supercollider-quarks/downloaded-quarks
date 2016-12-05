/*
\lfo_step_noise

Creates an UMap for generating step noise. Random values are chosen on time intervals.

time: the time between the steps in seconds, minimum and maximum
type: the type of transitions; step or line:
	0: step or sample-and-hold noise; hard jumps at each value change
	1: linear interpolation
seed: random seed (positive whole number). The same seed will always result in exactly the signal on any computer.
range: the output range

The 'range' arg is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lfo_step_noise, { |time = #[0.1,1], type = 0, seed = 12345, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir(seed);
	sig = DemandEnvGen.kr( Dwhite( *range ), Dwhite( 0,1 ).linexp(0,1, *time), type );
	UMapOut.kr(sig);
})
	.setSpec( \type, ListSpec([0,1], 0, [\step, \linear]) )
	.setSpec( \time, RangeSpec(0.001,100, warp: \exp, default: [0.1,1]) )
	.setSpec( \seed, PositiveIntegerSpec(12345), \init )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
