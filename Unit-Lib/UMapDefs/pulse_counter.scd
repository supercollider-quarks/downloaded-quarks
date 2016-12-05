/*
\pulse_counter

This UMapDef implements a pulse counter.

trigger: the trigger to be counted
reset: a trigger resets the count
max: the maximum value to be counted
step: the size of the steps (can be negative too)
startValue: the value from where the counting starts (default value before any pulse is counted)
mode: there are three possible modes:
	\clip - clip the output to the max number
	\wrap - when the max number is reached, jump to zero
	\fold - after the max number is reached, count backwards, and when zero is reached count forwards again
range: the values to which zero and max are mapped.

the 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \pulse_counter, {
	var index, output;
	var trigger, reset, step, n, max, mode, range, startValue;
	trigger = \trigger.tr(0);
	reset = \reset.tr(0);
	n = \n.kr(8);
	max = n-1;
	step = \step.kr(1);
	startValue = \startValue.kr(0);
	mode = \mode.kr(1); // wrap, fold
	range = \range.kr([0,1]);
	index = Stepper.kr( trigger, reset, 0, Select.kr( mode, [max, (max * 2)-1]), step, startValue );
	output = Select.kr( mode, [
		index,
		index.fold(0, max),
	]);
	UMapOut.kr( output.linlin(0,max,*range ) );
})
.mappedArgs_([ \range ])
.setSpec( \trigger, TriggerSpec() )
.setSpec( \reset, TriggerSpec() )
.setSpec( \step,  IntegerSpec(1) )
.setSpec( \n, PositiveIntegerSpec(8,2,16777215) )
.setSpec( \startValue,  PositiveIntegerSpec(0) )
.setSpec( \mode,  ListSpec([0,1],1,[\wrap, \fold]) )
.category_( 'trigger_to_value' )