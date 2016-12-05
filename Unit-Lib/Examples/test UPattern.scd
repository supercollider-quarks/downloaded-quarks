~pat = UPattern( \sine, \output ); // create an UPattern

~pat.gui; // show GUI (sustain and timeToNext are not yet shown)

// make random start times and durations
~pat.sustain = UMap( \random_value, [ \range, [0.05,0.5] ] );
~pat.timeToNext = UMap( \random_value, [ \range, [0.05,0.5] ] );

// single values for regular times
~pat.sustain = 0.1; 
~pat.timeToNext = 0.1;

// define some UPatDefs
(
UPatDef( \p_brown, { |unit, lo = 0.0, hi = 1.0, step = 0.125|
	Pbrown( lo, hi, step, inf ).asStream;
}).mappedArgs_([\lo, \hi]).category_( 'pattern' );

UPatDef( \p_step, { |unit, lo = 0.0, hi = 1.0, steps = 12|
	{
		var current = 0;
		current.linlin( 0, 1, lo.next, hi.next ).yield;
		inf.do { |i|
			current = (current + ((1/steps.next).clip2( 1.0e12 ))).wrap( 0.0, 1.0 );
			current.linlin( 0, 1, lo.next, hi.next ).yield;
		};
	}.r;
}).mappedArgs_([\lo, \hi])
.setSpec( \steps, [-100,100,\lin].asSpec )
.category_( 'pattern' );
);

// various assignments for freq of sine wave
~pat[0].freq = [ \p_brown, [ \lo, 440, \hi, 880, \step, 0.1 ] ]; 

~pat[0].freq = [ \p_step, [ \lo, 440, \hi, 880, \steps, 12 ] ];

// can be nested too
~pat[0].freq.lo = [ \p_step, [ \lo, 220, \hi, 660, \steps, 2 ] ];

// defs can be changed during playback
~pat.prepareAndStart;

~pat.stop;

~pat.duration = 5; // limit duration
~pat.repeats = 5; // limit max repeats

~pat.duration = inf; // don't limit duration
~pat.repeats = inf; // don't limit max repeats