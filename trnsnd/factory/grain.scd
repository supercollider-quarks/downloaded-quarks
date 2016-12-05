GrainFactory.make;

// timing test
Pbind(\instrument, \g_sine_hann_pan2, \dur, 0.01, \sustain, 0.015, \pan, [-1, 1], \degree, [0, 0]).play;
Pbind(\instrument, \g_sine_hann_pan2, \dur, 0.01, \sustain, Env([0.0005, 0.02, 0.0005], [3, 5], \exp), \phase, Pfunc{[2pi.rand, 2pi.rand]}, \pan, [-1, 1], \degree, [0, 0]).play;

(
// phase diffusion
p = Pbind(
	\type, \grain,
	\instrument, \g_sine_hann_pan2,
	\phase, Pwhite(0, 2pi),
	\octave, 5,
	\mtranspose, 0,
	\pan, 0,
	\degree, Pstutter(50, Pseries(0, 1, 7).loop),
	\dur, 0.02,
	\legato, 2,
	\db, -20,
).play;
)
(
// phase diffusion
p = Pbind(
	\type, \grain,
	\instrument, \g_sine_hann_pan2,
	\phase, Pbrown(0, 2pi, 0.5pi),
	\octave, 5,
	\mtranspose, 0,
	\pan, 0,
	\degree, Pstutter(50, Pseries(0, 1, 7).loop),
	\dur, 0.02,
	\legato, 2.5,
	\db, -20,
).play;
)
(
// envelope overlap
r = Pbind(
	\instrument, \g_sine_hann_pan2,
	\octave, 5,
	\degree, 1,
	\phase, Pwhite(0, 2pi),
	\dur, 0.02,
	\legato, 2.5,
	\db, -20,
).play;
)

