
// Implementation of an urn based random selection principle.

Purno : ListPattern {
	*new { arg list, repeats=1;
		^super.new(list, repeats);
	}
	embedInStream {  arg inval;
		var offsetValue, tempArray, prevItem;
		// Do the loop to peform the random series selections.
		repeats.value(inval) * list.value(inval).size do: {
			var item, index;
			// If tempArray size is 0 copy input list
			if (tempArray.size == 0, { tempArray = list.copy;  });
			// While new item is the same as the previous value or nil, select randomly a new element from the list.
			while ({var bool;
				if ((item == prevItem).or(item.isNil), {
					bool = true;
					},{
						bool = false;
						// class.isKindOf(class) // doesn't work in this constellation. Because instVarHash gives different number for each
						// different setted class this if expression only work on non numerical objects.
						if (item.isNumber != true && (prevItem.isNumber != true), { bool = item.instVarHash == prevItem.instVarHash });
				});
				bool;
			})
			{
				// generate a random index
				index = tempArray.size.rand;
				// grab item from tempArray with the new index
				item = tempArray[index];
			};
			// assign new item the prevItem variable
			prevItem = item;
			// remove index and selected item from the tempoRaary
			tempArray.removeAt(index);
			// don't know why this is? guess there it has a similar function to yield.
			inval = prevItem.embedInStream(inval);
		};
		^inval;
	}
	storeArgs { ^[ list, repeats ] }
}

