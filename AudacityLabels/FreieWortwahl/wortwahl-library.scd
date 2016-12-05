{
	var verben = (

		// parallel
		'**': { |liste|
			var max;
			liste.do({ |x|
				x.play;
				x[\wort].post; " ".post;
			});
			max = liste.value.maxItem({ |x| x[\dur] });
			if(max.notNil) { max[\dur].wait };
			[]
		},

		// seriell
		'*': { |liste|
			liste.do({ |x|
				x.play;
				x[\wort].post; " ".post;
				x[\dur].wait;
			});
			[]
		}

	);

	var kanaele = ();

	var zerteile = { |satz|
		var brocken;
		brocken = satz.separate { |a, b| a.isSpace and: b.isSpace.not };
		brocken.collect { |wort| wort.reject { |buchstabe| buchstabe.isSpace } }
	};

	var verwandle = { |satz|
		var events;
		var woerter = zerteile.(satz);

		woerter.do { |wort|
			var verb, event;
			wort = wort.asSymbol;
			verb = verben[wort];

			if(verb.isNil) {
				event = ~labels.at(wort);
				if(event.isNil) {
					event = (freq: \rest, dur: 0.5pi.rand, wort: "(%)".format(wort))
				}
			} {
				event = (type: \verb, wort: wort, play: verb)
			};
			events = events.add(event);

		};
		events

	};

	var sage = { |satz,  kanal = \standard|
		var last = satz.split($ ).last.asSymbol;
		if(verben[last].isNil) {
			satz = satz ++ " *";
		};
		verspreche.(satz)
	};

	var unterbreche = { |satz,  kanal = \standard|
		stop.(kanal);
		sage.(satz, kanal);
	};

	var verspreche = { |satz, kanal = \standard|
		var keller, events, hatExistiert;

		events = verwandle.(satz);
		keller = kanaele[kanal];
		hatExistiert = keller.notNil;

		if(keller.isNil) {
			keller = (
				inhalt: [],
				condition: Condition.new
			);
			if(kanal.notNil) { kanaele[kanal] = keller };
		};

		keller[\inhalt] = keller[\inhalt].addAll(events);

		"in den Keller gelegt: %\n\n".postf(postx.(events));
		"neuer Kellerinhalt: %\n\n".postf(postx.(keller[\inhalt]));

		if(hatExistiert) { setzeFort.value(keller) } { starte.value(keller) };

	};

	var setzeFort = { |keller|
		"UNHNAG".postln;
		keller[\condition].unhang;
		//if(keller[\prozess].isPlaying.not) { keller[\prozess].resume };
	};

	var postx = { |events|
		events.collect { |x| x[\wort] }.join($ ).quote
	};

	var starte = { |keller|
		keller[\prozess] = Task {
			var bis = 0, event, bisher, zukunft;
			"\n===== START =====".postln;
			loop {
				event = keller[\inhalt][bis];
				event !? { "STEP TO %\n".postf(event[\wort]) };
				if(event.notNil and: { event[\type] == \verb }) {
					"\n=======  VERB: %\n".postf(event[\wort]);

					bisher = keller[\inhalt][..bis - 1]; // what has happened so far

					bisher = event[\play].value(bisher);

					zukunft = keller[\inhalt][bis ..].drop(1); // drop verb, keep future
					postf("\nVERB: neuer keller: %  ++ %\n", postx.(bisher), postx.(zukunft));
					keller[\inhalt] = bisher ++ zukunft;
					bis = bisher.size;
				} {
					bis = bis + 1;
				};
				if(bis > (keller[\inhalt].lastIndex ? 0)) {
					"\n\n... WARTE AUF NEUE EINGABE".postln;
					keller[\condition].hang;
				}
			};
		}.play
	};

	var stop = { |kanal = \standard|
		var keller = kanaele[kanal];
		keller !? {
			keller[\prozess].stop;
			keller[\prozess] = nil;
			keller[\inhalt].clear;
			kanaele[kanal] = nil;
		}
	}.flop;

	var stopAll = { stop.(kanaele.keys) };

	var pause = { |kanal = \standard|
		var keller = kanaele[kanal];
		keller[\prozess].stop;
	}.flop;

	var pauseAll = { pause.(kanaele.keys) };

	if(~labels.isNil) {
		"Ich kann leider keine Soundfile Labels finden. Bitte noch nachladen!".warn;
	};


	// external access
	~verben = verben;
	~verspreche = verspreche;
	~sage = sage;
	~kanaele = kanaele;
	~stop = stop;
	~pause = pause;
	~unterbreche = unterbreche;

	preProcessor = { |str|
		if(str.beginsWith("--")) { "~sage.value(\"%\")".format(str.drop(2)) } { str }
	};

}.value;

