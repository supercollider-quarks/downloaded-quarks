
InstrumentDefinitions {
	var <>definitionDict, gui, guiLooks, parent;

	*new { ^super.newCopyArgs().init }

	init {
		guiLooks = ();
		guiLooks[\labelFont] = Font("Menlo", 10, italic: true);
		guiLooks[\labelFontColor] = Color.yellow;
		guiLooks[\labelBackgroundColor] = Color.blue;
		this.getDefinitionsFromDisk;
	}

	getDefinitionsFromDisk {
		var path = Platform.userExtensionDir ++ "/caAC-MLib/Settings/";
		path = path.pathMatch.first ++ "InstrumentList.scil";
		definitionDict = Object.readArchive(path).copy;
		^definitionDict
	}

	storeDefinitionsToDisk {
		var path = Platform.userExtensionDir ++ "/caAC-MLib/Settings/";
		path = path.pathMatch.first ++ "InstrumentList.scil";
		definitionDict.copy.writeArchive(path)
	}

	makeGui {
		if (gui.isNil) {
			parent = Window.new(
				"IList",
				Rect(50, Window.screenBounds.bounds.height - 480, 140, 400),
				false,
				scroll: true
			)
			.background_(Color(0.39607843137255, 0.46274509803922, 0.42352941176471));
			this.buildElements;
		};
		parent.front;
	}

	buildElements {
		var bothSize, order = this.getDefinitionsFromDisk[\order];
		gui = ();
		gui[\dragWidgets] = List.new;
		gui[\labelWidgets] = List.new;

		bothSize = { gui[\dragWidgets].size + gui[\labelWidgets].size };

		order do: { |labelName, i|
			var element;
			element = StaticText(parent, Rect(0, 15 * bothSize.value + 10, parent.bounds.width - 4, 15))
			.string_("  " ++ labelName.postln)
			.stringColor_(guiLooks[\labelFontColor])
			.background_(guiLooks[\labelBackgroundColor])
			.font_(guiLooks[\labelFont]);
			gui[\labelWidgets].add(element);

			definitionDict[labelName] do: { |name|
				var dragElement;
				dragElement = DragSource(parent, Rect(20, 15 * bothSize.value + 10, parent.bounds.width - 44, 15))
				.align_(\left)
				.setBoth_(false)
				.object_(name)
				.string_(name)
				.dragLabel_(name)
				.background_(Color.grey)
				.font_(guiLooks[\labelFont]);
				gui[\dragWidgets].add(dragElement);
			};
		};
		parent.front;
	}
}
