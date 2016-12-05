// open files within the Modality workshop folder

~basePath = "".resolveRelative;

// installing Modality and other quarks for this workshop:
(~basePath +/+ "0_installing.scd").openDocument;

// finding and defining devices:
(~basePath +/+ "1_finding_and_defining_devices_hid.scd").openDocument;
(~basePath +/+ "1_finding_and_defining_devices_midi.scd").openDocument;

// exploring elements and actions:
(~basePath +/+ "2_elements_and_actions_hid.scd").openDocument;
(~basePath +/+ "2_elements_and_actions_midi.scd").openDocument;

// using ndefs:
(~basePath +/+ "3_using_ndefs_hid.scd").openDocument;
(~basePath +/+ "3_using_ndefs_midi.scd").openDocument;


// some shared sound processes
(~basePath +/+ "shared_processes").openOS;

(~basePath +/+ "shared_processes/*").pathMatch.collect(_.basename).postcs;
(~basePath +/+ "shared_processes/*").pathMatch.choose.openDocument;
// open one of them:
(~basePath +/+ "shared_processes/cat.scd").openDocument;
// load one directly, e.g. for use in controller patches:
(~basePath +/+ "shared_processes/cat.scd").load;

// ... you are welcome to share your own sound processes here!

// some more modal functionality using MFunc:
(~basePath +/+ "5_modal_functionality_2_hid.scd").openDocument;
(~basePath +/+ "5_modal_functionality_2_midi.scd").openDocument;
(~basePath +/+ "5_modal_functions_2b_midi.scd").openDocument;

