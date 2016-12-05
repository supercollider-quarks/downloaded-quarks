VGTuning {
	
	classvar <>verbose=false; 
	
		// SPECIFICS for gamKUG: 
			// paths for data files
	classvar <>vggDir, <>synthDataDir; 
	classvar <>sonatorListFilename = "sonatorNames_gamKUG.txt";
	classvar <>synthDataFilename = "synthData_gamKUG.txt";
	classvar <>synthDataFixFuncFilename = "SynthDataFixFunc.scd";
	classvar <>balanceFilename = "ampBalancePitch_gamKUG.scd";

			// strings for name parts
	classvar <>sampSetPrefix = "gamKUG_"; 
	classvar <slendroPrefix = "Sl_", <pelogPrefix = "Pl_";
	classvar <scalesForPrefixes;

	classvar <>laras=\slendro;
	classvar <tunings, <mulTrees;
	
	classvar <>synthDataFixFunc; 
	
	classvar 	<instGroupings = #[
			[ \BonangPan, \BonangBar ], 
			[ \PekingA, \SaronA, \SaronB, \DemungA ], 
			[ \GenderPan, \GenderBar, \Slenthem ], 
			[ \Kempyang, \Kethuk, \Kenong, \Kempul, \GongSuw, \GongAgeng ],
				 // raw format, take them out for now.
			[ \Kendhang /*, \KendhangGdh, \Ketipung*/ ]
		];

		// server stuff 
	classvar <busKeys = #[\freq, \ringtime, \amp, \attack];
	classvar <ensembleBusKeys = #[\freq, \ringtime, \amp, \attack];
	classvar <server;

	classvar <allSonatorNames, <allSlendroNames, <allPelogNames, <allNeutralNames;

	*slendro { ^tunings[\slendro] }
	*pelog { ^tunings[\pelog] }
	*neutral { ^tunings[\neutral] }


	*initClass { 
		vggDir = VGTuning.filenameSymbol.asString.dirname.dirname.dirname;
		synthDataDir = vggDir ++ "_synthData";
		"\nVGTuning: vggDir = %\n".postf(vggDir);
		"VGTuning: synthDataDir = %\n\n".postf(synthDataDir);
	}

	*init { |argServer| 
		this.free; 
		
		scalesForPrefixes = ( Sl_: \slendro, Pl_: \pelog );

			// tunigns and multrees are sort of parallel ... hmmm... 
		tunings = (); 
		tunings.pelog = (); 
		tunings.slendro = (); 
		tunings.neutral = (); 
		
		mulTrees = (); 


			// load SynthDataFunc if present.
		(VGTuning.synthDataDir +/+ synthDataFixFuncFilename).load;

		this.loadSonatorNames;
		this.loadSynthData;
				
		this.checkSonatorsAndData; 
		
		this.readBalanceInfo;

		synthDataFixFunc.value; 	// repair synthdata here, and post.
		
		this.prepTunings; 

		server = argServer ? Server.default;
		if(server.serverRunning.not) {Ê
			"*** VGTuning: cannot init when server is not running - s.boot first! ***\n\n".warn;
			^this;
		};

		this.prepMulTrees;

		this.prepRefs; 
	}

	*prepRefs { 
		var noteCount = 0; 
		[\pelog, \slendro].do { |laras| 
					// for the tuning, the reference is an instrument's refnote.
			var node =  VGTuning.mulTrees[laras];
//			node.metaData[\reference] = VGScale.....
			
			node.branches.keysValuesDo { |instKey, instTree | 
					// for the instrument, the reference is the highest 6 note
					// or else simply the highest available note. 
					// to be refined when we are informed better. 
					
				instTree.branches.keys.asArray.sort.do { |noteKey|
							// for sonators, it is the index of the best matching partial.
					var partIndex, node; 
					#partIndex, node = VGSound.findRefPartialFor(
						laras, instKey, noteKey, verbose);
						
					node.metaData[\reference] = partIndex;
					noteCount = noteCount + 1;
				};
			};
		};
		"\nVGTuning: found best partials matching scalenotes for % sonators.\n".postf(noteCount);
	}
	
	*checkSonatorsAndData { 
		var nameSet = allSonatorNames.collectAs(_.asSymbol, Set);
		var dataNameSet = VGSynthData.sonatorSpecs.keys; 
		
		if (	nameSet == dataNameSet ) { 
			"VGTuning: allSonatorNames and synthData are consistent.\n\n".postln;
		} { 
			warn("VGTuning: inconsistency between allSonatorNames and synthData!");
			"Details:\n in allSonatorNames, but not in synthdata: ".postln; 
			nameSet.difference(dataNameSet).postcs; 
			"in synthdata, but not in allSonatorNames: ".postln;
			dataNameSet .difference(nameSet).postcs; 
		};
	}
		
	*free {
		this.freeMulTrees;
	}
	
	*readBalanceInfo { |path|
		(synthDataDir +/+ (path ? balanceFilename)).loadPaths;
		
		/**** read a file with tabbed info: 
			sonatorName	ampComp(or nil)	rootFreq(or nil)
			...
		ignore lines that being with #, and empties. 
		such a file could also be written automatically, 
		and fine-tuned by hand. 
		*****/ 
	}
		// how many partials for which sonator? 
		// filter out Kendhang, as they are not used
		// only make those
	*getPartialNumbers { 
	
	}
	
	
	/////////////////////  loading names //////////////////
	
	*loadSonatorNames { |path| 
		
		path = synthDataDir +/+ (path ? sonatorListFilename);
	//	path.postcs;
		allSonatorNames = TabFileReader.read(path, true).collect({ |line| line.first.drop(7) });
	}
	
	*prepTunings { 
		var allNames = VGSynthData.sonatorSpecs.keys.collectAs(_.asString, Array).sort; 
		
		allPelogNames = allNames.select({ |name| name.keep(3) == pelogPrefix });
		allSlendroNames = allNames.select({ |name| name.keep(3) == slendroPrefix });
		allNeutralNames = allNames.select({ |name| 
			[pelogPrefix, slendroPrefix].every(_ != name.keep(3));
		});

		"VGTuning: now has % tuning_inst_key_names: % pelog, % slendro and % neutral (drums).
		first pelog: %
		first slendro: %
		last neutral: %\n\n".postf(
			VGSynthData.sonatorSpecs.size,
			allPelogNames.size, 
			allSlendroNames.size, 
			allNeutralNames.size, 
			allPelogNames.first, 
			allSlendroNames.first,
			allNeutralNames.last
		);

			// convert pelog and slendro note names
		[ 
			[tunings.pelog, allPelogNames, pelogPrefix],
		  	[tunings.slendro, allSlendroNames, slendroPrefix ]
		  	
		].do { |line, i| 
			var tuning, names, prefix;
			#tuning, names, prefix = line; 
			
			names.do { |rawname| 
				var shortname, instname, octaveNote, noteName;
				var instdict, notedict;
				
				shortname = if (rawname.beginsWith(prefix)) 
					{ shortname = rawname.drop(3) } // // drop tuning prefix
					{ shortname = rawname };

				#instname, octaveNote = shortname.split($_);
				
					// cut GenderPan1, GenderPan7 to GenderPan.
				instname = instname.reject(_.isDecDigit); 
				
				noteName = octaveNote;		// for gongs and drums I think
				instname = instname.asSymbol; 
				noteName = noteName.asSymbol; 
				rawname = rawname.asSymbol;
				shortname = shortname.asSymbol;
				
				instdict = (tuning[instname] = tuning[instname] ?? { () });
				
				instdict[instname].put(noteName, 
					(noteName: noteName, 
					shortName: shortname, 
					rawname: rawname)
				); 
			};
		};
		
			// neutral note names; gongs and drums: 
			// should conform to 
		allNeutralNames.do { |name, i| 
			var tuning = tunings.neutral;
			var instName, noteName, instNotesDict, sepIndex;
			
			sepIndex = name.indexOf($_);
			instName = name.keep(sepIndex).asSymbol;
			noteName = name.drop(sepIndex + 1).asSymbol;

			(tuning[instName] = instNotesDict = tuning[instName] ?? { () });
			instNotesDict.put(noteName, 
				(noteName: noteName, 
				rawname: name.asSymbol)
			)
		};
			// now they can be symbols.
		allPelogNames = allPelogNames.collect(_.asSymbol);
		allSlendroNames = allSlendroNames.collect(_.asSymbol);
		allNeutralNames = allNeutralNames.collect(_.asSymbol);
	}
	
	/////////////////////  loading analysis specs //////////////////

	*loadSynthData { |path| 
		VGSynthData.loadData(synthDataDir +/+ (path ? synthDataFilename));
	}
		
	*prepMulTrees { 
		var pelogDataDict, slendroDataDict, collectFunc, dataDict;
		var gongs, makeSpec; 
		var dataToFill = []; 

		makeSpec = { 1.dup(busKeys.size) };
		
		"VGTuning: prepMulTrees...".postln;
		
		collectFunc = { | instDescr | 
						// instrument level
			(
				specs: makeSpec.value,
				branches: 
						// note level
					instDescr.collect({ |notedesc, notename| 
						var rawname = notedesc.rawname; 
						var foundData =  VGSynthData.sonatorSpecs[rawname]; 
						// "\n%\n".postf(rawname); 
						
						if (foundData.isNil) { 
							"no data found for: %\n\n".postf([rawname, notename]); 
							dataToFill = dataToFill.add(notedesc.rawname);
							foundData = [1, 0, 0, 0];  // should be easy to find.
						};
						(	specs: makeSpec.value,
							relSpecs: [1].reshapeLike(foundData),
							baseSpecs: foundData
						)
					})
			);
			// tunings.neutral should go here ... 
		};
		
		pelogDataDict = tunings.pelog.copy.collect(collectFunc);
		
		slendroDataDict = tunings.slendro.copy.collect(collectFunc); 
				
		if (dataToFill.notEmpty) { 
			warn("VGTuning: synthdata for % sounds not found: \n    %\n\n"
				.format(dataToFill.size, dataToFill));
		} { 
			"VGTuning: prepMulTrees - found all data.".postln;
		};
		
		pelogDataDict = (
			specs: makeSpec.value, branches: pelogDataDict, argNames: busKeys
		);
		slendroDataDict = (
			specs: makeSpec.value, branches: slendroDataDict, argNames: busKeys
		);
		
		mulTrees.pelog_(VGMulTree.newFromDict(pelogDataDict, server));
		mulTrees.slendro_(VGMulTree.newFromDict(slendroDataDict, server));
		
	}
	
	*freeMulTrees {
		mulTrees.do(_.free)
	}
}
