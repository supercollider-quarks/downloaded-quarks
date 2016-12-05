VGSamp : VGSound {

	classvar <>dir, <>laras=\slendro; 
	classvar <bufenv, <win, <loaded = false;
	classvar <>defaultDef = \vgSampFix, <pathsDict;
	classvar <>defNames = #[\vgSampFix, \vgSampDyn];
	classvar <>sampleFixFunc; 
	
	*initClass { 
		Class.initClassTree(VGTuning);		
				// overwrite from outside if you have them elsewhere.
		VGSamp.dir_(this.defaultDir);
		pathsDict = ();		
		defNames.do(synthEngines.put(_, this));		
	}
	
	*defaultDir { ^VGTuning.synthDataDir +/+ "samples_gamKUG/" }
	
	*startUp { |doneFunc| 
		this.makeSynthDefs;
				
		"VGSampler initialises, may take 20 seconds...".postln;

		VGSamp.clear;	
		VGSamp.makeSynthDefs;
		VGSamp.prepPaths;

		(dir.dirname +/+ "SampleFixFunc.scd").load;
		
		
		VGSamp.readBufs({ 
				// last chance to repair things...
			this.sampleFixFunc.value; 
			
			this.checkNames;
			
			doneFunc.value;
		});	// takes 15 secs or so.
		
	}
	
	*makeSynthDefs { 
		var numArgs = VGTuning.busKeys.size;
		
				// fixed sampler : ignore rate from event, 
				// read busses.
		SynthDef(\vgSampDyn, { |out, gate=1, pan, amp=1, ampComp=10, ebus, ibus, sbus, bufnum, rev| 
				
				var freqs, attacks, ringtimes, amps;
				var shiftBusses = [ebus, ibus, sbus].collect (In.kr(_, numArgs)); 
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);
				var sound, shiftfreq, shiftamp;

				#shiftfreq, shiftamp = shiftBusses.flop[[0, 2]].flop.product;
				sound = PlayBuf.ar(1, bufnum, shiftfreq);
				
				amp = cutoff * ampComp * amp * shiftamp;
				DetectSilence.ar(sound , doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, amp));
		}).add;
		
			// get rate and ampComp from event only - if rate is ever used.  
		SynthDef(\vgSampFix, { |out, gate=1, pan, amp=1, ampComp=10, ebus, ibus, sbus, rate=1, bufnum, rev| 
				
				var cutoff = EnvGen.kr(Env.cutoff, gate, doneAction: 2);
				var sound = PlayBuf.ar(1, bufnum, rate);
				
				amp = cutoff * ampComp * amp;
				DetectSilence.ar(sound , doneAction: 2); 
				Out.ar(out, Pan2.ar(sound, pan, amp));
		}).add;
			
	
		
	}
	
	*clear { 
		bufenv.clear(true); bufenv = nil;
		pathsDict.clear;
		loaded = false;
	}

	*checkNames { 
		// check how many of VGTuning's notenames are present as files, 
		// post missing files...
		// then fix them somehow. 

		var missingNames = VGTuning.allSonatorNames.collect(_.asSymbol);

		bufenv.envir.keysValuesDo { |key| missingNames.remove(key) };

		if (missingNames.size > 0) { 
			warn("VGSamp: after fixFunc no buffers were found for % noteKeys:".format(missingNames.size));
			missingNames.postcs;
			"\n\n".postln;
		} { "VGSamp: buffers complete.".postln };
	}
		
	*prepPaths { // always prepare all samples. 
				// builds 3 dicts: 
				// instNoteNames.slendro, instNoteNames.pelog, instNoteNames.neutral (e.g. drums)
				// could be refined later. 
	
		var pathnames, missingNames, prefixSize; 
		dir = dir ?? { this.defaultDir };	
		pathnames = (dir ++ "*.wav").pathMatch; 
		if (pathnames.size == 0) { 
			"VGSamp - NO FILES FOUND! Dir: % ".format(dir).warn;
			^this
		};
			// OK: 
		
		("VGSamp found % files in dir: \n\t % \n first: %\n last: %\n\n")
			.postf(pathnames.size, dir, 
				pathnames.first, 
				pathnames.last 
			);
		
		// check how many of VGTuning's notenames are present as files, 
		// post missing files...
		// then fix them somehow. 
		
		missingNames = VGTuning.allSonatorNames.collect(_.asSymbol);
		
		prefixSize = VGTuning.sampSetPrefix.size;
		pathnames.do { |path| 
			var filekey = path.basename.splitext.first.drop(prefixSize).asSymbol;
			missingNames.remove(filekey);
			pathsDict.put(filekey, path);
		};
		
		if (missingNames.size > 0) { 
			inform("VGSamp: no files found for % noteKeys.".format(missingNames.size));
		//	missingNames.postcs;
			"\n\n".postln;
		} { "VGSamp: found all % soundfiles.\n\n".postf(VGTuning.allSonatorNames.size) };
		
	}
		
	*readBufs { |doneFunc| 
		server = server ? Server.default;
		if (server.serverRunning.not) {
			"VGSamp: server not running. please boot first!".warn;
			^this
		};
	
		if (loaded) {
			"VGSamp: samples loaded already.".inform;
		//	bufenv.envir.keys.asArray.sort.postln;
			^this;
		};
		
		"VGSamp reads all buffers - may take 30 seconds or so!\n\n...\n\n".inform;

		Task { 
			var stillEmpties; 				0.2.wait; 

			bufenv !? { bufenv.clear(true) };			bufenv = BufEnvir(server);			
			
			pathsDict.keysValuesDo {|key, path| 
				bufenv.read(key, path); 
				server.sync;
			};
			
			0.5.wait; 
			
			server.sync; 

			bufenv.do (_.updateInfo);

			0.5.wait; 
				
			stillEmpties = VGSamp.numNotLoaded; 
			
			if (stillEmpties > 0) { 
				"VGSamp: % buffers are still empty - loading may have failed.\n\n"
					.postf(stillEmpties).warn;
			} { 
				"VGSamp: % buffers are loaded.\n\n".postf(bufenv.envir.size); 
				
				doneFunc.value; 
				
				loaded = true;
			}			
		}.play(AppClock);
	}

	*makeWin { |laras| ^VGSampGui(laras) }
		
	*numNotLoaded {		
		^VGSamp.bufenv.envir.count({ |b| b.numFrames == 0 });
	}
}

