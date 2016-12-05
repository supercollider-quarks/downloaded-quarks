CVWidgetShortcuts {
	classvar <shortcuts;

	*initClass {
		var scFunc;

		shortcuts = ();

		scFunc =
		"// focus previous widget (alphabetically ordered)\n" ++
		{ |view|
			block { |break|
				CVCenter.cvWidgets.order.do({ |name, i|
					if(CVCenter.cvWidgets[name].focusElements.includes(view)) {
						break.value(
							CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i-1)].parent.front.focus;
							CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i-1)].label.focus;
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			'alt + arrow left',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes['arrow left'],
				modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
				modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
			)
		);
		scFunc =
		"// focus next widget (alphabetically ordered)\n" ++
		{ |view|
			block { |break|
				CVCenter.cvWidgets.order.do({ |name, i|
					if(CVCenter.cvWidgets[name].focusElements.includes(view)) {
						break.value(
							CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i+1)].parent.front.focus;
							CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i+1)].label.focus;
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			'alt + arrow right',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes['arrow right'],
				modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
				modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
			)
		);
		scFunc =
		"// open a CVWidget(MS)Editor and focus its Spec tab\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
										CVCenter.cvWidgets[key].editor.msEditor.isClosed }
									) {
										CVWidgetMSEditor(CVCenter.cvWidgets[key], 0)
									} {
										CVCenter.cvWidgets[key].editor.msEditor.front(0)
									}
								},
								CVWidget2D, { #[lo, hi].do({ |slot|
									if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
										CVCenter.cvWidgets[key].editor[slot].isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 0, slot)
									} {
										CVCenter.cvWidgets[key].editor[slot].front(0)
									}
								})},
								{
									if(CVCenter.cvWidgets[key].editor.isNil or:{
										CVCenter.cvWidgets[key].editor.isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 0)
									} {
										CVCenter.cvWidgets[key].editor.front(0)
									}
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\s,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$s])
		);
		scFunc =
		"// open a CVWidget(MS)Editor and focus its MIDI tab\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
										CVCenter.cvWidgets[key].editor.msEditor.isClosed }
									) {
										CVWidgetMSEditor(CVCenter.cvWidgets[key], 1)
									} {
										CVCenter.cvWidgets[key].editor.msEditor.front(1)
									}
								},
								CVWidget2D, { #[lo, hi].do({ |slot|
									if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
										CVCenter.cvWidgets[key].editor[slot].isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 1, slot)
									} {
										CVCenter.cvWidgets[key].editor[slot].front(1)
									}
								})},
								{
									if(CVCenter.cvWidgets[key].editor.isNil or:{
										CVCenter.cvWidgets[key].editor.isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 1)
									} {
										CVCenter.cvWidgets[key].editor.front(1)
									}
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\m,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$m])
		);
		scFunc =
		"// open a CVWidget(MS)Editor and focus its OSC tab\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
										CVCenter.cvWidgets[key].editor.msEditor.isClosed }
									) {
										CVWidgetMSEditor(CVCenter.cvWidgets[key], 2)
									} {
										CVCenter.cvWidgets[key].editor.msEditor.front(2)
									}
								},
								CVWidget2D, { #[lo, hi].do({ |slot|
									if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
										CVCenter.cvWidgets[key].editor[slot].isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 2, slot)
									} {
										CVCenter.cvWidgets[key].editor[slot].front(2)
									}
								})},
								{
									if(CVCenter.cvWidgets[key].editor.isNil or:{
										CVCenter.cvWidgets[key].editor.isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 2)
									} {
										CVCenter.cvWidgets[key].editor.front(2)
									}
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\o,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$o])
		);
		scFunc =
		"// open a CVWidget(MS)Editor and focus its Actions tab\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
										CVCenter.cvWidgets[key].editor.msEditor.isClosed }
									) {
										CVWidgetMSEditor(CVCenter.cvWidgets[key], 3)
									} {
										CVCenter.cvWidgets[key].editor.msEditor.front(3)
									}
								},
								CVWidget2D, { #[lo, hi].do({ |slot|
									if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
										CVCenter.cvWidgets[key].editor[slot].isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 3, slot)
									} {
										CVCenter.cvWidgets[key].editor[slot].front(3)
									}
								})},
								{
									if(CVCenter.cvWidgets[key].editor.isNil or:{
										CVCenter.cvWidgets[key].editor.isClosed }
									) {
										CVWidgetEditor(CVCenter.cvWidgets[key], 3)
									} {
										CVCenter.cvWidgets[key].editor.front(3)
									}
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\a,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$a])
		);
		scFunc =
		"// set focus to the view that contains the widget\n" ++
		{ CVCenter.prefPane.focus }.asCompileString;
		this.shortcuts.put(
			\esc,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[\esc])
		);
		scFunc =
		"// start or stop OSC calibration\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									if(
										CVCenter.cvWidgets[key].msSize.collect(
											CVCenter.cvWidgets[key].getCalibrate(_)
										).select(_ == true).size == CVCenter.cvWidgets[key].msSize
									) {
										CVCenter.cvWidgets[key].msSize.do(
											CVCenter.cvWidgets[key].setCalibrate(false, _)
										)
									} {
										CVCenter.cvWidgets[key].msSize.do(
											CVCenter.cvWidgets[key].setCalibrate(true, _)
										)
									}
								},
								CVWidget2D, {
									if(
										#[lo, hi].collect(
											CVCenter.cvWidgets[key].getCalibrate(_)
										).select(_ == true).size == 2
									) {
										#[lo, hi].do(CVCenter.cvWidgets[key].setCalibrate(false, _))
									} {
										#[lo, hi].do(CVCenter.cvWidgets[key].setCalibrate(true, _))
									}
								},
								{
									if(CVCenter.cvWidgets[key].getCalibrate == true) {
										CVCenter.cvWidgets[key].setCalibrate(false)
									} {
										CVCenter.cvWidgets[key].setCalibrate(true)
									}
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\c,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$c])
		);
		scFunc =
		"// reset current OSC calibration constraints and start OSC calibration\n" ++
		{ |view|
			block { |break|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						break.value(
							switch(CVCenter.cvWidgets[key].class,
								CVWidgetMS, {
									CVCenter.cvWidgets[key].msSize.do(
										CVCenter.cvWidgets[key].setOscInputConstraints(Point(0.0001), _);
										CVCenter.cvWidgets[key].setCalibrate(true, _)
									)
								},
								CVWidget2D, {
									#[lo, hi].do(
										CVCenter.cvWidgets[key].setOscInputConstraints(Point(0.0001), _);
										CVCenter.cvWidgets[key].setCalibrate(true, _)
									)
								},
								{
									CVCenter.cvWidgets[key]
									.setOscInputConstraints(Point(0.0001))
									.setCalibrate(true)
								}
							)
						)
					}
				})
			};
			true;
		}.asCompileString;
		this.shortcuts.put(
			\r,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$r])
		);
		scFunc =
		"// connect or disconnect sliders\n" ++
		{ |view|
			CVCenter.all.keys.do({ |key|
				if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
					CVCenter.cvWidgets[key].connectGUI(CVCenter.cvWidgets[key].connectS.not, nil);
				}
			});
			true;
		}.asCompileString;
		this.shortcuts.put(
			'shift + b',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$b],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
		scFunc =
		"// connect or disconnect textfields\n" ++
		{ |view|
			CVCenter.all.keys.do({ |key|
				if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
					CVCenter.cvWidgets[key].connectGUI(nil, CVCenter.cvWidgets[key].connectTF.not);
				}
			});
			true;
		}.asCompileString;
		this.shortcuts.put(
			'shift + v',
			(
				func: scFunc,
				keyCode: KeyDownActions.keyCodes[$v],
				modifierQt: KeyDownActions.modifiersQt[\shift],
				modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
			)
		);
	}
}