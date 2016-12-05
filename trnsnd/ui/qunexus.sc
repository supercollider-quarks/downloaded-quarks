QuNexus {
	// use chan to index node array if poly-bend (16 chan) is needed
	// otherwise note numbers (128)
	*new { |onFunc offFunc polyFunc bendFunc|
		var node = Array.newClear(128);
		onFunc = onFunc ? { |node n v| node[n] = Synth(\default, [freq: n.midicps, amp: v]) };
		offFunc = offFunc ? { |node n| node[n].release };
		MIDIFunc.noteOn { |vel note chan|
			onFunc.(`node, note, vel/127, chan);
		};
		MIDIFunc.noteOff { |vel note chan|
			offFunc.(`node, note, vel/127, chan);
		};
		MIDIFunc.polytouch { |val note chan|
			polyFunc.(`node, note, val/127, chan);
		};
		MIDIFunc.bend { |val chan|
			bendFunc.(`node, val/16256, chan);
		};
	}
}