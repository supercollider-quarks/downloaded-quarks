Safety {
	classvar <all;
	classvar <>defaultDefName = \safeClip;
	var <server, <defName, <synthDefs, <treeFunc, <synth;

	*initClass {
		all = ();
		Class.initClassTree(Server);
		Class.initClassTree(SynthDescLib);
		Server.all.do { |serv|
			Safety.all.put(serv.name, Safety(serv));
		};
	}

	storeArgs { ^[server.name] }
	printOn { |stream| ^this.storeOn(stream) }

	*new { |server, defName = (defaultDefName)|
		if (all[server].notNil) { ^all[server] };
		if (all[server.name].notNil) { ^all[server.name] };
		^super.newCopyArgs(server, defName).init;
	}

	numChannels { ^server.numChannels }

	init {
		this.initSynthDefs(this.numChannels);
		treeFunc = {
			fork {
				// send here just to make sure we dont get buildup?
				// synth.free;
				synthDefs[defName].send(server);
				server.sync;
				synth = Synth.tail(RootNode(server), defName);
				"% added synth %.\n".postf(this, defName.cs);
			};
		};
		this.enable;
	}

	enable {
		ServerTree.add(treeFunc, server);
		if (server.serverRunning) { treeFunc.value };
	}
	disable {
		synth.free;
		synth = nil;
		ServerTree.remove(treeFunc, server)
	}

	defName_ { |name|
		if (synthDefs[name].notNil) {
			defName = name;
			^this
		};

		"%: no synthdef % found - keeping %.\n".postf(this, name, defName);
	}

	addDef { |synthDef|
		// dont .add def here, numChans may differ for each server
		synthDefs.put(synthDef.name, synthDef);
	}

	initSynthDefs { |numChans|
		synthDefs = ();
		[
			SynthDef(\safeClip, { |limit=1|
				var mainOuts = In.ar(0, numChans);
				var safeOuts = ReplaceBadValues.ar(mainOuts);
				var limited = safeOuts.clip2(limit);
				ReplaceOut.ar(0, limited);
			}),
			SynthDef(\safeSoft, { |limit=1|
				var mainOuts = In.ar(0, numChans);
				var safeOuts = ReplaceBadValues.ar(mainOuts);
				var limited = safeOuts.softclip * limit;
				ReplaceOut.ar(0, limited);
			}),
			SynthDef(\safeTanh, { |limit=1|
				var mainOuts = In.ar(0, numChans);
				var safeOuts = ReplaceBadValues.ar(mainOuts);
				var limited = safeOuts.tanh * limit;
				ReplaceOut.ar(0, limited);
			});
			// introduces 0.01 * 2 sec latency ...
			SynthDef(\safeLimit, { |limit=1|
				var mainOuts = In.ar(0, numChans);
				var safeOuts = ReplaceBadValues.ar(mainOuts);
				var limited = Limiter.ar(safeOuts, limit);
				ReplaceOut.ar(0, limited);
			})
		].do { |def| this.addDef(def) };
	}
}