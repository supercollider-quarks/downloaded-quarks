PparSwitch : Pswitch {
	var <>accum;
	*new { arg list, which = 0, accum = false;
		^super.new(list, which).accum_(accum)
	}
	// a convenient implementation, using Ppar. 
	// could be more effective, but this is easy.
	embedInStream { arg inval;
		
		var outval, patterns, stream, key, ended;
		var whichStr, index, delta = 0.0;
		
		key = UniqueID.next;
		patterns = list.collect { |pat, i|
			pat.collect { |event| event.put(key, i) } 
		};
		
		stream = Ppar(patterns).asStream;
		whichStr = which.asStream;
		
		loop {
			index = whichStr.next(inval).asArray;
			outval = stream.next(inval);
			if(outval.isNil or: { index.isNil }) { 
				nil.yield; ^inval 
			};
			if(index.includes(outval.at(key)).not) {
				if(accum) {
				delta = delta + outval.delta;
				if(delta > 1.0) { // return at least one event per accum period.
					inval = Event.silent(delta).yield;
				}
				} {
					inval = outval.put(\degree, \rest).yield;
				}
			} {
				if(delta > 0) {
					inval = Event.silent(delta).yield;
				};
				outval.put(key, nil);
				delta = 0.0;
				inval = outval.yield;
			};
		}

	}
}

Pmerge : Ppar {
	var <>seedPattern;
	
	*new { arg seedPattern, list, repeats=1;
		^super.new(list, repeats).seedPattern_(seedPattern)
	}
	
	embedInStream { arg inval;
		var assn;
		var priorityQ = PriorityQueue.new;
	
		repeats.value.do {
			var outval, outvalToMerge, stream, nexttime, now = 0.0, delta = 0.0;
			var seedStream = seedPattern.asStream, mergerEvent;

			this.initStreams(priorityQ);
			mergerEvent = ();
					
			inval ?? { this.purgeQueue(priorityQ); ^nil.yield };
			
			
			while { priorityQ.notEmpty } {
				
				outval = seedStream.next(inval);
				if(outval.isNil) { ^nil.yield };
				
				
				while {
					nexttime = priorityQ.topPriority;
					
					if(nexttime.isNil) {
						priorityQ.clear;
					};
					priorityQ.notEmpty and: { nexttime - now < delta }
				} {
					stream = priorityQ.pop;
					outvalToMerge = stream.next(inval);
					
					outvalToMerge !? {
							
							// requeue stream
							priorityQ.put(nexttime + outvalToMerge.delta, stream);
							
							if(mergerEvent.notNil) {
								mergerEvent.putAll(outvalToMerge);
							} {
								mergerEvent = outvalToMerge.copy;
							};
					};
						
				};
				
				// merge event into outval
				mergerEvent.keysValuesDo { arg key, val;
							if(key !== \dur) { outval.put(key, val) };
				};
				inval = outval.yield;
				inval ?? { this.purgeQueue(priorityQ); ^nil.yield };
				
				delta = outval.delta;
					
				now = now + delta;
				
				};
					
					
				};
		^inval;
	}

}

