Osc1 {
	*ar { |buf, dur=1|
		^BufRd.ar(1, buf, Line.ar(0, BufFrames.ir(buf) - 1, dur, doneAction: 2), 0)
	}
	*kr { |buf, dur=1|
		^BufRd.kr(1, buf, Line.kr(0, BufFrames.ir(buf) - 1, dur, doneAction: 2), 0)
	}
}