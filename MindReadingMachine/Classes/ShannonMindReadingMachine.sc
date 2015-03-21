/*

From: Claude E. Shannon: A Mind-Reading (?) Machine
Bell Laboratories Memorandum; March 18, 1953.

This machine is a somewhat simplified model of a machine designed by D. W. Hagelbarger. It plays what is essentially the old game of matching pennies or "odds and evens." This game has been discussed from the game theoretic angle by von Neumann and Morgenstern, and from the psychological point of view by Edgar Allen Poe in the "The Purloined Letter." Oddly enough, the machine is aimed more nearly at Poe's method of play than von Neumann's.

The code below is somewhat generalized to permit extension in two ways:
(a) longer recognition patterns
(b) arbitrary objects instead of zero and one.

*/


MindReadingMachine : Stream {

	var <table, <stack, <prev, <prediction;
	var <patternSize, <strategyClass;
	var <>verbose = false;

	*new { |patternSize = (3), strategyClass|
		^super.new.init(patternSize, strategyClass)
	}

	init { |size, argStrategyClass|
		if(size - 3 % 2 != 0) { Error("Incompatible pattern size").throw };
		patternSize = size;
		strategyClass = argStrategyClass ? ShannonMindReadingStrategy;
		this.reset;
	}

	reset {
		table = IdentitySet[];
		stack = [];
		prev = nil;
		prediction = this.getCurrentStrategy.next(nil);
	}

	next { |call|
		var previousPrediction, result;

		result = if(call != prediction) { 'wins' } { 'loses' };
		this.updateMemory(call, result);

		if(verbose) { "Result of game: Player %.\n".postf(result) };

		previousPrediction = prediction;
		prev = call;
		prediction = this.getCurrentStrategy.next(prev);

		^previousPrediction
	}

	prose {
		table.do { |strategy|
			strategy.postln;
		}
	}

	// PRIVATE IMPLEMENTATION

	getCurrentStrategy {
		var strategy = strategyClass.new(stack, patternSize);
		^table.detect {|pastStrategy| pastStrategy == strategy } ? strategy;
	}


	updateMemory { |call, result|
		var strategy, finalAction;
		if(prev.isNil) { stack.add(result); ^this };
		stack = stack.keep(patternSize.neg);
		strategy = this.getCurrentStrategy;
		finalAction = strategy.sameOrDifferent(call, prev);
		stack = stack.add(finalAction).add(result);
		if(this.stackIsFull) {
			strategy.response = finalAction;
			table.add(strategy);
		};
	}

	stackIsFull {
		^stack.size > patternSize
	}


}

/*
Something like:
"The player loses, plays differently, and wins. He may then play the same or differently."
*/

AbstractShannonMindReadingStrategy : Stream {

	var <response, previousResponse;
	var <behavioralPattern;

	*new { |stack, patternSize|
		^super.new.init(stack, patternSize)
	}

	init { |stack, patternSize|
		if(stack.notEmpty) {
			patternSize = patternSize ? 3;
			behavioralPattern = stack.keep(patternSize);
			behavioralPattern.pairsDo { |a, b|
				if(a != \wins and: { a != \loses }) {
						Error("stack corruption:" ++ stack).throw;
				};
				if(b != \same and: { b != \differently }) {
						Error("stack corruption:" ++ stack).throw;
				};
			}
		}
	}

	differentValue {
		^this.subclassResponsibility(thisMethod)
	}

	sameOrDifferent {
		^this.subclassResponsibility(thisMethod)
	}

	response_ { |val|
		previousResponse = response;
		response = val
	}

	next { |prev|
		^if(this.isCertain.not) { #[1, 0].choose } {
			if(response == \same) { prev } { this.differentValue(prev) }
		}
	}

	reset {
		response = previousResponse = nil;
	}

	isCertain {
		^response.notNil and: { response == previousResponse }
	}

	printOn { |stream|
		stream << "The player";
		behavioralPattern.pairsDo { |a, b|
			stream << " %, plays %, and ".format(a, b);
		};
		stream << behavioralPattern.last << ". ";
		if(response.isNil) {
			stream << "He may then play the same or differently."
		} {
			if(this.isCertain) {
				stream << "He then plays %.".format(response)
			} {
				stream << "He then plays %.".format(response)
			}
		}
	}


	== { arg other;
		^other.behavioralPattern == behavioralPattern
	}

	hash {
		^behavioralPattern.hash
	}


}

ShannonMindReadingStrategy : AbstractShannonMindReadingStrategy {

	// subclasses for different objects only need to implement two methods.
	// one for getting from one value to another (in a two element system this is just the opposite)
	// one for distinguishing one value from another (in simple cases, this is just (in-)equality)

	differentValue { |val|
		^if(val == 0) { 1 } { 0 }
	}

	sameOrDifferent { |current, previous|
		^if(current == previous)  { 'same' } { 'differently' }
	}

}

/*
Supplies an indexed, indefinite set of mind reading machines that are indexed by a stream of indices.
*/

Pmindread : Pattern {
	var <>pattern, <machineIndex, <behaviorSize, <strategyClass;
	var <>verbose = false;

	*new { |pattern, machineIndex, behaviorSize = 3, strategyClass|
		^super.newCopyArgs(pattern)
	}

	embedInStream { |inval|
		var indexStream = machineIndex.asStream;
		var mindReaders = Order.new;
		var stream = pattern.asStream;
		var val, index = 0, mindReader;
		while {
			val = stream.next(inval);
			val.notNil
		} {
			index = indexStream.next ? index;
			mindReader = mindReaders.at(index);
			if(mindReader.isNil) {
				mindReader = MindReadingMachine.new(behaviorSize, strategyClass);
				mindReader.verbose = verbose;
				mindReaders.put(index, mindReader);
			};
			inval = mindReader.next(val).yield;
		}

	}
}




