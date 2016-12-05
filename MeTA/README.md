MeTA
====
*as of 06.04.2016, Till Bovermann*
[ [TAI-studio](http://tai-studio.org) | [3DMIN](http://3DMIN.org) | [Modality](http://modalityteam.github.io/)]

MeTA is a performance framework for [SuperCollider](http://supercollider.github.io). It consists of a guideline (this), a structured directory (directory: "proto") and a set of classes (directory "classes").

## Install

`MeTA-install.scd`
allows to install MeTa for a new project.
Evaluating it copies all items in the ```proto``` folder to a given project folder.

PLEASE PROVIDE THE PATH TO YOUR FOLDER BEFORE BLINDLY EVALUATING THE FILE!

MeTA requires you to install the directory "classes" to the SuperCollider extensions folder. If you installed it via the Quarks system, this should already be the case.

## File system structure

Foldernames and filenames may have an initial number that informs about their intended order of execution. If folders (or files within folders) have the same number, their execution does not rely on each other.
Common features such as directory and file access/evaluation are implemented in the MeTA class. See its Documentation for details.

+ ```main.scd``` -- your main performance file. This file comes as a barebone, i.e. it has a lot of stuff already in it which you you can (and should) customise to your liking.
+ ```tests.scd``` -- a notepad/scratchpad for repeating tests such as "post controller values" etc. Initially empty.
+ ```0_utils``` -- tools and utilities
    * add general functionality here, mainly stuff that (a) does not fit into the other categories and (b) is required for the rest of your setup.
    * since all utility functions necessary to run the MeTA system itself are implemented in the ```MeTA``` class, this folder is initially empty.
+ ```1_configs``` -- configuration files
    * server configuration – pre-filled configuration for your sound server. Adjust to the demands of your performance.
    * network configuration – pre-filled configuration for network access (useful e.g. for network performances). Adjust to the demands of your performance.
    * user configuration -- user configs like your artist name, specific hardware-related parameters etc. go here.
+ ```3_engines``` -- (audio/video) engines
    * typically ```Ndef``` (possibly also ```Tdef```, or ```Pdef```)
    * loading one file loads an entire process and its side-info in one go.
    * filename should be the same as the process-name + ```.scd```
    * when loaded with ```MeTA:loadGen```, a gen should conform to this interface:
        - ```m.gens[\name].getHalo(\onFunc)``` -- makes the process audible
        - ```m.gens[\name].getHalo(\offFunc)``` -- mutes the process, continues playing in the background
        - ```m.gens[\name].pause``` -- pauses rendering (not audible anymore)
        - ```m.gens[\name].resume``` -- resumes rendering
        - ```m.gens[\name].controlKeys``` -- possible mapping points (*parameters*)
        - ```m.gens[\name].getSpec``` -- all ControlSpecs
        - ```m.gens[\name].getSpec(<controlKey>)```  -- ```ControlSpec``` for a controlKey
            + set e.g. via ```m.gens[\name].addSpec(\blink, [1, 10, \exp]);```
    * does ```play``` itself during loading but remains muted (e.g. via an ```on```-parameter set to ```0```).
+ ```3_efx/aux``` -- auxilliary effects
    * loading one file loads an entire effect and its side-info in one go.
    * file name equals process-name + ```.scd```
    * typically ```Ndef```
    * has an ```In.ar(\in.ar)``` slot defined through which the input signal is (automatically) routed
    * output is fully wet
    * responds to the interface
        - ```play``` -- makes the process audible
        - ```stop``` -- mutes the process, continues playing in the background
        - ```pause``` -- pauses rendering (not audible anymore)
        - ```resume``` -- resumes rendering
        - ```controlKeys``` -- possible mapping points (*parameters*)
        - ```getSpec``` -- all ControlSpecs
        - ```getSpec(<controlKey>)```  -- ```ControlSpec``` for a controlKey
            + set e.g. via ```Ndef(\blonk).addSpec(\blink, [1, 10, \exp]);```
    * does ```play``` itself during loading.
    * assumes that all inputs are inserted via ```ProxySubmix(<filename>+'Aux')```
        - this is ensured if uax-effects are loaded via ```MeTA:loadAux```
+ ```3_controllers``` -- set-up of controllers
    * grabs controllers and makes it accessible via ```MeTA:ctls```
    * does _not_ implement the mapping (see ```mapping```-directory below)
+ ```3_helperNdefs``` -- Ndefs for common tasks 
    * here you can store Ndefs for amplitude tracking or ramping (needed e.g. for server-side quantisation)
+ ```5_mapping``` -- mapping strategies between sound engines and controllers.
    * automation of mapping routings between generators and controllers.
    * actual mapping is defined in the generator files (via ndef-Halo's, see gen-files for details). Here, only the actual connection between the controllers and those functions is implemented.

## Other directories

+ ```resources``` -- contains data needed for the performance (samples/photos/...)
    * subdirectory ```samples``` contains directories with sample-packs to be loaded into buffers
        - load sample-packs into ```Buffer```s via ```MeTA:loadSamples```
        - sample-Buffers accessible via ```MeTA:samples```
    * subdirectory ```images``` contains images (no loading routine implemented yet)
    * subdirectory ```midi``` contains midi-files (no loading routine implemented yet)

## Acknowledgements

Alberto de Campo and Hannes Hoelzl provided an initial "structured directory" and many of the configuration files and routines, as well as lots of valuable ideas.
Dominik Hildebrand Marques Lopes and Amelie Hinrichsen did lots of testing and suggesting.

3DMIN -- MeTA was initiated within [3DMIN](http://3dmin.org), a project funded by [Einsteinstiftung Berlin](http://www.einsteinfoundation.de/).
Modality -- controller access in SuperCollider made easy by nice people!

