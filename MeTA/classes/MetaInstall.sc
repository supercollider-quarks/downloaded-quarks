
MeTAInstall {
	*getSubDirs { |path, parentDirs|
		var paths = "% %".format(listCmd, path +/+ parentDirs)
		.unixCmdGetStdOutLines.collect(_.escapeChar($ ));
		var listCmd = "ls -1AbpP"; // print all files in a row, dirs have a trailing slash

		^paths.notEmpty.if({
			paths.inject([parentDirs.drop(1).drop(-1)], {|last, elem|
				elem = parentDirs +/+ elem;
				last ++
				(elem.last.isPathSeparator).if({
					[this.getSubDirs(path, elem)];
				}, {
					[elem.drop(1)]
				})
			})
		}, {
			parentDirs.drop(1).drop(-1); // get rid of trailing slash
		})
	}

	*copyProto { |projectDir, protoDir, protos|
		var stamp = Date.getDate.stamp;
		protos.do { |proto|
			("file -b %".format(protoDir +/+ proto).unixCmdGetStdOut == "directory\n").if({
				"install -d %".format(projectDir +/+ proto).unixCmd;
			}, {
				"install -B .%.old -bC % %".format(
					stamp,
					protoDir +/+ proto,
					projectDir +/+ proto
				).unixCmd;
			})
		};

		// write README.md as README-MeTA.md
		"install -B .%.old -bC % %".format(
			stamp,
			protoDir.dirname +/+ "README.md",
			projectDir +/+ "README-MeTA.md"
		).unixCmd;
	}

	*make { |basePath = "~/Desktop/", projectName="MeTA_blank"|

		var projectDir = basePath +/+ projectName;

		var mEtaDir = MeTA.filenameSymbol.asString.dirname.dirname;
		var mEtaProtoDir = mEtaDir +/+ "proto".standardizePath;

		var mEtaProtoFiles = this.getSubDirs(mEtaProtoDir, "")
		.flatIf(_.isKindOf(String).not);

		this.copyProto(projectDir, mEtaProtoDir, mEtaProtoFiles);
		"// Open your new MeTA project dir with: \n"
		"%.openOS;\n\n".postf(projectDir.cs);
	}
}
