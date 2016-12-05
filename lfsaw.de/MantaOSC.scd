/* debug tracing of OSC messages
OSCFunc.trace(true); 
OSCFunc.trace(false); 
// */

// opens a terminal window and runs MantaOSC in it.
MantaOSC.start;


(
q = q ? ();
m = MantaOSC().start;
m.padVelAction = {|idx, num, time|
	idx.postln;	
};
)



// EXAMPLE -- enable ledControl and simulate native feature of lighting buttons
(
m.setLEDControl(\padandbutton, true);
m.padVelAction = {|idx, val, time|
	m.setPadLED(idx.postln, #[\off, \amber][(val>0).asInteger]);
};
m.butVelAction = {|idx, val, time|
	m.setButtonLED(idx, #[\off, \amber][(val>0).asInteger])
};
)

// turn off
(
m.padVelAction = nil;
m.butVelAction = nil;
m.setPadLED((0..47), \off);
m.setLEDControl(\padandbutton, false);
)


// EXAMPLE -- set patterns, toggle pad light when touched
(
q.patterns = IdentityDictionary[ 
	('spiral'          -> #[1,0,1,1,1,1,0,1,0,1,0,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,1,0,1,0,1,0,0,0,0,1,0]), 
	('leftRight'       -> #[1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1]), 
	('flower'          -> #[0,1,1,0,1,0,1,1,1,0,1,0,0,1,0,1,0,1,1,0,1,0,1,1,1,1,0,1,0,1,1,0,1,0,1,0,0,1,0,1,1,1,0,1,0,1,1,0]), 
	('hStripes'        -> #[1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0]), 
	('smallChecker'    -> #[1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1]), 
	('checker'         -> #[1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1]), 
	('vStripes'        -> #[1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0]), 
	('frame'           -> #[0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,1,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0]), 
	('rStripes'        -> #[1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0]), 
	('lStripes'        -> #[1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1]), 
	('reflection'      -> #[1,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,0,1,1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,1]), 
	('realTriads'      -> #[1,1,0,1,1,0,1,1,1,0,0,1,0,0,1,0,1,1,0,1,1,0,1,1,1,0,0,1,0,0,1,0,1,1,0,1,1,0,1,1,1,0,0,1,0,0,1,0]), 
	('twoStripes'      -> #[1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0]), 
	('squareFrame'     -> #[0,1,1,1,1,1,1,0,0,1,0,0,0,0,1,0,0,1,0,1,1,0,1,0,0,1,0,1,1,0,1,0,0,1,0,0,0,0,1,0,0,1,1,1,1,1,1,0]), 
	('upperLower'      -> #[1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1]), 
	('ring'            -> #[1,0,1,1,1,1,0,1,0,1,0,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,1,0,1,0,0,1,0,1,1,0,1,0,0,1,0,0,0,1,0,1]), 
	('checkeredTriads' -> #[1,1,0,1,1,0,1,1,1,0,0,1,0,0,1,0,0,0,1,0,0,1,0,0,0,1,1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,0,1,0,0,1,0]), 
	('lrBurst'         -> #[1,1,0,1,0,1,0,0,0,1,0,1,1,0,1,1,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,1,0,1,1,0,1,0,0,0,1,0,1,0,1,1]) 
];

q.padLEDState = 0!m.inverseMappingTable.size;
q.patternName = \spiral;
q.pattern = q.patterns[q.patternName];
q.isInverted = false;

m.setLEDControl(\padandbutton, true);

q.generatePattern = {|q, modes|
	[0!47, modes].flop.collect{|val| val.sum%2}
};

q.setPattern = {|q, pattern, invert=false|
	var toggles, rawPattern;

	pattern.isKindOf(Symbol).if({
		q.patternName = pattern;
		rawPattern = q.patterns[pattern];
	}, {
		q.patternName = \custom;
		rawPattern = pattern;
	});

	q.isInverted = invert;
	
	rawPattern.postln;
	
//	toggles = (rawPattern + q.padLEDState)%2;
	toggles = rawPattern;
	toggles.postln;
	invert.if{toggles = 1-toggles};
	m.setRawPadLED((0..47), #[\off, \amber][toggles]);
	q.pattern = m.inverseMappingTable.collect{|idxs|
		rawPattern.wrapAt(idxs)
	}
};

m.padVelAction = {|idx, val, time|
	var toggle;
	q.padLEDState[idx] = (val>0).if({ // pressed
		1;
	}, { // released
		0
	});
	
	toggle = (q.pattern[idx] + q.padLEDState[idx])%2;
	q.isInverted.if{toggle = 1-toggle}; 
	m.setPadLED(idx, #[\off, \amber][toggle]);
	idx.postln
};

m.butVelAction = {|idx, val, time|
	m.setButtonLED(idx, #[\off, \amber][(val>0).asInteger])
};
)


// set pattern, second arg inverts it
q.setPattern('spiral', true);
q.setPattern('spiral', false);
q.setPattern('leftRight');
q.setPattern('flower');
q.setPattern('hStripes');
q.setPattern('smallChecker');
q.setPattern('checker');
q.setPattern('vStripes');
q.setPattern('frame');
q.setPattern('rStripes');
q.setPattern('lStripes');
q.setPattern('reflection');
q.setPattern('realTriads');
q.setPattern('twoStripes');
q.setPattern('squareFrame');
q.setPattern('upperLower');
q.setPattern('ring');
q.setPattern('checkeredTriads');
q.setPattern('lrBurst');


q.setPattern(q.generatePattern([1, 0, 1])); ""
q.setPattern(q.generatePattern([1, 1, 0]))
q.setPattern(q.generatePattern([0, 0, 1, 0, 1, 1]))

q.setPattern(q.generatePattern([0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1]))


q.ledControl(\padandbutton, false)



/////////// mapping values
(
q.path = "/localvol/sound/src/libmanta/HarmonicTable.map";
// q.path = "/localvol/sound/src/libmanta/Wicki-Hayden.map";


q.rawMapping = FileReader.read(q.path, true, true);
q.mappingTable = q.rawMapping.select{|row| row.first != "#"}.collect{|row| row.collect(_.asInteger)}.flop.last;
m.padMappingTable = q.mappingTable;
)


(
// set soft light and show surroundings (-2, -1, 0, 1, 2)
// visuals break when releasing a "chord" in a sequence
m.setLEDControl(\padandbutton, true);
m.padVelAction = {|idx, val, time|
	m.setPadLED(idx.postln + [-2, -1, 0, 1, 2], #[\off, \amber][(val>0).asInteger]);
};
m.butVelAction = {|idx, val, time|
	m.setButtonLED(idx, #[\off, \amber][(val>0).asInteger])
};
)

//q.setPattern('vStripes');

(
s.waitForBoot{
	SynthDef(\complexResSimple, {
		var src;
		
		src = ComplexRes.ar(Decay2.ar(Impulse.ar(0, mul: \amp.kr(0.1)), 0.001, 0.2) * PinkNoise.ar, \freq.kr(100) + SinOsc.ar(4*\freq.kr, Rand(), \depth.kr(0)), \decay.kr(1));
		//Line.kr(1, 0, \decay.kr * 10, doneAction:2);
		src = src * EnvGen.kr(Env.adsr(0.01, 1, \release.kr(0.1)), gate: \gate.kr(1), doneAction:2) * 400;
		
		Out.ar(0, src.tanh!2)
	}).add
}
)
s.freeAll




(
// minimal synth ctl with visual aid (ring around + highlighting octaves)
// visuals break when releasing a "chord" in a sequence

q.octave = 0;
q.synths = nil!(m.maxIndex+1);
q.addTime = 0!(m.maxIndex+1);

m.setLEDControl(\padandbutton, true);
m.padVelAction = {|idx, val, time|
	//m.setPadLED(idx + [-48, -36, -24, -12, -4, -7, -3, 3, 7, 4, 12, 24, 36, 48], #[\off, \amber][(val>0).asInteger]);
	m.setPadLED(idx + [-48, -36, -24, -12, 0, 12, 24, 36, 48], #[\off, \amber][(val>0).asInteger]);

	(val>0).if({
		q.synths[idx].isNil.if{
			q.synths[idx] = Synth(\complexResSimple, [\freq, (18 + (12*q.octave) +idx).midicps, \amp, val * 0.015]);
		};
	}, {
		(q.synths[idx].notNil).if{
			Routine{
				s.sync;
				q.synths[idx].release;
				q.synths[idx] = nil;
			}.play
		};
	})
	
};

//m.padConAction = nil;
m.padConAction = {|idx, val, time|
	q.synths[idx].notNil.if{q.synths[idx].set(\depth, val*2)};
};

m.butVelAction = {|idx, val, time|

	(val>0).if{
		q.octave = idx;
		m.setButtonLED(((0..3) + idx)%4, #[\amber, \off, \off, \off]);
	}
	
	
	
};
)

s.freeAll




