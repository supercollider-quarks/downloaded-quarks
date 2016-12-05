
	// read in example text files
(
b = ();	// maybe set path by hand if needed
(thisProcess.nowExecutingPath.dirname +/+ "*.txt").pathMatch.do { |path|
	var textName = path.basename.splitext[0].asSymbol.postcs;
	var file = File(path, "r");
	var lines = file.readAllString.split($@);
	if (file.isOpen) {
		lines =
		b[textName] = lines;
		file.close;
	};
};
)
	// make a number of new text generations,
	// and compare them for occurrence within originals.

b.genTextSimple = { arg dict, numGenerations, corpus, starterLength, minDepth;
	var corpusSnake = ContextSnake(corpus, minDepth: minDepth, starterLength: starterLength);
	var count = 0, result;

	while({ count < numGenerations },
		{
			result = corpusSnake.randStarter.asStream.all.join;
			if (corpusSnake.isNew(result),
  			{ 	count = count+1;
  				result.postcs;
  			})

     });
};
)
	// check they are there
b.erlkoenig.printAll;"";
b.erlEng.printAll;"";
b.billOfRights.printAll;"";

7.do { b.genTextSimple(1, b.billOfRights, 4, 4) };
7.do { b.genTextSimple(1, b.erlEng, 4, 4) };
7.do { b.genTextSimple(1, b.erlkoenig, 4, 4) };
