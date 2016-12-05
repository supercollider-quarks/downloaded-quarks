// to do
// child views ignore input ... View
// MicKeySetSynth, key modifiers/ restrict to alphaNum?
// info: add description of Func
// remake win with func doen't work

MicKeyMouse {

	classvar <win, utf8, timer, keyUp;
	classvar mx,my,wx,wy,wmod,<dxBus,<dyBus;
	classvar info, prompt;

	*initClass {
		// catch everything
		utf8 = 2 ** 8;
		timer = Array.fill(utf8, { 0 });
		keyUp = Array.fill(utf8, { true });
		// catch mouse scroll delta
		dxBus = Bus.control(Server.default);
		dyBus = Bus.control(Server.default);
	}
}

MicKeyWindow : MicKeyMouse {

	classvar font, fontSize;

	*initClass {
		fontSize = 10;
		font = Font("Helvetica", fontSize);
		win = Window("mickey", Rect(0,0,700,700*0.6), false, true);
	}
	*makeWindow {
		// win = Window(bounds: 256@256, border: false)
		// .alwaysOnTop_(true)
		// .alpha_(0.7)
		win.view
		.acceptsMouseOver_(true)
		.mouseOverAction_({ |v,x,y|
			mx = x;
			my = y;
			info[1].string_(" mouse x: " ++ mx);
			info[2].string_(" mouse y: " ++ my);
		})
		.mouseWheelAction_({ |v,x,y,m,dx,dy|
			wx = x;
			wy = y;
			wmod = m;
			dxBus.set(dx);
			dyBus.set(dy);
			info[3].string_(" mouse wheel dx: " ++ dx);
			info[4].string_(" mouse wheel dy: " ++ dy);
		})
		.keyDownAction_({ |v,c,m,u,k|
			if(keyUp[u], {
				timer[u] = Date.getDate.rawSeconds;
				// key is down
				keyUp[u] = false;
			});
		})
		.keyUpAction_({ |v,c,m,u,k|
			timer[u] = Date.getDate.rawSeconds - timer[u];
			keyUp[u] = true;
			c.switch(
				Char.nl,    { info[0].string_(" key: NL up duration " ++ timer[u] ++ " sec."); },
				Char.tab,   { info[0].string_(" key: TAB up duration " ++ timer[u] ++ " sec."); },
				Char.space, { info[0].string_(" key: SPACE up duration " ++ timer[u] ++ " sec."); },
				{
					c.isNil.not.if({
						if(c.isPrint,
							{ info[0].string_(" key: " ++ c ++ " up duration " ++ timer[u] ++ " sec."); },
							{ info[0].string_(" key: isn't printable."); }
						);
					});
				}
			);
		});
		// add info
		prompt = StaticText(win, Rect(fontSize, fontSize, win.bounds.width-(2*fontSize), 50))
		.acceptsMouseOver_(true).mouseOverAction_({false}).mouseWheelAction_({false})
		.stringColor_(Color.white)
		.background_(Color.green)
		.font_(Font("Helvetica Neue", 50))
		.string_("print.");

		info = Array.fill(5, { |i|
			StaticText(win, Rect(fontSize, fontSize*i+50+fontSize, fontSize*28, fontSize))
			.acceptsMouseOver_(true).mouseOverAction_({false}).mouseWheelAction_({false})
			.stringColor_(Color.white)
			.background_(Color.black)
			.font_(font);
		});
		// remake win
		win.onClose_({ this.initClass });
	}
	*inform { |string|
		{ prompt.string_(string) }.defer;
	}
	*front {
		if(win.visible.not || win.isClosed, { this.makeWindow; win.front });
	}
	*width { ^win.bounds.width }
	*height { ^win.bounds.height }
}

MicKeyFunc : MicKeyMouse {

	classvar <dict;

	*initClass {
		// key -> func
		dict = ();
	}
	*new { |key, func|
		var f = dict[key];
		if(f.isNil, {
			super.new.initMicKeyFunc(key, func);
			^func
		}, {
			if(func.notNil, {
				this.free(key);
				super.new.initMicKeyFunc(key, func);
				^func;
			});
		});
		^f
	}
	*clear {
		dict.keys.do({ |key| win.view.keyUpAction.removeFunc(dict[key]) });
		dict = ();
	}
	*free { |key|
		win.view.keyUpAction.removeFunc(dict[key]);
		dict[key] = nil;
	}
	initMicKeyFunc { |key, func|
		dict.add(key -> this.wrapFunc(key, func));
		win.view.keyUpAction = win.view.keyUpAction.addFunc(dict[key]);
	}
	wrapFunc { |key, func|
		^{ |v,c,m,u,k| if(c.asSymbol == key, { func.(timer[u], mx, my, dxBus, dyBus) }) }
	}
}

MicKeySynth : MicKeyFunc {

	wrapFunc { |key, func|
		var defName = (\mkySynth_ ++ key).asSymbol;
		// asSynthDef
		SynthDef(defName, func).send;
		^{ |v,c,m,u,k|
			if(c.asSymbol == key, {
				Synth.grain(defName, [
					timer: timer[u],
					mx: mx,
					my: my,
					dxBus: dxBus.asMap,
					dyBus: dyBus.asMap
				]);
			})
		}
	}
}