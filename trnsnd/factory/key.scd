MIDIIn.connectAll;
KeyFactory.make;
QuNexus({ |node n v| node[n] = Synth(\key_sine, [freq: n.midicps, amp: v*0.3]) }, { |node n| node[n].release });
{SinOsc.ar(40)}.play;
{SinOsc.ar(40)}.play(outbus: 1);
s.scope