+ Pattern {
	endless { arg default;
		^if(default.isNil) {
			Prout { arg inval;
				var outval, stream, previous;
				stream = this.asStream;
				loop {
					outval = stream.next(inval);
					if(outval.notNil) { previous = outval } {Êoutval = previous };
					inval = outval.yield;
				}
			}
		} {
			Prout { arg inval;
				var stream;
				stream = this.asStream;
				loop {
					inval = (stream.next(inval) ? default).yield;
				}
			}
			
		}
	}
}


/*
+ Stream {

	nextNDur { arg dt, inval;
		^Pfindur(dt, this).asStream.allEvents(inval); // no way to pass back inval.
	}
	
	allEvents { arg inval;
		// don't do this on infinite streams either.
		var array;
		inval = this.doInval({|item| array = array.add(item); }, inval);
		^array
	}
	
	doInval { arg function, inval;
		var item, i=0;
		while { 
			item = this.next(inval); 
			item.notNil 
		}{
			function.value(item, i);
			i = i + 1;
			inval = item;
		};
		^inval
	}
}
*/

