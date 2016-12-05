/*
\poll

An UMap that posts incoming values in the 'post' window. The value itself is passed through unchanged.

value: the value to be posted
trigger: a trigger causes the value to be posted
onChange: when true, the value will be posted whenever it changes (can result in many posts)
speed: number of times per second to post the value automatically (default 0 - no automatic posting).

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \poll, { |value = 0.0|
	var speed, trigger, change, onChange, mappedVal;
	trigger = \trigger.tr( 1 );
	onChange = \onChange.kr( 0 );
	speed = \speed.kr( 0 );
	change = HPZ1.kr( value ).abs > 0;
	change = (HPZ1.kr( change ).abs > 0) + (TDuty.kr( 0.1, change ) * change);
	change = change * onChange;
	Poll.kr( Impulse.kr( speed ) + trigger + change, 
		\u_spec.asSpecMapKr( value ),
		"poll" );
	UMapOut.kr(value);
})
.setSpec( \onChange, BoolSpec( false ) )
.setSpec( \speed, [0,20,\lin,0,0].asSpec )
.setSpec( \trigger, TriggerSpec( ) )
.mappedArgs_([ \value ])
.category_( 'utility' )