CVCenterShortcuts {
	classvar <shortcuts;

	*initClass {
		var scFunc;

		shortcuts = ();

		scFunc =
		"// next tab\n" ++
		{
			CVCenter.tabs.focus(
				(CVCenter.tabs.activeTab.index+1).wrap(0, CVCenter.tabs.tabViews.size-1)
			)
		}.asCompileString;
		shortcuts.put(
			'arrow right',
			(func: scFunc, keyCode: KeyDownActions.keyCodes['arrow right'])
		);
		scFunc =
		"// previous tab\n" ++
		{
			CVCenter.tabs.focus(
				(CVCenter.tabs.activeTab.index-1).wrap(0, CVCenter.tabs.tabViews.size-1)
			)
		}.asCompileString;
		shortcuts.put(
			'arrow left',
			(func: scFunc, keyCode: KeyDownActions.keyCodes['arrow left'])
		);
		scFunc =
		"// select first widget\n" ++
		{
			var labels = CVCenter.cvWidgets.order;
			CVCenter.cvWidgets[labels.first].parent.front.focus;
			CVCenter.cvWidgets[labels.first].label.focus;
		}.asCompileString;
		shortcuts.put(
			'alt + arrow right',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes['arrow right'],
				modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
				modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
			)
		);
		scFunc =
		"// select last widget\n" ++
		{
			var labels = CVCenter.cvWidgets.order;
			CVCenter.cvWidgets[labels.last].parent.front.focus;
			CVCenter.cvWidgets[labels.last].label.focus;
		}.asCompileString;
		shortcuts.put(
			'alt + arrow left',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes['arrow left'],
				modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
				modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
			)
		);
		scFunc =
		"// OSCCommands gui\n" ++
		{ OSCCommands.front }.asCompileString;
		shortcuts.put(
			'alt + c',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$c],
				modifierQt: KeyDownActions.modifiersQt[\alt],
				modifierCocoa: KeyDownActions.modifiersCocoa[\alt]
			)
		);
		scFunc =
		"// CVCenterControllersMonitor OSC\n" ++
		{ CVCenterControllersMonitor(1) }.asCompileString;
		shortcuts.put(
			\o,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$o])
		);
		scFunc =
		"// set temporary shortcuts\n" ++
		{ CVCenterShortcutsEditor.dialog }.asCompileString;
		shortcuts.put(
			'alt + s',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$s],
				modifierQt: KeyDownActions.modifiersQt[\alt],
				modifierCocoa: KeyDownActions.modifiersCocoa[\alt]
			)
		);
		scFunc =
		"// CVCenterControllersMonitor MIDI\n" ++
		{ CVCenterControllersMonitor(0) }.asCompileString;
		shortcuts.put(
			\m,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$m])
		);
		scFunc =
		"// close all CVWidget(MS)Editors\n" ++
		AbstractCVWidgetEditor.allEditors.pairsDo({ |k, v|
			switch(CVCenter.cvWidgets[k].class,
				CVWidgetKnob, {
					v.editor !? { v.editor.close }
				},
				CVWidget2D, { #[lo, hi].do({ |sl|
					v[sl] !? { v[sl].editor !? { v[sl].editor.close }}
				}) },
				CVWidgetMS, {
					CVCenter.cvWidgets[k].msSize.do({ |i|
						v[i] !? { v[i].editor !? { v[i].editor.close }}
					})
				}
			);
			CVCenter.cvWidgets[k] !? {
				v.editor !? { v.editor.close }
			}
		}).asCompileString;
		shortcuts.put(
			'shift + esc',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[\esc],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
		scFunc =
		"// History GUI: start History and open History window\n" ++
		{ if (History.started === false) { History.start };
			if (CVCenter.scv.historyWin.isNil or:{
				CVCenter.scv.historyWin.isClosed
			}) {
				CVCenter.scv.historyGui = History.makeWin(
					Window.screenBounds.width-300@Window.screenBounds.height
				);
				CVCenter.scv.historyWin = CVCenter.scv.historyGui.parent;
			};
			if (CVCenter.scv.historyWin.notNil and:{
				CVCenter.scv.historyWin.isClosed.not
		}) { CVCenter.scv.historyWin.front }}.asCompileString;
		shortcuts.put(
			\h,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$h])
		);
		scFunc =
		"// NdefMixer\n" ++
		{ if (CVCenter.scv.nDefWin.isNil or:{ CVCenter.scv.nDefWin.isClosed }) {
			CVCenter.scv.nDefGui = NdefMixer(Server.default);
			CVCenter.scv.nDefWin = CVCenter.scv.nDefGui.parent
		};
		if (CVCenter.scv.nDefWin.notNil and:{
			CVCenter.scv.nDefWin.isClosed.not
		}) {
			CVCenter.scv.nDefWin.front
		}}.asCompileString;
		shortcuts.put(
			\n,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$n])
		);
		scFunc =
		"// save setup\n" ++
		{ CVCenter.saveSetup }.asCompileString;
		shortcuts.put(
			\s,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$s])
		);
		scFunc =
		"// load setup\n" ++
		{ CVCenterLoadDialog.new }.asCompileString;
		shortcuts.put(
			\l,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$l])
		);
		scFunc =
		"// open the preferences dialog\n" ++
		{ CVCenterPreferences.dialog }.asCompileString;
		shortcuts.put(
			\p,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$p])
		);
		scFunc =
		"// PdefAllGui\n" ++
		{ if (CVCenter.scv.pDefWin.isNil or:{ CVCenter.scv.pDefWin.isClosed }) {
			CVCenter.scv.pDefGui = PdefAllGui();
			CVCenter.scv.pDefWin = CVCenter.scv.pDefGui.parent
		};
		if (CVCenter.scv.pDefWin.notNil and:{
			CVCenter.scv.pDefWin.isClosed.not
		}) {
			CVCenter.scv.pDefWin.front
		}}.asCompileString;
		shortcuts.put(
			'shift + p',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$p],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
		scFunc =
		"// PdefnAllGui\n" ++
		{ if (CVCenter.scv.pDefnWin.isNil or:{ CVCenter.scv.pDefnWin.isClosed }) {
			CVCenter.scv.pDefnGui = PdefnAllGui();
			CVCenter.scv.pDefnWin = CVCenter.scv.pDefnGui.parent;
		};
		if (CVCenter.scv.pDefnWin.notNil and:{
			CVCenter.scv.pDefnWin.isClosed.not
		}) {
			CVCenter.scv.pDefnWin.front
		}}.asCompileString;
		shortcuts.put(
			'alt + p',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$p],
				modifierQt: KeyDownActions.modifiersQt[\alt],
				modifierCocoa: KeyDownActions.modifiersCocoa[\alt]
			)
		);
		scFunc =
		"// TdefAllGui\n" ++
		{ if (CVCenter.scv.tDefWin.isNil or:{ CVCenter.scv.tDefWin.isClosed }) {
			CVCenter.scv.tDefGui = TdefAllGui();
			CVCenter.scv.tDefWin = CVCenter.scv.tDefGui.parent
		};
		if (CVCenter.scv.tDefWin.notNil and:{
			CVCenter.scv.tDefWin.isClosed.not
		}) {
			CVCenter.scv.tDefWin.front
		}}.asCompileString;
		shortcuts.put(
			\t,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$t])
		);
		scFunc =
		"// AllGui\n" ++
		{ if (\AllGui.asClass.notNil) {
			if (CVCenter.scv.allWin.isNil or:{ CVCenter.scv.allWin.isClosed }) {
				CVCenter.scv.allGui = \AllGui.asClass.new;
				CVCenter.scv.allWin = CVCenter.scv.allGui.parent;
			};
			if (CVCenter.scv.allWin.notNil and:{
				CVCenter.scv.allWin.isClosed.not
			}) { CVCenter.scv.allWin.front };
		}}.asCompileString;
		shortcuts.put(
			\a,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$a])
		);
		scFunc =
		"// MasterEQ\n" ++
		{ if (\MasterEQ.asClass.notNil) {
			if (CVCenter.scv.eqWin.isNil or:{ CVCenter.scv.eqWin.isClosed }){
				CVCenter.scv.eqGui = \MasterEQ.asClass.new(
					Server.default.options.firstPrivateBus, Server.default
				);
				CVCenter.scv.eqWin = CVCenter.scv.eqGui.window;
			};
			if (CVCenter.scv.eqWin.notNil and:{
				CVCenter.scv.eqWin.isClosed.not
			}) { CVCenter.scv.eqWin.front };
		}}.asCompileString;
		shortcuts.put(
			\e,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$e])
		);
		(0..9).do({ |i|
			scFunc =
			"// focus tab "++i++"\n" ++
			{ CVCenter.tabs.tabViews["++i++"] !? { CVCenter.tabs.focus("++i++") }}.asCompileString;
			shortcuts.put(
				i.asSymbol,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[i.asString[0]])
			);
		});
		scFunc =
		"// end History and open in new Document (Cocoa-IDE only)\n" ++
		{
			History.end;
			if (Platform.ideName == "scapp" or:{
				(Platform.ideName == "scqt").and(Main.versionAtLeast(3, 7))
			}) { History.document };
			if (CVCenter.scv.historyWin.notNil and:{
				CVCenter.scv.historyWin.isClosed.not
			}) { CVCenter.scv.historyWin.close }
		}.asCompileString;
		shortcuts.put(
			'shift + h',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$h],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
		scFunc =
		"// detach the currently focused tab from the main window\n" ++
		{ CVCenter.tabs.activeTab.detachTab }.asCompileString;
		shortcuts.put(
			\d,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$d])
		);
		scFunc =
		"// activate OSC calibration for all widgets\n" ++
		{ CVCenter.cvWidgets.do({ |wdgt|
			switch(wdgt.class,
				CVWidget2D, {
					#[lo, hi].do({ |sl| wdgt.setCalibrate(true, sl) });
				},
				CVWidgetMS, {
					wdgt.msSize.do({ |i| wdgt.setCalibrate(true, i) });
				},
				{ wdgt.setCalibrate(true) }
			)
		}) }.asCompileString;
		shortcuts.put(
			'shift + c',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$c],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
		scFunc =
		"// deactivate OSC calibration for all widgets\n" ++
		{ CVCenter.cvWidgets.do({ |wdgt|
			switch(wdgt.class,
				CVWidget2D, {
					#[lo, hi].do({ |sl| wdgt.setCalibrate(false, sl) });
				},
				CVWidgetMS, {
					wdgt.msSize.do({ |i| wdgt.setCalibrate(false, i) });
				},
				{ wdgt.setCalibrate(false) }
			)
		}) }.asCompileString;
		shortcuts.put(
			'alt + shift + c',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$c],
				modifierQt: KeyDownActions.modifiersQt['alt + shift'],
				modifierCocoa: KeyDownActions.modifiersCocoa['alt + shift']
			)
		);
		scFunc =
		"// reset and restart OSC calibration for all widgets\n" ++
		{ CVCenter.cvWidgets.do({ |wdgt|
			switch(wdgt.class,
				CVWidget2D, {
					#[lo, hi].do({ |sl|
						wdgt.setOscInputConstraints(Point(0.0001, 0.0001), sl).setCalibrate(true, sl);
					})
				},
				CVWidgetMS, {
					wdgt.msSize.do({ |i|
						wdgt.setOscInputConstraints(Point(0.0001, 0.0001), i).setCalibrate(true, i);
					})
				},
				{ wdgt.setOscInputConstraints(Point(0.0001, 0.0001)).setCalibrate(true) }
			)
		}) }.asCompileString;
		shortcuts.put(
			'shift + r',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$r],
				modifierQt: KeyDownActions.modifiersQt['shift'],
				modifierCocoa: KeyDownActions.modifiersCocoa['shift']
			)
		);
		scFunc =
		"// connect/disconnect textfields in all widgets\n" ++
		{ CVCenter.cvWidgets.do({ |wdgt|
			wdgt.connectGUI(nil, wdgt.connectTF.not)
		})}.asCompileString;
		shortcuts.put(
			'alt + shift + v',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$v],
				modifierQt: KeyDownActions.modifiersQt['alt + shift'],
				modifierCocoa: KeyDownActions.modifiersCocoa['alt + shift']
			)
		);
		scFunc =
		"// connect/disconnect sliders in all widgets\n" ++
		{ CVCenter.cvWidgets.do({ |wdgt|
			wdgt.connectGUI(wdgt.connectS.not, nil)
		})}.asCompileString;
		shortcuts.put(
			'alt + shift + b',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$b],
				modifierQt: KeyDownActions.modifiersQt['alt + shift'],
				modifierCocoa: KeyDownActions.modifiersCocoa['alt + shift']
			)
		);

	}
}