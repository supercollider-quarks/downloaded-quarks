
// von auszen neue "verben" einführen geht so:
~verben['?'] = { |liste| liste.do({ |x| x.put(\rate, rrand(2.0, 2.2)) }) };
// mit weniger mickey mousing:
~verben['?'] = { |liste| liste.do({ |x| x.put(\rate, rrand(1.0, 1.2)) }) };
// oder nur das letzte
~verben['?'] = { |liste| liste.last.put(\rate, rrand(1.0, 1.2)); liste };

~sage.value("the missile knows greater at all times ? *");
~stop.value;


// zufällige reihenfolge und manche rückwärts
(
~verben['%'] = { |liste|
	liste = liste.scramble;
	liste.do { |x| if(0.5.coin) { x[\rate] = (x[\rate] ? 1.0).neg }};
	liste
};
);


// ~verspreche.value("ergebnis alleine n liebe zwergenaufstand kriegsfall");

~sage.value("the missile knows  % greater at all times .");
~sage.value("it obtain a deviation ? where where where ** it isn't . ");
~stop.value;
~labels[\obtains].copy.play;
~labels.dict.keys; // alle wörter



// nur mal so als Beispiel: ein effekt (geht noch nicht ganz so wie man möchte)

(
SynthDef(\raum, { |out, gate = 1|
	var input = In.ar(out, 2);
	var env = EnvGen.kr(Env.asr, gate);
	var sig = GVerb.ar(input.sum * env, Rand(4, 11), revtime: Rand(0.4, 0.5), damping:Rand(0.1, 0.8));
	DetectSilence.ar(sig, doneAction: 2);
	ReplaceOut.ar(out, sig + (0.8 * input))
}).add;

~verben[\uhu] = { |liste|
	var on = (type: \on, instrument: \raum, addAction: \addToTail);
	var off = on.copy.put(\type, \off);
	liste = liste.addFirst(on).add(off);
};
)

~sage.value("the missile knows greater at all times uhu *");

~verspreche.value("the missile uhu knows greater at uhu all times uhu *");
