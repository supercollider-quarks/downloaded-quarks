CVWidgetEditorShortcuts {
	classvar <shortcuts;

	*initClass {
		var scFunc;

		shortcuts = ();

		scFunc =
		"// focus 'specs' tab\n" ++
		{ |view|
			AbstractCVWidgetEditor.allEditors.do({ |ed|
				case
				{ ed.keys.includes(\hi) or:{ ed.keys.includes(\lo) }} {
					#[lo, hi].do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(0) }
						}
					})
				}
				{ ed.keys.select(_.isNumber).size == ed.keys.size } {}
				{
					if(ed.editor.notNil and:{
						ed.editor.isClosed.not and:{
							view == ed.tabs.view
						}
					}) { ed.editor.tabs.focus(0) }
				}
				;
			})
		}.asCompileString;
		this.shortcuts.put(
			\s,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$s])
		);
		scFunc =
		"// focus 'midi' tab\n" ++
		{ |view|
			AbstractCVWidgetEditor.allEditors.do({ |ed|
				case
				{ ed.keys.includes(\hi) or:{ ed.keys.includes(\lo) }} {
					#[lo, hi].do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(1) }
						}
					})
				}
				{ ed.keys.select(_.isNumber).size == ed.keys.size } {
					ed.keys.do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(0) }
						}
					})
				}
				{
					if(ed.editor.notNil and:{
						ed.editor.isClosed.not and:{
							view == ed.tabs.view
						}
					}) { ed.editor.tabs.focus(1) }
				}
				;
			})
		}.asCompileString;
		this.shortcuts.put(
			\m,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$m])
		);
		scFunc =
		"// focus 'osc' tab\n" ++
		{ |view|
			AbstractCVWidgetEditor.allEditors.do({ |ed|
				case
				{ ed.keys.includes(\hi) or:{ ed.keys.includes(\lo) }} {
					#[lo, hi].do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(2) }
						}
					})
				}
				{ ed.keys.select(_.isNumber).size == ed.keys.size } {
					ed.keys.do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(1) }
						}
					})
				}
				{
					if(ed.editor.notNil and:{
						ed.editor.isClosed.not and:{
							view == ed.tabs.view
						}
					}) { ed.editor.tabs.focus(2) }
				}
				;
			})
		}.asCompileString;
		this.shortcuts.put(
			\o,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$o])
		);
		scFunc =
		"// focus 'actions' tab\n" ++
		{ |view|
			AbstractCVWidgetEditor.allEditors.do({ |ed|
				case
				{ ed.keys.includes(\hi) or:{ ed.keys.includes(\lo) }} {
					#[lo, hi].do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.tabs.focus(3) }
						}
					})
				}
				{ ed.keys.select(_.isNumber).size == ed.keys.size } {}
				{
					if(ed.editor.notNil and:{
						ed.editor.isClosed.not and:{
							view == ed.tabs.view
						}
					}) { ed.editor.tabs.focus(3) }
				}
				;
			})
		}.asCompileString;
		this.shortcuts.put(
			\a,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$a])
		);
		scFunc =
		"// close the editor\n" ++
		{ |view|
			AbstractCVWidgetEditor.allEditors.do({ |ed|
				case
				{ ed.keys.includes(\hi) or:{ ed.keys.includes(\lo) }} {
					#[lo, hi].do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.close(k) }
						}
					})
				}
				{ ed.keys.select(_.isNumber).size == ed.keys.size } {
					ed.keys.do({ |k|
						ed[k] !? {
							if(ed[k].editor.notNil and:{
								ed[k].editor.isClosed.not and:{
									view == ed[k].tabs.view
								}
							}) { ed[k].editor.close(k) }
						}
					})
				}
				{
					if(ed.editor.notNil and:{
						ed.editor.isClosed.not and:{
							view == ed.tabs.view
						}
					}) { ed.editor.close }
				}
				;
			})
		}.asCompileString;
		this.shortcuts.put(
			\esc,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[\esc])
		);
		scFunc =
		"// collect OSC-commands resp. open the collector's GUI" ++
		{ OSCCommands.front }.asCompileString;
		this.shortcuts.put(
			\c,
			(func: scFunc, keyCode: KeyDownActions.keyCodes[$c])
		)
	}
}