VGTagGui {
	var <parent, <bounds, <title, <event, <choices, <keys;
	var <>action;
	var <comp;
	var keys, allViews, updateActions;
	
	*new { |parent, bounds, title, event, choices, keys|
		^super.newCopyArgs(parent, bounds, title, event, choices, keys).init
	}
	
	set { |...keyVals|
		event.putPairs(keyVals);
		this.update;
	}
	
	update {
		allViews.keysValuesDo {|key, view|
			updateActions[view.class].value(event[key], view).postln;
		} 
	}
	
	init {
		var height = 17, labelWidth = bounds.width -2, viewWidth = bounds.width -2;
		var height2 = 130;
		var font = Font("Helvetica", 11);
		var colPal = [[233,209,077,255], //curry
						[186,154,053,255], //brown curry
						[173,127,052,255], //mid brown
						[190,055,049,255], //red
						[112,036,014,255] //darker brown
						] / 255;

						
		colPal = colPal.collect{|rgba| Color.fromArray(rgba) };
		
		keys = keys ?? { event.keys.asArray.sort };
		allViews = ();
		
		updateActions = ()
			.put(TextView.implClass, { |val, view| view.string = val.asCompileString })

			.put(PopUpMenu.implClass, { |val, view| view.value = 
						view.items.indexOfEqual(this.stringForPopUp(val)) ? 0 })
			.put(EZSlider, { |val, view| view.value = val });
		
		
		comp = CompositeView(parent, bounds);
			 //.background_(colPal[1]);
		comp.decorator = FlowLayout(comp.bounds);
		
		Button(comp, Rect(0,0, viewWidth, 20))
			.font_(font)
			.states_([[title, Color.black, colPal[0]]])
			.action_({ event.postcs; this.openDoc; })
			.resize_(2);
		comp.decorator.nextLine;
		comp.decorator.shift(0, -4);
		
		keys.do { |key|
			var possibleValues = choices[key], slider;
			
			allViews[key] = StaticText(comp, Rect(0,0, labelWidth, height))
				.background_(Color.grey(0.9))
				.font_(font)
				.align_(\center)
				.string_(" " ++ key);
			if(key == \synthDef) {allViews[key].background_(colPal[2]).font_(Font("Helvetica", 13))};
			if(key == \out) {allViews[key].background_(colPal[2]).font_(Font("Helvetica", 13))};
			if(key == \level) {allViews[key].background_(colPal[2]).font_(Font("Helvetica", 13))};
			if(key == \pan) {allViews[key].background_(colPal[2]).font_(Font("Helvetica", 13)) };
			
			comp.decorator.nextLine;
			comp.decorator.shift(0, -6);

				// function is a constraint, what is allowed:
			if(possibleValues.isNil or: possibleValues.isKindOf(Function)) { 
				allViews[key] = VGTextView(comp, Rect(0,0, viewWidth, height), 
					{ |res| 
						event[key] = res; 
						action.(this, key, res); 
					}, possibleValues
					)
					.font_(font)
					.resize_(2)				
			} {
				if(possibleValues.isKindOf(Symbol)) {
					
					if(possibleValues == \pan){
						slider = EZSlider.new(comp, viewWidth @ (height * 2.5), nil, possibleValues.asSpec,
							labelWidth: 0, unitWidth: 0, numberWidth: 12, layout: \vert)
							.font_(Font("Helvetica", 15))
							.action_({|v| 
								event[key] = v.value;
								action.(this, key, v.value);
							});
						slider.numberView.align_(\center);
						slider.setColors(	colPal[2], //stringBackground
										Color.black, //stringColor
										colPal[2], //sliderBackground
										colPal[2], //numBackground
										Color.black, //numStringColor
										Color.black, //numNormalColor
										Color.black, //numTypingColor
										colPal[4], //knobColor
										colPal[2] //background
										);
						allViews[key] =  slider;
					};	
					
					if(possibleValues == \level){
						//thin senseless field just for the look
						comp.decorator.shift(0,1);
						StaticText(comp, Rect(0,0, viewWidth, height2 + 7))
							.background_(colPal[2])
							.string_(" ");

						comp.decorator.shift(viewWidth * 0.2 - viewWidth -4, 3);
						slider = EZSlider.new(comp, (viewWidth * 0.6) @ height2, nil, possibleValues.asSpec,
							labelWidth: 0, unitWidth: 0, numberWidth: 12, layout: \vert)
							
							.font_(Font("Helvetica", 15))
							.action_({|v| 
								event[key] = v.value;
								action.(this, key, v.value);
							});
						slider.numberView.align_(\center);
						slider.setColors(	colPal[2], //stringBackground
										Color.black, //stringColor
										colPal[2], //sliderBackground
										colPal[2], //numBackground
										Color.black, //numStringColor
										Color.black, //numNormalColor
										Color.black, //numTypingColor
										colPal[4], //knobColor
										colPal[2] //background
										);
						allViews[key] =  slider;
						comp.decorator.shift(0, 48);
					};	
					
					
				} {
					allViews[key] = PopUpMenu(comp, Rect(0,0, viewWidth, height))
							.items_(possibleValues.collect(this.stringForPopUp(_)))
							.resize_(2)
							.font_(font)
							.action_({|v| 
								event[key] = possibleValues[v.value] ? event[key];
								action.value(this, key, event[key])
							})
							//new line !! dhml 2012 // now mixer shows correct inst and out !!
							//update after loading a preset is still broken
							.value_(possibleValues.indexOf(event[key]));
					
							//.setColors( 	colPal[2], //stringBackground
//											Color.black, //stringColor
//											colPal[2], //menuStringBackground
//											Color.black, //menuStringColor
//											colPal[2] //background
//											);
				}
			};
			comp.decorator.nextLine;
			comp.decorator.shift(0, -4);

			
		};
		this.update;
	
	}
	
	// workaround for sc view bug:
	stringForPopUp { |obj|
		^obj.asString.replace("(", "").replace(")", "");
	}
	
	openDoc {
		var w;
		w = Window("edit" + title, parent.bounds.resizeTo(400, 200));
		TextView(w, Rect(10, 10, 380, 180)).string_(event.asCompileString)
			.enterInterpretsSelection_(false)
			.resize_(5)
			.autohidesScrollers_(true)
			.keyDownAction_({|v, char, mod, keycode| 
				var res;
				if(mod == 256 and: { keycode == 3 }) { 
				
				res = VGTextView.localInterpret(v.string);
				res !? { 
					event.putAll(res);
					event.keysValuesDo { |key, value|
						action.value(this, key, value)
					};
					this.update;
				}
			};
			});
			
		w.front;
	}
	
	


}

VGTextView {
	
	
	*new { |parent, bounds, action, constraint|
		var prevString, res;
		res = TextView(parent, bounds)
						.enterInterpretsSelection_(false)
						.usesTabToFocusNextView_(true)
						.font_(Font("Helvetica", 12))
						.keyDownAction_({Ê|v, char, mod, keycode| 
								var res;
								
								if(mod == 256 and: { [3, 13].includes(keycode) }) { 
									res = VGTextView.localInterpret(v.string);
									if(res.notNil 
										and: { constraint.isNil 
											or: { constraint.value(res) == true }}
									) {
										action.value(res);
										prevString = v.string;
									} {
										v.background = Color.red;
										fork {Ê0.2.wait; defer { 
											v.background = Color.white;
											v.string = prevString; 
											}
										};
									};
									fork { 0.01.wait; 
										defer { 
											v.string = v.string.replace("\n", "") 
										}
									};
									
								};
								
				});
				fork {Ê0.2.wait; defer {Ê prevString = res.string } };
			^res;
	}
	
	// does not help yet - why?
	
	*localInterpret { |string| // block interpreter variables here.
			var a, b, c, d, e, f, g, h, i, j;
			var k, l, m, n, o, p, q, r, s, t;
			var u, v, w, x, y, z;
			 // block environment here.
			^Environment.new.use {
				string.compile.value;
			};
	}

}



