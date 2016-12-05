// create new MeTA object

m = MeTA("/localvol/3DMin/projects/pushpull/pushPullOSC-perfBarebones");


m.dump

m.dirnames
m.server; // defaults to Server.default

m.loadedFiles.asAssociations.printAll; // nothing loaded yet


m.postServerOptions

// explicitely removed wildcards
m.filePaths(\gen, ["drumsDHMLe", "drumsDHML"], warn: false); // displays a warning
m.filePaths(\gen, ["drumsDHMLe", "drumsDHML"], warn: false)



m.openFiles(\gen, ["test"], "scd")
m.loadFiles(\gen, ["test"], "scd")

MeTA_Utils.slash
MeTA_Utils.getFilteredPaths("/localvol/3DMin/projects/pushpull/pushPullOSC-perfBarebones/resources/samples", ["wav"])


m.loadSamples("drums")
s.boot
m.loadSamples("drums")
m.samples.drums.choose.play
m.loadSamples("drums", true)
