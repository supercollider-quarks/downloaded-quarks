/*
\gate

This UMap implements a gating process. It allows a modulating input value to pass when gate is on, otherwise holds last value.

value: the value or UMap to sample
gate: if true (on), the signal is passed through, if false the signal is kept at the last value
the value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \gate, {
	var value, gate, output;
	value = \value.kr(0.0);
	gate = \gate.kr( 1 );
	output = Gate.kr( value, gate);
	UMapOut.kr( output );
})
.mappedArgs_([ \value ])
.setSpec( \gate, BoolSpec( true ) )
.category_( 'utility' )