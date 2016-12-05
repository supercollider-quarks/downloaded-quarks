/*
2013
Marinus Klaassen
rotterdamruis.nl
*/


ScorePresetMenu {
	var <canvas, <>presets, <currentPresetIndex, <presetMenuItems, <gui, >action, >storeAction;

	currentPresetIndex_ { |argCurrentPresetIndex|
		currentPresetIndex = argCurrentPresetIndex;
		if (gui[\presetMenu].notNil) { gui[\presetMenu].value = currentPresetIndex };
		if (action.notNil) {
			action.value(presets[presetMenuItems[currentPresetIndex].asSymbol])
		};
	}

	presetMenuItems_ { |argPresetMenuItems|
		presetMenuItems = argPresetMenuItems;
		if (gui[\presetMenu].notNil) { gui[\presetMenu].items_(presetMenuItems).value_(currentPresetIndex); }
	}

	*new { ^super.new.init }

	init {
		presets = Dictionary.new;
		presetMenuItems = Array.new;
		currentPresetIndex = 0;
	}

	makeGui { |parent, argBounds|
		var width, height, bounds = argBounds.asRect;
		width = bounds.width - 20; height = bounds.height - 8;
		canvas = CompositeView(parent, bounds)
		.background_(Color.new255(0,0,0,0));
		canvas.addFlowLayout(0@0,4@0);

		gui = ();
		gui[\presetMenu] = PopUpMenu(canvas, 37/160 * width @ height)
		.background_(Color.black.alpha_(0.8))
		.stringColor_(Color.red)
		.items_(presetMenuItems)
		.value_(currentPresetIndex)
		.action_({arg val; this.currentPresetIndex = val.value.postln; });

		gui[\buttons] = ["+", "-", "recall", "store", "replace","delete"] collect: { |name, i|

			Button(canvas, if(i < 2, { 10 / 160 * width }, { 25 / 160 * width }) @ height)
			.states_([[name,Color.red, if(i < 2, { Color.new255(189, 183, 107) }, { Color.black.alpha_(0.8) })]])
			.action_(
				[
					{     this.currentPresetIndex = currentPresetIndex + 1 % presetMenuItems.size; },
					{     this.currentPresetIndex = currentPresetIndex - 1 % presetMenuItems.size; },
					{     this.currentPresetIndex = gui[\presetMenu].value.postln }, {
						TypePresetName({ |name|
							presetMenuItems = presetMenuItems.insert(currentPresetIndex, name);
							gui[\presetMenu].items_(presetMenuItems);
							gui[\presetMenu].value_(currentPresetIndex);
							if (storeAction.notNil) { presets[name.asSymbol] = storeAction.value.copy };
					}) }, {
						TypePresetName({ |name|
							presetMenuItems[currentPresetIndex] = name;
							gui[\presetMenu].items_(presetMenuItems);
							gui[\presetMenu].value_(currentPresetIndex);
							if (storeAction.notNil) { presets[name.asSymbol] = storeAction.value.copy };
					}) }, {
						var presetName = presetMenuItems.removeAt(currentPresetIndex);
						gui[\presetMenu].items_(presetMenuItems);
						currentPresetIndex = if (currentPresetIndex >= presetMenuItems.size)
						{ presetMenuItems.size - 1; }
						{ currentPresetIndex };
						presets[presetName.asSymbol] = nil;
						gui[\presetMenu].value = currentPresetIndex;
			}][i])
		};
	}

	loadState { |argPreset|
		presets = argPreset[\presets];
		presetMenuItems = argPreset[\presetMenuItems];
		currentPresetIndex = argPreset[\currentPresetIndex];
	}

	getState {
		var preset = Dictionary.new;
		preset[\currentPresetIndex] = currentPresetIndex.copy;
		preset[\presets] = presets.copy.postln;
		preset[\presetMenuItems] = presetMenuItems.copy;
		^preset;
	}

}
								