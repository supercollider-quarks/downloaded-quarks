
// collection is collection of associations

+ SequenceableCollection {



	detectRecognised { arg list, index=0;
		^this.detect { |item|
			item.recognise(list, index)
		}
	}


	selectRecognised { arg list, index=0;
		^this.select { |item|
			item.recognise(list, index)
		}
	}

	detectTransform { arg list, index=0;
		^this.detectRecognised(list, index).transform(list, index)
	}

	selectTransform { arg list, index=0;
		^this.selectRecognised(list, index).collect(_.transform(list, index))
	}



}



+ Association {


	transform { arg list, index=(0);
		^value.transform(list, index)
	}
	recognise { arg list, index=0;
		^key.recognise(list, index)
	}


}


+ Nil {

	recognise { ^true }
}


+ Object {

	recognise { arg list, index=0;
		if(list.nthItem(index).isNil) {
			^false
		};
		^this.matchAttributes(list.nthItem(index))
	}

	transform { arg list, index=0;
		^this.value(list, index)
	}

	nthItem { ^this } // works with list and objects

	setEventDuration { ^this }
}


// set is recognised if any element matches.
// as a rule value it represents a random choice.

+ Set {
	recognise { arg list, index = 0;
		^this.any(_.recognise(list, index))
	}
	transform { arg list, index=0;
		^this.choose.transform(list, index)
	}
	eventDuration {
		^this.maxItem(_.eventDuration)
	}
}

+ Dictionary {
	nthItem { arg index = 0;
		^this
	}
	recognise { arg list, index = 0;
		^this.matchAttributes(list.nthItem(index))
	}
	transform { arg list, index=0;
		^this
	}
	eventDuration {
		^this.at(\dur) ? 1.0
	}
	setEventDuration { arg ratio;
		this.put(\dur, (this.at(\dur) ? 1.0) * ratio)
	}
}

+ Pattern {
	recognise { arg list, index = 0;
		var stream = this.asStream, i = 0;
		var key, item;
		loop {
			item = list[i + index];
			key = stream.next(item);
			// postf("key: % value: %\n", key, item);
			if(key.isNil) { ^true }; // match over
			if(item.isNil) {�^false }; // match too short
			if(key.matchAttributes(item).not) {�^false };
			i = i + 1;
		};
	}

}

// sequenceable collection is a collection of dictionaries here

+ SequenceableCollection {

	nthItem { arg index = 0;
		^this.at(index)
	}

	recognise { arg list, index=0;
		var lastIndex = list.lastIndex;
		if(index == -1) {
			this.reverseDo { |x, i|
				var item = list[lastIndex - i];
				if(item.isNil) {�^false }; // match too short
				if(x.matchAttributes(item).not) {�^false }
			};
		} {
			this.do { |x, i|
				var item = list[i + index];
				if(item.isNil) {�^false }; // match too short
				if(x.matchAttributes(item).not) {�^false }
			};
		};
		^true
	}

	findRule { arg key, list, index=0;
		^this.detect { |item|
			item.recognise(list, index)
		}
	}



	/////////////////////////////////////////////////////////////////////////




	eventDuration {
		^this.sum { |event|�event.eventAt(\dur) ? 1.0 }
	}

	scaleEventDuration { arg totalDur;
		var sum = this.eventDuration;
		var ratio = totalDur / sum;
		if(ratio == 1) { ^this };
		^this.collect(_.setEventDuration(ratio))
	}

	maximalKeySize {
		var res = 0;
		this.do { |each|
			res = max(each.maximalKeySize, res)
		};
		^res
	}





}

