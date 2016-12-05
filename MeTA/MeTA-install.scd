/*
# MeTA -- a structured environment for SuperCollider performance system development.
(cc-by-ny) 2015 Till Bovermann, ...

http://3DMIN.org | http://modality.github.io | http://tai-studio.org

This is a script to create the basic MeTA filesystem structure.
Please provide

+ basePath    -- where should everything be installed to?
+ projectName -- the name of your project

All files in subdirectory ```proto``` are installed via the unix-command ```install```.

## ToDo

+ select which parts/feature-sets to install
+ GUI?
+ as Class?

*/

var q = ();
q.slash = Platform.pathSeparator;

	// put path basePath for your setup here
	// this path must exist.
q.basePath = ("HOME".getenv +/+ "tmp").pathMatch.first;

	// your project's name
	// this name should be unique
q.projectName = "perform";

q.mEtaDir = thisProcess.nowExecutingPath.dirname;
q.mEtaProtoDir = q.mEtaDir +/+ "proto".standardizePath;


// ---------------------- Assembly -------------------------------------

// ---------------------- do not change anything below -----------------
q.listCmd = "ls -1AbpP"; // print all files in a row, dirs have a trailing slash

q.getSubDirs = {|q, path, parentDirs|
	var paths = "% %".format(q.listCmd, path +/+ parentDirs).postln
		.unixCmdGetStdOutLines.collect(_.escapeChar($ ));

	paths.notEmpty.if({
		paths.inject([parentDirs.drop(1).drop(-1)], {|last, elem|
			elem = parentDirs +/+ elem;
			last ++
				(elem.last == q.slash).if({
					[q.getSubDirs(path, elem)];
				}, {
					[elem.drop(1)]
				})
		})
	}, {
		parentDirs.drop(1).drop(-1); // get rid of trailing slash
	})
};


q.mEtaProtoFiles = q.getSubDirs(q.mEtaProtoDir, "").flatNoString; //WSlib dependancy


q.projectDir = q.basePath +/+ q.projectName;

q.install = {|q, projectDir, protoDir, protos|
	var stamp = Date.getDate.stamp;
	protos.do{|proto|
		("file -b %".format(protoDir +/+ proto).unixCmdGetStdOut == "directory\n").if({
			"install -d %".format(projectDir +/+ proto).postln.unixCmd;
		}, {
			"install -B .%.old -bC % %".format(
				stamp,
				protoDir +/+ proto,
				projectDir +/+ proto
			).postln.unixCmd;
		})
	};

	// write README.md as README-MeTA.md
	"install -B .%.old -bC % %".format(
		stamp,
		q.mEtaDir +/+ "README.md",
		projectDir +/+ "README-MeTA.md"
	).postln.unixCmd;
};

q.install(q.projectDir, q.mEtaProtoDir, q.mEtaProtoFiles);

q.projectDir.openOS;


/*
"rm -rf %".format(q.projectDir).unixCmd; // */