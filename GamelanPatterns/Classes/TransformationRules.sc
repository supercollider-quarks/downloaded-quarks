VGTransformationRule : Association {
	var <sourceKey, <sourceVal;
	
	// multichannel expand
	*new { arg assoc;
		^assoc.asArray.collect { |x|
			if(x.key.isAssociation) { 
				Error("A key shouldn't be an Association. You may have forgotten a comma!").throw 
			};
			super.new.init(x)
		}.unbubble	
	}
	
	// keep original key / value
	init { arg assoc;
		this.initAssoc(assoc);
		sourceKey = assoc.key;
		sourceVal = assoc.value;
	}
	
	fromScoreToEvents { } // already converted
	
	// here the association passed in is reformulated
	initAssoc { arg assoc;
		^this.subclassResponsibility(thisMethod)
	}
	
	//////////////////////////////////////////////////////////////////////
	// this is the interface for matching and rewriting (repeated here for clarity).
	//////////////////////////////////////////////////////////////////////

	recognise { arg list, index=0;
		^key.recognise(list, index)
	}
	
	transform { arg list, index=0;
		^value.transform(list, index)
	}
	

	/*
	the response to "recognise(list, index)" (a boolean) will 
	determine whether it is selected from a list of transformations.
	the response to "value.value(list, index)" will which produce the resulting value.
	
	subclasses can, when needed, override the "value" message.
	*/
	
	printOn { arg stream;
		stream << this.class.name << "(" <<< sourceKey << " -> " <<< sourceVal << ")"
	}
		
}



TafsiranNode : VGTransformationRule {
	
	var <matchFunction;
	var <arguments, <argumentPositions, <variables, <variablePositions;
	var <fromArgPositionsToVariablePositions, <fromArgNamesToArgPositions, <duplicateArgPositions;
	var <valueList, <substitutionIndex, <suffixIndex, <hasContext;
	
	// index -1 todo (if needed)
	recognise { arg list, index=0;
		if(hasContext) {
			index = index - substitutionIndex;
			if(index < 0) { ^false };
			if(index > list.lastIndex) { "warning: list too small".postln };
		};
		
	//	"trying to recognise: % at index: %\n".postf( list.unconvert, index);
		if(matchFunction.isNil) { ^key.recognise(list, index) };
		^key.recognise(list, index) and: {
			matchFunction.value(list, index)
		}
	}
	
	transform { arg list, index=(0);
		if(hasContext) {
			index = index - substitutionIndex;
			if(index < 0) { ^false };
			if(index > list.lastIndex) { "warning: list too small".postln };
		};
		^value.transform(list, index)
	}
	
	prefixSize {
		^if(substitutionIndex.isNil) { 0 } { substitutionIndex }
	}
	
	keyDropSize {
		^if(this.hasContext) {
			suffixIndex - substitutionIndex
		} {
			key.keySize 
		};
	}
	
	keySize {
		^key.keySize
	}

	keyDropDuration {
		^if(this.hasContext) {
			key[substitutionIndex .. suffixIndex - 1].eventDuration
		} { 
			key.eventDuration 
		};
	}
	
	storeOn { arg stream;
		stream << this.class.name;
		super.storeOn(stream)
	}
	
	// for Tafsiran compatibility
	
	maximalKeySize {
		^key.keySize
	}
	
	findRule { arg key, list, index;
			^if(this.recognise(list, index), { this }, { nil })
	}
	
	/////////////////////////////////
	
	variableMatchKey { ^\degree } // this could be made configurable later
 
	
	initAssoc { arg assoc;
		var val, keyList, duplicates;
		
		valueList = assoc.value;
		keyList = assoc.key;
		
		//////////////////////////////////////////////////////////////////////
		// translate any score strings to event lists
		//////////////////////////////////////////////////////////////////////
		
		if(keyList.isString) {
			
			// keyList = keyList.balungan(false);
			substitutionIndex = keyList.detectIndex { |ev| 
				ev.at(\resource).eventAt(\context) === \open };
			suffixIndex =  keyList.detectIndex { |ev| 
				ev.at(\resource).eventAt(\context) === \close };
		
		};
		/*if(valueList.isString) {
			valueList = valueList.balungan(true);
		};
*/			
		// make default context borders
		if(substitutionIndex.notNil or: { suffixIndex.notNil }) {
			if(substitutionIndex.isNil) { substitutionIndex = 0 };			suffixIndex = suffixIndex ?? { keyList.size };
			hasContext = true;
		} {
			hasContext = false;
		};
		
		//////////////////////////////////////////////////////////////////////
		// catch simple case here, objects or functions that don't need to be converted.
		if(keyList.isSequenceableCollection.not) {
			value = valueList;
			key = keyList;
			^this
		};
		
		//////////////////////////////////////////////////////////////////////
		// derive all mappings from argument positions to their respective parameter positions
		//////////////////////////////////////////////////////////////////////
		
		fromArgNamesToArgPositions = IdentityDictionary.new;
		
		// for polyphonic matching, we'd need to change this here
		keyList.do { |event, i|
			var pos;
			var name = event.at(\resource).eventAt(\varname);
			
			if(name.notNil) {
				arguments = arguments.add(name);
				argumentPositions = argumentPositions.add(i);
								
				pos = fromArgNamesToArgPositions.at(name);
				fromArgNamesToArgPositions.put(name, pos.add(i));
			};
	
		};
		
		
		//////////////////////////////////////////////////////////////////////
		// no need for all the calculations then ...
		if(arguments.isNil and: { hasContext.not })
		{ 
			value = valueList;
			key = keyList;
			//"TafsiranNode: no further translation needed, no variables detected.".postln;
			^this
		};
		
		fromArgNamesToArgPositions = fromArgNamesToArgPositions.select(_.isSequenceableCollection);
		duplicateArgPositions = fromArgNamesToArgPositions.values;
		
		//////////////////////////////////////////////////////////////////////
		// create a match function that only matches when the 
		// values of the duplicate variables match
		//////////////////////////////////////////////////////////////////////
		// "making match function".postln;
		
		matchFunction = { |list, index=0|
			// index -1 todo (if needed.)
			
			duplicateArgPositions.every { |pos|
				var val = ().put(
							this.variableMatchKey, 
							list.at(pos[0] + index).at(this.variableMatchKey)
						);
				//val = list.at(pos[0] + index);
				//postf("trying to match value: %\nat index: % (offset: %)\n", 
				//	val, pos[0] + index, index); 
				
				pos.every { |j|
					val.matchAttributes(list.at(j + index));
				}
			}
		};
		key = keyList;
		
		
		// no need for a variable logic of the value side..
		if(valueList.isSequenceableCollection.not) {
			value = valueList;
			key = keyList;
			^this
		};
		
		
		//////////////////////////////////////////////////////////////////////
		// if there are variables, create a function both for matching and for translating.
		//////////////////////////////////////////////////////////////////////
		
		
		// collect variable names and positions
		valueList.do { |event, i|
			var name = event.at(\resource).eventAt(\varname);
			if(name.notNil) {
				variables = variables.add(name);
				variablePositions = variablePositions.add(i);
			}
		};
		
		// no need for a translation logic
		if(variables.isNil) {
			value = valueList;
			key = keyList;
			^this
		};
		
		
		// collect argument names and positions and the mappings
		arguments.do { |name, i|
			var indices;
			variables.do { |varname, j|
				if(name === varname) { indices = indices.add(variablePositions.at(j)) };
			};
			fromArgPositionsToVariablePositions = fromArgPositionsToVariablePositions.add(indices)
		};
		// remove duplicate mappings
		duplicates = Set.new;
		fromArgPositionsToVariablePositions.copy.do { |list, i|
			if(list.notNil) { // check later..
				if(duplicates.includes(list)) {
					fromArgPositionsToVariablePositions[i] = #[];
				} {
					duplicates.add(list)
				}
			};
		};
		
		
		
		//////////////////////////////////////////////////////////////////////
		// create a value function that generates a transformation from a list
		//////////////////////////////////////////////////////////////////////

		value = { |list, index=0|
			var argumentValues, variableValues;
			var result = valueList.copy;
			var prefix, suffix;
			if(argumentPositions.notNil) {
				argumentValues = list[argumentPositions + index];
			};
			
			fromArgPositionsToVariablePositions.do { |indices, i|
				var argEvent = argumentValues.at(i);
				indices.do { |j|
					var inevent = argEvent.copy;
					var event = valueList.at(j).copy;//
//					event[\degree] = inevent[\degree]; // for now: copy only degree / octave.
//					event[\octave] = inevent[\octave];
//					event = (degree: inevent[\degree], octave: inevent[\octave]).putAll(event);
					event = inevent.copy.putAll(event);
					event[\resource] = nil; // remove resource.
					result.put(j, event)
				}
			};
			
			/*
			if(hasContext) {
				prefix = keyList[..substitutionIndex - 1];
				suffix =  keyList[suffixIndex ..];
				postf("prefix: % between: % suffix: %\n", 
					prefix.unconvert, result.unconvert, suffix.unconvert);
				
				
			};
			*/
			
			result
		}
			
	}
	
}

VarTrans : TafsiranNode {

	*new { arg ... args;
		"please change VarTrans to: TafsiranNode".postln;
		^super.new(*args)
	}
}

