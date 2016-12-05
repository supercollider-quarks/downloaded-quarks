
ReplaceBadValues {
	*ar { |in, sub = 0, id = 0,  post = 2|
		var subIndex =  CheckBadValues.ar(in, id, post) > 0;
		// prepare for Select
		sub = sub.asArray.collect { |sub1|
			if (sub1.rate != \audio) { sub = K2A.ar(sub) } { sub };
		};
		^Select.ar(subIndex, [in, sub]);
	}
	*kr { |in, sub = 0, id = 0,  post = 2|
		var subIndex = CheckBadValues.kr(in, id, post) > 0;
		^Select.kr(subIndex, [in, sub]);
	}
}
