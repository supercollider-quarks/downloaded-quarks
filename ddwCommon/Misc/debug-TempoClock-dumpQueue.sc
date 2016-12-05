
+ TempoClock {
		// for debugging purposes
		// this will generate A LOT of information!
	dumpQueue {
		queue.clump(3).do({ |pair|
			// ("\n" ++ pair[0]).postln;
			"\n%, priority %\n".postf(pair[1], pair[0]);
			pair[2].dumpFromQueue;
		});
	}
}


+ Function {
	dumpFromQueue {
		("Arguments: " ++ this.def.argNames).postln;
		("Variables: " ++ this.def.varNames).postln;
		this.def.dumpByteCodes;
	}
}

+ Object {
	dumpFromQueue {
		this.dump
	}
}
