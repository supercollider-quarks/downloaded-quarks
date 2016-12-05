/*
\onsets

Creates an UMap that listens to an audio bus from a previous unit in the chain and detects the beginning of notes/drumbeats/etc..

bus: the number of the bus from the audio chain(usually 0)
threshold: the detection threshold, typically between 0 and 1
odftype: chooses which onset detection function is used. In many cases the default will be fine. The following choices are available;
	\power
	generally OK, good for percussive input, and also very efficient
	\magsum
	generally OK, good for percussive input, and also very efficient
	\complex
	performs generally very well, but more CPU-intensive
	\rcomplex
	performs generally very well, and slightly more efficient than \complex
	\phase
	generally good, especially for tonal input, medium efficiency
	\wphase
	generally very good, especially for tonal input, medium efficiency
	\mkl
	generally very good, medium efficiency, pretty different from the other methods
relaxtime: specifies the time (in seconds) for the normalisation to "forget" about a recent onset. If you find too much re-triggering (e.g. as a note dies away unevenly) then you might wish to increase this value.
floor: floor is a lower limit, connected to the idea of how quiet the sound is expected to get without becoming indistinguishable from noise. For some cleanly-recorded classical music with wide dynamic variations, I found it helpful to go down as far as 0.000001.
mingap: specifies a minimum gap (in FFT frames) between onset detections, a brute-force way to prevent too many doubled detections.
medianspan: specifies the size (in FFT frames) of the median window used for smoothing the detection function before triggering.
range: the UMap's output range. Triggers will cause jumps from the minimum to the maximum of this range.
fftsize: the size of the fft buffer used for the analisys. A smaller buffer will result in quicker but less accurate detection.

The 'range' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \onsets, { |bus = 0, 
	filter = #[20,20000], 
	threshold = 0.5,
	odftype = 3,
	relaxtime = 1.0,
	floor = 0.1, 
	mingap = 10,
	medianspan = 11,
	range = #[0.0,1.0]|
	var sig, fft, onsets, fftsize;
	fftsize = \fftsize.ir( 512 );
	sig = In.ar(bus + UIn.firstPrivateBus);
	sig = BLowCut.ar( sig, filter[0], 2 );
	sig = BHiCut.ar( sig, filter[1], 2 );
	fft = FFT( LocalBuf( fftsize, 1 ), sig );
	onsets = Onsets.kr( fft, threshold, \rcomplex, relaxtime, floor, mingap, medianspan );
	onsets = onsets.linlin(0,1,*range);
	UMapOut.kr(onsets);
})
	.setSpec( \filter, \freq.asSpec.asRangeSpec )
	.setSpec( \threshold, [0,1,\lin,0,0.5].asSpec )
	.setSpec( \odftype, ListSpec([0,1,2,3,4,5,6],3,
		[\power, \magsum, \complex, \rcomplex, \phase, \wphase, \mkl]) )
	.setSpec( \relaxtime, [0.1,10,\exp,0,1].asSpec )
	.setSpec( \floor, [0.00001,0.1,\exp,0,0.1].asSpec )
	.setSpec( \mingap, [1,100,\lin,1,10].asSpec )
	.setSpec( \medianspan, [1,100,\lin,1,11].asSpec )
	.setSpec( \fftsize, ListSpec([512,1024],0), \init )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \range ])
	.category_(\input);

