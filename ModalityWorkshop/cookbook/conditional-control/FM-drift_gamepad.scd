m = MKtl(\gp, 'hid_0_run_n_drive');


m.trace(true)
m.trace(false)


(
s.latency = nil;
s.boot;
NdefMixer(s);
)

(
Ndef(\fSine).addSpec(\freqDrift, [-1, 1]);
Ndef(\fSine).addSpec(\fb, [-pi, pi]);
Ndef(\fSine).addSpec(\mFb, [-pi, pi]);
Ndef(\fSine, {
	var freq, mFreq, mAmp, src;

	freq = Integrator.kr(\freqDrift.kr(0) * 0.001).linlin(-1, 1, 30, 68, clip: \none).midicps;
	freq = [0.719724, 0, 0.235] + freq;
	mFreq = freq * (
		Integrator.kr(\mIdxDrift.kr(0) * 0.001).linlin(-1, 1, 0, 20, clip: \none));
	mAmp = Integrator.kr(\mAmpDrift.kr(0) * 0.001).linlin(-1, 1, 0, 1000, clip: \none);


	Splay.ar(SinOscFB.ar(freq + (SinOscFB.ar(mFreq, \mfb.kr(0.1)) * mAmp), \fb.kr(0.1)))
}).play;
)

(
Ndef(\verb).addSpec(\t60, [0.1, 60, \exp]);
Ndef(\verb).addSpec(\damp, [0, 1]);
Ndef(\verb).addSpec(\size, [0.5, 3]);
Ndef(\verb).addSpec(\earlydiff, [0, 1]);
Ndef(\verb).addSpec(\mdepth, [0, 50]);
Ndef(\verb).addSpec(\mfreq, [0, 10]);
Ndef(\verb).addSpec(\lowx, [0, 1]);
Ndef(\verb).addSpec(\midx, [0, 1]);
Ndef(\verb).addSpec(\highx, [0, 1]);
Ndef(\verb).addSpec(\lowband, [100, 6000, \exp]);
Ndef(\verb).addSpec(\highband, [1000, 10000, \exp]);

Ndef(\verb, {
	JPverb.ar(Ndef(\fSine).ar,
		\t60.kr(1),
		\damp.kr(0),
		\size.kr(1),
		\earlydiff.kr(0.707),
		\mdepth.kr(0.1),
		\mfreq.kr(0.0234),
		\lowx.kr(1),
		\midx.kr(1),
		\highx.kr(1),
		\lowband.kr(500),
		\highband.kr(2000)
	)
});
)




q = ();


(
q.activated = m.elementAt(\bt, 1); // activate sound

m.elementAt(\joy, \l, \y).action_{|el|
		(q.activated.value > 0).if{
		Ndef(\fSine).set(\freqDrift, el.value.neg + 0.502)
	};
};
m.elementAt(\joy, \r, \y).action_{|el|
		(q.activated.value > 0).if{
		Ndef(\fSine).set(\mIdxDrift, el.value.neg + 0.502)
	};
};
m.elementAt(\joy, \r, \x).action_{|el|
		(q.activated.value > 0).if{
		Ndef(\fSine).set(\mAmpDrift, el.value.neg + 0.502)
	};
};
m.elementAt(\throt, \r).action_{|el|
		(q.activated.value > 0).if{
		Ndef(\fSine).setUni(\fb, el.value)
	};
};

q.activated.action_{|el|
	(el.value < 1).if{ // up
		Ndef(\fSine).set(\freqDrift, 0);
		Ndef(\fSine).set(\mAmpDrift, 0);
		Ndef(\fSine).set(\mIdxDrift, 0);
	}


}

)







(
m.elementAt(\throt, \l).action_{|el|

	[q.sounds, q.activated.collect(_.value)].flop.do{|a|
		(a[1] > 0).if{
			a[0].setUni(\amp, el.value)
		}
	};

};
m.elementAt(\joy, \l, \y).action_{|el|
	[q.sounds, q.activated.collect(_.value)].flop.do{|a|
		(a[1] > 0).if{
			a[0].setUni(\freq, el.value.linlin(0, 1, 1, 0))
		}
	};
};
m.elementAt(\joy, \l, \x).action_{|el|
	[q.sounds, q.activated.collect(_.value)].flop.do{|a|
		(a[1] > 0).if{
			a[0].setUni(\modIndex, el.value)
		}
	};
};
m.elementAt(\throt, \r).action_{|el|
	[q.sounds, q.activated.collect(_.value)].flop.do{|a|
		(a[1] > 0).if{
			a[0].setUni(\modAmp, el.value)
		}
	};
};
m.elementAt(\joy, \r, \x).action_{|el|
	[q.sounds, q.activated.collect(_.value)].flop.do{|a|
		(a[1] > 0).if{
			a[0].setUni(\spread, el.value)
		}
	};
};
)