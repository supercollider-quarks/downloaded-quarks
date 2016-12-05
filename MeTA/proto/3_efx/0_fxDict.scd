/*
selection of filters to be used in MasterFX and ProxyChain.
*/

q.utils.addFx(
	'ring',
	{ arg in, ringfr=100;
	in * SinOsc.ar(ringfr)
	}, (

	)
);

q.utils.addFx(
	'in',
	{ arg in, level=1, llag=0.2, noisefloor=0.01;
		LeakDC.ar( in * level.lag(llag) )
		+ BrownNoise.ar(0.0001).madd(noisefloor ! q.numChans);
	},
	(
		\level: [0, 20, 5],
		\llag: [0, 600, 8]
	)
);

q.utils.addFx(
	'dbComp',
	{ arg in, xFreq=400, thresh=0.5, loGain=2, hiGain=0.5, compRatio=2;
		DualBandComp.ar(
			in,
			xFreq, loGain,  hiGain, thresh, compRatio
		)
	},
	(
		\xFreq: \freq,
		\loGain: ControlSpec(0, 10, \amp, 0, 1),
		\hiGain: ControlSpec(0, 10, \amp, 0, 1),
		\compRatio: [1, 10, \exp, 0.1, 3]
	)
);

q.utils.addFx(
	'compander',
	{ arg in, thresh=0.5, slopLo=0.9, slopHi=0.5;
		Compander.ar(
			in, in,
			thresh, slopLo, slopHi,
			0.03, 0.05
		)
	},
	(
		\thresh: \amp,
		\slopLo: [1/10, 10, \exp, 0.0],
		\slopHi: [1/10, 10, \exp, 0.0]
	)
);



q.utils.addFx(
	'gate',
	{|in, threshG = 0.03, attG=0.01, decG=1|
		Compander.ar(
			in, in,
			threshG, 10, 1,
			attG, decG
		)
}, (
		\threshG: \amp,
		\attG:    [0.0025, 0.2, \exp],
		\decG:    [0.5, 20, \exp]
));

q.utils.addFx(
	'freqBal',
	{ arg in, bFreq=200, pan=0;
		var split, lo;
		pan = LinExp.kr(pan, -1,1, 1/20,20);
		lo = LPF.ar(in, bFreq);
		split = [lo, in-lo] * [ pan.reciprocal, pan ];
		split.sum;
	},
	(
		\bFreq: \freq
	)
);

q.utils.addFx(
	'onePolBal', { arg in, coeff= 0.6, balnZ = 0.5;
		var split, lo;
		lo = OnePole.ar(in, coeff);
		split = [lo, in-lo] * [ balnZ.reciprocal, balnZ ];
		split.sum;
	},
	(
		\coeff: [0, 0.99999, -7.5, 0.00001],
		\balnZ: [1/12, 12, \exp, 0, 0]
	)
);

q.utils.addFx(
	'delay', { arg in, delayTime1 = 1, delayTime2 = 0.76, mod=0.66, feedback=3;
		var dtKr, comb, tempo;
		tempo = 1;   // quarter tempo..
		dtKr = [delayTime1, delayTime2 ].lag(1.0);
		comb = CombL.ar(
			HPF.ar(in, 200),
			6,
			dtKr * ( 1 - LFDNoise3.kr(mod.sqrt!2 * 4, 0.8 * mod).cubed ),
			dtKr * feedback,
			0.8
		);

		Rotate2.ar(comb[0], comb[1], SinOsc.kr(0.025)) + in;
	},
	(
		\feedback: [1, 100, \exp, 0.1],
		\delayTime1: [0.005, 6, \exp],
		\delayTime2: [0.005, 6, \exp],
		\mod: [0, 1, \lin]
	)
);


q.utils.addFx(
	'adcVerb', {| in, predelay=0.048, revTime=3, combScale=1, apScale=1, mod=0.05 |
		var sig, y, z;
		AdCVerb.ar( in, revTime, LFNoise1.kr(0.2!2,0.1*mod, 0.11),  2, predelay,
			8, 4, 0.6, combScale = 1, apScale = 1);

	}
	,
	(
		\combScale: \amp,
		\apScale: \amp,
		\predelay: [0.0001, 1, \linear]
	)
);

q.utils.addFx(
	'thrasher', { arg in, bit = 7, downsample= 6000, downsmod=0.75;
		var zin, zout;
		zin = in;
		zout = zin.round(2.pow(bit).reciprocal);
		zout = downsample<22050 *
		Latch.ar(
			zout,
			Impulse.ar(
				downsample
				* LFDNoise1.kr(
					downsmod.cubed * 75 ! 2,
					0.25, 0.75
				).abs
			)
		)
		+ (downsample >= 22050 * zout)
	}
	,
	(
		\bit: [16, 2, \lin, 1],
		\downsmod: [0, 1, \lin],
		\downsample: [q.server.sampleRate ? 44100 /2, 100, \lin],
	)
);

q.utils.addFx(
	'ixiDist', { |in, pregain=2, dFreq= 4000, postgain=1, loSlop=0.6 |
		var sig, sigtocomp, fx, y, z;
		sig = in;
		sigtocomp = LPF.ar((sig * pregain).distort, dFreq, postgain).distort;
		fx = Compander.ar(sigtocomp, sigtocomp, 1, loSlop, 1 );
		LeakDC.ar(fx, mul: 0.5 );
	}
	,
	(
		\dAmp: \amp,
		\dFreq: \freq,
		\pregain: [0.01, 20, \linear],
		\postgain: [0.01, 20, \linear],
		\loSlop: [1, 0.0, \linear, 0, 1],
	)
);

q.utils.addFx(
	'ixiVerb', {| in, predelay=0.048, combdecay=5, apDecay=1, ixivAmp=0.1 |
		var sig, y, z;
		sig = in.madd(ixivAmp).rotate(-1);
		z = DelayN.ar(sig, 1, predelay);
		y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) }).scramble);
		6.do({ y = AllpassN.ar(y.rotate(1), 0.050, rrand(0.03, 0.05), apDecay) });
		y+in;
	}
	,
	(
	)
);

q.utils.addFx(
	'roto', { arg in, speed = 0.02;
		var sins, n = q.numChans;
		sins = LFSaw.ar(speed, 0, 1, (1..n)-1*2/n * 0.67 );
		(({ arg i; PanAz.ar(n, in[i], sins[i], 1, 1.9) } ! n)).sum;
	}
	,
	(
		speed: [0, 20, \exp]
	)
);


q.utils.addFx(
	'endFilt', { arg in, higCut=15000, lowCut=40;
		var l, h;
		l = LPF.ar( in, lowCut);
		h = LPF.ar( in, higCut);
		h-l;
	},
	(
		\lowCut: \freq.asSpec,
		\higCut: \freq.asSpec
	)
);


q.utils.addFx(
	'ampFin', { |in, limDrive=1, ampLimit=0.9, postAmp=1 |
		var st = Limiter.ar(in * limDrive, ampLimit) * postAmp;
		LeakDC.ar(st)
},  ());



