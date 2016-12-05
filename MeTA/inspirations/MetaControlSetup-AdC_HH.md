MetaControlSetup -- a music-making architecture
===============================================

## Glossar

+ ```q``` -- global dictionary holding everything



## File system structure

+ ```0_globals``` -- tools and utilities
    * server config
    * sample loading routine
    * network config
    * setting up master FX
    * variables to find/open all files easily
        - q.globalsDir = thisProcess.nowExecutingPath.dirname;
        - q.topDir = q.utilDir.dirname;
        - q.fulldirnames = (q.topDir +/+ "*/").pathMatch;
        - q.dirnames = q.fulldirnames.collect { |path| path.basename };
    * preset management
+ ```2_processes``` -- audio engines
    * typically ```Ndef```, ```Tdef```, or ```Pdef```
    * loading one file loads an entire process and its side-info in one go.
    * filename equals process-name + ```.scd```
    * responds to the interface
        - ```play``` -- makes the process audible
        - ```stop``` -- mutes the process, continues playing in the background
        - ```pause``` -- pauses rendering (not audible anymore)
        - ```resume``` -- resumes rendering
        - ```controlKeys``` -- possible mapping points (*parameters*)
        - ```getSpec``` -- all ControlSpecs
        - ```getSpec(<controlKey>)```  -- ```ControlSpec``` for a controlKey
            + set e.g. via ```Ndef(\blonk).addSpec(\blink, [1, 10, \exp]);```
    * does not ```play``` itself during loading.
+ ```3_interfaces``` -- set-up of controllers
    * grabs a controller and assigns functionality to it.
    * this specifically includes mapping of parameters to processes.
+ ```4_samples``` -- samples to load
    * subdirectories contain sample-packs
        - load sample-packs into ```Buffer```s via ```loadToBuffer```-util.

