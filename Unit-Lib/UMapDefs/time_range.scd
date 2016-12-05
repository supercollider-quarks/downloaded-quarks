/*
\time_range

Creates an UMap intended for use on modulatable time parameters. This allows for modulation using any of the available one-value UMaps on a specific range of time. It scales a value (0-1) to a range between min and max. Scaling can be set to exponential or linear (default)

time: the time value (can be an UMap) (0-1)
min: the minimum of the range (if time == 0, this will be the output)
max: the maximum of the range (if time == 1, this will be the output)

This UMap can only be inserted and used on SMPTESpec parameters.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \time_range, { |time = 0.5, exponential = 0, min = 1, max = 10|
	time = if( exponential,
		time.linexp(0,1,min,max,\none),
		time.linlin(0,1,min,max,\none)
	).clip(min,max);
	UMapOut.kr( time, false);
})
	.setSpec( \time, [0,1,\lin,0,0.5] )
	.setSpec( \exponential, BoolSpec( false ) )
	.setSpec( \min, SMPTESpec(0.001, (360000 - 0.0001), 1000, 1 ) )
	.setSpec( \max, SMPTESpec(0.001, (360000 - 0.0001), 1000, 10 ) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})
	.canInsert_( false )
	.category_( 'time' );