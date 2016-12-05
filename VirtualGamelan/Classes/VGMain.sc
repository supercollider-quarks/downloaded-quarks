VGMain {
    classvar <vggDir, <klenenganDir, <mainGuiDir, <pdfDir, <>synthDataDir, <>mixerPresetsDir, <>samplesDir;
    classvar <klenenganFiles, <dataFiles, <tafsiranFiles, <testFiles, <garapFiles, <mainGuiFiles;
    classvar <defaultsFile, <resetKlenenganFile, <>synthDataFixFuncFile, <>balanceFile, <>synthDataFile, <>sonatorListFile;
    classvar <>currentGamelan, <>sampSetPrefix;
	*initClass {
		vggDir =  Platform.userAppSupportDir.asString +/+ "vgg";
		klenenganDir = vggDir +/+ "Klenengan";
		mainGuiDir = vggDir +/+ "Gui";
		defaultsFile = vggDir +/+ "Defaults.scd";
        resetKlenenganFile = klenenganDir +/+ "01_Reset.scd";
		klenenganFiles = pathMatch(klenenganDir +/+ "*");
        dataFiles = pathMatch(klenenganDir +/+ "02*");
        tafsiranFiles =  pathMatch(klenenganDir +/+ "03*");
        testFiles =  pathMatch(klenenganDir +/+ "04*");
        garapFiles =  pathMatch(klenenganDir +/+ "05*");
		mainGuiFiles = pathMatch(mainGuiDir +/+ "*");
        currentGamelan = "gamKUG";
        sampSetPrefix = currentGamelan ++ "_";
		synthDataDir = Platform.userAppSupportDir +/+ "vgg_synthData" +/+ currentGamelan;
        synthDataFixFuncFile = synthDataDir +/+ "SynthDataFixFunc.scd";
        synthDataFile = synthDataDir +/+ "synthData.txt";
        sonatorListFile = synthDataDir +/+ "sonatorNames.txt";
        balanceFile = synthDataDir +/+ "ampBalancePitch.scd";
        mixerPresetsDir = synthDataDir +/+ "MixerPresets/";
        samplesDir = synthDataDir +/+ "Samples/";
        pdfDir = vggDir +/+ "Materials/BalunganBarry";
		"\nVGMain: vggDir = %\n".postf(vggDir);
		"VGMain: synthDataDir = %\n\n".postf(synthDataDir);
		SkipJack.verbose_(false);	// reduce clutter
    }
	*startUp {
        (vggDir +/+ "COPYRIGHT.txt").load.postln;
        VGSound.startUp({
            "Server synced - Starting to load klenengan-files...".postln;
            {
                try {
                    VGMain.defaultsFile.load;
                    VGMain.klenenganFiles.do(_.load);
                    0.1.wait;
                    (VGMain.mainGuiDir +/+ "02_MainGuiData.scd").load;
                     "All Klenengan-files loaded successfully!".postln;
				} {
					"Klenengan files load failed!".postln;
                };
                0.5.wait;
                (VGMain.mainGuiDir +/+ "04_MainGui.scd").load;
            }.fork(AppClock);
		});
	}
}

