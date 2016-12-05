Paccel : Pattern {
	var <>beat=1, <>div=9, <>coef=0.5;
	*new {| beat=1, div=9, coef=0.5 |
		^super.newCopyArgs(beat, div, coef)
	}
	storeArgs { ^[beat,div,coef] }
	embedInStream {| inval |
		var stream = Pseq(Array.geom(div, 1, coef).normalizeSum * beat, div).asStream;
		var counter = 0;
		while { counter < div } {
			counter = counter + 1;
			inval = stream.next(inval).yield;
		};
		^inval;
	}
}

PleakDC : FilterPattern {
	embedInStream { | inval |
		var stream = pattern.asStream;
		var next, prev = stream.next(inval);
		var y = 0;
		while {
			next = stream.next(inval);
			next.notNil;
		}{
			inval = (next - prev + (0.9 * y));
			inval.yield;
			y = inval;
			prev = next;
		}
		^inval
	}
}
Phpz1 : FilterPattern {
	embedInStream { | inval |
		var stream = pattern.asStream;
		var next, prev = stream.next(inval);
		while {
			next = stream.next(inval);
			next.notNil;
		}{
			inval = (0.5 * (next - prev)).yield;
			prev = next;
		}
		^inval
	}
}
Plpz1 : Phpz1 {
	embedInStream { | inval |
		var stream = pattern.asStream;
		var next, prev = stream.next(inval);
		while {
			next = stream.next(inval);
			next.notNil;
		}{
			inval = (0.5 * (next + prev)).yield;
			prev = next;
		}
		^inval
	}
}

// _________________________________________ array
Pat : FilterPattern {
	var <>index;
	*new { |pattern, index=0|
		^super.new(pattern).index_(index)
	}
	embedInStream { |inval|
		var stream = pattern.asStream, indexStrm = index.asStream;
		var array, indexVal;
		loop {
			array = stream.next(inval);
			indexVal = indexStrm.next(inval);
			if(array.isNil || indexVal.isNil, { ^inval });
			inval = yield(array.at(indexVal));
		}
	}
}
// expandable if
Pifx : Pattern {
	var	<>condition, <>iftrue, <>iffalse, <>default;
	*new { |condition, iftrue, iffalse, default|
		^super.newCopyArgs(condition, iftrue, iffalse, default)
	}
	storeArgs { ^[condition, iftrue, iffalse, default] }
	asStream {
		var	condStream = condition.asStream,
			trueStream = iftrue.asStream,
			falseStream = iffalse.asStream;

		^FuncStream({ |inval|
			var test, return;
			test = condStream.next(inval);
			if(test.isKindOf(SequenceableCollection), {
				return = Array.newClear(test.size);
				test.do({ |bool,i|
					if(bool.isNil, {
						return[i] = nil
					}, {
						if(bool, {
							return[i] = trueStream.next(inval) ? default
						}, {
							return[i] = falseStream.next(inval) ? default
						});
					});
				});
			}, {
				if(test.isNil, {
					return = nil
				}, {
					if(test, {
						return = trueStream.next(inval) ? default
					}, {
						return = falseStream.next(inval) ? default
					});
				});
			});
			return;
		});
	}
}
// todo: make Perform Object
+ Pattern {
	tsync { ^(this / Pfunc { TempoClock.default.tempo }) }
	// array
	at { |index| ^Pat(this, index) }
	// method
	perform { |selector| ^Punop(selector, this) }
	dif { ^Pdiff(this) }
	if { |trueStream, falseStream| ^Pifx(this, trueStream, falseStream) }
	leakDC { ^PleakDC(this) }
	hpz1 { ^Phpz1(this) }
	lpz1 { ^Plpz1(this) }
	plot { |n=2| this.asStream.nextN(n).plot(this.class.asString) }
}
// todo: shotcut all ListPattern
+ SequenceableCollection {
	pseq { |repeats=inf, offset=0| ^Pseq(this, repeats, offset) }
	pser { |repeats=inf, offset=0| ^Pser(this, repeats, offset) }
	pshuf { |repeats=inf, offset=0| ^Pshuf(this, repeats) }
	prand { |repeats=inf| ^Prand(this, repeats) }
	pxrand { |repeats=inf| ^Pxrand(this, repeats) }
}