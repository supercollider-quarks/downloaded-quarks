MeTA {

	var <utils;
	var <config;
	var <maps;
	var <ctls;
	var <gens;
	var <helpers;
	var <efx;
	var <aux;
	var <auxBus;
	var <views;
	var <samples;
	var <data;

	var <>server;

	var <>topDir;
	var <fulldirnames;
	var <dirnames;
	var <resourcesDir;
	var <samplesDir;
	var <>sampleFormats;

	var <loadedFiles;

	var <>guiWarnings = true;



	*new { |path = "", server|
		^super.new()
			.initDicts()
			.initPaths(path)
			.initBasics(server);
	}

	initDicts {
		utils 		= ();
		config 		= ();
		config.genNames = ();

		maps 		= ();
		ctls 		= ();
		helpers 	= ();
		gens 		= ();
		efx 		= ();
		aux 		= ();
		auxBus      = ();
		views 		= ();
		samples 	= ();
		data    	= ();
	}

	initPaths { |path|

		(path.pathType != \folder).if({
			"%: path not a valid directory; Exiting.\n \t%\n".format(
				this.class,
				path
			).warn;
			^nil;
		});

		topDir       = path;
		fulldirnames = (topDir +/+ "*/").pathMatch;
		dirnames     = fulldirnames.collect { |path| path.basename };

		resourcesDir = dirnames.detect{|dir| dir.contains("resources")};
		resourcesDir.notNil.if({
			samplesDir   = resourcesDir +/+ "samples";
		}, {
			"%: path does not contain resources directory. Please create one.\n \t%\n".format(
				this.class,
				path
			).warn;
		});


		// keeping track of loaded files
		loadedFiles = ();
		dirnames.do{|name|
			loadedFiles[name.asSymbol] = OrderedIdentitySet[];
		};


		"%:\n\ttopDir: %\n".format(
			this.class,
			topDir
		).inform;
	}

	initBasics {|argServer|
		server = argServer ?? {Server.default};
		sampleFormats = [".wav", ".aiff"];

	}

	///////////// file loading and opening ////////

	pr_id2dirname {|identifier|
		^dirnames.detect{|dir| dir.contains(identifier.asString)}
	}


	/** returns a sorted array of absolute paths
	identifier -- identifier for directory
	docs       -- array of filenames, deliberately dis-allowing wildcards
	ext        -- file extension
	warn       -- if true, show warning window in case a filename is not valid
	*/
	filePaths {|identifier, docs, ext = "scd", warn = true|
		var basePath, paths, invalids;

		basePath = topDir +/+ this.pr_id2dirname(identifier);
		paths = docs.bubble.flatIf(_.isString.not).collect { |doc|
			basePath +/+ "%.%".format(doc, ext);
		}.flatIf(_.isString.not).sort;

		invalids = paths.select{|p| p.pathType != \file};
		invalids.notEmpty.if{
			warn.if{
				this.warnWin(
					"Following files do not exist, exiting.\n"
					"%".format(invalids),
					this.class
				);
				^nil
			};

			paths = paths.select{|p| p.pathType == \file};

		};

		^paths;

	}


// // opens docs
	openFiles {|identifier, docs, ext = "scd", warn = false|
		this.filePaths(identifier, docs, ext, warn).do{|path|
			Document.open(path);
		}
	}


// load files and collect return values
	loadFiles {|identifier, docs, warn = true|
		// load only .scd files,
		// in alphabetical order
		var paths = this.filePaths(identifier, docs, "scd", warn);
		var keys = paths.collect{|p| p.basename.asSymbol};

		// book-keeping
		loadedFiles[this.pr_id2dirname(identifier).asSymbol].addAll(keys);

		^paths.collect(_.load)
	}

	loadGen { |name, index = 0|
		var res = this.loadFiles("gen", [name]);
		config.genNames[index] = res.first.key;
	}

	loadAux { |name, numChans = 2, defaultInGain = 0, postVol = true|
		r{
			var res, ndef, auxBusName;
			res = this.loadFiles("efx", ["aux/%".format(name)]);
			ndef = res.first;
			auxBusName = "%Aux".format(ndef.key).asSymbol;
			0.5.wait;
			ProxySubmix(auxBusName).ar(numChans);
			0.5.wait;
			ndef.map(\in, ProxySubmix(auxBusName));
			this.getNdefGens.do {|gen|
				ProxySubmix(auxBusName).addMix(gen, defaultInGain, postVol);
			};
			auxBus[auxBusName] = ProxySubmix(auxBusName);
		}.play(AppClock)
	}

	loadSamples {|relPath, forceReload = false|
		var absolutePath;
		var samplePaths;
		var baseKeyArray = relPath.notNil.if({
			relPath.split(Platform.pathSeparator).collect(_.asSymbol)
		},{
			[]
		});

		var loadSamples = {
			absolutePath =
				topDir +/+ samplesDir +/+ (relPath ? "");

			// get all paths to sound files
			samplePaths = MeTA_Utils.getFilteredPaths(
				absolutePath,
				sampleFormats
			);

			samplePaths.do{|path|
				var sampleKeys, buffer;

				sampleKeys = baseKeyArray ++ path.splitext.first
				.split(Platform.pathSeparator)
				.collect(_.asSymbol);

				buffer = Buffer.read(
					server,
					absolutePath +/+ path,
					action: {|b|
						"% loaded".format(sampleKeys).inform;
					}
				);
				samples.traversePut(sampleKeys, buffer);
			};
		};

		server.serverRunning.not.if{
			"%:loadSamples: Server needs to be running.".format(this.class).inform;
			^this;
		};

		// test if samples are already loaded
		samples.traverseAt(baseKeyArray).isNil.if({
			loadSamples.value;
		}, {
			forceReload.if({
				try{
					// free Buffers of samples originally stored there.
					samples.traverseAt(baseKeyArray).traverseDo{|b| b.postln; b.free};
					samples.traversePut(baseKeyArray, nil);
				} {
					"%:loadSamples: sample loading failed."
					.format(this.class).warn;
					this.dump;
					^this;
				};
				// load samples
				loadSamples.value;
			},{
				"%:loadSamples: samples \"%\" already loaded.\n\tUse flag forceReload to reload"
				.format(this.class, relPath).inform;
			})
		});




	}

	selectGenerator { |genKey|

		// turn off everything but selected gen
		gens.select{|gen, key| key != genKey}.do{|gen|
			gen.getHalo(\offFunc).value;
		};

		// turn selected gen on
		gens[genKey].getHalo(\onFunc).value;
	}



	getNdefGens {
		var res = List[];
		gens.traverseDo({|gen, key| gen.isKindOf(Ndef).if{
			res.add(gen)
		}});
		^res.asArray.sort { |px1, px2| px1.key < px2.key }
	}


	///////////// posting         /////////////////

	postServerOptions {
		var myClass = this.class;
		"********** Server info : **********\n"
		"%.server.name:    % \n"
		"%.server.address: % \n"
		"%.server.latency: % \n"
		.format(this.class, server.name.asCompileString, this.class, server.addr, this.class, server.latency).inform;
		"Server options:".inform;
		server.options.dump;
		"***********************************\n".inform;

	}


	///////////// GUI notification /////////////////

	warnWin { |string="?!?", title="?!?"|
		var w = Window("Warning: %".format(title)).front.alwaysOnTop_(true);
		var tv = StaticText(w, w.view.bounds);
		tv.background_(Color(1, 0.75));
		tv.align_(\center);
		w.alpha_(0.9);
		tv.string_(string).font_(Font("Arial", 24));
	}




}



MeTA_Utils {
	classvar listCmd = "ls -1Abp"; // print all files in a row, dirs have a trailing slash
	classvar <slash;

	*initClass {
		slash = Platform.pathSeparator;
	}

	*traversePathMatch { |path, parentDirs|
		var paths = "% %".format(listCmd, path +/+ parentDirs)
			.unixCmdGetStdOutLines.collect(_.escapeChar($ ));

		paths.notEmpty.if({
			^paths.inject([parentDirs.drop(1).drop(-1)], {|last, elem|
				elem = parentDirs +/+ elem;
				last ++
				(elem.last == slash).if({
					[this.traversePathMatch(path, elem)];
				},{
					[elem.drop(1)]
				})
			})
		},{
			^parentDirs.drop(1).drop(-1); // get rid of trailing slash
		})
	}

	*getFilteredPaths { |absolutePath, formats|
		^this.traversePathMatch(absolutePath, "")
			.flatIf(_.isString.not).select{|path|
				(path != "") &&
				{path.contains(".DS_Store").not}
				&& {
					formats.inject(false, {|last, format|
						last || path.toLower.endsWith(format)
					})
				}
			};
	}

	///////////// FX              /////////////////
	*addFx { |key, efxFunc, specs|
		ProxyChain.add(key.asSymbol, \filterIn -> efxFunc);
		specs.keysValuesDo{|key, spec|
			Spec.add(key, spec)
		};
	}


}