TuningLib
=========

the development version of the Supercollider Quark of the same name






Here is a talk I gave about the quark in 2013:



##Picking Musical Tones

One of the great problems in electronic music is picking pitches and tunings.

The TuningLib quark helps manage this process.
















First, there is some Scale stuff already in SuperColider.

How to use a scale in a Pbind:
```
(
s.waitForBoot({
	    a = Scale.ionian;

	    p = Pbind(
		        \degree, Pseq([0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1, 0, \rest], 2),
		        \scale, a,
		        \dur, 0.25
	    );

	    q = p.play;
})
)
```









#Key

Key tracks key changes and modulations, so you can keep modulating or back out of modulations:
```
k = Key(Scale.choose);
k.scale.degrees;
k.scale.cents;
k.change(4); // modulate to the 5th scale degree (we start counting with 0)
k.scale.degrees;
k.scale.cents;
k.change; // go back

k.scale.degrees;
```
This will keep up through as amny layers of modulations as you want.  These modulations keep track of transpositions in any tuning system. This means, that if you modulate to 3/2 in Just Intonation, it will do all the necessary multiplcations for you. This is compatible with any tuning system.

It also does rounding:

`quantizeFreq (freq, base, round , gravity )`

Snaps the feq value in Hz to the nearest Hz value in the current key

gravity changes the level of attraction ot the in tune frequency.

`k.quantizeFreq(660, 440, \down, 0.5) // half way in tune`

By changing gravity over time, you can have pitches tend towards being in or out of tune.








#Scala

There is a huge library of pre-cooked tunings for the scala program. ( at http://www.huygens-fokker.org/scala/scl_format.html
) This class opens those files.
```
a = Scala("slendro.scl");
b = a.scale;
```








#Diamond
(formerly Lattice)

This is a partchian tuning diamond 

`d = Diamond([ 2, 5, 3, 7, 9])`

The array is numbers to use in generated tuning ratios, so this gives:

* 1/1 5/4 3/2 7/4 9/8   for otonality
* 1/1 8/5 4/3 8/7 16/9  for utonality

* otonality is overtones – the numbers you give are in the numerator
* utonality is undertones – the numbers are in denominator

all of the other numbers are powers of 2.  You could change that with an optional second argument to any other number, such as 3:

`d = Diamond([ 2, 3, 5, 7, 11], 3)`




Diamonds also generate a table:

|      |     |      |     |     |
|------|-----|------|-----|-----|
| 1/1  |5/4  | 3/2  |7/4  | 9/8 |
| 8/5  |1/1  | 6/5  |7/5  | 9/5 |
| 4/3  |5/3  | 1/1  |7/6  | 3/2 |
| 8/7  |10/7 | 12/7 |1/1  | 9/7 |
| 16/9 |10/9 | 4/3  |14/9 | 1/1 |

It is posisble to walk around this table to make nice triads that are harmonically related:
```
(
s.waitForBoot({

	var dia, orientation, startx, starty, baseFreq;

	SynthDef("sine", {arg out = 0, dur = 5, freq, amp=0.2, pan = 0;
		var env, osc;
		env = EnvGen.kr(Env.sine(dur, amp), doneAction: 2);
		osc = SinOsc.ar(freq, 0, env);
		Out.ar(out, osc * amp);
	}).add;

	s.sync;


	dia = Diamond.new;
	orientation = true;
	startx = 0;
	starty = 0;
	baseFreq = 440;

	Pbind(
		\instrument, \sine,
		\amp, 0.3,
		\freq, Pfunc({
			var starts, result;
			  orientation = orientation.not;
			  starts = dia.d2Pivot(startx, starty, orientation);
			  startx = starts.first;
			  starty = starts.last;
			result = dia.makeIntervals(startx, starty, orientation);
			(result * baseFreq)
		})
	).play
})
)

```

(Somewhat embarassingly, I got confused between 2 and 3 dimensions in a previous version. This is now fixed.)











#DissonanceCurve

This is not the only quark that does dissonance curves in SuperCollider.





Dissonance curves are used to compute tunings based on timbre, which is to say the spectrum.




```
d = DissonanceCurve([440], [1])
d.plot
```

The high part of the graph is highly dissonant and the low part is not dissonant.  This is for just one pitch, but with additional pitches, the graph changes:

```
d = DissonanceCurve([335, 440], [0.7, 0.3])
d.plot
```


The combination of pitches produces a more complex graph with minima. Those minima are good scale steps.







This class is currently optomised for FM, but subsequent versions will calculate spectra for Ring Modulation, AM Modulation, Phase Modulation and combinations of all of those things.









```
(

s.waitForBoot({

	var carrier, modulator, depth, curve, scale, degrees;

	SynthDef("fm", {arg out, amp, carrier, modulator, depth, dur, midinote = 0;
		var sin, ratio, env;

		ratio = midinote.midiratio;
		carrier = carrier * ratio;
		modulator = modulator * ratio;
		depth = depth * ratio;

		sin = SinOsc.ar(SinOsc.ar(modulator, 0, depth, carrier));
		env = EnvGen.kr(Env.perc(releaseTime: dur)) * amp;
		Out.ar(out, (sin * env).dup);
	}).add;

	s.sync;

	carrier = 440;
	modulator = 600;
	depth = 100;
	curve = DissonanceCurve.fm(carrier, modulator, depth, 1200);
	scale = curve.scale;


	degrees = (0..scale.size); // make an array of all the scale degrees


// We don't know how many pitches per octave  will be until after the
// DissonanceCurve is calculated.  However, deprees outside of the range
// will be mapped accordingly.


	Pbind(

		\instrument,	\fm,
		\octave,	0,
		\scale,	scale,
		\degree,	Pseq([
			Pseq(degrees, 1), // play one octave
			Pseq([-3, 2, 0, -1, 3, 1], 1) // play other notes
		], 1),

		\carrier,	carrier,
		\modulator,	modulator,
		\depth,	depth
	).play
});
)
```





The only problem here is that this conflicts entirely with Just Intonation!







For just tunings based on spectra, we would calculate dissonance based on the ratios of the partials of the sound.  Low numbers are more in tune, high numbers are less in tune.

There's only one problem with this, which is visible if we generate a graph of just a sine tone:
```
d = DissonanceCurve([440], [1])
d.just_curve.collect({|diss| diss.dissonance}).plot
```
There is no pattern of minima and maxima. How do we pick tuning degrees?



There are two answers provided. 

The first is to use a moving window where we pick the most consontant tuning within that window.  This defaults to 100 cents, assuming you want something with roughly normal step sizes.



Then to pick scale steps, we can ask for the n most consontant tunings

`t = d.justScale(100, 7); // pick the 7 most consonant tunings``


The other method is to limit the integer size in the ratios. 
For example, if we pick a limit of 21, neither the numerator or the denominator will be larger than 21.
For this selection method, we also need to specify how many ratios we want in our tuning 
(and how many of those we want in our scale).

```
t = d.limitedJustScale(21, 11, 7); // pick the 11 most consonant tunings limitted by 21
                 // and put the 7 most consonant tunings into a Scale
  
```  

The higher the limit, the more closely related the scale is to the timbre.
  
These two methods will produce different scales. 
If the timbre has a harmonic sound, the scales will be more closely related than if the sound is enharmonic.



```
(
var carrier, modulator, depth, curve, scale, degrees;
carrier = 440;
modulator = 600;
depth = 100;
curve = DissonanceCurve.fm(carrier, modulator, depth, 1200);
scale = curve.justScale(100, 7); // pick the 7 most consonant tunings
degrees = (0..(scale.size - 1)); // make an array of all the scale degrees (you can't assume the size is 7)

Pbind(
	\instrument, \fm,
	\octave, 0,
	\scale, scale,
	\degree, Pseq([
		Pseq(degrees, 1), // play one octave
		Pseq([-7, 2, 0, -5, 4, 1], 1)], 1), // play other notes
	\carrier, carrier,
	\modulator, modulator,
	\depth, depth
).play
)
```







#Future plans

* Add the ability to calculate more spectra - PM, RM AM, etc
* Allow the user to specify how determine the consonance of Just ratios

##Comments / Feature Requests
* lattice - make n dimensional

