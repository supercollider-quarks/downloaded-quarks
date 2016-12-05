Pathet {
	var <name;
	
	classvar <>dict, <maps, <unmaps, <scales, <pathetNames;
	classvar <pelog, <slendro;

	classvar <>verbose = true;
	
	*new { arg pathetName;
		if(pathetNames.includes(pathetName).not) { ^nil };
		^super.newCopyArgs(pathetName) 
	}
	
	scale { ^scales.at(name) }
	
	map { arg degree;
		^if(degree.isSequenceableCollection) {
			degree.collect { |x|Êmaps.at(name).at(x) }
		} {
			maps.at(name).at(degree)
		}
	}
	
	mapEvent { arg event, addScale=false;
		^this.convertEvent(event, false, addScale);
	}
	
	unmapEvent { arg event, addScale=false;
		^this.convertEvent(event, true, addScale);
	}
	
	applyToEvent { arg event, unmap = false, addScale=false;
		var mapping = if(unmap) {Êunmaps } {Êmaps }.at(name);
		var degree;
		
		event.use {
					degree = ~degree;
					if(degree.isNumber) {
// disable scale step remapping, 
// it is probably obsolete!
//						~degree = mapping.at(degree);
//						if(~degree.isNil) { 	
//							"(un)mapping of degree (%) failed.\n".postvg(degree);
//							~degree = \rest;
//						};
						if(verbose) { 
							if(unmap) {
								"unpathet: % -> %\n".postvg(degree, ~degree)
							} {
								"pathet: % -> %\n".postvg(degree, ~degree)
							} };
					};
					if(addScale) {
					//	~scale = this.scale; 
						~pathet = name;
					}
		};
		^event
	}
	
	convertEvent { arg event, unmap = false, addScale=false;
		
		^if(event.isSequenceableCollection) {
			event.collect { |ev|
				ev = ev.copy;
				this.applyToEvent(ev, unmap, addScale);
				ev
			}
		} {
			this.applyToEvent(event, unmap, addScale);
			event
		}
	}
	
	unmap { arg degree;
		^if(degree.isSequenceableCollection) {
			degree.collect { |x|Êunmaps.at(name).at(x) }
		} {
			unmaps.at(name).at(degree)
		}
	}
	
	mapRules { arg rules;
		// rules is an array of associations [events...] -> [events...]
		^rules.collect { arg assoc;
			assoc = assoc.copy;
			assoc.key = this.mapEvent(assoc.key, false);
			if(assoc.value.isSequenceableCollection) {
				assoc.value = this.mapEvent(assoc.value, true)
			};
			assoc
		}
	
	}
	
	
	*initClass {
		
			
		pelog = [0, 1.5, 2.7, 5.27, 6.68, 8, 9.72] + 2.5;
		slendro = [0, 2.4, 4.8, 7.2, 9.6] + 1.0;
		dict = (
			s:  #[0, 1, 2, 3, 4],
			p5: #[0, 1, 3, 4, 5],
			p6: #[0, 1, 2, 4, 5],	
			p7: #[1, 2, 4, 5, 6]
		);
		
		maps = (
			s:(
				1:0,
				2:1,
				3:2,
				4:2, // 99 %
				5:3,
				6:4,
				7:0 // 100 %
			),
			// pelog lima (5)
			p5: (
				1:0,
				2:1,
				3:2b, // 98 % of the cases..
				4:2,
				5:3,
				6:4,
				7:0b
			),
			// pelog nem (6)
			p6: (
				1:0,
				2:1,
				3:2,
				4:2s,
				5:3,
				6:4,
				7:0b
			),
			// pelog barang
			p7: (
				1:5s,
				2:0,
				3:1,
				4:2b, // 80 % of the cases..
				5:2,
				6:3,
				7:4
			)
			
		);
		
		pathetNames = maps.keys.asArray;
		
		unmaps = ();
		maps.keysValuesDo { |name, map|
			var reversedMap = ();
			map.keysValuesDo { |key, val| reversedMap.put(val, key) };
			unmaps.put(name, reversedMap);
		};
		
		scales = ();
		dict.keysDo { |pathetName|
			var selection, tonalMaterial, scale;
			tonalMaterial = if(pathetName.asString.at(0) === $s) { slendro } { pelog };
			selection = dict.at(pathetName);
			if(selection.isNil) { Error("pathet % not found!".format(pathetName)).throw };
			scale = tonalMaterial.at(selection);
			scales.put(pathetName, scale);
		};
		
	}

}


Ppathet : FilterPattern {
	var <>pathetName;
		
	*new { arg pathetName, pattern;
		^super.new(pattern).pathetName_(pathetName)
	}
	
	
	embedInStream { arg inval;
		var pathetStream = pathetName.asStream;
		^pattern.collect { |event|
			var pat = pathetStream.next(event);
			var pathetObj = Pathet(pat);
			if(pathetObj.notNil) {
				pathetObj.mapEvent(event, true)
			} {
				event
			}
		}.embedInStream(inval)
	
	}

}

PunPathet : FilterPattern {
	
	embedInStream { arg inval;
		^pattern.collect { |event|
			var pathet = event.at(\pathet);
			var pathetObj = Pathet(pathet);
			if(pathetObj.notNil) {
				pathetObj.unmapEvent(event, false)
			} {
				event
			}
		}.embedInStream(inval)
	
	}
	
}