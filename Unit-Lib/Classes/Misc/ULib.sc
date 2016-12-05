/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2013 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

ULib {
    classvar <>servers;

    *initClass {
        servers = [Server.default]
    }

	*allServers {
        ^servers.collect{ |s|
            if( s.isKindOf( LoadBalancer ) ) {
                s.servers
            } {
                s
            }
        }.flat
    }

	*waitForServersToBoot {
        while({ this.allServers.collect( _.serverRunning ).every( _ == true ).not; },
            { 0.2.wait; });
    }

	*sync {
        this.allServers.do( _.sync )
    }

    *sendDefs { |defs|
        this.allServers.do{ |s|
            defs.do{ |def|
                def.send(s)
            }
        }
    }

    *serversWindow {
        var makePlotTree, makeMeter, killer;
        var servers = ULib.allServers;
        var w = Window("ULib servers", Rect(10, 10, 390, 3 + ( servers.size * 29))).front;
        w.addFlowLayout;
        killer = Button(w, Rect(0,0, 20, 18));
        killer.states = [["K"]];
        killer.canFocus = false;
        killer.action = { Server.killAll };
        w.view.decorator.nextLine;
        servers.do{ |s| s.makeView(w) };
        w.view.keyDownAction = { arg view, char, modifiers;
            // if any modifiers except shift key are pressed, skip action
            if(modifiers & 16515072 == 0) {

                case
				{char === $n } { fork{ servers.do{ |s| s.queryAllNodes(false); 0.5.wait; } } }
				{char === $N } { fork{ servers.do{ |s| s.queryAllNodes(true); 0.5.wait; } } }
                {char === $l } { makeMeter.() }
                {char === $p}  { makePlotTree.() }
                {char === $ }  { servers.do{ |s| if(s.serverRunning.not) { s.boot } } }
            };
        };
        makePlotTree = {
            var onClose, comp;
            var servers = ULib.allServers;
            var window = Window.new("Node Tree(s)",
                Rect(128, 64, 1000, 400),
                scroll:true
            ).front;
            var x = CompositeView(window.view, Rect(0,0,4000,4000));
            x.addFlowLayout(0@0,0@0);
            comp = servers.collect{ CompositeView(x,400@400) };
            window.view.hasHorizontalScroller_(false).background_(Color.grey(0.9));
            onClose = [servers, comp].flopWith{ |s,c| s.plotTreeView(0.5, c, { defer {window.close}; }) };
            window.onClose = {
                onClose.do( _.value );
            };
        };
        makeMeter = {
            var window = Window.new("Meter",
                Rect(128, 64, 1000, 1000),
            ).front;
            var x = CompositeView(window.view, Rect(0,0, 1000, 1000));
            x.addFlowLayout;
            servers.do{ |s|
                var numIns = s.options.numInputBusChannels;
                var numOuts = s.options.numOutputBusChannels;
                ServerMeterView(s, x, 0@0, numIns, numOuts)
            }
        };
        ^w
    }

	*startup { |sendDefsOnInit = true, createServers = false, numServers = 4, options, startGuis = true|

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

		UnitRack.defsFolders = UnitRack.defsFolders.add(
			Platform.userAppSupportDir ++ "/UnitRacks/";
		);

		if(createServers) {
			if(numServers > 1) {
			servers = [LoadBalancer(*numServers.collect{ |i|
				Server("ULib server "++(i+1), NetAddr("127.0.0.1",57110+i), options)
			})];
			}{
				servers = [Server("ULib server", NetAddr("127.0.0.1",57110), options)]
			};
			Server.default = this.allServers[0]
		};

		if( startGuis ) {
			if( (thisProcess.platform.class.asSymbol == 'OSXPlatform') && {
				thisProcess.platform.ideName.asSymbol === \scapp
			}) {
				UMenuBar();
			} {
				UMenuWindow();
			};
			UGlobalGain.gui;
			UGlobalEQ.gui;
			if( ((thisProcess.platform.ideName == "scqt") && (ULib.allServers.size == 1)).not  ) {
				ULib.serversWindow
			}
		};


		//if not sending the defs they should have been written to disk once before
		// with writeDefaultSynthDefs
		if( sendDefsOnInit ) {
			var defs = this.getDefaultSynthDefs;
			ULib.allServers.do{ |sv| sv.waitForBoot({

				defs.do( _.load( sv ) );

			})
			}
		} {
			ULib.allServers.do(_.boot);
			Udef.loadOnInit = false;
			this.getDefaultUdefs;
			Udef.loadOnInit = true;
        };

		"\n\tUnit Lib started".postln
	}

	*getDefaultUdefs{
		^(Udef.loadAllFromDefaultDirectory ++
			UMapDef.loadAllFromDefaultDirectory).select(_.notNil)
	}

	*getDefaultSynthDefs{
		var defs;
		Udef.loadOnInit = false;
		defs = this.getDefaultUdefs.collect(_.synthDef).flat.select(_.notNil);
		Udef.loadOnInit = true;
		^defs

	}

	*writeDefaultSynthDefs {
		this.getDefaultSynthDefs.do{ |def|
			"writting % SynthDef file".format(def.name).postln;
			def.justWriteDefFile;
		}
	}

}


	