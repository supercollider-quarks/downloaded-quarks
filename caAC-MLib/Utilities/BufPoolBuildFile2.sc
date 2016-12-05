

BPDragAndDropElement {

    var stringModel, stringValueFunction, stringActionDependant, oldString;
	var editModel, editValueFunction, editDependant, editModel;
	var >beginDragAction, >endDragAction, <>stringAction, >selectAction;
	var dragAndDropContainer, editView, dragAndDropView, textEdit, stringGuiDependant, editDependant, removeButton;
	var >removeAction;
	var <>what, <>index, <>extra;

	*new {

        |argIndex, argWhat, argString|

        ^super.newCopyArgs.init(argIndex, argWhat, argString);

    }

	stringModelViewController {

		stringModel = (string: "");

		stringValueFunction = { |argString|
			var check = true;
			if(argString.class == Array) { argString = argString[1]; check = false; };
			oldString = stringModel[\string];
			stringModel[\string] = argString;
			if (check) {
				stringModel.changed(\string, argString);
			} {
				stringModel.changed(\dontPerformStringAction, argString);
			}
		};

		stringActionDependant = { |theChanger, what, argString|
			if (what != \dontPerformStringAction) {
				if(stringAction.notNil) { stringAction.value(argString, oldString, what, index); }
			}

		};

		stringModel.addDependant(stringActionDependant);
	}

	editModelViewController {

		editModel = (bool: false);

		editValueFunction = { |argBool|
			editModel[\bool] = argBool;
			editModel.changed(\bool, argBool);
		};
	}

	init { |argIndex, argWhat, argString|
		index = argIndex;
		what = argWhat;
		this.stringModelViewController;
		this.editModelViewController;
		stringValueFunction.value(argString);
	}

	makeGui { |argParent, argBounds|
		var bounds = argBounds.asRect;
		editView = CompositeView(argParent, bounds)
		.background_(Color.black.alpha_(0));

		textEdit = TextField(editView, bounds.extent)
		.background_(Color.black.alpha_(0.99))
		.string_(stringModel[\string])
		.stringColor_(Color.yellow)
		.action_({ |text| stringValueFunction.value(text.string) });

		removeButton = MButtonV(editView, Rect(bounds.bounds.width -20, 5, 15, 15));
		removeButton.action = { if (removeAction.notNil) { removeAction.value(what, index,  stringModel[\string], extra) } };

		dragAndDropView = CompositeView(argParent, bounds)
		.background_(Color.black.alpha_(0));

		dragAndDropContainer = DragBoth(dragAndDropView,bounds.extent).align_(\10)
		.background_(Color.black.alpha_(0.99))
		.object_(stringModel[\string])
		.stringColor_(Color.white)

		.beginDragAction_({
			if (beginDragAction.notNil) { beginDragAction.value(what, index, stringModel[\string], extra); };
		})
		.receiveDragHandler_({arg obj;
			if (endDragAction.notNil) { endDragAction.value(what,index,stringModel[\string], extra); }
		});

		stringGuiDependant = { |theChanger, what, argString|
			dragAndDropContainer.object = argString;
			textEdit.string = argString.asString;
		};
		stringModel.addDependant(stringGuiDependant);

		if (editModel[\bool]) {
			dragAndDropView.visible = false;
			editView.visible = true;
		} {
			dragAndDropView.visible = true;
			editView.visible = false;

		};

		editDependant = { |theChanger, what, argBool|
			if (argBool) {
				dragAndDropView.visible = false;
				editView.visible = true;
			} {
				dragAndDropView.visible = true;
				editView.visible = false;
				// textEdit.doAction; // TODO tijdelijk uitgecomment de gebruiker moet tijdelijk
			};



		};
		editModel.addDependant(editDependant);

	}

	closeGui {
		// Remove views
		editView.remove; textEdit.remove; removeButton.remove;
		dragAndDropView.remove; dragAndDropContainer.remove;
		// Remove GUI dependants from control models
		stringModel.removeDependant(stringGuiDependant);
		editModel.removeDependant(editDependant);
	}

	remove {
		stringModel.removeDependant(stringActionDependant); this.closeGui;
	}

	string_ { |argString|
		stringValueFunction.value(argString);
	}

	string {
		^stringModel[\string];
	}

	edit {  |argEdit|
		editValueFunction.value(argEdit)
	}

}


BPdata {
	classvar <>bufferpool;

	*new {
		bufferpool = IdentityDictionary.new;
	}
}


BPBankView {
	var startDragArray;
	var view, <units;
	var <>bpData;
	var >addAction, <>selectAction, <>removeAction, <>stringAction;
	var bounds, parent;

	*new{ ^super.new.init }

	init {
		units = Array.new;
	}

	makeGui { |argParent, argBounds|
		parent = argParent; bounds = argBounds;
		view = ScrollView(argParent, Rect(5, 5, 180, 300));
		view.hasHorizontalScroller = false;
		view.background = Color.grey(0.3);
		view.canReceiveDragHandler = true;
		view.receiveDragHandler = {
			if (View.currentDrag == \add) { this.add }
		};


		units do: { |bankUnit, i|
			bankUnit.makeGui(view, Rect(0, 35 * i, 175, 30));

			bankUnit.beginDragAction = { |...array| startDragArray = array.postln; };

			bankUnit.endDragAction = { |what, index, string, extra|
				var currentDragObject = View.currentDrag;
				var dragString = if (currentDragObject.isArray) { currentDragObject[2] } { nil };

				if (string != dragString) {

					case { currentDragObject.isString } {
						"Append Soundfile to this bank: %\n".postf(bankUnit.string);
					} { currentDragObject.isArray } {
						if (currentDragObject.at(0) == "bank") {
							units[startDragArray[1]].string = [\bankSwap, units[bankUnit.index.postln].string.copy];
							units[bankUnit.index].string = [\bankSwap, startDragArray[2]];
                            // Verzamel de banknamen
                            BufferCM.changed(\bankUpdate, Array.fill(units.size, { |i| units[i].string }));
						};
						if (startDragArray[3].postln != bankUnit.string.postln && (startDragArray[0] == "soundfile")) {
							"Append soundfile to %\n".postf(bankUnit.string);
							"Peform remove action from previous arrays".postln;
							"Update GUI".postln;
						};
					} { currentDragObject == \add } {
						this.add(index);
					};
				} {
					if (selectAction.notNil) {
						selectAction.value(what, index, string, extra)
					}
				};

			}
		}
	}

	closeGui { view.remove; view = nil; units do: (_.closeGui) }

	update {
		units do: { |unit, i| unit.index = i };
		if (view.notNil) { this.closeGui; this.makeGui(parent, bounds); }
	}

	removeBank { |argIndex|
		var unit = units.removeAt(argIndex);
		unit.closeGui;
		unit.remove;
		this.update;
		if (removeAction.notNil) { removeAction.value(unit.what, unit.index, unit.string, unit.extra) }
	}

	remove { this.closeGui; units do: (_.remove); }

	add { |argIndex, argName|
		var index, unit;
		if (argName.isNil) { argName = "default bank " ++ units.size };
		if (argIndex.isNil) {
			index = units.size;
			unit = BPDragAndDropElement(index, "bank", argName);
			units = units.add(unit);
		} {
			index = argIndex;
			unit = BPDragAndDropElement(index, "bank", argName);
			units = units.insert(index, unit);
		};

		// Set action functions
		units[index].selectAction = selectAction;
		units[index].removeAction = { |what, index, string, extra|

            this.removeBank(index);
		};


        units[index].stringAction  = { |...args|
            /* collect banknames TODO method */
            args = args.add(Array.fill(units.size, { |i| units[i].string }));

            if (stringAction.notNil) {
                stringAction.value(args);
            };

        };

		this.update;

		if (addAction.notNil) { addAction.value(unit) };
        /* collect banknames TODO method */
        BufferCM.changed(\bankUpdate, Array.fill(units.size, { |i| units[i].string }));
	}
}


BPSoundfileView {
	var startDragArray;
	var view, <units;
	var <>bpData, <>bankName;
	var >addAction, <>addBufAction, <>selectAction, <>swapAction, >removeAction;
	var bounds, parent;

	*new{ ^super.new.init }

	init {
		units = Array.new;
	}

	makeGui { |argParent, argBounds|
		parent = argParent; bounds = argBounds;
		view = ScrollView(argParent, argBounds);
		view.hasHorizontalScroller = false;
		view.background = Color.grey(0.3);
		view.canReceiveDragHandler = true;
		view.receiveDragHandler = {
			var currentDrag = View.currentDrag;
			case
			{ currentDrag == \add } { this.openFileDialog("soundfile", units.size); }
			{ currentDrag.isString } { this.addFileToBuffer(currentDrag) };
		};

		// Set actions!!!
		units do: { |soundfileUnit, i|
			soundfileUnit.makeGui(view, Rect(0, 35 * i, argBounds.width - 4, 30));

			soundfileUnit.beginDragAction = { |...array| startDragArray = array; };

			soundfileUnit.endDragAction = { |what, index, string, extra|
				var currentDragObject = View.currentDrag;
				var dragString = if (currentDragObject.isArray) { currentDragObject[2] } { nil };
				var thisWhat = soundfileUnit.what;
				var thisIndex = soundfileUnit.index;

				if (string != dragString) {

					case { currentDragObject.isString } {
						this.addFileToBuffer(currentDragObject, thisWhat, thisIndex, currentDragObject, extra);

					} { currentDragObject.isArray } {
						if (currentDragObject.at(0) == "soundfile") {
							units[startDragArray[1]].string = units[soundfileUnit.index].string.copy;
							units[soundfileUnit.index].string = startDragArray[2];
							if (swapAction.notNil) {
								swapAction.value(bankName, startDragArray[1], soundfileUnit.index) };
						};

					} { currentDragObject == \add } { this.openFileDialog(thisWhat, thisIndex) }
				} {
					if (selectAction.notNil) {
						selectAction.value(bankName, index, string)
					}
				}

			}
		}
	}

	closeGui { view.remove; view = nil; units do: (_.closeGui) }

	update {
		units do: { |unit, i| unit.index = i };
		if (view.notNil) { this.closeGui; this.makeGui(parent, bounds); }
	}

	removeSoundFile { |argIndex|
		var unit = units.removeAt(argIndex);
		unit.closeGui;
		unit.remove;
		this.update;
		if (removeAction.notNil) { removeAction.value(bankName, argIndex) };
	}

	remove { this.closeGui; units do: (_.remove); }

	add { |argIndex, argName|
		var index, unit;
		if (argName.isNil) { argName = "drop an audio file in here" };

		if (argIndex.isNil) {
			index = units.size;
			unit = BPDragAndDropElement(index, "soundfile", argName);
			units = units.add(unit);

		} {
			index = argIndex;
			unit = BPDragAndDropElement(index, "soundfile", argName);
			units = units.insert(index + 1, unit);
		};

		// Set action functions
		units[index].selectAction = selectAction;
		units[index].removeAction = { |what, index, string, extra|
			this.removeSoundFile(index);
		};

		this.update;

		if (addAction.notNil) { addAction.value(argName) };

	}

	addBuffer { |argBuffer, path, what, index|
		if (addBufAction.notNil) {
			addBufAction.value(argBuffer, path, what, index, bankName)
		}
	}

	addFileToBuffer {

         /* Laad een filepath in een audio Buffer */

        |path, what, index|

        if (path.pathExists != false) {

            Buffer.readChannel(channels: [0], path: path , action: {

                |buf|

                /* Voer een actie functie uit zodat aan processen geupdate worden */
                if (addBufAction.notNil) {

                    addBufAction.value(buf, buf.path, what, index, bankName)

                }

            })

        } /* if (path.pathExists != false) */

	} /* end method addFileToBuffer */


	openFileDialog { |what, index|

        Dialog.openPanel({ |path|

            Buffer.readChannel(channels: [0], path: path , action: { |buf|

                if (addBufAction.notNil) {
					addBufAction.value(buf, buf.path, what, index, bankName)

                }

            })

        })

    } /* end method openFileDialog */

} /* end class BPSoundfileView */


BPTransporter {
	var view, playFlag, recordFlag, editFlag, waveformFlag, >editAction;
	var addView, thrashView, addView, audioRecorder, editView, playView, waveformView;
	var <buffer, playSynth;

	*new { ^super.new.init; }

	recorderDoneAction_ { |argDoneAction|
		audioRecorder.doneAction = argDoneAction;
	}

	init {
		playFlag = 0;
		audioRecorder = AudioRecorder();
		editFlag = 0;
		waveformFlag = 0;
	}

	buffer_ { |argBuffer|
		buffer = argBuffer;
		if (playSynth.isPlaying.postln) {
			playSynth.free;
			playSynth = { PlayBuf.ar(buffer.numChannels, buffer, loop: 1) }.play;
			playSynth.track;

		};
	}

	makeGui { |argParent, argBounds|
		view = CompositeView(argParent, argBounds);
		view.background = Color.grey;

		thrashView = DragBoth(view,Rect(5, 5, 50, 50))
		.align_(\10)
		.background_(Color.yellow(0.8))
		.object_(\trash)
		.align_(\center)
		.stringColor_(Color.black)
		.receiveDragHandler_{
			"implement drag to trash method".postln;
			View.currentDrag.postln;
		};

		editView = Button(view, Rect(60, 5, 50, 50))
		.states_([
			["edit", Color.yellow, Color.black],
			["edit off", Color.yellow, Color.blue]
		])
		.action_({ |b|
			editFlag = b.value;
			if (editAction.notNil) { editAction.value(editFlag) }
		})
		.value_(editFlag);

		addView = DragBoth(view,Rect(115, 5, 50, 50)).align_(\10)
		.background_(Color.black.alpha_(0.99))
		.object_(\add)
		.align_(\center)
		.stringColor_(Color.yellow);

		audioRecorder.makeGui(view, Rect(170, 5, 50, 50));


		playView = Button(view, Rect(280, 5, 50, 50))
		.states_([
			["PLAY", Color.yellow, Color.black],
			["PLAY", Color.black, Color.yellow]
		])
		.action_({ |b|
			playFlag = b.value;
			if (b.value > 0 && buffer.notNil) {
				playSynth = { PlayBuf.ar(buffer.numChannels, buffer, loop: 1) }.play;
				playSynth.track;
			} {
				playSynth.release(0.05)
			};
		})
		.value_(playFlag);

		waveformView = Button(view, Rect(225, 5, 50, 50))
		.states_([
			["WAV", Color.yellow, Color.black],
			["WAV", Color.yellow, Color.blue]
		])
		.action_({ |b| waveformFlag = b.value })
		.action_(waveformFlag);
	}

	closeGui {
		view.remove; addView.remove; thrashView.remove; addView.remove;
		audioRecorder.closeGui; playView.remove; waveformView.remove;
	}
}



BufferPool {
	var <parent, <>soundFileViews, <bankView;
	var <currentBankName, transporter, storeAndReadProject;

	*new { ^super.new.init; }

	init {
		if (BufferCM.model.isNil) { BufferCM.init; };

		soundFileViews = Dictionary();

        BufferDict.init;

		storeAndReadProject = BPStoreAndReadProject(this);

		bankView = BPBankView();
		bankView.addAction = { |thisBank|

			var newSoundFileView = BPSoundfileView();

			if (soundFileViews[currentBankName].notNil) {
				soundFileViews[currentBankName].closeGui;
			};

			BufferDict.buffers[thisBank.string] = Array();

			newSoundFileView.bankName = thisBank.string;

			newSoundFileView.makeGui(parent, Rect(200, 5, 400, 300));

			newSoundFileView.addBufAction = { |buf, path, what, index, bankName|
				transporter.buffer = buf;
				{

                    if (index.isNil) { index = newSoundFileView.units.size };
					newSoundFileView.add(index, path);
					BufferDict.buffers[thisBank.string] = BufferDict.buffers[thisBank.string].insert(index + 1, buf);
					BufferCM.changed(\add, bankName, index + 1, buf);


				}.defer;

			};

			newSoundFileView.swapAction = { |bank, index1, index2|
				var a1 = BufferDict.buffers[bank][index1];
				var a2 = BufferDict.buffers[bank][index2];
				BufferDict.buffers[bank][index1] = a2;
				BufferDict.buffers[bank][index2] = a1;
				transporter.buffer = a1;
				BufferCM.changed(\SwapBuffer, bank, index1, index1);
			};

			newSoundFileView.selectAction = { |argBankName, argIndex, argName|
				transporter.buffer = BufferDict.buffers[argBankName][argIndex];
			};

			newSoundFileView.removeAction = { |argBankName, argIndex|
				BufferDict.buffers[argBankName].removeAt(argIndex);
				BufferCM.changed(\RemoveBuffer, currentBankName, argIndex);


			};

			soundFileViews[thisBank.string] = newSoundFileView;

			currentBankName = thisBank.string;

		};

		bankView.selectAction = { |bank, index, bankName|

            if (currentBankName != bankName) {
				soundFileViews[currentBankName].closeGui;
				soundFileViews[bankName].makeGui(parent, Rect(200, 5, 400, 300));
				currentBankName = bankName;
			};
		};

		bankView.removeAction = { |...args|
            soundFileViews[args[2]].closeGui();
            soundFileViews[args[2]].remove;
			soundFileViews[args[2]] = nil;
			BufferDict.buffers[args[2]] do: (_.free);
			BufferDict.buffers[args[2]] = nil;
            BufferCM.changed(\bankRemove, args[2]);


		};

		bankView.stringAction = { |tRenameArgs|

            /* Methode zodat een sample bank naam wijziging kan worden doorgevoerd */

            var cNewBankname = tRenameArgs[0].postln;
            var cOldBankName = tRenameArgs[1].postln;

            soundFileViews[cOldBankName].bankName = cNewBankname;
            soundFileViews[cNewBankname] = soundFileViews[cOldBankName].copy;
            soundFileViews[cOldBankName] = nil;

            if (currentBankName == cOldBankName) { currentBankName = cNewBankname };
            "WATGEBEURT".postln;
            BufferDict.buffers[cNewBankname] = BufferDict.buffers[cOldBankName].copy;
            BufferDict.buffers[cNewBankname].postln;
            BufferDict.buffers[cOldBankName] = nil;
            BufferDict.buffers[cNewBankname].postln;


            /* Update de dependant objecten */
            BufferCM.changed(\changeBankname, cNewBankname, cOldBankName, tRenameArgs[4]);

		};

		transporter = BPTransporter();
		transporter.editAction = { |buttonValue|
			if (buttonValue > 0) {
				bankView.units do: { |i| i.edit(true); };
				if (soundFileViews[currentBankName].notNil) {
					soundFileViews[currentBankName].units do: (_.edit(true));
				}
			} {
				bankView.units do: { |i| i.edit(false); };
				if (soundFileViews[currentBankName].notNil) {
					soundFileViews[currentBankName].units do: (_.edit(false));
				}
			}
		};
		transporter.recorderDoneAction = { |buffer|
			soundFileViews[currentBankName].addBuffer(buffer, "recording_" ++ 100000.rand, index: soundFileViews[currentBankName].units.size);
		};

	}

	makeGui {
		parent = Window("", bounds: Rect(200, 800, 604, 400), resizable: false);
		parent.background_(Color.grey(0.1, 0.9));

		bankView.makeGui(parent, Rect(5, 5, 180, 300));

		transporter.makeGui(parent, Rect(5, 320, 350, 60));
		if (soundFileViews[currentBankName].notNil) {
			soundFileViews[currentBankName].makeGui(parent, Rect(200, 5, 400, 300));
		};

		storeAndReadProject.makeGui(parent, Rect(370, 360, 200, 20));

		parent.front;
	}

	closeGui {
		parent.remove;
		bankView.closeGui;
		transporter.closeGui;
		soundFileViews do: (_.closeGui);
		storeAndReadProject.closeGui;
	}
}


BPStoreAndReadProject {
	var doneAction, bufferPoolObject, canvas, gui;

	*new { |argBufferPoolObject| ^super.new.init(argBufferPoolObject); }

	init { |argBufferPoolObject|
		bufferPoolObject = argBufferPoolObject;
	}

	saveDialog {
		File.saveDialog(successFunc: {
			|path| var file;
			path = PathName(path.standardizePath).extension_("xml").fullPath;
			file = File(path, "w");

			this.retrieveXMLSTringAndSaveBuffersToAudioFiles(path);
			doneAction = { |xmlFileString|
				file.putString(xmlFileString);
				file.close;
			}
		});
	}

	retrieveXMLSTringAndSaveBuffersToAudioFiles { |argPath|
		var audio_file_bank_settings, audiofile_path;
		var newDOMDocument = DOMDocument.new;
		var root = newDOMDocument.createElement("bufferPool");
		root.setAttribute( "selected_bank", bufferPoolObject.currentBankName);
		newDOMDocument.appendChild(root);

		audio_file_bank_settings = newDOMDocument.createElement("audiofile_bank_settings");
		root.appendChild(audio_file_bank_settings);

		audiofile_path = PathName(argPath).pathOnly ++ "audio/";
		if (audiofile_path.pathExists != \folder) {

			"mkdir %".format(audiofile_path).unixCmd(postOutput: false)
		};

		fork {
			0.05.wait;

			bufferPoolObject.soundFileViews keysValuesDo: { |bankName, soundFileView|
				var bank = newDOMDocument.createElement("bank");
				bank.setAttribute("name", bankName);
				audio_file_bank_settings.appendChild(bank);

				bufferPoolObject.soundFileViews[bankName].units do: { |bufferUnit|
					var bufferElement = newDOMDocument.createElement("audio_file");

					var pathName = bufferUnit.string;
					if (pathName.pathExists != \file) {
						pathName = audiofile_path ++ bufferUnit.string ++ ".aiff";
						BufferDict.buffers[bankName][bufferUnit.index].write(pathName) /* TODO er mag maar 1 instantie bestaan */
					};

					bufferElement.setAttribute("pathname", pathName);
					bank.appendChild(bufferElement);
				}
			};
			doneAction.value(newDOMDocument.format);
		}
	}

	openDialog {
		File.openDialog(successFunc: {
			|path| var file, xmlString;
			path.standardizePath;
			file = File(path, "r");
			xmlString = String.readNew(file);
			file.close;
			this.loadStateFromXML(xmlString);
		})
	}

	loadStateFromXML { |xmlString|
		var root, audio_file_bank_settings, banks;
		var newDOMDocument = DOMDocument.new;
		newDOMDocument.parseXML(xmlString);

		root = newDOMDocument.getChildNodes;
		audio_file_bank_settings = root.first.getChildNodes.first;
		banks = audio_file_bank_settings.getChildNodes;
		banks do: { |bank, index|
			var bankName = bank.first.getAttribute("name");
			bufferPoolObject.bankView.add(argName: bankName);

			bank.getChildNodes do: { |file|
				var pathName = file.getAttribute("pathname");
				bufferPoolObject.soundFileViews[bankName].addFileToBuffer(pathName);
			};
		};
	}

	makeGui { |argParent, argBounds|
		var width = argBounds.width, height = argBounds.height;
		canvas = CompositeView(argParent, argBounds)
		.background_(Color.green(1,0));

		gui = { |i|
			Button(canvas, Rect(i * 0.5 * width, 0, width * 0.5, height))
			.states_([[["open file", "save to file"][i], Color.black, Color.red(0.8,0.6)]])
			.action_({ var func = [{this.openDialog; }, {this.saveDialog}][i]; func.value;  })
			.font_(Font("Menlo",12)) } ! 2;
	}

	closeGui {
		canvas.remove; gui do: (_.remove)
	}

}


