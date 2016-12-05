/*
2013
Marinus Klaassen
rotterdamruis.nl
*/


EZLemurGui {
	var currentLemurWidgets, tempObjectType, responder, <pagename, <>objectReferenceName, <name, <>lemur, <>action, <value, <>cmdName, <>xOffset;

	*new { ^super.new.init; }

	init {  currentLemurWidgets = Bag[]; }

	name_ { |argName|
		name = argName;
		this.makeGui(tempObjectType, value);
	}

	value_ { |argValue|
		if (lemur.notNil) { lemur.oscaddr.sendMsg(*([cmdName] ++ argValue)); };
		value = argValue;
	}

	pagename_ { |argPageName|

		this.makeGui(tempObjectType, value, argPageName);
	}

	remove {
		if (responder.notNil) { responder.free; responder = nil };
		currentLemurWidgets do: { |objectType|
			case { objectType == "Fader" } {
				lemur.removeFader(pagename, pagename ++ objectReferenceName ++ objectType);
			} { objectType == "Range" } {
				lemur.removeRange(pagename, pagename ++ objectReferenceName ++ objectType);
			} { objectType == "Text" } {
				lemur.removeText(pagename, pagename ++ objectReferenceName ++ objectType);
			};
			currentLemurWidgets.remove(objectType);
		};
		tempObjectType = nil;

	}

	move { |argxOffset|
		xOffset = argxOffset;
		currentLemurWidgets do: { |objectType|
			case { objectType == "Fader" } {
				lemur.fader(pagename, pagename ++ objectReferenceName ++ objectType, x: xOffset, color: Color.blue);
			} { objectType == "Range" } {
				lemur.range(pagename, pagename ++ objectReferenceName ++ objectType,x:  xOffset, color: Color.blue);
			} { objectType == "Text" } {
				lemur.text(pagename,  pagename ++ objectReferenceName ++ "Text", name,  x: xOffset, y: 100, color: Color.blue);
			};
		};
	}


	makeResponder { |argObjectType|
		cmdName = "/" ++ pagename ++ objectReferenceName ++ argObjectType ++ "/x";
		if (responder.notNil) { responder.free; responder = nil };
		responder = OSCFunc({ |msg|
			msg.removeAt(0);
			if (action.notNil) { action.value(msg); };
			value = msg;
		}, cmdName)
		.permanent_(true);

	}

	makeGui { |objectType, argValue, argPageName|
		this.remove;
		if (argPageName.notNil) { pagename = argPageName };
		tempObjectType = objectType;
		case { objectType == "Fader" } {
			lemur.fader(pagename, pagename ++ objectReferenceName ++ objectType, x: xOffset, color: Color.blue);
			currentLemurWidgets.add(objectType);
		} { objectType == "Range" } {
			lemur.range(pagename, pagename ++ objectReferenceName ++ objectType, x: xOffset, color: Color.blue);
			currentLemurWidgets.add(objectType);
		};
		if (objectType == "Fader" || (objectType == "Range")) {
			lemur.text(pagename,  pagename ++ objectReferenceName ++ "Text", name,  x: xOffset, y: 100, color: Color.blue);
			currentLemurWidgets.add("Text");
			this.makeResponder(objectType);
		};
		fork { 0.04.wait; this.value_(argValue) }
	}
}


