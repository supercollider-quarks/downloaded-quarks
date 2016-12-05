RequiredFile {
	var <>path, <>result, <>mtime;

	*new { |path, result, mtime| ^super.newCopyArgs(path, result, mtime) }

	== {
		|other|
		^((path == other.path) && (mtime == other.mtime))
	}

	!= {
		|other|
		^(this == other).not
	}
}

Require {
	classvar <requireTable;

	*test {
		UnitTestScript("Require",
			Require.filenameSymbol.asString.dirname +/+ "Test" +/+ "Require_unittest.scd").runScript();
	}

	*initClass {
		requireTable = IdentityDictionary();
	}

	*reset {
		requireTable.clear();
	}

	*new {
		arg identifier, cmdPeriod = false, always = false;
		^this.require(identifier, cmdPeriod, always);
	}

	// A resolveRelative that always assumes the interpreter as parent.
	*resolveRelative {
		arg str;
		var path = thisProcess.nowExecutingPath;

		if (str[0] == thisProcess.platform.pathSeparator) {^str};
		if (path.isNil) { ^str }; // It's okay if path is nil, just always resolve absolutely.
		^(path.dirname +/+ str)
	}

	*pathMatch {
		|str|
		var result = str.pathMatch;
		^result.select({
			|path|
			path.endsWith(".scd");
		})
	}

	*canonicalPath {
		|path|
		^File.realpath(PathName(path).asAbsolutePath().standardizePath).asSymbol();
	}

	*require {
		arg identifier, cmdPeriod = false, always = false;
		var paths, results, caller;

		// Don't allow wildcard to be executed in a root directory.
		if (identifier.contains(thisProcess.platform.pathSeparator).not
			&& (identifier.contains("?") || identifier.contains("*")) )
		{
			identifier = "." +/+ identifier;
		};

		// First match as if an absolute path
		paths = this.pathMatch(identifier);

		// Then relative
		if (paths.isEmpty()) {
			paths = this.pathMatch(this.resolveRelative(identifier));
		};

		// Then relative with implicit ./
		if (paths.isEmpty() && (identifier[0] != ".")) {
			identifier = "." +/+ identifier;
			paths = this.pathMatch(this.resolveRelative(identifier));
		};

		// Then relative with implicit extension
		if (paths.isEmpty() && identifier.endsWith(".scd").not) {
			identifier = identifier ++ ".scd";
			paths = this.pathMatch(this.resolveRelative(identifier));
		};

		if (paths.isEmpty) {
			Exception("No files found for Require(%)! (executing from: %)".format(identifier, thisProcess.nowExecutingPath).warn).throw;
		} {
			var results = paths.collect({
				|path|
				var requiredFile, oldPath, func;

				requiredFile = RequiredFile();
				requiredFile.path = this.canonicalPath(path);
				requiredFile.mtime = File.mtime(requiredFile.path);

				if (always or: {requireTable[requiredFile.path].isNil or:{ requireTable[requiredFile.path].mtime != requiredFile.mtime}}) {
					oldPath = thisProcess.nowExecutingPath;
					thisProcess.nowExecutingPath = requiredFile.path;

					try {
						func = thisProcess.interpreter.compileFile(requiredFile.path.asString);
						if (func.isNil) { Exception().throw() }; // failed to compile
						requiredFile.result = func.value();
						requireTable[requiredFile.path] = requiredFile;
					} {
						|e|
						"Require of file % failed!".format(requiredFile.path).error;
						requireTable[requiredFile.path] = nil;
						e.throw();
					};

					thisProcess.nowExecutingPath = oldPath;

					if (cmdPeriod) {
						CmdPeriod.doOnce({
							requireTable[requiredFile.path] = nil;
						})
					}
				} {
					requiredFile = requireTable[requiredFile.path];
				};

				requiredFile.result;
			});

			if (results.size == 1) {
				^results[0];
			} {
				^results;
			}
		};
	}
}