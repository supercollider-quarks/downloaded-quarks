/*
\add

Creates an UMap adding (+) two values together, with clipping and wrapping functionality.

value: the value to add to
add: the amount to add
clipMode: can be one of the following modes:
	0 - 'clip' (default): clip values outside fromRange
	1 - 'fold': fold values outside fromRange
	2 - 'wrap': wrap around values outside fromRange
	3 - 'none': no clipping applied (values can go outside toRange)
clipSoftness: only used for 'clip' and 'fold' (0-1).

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


UMapDef( \add, { |value = 0.0, add = 0.0, clipMode = 0, clipSoftness = 0.0|
	var sig;
	sig = value + add;
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.softwrap2(1, clipSoftness),
	]);
	sig = sig.linlin(-1,1,0,1);
	UMapOut.kr(sig);
})
	.mappedArgs_([ \value, \add ])
	.setSpec( \clipMode, ListSpec( (..2), 0, [ \clip, \fold, \wrap ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.1].asSpec )
	.category_( 'utility' )