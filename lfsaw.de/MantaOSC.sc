// Simple interface to MantaOSC as found in libManta.
// TODO: test SliderLED

MantaOSC {
classvar <>pathToBinary, <>binaryName;

var <mantaAddr;
var padVelResponder, padConResponder, butVelResponder, butConResponder, sliderConResponder;
var <>padVelAction, <>padConAction, <>butVelAction, <>butConAction, <>sliderConAction;
var <padMappingTable, <inverseMappingTable;

*initClass {
	pathToBinary = "/localvol/sound/src/libmanta/MantaOSC/build/";
	binaryName = "MantaOSC";
}


*new {|addr, mappingTable|
	^super.new.init(addr)
}
init {|addr, mappingTable|
	mantaAddr = addr ?? {NetAddr("127.0.0.1", 31417)};
	// initialise responders	 (without starting)
	// Pad
	this.padMappingTable_(mappingTable ?? {(0..47)});

	padVelResponder = OSCFunc({|msg, time|
		var idx = msg[1];
		var val = msg[2];
		padVelAction.value(padMappingTable[idx], val, time, this);
	}, "/manta/velocity/pad", srcID: mantaAddr).disable;

	padConResponder = OSCFunc({|msg, time|
		var idx = msg[1];
		var val = msg[2];
		padConAction.value(padMappingTable[idx], val, time, this);
	}, "/manta/continuous/pad", srcID: mantaAddr).disable;

	// Button
	butVelResponder = OSCFunc({|msg, time|
		var idx = msg[1];
		var val = msg[2];
		butVelAction.value(idx, val, time, this);
	}, "/manta/velocity/button", srcID: mantaAddr).disable;

	butConResponder = OSCFunc({|msg, time|
		var idx = msg[1];
		var val = msg[2];
		butConAction.value(idx, val, time, this);
	}, "/manta/continuous/button", srcID: mantaAddr).disable;

	// Slider
	sliderConResponder = OSCFunc({|msg, time|
		var idx = msg[1];
		var val = msg[2];
		sliderConAction.value(idx, val, time, this);
	}, "/manta/continuous/slider", srcID: mantaAddr).disable;
}

runBinary {
	"%/% % % %".format(pathToBinary, binaryName, 0, mantaAddr.port, NetAddr.localAddr.port).runInTerminal;
}

padsEnabled{|velEnabled=true, conEnabled=true|
	velEnabled.if({
		padVelResponder.enable;
	},{
		padVelResponder.disable;
	});
	conEnabled.if({
		padConResponder.enable;
	},{
		padConResponder.disable;
	});
}
buttonsEnabled{|velEnabled=true, conEnabled=true|
	velEnabled.if({
		butVelResponder.enable;
	},{
		butVelResponder.disable;
	});
	conEnabled.if({
		butConResponder.enable;
	},{
		butConResponder.disable;
	});
}
slidersEnabled{|enabled=true|
	enabled.if({
		sliderConResponder.enable;
	},{
		sliderConResponder.disable;
	});
}


start { // starts listening
	this.padsEnabled;
	this.buttonsEnabled;
	this.slidersEnabled;
}

stop { // stops listening
	[
		padVelResponder, padConResponder,
		butVelResponder, butConResponder,
	sliderConResponder
	].do(_.disable);
}

/// LEDs
setLEDControl {|mode, state|
	// mode == \padandbutton
	// mode == \slider
	// mode == \button
	// state: true or false
	mantaAddr.sendMsg("/manta/ledcontrol", mode, state.asInteger);
}
setPadLED {|num, mode|
	// mode == \amber
	// mode == \red
	// mode == \off

	[mode, num.asArray.collect{|idx| inverseMappingTable[idx]}].flop.do{|args|
		args[1].do{|idx|
			mantaAddr.sendMsg("/manta/led/pad", args[0], idx);
		}
	}
}
setRawPadLED {|num, mode|
	// mode == \amber
	// mode == \red
	// mode == \off

	[mode, num].flop.do{|args|
			mantaAddr.sendMsg("/manta/led/pad", *args);
	}
}


setButtonLED {|num, mode|
	// mode == \amber
	// mode == \red
	// mode == \off

	[mode, num].flop.do{|args|
		mantaAddr.sendMsg("/manta/led/button", *args);
	}
}
setSliderLED {|num, mode, val|
	// num: 0 (upper), 1 (lower)
	// mode == \amber
	// mode == \red
	// mode == \off

	mantaAddr.sendMsg("/manta/led/slider", mode, num, val);
}
maxIndex {
	^padMappingTable.maxItem;
}
padMappingTable_{|table|
	// table -- array of indices to which each pad links to.
	padMappingTable = table;
	inverseMappingTable = (table.maxItem+1).collect{|idx|
		var res = Array.new;
//		table.detectAll({|val| val == idx});
		table.do {|elem, i|
				(elem == idx).if{
					res = res.add(i)
			}
		};
		res;
	};

}
}