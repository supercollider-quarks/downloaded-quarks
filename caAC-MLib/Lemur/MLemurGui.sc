/*
Marinus Klaassen 2012

Version 0.4 of the MLemurGui Class.

Simple Lemur interface building with SuperCollider.

Color option. Lemur manual page 129 gives the formular to
convert rgb values with with values between 0.00 - 1.00
to a color range of 0 8355711. Color argument is set with
a Color object.

Font sizes are clipped between 8 and 24

- add knop

- With pads toggle and sequencers can be build. check editor!
*/


MLemurGui {
	classvar connections, buildPort = 8002, oscPort = 8000;
	var <current_ip, <buildInfo, <oscaddr, <>standardColor;

	*initClass {
		connections = IdentityDictionary.new;
	}

	*new { ^super.new; }

	connect { |ip = "192.10.1.2"|
		if (connections[ip] == nil, {
			connections[ip] = [
				NetAddr(ip,buildPort).connect,
				NetAddr(ip,oscPort)
			];
		});

		oscaddr = connections[ip].last;
		current_ip = ip; // current ip is stored in an instance variable to
		current_ip.postln;
		connections.postln;
		buildInfo = IdentityDictionary.new;
		standardColor = Color.blue;
	}

	disconnect {
		connections[current_ip] do: (_.disconnect);
		connections[current_ip] = nil;
	}

	sendPacket { |message|
		if (connections[current_ip].notNil, {
			connections[current_ip][0].sendMsg("/jzml",message.ascii.add(0).as(Int8Array));
			},{
				"evaluate method .connect".postln;
		});
	}

	setColor { |colorClass|
		if (colorClass.isNil) { colorClass = standardColor };
		colorClass = colorClass.asArray.copyRange(0,2).round(0.01).clip(0,1.0);
		colorClass = this.convertColor(*colorClass);
		^colorClass
	}

	convertColor { |r, g, b|
		var return = (r * 127 * 65536) + (g * 127 * 256) + (b * 127);
		^return
	}

	resetCode { ^"<RESET/>" }

	resetAll {
		// reset lemur and set the scheme name inside lemur-app
		var string = '<RESET/><OSC request="1"/><SYNCHRO mode="0"/><PROJECT title="more beer" version="3030" width="1024" height="724" osc_target="-2" midi_target="-2" kbmouse_target="-2"/>'.asString;
		this.sendPacket(string);
	}


	// Code snippets to add and remove a page.
	addPageCode { |pagename = "Default", x = 0, y = 0, width = 1024, height = 724|
		// name is also used when selecting a page name here and it is also the id name
		var string =
		'<WINDOW class="JAZZINTERFACE" text="%" x="%" y="%" width="%" height="%" state="1" group="0" font="tahoma,11,0" >'
		.asString.format(pagename, x, y, width, height);
		^string;
	}

	removePageCode { |pagename = "Default"|
		// this xml code removes a page of "name"
		var string =
		'<DELETE> <WINDOW class="JAZZINTERFACE" text="%" group="0"> </WINDOW> </DELETE>'
		.asString.format(pagename);
		^string;
	}

	addPage { |pagename = "Default", x = 0, y = 0, width = 1024, height = 724|
		this.sendPacket(this.addPageCode(pagename,x,y,width,height));
	}

	// remove page doen't work..
	removePage { |pagename = "Default"|
		this.sendPacket(this.removePageCode(pagename));
	}

	renamePage { |oldPageName, newPageName|
	var message = '<RENAME text="%"> <WINDOW class="JAZZINTERFACE" text="%"> "</RENAME>"'.asString.format(newPageName, oldPageName);
	this.sendPacket(message);
	}

	addObject { |objectType = "Fader", pageName = "Default", idName = "Default", x = 6, y = 15, width = 100, height = 678, color|
	var string =
	'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="%" text="%" x="%" y="%" width="%" height="%" id="1" state="1" group="0" font="tahoma,10,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" capture="1" color="%" cursor="0" grid="0" grid_steps="1" label="0" physic="1" precision="3" unit="" value="0" zoom="0.000000"> <PARAM name="x=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/x" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> <PARAM name="z=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/z"/> </WINDOW>'
	.asString.format(pageName, objectType, idName, x, y, width, height, this.setColor(color),idName, idName);
		this.sendPacket(string);
	}

	removeObject { |objectType = "Fader", pageName = "Default", idName = "Default"|
	var string =
	'<DELETE> <WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="%" text="%" id="1" state="1" group="0"> </WINDOW> </DELETE>'
	.asString.format(pageName, objectType, idName);
		this.sendPacket(string);
	}

	renameObjectReferenceName { |objectType = "Fader", pageName = "Default", oldIdName = "Default", newIdName = "Spanking"|
	var string =
	'<RENAME text="%"> <WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="%" text="%" id="1" state="1" group="0"> </WINDOW> </DELETE>'
	.asString.format(newIdName, pageName, objectType, oldIdName);
		this.sendPacket(string);
	}

	// Fader control add color control
	faderCode { | pagename = "Default", idname = "Fader1", x = 6, y = 15, width = 100, height = 678, color|
		// this will add a green slider.
		var string =
		'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="Fader" text="%" x="%" y="%" width="%" height="%" id="1" state="1" group="0" font="tahoma,10,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" capture="1" color="%" cursor="0" grid="0" grid_steps="1" label="0" physic="1" precision="3" unit="" value="0" zoom="0.000000"> <PARAM name="x=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/x" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> <PARAM name="z=" value="0.000000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/z"/> </WINDOW>'
		.asString.format(pagename, idname, x, y, width, height, this.setColor(color),idname, idname);
		^string;
	}

	removeFaderCode { |pagename = "Default", idname = "Fader1"|
		var string = '<WINDOW class="JAZZINTERFACE" text="%"> <DELETE> <WINDOW class="Fader" text="%" group="0" id="1"/></WINDOW> <DELETE>'
		.asString.format(pagename,idname);
		^string;
	}

	fader { | pagename = "Default", idname = "Fader1", x = 6, y = 15, width = 100, height = 678, color |
		this.sendPacket(this.faderCode(pagename,idname,x,y,width,height,color));
	}

	removeFader { |pagename = "Default", idname = "Fader1"|
		this.sendPacket(this.removeFaderCode(pagename,idname));
	}

	rangeCode { |pagename = "Default", idname = "Range1", x = 6, y = 15, width = 100, height = 678, color|
		// this will add a range slider
		^'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="Range" text="%" x="%" y="%" width="%" height="%" id="1" state="1" group="0" font="tahoma,10,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" capture="1" color="%" grid="0" grid_steps="1" horizontal="0" label="0" physic="0"> <PARAM name="x=" value="0.250000,0.750000" send="17" osc_target="0" osc_trigger="1" osc_message="/%/x" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" osc_scale="0.000000,1.000000" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/>'
		.asString.format(pagename, idname, x, y, width, height, this.setColor(color), idname);

	}

	removeRangeCode { |pagename = "Default", idname = "Range1"|
		^'<WINDOW class="JAZZINTERFACE" text="%"> <DELETE> <WINDOW class="Range" text="%" group="0" id="1"/></WINDOW> <DELETE>'
		.asString.format(pagename, idname);
	}

	range { |pagename = "Default", idname = "Range1", x = 6, y = 15, width = 100, height = 678, color |
		this.sendPacket(this.rangeCode(pagename,idname,x,y,width,height, color));
	}

	removeRange { |pagename = "Default", idname = "Range1"|
		this.sendPacket(this.removeRangeCode(pagename, idname));
	}

	textCode { |pagename = "Default", idname = "Text1", content = "parname", x = 6, y = 129, width = 100, height = 48, color, fontSize = 24|
		// this will add a text gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="Text" text="%" x="%" y="%" width="%" height="%" id="1" state="245" group="0" font="tahoma,%,0" send="1" osc_target="-2" midi_target="-2" kbmouse_target="-2" color="%" content="%"> <VARIABLE name="light=0" send="0" osc_target="0" osc_trigger="1" osc_message="/Text1/light" midi_target="-1" midi_trigger="1" midi_message="0x90,0x90,0,0" midi_scale="0,16383" kbmouse_target="-1" kbmouse_trigger="1" kbmouse_message="0,0,0" kbmouse_scale="0,1,0,1"/> </WINDOW>'
		.asString.format(pagename,idname,x,y,width,height,fontSize.asInt,this.setColor(color),content);
	}

	removeTextCode { |pagename = "Default", idname = "Text1", content = "parname", x = 6, y = 129, width = 100, height = 48|
		// this will add a text gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <DELETE> <WINDOW class="Text" text="%" group="0" id="1"/></WINDOW> <DELETE>'
		.asString.format(pagename, idname);
	}

	text { |pagename = "Default", idname = "Text1", content = "parname", x = 6, y = 129, width = 100, height = 48, color, fontSize = 24|
		this.sendPacket(this.textCode(pagename,idname,content,x,y,width,height,color,fontSize.clip(8,24)));
	}

	removeText { |pagename = "Default", idname = "Text1"|
		this.sendPacket(this.removeTextCode(pagename,idname));
	}

	padsCode { |pagename = "Default", idname = "Pads1", x = 200, y = 0, width = 100, height = 700, column = 1, row = 1, colorOff, colorOn|
		// this will add a pads gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="Pads" text="%" x="%" y="%" width="%" height="%" column="%" row="%" id="1" group="0" multilabel="1" label="0" color="%,%" radio="%"/> </WINDOW>'
		.asString.format(pagename,idname,x,y,width,height,column,row,this.setColor(colorOff),this.setColor(colorOn))
	}

	removePadsCode { |pagename = "Default", idname = "Pads1"|
		// this will remove a pads gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <DELETE> <WINDOW class="Pads" text="%" group="0" id="1"/></WINDOW> <DELETE>'
		.asString.format(pagename, idname);
	}

	pads { |pagename = "Default", idname = "Pads1", x = 200, y = 0, width = 100, height = 700, column = 1, row = 1, colorOff, colorOn|
		this.sendPacket(this.padsCode(pagename,idname,x,y,width,height,column,row,colorOff,colorOn));
	}

	removePads { |pagename = "Default", idname = "Pads1"|
		this.sendPacket(this.removePadsCode(pagename,idname));
	}

	switchesCode { |pagename = "Default", idname = "Switches1", x = 200, y = 0, width = 100, height = 700, column = 1, row = 1, colorOff, colorOn, radio = 0|
		// this will add a pads gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <WINDOW class="Switches" text="%" x="%" y="%" width="%" height="%" column="%" row="%" id="1" group="0" multilabel="1" label="0" color="%,%" radio="%"/> </WINDOW>'
		.asString.format(pagename,idname,x,y,width,height,column,row,this.setColor(colorOff),this.setColor(colorOn),radio)
	}

	removeSwitchesCode { |pagename = "Default", idname = "Switches"|
		// this will remove a pads gui
		^'<WINDOW class="JAZZINTERFACE" text="%"> <DELETE> <WINDOW class="Switches" text="%" group="0" id="1"/></WINDOW> <DELETE>'
		.asString.format(pagename, idname);
	}

	switches { |pagename = "Default", idname = "Switches1", x = 200, y = 0, width = 100, height = 700, column = 1, row = 1, colorOff, colorOn, radio = 0|
		this.sendPacket(this.switchesCode(pagename,idname,x,y,width,height,column,row,colorOff,colorOn),radio);
	}

	removeSwitches { |pagename = "Default", idname = "Switches1"|
		this.sendPacket(this.removeSwitchesCode(pagename,idname));
	}

	set_osctarget { |target_number=0,ip_host="192.10.1.16",port=57120|
		this.sendPacket('<OSC target=\"%\" ip=\"%\" port=\"%\"/>'.asString.format(target_number,ip_host,port));
	}

	buildBind { | bindGui, pageName, id = 1|
		var type,snippets,idNames,typeArray,parNames;
		typeArray = bindGui.typeArray; parNames = bindGui.nameArray;

		if ( typeArray.size == parNames.size, {
			snippets = snippets ++ this.addPageCode(pageName);

			parNames do: { |name, i|
				var type = typeArray[i];

				case
				{ type == \Fader }
				{       idNames = idNames ++ ["p" ++ id ++ "Fader" ++ i];
					snippets = snippets ++ this.faderCode(pageName,idNames.last,i * 100 + 6,15,100, 678);
				}
				{ type == \Range }
				{       idNames = idNames ++ ["p" ++ id ++ "Range" ++ i];
					snippets = snippets ++ this.rangeCode(pageName,idNames.last,i * 100 + 6,15,100, 678);
				};

				snippets = snippets ++ this.textCode(pageName,"p" ++ id ++ "Text" ++ i,name,i * 100 + 6,129,100,48);
			};
			buildInfo[id, \pageName] = pageName;
			buildInfo[id, \idNames] = idNames;

			this.sendPacket("<JZML>" ++ snippets ++ "<JZML>");

			r { 0.1.wait; connections[current_ip][1].sendMsg("/interface", pageName); bindGui.randomize;}.play;
		}, { "input typeArray and oscTagArray don't have the same sizes".postln; });
	}

	buildDemo { | typeArray, parNames, pageName = "default page", id = 1|
		var type,snippets,idNames;

		if ( typeArray.size == parNames.size, {
			snippets = snippets ++ this.addPageCode(pageName);

			parNames do: { |name, i|
				var type = typeArray[i];

				case
				{ type == \Fader }
				{       idNames = idNames ++ ["p" ++ id ++ "Fader" ++ i];
					snippets = snippets ++ this.faderCode(pageName,idNames.last,i * 100 + 6,15,100, 678);
				}
				{ type == \Range }
				{       idNames = idNames ++ ["p" ++ id ++ "Range" ++ i];
					snippets = snippets ++ this.rangeCode(pageName,idNames.last,i * 100 + 6,15,100, 678);
				};

				snippets = snippets ++ this.textCode(pageName,"p" ++ id ++ "Text" ++ i,name,i * 100 + 6,129,100,48);
			};
			buildInfo[id, \pageName] = pageName;
			buildInfo[id, \idNames] = idNames;

			this.sendPacket("<JZML>" ++ snippets ++ "<JZML>");

			r { 0.1.wait; connections[current_ip][1].sendMsg("/interface", pageName); nil }.play;
		}, { "input typeArray and oscTagArray don't have the same sizes".postln; });
	}
}