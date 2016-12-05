
DocumentFile {

	classvar <dir;

	*open { arg path;
		if(\Document.asClass.notNil, {
			Document.open(path);
		}, {
			path = this.standardizePath(path);
			("open " + path.escapeChar($ )).unixCmd;
		});
	}

	*dir_ { arg path;
		dir = path.standardizePath(path);
	}

	*standardizePath { | p |
		var pathName;
		pathName = PathName(p.standardizePath);
		^if(pathName.isRelativePath) {
			dir  ++ pathName.fullPath
		} {
			pathName.fullPath
		}
	}

	*abrevPath { | path |
		if(path.size < dir.size) { ^path };
		if(path.copyRange(0, dir.size - 1) == dir) {
			^path.copyRange(dir.size, path.size - 1)
		};
		^path
	}
}
