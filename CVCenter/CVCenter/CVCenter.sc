/* (c) Stefan Nussbaumer */
/*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

CVCenter {

	classvar <all, nextCVKey, <cvWidgets, <window, <childViews, <tabs, <prefPane, swFlow, removeButs;
	classvar prefPaneBounds, tabsBounds;
	classvar <>midiMode, <>midiResolution, <>ctrlButtonBank, <>midiMean, <>softWithin;
	classvar <>shortcuts, <scv;
	classvar <alwaysOnTop = false;
	classvar <>guix, <>guiy, <>guiwidth, <>guiheight;
	classvar <widgetStates;
	classvar <tabProperties, colors, nextColor;
	classvar widgetwidth, widgetheight=160, colwidth, rowheight;
	classvar nDefWin, pDefWin, pDefnWin, tDefWin, allWin, historyWin, eqWin;
	classvar prefs, boundsOnShutDown, <>dontSave, <systemWidgets, <snapShots, snapShotSelect;
	// CVWidgetMS: how many slots at max for one column
	classvar <>numMsSlotsPerColumn = 15;
	classvar <>connectSliders = true, <>connectTextFields = true;

	*initClass {
		var newPrefs, newBounds;
		var scPrefs = false;
		var shutDownFunc;
		var scFunc;

		Class.initClassTree(CVCenterPreferences);
		Class.initClassTree(CVWidget);
		Class.initClassTree(KeyDownActions);
		Class.initClassTree(CVCenterShortcuts);

		this.dontSave_(['select snapshot', \snapshot]);
		systemWidgets = ['select snapshot', \snapshot];
		snapShots = ();

		prefs = CVCenterPreferences.readPreferences;

		prefs !? {
			prefs[\saveGuiProperties] !? {
				shutDownFunc = {
					// "shutdown action triggered".postln;
					newPrefs = CVCenterPreferences.readPreferences;
					CVCenterPreferences.writePreferences(
						newPrefs[\saveGuiProperties],
						boundsOnShutDown ?? { newPrefs[\guiProperties] },
						newPrefs[\saveClassVars],
						newPrefs[\midiMode],
						newPrefs[\midiResolution],
						newPrefs[\midiMean],
						newPrefs[\softWithin],
						newPrefs[\ctrlButtonBank],
						newPrefs[\removeResponders],
						newPrefs[\initMidiOnStartUp],
						newPrefs[\shortcuts],
						newPrefs[\globalShortcuts],
						newPrefs[\keyCodesAndMods]
					)
				};
				if (prefs[\saveGuiProperties] == 1 or:{
					prefs[\saveGuiProperties] == 2
				}, {
					this.guix_(prefs[\guiProperties] !? { prefs[\guiProperties].left });
					this.guiy_(prefs[\guiProperties] !? { prefs[\guiProperties].top });
					this.guiwidth_(prefs[\guiProperties] !? { prefs[\guiProperties].width });
					this.guiheight_(prefs[\guiProperties] !? { prefs[\guiProperties].height });
				});
				if (prefs[\saveGuiProperties] == 1, {
					if (\UI.asClass.isNil, {
						ShutDown.add(shutDownFunc);
					}, {
						UI.registerForShutdown(shutDownFunc);
					})
				})
			};
			prefs[\initMidiOnStartUp] !? {
				if (prefs[\initMidiOnStartUp], {
					if (MIDIClient.initialized.not, {
						Class.initClassTree(MIDIClient);
						Class.initClassTree(MIDIEndPoint);
						MIDIClient.init;
						MIDIIn.connectAll;
					})
				})
			};
			prefs[\saveClassVars] !? {
				if (prefs[\saveClassVars], {
					prefs[\midiMode] !? { this.midiMode_(prefs[\midiMode]) };
					prefs[\midiResolution] !? { this.midiResolution_(prefs[\midiResolution]) };
					prefs[\midiMean] !? { this.midiMean_(prefs[\midiMean]) };
					prefs[\softWithin] !? { this.softWithin_(prefs[\softWithin]) };
					prefs[\ctrlButtonBank] !? { this.ctrlButtonBank_(prefs[\ctrlButtonBank]) };
				})
			};
			prefs[\removeResponders] !? { CVWidget.removeResponders_(prefs[\removeResponders]) };
		};

		this.shortcuts = IdentityDictionary.new;
		#all, cvWidgets, widgetStates, removeButs, tabProperties, childViews = IdentityDictionary.new!6;

		// shortcuts
		scv = (); // environment holding various variables used in shortcut-functions;,

		// "prefs[\shortcuts]: %\n".postf(prefs[\shortcuts]);
		prefs !? { prefs[\shortcuts] !? { prefs[\shortcuts][\cvcenter] !? { scPrefs = true }}};

		if (scPrefs == false, {
			this.shortcuts = CVCenterShortcuts.shortcuts;
		}, {
			this.shortcuts = prefs[\shortcuts][\cvcenter];
		});
	}

	*new { |cvs...setUpArgs|
		var r, g, b;

		if (all.size == 0, {
			r = g = b = (0.6, 0.65 .. 0.75);
			colors = List();

			if (setUpArgs.size > 0, {
				this.prSetup(setUpArgs);
			});

			r.do({ |red|
				g.do({ |green|
					b.do({ |blue|
						colors.add(Color(red, green, blue));
					})
				})
			});

			nextColor = Pxrand(colors, inf).asStream;

			if (cvs.isNil, {
				nextCVKey = 1;
			}, {
				if (cvs.isKindOf(Dictionary).not and:{
					cvs.isKindOf(IdentityDictionary).not and:{
						cvs.isKindOf(Event).not
					}
				}, {
					Error("Arguments for CVCenter have to be either a Dictionary, an IdentityDictionary or an Event.").throw;
				}, {
					cvs.keysValuesDo({ |k, v|
						if ("^cv[0-9]".matchRegexp(k.asString).not, {
							all.put(k.asSymbol, v.asSpec);
						}, {
							"Your given key-name matches the reserved names for new keys in the CVCenter. Please choose a different name.".warn;
						})
					});
				})
			})
		});

		all['select snapshot'] ?? { all.put('select snapshot', SV(["select snapshot..."])) };
		all[\snapshot] ?? { all.put(\snapshot, CV(#[0, 1, \lin, 1.0])) };
	}

	*makeWindow {
		this.deprecated(thisMethod, this.class.findMethod(\front));
		^this.front;
	}

	*front { |tab...cvs|
		var flow;
		// var cvTabIndex, order, orderedCVs, msSize;
		var updateRoutine, lastUpdate, lastUpdateBounds, lastSetUp, lastCtrlBtnBank, removedKeys, skipJacks;
		var lastCtrlBtnsMode/*, swFlow*/;
		var allTabs, thisTabLabel;
		var rows, prefBut, saveBut, loadBut, shortcutsBut, globalShortcutsView, activateGlobalShortcuts;
		var snapShotBut, snapShotEdit;
		var tmp, doMakeWdgt;
		// var nDefGui, pDefGui, pDefnGui, tDefGui, allGui, historyGui, eqGui;
		var prefs, newPrefs;
		var buildCheckbox;
		var tmpConnectS, tmpConnectTF;
		// TabbedView2 specific

		// "adding tab within *front: %\n".postf(tab);

		// function for building cross-platform checkboxes
		buildCheckbox = { |view, active, action|
			var cBox;
			if (GUI.id === \cocoa, {
				cBox = Button(view, Point(15, 15))
					.states_([
						["", Color.white, Color.white],
						["X", Color.black, Color.white],
					])
					.font_(Font(Font.available("Arial Black") ? Font.defaultSansFace, 10, true))
				;
				if (active, { cBox.value_(1) }, { cBox.value_(0) });
			}, {
				cBox = \CheckBox.asClass.new(view, Point(15, 15)).value_(active);
			});
			cBox.action_(action);
		};

		tab !? { thisTabLabel = tab };
		cvs !? { this.put(*cvs) };
		prefs = CVCenterPreferences.readPreferences;

		this.guix ?? { this.guix_(prefs !? { prefs[\guiProperties] !? { prefs[\guiProperties].left }} ?? { 0 }) };
		this.guiy ?? { this.guiy_(prefs !? { prefs[\guiProperties] !? { prefs[\guiProperties].top }} ?? { 0 }) };
		this.guiwidth ?? { this.guiwidth_(prefs !? { prefs[\guiProperties] !? { prefs[\guiProperties].width }} ?? { 500 }) };
		this.guiheight ?? { this.guiheight_(prefs !? { prefs[\guiProperties] !? { prefs[\guiProperties].height }} ?? { 265 }) };

		if (window.isNil or:{ window.isClosed }, {
			window = Window("CVCenter", Rect(this.guix, this.guiy, this.guiwidth, this.guiheight)).alwaysOnTop_(alwaysOnTop).acceptsMouseOver_(true);
			if (Quarks.isInstalled("wslib") and:{ GUI.id !== \swing }, { window.background_(Color.black) });
			window.view.background_(Color.black);
			tabsBounds = Rect(4, 4, window.view.bounds.width, window.view.bounds.height-35);

			tabs = TabbedView2(window, tabsBounds)
				.tabCurve_(3)
				.labelPadding_(10)
				.alwaysOnTop_(alwaysOnTop)
				.resize_(5)
				.tabHeight_(15)
				.clickbox_(15)
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 12, true))
				.dragTabs_(true)
			;

			// tabs.view.backColor_(Color.rand);

			tabs.view.keyDownAction_({ |view, char, modifiers, unicode, keycode, key|
				if (keycode == KeyDownActions.keyCodes[\esc], { prefPane.focus })
			});

			// flow.shift(0, 0);

			prefPaneBounds = Rect(4, tabs.view.bounds.top+tabs.view.bounds.height, window.view.bounds.width, 35);
			prefPane = ScrollView(window, prefPaneBounds).hasBorder_(false);
			prefPane.decorator = swFlow = FlowLayout(prefPane.bounds, Point(0, 0), Point(1, 1));
			prefPane.resize_(8).background_(Color.black);

			prefBut = Button(prefPane, Point(70, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.states_([["preferences", Color.white, Color(0.3, 0.3, 0.3)]])
				.action_({ |pb| CVCenterPreferences.dialog })
				.acceptsMouseOver_(true)
			;

			if (GUI.id !== \cocoa, {
				prefBut.toolTip_("Edit the global preferences for CVCenter (resp.\nCVWidget). Preferences will be written to disk\nand become active upon library-recompile.")
			});

			saveBut = Button(prefPane, Point(70, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.states_([["save setup", Color.white, Color(0.15, 0.15, 0.15)]])
				.action_({ |sb| this.saveSetup })
			;

			if (GUI.id !== \cocoa, {
				saveBut.toolTip_("Save the current setup of CVCenter,\nincluding currently active OSC-/MIDI-\nresponders and actions.")
			});

			loadBut = Button(prefPane, Point(70, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.states_([["load setup", Color.white, Color(0.15, 0.15, 0.15)]])
				.action_({ |pb|
					CVCenterLoadDialog.new;
				})
			;

			if (GUI.id !== \cocoa, {
				loadBut.toolTip_("Load a CVCenter-setup from disk. You\nmay load OSC-/MIDI-responders and\nactions if the corresponding checkboxes\nto the right are checked accordingly.")
			});

			//snapShotBut, snapShotSelect, snapShotEdit;

			snapShotBut = Button(prefPane, Point(70, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.states_([
					["snapshot", Color.black, Color.yellow],
					["snapshot", Color.white, Color.yellow],
				])
			;

			// "this.at(\snapshot): %\n".postf(this.at(\snapshot));
			this.at(\snapshot).connect(snapShotBut);

			snapShotSelect = PopUpMenu(prefPane, Point(120, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.background_(Color.black)
				.stringColor_(Color.yellow)
			;

			// "this.at('select snapshot'): %\n".postf(this.at('select snapshot'));
			this.at('select snapshot').connect(snapShotSelect);

			shortcutsBut = Button(prefPane, Point(70, 20))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
				.states_([["shortcuts", Color.white, Color.red]])
				.action_({ |scb|
					CVCenterShortcutsEditor.dialog;
				})
			;

			globalShortcutsView = CompositeView(prefPane, Point(142, 20));

			activateGlobalShortcuts = buildCheckbox.(
				globalShortcutsView,
				KeyDownActions.globalShortcutsEnabled, {
					KeyDownActions.globalShortcutsEnabled_(activateGlobalShortcuts.value)
			}).bounds_(Rect(5, 2, 15, 15));

			StaticText(globalShortcutsView, Rect(25, 0, 122, 20))
				.string_("enable global shortcuts")
				.stringColor_(Color.white)
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 11))
			;

			// correct prefPane height if its contents span more than 1 line
			rows = prefPane.children.collect({ |child| child.bounds.top }).asSet.size;
			if (rows > 1, {
				prefPane.bounds_(Rect(
					prefPaneBounds.left,
					window.view.bounds.height-35-(rows-1*21),
					prefPane.bounds.width,
					prefPaneBounds.height+(rows-1*21)
				));
				tabs.view.bounds_(Rect(
					tabsBounds.left,
					tabsBounds.top,
					tabs.view.bounds.width,
					window.view.bounds.height-prefPane.bounds.height
				))
			});

			// this.setShortcuts;
			[tabs.views, prefPane].flat.do({ |view|
				KeyDownActions.setShortcuts(view, this.shortcuts);
			});

			window.onClose_({
				if (childViews.size > 0, {
					childViews.keysDo(_.close)
				});
				childViews.clear;
				tabProperties.clear;
				prefs !? {
					if (prefs[\saveGuiProperties] == 1, {
						newPrefs = CVCenterPreferences.readPreferences;
						if (newPrefs[\saveGuiProperties] == 1, {
							this.guix_(prefs[\guiProperties].left)
							.guiy_(prefs[\guiProperties].top)
							.guiwidth_(prefs[\guiProperties].width)
							.guiheight_(prefs[\guiProperties].height)
							;
							newPrefs.put(\guiProperties, prefs[\guiProperties]);
							CVCenterPreferences.writePreferences(
								newPrefs[\saveGuiProperties],
								newPrefs[\guiProperties],
								newPrefs[\saveClassVars],
								newPrefs[\midiMode],
								newPrefs[\midiResolution],
								newPrefs[\midiMean],
								newPrefs[\softWithin],
								newPrefs[\ctrlButtonBank],
								newPrefs[\removeResponders],
								newPrefs[\initMidiOnStartUp],
								informString: "Your CVCenter-preferences have successfully been written to disk."
							)
						})
					})
				};
				AbstractCVWidgetEditor.allEditors.pairsDo({ |editor, val|
					switch(cvWidgets[editor].class,
						CVWidgetKnob, {
							val.window.close;
						},
						CVWidget2D, {
							#[lo, hi].do({ |hilo|
								val[hilo] !? { val[hilo].window.close };
							})
						},
						CVWidgetMS, {
							cvWidgets[editor].msSize.do({ |sl|
								val[sl] !? { val[sl].window.close };
							});
							cvWidgets[editor].editor.msEditor !? {
								cvWidgets[editor].editor.msEditor.window.close;
							}
						}
					)
				})
			});

			if (cvWidgets.collect({ |w| w.notNil and:{ w.isClosed.not } }).size > 0, {
				allTabs = widgetStates.collectAs(_.tabKey, Array);
			}, {
				allTabs = [];
				if (tab.notNil, {
					allTabs = allTabs.add(tab.asSymbol)
				}, { allTabs = allTabs.add(\default) })
			});

			allTabs.do({ |label|
				this.prAddTab(thisTabLabel = label)
			});

			all.pairsDo({ |key, cv|
				// [key, cv].postln;
				cvWidgets[key] !? {
					cvWidgets[key].wdgtControllersAndModels !? {
						tmpConnectS = cvWidgets[key].wdgtControllersAndModels.slidersTextConnection.model.value[0];
						tmpConnectTF = cvWidgets[key].wdgtControllersAndModels.slidersTextConnection.model.value[1];
					}
				};

				// "tmpConnectS: %, tmpConnectTF: %\n".postf(tmpConnectS, tmpConnectTF);

				if ((cvWidgets[key].notNil and:{ cvWidgets[key].isClosed }).or(
					cvWidgets[key].isNil
				), {
					widgetStates[key] !? {
						widgetStates[key].tabKey !? { thisTabLabel = widgetStates[key].tabKey }
					};
					if (all[key].class == Event, {
						#[lo, hi].do({ |slot|
							tmp = all[key][slot].value;
							this.prAddWidget(
								thisTabLabel,
								(key: key, slot: slot, spec: all[key][slot].spec),
								key,
								tmpConnectS ? this.connectSliders,
								tmpConnectTF ? this.connectTextFields
							);
							this.at(key)[slot].value_(tmp);
						})
					}, {
						if (systemWidgets.includes(key).not, {
							this.prAddWidget(
								thisTabLabel,
								key: key,
								connectS: tmpConnectS ? this.connectSliders,
								connectTF: tmpConnectTF ? this.connectTextFields
							);
						}, {
							this.prAddWidget(
								\default,
								key: key,
								connectS: tmpConnectS ? this.connectSliders,
								connectTF: tmpConnectTF ? this.connectTextFields
							);
							case
								{ (key == \snapshot).or(key == 'select snapshot') } {
									cvWidgets[key].background_(Color.yellow);
								}
							;
						});
					})
				})
			});

			cvWidgets['select snapshot'].addAction('select snapshot', { |cv|
				if (cv.value > 0, {
					CVCenter.snapShots[cv.items[cv.value]].pairsDo({ |k, v|
						if (k != 'select snapshot' and:{
							k != 'snapshot'
						}, {
							if (CVCenter.at(k).class == Event) {
								#[lo, hi].do({ |slot| CVCenter.at(k)[slot].value_(v[slot]) })
							} { CVCenter.at(k).value_(v) }
						})
					})
				})
			});

			cvWidgets[\snapshot].addAction('save snapshot with confirm', { |cv|
				if (cv.value == 1, { defer { CVCenter.saveSnapshot(true) }; cv.value_(0) });
			}, active: false);

			cvWidgets[\snapshot].addAction('save snapshot no confirm', { |cv|
				if (cv.value == 1, { defer { CVCenter.saveSnapshot(false) }; cv.value_(0) });
			}, active: true)
		});

		window.front;

		skipJacks = SkipJack.all.collect({ |r| r === updateRoutine });
		if (skipJacks.includes(true).not, {
			updateRoutine = SkipJack({
				lastUpdate ?? { lastUpdate = all.size };
				lastSetUp !? {
					if (this.setup != lastSetUp, {
						this.prSetup(this.setup);
					})
				};
				if (all.size != lastUpdate, {
					if (all.size > lastUpdate and:{ cvWidgets.size <= lastUpdate }, {
						this.prAddWidget;
					});
					if (all.size < lastUpdate, {
						removedKeys = cvWidgets.keys.difference(all.keys);
						removedKeys.do({ |k|
							this.removeAt(k);
						});
						([tabs.activeTab]++childViews.collect({ |view| view.tabs.keys.asArray })).flat.do({ |view| this.prRegroupWidgets(view) });
					});
					lastUpdate = all.size;
				});
				if (lastUpdateBounds.notNil and:{ window.bounds.width != lastUpdateBounds.width }, {
					this.prRegroupWidgets(tabs.activeTab);
					this.prRegroupPrefPane;
				});
				if (childViews.size > 0, {
					childViews.pairsDo({ |child, childProps|
						if (childProps.lastUpdateBounds.notNil and:{
							child.bounds.width != childProps.lastUpdateBounds.width
						}, {
							childProps.tabs.keysDo({ |tab| this.prRegroupWidgets(tab) })
						})
					})
				});
				if (window.bounds != lastUpdateBounds, {
					prefs !? {
						if (prefs[\saveGuiProperties] == 1, { prefs[\guiProperties] = window.bounds });
					};
				});
				lastUpdateBounds = window.bounds;
				if (childViews.size > 0, {
					childViews.pairsDo({ |child, childProps|
						childProps.put(\lastUpdateBounds, child.bounds)
					})
				});
				prefs !? {
					if (prefs[\saveGuiProperties] == 1, { boundsOnShutDown = lastUpdateBounds });
				};
				lastSetUp = this.setup;
			}, 0.5, { window.isClosed }, "CVCenter-Updater");
		});
	}

	*prAddTab { |label|
		var labelColor, unfocusedColor;
		var modsDict, arrModsDict;
		var thisTab, thisTabLabel, thisIndex;
		var cachedView, oldChildView;

		if (label.notNil, { thisTabLabel = label.asSymbol }, {
			Error("*prAddTab has been called without providing a label for the tab").throw;
		});

		switch(GUI.id,
			\cocoa, {
				modsDict = KeyDownActions.modifiersCocoa;
				arrModsDict = KeyDownActions.arrowsModifiersCocoa;
			},
			\qt, {
				modsDict = KeyDownActions.modifiersQt;
				arrModsDict = KeyDownActions.arrowsModifiersQt;
			}
		);

		if (tabProperties[thisTabLabel].notNil, {
			labelColor = tabProperties[thisTabLabel].tabColor;
		}, {
			labelColor = nextColor.next;
		});
		unfocusedColor = labelColor.copy.alpha_(0.3);

		tabProperties[thisTabLabel] ?? {
			thisTab = tabs.add(thisTabLabel, scroll: true)
			.focusAction_({ |tab|
				this.prRegroupWidgets(tab)
			})
			.useDetachIcon_(true)
			.background_(Color.black)
			.labelColor_(labelColor)
			.unfocusedColor_(unfocusedColor)
			.stringColor_(Color.white)
			.stringFocusedColor_(Color.black)
			.onChangeParent_({ |view|
				childViews[view.parent.parent] !? {
					oldChildView = childViews[view.parent.parent][\tabs][view];
				};
				if (tabs.tabViews.includes(view), {
					cachedView = (widgets: this.widgetsAtTab(thisTabLabel));
				}, {
					childViews.do({ |child|
						cachedView = child[\tabs][view];
						child[\tabs].removeAt(view);
					});
				});
				this.shortcuts.do({ |keyDowns|
					// "onChangeParent view: %\n".postf(view.parent.parent);
					view.keyDownAction_(
						view.keyDownAction.addFunc({ |view, char, modifiers, unicode, keycode|
							var thisMod, thisArrMod;
							thisMod = keyDowns.modifierQt;
							thisArrMod = keyDowns.arrowsModifierQt;

							case
							{ modifiers == modsDict[\none] or:{ modifiers == arrModsDict[\none] }} {
								// "no modifier".postln;
								if (keycode == keyDowns.keyCode and:{
									thisMod.isNil and:{ thisArrMod.isNil }
								}, { keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode) });
							}
							{ modifiers != modsDict[\none] and:{ modifiers != arrModsDict[\none] }} {
								// "some modifier...".postln;
								if (keycode == keyDowns.keyCode and:{
									(modifiers == thisArrMod).or(modifiers == thisMod)
								}, { keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode) })
							}
							;
						})
					)
				})
			})
			.onAfterChangeParent_({ |view|
				view.tabbedView.window !? {
					view.tabbedView.window.background_(Color.black).alwaysOnTop_(alwaysOnTop);
				};
				cachedView !? {
					if (tabs.tabViews.includes(view).not, {
						if (childViews[view.parent.parent].isNil, {
							childViews.put(view.parent.parent, ());
						});
						childViews[view.parent.parent][\tabs] ?? {
							childViews[view.parent.parent].put(\tabs, ());
						};
						childViews[view.parent.parent][\tabs].put(view, cachedView);
					});
					window.name_("CVCenter: "++tabs.tabViews.collect(_.label));
					childViews.pairsDo({ |child, childProps|
						child.name_("CVCenter: "++childProps[\tabs].keys.collectAs({ |tab| tab.label }, Array));
					});
				};
				childViews.pairsDo({ |child, childProps| if (childProps.tabs.size < 1, { childViews.removeAt(child) }) });
			})
			;

			tabs.labelPadding_(10).refresh;

			thisTab.view.hasBorder_(false);

			this.window.name_("CVCenter: "++tabs.tabViews.collectAs(_.label, Array));

			this.shortcuts.do({ |keyDowns|
				thisTab.keyDownAction_(
					thisTab.keyDownAction.add({ |view, char, modifiers, unicode, keycode|
						var thisMod, thisArrMod;

						switch(GUI.id,
							\cocoa, {
								thisMod = keyDowns.modifierCocoa;
								thisArrMod = keyDowns.arrowsModifierCocoa;
							},
							\qt, {
								thisMod = keyDowns.modifierQt;
								thisArrMod = keyDowns.arrowsModifierQt;
							}
						);

						case
						{ modifiers == modsDict[\none] or:{ modifiers == arrModsDict[\none] }} {
							// "no modifier".postln;
							if (keycode == keyDowns.keyCode and:{
								thisMod.isNil and:{ thisArrMod.isNil }
							}, { keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode) });
						}
						{ modifiers != modsDict[\none] and:{ modifiers != arrModsDict[\none] }} {
							// "some modifier...".postln;
							if (keycode == keyDowns.keyCode and:{
								(modifiers == thisArrMod).or(modifiers == thisMod)
							}, { keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode) })
						}
						;
					})
				)
			})
		};

		tabProperties[thisTabLabel] ?? {
			tabProperties.put(thisTabLabel, (nextPos: Point(0, 0), tabColor: labelColor, detached: false));
		};
		thisTab.focus;
		^thisTab;
	}

	*prAddWidget { |tab, widget2DKey, key, connectS, connectTF|
		var allCVKeys, widgetKeys, thisKeys;
		var rowwidth, colcount;
		var cvTabIndex, tabLabels;
		var labelColor, unfocusedColor;
		var cvcArgs, btnColor;
		var msSize, tmp;
		var allTabs;
		var thisTab, thisTabLabel, thisTabColor, thisNextPos;
		var modsDict, arrModsDict;

		// "prAddWidget called: %, %, %\n".postf(tab, widget2DKey, key);

		if (tabProperties.notNil, {
			allTabs = (tabs.tabViews++childViews.collect({ |view| view.tabs.keys.asArray })).flat;
		}, { allTabs = [] });

		// "allTabs: %\n".postf(allTabs);

		if (tab.notNil, { thisTabLabel = tab.asSymbol }, {
			if (tabs.activeTab.notNil, { thisTabLabel = tabs.activeTab.label.asSymbol }, { thisTabLabel = \default });
		});

		// "tabProperties: %\n".postf(tabProperties);

		thisTab = allTabs.detect({ |ttab| ttab.label.asSymbol == thisTabLabel }) ?? {
			thisTab = this.prAddTab(thisTabLabel);
		};
		cvTabIndex = tabs.tabViews.detect { |tab| tab.label.asSymbol == thisTabLabel }.index;
		thisNextPos = tabProperties[thisTabLabel].nextPos;

		rowheight = widgetheight+1+15; // add a small gap between rows

		if (cvWidgets[key].notNil, {
			if (cvWidgets[key].midiOscEnv.notNil, {
				cvcArgs = (midiOscEnv: cvWidgets[key].midiOscEnv);
			}, {
				cvcArgs = true;
			});
		}, { cvcArgs = true });

		case
			{ all[key].class === Event and:{
				all[key].keys.includesAny(#[lo, hi])
			}} {
				tmp = (
					setup: (
						lo: (
							midiMode: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMode(\lo) }, { this.midiMode }),
							midiResolution: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiResolution(\lo) }, { this.midiResolution }),
							midiMean: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMean(\lo) }, { this.midiMean }),
							ctrlButtonBank: if (cvWidgets[key].notNil, { cvWidgets[key].getCtrlButtonBank(\lo) }, { this.ctrlButtonBank }),
							softWithin: if (cvWidgets[key].notNil, { cvWidgets[key].getSoftWithin(\lo) }, { this.softWithin }),
							calibrate: if (cvWidgets[key].notNil, { cvWidgets[key].getCalibrate(\lo) }, { true })
						),
						hi: (
							midiMode: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMode(\hi) }, { this.midiMode }),
							midiResolution: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiResolution(\hi) }, { this.midiResolution }),
							midiMean: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMean(\hi) }, { this.midiMean }),
							ctrlButtonBank: if (cvWidgets[key].notNil, { cvWidgets[key].getCtrlButtonBank(\hi) }, { this.ctrlButtonBank }),
							softWithin: if (cvWidgets[key].notNil, { cvWidgets[key].getSoftWithin(\hi) }, { this.softWithin }),
							calibrate: if (cvWidgets[key].notNil, { cvWidgets[key].getCalibrate(\hi) }, { true })
						),
					),
					wdgtActions: cvWidgets[key] !? { cvWidgets[key].wdgtActions }
				);
				if (cvWidgets[key].isNil or:{ cvWidgets[key].isClosed }, {
					cvWidgets[key] = CVWidget2D(
						thisTab,
						(lo: all[key].lo, hi: all[key].hi),
						key,
						connectS ? this.connectSliders,
						connectTF ? this.connectTextFields,
						Rect(thisNextPos.x, thisNextPos.y, widgetwidth = 105, widgetheight),
						setup: tmp.setup,
						controllersAndModels: cvWidgets[key] !? {
							(lo: cvWidgets[key].wdgtControllersAndModels.lo, hi: cvWidgets[key].wdgtControllersAndModels.hi)
						},
						cvcGui: cvcArgs
					);
					defer {
						removeButs.put(key,
							Button(thisTab, Rect(thisNextPos.x, thisNextPos.y+widgetheight, widgetwidth, 15))
								.states_([["remove", Color.white, Color(0.0, 0.15)]])
								.action_({ |b| this.removeAt(key) })
								.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 10))
							;
						)
					};
					if (widgetStates[key].isNil, {
						widgetStates.put(key, (
							tabIndex: cvTabIndex,
							tabKey: thisTabLabel,
							// slidersConnected: connectS ? this.connectSliders,
							// textFieldsConnected: connectTF ? this.connectTextFields
						))
					}, {
						widgetStates[key].tabIndex = cvTabIndex;
						widgetStates[key].tabKey = thisTabLabel;
					// widgetStates[key].slidersConnected = connectS ? this.connectSliders;
					// widgetStates[key].textFieldsConnected = connectTF ? this.connectTextFields;
					});
					cvWidgets[key].background_(tabProperties[thisTabLabel].tabColor);
				});
				tmp.wdgtActions !? { cvWidgets[key].wdgtActions = tmp.wdgtActions };
			}
			{ #[minval, maxval, step, default].select({ |prop| all[key].spec.perform(prop).isArray }).size > 0} {
				msSize = #[minval, maxval, step, default].collect({ |prop| all[key].spec.perform(prop).size }).maxItem;
				tmp = (
					setup: msSize.collect({ |sl|
						(
							midiMode: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMode(sl) }, { this.midiMode }),
							midiResolution: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiResolution(sl) }, { this.midiResolution }),
							midiMean: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMean(sl) }, { this.midiMean }),
							ctrlButtonBank: if (cvWidgets[key].notNil, { cvWidgets[key].getCtrlButtonBank(sl) }, {this.ctrlButtonBank }),
							softWithin: if (cvWidgets[key].notNil, { cvWidgets[key].getSoftWithin(sl) }, { this.softWithin }),
							calibrate: if (cvWidgets[key].notNil, { cvWidgets[key].getCalibrate(sl) }, { true })
						)
					}),
					wdgtActions: cvWidgets[key] !? { cvWidgets[key].wdgtActions }
				);

				if (msSize <= numMsSlotsPerColumn, { widgetwidth = 105 }, {
					widgetwidth = msSize.div(numMsSlotsPerColumn)*53+52
				});

				if (cvWidgets[key].isNil or:{ cvWidgets[key].isClosed }, {
					cvWidgets[key] = CVWidgetMS(
						thisTab,
						all[key],
						key,
						connectS ? this.connectSliders,
						connectTF ? this.connectTextFields,
						Rect(thisNextPos.x, thisNextPos.y, widgetwidth, widgetheight),
						setup: tmp.setup,
						controllersAndModels: cvWidgets[key] !? { cvWidgets[key].wdgtControllersAndModels },
						cvcGui: cvcArgs,
						numSliders: msSize
					);

					defer {
						removeButs.put(key,
							Button(thisTab, Rect(thisNextPos.x, thisNextPos.y+widgetheight, widgetwidth, 15))
								.states_([["remove", Color.white, Color(0.0, 0.15)]])
								.action_({ |b| this.removeAt(key) })
								.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 10))
							;
						)
					};

					if (widgetStates[key].isNil, {
						widgetStates.put(key, (
							tabIndex: cvTabIndex,
							tabKey: thisTabLabel,
						// slidersConnected: connectS ? this.connectSliders,
						// textFieldsConnected: connectTF ? this.connectTextFields
						))
					}, {
						widgetStates[key].tabIndex = cvTabIndex;
						widgetStates[key].tabKey = thisTabLabel;
					// widgetStates[key].slidersConnected = connectS ? this.connectSliders;
					// widgetStates[key].textFieldsConnected = connectTF ? this.connectTextFields;
					});
					defer { cvWidgets[key].background_(tabProperties[thisTabLabel].tabColor) };
				});
				tmp.wdgtActions !? { cvWidgets[key].wdgtActions = tmp.wdgtActions };
			}
			{
				tmp = (
					setup: (
						midiMode: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMode }, { this.midiMode }),
						midiResolution: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiResolution }, { this.midiResolution }),
						midiMean: if (cvWidgets[key].notNil, { cvWidgets[key].getMidiMean }, { this.midiMean }),
						ctrlButtonBank: if (cvWidgets[key].notNil, { cvWidgets[key].getCtrlButtonBank }, { this.ctrlButtonBank }),
						softWithin: if (cvWidgets[key].notNil, { cvWidgets[key].getSoftWithin }, { this.softWithin }),
						calibrate: if (cvWidgets[key].notNil, { cvWidgets[key].getCalibrate }, { true }),
					),
					wdgtActions: cvWidgets[key] !? { cvWidgets[key].wdgtActions }
				);

			// "connectS: %, connectTF: %\n".postf(connectS, connectTF);

				if (cvWidgets[key].isNil or:{ cvWidgets[key].isClosed }, {
					cvWidgets[key] = CVWidgetKnob(
						thisTab,
						all[key],
						key,
						connectS ? this.connectSliders,
						connectTF ? this.connectTextFields,
						Rect(thisNextPos.x, thisNextPos.y, widgetwidth = 52, widgetheight),
						setup: tmp.setup,
						controllersAndModels: cvWidgets[key] !? { cvWidgets[key].wdgtControllersAndModels },
						cvcGui: cvcArgs
					);

					defer {
						removeButs.put(key,
							Button(thisTab, Rect(thisNextPos.x, thisNextPos.y+widgetheight, widgetwidth, 15))
								.states_([["remove", Color.white, Color(0.0, 0.15)]])
								.action_({ |b| this.removeAt(key) })
								.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 10))
							;
						);
						if (key === \snapshot or:{ key === 'select snapshot'}) {
							removeButs[key].enabled_(false);
						}
					};

					if (widgetStates[key].isNil, {
						widgetStates.put(key, (
							tabIndex: cvTabIndex,
							tabKey: thisTabLabel,
						))
					}, {
						widgetStates[key].tabIndex = cvTabIndex;
						widgetStates[key].tabKey = thisTabLabel;
					});
					defer { cvWidgets[key].background_(tabProperties[thisTabLabel].tabColor) };
				});
				tmp.wdgtActions !? { cvWidgets[key].wdgtActions = tmp.wdgtActions };
			}
		;

		switch(cvWidgets[key].class,
			CVWidgetKnob, {
				cvWidgets[key].wdgtControllersAndModels.midiDisplay.model.value_(
					cvWidgets[key].wdgtControllersAndModels.midiDisplay.model.value
				).changedKeys(cvWidgets[key].synchKeys);
				cvWidgets[key].wdgtControllersAndModels.oscDisplay.model.value_(
					cvWidgets[key].wdgtControllersAndModels.oscDisplay.model.value
				).changedKeys(cvWidgets[key].synchKeys);
				cvWidgets[key].wdgtControllersAndModels.actions.model.value_((
					numActions: cvWidgets[key].wdgtActions.size,
					activeActions: cvWidgets[key].wdgtActions.select({ |v| v.asArray[0][1] == true }).size
				)).changedKeys(cvWidgets[key].synchKeys);
			},
			CVWidget2D, {
				#[lo, hi].do({ |hilo|
					cvWidgets[key].wdgtControllersAndModels[hilo].midiDisplay.model.value_(
						cvWidgets[key].wdgtControllersAndModels[hilo].midiDisplay.model.value
					).changedKeys(cvWidgets[key].synchKeys);
					cvWidgets[key].wdgtControllersAndModels[hilo].oscDisplay.model.value_(
						cvWidgets[key].wdgtControllersAndModels[hilo].oscDisplay.model.value
					).changedKeys(cvWidgets[key].synchKeys);
					cvWidgets[key].wdgtControllersAndModels[hilo].actions.model.value_((
						numActions: cvWidgets[key].wdgtActions[hilo].size,
						activeActions: cvWidgets[key].wdgtActions[hilo].select({ |v| v.asArray[0][1] == true }).size
					)).changedKeys(cvWidgets[key].synchKeys);
				})
			},
			CVWidgetMS, {
				cvWidgets[key].msSize.do({ |sl|
					cvWidgets[key].wdgtControllersAndModels.slots[sl].midiDisplay.model.value_(
						cvWidgets[key].wdgtControllersAndModels.slots[sl].midiDisplay.model.value
					).changedKeys(cvWidgets[key].synchKeys);
					cvWidgets[key].wdgtControllersAndModels.slots[sl].oscDisplay.model.value_(
						cvWidgets[key].wdgtControllersAndModels.slots[sl].oscDisplay.model.value
					).changedKeys(cvWidgets[key].synchKeys);
				});
				cvWidgets[key].wdgtControllersAndModels.actions.model.value_((
					numActions: cvWidgets[key].wdgtActions.size,
					activeActions: cvWidgets[key].wdgtActions.select({ |v| v.asArray[0][1] == true }).size
				)).changedKeys(cvWidgets[key].synchKeys);
			}
		);

		colwidth = widgetwidth+1; // add a small gap between widgets
		rowwidth = thisTab.bounds.width-15;
		if (thisNextPos.x+colwidth >= (rowwidth-colwidth-15), {
			// jump to next row
			thisNextPos = tabProperties[thisTabLabel].nextPos; //old
		}, {
			// add next widget to the right
			tabProperties[thisTabLabel].nextPos = thisNextPos = thisNextPos.x + Point(colwidth, (thisNextPos.y));
		});

		widget2DKey !? {
			cvWidgets[widget2DKey.key].setSpec(widget2DKey.spec, widget2DKey.slot);
		};

		tabs.activeTab !? {
			if (tabs.activeTab.index == cvTabIndex, {
				// "tabs.activeTab.index == cvTabIndex".postln;
				this.prRegroupWidgets(tabs.activeTab)
			}/*, {
				"tabs.activeTab.index != cvTabIndex".postln;
				tabs.focus(cvTabIndex)
			}*/);
		};
		window.front;
	}

	*put { |...args|
		var inputArgs, overwrite=false, tmp;
		inputArgs = args;
		if (inputArgs.size.odd, {
			overwrite = inputArgs.pop;
			if (overwrite.isKindOf(Boolean).not, {
				overwrite = nil;
			});
		});
		this.new;
		inputArgs.pairsDo({ |key, cv|
			if (cv.isKindOf(CV).not and:{ cv.isKindOf(Array).not }, {
				Error("CVCenter expects a single CV or an array of CVs as input!").throw;
			});
			[cv].flat.do({ |cv|
				if (cv.isKindOf(CV).not, {
					Error("The value provided for key '"++key.asString++"' doesn't appear to be a CV.\nPlease choose a valid input!").throw;
				})
			});
			if (cv.isKindOf(Array) and:{ cv.size == 2 }, {
				tmp = cv.copy;
				#[lo, hi].do({ |key, i|
					cv.isKindOf(Event).not.if { cv = () };
					cv.put(key, tmp[i]);
				})
			});

			if (overwrite, {
				all.put(key.asSymbol, cv);
			}, {
				if (all.matchAt(key.asSymbol).isNil, {
					all.put(key.asSymbol, cv);
				}, {
					("There is already a CV stored under the name '"++key.asString++"'. \nPlease choose a different key-name!").warn;
				})
			})
		})
	}

	*removeAt { |key|
		var lastVal, thisKey, thisTabIndex, thisTabKey;

		thisKey = key.asSymbol;
		// prevent removal of default widgets;
		if (thisKey === \snapshot or: { thisKey === 'select snapshot'}) { ^this };
		thisTabKey = widgetStates[thisKey].tabKey;
		all.removeAt(thisKey);
		cvWidgets[thisKey].class.switch(
			CVWidgetKnob, {
				if (cvWidgets[thisKey].editor.notNil and:{ cvWidgets[thisKey].editor.isClosed.not }, {
					cvWidgets[thisKey].editor.close;
				});
				cvWidgets[thisKey].midiOscEnv.cc !? {
					cvWidgets[thisKey].midiOscEnv.cc.remove;
					cvWidgets[thisKey].midiOscEnv.cc = nil;
				};
				cvWidgets[thisKey].midiOscEnv.oscResponder !? {
					cvWidgets[thisKey].midiOscEnv.oscResponder.remove;
					cvWidgets[thisKey].midiOscEnv.oscResponder = nil;
				};
			},
			CVWidget2D, {
				#[lo, hi].do({ |hilo|
					if (cvWidgets[thisKey].editor[hilo].notNil and:{
						cvWidgets[thisKey].editor[hilo].isClosed.not
					}, {
						cvWidgets[thisKey].editor[hilo].close;
					});
					cvWidgets[thisKey].midiOscEnv[hilo].cc !? {
						cvWidgets[thisKey].midiOscEnv[hilo].cc.remove;
						cvWidgets[thisKey].midiOscEnv[hilo].cc = nil;
					};
					cvWidgets[thisKey].midiOscEnv[hilo].oscResponder !? {
						cvWidgets[thisKey].midiOscEnv[hilo].oscResponder.remove;
						cvWidgets[thisKey].midiOscEnv[hilo].oscResponder = nil;
					}
				})
			},
			CVWidgetMS, {
				if (cvWidgets[thisKey].editor.msEditor.notNil and:{
					cvWidgets[thisKey].editor.msEditor.isClosed.not
				}, {
					cvWidgets[thisKey].editor.msEditor.close;
				});
				cvWidgets[thisKey].msSize.do({ |sl|
					if (cvWidgets[thisKey].editor.editors[sl].notNil and:{
						cvWidgets[thisKey].editor.editors[sl].isClosed.not
					}, {
						cvWidgets[thisKey].editor.editors[sl].close
					});
					cvWidgets[thisKey].midiOscEnv[sl].cc !? {
						cvWidgets[thisKey].midiOscEnv[sl].cc.remove;
						cvWidgets[thisKey].midiOscEnv[sl].cc = nil;
					};
					cvWidgets[thisKey].midiOscEnv[sl].oscResponder !? {
						cvWidgets[thisKey].midiOscEnv[sl].oscResponder.remove;
						cvWidgets[thisKey].midiOscEnv[sl].oscResponder = nil;
					}
				})
			}
		);
		cvWidgets[thisKey].remove;
		cvWidgets.removeAt(key);
		removeButs[thisKey].remove;
		removeButs.removeAt(thisKey);

		if (window.notNil and:{
			window.isClosed.not
		}, {
			if (this.widgetsAtTab(widgetStates[thisKey][\tabKey]).size == 0, {
				this.prRemoveTab(thisTabKey);
			})
		});

		if (this.widgetsAtTab(widgetStates[thisKey][\tabKey]).size == 0, {
			if (tabProperties.size > 1, {
				widgetStates.do({ |ws|
					if (ws.tabIndex > widgetStates[thisKey].tabIndex, { ws.tabIndex = ws.tabIndex-1 });
				})
			})
		});

		widgetStates.removeAt(thisKey);
	}

	*removeAll { |...keys|
		var toBeRemoved = all.keys.reject({ |k| k === \snapshot or:{ k === 'select snapshot' }});

		if (keys.size < 1, {
			toBeRemoved.do(this.removeAt(_));
		}, {
			keys.do(this.removeAt(_));
		});
	}

	*removeAtTab { |label|
		var wdgts;
		wdgts = this.widgetsAtTab(label.asSymbol);
		if (wdgts.isEmpty.not, { this.removeAll(*wdgts) });
	}

	*removeTab { |label|
		var thisTab = tabs.tabViews.detect{ |tab| tab.label.asSymbol == label.asSymbol };
		this.removeAtTab;
		thisTab !? {
			tabs.removeAt(thisTab.index);
			tabProperties[label.asSymbol] = nil;
		}
	}

	*at { |key|
		^all.at(key.asSymbol);
	}

	*prAdd { |key, spec, value, tab, slot, svItems, connectS, connectTF|
		var thisKey, thisSpec, thisVal, testSlot, thisSlot, thisTab, widget2DKey;
		var specName, cvClass, thisSVItems;

		thisKey = key.asSymbol;

		if (svItems.notNil, {
			if (svItems.isKindOf(SequenceableCollection).not, {
				Error("svItems must be a SequenceableCollection or an instance of one of its subclasses!").throw;
			}, {
				thisSVItems = svItems.collect(_.asSymbol);
				cvClass = SV;
			})
		}, {
			cvClass = CV;
		});

		// "add called with key '%' and tab '%'\n".postf(key, tab);

		// if a 2D-widget under the given key exists force the given slot to become
		// the other slot of the already existing widget
		// also prevents misbehaviour in case of bogus slots
		if (cvWidgets[thisKey].notNil and:{
			cvWidgets[thisKey].class == CVWidget2D
		}, {
			block { |break|
				#[lo, hi].do({ |hilo|
					if (widgetStates[thisKey].notNil and:{
						widgetStates[thisKey][hilo].isNil
					}, { break.value(thisSlot = hilo) })
				})
			}
		});

		// above test didn't apply. so we can assume no widget exists under the given key
		if (slot.notNil and:{ thisSlot.isNil }, {
			thisSlot = slot.asString.toLower.asSymbol;
			if (#[lo, hi].detect({ |sbl| sbl === thisSlot }).class !== Symbol, {
				Error("Looks like you wanted to create a multi-dimensional widget. However, the given slot-value '%' is not valid!".format(slot)).throw;
			})
		});

		if (spec.class == ControlSpec, { thisSpec = spec }, {
			// CVWidgetMS
			// if spec.asSpec returns nil make it a default ControlSpec by calling as Spec again
			if (spec.isArray.not, { thisSpec = spec.asSpec.asSpec }, {
				// protect from arrays containing nils
				if (spec.indicesOfEqual(nil).size == spec.size or: {
					spec.asSet.size == 1
				}) {
					thisSpec = ControlSpec(
						spec.collect({ |sp| sp.asSpec.asSpec.minval }),
						spec.collect({ |sp| sp.asSpec.asSpec.maxval }),
						spec[0].asSpec.asSpec.warp,
						spec.collect({ |sp| sp.asSpec.asSpec.step }),
						spec.collect({ |sp| sp.asSpec.asSpec.default })
					);
				} {
					thisSpec = spec.asSpec;
				};
				if (thisSpec.safeHasZeroCrossing) { thisSpec.warp_(\lin) };

				// protect from arrays containing nils
				if (spec.indicesOfEqual(nil).size == spec.size or: {
					spec.asSet.size == 1
				}) {
					if ((specName = Spec.specs.findKeyForValue(spec[0].asSpec)).notNil) {
						Spec.add((specName++"_"++spec.size).asSymbol, thisSpec);
					}
				}
			}, {
				Error("The given value '%' is an array but could not be converted to a valid ControlSpec".format(spec)).throw;
			})
		});

		case
			{ tab.notNil } { thisTab = tab }
			{ tab.isNil and:{ tabs.notNil and:{ tabs.activeTab.notNil }}} { thisTab = tabs.activeTab.label }
			{ tab.isNil and:{ tabs.notNil and:{ tabs.activeTab.isNil }}} { thisTab = \default }
			{ tab.isNil and:{ tabs.isNil }} { thisTab = \default }
		;

		this.new;

		thisSlot !? {
			widgetStates[thisKey] ?? { widgetStates.put(thisKey, ()) };
			widgetStates[thisKey][thisSlot] ?? { widgetStates[thisKey].put(thisSlot, ()) };
		};

		if (value.notNil, {
			case
				{ value.isNumber } { thisVal = value }
				{ value.isArray and:{ value.select(_.isNumber).size == value.size }} { thisVal = value }
				{ thisVal = thisSpec.default }
			;
		}, {
			thisVal = thisSpec.default;
		});

		// make sure the default value is suitable for multidimensional ControlSpecs
		if ([thisSpec.minval, thisSpec.maxval, thisSpec.step, thisSpec.default].select(_.isArray).size > 0, {
			thisVal = thisVal.asArray;
		});

		if (thisSlot.notNil and:{ (thisSlot === \lo).or(thisSlot === \hi) }, {
			// CVWidget2D
			if (cvWidgets[thisKey].notNil and:{ cvWidgets[thisKey].isClosed.not }, {
				if (widgetStates[thisKey][\hi][\made] == true or:{
					widgetStates[thisKey][\lo][\made] == true
				}, {
					widgetStates[thisKey][thisSlot][\made] ?? {
						cvWidgets[thisKey].setSpec(thisSpec, thisSlot);
						this.at(thisKey)[thisSlot].value_(thisVal);
					}
				});
				widgetStates[thisKey][thisSlot][\made] = true;
				^all[thisKey][thisSlot];
			}, {
				all[thisKey] ?? { all.put(thisKey, (lo: cvClass.new, hi: cvClass.new)) };
				all[thisKey][thisSlot].spec_(thisSpec);
				if (cvClass === SV and:{
					all[thisKey][thisSlot].items.unbubble.isNil
				}, { all[thisKey][thisSlot].items_(thisSVItems) });
				widget2DKey = (key: thisKey, slot: thisSlot, spec: thisSpec);
				widgetStates[thisKey][thisSlot].made = true;
			})
		}, {
			// other CVWidgets
			all[thisKey] ?? { all.put(thisKey, cvClass.new(thisSpec, thisVal)) };
			if (cvClass === SV and:{ all[thisKey].items.unbubble.isNil }, {
				all[thisKey].items_(thisSVItems)
			})
		});

		if (window.isNil or:{ window.isClosed }, {
			this.front(thisTab);
		}, {
			if (cvWidgets[thisKey].isNil or: { cvWidgets[thisKey].class == CVWidget2D })  {
				this.prAddWidget(thisTab, widget2DKey, thisKey, connectS, connectTF);
			}
		});

		if (slot.notNil, {
			^all[thisKey][thisSlot];
		}, {
			^all[thisKey];
		})
	}

	// spec inference - if it does not find the name, zaps all the non-alpha and looks again
	// This allows "freq 1" to resolve to \freq
	*findSpec { |name|
		var spec = name.asSymbol.asSpec;
		spec ?? { spec = name.asString.select({ |c| c.isAlpha }).asSymbol.asSpec };
		^spec;
	}

	// add a CV using spec inference
	*use { |key, spec, value, tab, slot, svItems, connectS, connectTF|
		var thisKey = key.asSymbol;
		var thisSlot;

		all[thisKey] !? {
			if (all[thisKey].class !== Event) {
				^all[thisKey];
			} {
				if (all[thisKey].size == 2 and:{
					(widgetStates[thisKey][\hi].notNil).and(widgetStates[thisKey][\lo].notNil)
				}) {
					^all[thisKey][thisSlot];
				}
			}
		};

		^this.prAdd(
			thisKey,
			spec ?? { this.findSpec(key) },
			value, tab, slot, svItems,
			connectS ?? { this.connectSliders },
			connectTF ?? { this.connectTextFields }
		)
	}

	*widgetConnectGUI { |key, connectSliders, connectTextFields|
		var thisKey;
		thisKey = key.asSymbol;
		connectSliders !? {
			connectSliders.isKindOf(Boolean).not.if{
				Error("connectSliders must either be a Boolean or nil!").throw;
			}
		};
		connectTextFields !? {
			connectTextFields.isKindOf(Boolean).not.if{
				Error("connectTextFields must either be a Boolean or nil!").throw;
			}
		};
		widgetStates !? {
			widgetStates[thisKey] !? {
				cvWidgets[key].connectGUI(connectSliders, connectTextFields);
			}
		}
	}


	*setup {
		^(
			midiMode: this.midiMode,
			midiResolution: this.midiResolution,
			midiMean: this.midiMean,
			ctrlButtonBank: this.ctrlButtonBank,
			softWithin: this.softWithin
		)
	}

	*alwaysOnTop_ { |bool|
		alwaysOnTop = bool.asBoolean;
		window !? { window.alwaysOnTop_(alwaysOnTop) };
		if (childViews.size > 0, {
			childViews.keys.do(_.alwaysOnTop_(alwaysOnTop));
		})
	}

	*guiMoveTo { |point|
		if (point.isKindOf(Point).not, {
			Error("guiMoveTo expects a Point in the form of e.g. Point(0, 0)").throw;
		});
		this.guix_(point.x);
		this.guiy_(point.y);
		window.bounds_(Rect(this.guix, this.guiy, this.guiwidth, this.guiheight));
	}

	*guiChangeDimensions { |point|
		if (point.isKindOf(Point).not, {
			Error("guiMoveTo expects a Point in the form of e.g. Point(0, 0)").throw;
		});
		this.guiwidth_(point.x);
		this.guiheight_(point.y);
		window.bounds_(Rect(this.guix, this.guiy, this.guiwidth, this.guiheight));
	}

	*bounds {
		^window.bounds;
	}

	*bounds_ { |rect|
		this.guix_(rect.left);
		this.guiy_(rect.top);
		this.guiwidth_(rect.width);
		this.guiheight_(rect.height);
		window.bounds_(rect);
	}

	*isClosed {
		if (this.childViews.size == 0 and:{
			this.window.isNil.or(this.window.notNil and:{ this.window.isClosed })
		}, { ^true });
		^false;
	}

	*renameTab { |oldName, newName|
		var index;
		index = tabs.tabViews.detectIndex({ |tab, i| tab.label == oldName.asString });
		tabs.tabAt(index).label = newName.asString;
		tabs.refresh;
		tabProperties.flipKeys(oldName.asSymbol, newName.asSymbol);
	}

	*addActionAt { |key, name, action, slot, active=true|
		key ?? { Error("You have to provide the CV's key in order to add an action!").throw };
		cvWidgets[key.asSymbol].addAction(name, action, slot, active);
	}

	*removeActionAt { |key, name, slot|
		key ?? { Error("You have to provide the CV's key in order to remove an action!").throw };
		cvWidgets[key.asSymbol].removeAction(name, slot);
	}

	*activateActionAt { |key, name, activate, slot|
		key ?? { Error("You have to provide the CV's key in order to activate or deactivate an action!").throw };
		cvWidgets[key.asSymbol].activateAction(name, activate, slot);
	}

	*widgetsAtTab { |label|
		var index, wdgts = [];
		all.keys.do({ |key|
			widgetStates[key] !? {
				if (widgetStates[key].tabKey == label.asSymbol, { wdgts = wdgts.add(key) });
			}
		});
		^wdgts;
	}

	*saveSnapshot { |dialog=false|
		var key, dialogWin, keyField, cv2D;

		key = Date.getDate.stamp.asSymbol;

		if (dialog, {
			dialogWin = Window("save snapshot", Rect(
				Window.screenBounds.width-300/2, Window.screenBounds.height-50/2,
				300, 50
			));

			dialogWin.view.background_(Color.black);

			keyField = TextField(dialogWin, Rect(4, 4, 290, 20))
				.string_(key)
				.font_(Font(Font.available("Courier") ? Font.defaultMonoFace, 14))
			;

			Button(dialogWin, Rect(4, 26, 144, 20))
				.states_([["cancel", Color.white, Color(0.1, 0.1, 0.1)]])
				.action_({ dialogWin.close })
			;

			Button(dialogWin, Rect(151, 26, 144, 20))
				.states_([["save snapshot", Color.white, Color.red]])
				.action_({
					snapShots.put(keyField.string.asSymbol, all.collect({ |cv|
						if (cv.class == Event) { (lo: cv.lo.value, hi: cv.hi.value) } { cv.value }
					}));
					this.at('select snapshot').items_(
						this.at('select snapshot').items ++ keyField.string.asSymbol
					);
					dialogWin.close;
				})
			;

			dialogWin.front;
		}, {
			snapShots.put(key, all.collect({ |cv|
				if (cv.class == Event) { (lo: cv.lo.value, hi: cv.hi.value) } { cv.value }
			}));
			this.at('select snapshot').items_(this.at('select snapshot').items ++ key.asSymbol);
		});

		window.onClose_(
			window.onClose.addFunc({ dialogWin !? { dialogWin.close }})
		)
	}

	*deleteSnapshots {
		this.at('select snapshot').items_(['select snapshot...']);
		snapShots = ();
	}

	*saveSetup { |path|
		var lib, successFunc;
		successFunc = { |f|
			lib = Library();
			lib.put( \all, ());
			all.pairsDo({ |k, cv|
				if (dontSave.includes(k).not, { // each slot in dontSave must be a Symbol
					lib[\all].put(k, ());
					switch(cvWidgets[k].class,
						CVWidget2D, {
							lib[\all][k].wdgtClass = CVWidget2D;
							#[lo, hi].do({ |hilo|
								lib[\all][k][hilo] = (
									spec: cvWidgets[k].widgetCV[hilo].spec,
									val: cvWidgets[k].widgetCV[hilo].value,
									actions: cvWidgets[k].wdgtActions !? {
										cvWidgets[k].wdgtActions[hilo].reject({ |k|
											"open Function".matchRegexp(k.values[0][0])
										})
									},
									osc: (
										addr: cvWidgets[k].midiOscEnv[hilo].oscResponder !? {
											cvWidgets[k].midiOscEnv[hilo].oscResponder.addr
										},
										cmdName: cvWidgets[k].midiOscEnv[hilo].oscResponder !? {
											cvWidgets[k].midiOscEnv[hilo].oscResponder.cmdName
										},
										msgIndex: cvWidgets[k].midiOscEnv[hilo].oscMsgIndex,
										calibConstraints: cvWidgets[k].getOscInputConstraints(hilo),
										oscMapping: cvWidgets[k].getOscMapping(hilo)
									),
									midi: (
										src: cvWidgets[k].midiOscEnv[hilo].midisrc,
										chan: cvWidgets[k].midiOscEnv[hilo].midichan,
										num: cvWidgets[k].midiOscEnv[hilo].midiRawNum,
										midiMode: cvWidgets[k].getMidiMode(hilo),
										midiMean: cvWidgets[k].getMidiMean(hilo),
										softWithin: cvWidgets[k].getSoftWithin(hilo),
										midiResolution: cvWidgets[k].getMidiResolution(hilo),
										ctrlButtonBank: cvWidgets[k].getCtrlButtonBank(hilo)
									)
								)
							})
						},
						CVWidgetKnob, {
							lib[\all][k] = (
								spec: cvWidgets[k].widgetCV.spec,
								val: cvWidgets[k].widgetCV.value,
								actions: cvWidgets[k].wdgtActions.reject({ |k|
									"open Function".matchRegexp(k.values[0][0])
								}),
								osc: (
									addr: cvWidgets[k].midiOscEnv.oscResponder !? {
										cvWidgets[k].midiOscEnv.oscResponder.addr
									},
									cmdName: cvWidgets[k].midiOscEnv.oscResponder !? {
										cvWidgets[k].midiOscEnv.oscResponder.cmdName
									},
									msgIndex: cvWidgets[k].midiOscEnv.oscMsgIndex,
									calibConstraints: cvWidgets[k].getOscInputConstraints,
									oscMapping: cvWidgets[k].getOscMapping
								),
								midi: (
									src: cvWidgets[k].midiOscEnv.midisrc,
									chan: cvWidgets[k].midiOscEnv.midichan,
									num: cvWidgets[k].midiOscEnv.midiRawNum,
									midiMode: cvWidgets[k].getMidiMode,
									midiMean: cvWidgets[k].getMidiMean,
									softWithin: cvWidgets[k].getSoftWithin,
									midiResolution: cvWidgets[k].getMidiResolution,
									ctrlButtonBank: cvWidgets[k].getCtrlButtonBank
								),
								wdgtClass: CVWidgetKnob
							)
						},
						CVWidgetMS, {
							lib[\all][k] = (
								spec: cvWidgets[k].widgetCV.spec,
								val: cvWidgets[k].widgetCV.value,
								actions: cvWidgets[k].wdgtActions.reject({ |k|
									"open Function".matchRegexp(k.values[0][0])
								}),
								wdgtClass: CVWidgetMS,
								midiOscRememberBatchConnection: cvWidgets[k].midiOscRememberBatchConnection,
								osc: ()!cvWidgets[k].msSize,
								midi: ()!cvWidgets[k].msSize,
							);
							cvWidgets[k].msSize.do({ |sl|
								// osc
								cvWidgets[k].midiOscEnv[sl].oscResponder !? {
									lib[\all][k].osc[sl].addr = cvWidgets[k].midiOscEnv[sl].oscResponder.addr;
									lib[\all][k].osc[sl].cmdName = cvWidgets[k].midiOscEnv[sl].oscResponder.cmdName;
								};
								lib[\all][k].osc[sl].msgIndex = cvWidgets[k].midiOscEnv[sl].oscMsgIndex;
								lib[\all][k].osc[sl].calibConstraints = cvWidgets[k].getOscInputConstraints(sl);
								lib[\all][k].osc[sl].oscMapping = cvWidgets[k].getOscMapping(sl);
								// midi
								lib[\all][k].midi[sl].src = cvWidgets[k].midiOscEnv[sl].midisrc;
								lib[\all][k].midi[sl].chan = cvWidgets[k].midiOscEnv[sl].midichan;
								lib[\all][k].midi[sl].num = cvWidgets[k].midiOscEnv[sl].midiRawNum;
								lib[\all][k].midi[sl].midiMode = cvWidgets[k].getMidiMode(sl);
								lib[\all][k].midi[sl].midiMean = cvWidgets[k].getMidiMean(sl);
								lib[\all][k].midi[sl].softWithin = cvWidgets[k].getSoftWithin(sl);
								lib[\all][k].midi[sl].midiResolution = cvWidgets[k].getMidiResolution(sl);
								lib[\all][k].midi[sl].ctrlButtonBank = cvWidgets[k].getCtrlButtonBank(sl);
							});
							cvWidgets[k].cvArray !? {
								lib[\all][k].isSplit = true;
							}
						}
					);
					lib[\all][k].connectS = cvWidgets[k].connectS;
					lib[\all][k].connectTF = cvWidgets[k].connectTF;
					lib[\all][k].notes = cvWidgets[k].nameField.string;
					lib[\all][k].tabLabel = widgetStates[k].tabKey;
				})
			});

			lib[\all].put(\shortcuts, (
				cvCenter: CVCenter.shortcuts,
				cvWidget: CVWidget.shortcuts,
				cvWidgetEditor: AbstractCVWidgetEditor.shortcuts,
				globalShortcuts: KeyDownActions.globalShortcuts
			));

			lib[\all].put(\snapshots, snapShots);

			if (GUI.id === \cocoa, {
				lib.writeTextArchive(*f);
			}, {
				lib.writeTextArchive(f);
			});
			lib = nil;
		};
		if (path.isNil, {
			if (GUI.id !== \qt, {
				File.saveDialog(
					prompt: "Save your current setup to a file",
					defaultName: "Setup",
					successFunc: successFunc
				)
			}, { Dialog.savePanel(successFunc) })
		}, {
			successFunc.(path);
		});
	}

	*loadSetup {
		|
			path, addToExisting=false,
			autoConnectOSC=true, oscConnectToIP=true, oscRestrictToPort=false, activateCalibration=false, resetCalibration=false,
			autoConnectMIDI=true, midiConnectSrc=false, midiConnectChannel=false, midiConnectCtrl=true,
			loadActions=true, midiSrcID, oscIPAddress, loadShortcuts=true, loadSnapshots=true,
			connectSliders, connectNumBoxes
		|
		var lib, successFunc, toBeRemoved;

		successFunc = { |f|
			if (GUI.id === \qt, {
				lib = Library.readTextArchive(*f);
			}, {
				lib = Library.readTextArchive(f);
			});
			if (this.childViews.size > 0, { childViews.keysDo(_.close) });

			{
				all !? {
					if (addToExisting === false, { this.removeAll });
				};
				lib[\all].pairsDo({ |key, v|
					if (key !== \shortcuts, {
						switch(v.wdgtClass,
							CVWidget2D, {
								#[lo, hi].do({ |hilo|
									this.prAdd(key, v[hilo].spec, v[hilo].val, v.tabLabel, hilo);
									cvWidgets[key].setMidiMode(v[hilo].midi.midiMode, hilo)
									.setMidiMean(v[hilo].midi.midiMean, hilo)
									.setSoftWithin(v[hilo].midi.softWithin, hilo)
									.setMidiResolution(v[hilo].midi.midiResolution, hilo)
									.setCtrlButtonBank(v[hilo].midi.ctrlButtonBank, hilo)
									;
									if (loadActions, {
										v[hilo].actions !? {
											v[hilo].actions.pairsDo({ |ak, av|
												this.addActionAt(key, ak, av.asArray[0][0], hilo, av.asArray[0]	[1]);
											})
										}
									});
									if (autoConnectOSC, {
										if (v[hilo].osc.notNil and:{ v[hilo].osc.cmdName.notNil }, {
											cvWidgets[key].oscConnect(
												if (oscIPAddress.isNil, {
													oscConnectToIP !? {
														if (oscConnectToIP, { v[hilo].osc.addr !? { v[hilo].osc.addr.ip }})
													}
												}, {
													oscIPAddress.asString.split($:)[0]
												}),
												if (oscIPAddress.isNil, {
													oscRestrictToPort !? {
														if (oscConnectToIP and:{ oscRestrictToPort }, {
															v[hilo].osc.addr !? { v[hilo].osc.addr.port }
														})
													}
												}, {
													oscIPAddress.asString.split($:)[1]
												}),
												v[hilo].osc.cmdName,
												v[hilo].osc.msgIndex,
												hilo
											);

											cvWidgets[key].setOscMapping(v[hilo].osc.oscMapping, hilo);
											if (activateCalibration and:{ resetCalibration }, {
												cvWidgets[key].setOscInputConstraints(Point(0.0001, 0.0001), hilo);
												cvWidgets[key].wdgtControllersAndModels[hilo].oscInputRange.model.value_(
													[Point(0.0001, 0.0001)]
												).changedKeys(cvWidgets[key].synchKeys);
											}, {
												cvWidgets[key].setOscInputConstraints(
													Point(
														v[hilo].osc.calibConstraints.lo,
														v[hilo].osc.calibConstraints.hi
													),
													hilo
												);
												cvWidgets[key].wdgtControllersAndModels[hilo].oscInputRange.model.value_(
													[v[hilo].osc.calibConstraints.lo, v[hilo].osc.calibConstraints.hi]
												).changedKeys(cvWidgets[key].synchKeys)
											});
											if (activateCalibration, { cvWidgets[key].setCalibrate(true, hilo) });
										})
									});
									if (autoConnectMIDI, {
										if (v[hilo].midi.notNil and:{ v[hilo].midi.num.notNil }, {
											try {
												cvWidgets[key].midiConnect(
													if (midiSrcID.isNil, {
														if (midiConnectSrc, { v[hilo].midi.src })
													}, {
														midiSrcID.asInt
													}),
													if (midiConnectChannel, { v[hilo].midi.chan }),
													if (midiConnectCtrl, { v[hilo].midi.num }),
													hilo
												)
											}
										})
									})
								})
							},
							CVWidgetKnob, {
								this.prAdd(key, v.spec, v.val, v.tabLabel);
								cvWidgets[key].setMidiMode(v.midi.midiMode)
									.setMidiMean(v.midi.midiMean)
									.setSoftWithin(v.midi.softWithin)
									.setMidiResolution(v.midi.midiResolution)
									.setCtrlButtonBank(v.midi.ctrlButtonBank)
								;
								if (loadActions, {
									v.actions !? {
										v.actions.pairsDo({ |ak, av|
											this.addActionAt(key, ak, av.asArray[0][0], active: av.asArray[0][1]);
										})
									}
								});
								if (autoConnectOSC, {
									v.osc.cmdName !? {
										cvWidgets[key].oscConnect(
											if (oscIPAddress.isNil, {
												oscConnectToIP !? {
													if (oscConnectToIP, { v.osc.addr !? { v.osc.addr.ip }})
												}
											}, {
												oscIPAddress.asString.split($:)[0]
											}),
											if (oscIPAddress.isNil, {
												oscRestrictToPort !? {
													if (oscConnectToIP and:{ oscRestrictToPort }, {
														v.osc.addr !? { v.osc.addr.port }
													})
												}
											}, {
												oscIPAddress.asString.split($:)[1]
											}),
											v.osc.cmdName,
											v.osc.msgIndex
										);
										cvWidgets[key].setOscMapping(v.osc.oscMapping);
										if (activateCalibration and:{ resetCalibration }, {
											cvWidgets[key].setOscInputConstraints(
												Point(0.0001, 0.0001)
											);
											cvWidgets[key].wdgtControllersAndModels.oscInputRange.model.value_(
												[Point(0.0001, 0.0001)]
											).changedKeys(cvWidgets[key].synchKeys);
										}, {
											cvWidgets[key].setOscInputConstraints(
												Point(v.osc.calibConstraints.lo, v.osc.calibConstraints.hi)
											);
											cvWidgets[key].wdgtControllersAndModels.oscInputRange.model.value_(
												[v.osc.calibConstraints.lo, v.osc.calibConstraints.hi]
											).changedKeys(cvWidgets[key].synchKeys);
										});
										if (activateCalibration, { cvWidgets[key].setCalibrate(true) });
									}
								});
								if (autoConnectMIDI, {
									v.midi.num !? {
										try {
											cvWidgets[key].midiConnect(
												if (midiSrcID.isNil, {
													if (midiConnectSrc, { v.midi.src })
												}, {
													midiSrcID.asInt
												}),
												if (midiConnectChannel, { v.midi.chan }),
												if (midiConnectCtrl, { v.midi.num }),
											)
										}
									}
								})
							},
							CVWidgetMS, {
								this.prAdd(key, v.spec, v.val, v.tabLabel);
								v.midiOscRememberBatchConnection !? {
									v.midiOscRememberBatchConnection.pairsDo({ |k, v|
										cvWidgets[key].midiOscRememberBatchConnection[k] = v;
									});
								};
								cvWidgets[key].msSize.do({ |sl|
									cvWidgets[key].setMidiMode(v.midi[sl].midiMode, sl)
										.setMidiMean(v.midi[sl].midiMean, sl)
										.setSoftWithin(v.midi[sl].softWithin, sl)
										.setMidiResolution(v.midi[sl].midiResolution, sl)
										.setCtrlButtonBank(v.midi[sl].ctrlButtonBank, sl)
									;
								});
								if (loadActions, {
									v.actions !? {
										v.actions.pairsDo({ |ak, av|
											this.addActionAt(key, ak, av.asArray[0][0], active: av.asArray[0][1]);
										})
									}
								});
								if (autoConnectOSC, {
									cvWidgets[key].msSize.do({ |sl|
										v.osc[sl].cmdName !? {
											cvWidgets[key].oscConnect(
												if (oscIPAddress.isNil, {
													oscConnectToIP !? {
														if (oscConnectToIP, { v.osc[sl].addr !? { v.osc[sl].addr.ip }})
													}
												}, {
													oscIPAddress.asString.split($:)[0]
												}),
												if (oscIPAddress.isNil, {
													oscRestrictToPort !? {
														if (oscConnectToIP and:{ oscRestrictToPort }, {
															v.osc[sl].addr !? { v.osc[sl].addr.port }
														})
													}
												}, {
													oscIPAddress.asString.split($:)[1]
												}),
												v.osc[sl].cmdName,
												v.osc[sl].msgIndex,
												sl
											);
											cvWidgets[key].setOscMapping(v.osc[sl].oscMapping, sl);
											if (activateCalibration and:{ resetCalibration }, {
												cvWidgets[key].setOscInputConstraints(
													Point(0.0001, 0.0001), sl
												);
												cvWidgets[key].wdgtControllersAndModels.slots[sl].oscInputRange.model.value_(
													[Point(0.0001, 0.0001)]
												).changedKeys(cvWidgets[key].synchKeys);
											}, {
												cvWidgets[key].setOscInputConstraints(
													Point(
														v.osc[sl].calibConstraints.lo,
														v.osc[sl].calibConstraints.hi
													),
													sl
												);
													cvWidgets[key].wdgtControllersAndModels.slots[sl].oscInputRange.model.value_(
													[v.osc[sl].calibConstraints.lo, v.osc[sl].calibConstraints.hi]
												).changedKeys(cvWidgets[key].synchKeys);
											});
											if (activateCalibration, { cvWidgets[key].setCalibrate(true, sl) });
										}
									})
								});
								if (autoConnectMIDI, {
									cvWidgets[key].msSize.do({ |sl|
										v.midi[sl].num !? {
											try {
												cvWidgets[key].midiConnect(
													if (midiSrcID.isNil, {
														if (midiConnectSrc, { v.midi[sl].src })
													}, {
														midiSrcID.asInt
													}),
													if (midiConnectChannel, { v.midi[sl].chan }),
													if (midiConnectCtrl, { v.midi[sl].num }),
													slot: sl
												)
											}
										}
									})
								});
								v.isSplit !? {
									if (v.isSplit) {
										cvWidgets[key].split;
									}
								};
							}
						);

						cvWidgets[key] !? {
							cvWidgets[key].nameField.string_(v.notes);
							if (GUI.id !== \cocoa, { cvWidgets[key].label.toolTip_(v.notes) });
							if (connectSliders.notNil, {
								if (connectSliders, { cvWidgets[key].connectGUI(true, nil)
								}, { cvWidgets[key].connectGUI(false, nil) });
							}, {
								// "v.connectS: %\n".postf(v.connectS);
								cvWidgets[key].connectGUI(v.connectS, nil)
							});
							if (connectNumBoxes.notNil, {
								if (connectNumBoxes, { cvWidgets[key].connectGUI(nil, true)
								}, { cvWidgets[key].connectGUI(nil, false) });
							}, {
								cvWidgets[key].connectGUI(nil, v.connectTF)
							})
						};

						if (CVCenterLoadDialog.window.notNil and:{ CVCenterLoadDialog.window.isClosed.not }, {
							CVCenterLoadDialog.window.close;
						})
					})
				});
				0.1.wait;
				if (loadShortcuts, {
					lib[\all][\shortcuts] !? {
						this.shortcuts_(lib[\all][\shortcuts][\cvCenter]);
						[tabs.views, prefPane].flat.do({ |view|
							KeyDownActions.setShortcuts(view, this.shortcuts);
						});
						CVWidget.shortcuts_(lib[\all][\shortcuts][\cvWidget]);
						cvWidgets.do({ |wdgt|
							wdgt.focusElements.do({ |el|
								KeyDownActions.setShortcuts(el, CVWidget.shortcuts);
							})
						});
						AbstractCVWidgetEditor.shortcuts_(lib[\all][\shortcuts][\cvWidgetEditor]);
						\KeyDownActions.asClass.globalShortcuts_(lib[\all][\shortcuts][\globalShortcuts]);
						if (Server.default.serverRunning, {
							\KeyDownActions.asClass.globalShortcutsSync;
						});
					}
				});
				0.5.wait;
				if (loadSnapshots, {
					if (addToExisting.not, {
						snapShots = ();
						this.at('select snapshot').items_(["select snapshot..."]);
					});
					lib[\all][\snapshots].pairsDo({ |k, v|
						snapShots.put(k, v);
						this.at('select snapshot').items_(this.at('select snapshot').items.add(k));
					});
				})
			}.fork(AppClock)
		};

		if (path.isNil, {
			if (GUI.id === \qt, {
				QDialog.openPanel(successFunc);
			}, {
				File.openDialog(
					prompt: "Please choose a setup",
					successFunc: successFunc
				)
			})
		}, {
			successFunc.(path);
		})
	}

	// private Methods - not to be used directly

	*prSetup { |setupDict|
		setupDict[\midiMode] !? { this.midiMode_(setupDict[\midiMode]) };
		setupDict[\midiResolution] !? { this.midiResolution_(setupDict[\midiResolution]) };
		setupDict[\midiMean] !? { this.midiMean_(setupDict[\midiMean]) };
		this.ctrlButtonBank_(setupDict[\ctrlButtonBank]);
//		setupDict[\ctrlButtonBank] !? { this.ctrlButtonBank_(setupDict[\ctrlButtonBank]) };
		setupDict[\softWithin] !? { this.softWithin_(setupDict[\softWithin]) };
		if (window.notNil and:{ window.notClosed }, {
			cvWidgets.pairsDo({ |k, wdgt|
				switch(wdgt.class,
					CVWidgetKnob, {
						this.midiMode !? { wdgt.setMidiMode(this.midiMode) };
						this.midiResolution !? { wdgt.setMidiResolution(this.midiResolution) };
						this.midiMean !? { wdgt.setMidiMean(this.midiMean) };
						this.ctrlButtonBank !? { wdgt.setCtrlButtonBank(this.ctrlButtonBank) };
						this.softWithin !? { wdgt.setSoftWithin(this.softWithin) };
					},
					CVWidget2D, {
						#[lo, hi].do({ |hilo|
							this.midiMode !? { wdgt.setMidiMode(this.midiMode, hilo) };
							this.midiResolution !? { wdgt.setMidiResolution(this.midiResolution, hilo) };
							this.midiMean !? { wdgt.setMidiMean(this.midiMean, hilo) };
							this.ctrlButtonBank !? { wdgt.setCtrlButtonBank(this.ctrlButtonBank, hilo) };
							this.softWithin !? { wdgt.setSoftWithin(this.softWithin, hilo) };
						});
					},
					CVWidgetMS, {
						wdgt.msSize.do({ |sl|
							this.midiMode !? { wdgt.setMidiMode(this.midiMode, sl) };
							this.midiResolution !? { wdgt.setMidiResolution(this.midiResolution, sl) };
							this.midiMean !? { wdgt.setMidiMean(this.midiMean, sl) };
							this.ctrlButtonBank !? { wdgt.setCtrlButtonBank(this.ctrlButtonBank, sl) };
							this.softWithin !? { wdgt.setSoftWithin(this.softWithin, sl) };
						});
					}
				);
			})
		})
	}

	*prRegroupWidgets { |tab|
		var thisNextPos, order, orderedRemoveButs;
		var widgetwidth, widgetheight=160;
		var thisTabKey;

		tab !? {
			thisTabKey = tab.label.asSymbol;
			order = cvWidgets.order;
			orderedRemoveButs = removeButs.atAll(order);
			order.do({ |k, i|
				thisNextPos ?? { thisNextPos = Point(0, 0) };
				if (cvWidgets[k].window === tab, {
					if (thisNextPos.x != 0, {
						if (thisNextPos.x+(cvWidgets[k].widgetProps.x) >= (tab.bounds.width-15), {
							thisNextPos = Point(0, thisNextPos.y
								+(cvWidgets[k].widgetProps.y)
								+(orderedRemoveButs[i].bounds.height+1)
							);
						}, {
							thisNextPos = tabProperties[thisTabKey].nextPos;
						})
					});
					tabProperties[thisTabKey].nextPos = thisNextPos + Point(cvWidgets[k].widgetProps.x+1, 0);
					cvWidgets[k].widgetXY_(thisNextPos);
					orderedRemoveButs[i].bounds_(Rect(
						thisNextPos.x,
						thisNextPos.y+widgetheight,
						orderedRemoveButs[i].bounds.width,
						orderedRemoveButs[i].bounds.height
					));
					thisNextPos = tabProperties[thisTabKey].nextPos;
				})
			})
		}
	}

	*prRegroupPrefPane {
		var children, rows;

		children = prefPane.children;
		swFlow.bounds.width_(prefPane.bounds.width);
		swFlow.reset;
		children.do({ |child|
			swFlow.place(child);
			rows = children.collect({ |child| child.bounds.top }).asSet.size;
			prefPane.bounds_(Rect(
				prefPaneBounds.left,
				window.view.bounds.height-35-(rows-1*21),
				prefPane.bounds.width,
				prefPaneBounds.height+(rows-1*21)
			));
			tabs.view.bounds_(Rect(
				tabsBounds.left,
				tabsBounds.top,
				tabs.view.bounds.width,
				window.view.bounds.height-prefPane.bounds.height
			))
		});
	}

	*prRemoveTab { |key|
		var index;

		if (window.isClosed.not and:{
			tabs.tabViews.detect({ |tab| tab.label.asSymbol == key.asSymbol }).notNil
		}, {
			index = tabs.tabViews.detect({ |tab| tab.label.asSymbol == key.asSymbol }).index;
			tabs.removeAt(index)
		});
		childViews.pairsDo({ |child, childProps|
			childProps.tabs.keysDo({ |view|
				if (view.label.asSymbol == key.asSymbol, {
					child.close;
					index = view.index;
					tabs.removeAt(index);
				});
			})
		});
		tabProperties.removeAt(key);
		widgetStates.do({ |w| if (w.tabIndex > index, { w.tabIndex = w.tabIndex-1 }) });
	}

	/* utilities */

	// key/value array way to connect CV's to a node
	// this allows a number of variants documented in the Conductor help file (see below)
	*connectToNode { |node, kvArray, environment|
		var cvcKeys = [], nodeVars, activate;

		if (node.class !== Symbol and:{ node.class !== String }, {
			nodeVars = node.getObjectVarNames(environment)
		});

		// "nodeVars: %\n".postf(nodeVars);
		forBy(1, kvArray.size - 1, 2, { |i|
			if (kvArray[i].isArray and:{ kvArray[i].isString.not }, {
				cvcKeys = cvcKeys.add(kvArray[i]);
				kvArray.put(i, kvArray[i].collect({ |key| this.at(key.asSymbol) }));
			}, {
				kvArray.put(i, this.at(kvArray[i].asSymbol));
			});
		});
		if (nodeVars.notNil and:{ nodeVars.size > 0 }, {
			nodeVars.do({ |n, i|
				if (i == 0, { activate = true }, { activate = false });
				kvArray.cvCenterBuildCVConnections(n.asString.interpret.server, n.asString.interpret.nodeID, n, cvcKeys, activate);
			})
		}, {
			if (node.class == String or:{ node.class == Symbol }, {
				kvArray.cvCenterBuildCVConnections(node.interpret.server, node.interpret.nodeID, node, cvcKeys)
			}, {
				kvArray.cvCenterBuildCVConnections(node.server, node.nodeID)
			})
		})
	}

	// not to be called directly - called internally by Synth:-cvcGui resp. NodeProxy:-cvcGui
	*finishGui { |obj, ctrlName, environment, more|
		// var interpreterVars, varNames = [], envs = [], thisSpec;
		// var pSpaces = [], proxySpace;
		var varNames, thisSpec;
		var activate = true;
		var actionName = "default";
		var wms, addActionFunc;

		// [obj, ctrlName, environment, more].postln;

		varNames = obj.getObjectVarNames(environment);
		// "varNames: %\n".postf(varNames);
		if (obj.class == Patch, { varNames = varNames.collect({ |v| v.asString++".synth" }) });

		if (more.specEnterText.notNil and:{
			more.specEnterText.interpret.asSpec.isKindOf(ControlSpec)
		}, {
			thisSpec = more.specEnterText.interpret.asSpec;
		}, {
			thisSpec = more.specSelect;
		});

//		"pSpaces: %\n".postf(pSpaces);
//		"varNames: %\n".postf(varNames);
//		"more: %\n".postf(more);

		if (more.type.notNil, {
			if (more.type === \w2d or:{ more.type === \w2dc }, {
				#[lo, hi].do({ |slot, i|
					this.prAdd(more.cName, thisSpec, more.slots[i], more.enterTab, slot);
					if (more.type == \w2d, {
						if (slot === \lo, {
							wms = "cv.value, CVCenter.at('"++more.cName++"').hi.value";
						}, {
							wms = "CVCenter.at('"++more.cName++"').lo.value, cv.value";
						})
					});
					if (varNames.size > 0, {
						varNames.do({ |v, j|
							actionName = "default"++(j+1);
							if (j == 0, { activate = true }, { activate = false });
							switch(more.type,
								\w2d, {
									this.addActionAt(more.cName, actionName, "{ |cv|"+v+"!? {"+v++".setn('"++ctrlName++"', ["++wms++"]) }}", slot, activate);
									if (obj.isKindOf(NodeProxy), {
										this.addActionAt(more.cName, actionName+"(xsetn)", "{ |cv|"+v+"!? {"+v++".xsetn('"++ctrlName++"', ["++wms++"]) }}", slot, false);
									});
								},
								\w2dc, {
									this.addActionAt(more.cName, actionName, "{ |cv|"+v+"!? {"+v++".set('"++more.controls[i]++"', cv.value) }}", slot, activate);
									if (obj.isKindOf(NodeProxy), {
										this.addActionAt(more.cName, actionName+"(xset)", "{ |cv|"+v+"!? {"+v++".xset('"++more.controls[i]++"', cv.value) }}", slot, false);
									});
								}
							)
						})
					}, {
						switch(more.type,
							\w2d, {
								this.addActionAt(more.cName, actionName, "{ |cv| Server('"++obj.server++"').sendBundle("++obj.server.latency++", ['/n_setn', "++obj.nodeID++", '"++ctrlName++"', 2, "++wms++"]) }", slot);
							},
							\w2dc, {
								this.addActionAt(more.cName, actionName, "{ |cv| Server('"++obj.server++"').sendBundle("++obj.server.latency++", ['/n_setn', "++obj.nodeID++", '"++more.controls[i]++"', 1, cv.value]) }", slot);
							}
						)
					})
				})
			}, {
				if (more.type === \wms, {
					// "varNames: %, more: %\n".postf(varNames, more);
					this.prAdd(more.cName, thisSpec!more.slots.size, more.slots, more.enterTab);
					if (varNames.size > 0, {
						varNames.do({ |v, j|
							actionName = "default"++(j+1);
							if (j == 0, { activate = true }, {activate = false });
							this.addActionAt(more.cName, actionName, "{ |cv|"+v+"!? {"+v++".setn('"++ctrlName++"', cv.value) }}", active: activate);
							if (obj.isKindOf(NodeProxy), {
								this.addActionAt(more.cName, actionName+"(xsetn)", "{ |cv|"+v+"!? {"+v++".xsetn('"++ctrlName++"', cv.value) }}", active: false);
							});
						})
					}, {
						this.addActionAt(more.cName, \default, "{ |cv| Server('"++obj.server++"').sendBundle("++obj.server.latency++", ['/n_setn', "++obj.nodeID++", '"++ctrlName++"', "++more.slots.size++", cv.value]) }");
					})
				})
			})
		}, {
			addActionFunc = {
				if (varNames.size > 0, {
					varNames.do({ |v, j|
						// "varNames: %\n".postf(v);
						actionName = "default"++(j+1);
						if (j == 0, { activate = true }, { activate = false });
						if (more.controls.notNil and:{ more.controls.size > 1 }, {
							this.addActionAt(more.cName, actionName, "{ |cv|"+v+"!? {"+v++".set('"++ctrlName++"', cv.value) }}", active: activate);
							if (obj.isKindOf(NodeProxy), {
								this.addActionAt(more.cName, actionName+"(xset)", "{ |cv|"+v+"!? {"+v++".xset('"++ctrlName++"', cv.value) }}", active: false);
							});
						}, {
							wms = [];
							more.slots.size.do({ |i|
								if (this.at((more.cName.asString++(i+1)).asSymbol) === this.at((more.cName.asString++(i+1)).asSymbol), {
									wms = wms.add("cv.value");
								}, {
									wms = wms.add("CVCenter.at('"++more.cName.asString++(i+1)++"').value")
								})
							});
							this.addActionAt(more.cName.asString++(j+1), actionName, "{ |cv|"+v+"!? {"+v++".setn('"++ctrlName++"', ["++(wms.join(", "))++"]) }}", active: activate);
							if (obj.isKindOf(NodeProxy), {
								this.addActionAt(more.cName.asString++(j+1), actionName+"(xsetn)", "{ |cv|"+v+"!? {"+v++".xsetn('"++ctrlName++"', ["++(wms.join(", "))++"]) }}", active: false);
							});
						})
					}, {
						this.addActionAt(more.cName, \default, "{ |cv| Server('"++obj.server++"').sendBundle("++obj.server.latency++", ['/n_setn', "++obj.nodeID++", '"++ctrlName++"', 1, cv.value]) }");
					})
				})
			};

			case
				{ more.slots.size == 1 } {
					this.prAdd(more.cName, thisSpec, more.slots[0], more.enterTab);
					varNames.postln;
					if (varNames.size > 0, {
						varNames.do({ |v, j|
							if (j == 0, { activate = true }, { activate = false });
							this.addActionAt(more.cName, actionName++(j+1), "{ |cv|"+v+"!? {"+v++".set('"++ctrlName++"', cv.value) }}", active: activate);
							if (obj.isKindOf(NodeProxy), {
								this.addActionAt(more.cName, actionName++(j+1)+"(xset)", "{ |cv|"+v+"!? {"+v++".xset('"++ctrlName++"', cv.value) }}", active: false);
							})
						})
					}, {
						this.addActionAt(more.cName, actionName, "{ |cv| Server('"++obj.server++"').sendBundle("++obj.server.latency++", ['/n_setn', "++obj.nodeID++", '"++ctrlName++"', 1, cv.value]) }");
					})
				}
				{ more.slots.size == 2 } {
					// more.slots.postln;
					[\Lo, \Hi].do({ |sl, k|
						this.prAdd(more.cName++sl, thisSpec, more.slots[k], more.enterTab);
					});
					addActionFunc.value;
				}
				{ more.slots.size > 2 } {
					more.slots.size.do({ |sl|
						this.prAdd(more.cName++sl, thisSpec, more.slots[sl], more.enterTab);
					});
					addActionFunc.value;
				}
			;
		});

		^obj;
	}

}
