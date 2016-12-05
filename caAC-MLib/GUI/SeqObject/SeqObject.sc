SeqChannel {
	var <>parent, <>bounds, <>seqValues, <widgets, pointer, <seqLenght, <stepDivision, <name, <pointerIndex, <>action, <>dragReceiveAction;
	*new { |argStepDivision, argname|
		^super.newCopyArgs.init(argStepDivision, argname);
	}

	init { |argStepDivision, argname|
		if (argStepDivision.isNil) { argStepDivision = [4,4,4,4] };
		if (argname.isNil) { argname = nil };

		name = argname;
		this.stepDivision_(argStepDivision);
	}

	stepDivision_ { |argStepDivision|
		stepDivision = argStepDivision;
		seqValues = Array.new;
		stepDivision do: { |divideBy| divideBy do: { seqValues = seqValues.add(divideBy.reciprocal.neg) } };
		seqValues.postln;
	}

	pointerIndex_ { |argPointerIndex|
		var buttonBounds = widgets[\buttonView][argPointerIndex].bounds;
		var bounds = Rect(buttonBounds.left,0,buttonBounds.width,widgets[\canvas].bounds.height);
		pointerIndex = argPointerIndex;
		defer({ widgets[\pointerView].bounds = bounds });
		^seqValues[pointerIndex];
	}

	getButtonState {
		var buttonState = Array.new, onoff;
		seqValues do: { |val|
			if (val > 0) { onoff = 1 } { onoff = 0 };
			buttonState = buttonState.add(onoff);
		};
		^buttonState;
	}

	name_ { |argName|
		name = argName;
		widgets[\dragSink] = argName;
	}

	loadButtonState { |buttonState|
		buttonState do: { |val, i|
			if (widgets.notNil) { widgets[\buttonView][i].value = val; };
			if (seqValues[i].notNil) { if (val == 0) { seqValues[i] = seqValues[i].neg } { seqValues[i] = seqValues[i].abs }};
		}
	}

	makeGui { |argParent, argBounds|
		var countWidth, countHeight;
		var sumIndex = 0;
		if (argParent.notNil) { parent = argParent };
		if (argBounds.notNil) { bounds = argBounds };
		// Check if there's already an gui open else assign a dictionary
		if (widgets.notNil) { this.closeGui; };
		bounds = bounds.asRect;
		widgets = Dictionary.new;

		widgets[\canvas] = CompositeView(parent,bounds)
		.background_(Color.black);

		widgets[\dragSink] = DragSink(widgets[\canvas],Rect(0,4,100,widgets[\canvas].bounds.height - 8))
		.string_(name)
		.font_(Font("Menlo", 12, true, true, true))
		.background_(Color(0.5,0.5,0.5))
		.align_(\center)
		.receiveDragHandler_({
			name = View.currentDrag.asString;
			name.postln;
			if (dragReceiveAction.notNil) { dragReceiveAction.value(name) };

			widgets[\dragSink].string_(name);
		});

		countWidth = bounds.width - 100 / stepDivision.size;
		widgets[\accentView] = Array.new;
		widgets[\buttonView] = Array.new;

		stepDivision do: { |divideBy, i|

			var xOffset = countWidth * i + 100;
			var xWidth = countWidth / divideBy;
			divideBy do: { |count|
				var thisIndex = sumIndex;
				if (count == 0) {
					widgets[\accentView] = widgets[\accentView].add(
						CompositeView.new(
							widgets[\canvas],
							Rect(
								xWidth * count + xOffset,
								0,
								xWidth,
								widgets[\canvas].bounds.height)
					).background_(Color.red))
				};

				widgets[\buttonView] = widgets[\buttonView].add(
					Button.new(widgets[\canvas],
						Rect(
							xWidth * count + xOffset + 2,
							4,
							xWidth - 8,
							widgets[\canvas].bounds.height - 8))
					.background_(Color.red.alpha_(0))
					.value_(2.rand)
					.states_([
						["--", Color.red, Color.black],
						["++", Color.black, Color.yellow],
					])
					.action_({ |toggle|
						var val;
						val = seqValues[thisIndex];
						if (toggle.value > 0) {
							val = val.abs;
						} {
							val = val.neg;
						};
						seqValues[thisIndex] = val;
						if (action.notNil) { action.value(seqValues, thisIndex) }; // return whole array when an element is changed
				}));
				sumIndex = sumIndex + 1;

			};

		};
		widgets[\pointerView] = CompositeView(widgets[\canvas], Rect(0,0,0,0))
		.background_(Color.yellow.alpha_(0.4));
		seqLenght = sumIndex;
	}

	closeGui {
		widgets do: { |slot|
			if (slot.isKindOf(Array)) {
				slot do: { |element| element.remove }
			} {
				slot.remove;
			}
		};
		widgets = nil;
	}

}


SequencerMatrix {
	var <>seqChannels, <>matrixSettings, <parent, <channelView, <bounds, <presetObject;

	*new { | ... argSeqChannels |
		^super.newCopyArgs.init(argSeqChannels);
	}

	init { | ... argSeqChannels |
		matrixSettings = ();
		seqChannels = argSeqChannels.first;
		presetObject = QuickPresetGui(2, 8);
		presetObject.getPresetAction = {
			var presets = Array.new;
			seqChannels do: { |seqChannel|
				presets = presets.add(seqChannel.getButtonState);
			};
			presets;
		};
		presetObject.loadPresetAction = { |presets|
			if (presets.isNil) {
				seqChannels do: { |ch| ch.loadButtonState(0!ch.seqLenght) };
			} {
				seqChannels do: { |ch, i|
					if (presets[i].notNil) {
						ch.loadButtonState(presets[i])
					}
				}
			}

		}
	}

	makeGui { |argParent, argBounds|
		var width,height,channelBounds, compositeBounds, compositeCanvas;
		if (argBounds.isNil) { bounds = Rect(200, Window.screenBounds.bounds.height - 480, 800, 400) } { bounds = argBounds;  };
		if (argParent.isNil) { parent = Window("", bounds, false); } { parent = argParent;  };

		parent.view.addFlowLayout(0@0);
		parent.background_(Color.new255(47, 79, 79).alpha_(0.95));

		compositeBounds = Rect(0, 0, bounds.width, bounds.height - 60);
		compositeCanvas = CompositeView(parent, compositeBounds);
		compositeCanvas.decorator = FlowLayout(compositeBounds);

		width = compositeBounds.width; height = compositeBounds.height / seqChannels.size - 5;
		channelBounds = asRect(width@height);

		seqChannels do: { |channel|
			channel.makeGui(compositeCanvas, channelBounds);
			channel.action = { |values, index|
				[values, index].postln;
				presetObject.getPreset;
			};
		};

		presetObject.makeGui(parent, Rect(0, 0, 530, 50), 2, 8);
		presetObject.canvas.background_(Color(0.39607843137255, 0.46274509803922, 0.42352941176471));
		parent.front;
	}
}



SequencerInstrumentRack {
	var <>parent, <>instrumentArray, <size;

	*new { | argInstrumentNames, argSize = 0 |
		^super.newCopyArgs.init(argInstrumentNames, argSize);
	}

	init { | argInstrumentNames, argSize |
		instrumentArray = nil!argSize;
		size = argSize;

		if (argInstrumentNames.notNil) {
			argInstrumentNames do: { |indexInstrumentName, i|
				if (indexInstrumentName.notNil) {
					instrumentArray[i] = EmbedGui.new(indexInstrumentName);
				}
			}
		};
	}

	putInstrument { |argInstrumentName, index|
		instrumentArray.postln;
		if (instrumentArray[index].notNil) {  instrumentArray[index].closeGui };

		instrumentArray[index] = EmbedGui(argInstrumentName.asSymbol);
		instrumentArray do: { |ins| if (ins.notNil) { ins.closeGui; }};

		parent.addFlowLayout(0@10, 0@10);

		instrumentArray do: { |ins|
			if (ins.notNil) {
			ins.widthOffset = 20;
			ins.makeGui(parent);
			}
		};
	}

	size_ { |argSize|
		instrumentArray do: { |ins| ins.closeGui; };
		instrumentArray = nil!size;
		size = argSize;
	}

	makeGui {
		parent = Window.new(
			"",
			Rect(Window.screenBounds.bounds.width - 430, Window.screenBounds.bounds.height - 600, 410, 500),
			true,
			scroll: true
		)
		.background_(Color.black);


		parent.addFlowLayout(0@10, 0@10);

		instrumentArray do: { |ins|
			if (ins.notNil) {
			ins.widthOffset = 20;
			ins.makeGui(parent);
			};
		};
		parent.front;
	}

}


SequencerDefinition {
	var <projectName, <>instrumentDefinitions, <>sequencerMatrix, <>sequencerInstrumentRack, <>projectSettings;

	*new { | argProjectName, argSequencerMatrix |
		^super.newCopyArgs.init(argProjectName, argSequencerMatrix);
	}

	init { |argProjectName, argSequencerMatrix|

		instrumentDefinitions = InstrumentDefinitions.new;
		projectName = argProjectName;

		if (argSequencerMatrix.isNil) {
			sequencerMatrix = SequencerMatrix();
		} {
			sequencerMatrix = argSequencerMatrix;
		};

		sequencerMatrix.seqChannels do: { |channel, channelIndex|
			channel.dragReceiveAction = {  |name|
				sequencerInstrumentRack.putInstrument(name, channelIndex);
			};
		};

		sequencerInstrumentRack = SequencerInstrumentRack(
			sequencerMatrix.seqChannels collect: (_.name),
			sequencerMatrix.seqChannels.size
		);
	}

	makeGui {
		instrumentDefinitions.makeGui;

		sequencerMatrix.makeGui;

		sequencerInstrumentRack.makeGui;
	}
}



