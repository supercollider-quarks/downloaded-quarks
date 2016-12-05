+ String {

	pathType { // types are : \empty, \multi, \dir and \file // and maybe \bundle
		var paths = this.pathMatch;
		var path;

		^case
		{ paths.size > 1 } { \multi }
		{ path = paths[0]; path.isNil } { \empty }
		{ (path.last == Platform.pathSeparator).not } { \file }
	//	{ path.pathIsBundle } { \bundle }
		{ \folder }
	}

	// only if and when needed, better check this separately
	pathIsBundle { |bundleExts|
		var ext = this.splitext.last;
		^(bundleExts.notNil and: { bundleExts.includesEqual(ext) })
		or: (ext.size > 0)
			and: { ext.every(_ == $ ).not
				and: { ext[0].isAlpha }
		}
	}
}

