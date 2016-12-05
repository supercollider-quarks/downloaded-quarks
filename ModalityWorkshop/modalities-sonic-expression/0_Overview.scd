// =====================================================================
// Modalities of Sonic Expression
// =====================================================================

// Installing:
Quarks.gui;

// click: check for updates
// - install Modality-toolkit

// get the workshop files from:
https://github.com/ModalityTeam/ModalityWorkshop


"1_Interfaces_to_Instruments.scd".resolveRelative.openDocument;


// Basic introduction
"Modality".openHelpFile;
"Modality Tutorial".openHelpFile;

MKtl(\nk2, "korg-nanokontrol2");
// Basic GUI showing all elements,
// or fancy gui when layout geometry is known in desc file
MKtl(\nk2).gui;


// Mapping examples
"2_mapping.scd".resolveRelative.openDocument;
"3_serversidemethods.scd".resolveRelative.openDocument;
// - buttons
"4_Buttons.scd".resolveRelative.openDocument;
// - sliders
"4_Sliders.scd".resolveRelative.openDocument;

// creating description files
"How to create a description file".openHelpFile;
