Factory.make("test", Vco.sine, Vcf.acomp, Vca.asr);
Lemur.iPhone(\freq, z1: {a=Synth("test",[amp:Cdef(\y).asMap, freq:Cdef(\x).asMap])}, z0:{a.release});

// airzero.local works
~lemur = NetAddr("yPad2.local", 8000);

// button
~lemur.sendMsg('/b0', '@label_off', "hello".scramble);
~lemur.sendMsg('/b0', '@color', Lemur.lightBlue, 8355711);
// pad
Factory.make("hi", Vco.fsine, nil, Vca.asr);
Pad(1, "hi", [freq: 400, amp: 0.1], -0.3);

OSCdef(\panic, { |msg| if(msg[1]+msg[2]==2, { s.freeAll }) }, '/panic/x');
OSCdef(\mute, { |msg| if(msg[1]==1, { s.mute }, { s.unmute }) }, '/servermute/x');
Pbind.new.play;


{ nil.makeError }.try({ |e| ~lemur.sendMsg('/post', '@content', e.errorString) })
~lemur.sendMsg('/post', '@content', "x");
// crazy!
c = Color.blue;
h = c.asHSV;


OSCdef(\x, { |k| ~lemur.sendMsg('/level/x', k[3]) }, '/amp');
{SendPeakRMS.kr(SoundIn.ar, 30, 3, '/amp')}.play;

//
MIDIIn.connectAll;
(
QuNexus.initPoly16(
	nil,
	nil,
	{ |node,n,v,c| [n,v,c].postln }
);
)