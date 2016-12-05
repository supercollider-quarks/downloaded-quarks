Tafsiran {
	var <>rules, <originalRules, <converter;
	var <maximalDropSize, <maximalKeySize;
	var <>verbose = true;

	*new { |rules, converter|
		^super.newCopyArgs(rules, rules, converter ? BalunganReader).init;
	}

	init {
		var class = TafsiranNode;
		var set;
		rules = rules.deepCollectAssoc { |assoc|
			assoc = assoc.fromScoreToEvents(converter, false);
			// sets with variables are expanded into sets of TafsiranNodes.
			if(this.hasVariableSetNode(assoc)) {
				this.makeNestedNode(assoc)
			} {
				TafsiranNode(assoc)
			}

		};
		maximalDropSize = rules.findMaximalDropSize; // test this again!
		maximalKeySize = rules.findMaximalKeySize;
	}

	hasVariableSetNode { arg assoc;
		^assoc.value.isKindOf(Set)
			and: {
				assoc.value.any {|list|
					list.any { |event|
						event.eventAt(\resource).eventAt(\varname).notNil
					}
				}
			}
	}

	makeNestedNode { arg assoc;
			var set = IdentitySet.new;
			assoc.value.do { |each|
					set.add(
						TafsiranNode((assoc.key -> each))
					);
			};
			^() -> set
	}

	findMaximalDropSize {
		^rules.findMaximalDropSize
	}

	findMaximalKeySize {
		^rules.findMaximalKeySize
	}

	findRule { | key, list, index=0 |
		^rules.deepFindRule(list, index)
	}

	deepFindRule { |list, index|
		^rules.deepFindRule(list, index)
	}

	recognise { |list, index=0|
		var key = list.first;
		^this.findRule(key, list, index).notNil;
	}

	scaleAllDurations { arg ratio;
		rules.deepDoAssoc({ |assoc|
			if(assoc.key.isSequenceableCollection) {
				assoc.key.do { |event|
					if(event[\dur].notNil) { event[\dur] = event[\dur] * ratio }
				};
			};
			if(assoc.value.isSequenceableCollection) {
				assoc.value.do { |event|
					if(event[\dur].notNil) { event[\dur] = event[\dur] * ratio }
				};
			};
		}, false)
	}


}
