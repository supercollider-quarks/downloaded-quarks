// func
F {
	// freq -> lambda (wavelength)
	*f2l { |freq=440| ^(K.speedOfSound / freq) }

	// a/v control rate
	*oFrate {
		^if(Server.default.serverRunning, {
			Server.default.sampleRate / K.oFblock
			}, {
			Error("server not running").throw
		});
	}
	*dir { ^thisProcess.nowExecutingPath.dirname }

	// spread array
	// for pan2
	*spread2 { |size|
		size = size - 1;
		^((0 .. size) * (2 / size) - 1)
	}
	// for pan4
	*spread4 {}
}
// const
K {
	*speedOfSound { ^344 } // meters per sec
}
// tempo
T {
	classvar t;
	*initClass { Class.initClassTree(TempoClock); t = TempoClock.default }
	// Mälzel's Metronome
	*mm_ { |mm| t.tempo = mm/60; ^mm/60 }
	*mm { ^t.tempo*60 }
	*dur { ^t.beatDur }
}