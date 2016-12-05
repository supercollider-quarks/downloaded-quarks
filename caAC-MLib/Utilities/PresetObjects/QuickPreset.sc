QuickPresetGui {
	var <>presetData, widgets, <canvas, currentX, currentY, <>font, <>font2;
	var storeReplaceProjectWidget,storeNewProjectWidget, loadProjectWidget;
	var <>projectName, <>getPresetAction, <>loadPresetAction, zeroButton;

	*new { |parent, bounds argRows = 1, argcolumns = 5|
		^super.newCopyArgs.init(parent, bounds, argRows, argcolumns);
	}

	init { |argRows = 1, argcolumns = 5|
		presetData = MultiLevelIdentityDictionary.new;
		currentX = 0;
		currentY = 0;

	}

	getPreset {
		if (getPresetAction.notNil) {
			presetData[currentY,currentX] = getPresetAction.value.copy;
		}
	}

	loadPreset {
		if (loadPresetAction.notNil) {
			loadPresetAction.value(presetData[currentY, currentX].copy)
			}
	}

	makeGui { |parent, bounds, argRows, argcolumns|
		var widgetWidth, widgetHeight, count = 0, color;

		var rows = argRows;
		var columns = argcolumns;

		font = Font("Monaco", 14);

		canvas = CompositeView(parent, bounds);
		canvas.background_(Color.grey);
		widgets = Array2D(argRows, argcolumns);

		widgetHeight = canvas.bounds.height - (2 * rows) / rows;
		widgetWidth = canvas.bounds.width - 240 - (2 * columns) / columns;

		color = Color.new255(139, 26, 26);

		widgets rowsDo: { |row,y| row do: { |colomn,x| var index;
			index = count;
			widgets.put(y, x, Button(canvas, Rect(widgetWidth * x + 2, widgetHeight * y + 2, widgetWidth - 4, widgetHeight - 4))
				.background_(color)
				.states_([
					["", Color.black, Color.green],
					["±", Color.black, Color.blue],
				])
				.font_(font)
				.action_({ arg butt;
					widgets.at(currentY, currentX).value = 0;
					if (index != count) { widgets.at(y, x).value = 0; } { widgets.at(y, x).value = 1 };
					currentX = x; currentY = y;
					this.loadPreset;
			})
			);
			}
		};
		widgets.at(currentY, currentX).value = 1;

		font2 = Font("Monaco", 7);



		storeReplaceProjectWidget = Button(canvas, Rect(canvas.bounds.width - 250, 2, 78, widgetHeight - 2))
		.background_(color)
		.states_([
			["REPLACE AND STORE", ],
		])
		.action_({ arg butt;
			"REPLACE AND STORE".postln;
		})
		.font_(font2);

		storeNewProjectWidget =  Button(canvas, Rect(canvas.bounds.width - 170, 2, 58, widgetHeight - 2))
		.background_(color)
		.states_([
			["STORE TO NEW", ],
		])
		.action_({
			TypePresetName({ TypePresetName.asString.postln })
		})
		.font_(font2);

		loadProjectWidget = Button(canvas, Rect(canvas.bounds.width - 110, 2, 58, widgetHeight - 2))
		.background_(color)
		.states_([
			["LOAD PROJECT", ],
		])
		.action_({ arg butt;
			"LOAD PROJECT".postln;
		})
		.font_(font2);

		zeroButton = Button(canvas, Rect(canvas.bounds.width - 20, 2, 18, widgetHeight - 2))
		.background_(color)
		.states_([
			["0", ],
		])
		.action_({ loadPresetAction.value(nil)
		})
		.font_(font2);
	}

	saveToFile { |what, projectName|
		var path = Platform.resourceDir ++ "/ProjectSettings/" ++ what.asString ++ "/";
		if (path.pathMatch.size == 0) { path.mkdir };
		presetData.writeArchive(path ++ projectName ++ ".smStates");
	}

	readFromFile { |what, projectName|
		var a, path = Platform.resourceDir ++ "/ProjectSettings/" ++ what.asString ++ "/" ++ projectName ++ ".smStates";
		if (path.pathMatch.size > 0) { presetData = Object.readArchive(path); };
		presetData.postln;
	}
}

		