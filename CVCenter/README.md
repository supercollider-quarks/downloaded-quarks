CVCenter is a container of CVs as well as it provides a graphical control-interface that lets you connect synth-controls or controls within patterns to hardware midi-sliders or OSC-controllers. A CV models a value constrained by a ControlSpec. Within CVCenter they get represented within CVWidgets that can hold one or more CVs. Each of them can be connected to an unlimited number GUI-elements or/and control parameters in running Synths or set values in Patterns. As CV inherits from Stream any instance can be used as any other Pattern (Pseq, Prand, Pwhite etcetc.).

For more info check out the CVCenter- resp. CVWidget-helpfile.

Installation
------------
1. get SuperCollider from https://supercollider.github.io/download or install SuperCollider from source (For more instructions see: http://supercollider.github.io/development/building.html and consult the specific READMEs that come with the program).
2. install the required extensions via the Quarks-mechanism:
	- TabbedView2
	- wslib (optional)
	- cruciallib (optional)  
	see the Quarks-helpfile for more information on how to do this.
3. CVCenter uses its own version of the Conductor library. **Do not use the version that can be installed via Quarks!** Instead use this repository (which is a submodule of CVCenter): https://github.com/nuss/Conductor
4. after installing SuperCollider and the required extensions put all content of CVCenter (and Conductor - if you haven't cloned the library as a submodule with CVCenter) in your user-app-support directory. Execute the following line SuperCollider to see where that is:

		Platform.userExtensionDir

	Under OSX this will resolve to:

		~/Library/Application Support/SuperCollider/Extensions

	Under Linux this will resolve to:

		~/.local/share/SuperCollider/Extensions

	Using Windows the mechanism should apply as well. However, the author of this document currently doesn't know what the result of the query will be...

Note: if you're on Linux you will need to have installed SuperCollider >= 3.5 as CVCenter depends on QtGUI. Under MacOSX CVCenter *should* be compatible with SC >= 3.4 resp. QtGUI as well as Cocoa. 
Under Windows it's recommended to use the latest version of SuperCollider as it comes with full Qt-support and the new SC-IDE. Get it here: http://sourceforge.net/projects/supercollider/files/Windows/3.6/SuperCollider-3.6.6-win32.exe/download

Using CVCenter with the current stable version of SuperCollider (3.7.2)
-----------------------------------------------------------------------
Using CVCenter with SC 3.7 and higher requires a fix due to the removal of GUI redirects in SC 3.7 (Cocoa is no longer supported. Hence, redirects have been removed). The fix is not yet contained within version of Conductor that can be installed via SC's quarks repository. You may install a modified version of the Conductor library by cloning it as a submodule of CVCenter:

1. on a fresh installation of CVCenter by cloning the repository *recursively*:

		$ git clone --recursive https://github.com/nuss/CVCenter.git

2. if you have already installed CVCenter by cloning the repository issue the following commands from within your CVCenter directory

		$ git submodule init
		$ git submodule update

3. for subsequent updates via git

		$ git pull && git submodule update
