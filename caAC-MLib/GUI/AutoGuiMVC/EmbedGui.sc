EmbedGui {

	// Control Elements is an array with only the control elements!
	var <controlElements, <synthDefName, synthDesc, <>controlPanel, canvas, tempNames, tempToggleSynth, <>index, <>widthOffset;

	// first inialization and creation of a new instance
	*new { |argSynthDefName|
		^super.newCopyArgs.init(argSynthDefName);
	}
	// init all nescessary value
	init { |argSynthDefName|
		synthDefName = argSynthDefName;
		synthDesc = SynthDesc.readDef(synthDefName);

		widthOffset = 0;

		controlPanel = MBControlPanel.new(synthDesc.metadata, synthDefName);
		controlPanel.randomAction = {
			controlElements do: { |element|
				if (element.isKindOf(MBFader)) { element.value_(1.0.rand) }
			}
		};
		controlPanel.playTrigger = { this.oneShotPlay };
		controlPanel.playToggle = { |value| this.togglePlay(value) };
		controlPanel.getPreset = { var preset = ();
			preset[\volgorde] = Array.new;
			controlElements do: { |element|
				preset[\volgorde] = preset[\volgorde].add(element.name);
				preset[preset[\volgorde].last] = element.value;
			};
			preset.copy;
		};
		controlPanel.loadPreset = { |preset|
			var volgorde = preset[\volgorde].copy;
			preset[\volgorde] = nil;

			volgorde do: { |key, i| controlElements[i].value_(preset[key]) }; // maybe a value action in elements
		};

		controlPanel.fileAction = { |toStoreMetaData| SynthDesc.storeMetaData(synthDefName, toStoreMetaData); };

		controlElements = List.new;
		tempNames = synthDesc.controlNames;

		synthDesc.metadata[\noGui] do: { |key| tempNames.removeAt(tempNames.indexOfEqual(key)) };

		tempNames do: { |key|
			var checkSpec = key.asSpec.isKindOf(ControlSpec) || synthDesc.metadata[\specs][key].isKindOf(ControlSpec);
			if (checkSpec) {
				controlElements.add(MBFader.new(key,if(key.asSpec.isKindOf(ControlSpec)){key.asSpec}{synthDesc.metadata[\specs][key]}));
			} {
				controlElements.add(MBNumberBox.new(key));
			};
			controlElements.last.name = key;
		};
	}

	makeGui { |argParent|
		var parent;
		if (argParent.isNil) { parent = Window.new(); parent.front; } { parent = argParent };

		canvas = CompositeView(parent,parent.bounds.width - widthOffset@(tempNames.size * 28 + 32));
		canvas.addFlowLayout(0@0, 0@2);
		canvas.background_(Color.grey);

		controlPanel.makeGui(canvas,canvas.bounds.width@30);

		controlElements do: { |element|
			element.makeGui(canvas, canvas.bounds.width @ 22);
			CompositeView(canvas,canvas.bounds.width@2).background_(Color.rand);
		};
	}

	closeGui { canvas.remove }

	getParamValuesArray {
		var paramValuesArray = Array.new;
		controlElements do: { |obj|
			paramValuesArray = paramValuesArray.add(obj.name.asSymbol);
			paramValuesArray = paramValuesArray.add(
				if(obj.isKindOf(MBFader)) {
					obj.spec.map(obj.value)
				} {
					obj.value;
				}
			)
		};
		^paramValuesArray
	}

	oneShotPlay {
		fork {
			var tempSynth, indexOf, paramsValues = this.getParamValuesArray;
			if (tempSynth.notNil) { tempSynth.release; tempSynth = nil };
			tempSynth = Synth(synthDefName, paramsValues);
			Server.default.sync;
			indexOf = paramsValues.indexOf(\atk);
			if (indexOf.notNil) {
				fork { (paramsValues[indexOf + 1]).wait; tempSynth.set(\gate, 0); tempSynth = nil }
			} {
				tempSynth.set(\gate, 0); tempSynth = nil
			};
		}
	}

	togglePlay { |toggle|
		var paramsValues = this.getParamValuesArray;

		if (toggle > 0) {
			controlElements do: { |element| element.action = { |value| tempToggleSynth.set(element.name, value) } };
			if (tempToggleSynth.notNil) { tempToggleSynth.release; tempToggleSynth = nil };
			tempToggleSynth = Synth(synthDefName, paramsValues);
		} {
			tempToggleSynth.set(\gate, 0); tempToggleSynth = nil;
			controlElements do: { |element| element.action = nil; };
		};
	}
}