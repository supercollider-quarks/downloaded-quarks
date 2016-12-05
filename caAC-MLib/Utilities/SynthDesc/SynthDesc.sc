+ SynthDesc {

	*readDef { |synthDefName|
		^this.read(SynthDef.synthDefDir ++ synthDefName ++ ".scsyndef")[synthDefName];
	}

	*storeMetaData { |synthDefName, metaData|
		var path = SynthDef.synthDefDir ++ synthDefName ++ ".txarcmeta";
		if (metaData.notNil) {
			TextArchiveMDPlugin.writeMetadataFile(metaData, synthDefName, path);
		}
	}

}