/*
\trig_random

This UMap implements triggered random value generator. Every time a trigger is received the output value a random value within the set 'range' of the UMap.

trigger: the trigger
range: the range of the value
time: a linear lag time to move to the new value

the 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \trig_random, {
	var lo, hi, trigger, time, output;
	var sig;
	trigger = \trigger.tr;
	#lo, hi = \range.kr([0.0,1.0]);
	time = \time.kr(0.0);
	URandSeed.ir( \seed.kr(12345) );
	output = TRand.kr( lo, hi, trigger );
	output = output.varlag( time );
	UMapOut.kr( output );
})
.mappedArgs_([ \range ])
.setSpec( \trigger, TriggerSpec() )
.setSpec( \seed, PositiveIntegerSpec(12345), \init )
.category_( 'trigger_to_value' )