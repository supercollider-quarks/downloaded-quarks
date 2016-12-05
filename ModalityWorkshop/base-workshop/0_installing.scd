/*
Modality Toolkit

The *Modality Toolkit* is a library to facilitate accessing (hardware) controllers in SuperCollider.
It is designed and developed by the ModalityTeam, a group of people that see themselves as both developers and (advanced) users of SuperCollider.

The central idea behind the Modality-toolkit is to simplify creation of individual (electronic) instruments with SuperCollider, using controllers of various kinds. To this end, a common code interface, MKtl, is used for connecting  controllers from various sources (and protocols). These are atm. HID, MIDI, and OSC; SerialPort and GUI-based are planned to be integrated.

The name *Modality* arose from the idea to scaffold the creation of modal interfaces, i.e. to create interfaces where one physical controller can be used for different purposes and it is possible to *change its modes of functionality, even at runtime*.
It is our belief that integration of such on-the-fly remapping features helps to create setups which are much more flexible, powerful, and interesting to play.
Such modal interfaces allow coping with fast changes of overall direction when that is musically desirable, e.g. when improvising with musicians playing acoustic instruments.
*/

//========= Installation with version 3.7 =========

Quarks.updateDirectory;
Quarks.install("Modality-toolkit");
// and reboot interpreter

//========= Additional quarks for this workshop: =========

// install the additional quarks:
Quarks.updateDirectory;
// if you get an error on OSX in the terminal that pops up that says something about the svn version being outdated, use this line to set the svn path:
QuarkSVNRepository.svnpath_( "/opt/local/bin/svn" );

// update to the latest versions of the quarks that we need:
Quarks.update("JITLibExtensions");
Quarks.update("adclib");
Quarks.update("SpeakersCorner");

// install the quarks that we need:
Quarks.install("JITLibExtensions");
Quarks.install("adclib");
Quarks.install("SpeakersCorner");

// and recompile the language to use it all:
thisProcess.recompile;


//========= Using GIT and the terminal: =========

// This is only needed in case Quarks install does not work properly!

// - open the terminal and type:
git clone https://github.com/ModalityTeam/Modality-toolkit
// - check the path where you cloned it:
// e.g.
/home/nescivi/git/Modality/Modality-toolkit/Modality/

// - then on Linux in the terminal:
cd ~/.local/share/SuperCollider/Extensions
ln -s /home/nescivi/git/Modality/Modality-toolkit/Modality/
// - and recompile sclang

// - on MacOSX:
cd ~/Library/Application\ Support/SuperCollider/Extensions/
ln -s /Users/nescivi/git/Modality/Modality-toolkit/Modality/

//========= Using the zip download link: =========

// Download the toolkit from:
/*
https://github.com/ModalityTeam/Modality-toolkit/archive/master.zip
*/
// Unpack the toolkit (doubleclick, or rightclick and choose unpack...)

// And copy the folder Modality to your Extensions folder.

// You can open the extensions folder with this little line of SC code
Platform.userAppSupportDir.openOS;

