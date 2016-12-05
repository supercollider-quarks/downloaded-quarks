VGMixer {

	*new {
			var w = Window("Mixer", Rect(200, 150, 1160, 440), false).front;
			var specs, settingsGuis, selectGuis, makeButtonA, makeButtonB, makeButtonC, makeButtonD, volSpec;
			var butfont = Font("Helvetica", 10);
			var colPal = [[233,209,077,255], //curry
						[186,154,053,255], //brown curry
						[173,127,052,255], //mid brown
						[190,055,049,255], //red
						[112,036,014,255] //darker brown
						] / 255;
						
			colPal = colPal.collect{|rgba| Color.fromArray(rgba) };
			
			w.view.background = colPal[1];
			w.view.decorator = FlowLayout(w.view.bounds, 4@4, 0@0);
			Spec.add(\level, [0, 4, \amp]);
			volSpec = [-90, 6, \db].asSpec;
			makeButtonA = { |action|
				Button(w, Rect(0, 0, 15, 18))
					.states_([["A", Color.grey(0.2), colPal[1]], ["A", Color.black, Color(0.6, 0.9, 0.4)]])
					.value_(0)
					.action_(action);
			};
			makeButtonB = { |action|
				Button(w, Rect(0, 0, 15, 18))
					.states_([["B", Color.grey(0.2), colPal[1]], ["B", Color.black, Color(0.6, 0.9, 0.4)]])
					.value_(1)
					.action_(action);
			};
			makeButtonC = { |action|
				Button(w, Rect(0, 0, 15, 18))
					.states_([["C", Color.grey(0.2), colPal[1]], ["C", Color.black, Color(0.6, 0.9, 0.4)]])
					.value_(0)
					.action_(action);
			};
			makeButtonD = { |action|
				Button(w, Rect(0, 0, 15, 18))
					.states_([["D", Color.grey(0.2), colPal[1]], ["D", Color.black, Color(0.6, 0.9, 0.4)]])
					.value_(0)
					.action_(action);
			};
			
			w.view.decorator.gap = 8@8;
			w.view.decorator.nextLine;
			
			
			// mute button
			Button(w, Rect(0, 0, 50, 18))
					.states_([
						["Mute", Color.black], 
						["Muted", Color.black, Color(0.9, 0.3, 0.1)]
					])
					.action_({|v| 
						if(v.value == 0) { VGSound.server.unmute }Ê{ VGSound.server.mute } 
					});
			
			EZSlider.new(w, 500 @ 18, "volume", volSpec, labelWidth: 50, numberWidth: 40)
					.value_(0.0)
					.action_({|v| VGSound.server.volume = v.value });
						
			Button(w, Rect(0, 0, 50, 18))
					.states_([
						["Save", Color.black]
					])
					.action_({
						Dialog.savePanel { |path|
							var file = File.new(path, "w");
							protect {
								file.write(VGSound.playbackSettings.asCompileString)
							} {
								file.close;
							}
						}
					
					});
			Button(w, Rect(0, 0, 50, 18))
					.states_([
						["Load", Color.black]
					])
					.action_({
						Dialog.getPaths { |paths|
							var res = paths[0].load;
							if(res.notNil) {
								VGSound.playbackSettings.keysValuesDo { |key, val|
									var new = res[key]; // if a setting exists in the file
									new !? { val.putAll(new) }; // use it.
								};
								settingsGuis.do(_.update);
							} {Ê
								"VGMixer - File load error".warn; 
							};
								
						}
					
					});
					
			w.view.decorator.nextLine;
			// w.view.decorator.shift(0, 10);
			w.view.decorator.gap = 0@0;
			
			//makeButtonA.value({ |v|
//						selectGuis.do { |gui| gui.value = v.value } 
//					});
			
			specs = (
						level:\level, 
						pan: \pan,
						out:(0..120),
						synthDef: VGSound.synthEngines.keys.asArray.sort
					);
			
			//VGTagGui(w, Rect(0,0, 300, 300), 
//					"-- MASTER - set all selected channels --", (
//						level:1.0, 
//						pan: 0.0, 
//						out:0, 
//						synthDef: \vgSampDyn,
//						solo: false,
//						mute: false
//					), 
//					specs.copy, [\synthDef, \level, \pan, \out]
//				)
//				.action_({ |gui, key, val|
//					settingsGuis.do { |gui, i|
//						if(selectGuis[i].value == 1 
//							// don't change Kendhang synthDef
//							and: { gui.title != 'Kendhang' or: { key != 'synthDef' }})
//						{
//							gui.event[key] = val;
//							gui.update;
//						}
//						
//					}
//				});
			
			//w.view.decorator.nextLine;

			w.view.decorator.shift(0, -30);
			
			VGSound.playbackSettings.keys.asArray.sort.do {|name|
				var setting = VGSound.playbackSettings[name]; 
				
				w.view.decorator.shift(0, 266);
				w.view.decorator.shift(0, 4);

				w.view.decorator.shift(2, 0);
				selectGuis = selectGuis.add(makeButtonA.value);
				w.view.decorator.shift(2, 0);
				selectGuis = selectGuis.add(makeButtonB.value);
				w.view.decorator.shift(2, 0);
				selectGuis = selectGuis.add(makeButtonC.value);
				w.view.decorator.shift(2, 0);
				selectGuis = selectGuis.add(makeButtonD.value);
				//w.view.decorator.shift(0, -60);

				w.view.decorator.shift(-68, 20);
				
					Button(w, Rect(0, 0, 34, 22))
						.states_([
							["M", Color.grey(0.2), Color.clear], 
							["M", Color.black, Color(0.6, 0.9, 0.4)], 
						])
						.font_(butfont)
						.value_(0)
						.action_({|v|
							setting[\mute] = v.value.booleanValue;
						});
				w.view.decorator.shift(2, 0);
				
					Button(w, Rect(0, 0, 34, 22))
						.states_([
							["S", Color.grey(0.2), Color.clear], 
							["S", Color.black, Color(0.9, 0.3, 0.1)]
						])
						.font_(butfont)
						.value_(0)
						.action_({|v|
							setting[\solo] = v.value.booleanValue;
						});
						
				w.view.decorator.shift(0, -266);

				w.view.decorator.shift(-74, 12);
				
				settingsGuis = settingsGuis.add(
					VGTagGui(w, Rect(0,0, 72, 320), 
						name, setting, specs.copy, [\synthDef, \level, \pan, \out]
					)
				);
				w.view.decorator.shift(4, -36);

				};
				
			w.view.decorator.nextLine;
			
			//makeButtonA.value({ |v|
//						selectGuis.do { |gui| gui.value = v.value } 
//					});

				
			}

}