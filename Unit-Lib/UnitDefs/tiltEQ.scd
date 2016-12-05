/*
\tiltEQ

Combined High Shelf and Low Shelf filter. Default settings mimic the Tonelux TILT eq response.

tilt: amount of tilt (in dBs). Positive numbers increase high frequencies.
center: center frequency (default 650)
rs: the reciprocal of S. Shell boost/cut slope for both shelving filters (default 2)
--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \tiltEQ, { |tilt = 0, center = 650, rs = 2|
	var in;
	in = UIn.ar(0,1);
	center = center.clip(20,20000);
	in = BHiShelf.ar( in, center, rs, tilt );
	in = BLowShelf.ar( in, center, rs, tilt.neg );
	UOut.ar( 0, in );
})
.category_( \filter )
.setSpec( \tilt, [-12,12,\lin,0,0].asSpec )
.setSpec( \center, \freq.asSpec.copy.default_(650) )
.setSpec( \rs, [ 0.6, 10, \exp, 0, 1 ].asSpec )
