
+ UGen {
	zap { |sub = 0|
		if (rate == \audio) {
			^ReplaceBadValues.ar(this, sub);
		} {
			^ReplaceBadValues.kr(this, sub);
		}
	}
}
+ Array {
	zap { |sub = 0|
		sub = sub.asArray;
		^this.collect { |item, i|
			item.zapBad(sub.wrapAt(i));
		}
	}
}
