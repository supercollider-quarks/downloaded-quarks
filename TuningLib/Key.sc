// by Charles Celeste Hutchins and Jascha Narveson

Key {

/*@
shortDesc: Handles key changes
longDesc: Keeps track of key changes and adjusts the tuning of the current scale accordingly.

Also can quantize a given semitone, cents value or frequency into the currently used scale
@*/

	var < changes;
	var < root;
	var	< scale;
	var	rootScale;
	var <modes;

	*new { |scale, root = 0|

		/*@
		desc: create a Key
		scale: The current scale
		ex:
		a = Scale.choose;
		k = Key(a);
		@*/

		^super.new.init(scale, root)
	}

	init { |scal, rot|

		scale = scal;
		root = rot;
		rootScale = scale.deepCopy;
		changes = [root];
		modes = [];
	}

	root_ { |newRoot|

		var tuning, newTune, offset;

		root = newRoot.abs;

		// ok, so how do we got from V of V to I?
		// answer: subtraction

		tuning = scale.tuning.tuning;
		offset = rootScale.tuning.tuning.at(root) * newRoot.sign;
		"offset: % \n".postf(offset);
		tuning = tuning + offset;//(tuning.at(root) * newRoot.sign); // if it's negative, that's essentially subtraction
		newTune = Tuning(tuning, scale.tuning.octaveRatio, scale.tuning.name);
		scale.tuning = newTune;

	}




	change { | degree, chromatic|

		/*@
		desc: Change the current Key. This works correctly with both ET and JI.
		degree: The degree of the scale to modulate by. If it and chromatic are nil, revert to previous state.
		chromatic: the chromatic interval to modulate by
		ex:
		k = Key(Scale.major);
		k.scale.degrees;
		k.scale.semitones;
		k.change(4); // modulate to the 5th scale degree (we start counting with 0)
		k.scale.degrees;
		k.scale.semitones;
		k.change(4); // modulate to the 5th scale degree (in the new key)
		k.scale.degrees;
		k.scale.semitones;
		k.change; // modulate back from the V/V degree
		k.scale.degrees;
		k.scale.semitones;
		k.change; // modulate back from the 5th scale degree
		k.scale.degrees;
		k.scale.semitones;
		k.change(chromatic:3); // modulate by a minor third
		k.scale.degrees;
		k.scale.semitones;
		k.change; // back to home key again
		k.scale.degrees;
		k.scale.semitones;

		@*/

		var newRoot;

		if ((degree.isNil && chromatic.isNil),  // if no argument, then...
			{
				if(changes.size>0,
					{this.root_(changes.pop.neg)}, //... return to the previous key
					{"already in original key!".error}
				);
			},
			{
				newRoot = chromatic;
				newRoot.isNil.if({

					newRoot = scale.degrees.wrapAt(degree);
					"root %\n".postf(newRoot);
				});

				changes = changes ++ newRoot;
				this.root_(newRoot);
				"changed".postln;
			}
		);
	}


	quantizeFreq{ | freq, base = 440, round='off', gravity = 1|

		/*@
		desc: Snaps the feq value in Hz to the nearest Hz value in the current key
		freq: in Hz
		base: The base frequency, or root frequency
		round: has three modes:
			\off  do not round the answer
			\up   round the answer to the nearest scale freq above
			\down round the answer to the nearest scale freq below
		gravity: determines how strong the attraction is:
			1 = fully quantized
			0 = no quantization
			0<x<1  interpolate between unquantized and fully quantized values
		ex:
		a = Scale.choose;
		k = Key(a);
		k.quantizeFreq(660, 440);
		@*/


		var tempArray, func, g;

		func = { |f|

			var ratio, degree, octave, result, semitone;

			ratio = f / base;
			octave = 0;

			{ratio < 1}. while ({

				ratio = ratio * 2;
				octave = octave -1;
			});

			{ratio > 2}. while ({

				ratio = ratio /2;
				octave = octave + 1;
			});


			semitone = this.quantize(ratio.ratiomidi, round, 1);
			result = (semitone.midiratio * base) * (scale.octaveRatio ** octave);


			result = f + ((result - f)* g);
			result;

		};

		g = gravity.min(1).max(0);

		if (freq.isKindOf(SequenceableCollection),
			{
				freq.do({|i|
					tempArray=tempArray.add(func.value(i));
				});
				^tempArray;
			},
			{
				^func.value(freq);
		});
	}


	quantizeCents { |cent, round = 'off', gravity = 1|
		/*@
		desc: snaps a cents value to the nearest semitone in the current key
		cents: the cents value to be quantized, or an array of cents
		round: has three modes:
			\off  do not round the answer
			\up   round the answer to the nearest scale freq above
			\down round the answer to the nearest scale freq below
		ex:
			k.quantizeCents(1150);
		@*/

		^(this.quantize(cent / 100, round, gravity) * 100);
	}


	quantize { |semitone, round = 'off', gravity = 1|

		/*@
		desc: snaps a semitone to the nearest semitone in the current key
		semitone: the tone to be quantized, or an array of semitones
		round: has three modes:
			\off  do not round the answer
			\up   round the answer to the nearest scale freq above
			\down round the answer to the nearest scale freq below
		ex:
			k.quantize(11.5);
			k.quantize([0.1, 3.5, 7.4]);
		@*/

		var tempArray, func, g;

		scale.semitones;

		if (['up','down','off'].includes(round), {
			func = {|given| var target, result, octave, ratio;

				//given = given % scale.pitchesPerOctave;
				octave = 0;

				{given >= scale.octaveRatio.ratiomidi}.while({

					ratio = given.midiratio / scale.octaveRatio;
					octave = octave + 1;
					given = ratio.ratiomidi;
				});


				case
				{round=='off'} {
					target=given.nearestInList(scale.semitones)}
				{round=='up'}  {
					target=scale.semitones.at(scale.semitones.indexInBetween(given).ceil)}
				{round=='down'} {
					target=scale.semitones.at(scale.semitones.indexInBetween(given).floor)};

				//target;
				(octave != 0).if ({
					target = target + (scale.octaveRatio ** octave).ratiomidi;
				});

				result = given + ((target - given)* g);
			};

			g = gravity.min(1).max(0); // make sure gravity is within 0-1 range

			if (semitone.isKindOf(SequenceableCollection),
				{
					semitone.do({|i|
						tempArray=tempArray.add(func.value(i));
					});
					^tempArray;
				},
				{
					^func.value(semitone);
				});
		},
		{"the round argument must be one of: 'up', 'down', or 'off'.".error});
	}

	mode_ { |mode|
		/*@
		desc: changes the mode of the current key
		mode: an array of degrees or a key from ScaleInfo. If nil, it revcerts to previous mode
		ex:
			k = Key(Scale.major);
			k.scale.degrees;
			k.scale.semitones;
			k.mode_(\minor);
			k.scale.degrees;
			k.scale.semitones;
			k.scale([0, 2, 4, 6, 8, 10, 11]);
			k.scale.degrees;
			k.scale.semitones;

		@*/
		var newScale, newRoot, record = true;

		mode.isNil.if({
			record = false;
			if (modes.size>0,
				{ mode = modes.pop; },
				{ "already in original mode".error; }
			);
		});

		mode.postln;
		mode.notNil.if({
			mode.isKindOf(ArrayedCollection).if(
				{
					newScale = Scale(mode, scale.pitchesPerOctave, scale.tuning);
					newRoot = Scale(mode, rootScale.pitchesPerOctave, rootScale.tuning);
					//"array".postln;
				},
				{
					newScale = Scale.newFromKey(mode, scale.tuning);
					newRoot = Scale.newFromKey(mode, rootScale.tuning);
				}
			);

			if (record, { modes = modes.add(scale.degrees); });
			rootScale = newRoot;
			scale = newScale;
		})
	}
}
