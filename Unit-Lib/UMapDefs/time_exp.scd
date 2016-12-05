/*
\time_exp

Creates an UMap intended for use on modulatable time parameters. This allows for modulation using any of the available one-value UMaps on a specific range of time. It scales a value exponentially between 0.1 and 10 to a specific time range. Ranges vary from 0.01 seconds to 1 hour.

time: the time value (can be an UMap)
timeScale: the amount to scale by

This UMap can only be inserted and used on SMPTESpec parameters.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \time_exp, { |time = 1, timeScale = 1|
	UMapOut.kr(time * timeScale, false);
})
	.setSpec( \time, [0.1,10,\exp,0,1] )
	.setSpec( \timeScale, ListSpec(
		[0.001, 0.01,0.1,1,10,60,600,3600],1,
		["1ms / 0.001s", "0.01s", "0.1s", "1s", "10s", "1m / 60s", "10m / 600s", "1h / 3600s" ]
	) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})
	.canInsert_( false )
	.category_( 'time' );