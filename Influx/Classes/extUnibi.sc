// convert unipolar to bipolar range,
// and optionally add some noise for control value dithering.
// bipow used for simple reversible bipolar range bending

+ SimpleNumber {
	unibi { |noise = 0.0| ^(this + noise.bilinrand).clip(0,1) * 2 - 1 }
	biuni { |noise = 0.0| ^(this + 1 * 0.5 + noise.bilinrand).clip(0,1) }
	bipow { |exp = 1| ^this.abs ** exp * this.sign }
}

+ Collection {
	unibi { |noise = 0.0| ^(this + noise.bilinrand).clip(0,1) * 2 - 1 }
	biuni { |noise = 0.0| ^(this + 1 * 0.5 + noise.bilinrand).clip(0,1) }
	bipow { |exp = 1| ^this.abs ** exp * this.sign }
}

+ AbstractFunction {
	unibi { arg function = 0.0, adverb; ^this.composeBinaryOp('unibi', function) }
	biuni { arg function = 0.0, adverb; ^this.composeBinaryOp('biuni', function) }
	biuni { arg function = 0.0, adverb; ^this.composeBinaryOp('bipow', function) }
}
