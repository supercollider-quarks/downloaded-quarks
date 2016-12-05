MantaGUI {
	var parent, view, extent, spacing;
	
	var <> selectColor, 
		<> strokeColor, 
		<> navigateColor, 
		<> textColor,
		< padExtent;
	var <> strokeWidth;
	var <>manta, <>selected, <>navigateIdx;
	var fac, rects;
	
	*new{|parent, extent, manta|
		^super.new.initGUI(parent, extent, manta)
	}

	initGUI {|argParent, argExtent, argManta|
		
		manta = argManta;
		
		parent = argParent;
		extent = argExtent ? Rect(0,0,335, 225);
		spacing = 3;
		padExtent = 20;
		
		// initialize
		selectColor = Color(1.0, 0.58106449884176, 0.001, 1);
		strokeColor = Color.gray(0.5);
		navigateColor = Color(0.37360832637219, 0.76881720430108, 0.12400279410424, 1);
		textColor = Color.gray(0.4);
		strokeWidth = 1;
		
		//vals = {|i| i/48}!48;
		selected = Set[];
		navigateIdx = 0;

		fac = 0.8660254037844388; // (0.5/tan(pi/6)).asCompileString;
		
		this.createRects;
		
		parent.isNil.if{
			parent = Window.new(\Manta, Rect(100, 100, 345, 235)).decorate;
			parent.front;
		};

		this.createView(parent, extent);
	}		
	refresh {
		view.refresh;
	}

	padExtent_ {|val|
		padExtent = val;
		this.createRects;	
	}

	createRects {	
		// create active areas for each pad
		rects = 6.collect{|j|
			8.collect{|i|
				Rect.aboutPoint(
						((i*((2*fac*padExtent)+spacing)) + ((j%2) * (fac*padExtent + (0.5*spacing)))) @
						((5-j)* ((1.5 * padExtent) + (1/fac * spacing))), 
						padExtent, padExtent
				) + [10 + (fac*padExtent), 10 + padExtent, 0, 0]
			}
		}.flatten;
	}


	createView {|parent, extent|
		view = UserView(parent, extent);
		view.background_(Color.clear);
		
		view.drawFunc = {|uview|
			var vals = manta.values.pad;
			//navigateIdx.postln;
			rects.do{|rect, i|
				this.drawHexagon(rect, padExtent, 
					(navigateIdx == (i+1)).if({
						(selected.includes(i+1)).if({
							navigateColor.blend(selectColor, 0.5)
						}, {
							navigateColor
						})
					}, {
						(selected.includes(i+1)).if({
							selectColor
						}, {
							strokeColor
						})
					}),
					Color.white.blend(Color.yellow, vals[i]), 
					i+1
				);
			};
		};
		//////////////////////////////////
		view.mouseDownAction={|me, x, y, mod, butNum, numClicks| 
			var idx;

			idx = this.clickToIdx(x, y);
			navigateIdx = 0;

			(mod == 131330).if({ // shift
				idx.notNil.if({ // shift-click, none selected -> nothing
					// shift-click, one clicked -> add to Set
					selected.add(idx)
				})
			}, {	// click -> replace Set with single element
				selected.clear;
				idx.notNil.if({
					selected.add(idx)
				});
			});
			//mod.postln;
			me.refresh;
		};
		//////////////////////////////////
		view.keyDownAction={|me, char, mods, unicode,  args|
			(mods == 524576).if{
				(unicode == 8364).if{ // alt-e
					"edit".inform
				};
				(unicode == 231).if{ // alt-c
					selected.clear;
					"clear".inform
				};
				(unicode == 160).if{ // alt-space
					selected.includes(navigateIdx).if({
						"add".postln;
						selected.remove(navigateIdx);	
					}, {
						"remove".postln;
						selected.add(navigateIdx);
					});
					"select".inform
				}	
			};
			((mods == 10486016).or{mods == 11010336}).if{
				(unicode == 63235).if{ // -> arrow
					(navigateIdx == 0).if({
						navigateIdx = 1;
					}, {
						navigateIdx = min(navigateIdx+1, ((((navigateIdx-1) div: 8) + 1)*8));
					})
					// "right".inform
				};
				(unicode == 63234).if{ // <- arrow
					(navigateIdx == 0).if({
						navigateIdx = 1;
					}, {
						navigateIdx = max(navigateIdx-1, ((((navigateIdx-1) div: 8))*8 + 1));
					})
					// "left".inform
				};
				(unicode == 63232).if{ // ^ arrow
					(navigateIdx == 0).if({
						navigateIdx = 1;
					}, {
						navigateIdx = min(navigateIdx+8, 48);
					})
					// "up".inform
				};
				(unicode == 63233).if{ // v arrow
					(navigateIdx == 0).if({
						navigateIdx = 1;
					}, {
						navigateIdx = max(navigateIdx-8, 1);
					})
					// "down".inform
				};
			};
			me.refresh;
			//[mods, unicode].postln
		};
	}

	drawHexagon {|rect, extent = 30, strokeColor, fillColor, name = 0|
		var pos = rect.center.asArray;

		//Pen.addRect(rect);
		//Pen.stroke;
		Pen.use{
			Pen.width = strokeWidth;
			Pen.translate(*(pos.asArray));
			Pen.strokeColor = strokeColor ?? {Color.gray(0.5)};
			Pen.fillColor = fillColor ?? {Color.yellow};
			Pen.beginPath;
			Pen.moveTo((          0)@(     extent));
			Pen.lineTo((fac*extent )@( 0.5*extent));
			Pen.lineTo((fac*extent )@(-0.5*extent));
			Pen.lineTo((          0)@(-1  *extent));
			Pen.lineTo((fac.neg*extent)@(-0.5*extent));
			Pen.lineTo((fac.neg*extent)@(0.5*extent));
			Pen.lineTo((          0)@(     extent));
			Pen.fillStroke;
			Pen.color = textColor;
			Pen.font = Font( "Helvetica", 12 );
			Pen.stringCenteredIn(name.asString, Rect(extent.neg, extent.neg, 2*extent, 2*extent));
			//Pen.strokeRect(Rect(extent.neg, extent.neg, 2*extent, 2*extent));
		}
	}
	
	clickToIdx {|x, y|
		var out;
		out = rects.detectIndex{|rect| rect.containsPoint(x@y)};
		out.notNil.if{
			^(out+1)
		};
		^out
	}

}