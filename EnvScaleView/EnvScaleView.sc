EnvScaleView {
	var <view,<background,<font,<drawFunc,<horzGridDist,<maxHorzGridDist,<minHorzGridDist,numVertGridLines,<gridBackgroundColor,<gridColor,<gridWidth,<showHorzAxis,<showVertAxis,<breakPointSize,<curvePointRadius,<domainMode,<minRange,<maxRange,<unitMode,<scaleResponsiveness,<env,envData,envView,rangeView,topSettingsView,bottomSettingsView,numGridLinesPerUnit,vhorzGridDist,selGridLineCoord,loopStartNode,loopEndNode,timeIncr,timeStep,uI,breakPointCoords,curvePointCoords,<>selectedBreakPoint,breakPointTime,envGrid,envGridHeight,<action;
	classvar unitStep;

	*initClass {
		Class.initClassTree(Array);
		Class.initClassTree(IdentityDictionary);
		// times for tempo are based on 1/4 note lasting 1/2 sec, i.e. 120 quarter notes per minute
		unitStep = IdentityDictionary.new.add(\time -> [
			100.0,50.0,20.0,10.0,5.0,2.0,1.0,0.5,0.2,0.1,0.05,0.02,0.01,0.005
		]).add(\tempo -> [
			IdentityDictionary.new.add(\measure -> [64,1]).add(\time -> 128.0),IdentityDictionary.new.add(\measure -> [16,1]).add(\time -> 32.0),IdentityDictionary.new.add(\measure -> [8,1]).add(\time -> 16.0),IdentityDictionary.new.add(\measure -> [4,1]).add(\time -> 5.0),IdentityDictionary.new.add(\measure -> [4,1]).add(\time -> 4.0),IdentityDictionary.new.add(\measure -> [1,1]).add(\time -> 2.0),IdentityDictionary.new.add(\measure -> [1,2]).add(\time -> 1.0),IdentityDictionary.new.add(\measure -> [1,4]).add(\time -> 0.5),IdentityDictionary.new.add(\measure -> [1,8]).add(\time -> 0.25),IdentityDictionary.new.add(\measure -> [1,16]).add(\time -> 0.125),IdentityDictionary.new.add(\measure -> [1,32]).add(\time -> 0.0625),IdentityDictionary.new.add(\measure -> [1,64]).add(\time -> 0.03125),IdentityDictionary.new.add(\measure -> [1,128]).add(\time -> 0.015625),IdentityDictionary.new.add(\measure -> [1,256]).add(\time -> 0.0078125)
		])
	}

	*new { arg parent,bounds,env,action;
		bounds = bounds ? (parent.notNil.if { parent.view.bounds } { nil } ? Rect(100,300,620,300));
		env = env ? Env([0,1,1,0],[0,0.5,0],[0,0,0],nil,nil);
		^super.new.init(parent,bounds,env,action)
	}

	init { arg parent,bounds,argEnv,action;
		var prevPoint,brPtModeView,breakPointMode,ctlKeyDown = false,remNumGridLines,scaleCount = 0,scaleRespCount = 0,onBreakPoint,onCurvePoint,dbreakPointXCoords,initBreakPointTime,dinitBreakPointTime,prevPoint2,brPtNumbView,brPtCurrNumbView,brPtAbsTView,brPtRelTView,brPtLevelView,crPtSlopeView,loopEndNodeView,loopStartNodeView,distInit;
		this.selectedBreakPoint = 1;

		// initialize grid vars
		this.gridWidth_(0.125,false);
		this.gridColor_(Color.grey,false);
		this.gridBackgroundColor_(Color(218/255,232/255,238/255,1),false);
		this.horzGridDist_(20,false);
		this.maxHorzGridDist_(horzGridDist*1.8,false);
		this.minHorzGridDist_(horzGridDist*0.5,false);

		// initialize axes vars
		this.showHorzAxis_(true,false);
		this.showVertAxis_(true,false);

		// initialize break point and curve point vars
		this.breakPointSize_(7,false);
		this.curvePointRadius_(2.5,false);

		// initialize other vars
		this.font_(Font("Courier",10),false);
		this.unitMode_(\time,false);
		this.domainMode_(\unipolar,false);
		this.minRange_(0,false);
		this.maxRange_(1,false);
		this.scaleResponsiveness_(3);

		uI = 8;
		numGridLinesPerUnit = 8;
		numVertGridLines = 20;
		unitMode = \time;
		timeStep = unitStep[\time][uI];
		timeIncr = timeStep/numGridLinesPerUnit;
		selGridLineCoord = ((0.4/timeIncr)*horzGridDist).asInteger@0.4;
		remNumGridLines = 0;
		vhorzGridDist = horzGridDist;
		this.env_(argEnv,false);
		breakPointMode = \slide;
		loopStartNode = 1;
		loopEndNode = 2;

		view = View(parent,bounds).resize_(5).background_(Color.white);

		envView = View(parent,bounds).onResize_({ |me|
			// when the user resizes the envelope view, makes sure that all break and curve points get scaled proportionally in the vertical direction
			envGridHeight = envGrid.bounds.height;
			this.prUpdateAllBreakPointCoordsY;
			this.prUpdateAllCurvePointCoordsY;
			me.refresh
		}).layout_(
			VLayout(
				4,
				envGrid = UserView(parent,bounds).background_(gridBackgroundColor).drawFunc_({ |me|
					var pos,lineResolution = 50,vertGridDist;

					// draw horizontal lines (fixed)
					Pen.width = gridWidth;
					Pen.strokeColor = gridColor;
					vertGridDist = me.bounds.height/numVertGridLines;
					(me.bounds.height/vertGridDist).ceil do: { |i|
						var y = i*vertGridDist;
						Pen.moveTo(0@y);
						Pen.lineTo(me.bounds.width@y)
					};
					Pen.stroke;

					Pen.font_(font);
					Pen.fillColor = gridColor;
					Pen.strokeColor = gridColor;

					// draw vertical lines and units (avoid drawing lines and units outside bounds of the view)
					(selGridLineCoord.x >= 0 and: { selGridLineCoord.x <= me.bounds.width }).if {
						// selGridLineCoord.x is in the visible part of the view
						this.prDrawVertGridLinesRight(selGridLineCoord.copy,me.bounds,remNumGridLines);
						this.prDrawVertGridLinesLeft(selGridLineCoord.copy - (vhorzGridDist@timeIncr),me.bounds,(remNumGridLines - 1).wrap(0,numGridLinesPerUnit - 1));
					} { var rem,pos,i;
						rem = remNumGridLines;
						pos = selGridLineCoord.copy;
						(selGridLineCoord.x < 0).if {
							// selGridLineCoord.x is to the left of the visible part of the view
							i = ((maxHorzGridDist.neg - pos.x)/vhorzGridDist).round(1).asInteger;
							pos.x = pos.x + (i*vhorzGridDist);
							pos.y = pos.y + (i*timeIncr);
							rem = (i + rem) % numGridLinesPerUnit;
							this.prDrawVertGridLinesRight(pos,me.bounds,rem);
						} {
							// selGridLineCoord.x is to the right of the visible part of the view
							i = ((pos.x - me.bounds.width - maxHorzGridDist)/vhorzGridDist).round(1).asInteger;
							pos.x = pos.x - (i*vhorzGridDist);
							pos.y = pos.y - (i*timeIncr);
							rem = (numGridLinesPerUnit - ((i - rem) % numGridLinesPerUnit)).wrap(0,numGridLinesPerUnit - 1);
							this.prDrawVertGridLinesLeft(pos,me.bounds,rem);
						}
					};

					Pen.stroke;

					// first draw line segments
					Pen.strokeColor = Color.grey(0.5); Pen.fillColor = Color.grey(1,0.5); Pen.width = 1;
					Pen.moveTo(breakPointCoords[0].x@(me.bounds.height + 1));
					(1..breakPointCoords.lastIndex) do: { |i|
						lineResolution do: { |j|
							var k = j/(lineResolution - 1);
							Pen.lineTo(
								(breakPointCoords[i - 1].x + (k*(breakPointCoords[i].x - breakPointCoords[i - 1].x)))@(breakPointCoords[i - 1].y + (env.curves[i - 1].asWarp.map(k)*(breakPointCoords[i].y - breakPointCoords[i - 1].y)))
							)
						}
					};
					Pen.lineTo(breakPointCoords.last.x@(me.bounds.height + 1));
					Pen.lineTo(breakPointCoords.first.x@(me.bounds.height + 1));
					Pen.fillStroke;

					// then draw breakpoints, curvepoints (if in visible part of the view)
					Pen.strokeColor = Color.red;
					breakPointCoords do: { |brPtCoord,i|
						(i == loopStartNode or: { i == loopEndNode }).if {
							Pen.width = 0.25;
							Pen.moveTo(brPtCoord.x@0);
							Pen.lineTo(brPtCoord.x@me.bounds.height);
							(i == loopEndNode).if {
								Pen.moveTo(brPtCoord);
								Pen.lineTo((breakPointCoords[loopStartNode].x - 2.5)@brPtCoord.y)
							};
							Pen.stroke
						};

						Pen.width = 0.5;
						Pen.fillColor = (selectedBreakPoint == i
							or: { selectedBreakPoint == 0 and: { i == breakPointCoords.lastIndex } }
							or: { selectedBreakPoint == breakPointCoords.lastIndex and: { i == 0 } }
						).if { Color.red } { Color.white };

						(brPtCoord.x >= maxHorzGridDist.neg and: { brPtCoord.x <= (me.bounds.width + maxHorzGridDist) }).if {
							Pen.addRect(Rect(brPtCoord.x - (breakPointSize/2),brPtCoord.y - (breakPointSize/2),breakPointSize,breakPointSize));
							(i > 0).if { Pen.addArc(curvePointCoords[i - 1],curvePointRadius,0,2pi) }
						};

						(i > 0 and: {
							curvePointCoords[i - 1].x >= maxHorzGridDist.neg and: { curvePointCoords[i - 1].x <= (me.bounds.width + maxHorzGridDist) }
						}).if {
							Pen.addArc(curvePointCoords[i - 1],curvePointRadius,0,2pi)
						};
						Pen.fillStroke
					}

				}).keyDownAction_({ |me,char,mod,key,uni|
					ctlKeyDown = mod.isCtrl
				}).keyUpAction_({ |me,char,mod,key,uni|
					ctlKeyDown = false
				}).mouseDownAction_({ |me,x,y,mod|
					var n,lim = breakPointSize*1.5;

					// calculate new selGridLineCoord
					(x != selGridLineCoord.x).if { selGridLineCoord = this.prFindNearestGridCoord(x) };
					// calculate new remNumGridLines (i.e. nr. of vertical grid lines to the left of a unit)
					remNumGridLines = this.prCalcRemNumGridLines;

					// check if position of mouse click is that of a break or curve point
					onBreakPoint = false;
					block { |break|
						breakPointCoords do: { |bPtCoord,i|
							((bPtCoord.x - x).abs <= lim and: { (bPtCoord.y - y).abs <= lim }).if {
								selectedBreakPoint = i;
								breakPointTime = env.times[0..i - 1].sum;
								onBreakPoint = true;
								break.value
							}
						}
					};
					onCurvePoint = false;
					block { |break|
						curvePointCoords do: { |cPtCoord,i|
							((cPtCoord.x - x).abs <= lim and: { (cPtCoord.y - y).abs <= lim }).if {
								selectedBreakPoint = i + 1;
								breakPointTime = env.times[0..i].sum;
								onCurvePoint = true;
								break.value
							}
						}
					};

					(breakPointMode == \slide and: { onBreakPoint } and: { selectedBreakPoint > 0 }).if {
						/*
						* calculate difference between breakpoints starting from selected breakpoint,
						* so breakpoints to the right of the selected breakpoint can be shifted horizontally
						* relative to the position of itself
						*/
						dbreakPointXCoords = breakPointCoords[selectedBreakPoint..breakPointCoords.lastIndex].performUnaryOp(\x).differentiate;
						dbreakPointXCoords.removeAt(0);
						initBreakPointTime = env.times[0..selectedBreakPoint - 1].sum;
						dinitBreakPointTime = env.times[selectedBreakPoint - 1]
					};

					ctlKeyDown.if {
						onBreakPoint.if {
							// delete break point except very first and don't delete any break points if there are only four left
							(selectedBreakPoint != 0 and: { breakPointCoords.size > 4 }).if { var tmp;

								// remove break point and associated curve point
								breakPointCoords.removeAt(selectedBreakPoint);
								curvePointCoords.removeAt(selectedBreakPoint - 1);
								tmp = env.levels.copy;
								tmp.removeAt(selectedBreakPoint);
								env.levels = tmp;
								tmp = env.curves.copy;
								tmp.removeAt(selectedBreakPoint - 1);
								env.curves = tmp;
								(selectedBreakPoint - 1 == env.levels.lastIndex).if {
									tmp = env.times.copy;
									tmp.removeAt(selectedBreakPoint - 1);
									env.times = tmp;
									selectedBreakPoint = env.levels.lastIndex;
								} {
									tmp = env.times.removeAt(selectedBreakPoint - 1);
									env.setTime(selectedBreakPoint - 1,env.times[selectedBreakPoint - 1] + tmp);
									this.prCalcXCurvePoint(selectedBreakPoint - 1);
									this.prCalcYCurvePoint(selectedBreakPoint - 1)
								};

								/*
								* delete break point distance of previous selected breakpoint to new selected break point in chained mode
								* and when not the last break point
								*/
								(breakPointMode == \slide and: { selectedBreakPoint < breakPointCoords.lastIndex }).if {
									dbreakPointXCoords.removeAt(0)
								};

								// shift loop nodes according to position of new break point
								(selectedBreakPoint == 1 and: { loopStartNode == 1 }).not.if {
									(selectedBreakPoint <= loopStartNode).if {
										loopStartNode = loopStartNode - 1;
										(loopStartNodeView.value == 1).if { env.loopNode = loopStartNode }
									}
								};
								(loopEndNode - loopStartNode > 1).if {
									(selectedBreakPoint <= loopEndNode).if {
										loopEndNode = loopEndNode - 1;
										(loopEndNodeView.value == 1).if { env.releaseNode = loopEndNode }
									}
								};

								// update break point number view in bottomSettingsView
								brPtNumbView.string_("/" ++ breakPointCoords.size.asString);
								breakPointTime = env.times[0..selectedBreakPoint - 1].sum
							}
						} { var insertInd,breakPointCoordX;
							#breakPointCoordX,breakPointTime = switch(unitMode,
								\time, { (x@this.prCalcTimeFromCoordX(x)).asArray },
								\tempo, { this.prFindNearestGridCoord(x).asArray }
							);

							// insert break point and associated curve point and select it
							insertInd = breakPointCoords.performUnaryOp(\x).indexOfGreaterThan(breakPointCoordX) ? breakPointCoords.size;
							env.levels = env.levels.insert(insertInd,y.linlin(0,me.bounds.height,maxRange,minRange));
							env.times = (insertInd == 1).if {
								env.times.insert(0,breakPointTime)
							} {
								env.times.insert(insertInd - 1,breakPointTime - env.times[0..insertInd - 2].sum)
							};
							(insertInd < env.times.size).if {
								env.setTime(insertInd,env.times[insertInd] - env.times[insertInd - 1])
							};
							breakPointCoords = breakPointCoords.insert(insertInd,breakPointCoordX@y);
							curvePointCoords = curvePointCoords.insert(insertInd - 1,(breakPointCoords[insertInd] + breakPointCoords[insertInd - 1])/2);
							env.curves = env.curves.insert(insertInd - 1,0);
							(insertInd < breakPointCoords.lastIndex).if {
								this.prCalcXCurvePoint(insertInd);
								this.prCalcYCurvePoint(insertInd)
							};

							// if inserted break point is the last, update break and curve point data for first break point
							(insertInd == env.levels.lastIndex).if {
								env.setLevel(0,env.levels[insertInd]);
								breakPointCoords[0].y = breakPointCoords[insertInd].y;
								this.prCalcYCurvePoint(0)
							};

							// if in chained mode recalculate break point distance
							dbreakPointXCoords = breakPointCoords[insertInd..breakPointCoords.lastIndex].performUnaryOp(\x).differentiate;
							dbreakPointXCoords.removeAt(0);

							// shift loop nodes according to position of new break point
							(insertInd <= loopStartNode).if {
								loopStartNode = loopStartNode + 1;
								(loopStartNodeView.value == 1).if { env.loopNode = loopStartNode }
							};
							(insertInd <= loopEndNode).if {
								loopEndNode = loopEndNode + 1;
								(loopEndNodeView.value == 1).if { env.releaseNode = loopEndNode }
							};

							selectedBreakPoint = insertInd;

							// update break point number view in bottomSettingsView
							brPtNumbView.string_("/" ++ breakPointCoords.size.asString)
						}
					};

					// distance from mouse position to selected break point position
					onBreakPoint.if { distInit = x@y - breakPointCoords[selectedBreakPoint] };

					brPtCurrNumbView.string_((selectedBreakPoint + 1).asString);
					brPtAbsTView.string_(this.prMakeStr(breakPointTime,3));
					brPtRelTView.string_(
						(selectedBreakPoint > 0).if {
							this.prMakeStr(env.times[selectedBreakPoint - 1],3)
						} {
							"0.0"
						}
					);
					brPtLevelView.string_(this.prMakeStr(env.levels[selectedBreakPoint],3));
					crPtSlopeView.string_(
						(selectedBreakPoint > 0).if {
							env.curves[selectedBreakPoint - 1]
						} {
							0
						}
					);
					prevPoint = x@y;
					this.action.(env);
					me.refresh
				}).mouseMoveAction_({ |me,x,y,mod|
					var dx = (x - prevPoint.x).clip(-10,10),dy = y - prevPoint.y,vleftNumGridLines,currZeroPos,breakPointLevel,breakPointCoordX,breakPointCoordY,prevBreakPointTime,nextBreakPointTime;

					onBreakPoint.if {
						breakPointCoordY = y - distInit.y;
						breakPointLevel = breakPointCoordY.linlin(0,me.bounds.height,maxRange,minRange);

						(selectedBreakPoint == 0).if {
							// first break point is only allowed to move up and down
							env.setLevel(0,breakPointLevel);
							breakPointCoords[0].y = breakPointCoordY.clip(0,me.bounds.height);
							env.setLevel(env.levels.lastIndex,env.levels[0]);
							breakPointCoords[breakPointCoords.lastIndex].y = breakPointCoords[0].y;

							// adjust first and last curve points
							this.prCalcYCurvePoint(0);
							this.prCalcYCurvePoint(curvePointCoords.lastIndex);
						} {
							// calculate absolute times of selected break point and the break point preceding it
							prevBreakPointTime = env.times[0..selectedBreakPoint - 2].sum;
							#breakPointCoordX,breakPointTime = switch(unitMode,
								\time, { ((x - distInit.x)@this.prCalcTimeFromCoordX(x - distInit.x)).asArray },
								\tempo, { this.prFindNearestGridCoord(x).asArray }
							);

							/*
							* if selected break point is the last, allow it to move up, down and to the right freely
							* but not past preceding break point and also move first break point up and down
							*/
							(selectedBreakPoint == env.levels.lastIndex).if {
								env.setLevel(selectedBreakPoint,breakPointLevel);
								breakPointTime = breakPointTime.max(prevBreakPointTime);
								env.setTime(selectedBreakPoint - 1,breakPointTime - prevBreakPointTime);
								breakPointCoords[selectedBreakPoint] = breakPointCoordX.max(breakPointCoords[selectedBreakPoint - 1].x)@breakPointCoordY.clip(0,me.bounds.height);
								env.setLevel(0,breakPointLevel);
								breakPointCoords[0].y = breakPointCoordY.clip(0,me.bounds.height);

								// adjust first and last curve points
								this.prCalcYCurvePoint(0);
								this.prCalcXCurvePoint(curvePointCoords.lastIndex);
								this.prCalcYCurvePoint(curvePointCoords.lastIndex)
							} {
								(breakPointCoords[selectedBreakPoint].x > breakPointCoords[selectedBreakPoint - 1].x and:
									{ breakPointCoords[selectedBreakPoint].x < breakPointCoords[selectedBreakPoint + 1].x }).if {
									/*
									* if x-coordinate of break point is in between the x-coordinate of preceding break point
									* and that of the next break point we can move the break point to any position
									*/
									env.setLevel(selectedBreakPoint,breakPointLevel);
									env.setTime(selectedBreakPoint - 1,breakPointTime - prevBreakPointTime);
									breakPointCoords[selectedBreakPoint] = breakPointCoordX@breakPointCoordY.clip(0,me.bounds.height)
								} {
									(breakPointCoords[selectedBreakPoint].x <= breakPointCoords[selectedBreakPoint - 1].x).if {
										/*
										* if x-coordinate of selected break point is equal to the x-coordinate of the preceding break point,
										* allow it to move up and down but not past preceding break point
										*/
										env.setLevel(selectedBreakPoint,breakPointLevel);
										breakPointTime = breakPointTime.max(prevBreakPointTime);
										env.setTime(selectedBreakPoint - 1,breakPointTime - prevBreakPointTime);
										breakPointCoords[selectedBreakPoint] = breakPointCoordX.max(breakPointCoords[selectedBreakPoint - 1].x)@breakPointCoordY.clip(0,me.bounds.height)
									} {
										/*
										* if the x-coordinate of the break point is equal to the x-coordinate of the next break point,
										* allow it to move up and down but not past the next break point when in fix mode
										*/
										env.setLevel(selectedBreakPoint,breakPointLevel);
										nextBreakPointTime = env.times[0..selectedBreakPoint].sum;
										(breakPointMode == \fix).if {
											breakPointTime = breakPointTime.min(nextBreakPointTime);
											breakPointCoordX = breakPointCoordX.min(breakPointCoords[selectedBreakPoint + 1].x)
										};
										env.setTime(selectedBreakPoint - 1,breakPointTime - prevBreakPointTime);
										breakPointCoords[selectedBreakPoint] = breakPointCoordX@breakPointCoordY.clip(0,me.bounds.height)
									}
								};

								// adjust curve points to the left and right of the break point
								this.prCalcXCurvePoint(selectedBreakPoint - 1);
								this.prCalcYCurvePoint(selectedBreakPoint - 1);
								this.prCalcXCurvePoint(selectedBreakPoint);
								this.prCalcYCurvePoint(selectedBreakPoint);

								/*
								* if in chained break point mode, shift all break points and curve points to the right of
								* the selected break point an equal amount in the horizontal direction
								*/
								(breakPointMode == \slide).if {
									env.setTime(selectedBreakPoint - 1,(dinitBreakPointTime + breakPointTime - initBreakPointTime).max(0));
									(selectedBreakPoint + 1..breakPointCoords.lastIndex) do: { |i,j|
										breakPointCoords[i].x = breakPointCoords[i - 1].x + dbreakPointXCoords[j];
										(j > 0).if {
											curvePointCoords[i - 1].x = (breakPointCoords[i - 1].x + breakPointCoords[i].x)/2
										}
									}
								}
							}
						};

						brPtAbsTView.string_(this.prMakeStr(breakPointTime),3);
						brPtRelTView.string_(
							(selectedBreakPoint > 0).if {
								this.prMakeStr(env.times[selectedBreakPoint - 1],3)
							} {
								"0.0"
							}
						);
						brPtLevelView.string_(this.prMakeStr(env.levels[selectedBreakPoint],3))
					} {
						onCurvePoint.if {
							this.prCalcYCurvePoint(selectedBreakPoint - 1,dy);
							crPtSlopeView.string_(
								(selectedBreakPoint > 0).if {
									env.curves[selectedBreakPoint - 1]
								} {
									0
								}
							)
						} {
							// if the mouse cursor is not on a selected break point or a curve point, scale or translate the view

							// translate view
							selGridLineCoord.x = selGridLineCoord.x + dx;

							// zoom in / out at currently selected grid line
							(scaleRespCount == 0).if {

								(dy < 0 and: { uI < (unitStep[\time].lastIndex - 1) } or: { dy > 0 and: { uI > 1 } }).if {
									vhorzGridDist = vhorzGridDist - dy
								};

								(dy < 0).if {
									// mouse is dragged up -> zoom in
									(vhorzGridDist > maxHorzGridDist).if {
										vhorzGridDist = minHorzGridDist;
										uI = uI + 1;
										timeStep = switch(unitMode,
											\time, { unitStep[\time][uI] },
											\tempo, { unitStep[\tempo][uI][\time] }
										);
										timeIncr = timeStep/numGridLinesPerUnit;
										remNumGridLines = this.prCalcRemNumGridLines
									}
								};
								(dy > 0).if {
									// mouse is dragged down -> zoom out
									(vhorzGridDist < minHorzGridDist).if {
										vhorzGridDist = maxHorzGridDist;
										uI = uI - 1;
										timeStep = switch(unitMode,
											\time, { unitStep[\time][uI] },
											\tempo, { unitStep[\tempo][uI][\time] }
										);
										timeIncr = timeStep/numGridLinesPerUnit;
										remNumGridLines = this.prCalcRemNumGridLines;
										// quantize previous selected grid coordinate to nearest current selected grid coordinate
										selGridLineCoord.y = (selGridLineCoord.y/timeStep).asInteger*timeStep + (remNumGridLines*timeIncr)
									}
								}
							};

							// lock left side of view to 0
							(domainMode == \unipolar).if { this.prAdjustZeroPos };

							// rescale envelope when grid is rescaled
							this.prUpdateAllBreakPointCoords;
							this.prUpdateAllCurvePointCoords
						}

					};

					scaleRespCount = scaleRespCount + 1;
					(scaleRespCount >= scaleResponsiveness).if { scaleRespCount = 0 };
					prevPoint = x@y;
					this.action.(env);
					me.refresh
				}),
				4
			).margins_(0)
		);

		rangeView = UserView(parent,bounds).background_(parent.notNil.if { parent.background } { nil }).drawFunc_({ |me|
			var numUnits = numVertGridLines.div(2) + 1,vertGridDist = envGrid.bounds.height/numVertGridLines;
			Pen.font_(font);
			Pen.strokeColor = Color.black;
			numUnits do: { |i|
				Pen.stringAtPoint(this.prMakeStr(maxRange - ((maxRange - minRange)/(numUnits - 1)*i)),0@(i*vertGridDist*2 - 1))
			};
			me.refresh
		});

		topSettingsView = View(parent,bounds).layout_(
			HLayout(
				GridLayout.rows([
					StaticText().string_("Min Range").font_(font).background_(gridBackgroundColor).align_(\center).minWidth_(65).maxWidth_(65),
					NumberBox().value_(minRange).action_({ arg number;
						var oldMinRange = minRange;
						(number.value > maxRange).if {
							minRange = maxRange;
							maxRange = number.value
						};
						minRange = number.value;
						this.prUpdateAllBreakPointCoordsY; this.prUpdateAllCurvePointCoordsY;
 						rangeView.refresh; envView.refresh
					}).minWidth_(40).maxWidth_(40).minHeight_(14).maxHeight_(14).align_(\right).font_(font),
				]).margins_(0).hSpacing_(0),
				GridLayout.rows([
					StaticText().string_("Max Range").font_(font).background_(gridBackgroundColor).align_(\center).minWidth_(65).maxWidth_(65),
					NumberBox().value_(maxRange).action_({ arg number;
						var oldMaxRange = maxRange;
						(number.value < minRange).if {
							maxRange = minRange;
							minRange = number.value
						};
						maxRange = number.value;
						this.prUpdateAllBreakPointCoordsY; this.prUpdateAllCurvePointCoordsY;
						rangeView.refresh; envView.refresh
					}).minWidth_(40).maxWidth_(40).minHeight_(14).maxHeight_(14).align_(\right).font_(font)
				]).margins_(0).hSpacing_(0),
				nil,
				GridLayout.rows([
					StaticText().string_("Sus").font_(font).background_(gridBackgroundColor).align_(\center).minWidth_(25).maxWidth_(25),
					loopStartNodeView = Button().states_([
						["",Color.black,Color(171/255,184/255,189/255)],
						["",Color.black,Color(255/255,215/255,127/255)]
					]).action_({ arg butt;
						switch(butt.value,
							0, { env.loopNode_(nil) },
							1, { env.loopNode_(loopStartNode) }
						)
					}).valueAction_(0).minWidth_(20).maxWidth_(20).minHeight_(14).maxHeight_(14)
				]).margins_(0).hSpacing_(0),
				GridLayout.rows([
					StaticText().string_("Rel").font_(font).background_(gridBackgroundColor).align_(\center).minWidth_(25).maxWidth_(25),
					loopEndNodeView = Button().states_([
						["",Color.black,Color(171/255,184/255,189/255)],
						["",Color.black,Color(255/255,215/255,127/255)]
					]).action_({ arg butt;
						switch(butt.value,
							0, { env.releaseNode_(nil) },
							1, { env.releaseNode_(loopEndNode) }
						)
					}).valueAction_(0).minWidth_(20).maxWidth_(20).minHeight_(14).maxHeight_(14)
				]).margins_(0).hSpacing_(0),
				GridLayout.rows([
					StaticText().string_("Tempo Sync").font_(font).background_(gridBackgroundColor).align_(\center).minWidth_(70).maxWidth_(70),
					Button().states_([
						["",Color.black,Color(171/255,184/255,189/255)],
						["",Color.black,Color(255/255,215/255,127/255)]
					]).action_({ arg butt;
						switch(butt.value,
							0, { unitMode = \time; timeStep = unitStep[\time][uI] },
							1, { unitMode = \tempo; timeStep = unitStep[\tempo][uI][\time] }
						);
						timeIncr = timeStep/numGridLinesPerUnit;
						selGridLineCoord = this.prFindNearestGridCoord(selGridLineCoord.x);
						remNumGridLines = this.prCalcRemNumGridLines;
						(domainMode == \unipolar).if { this.prAdjustZeroPos };
						this.prUpdateAllBreakPointCoords;
						this.prUpdateAllCurvePointCoords;
						envView.refresh
					}).minWidth_(20).maxWidth_(20).minHeight_(14).maxHeight_(14)
				]).margins_(0).hSpacing_(0)
			).margins_(0)
		);

		bottomSettingsView = View(parent,bounds).layout_(
			HLayout(
				// break point number section
				GridLayout.rows([
					StaticText().string_("BP #").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(30).maxWidth_(30),
					brPtCurrNumbView = StaticText().mouseDownAction_({ |me,x,y,mod|
						prevPoint2 = x@y
					}).mouseMoveAction_({ |me,x,y,mod|
						var dy = y - prevPoint2.y,brPtInd = me.string.asInteger;

						(dy < 0).if {
							// mouse is dragged up -> increment selected break point index
							(selectedBreakPoint < breakPointCoords.lastIndex).if {
								selectedBreakPoint = selectedBreakPoint + 1
							}
						};
						(dy > 0).if {
							// mouse is dragged down -> decrement selected break point index
							(selectedBreakPoint > 0).if {
								selectedBreakPoint = selectedBreakPoint - 1
							}
						};

						me.string_((selectedBreakPoint + 1).asString);
						envView.refresh;
						prevPoint2 = x@y
					}).string_((selectedBreakPoint + 1).asString).font_(font).background_(gridBackgroundColor).align_(\right).minWidth_(24).maxWidth_(24),
					brPtNumbView = StaticText().string_("/" ++ breakPointCoords.size.asString).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(24).maxWidth_(24)
				]).margins_(0).hSpacing_(0),
				2,
				// break point mode section
				GridLayout.rows([
					StaticText().string_("Mode").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(44).maxWidth_(44),
					brPtModeView = StaticText().string_("SLD").mouseDownAction_({ |me|
						switch(me.string.asSymbol,
							\SLD, { breakPointMode = \fix; me.string_("FIX") },
							\FIX, { breakPointMode = \slide; me.string_("SLD") }
						)
					}).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(40).maxWidth_(40)
				]).margins_(0).hSpacing_(0),
				2,
				// break point absolute time section
				GridLayout.rows([
					StaticText().string_("Abs T").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(44).maxWidth_(44),
					brPtAbsTView = StaticText().string_(this.prMakeStr(env.times[0..selectedBreakPoint - 1].sum),3).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(50).maxWidth_(50)
				]).margins_(0).hSpacing_(0),
				2,
				// break point relative time section
				GridLayout.rows([
					StaticText().string_("Rel T").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(44).maxWidth_(44),
					brPtRelTView = StaticText().string_(this.prMakeStr(env.times[selectedBreakPoint - 1],3)).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(50).maxWidth_(50)
				]).margins_(0).hSpacing_(0),
				2,
				// break point level section
				GridLayout.rows([
					StaticText().string_("Level").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(44).maxWidth_(44),
					brPtLevelView = StaticText().string_(this.prMakeStr(env.levels[selectedBreakPoint])).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(50).maxWidth_(50)
				]).margins_(0).hSpacing_(0),
				2,
				// curve point slope section
				GridLayout.rows([
					StaticText().string_("Slope").font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(44).maxWidth_(44),
					crPtSlopeView = StaticText().string_(env.curves[selectedBreakPoint - 1]).font_(font).background_(gridBackgroundColor).align_(\left).minWidth_(50).maxWidth_(50)
				]).margins_(0).hSpacing_(0)
			).margins_(0)
		);

		this.action = action;
		envGridHeight = envGrid.bounds.height;

		view.layout_(
			GridLayout.rows(
				[nil,topSettingsView],
				[rangeView,envView],
				[nil,bottomSettingsView]
			).hSpacing_(0).vSpacing_(0).setColumnStretch(0,1).setColumnStretch(1,20).setRowStretch(0,1).setRowStretch(1,80).setRowStretch(2,1)
		)
	}

	background_ { arg newBackground,refreshFlag = true;
		envView.background_(newBackground);
		refreshFlag.if { envView.refresh }
	}

	horzGridDist_ { arg newHorzGridDist,refreshFlag = true;
		horzGridDist = newHorzGridDist;
		refreshFlag.if { envView.refresh }
	}

	maxHorzGridDist_ { arg newMaxHorzGridDist,refreshFlag = true;
		maxHorzGridDist = newMaxHorzGridDist;
		refreshFlag.if { envView.refresh }
	}

	minHorzGridDist_ { arg newMinHorzGridDist,refreshFlag = true;
		minHorzGridDist = newMinHorzGridDist;
		refreshFlag.if { envView.refresh }
	}

	gridColor_ { arg newGridColor,refreshFlag = true;
		gridColor = newGridColor;
		refreshFlag.if { envView.refresh }
	}

	gridBackgroundColor_ { arg newGridBackGroundColor,refreshFlag = true;
		gridBackgroundColor = newGridBackGroundColor;
		refreshFlag.if { envView.refresh; topSettingsView.refresh; bottomSettingsView.refresh }
	}

	gridWidth_ { arg newGridWidth,refreshFlag = true;
		gridWidth = newGridWidth;
		refreshFlag.if { envView.refresh }
	}

	showHorzAxis_ { arg newShowHorzAxis,refreshFlag = true;
		showHorzAxis = newShowHorzAxis;
		refreshFlag.if { envView.refresh }
	}

	showVertAxis_ { arg newShowVertAxis,refreshFlag = true;
		showVertAxis = newShowVertAxis;
		refreshFlag.if { envView.refresh }
	}

	breakPointSize_ { arg newBreakPointSize,refreshFlag = true;
		breakPointSize = newBreakPointSize;
		refreshFlag.if { envView.refresh }
	}

	curvePointRadius_ { arg newCurvePointRadius,refreshFlag = true;
		curvePointRadius = newCurvePointRadius;
		refreshFlag.if { envView.refresh }
	}

	font_ { arg newFont,refreshFlag = true;
		font = newFont;
		refreshFlag.if { topSettingsView.refresh; rangeView.refresh; envView.refresh }
	}

	unitMode_ { arg newUnitMode,refreshFlag = true;
		unitMode = newUnitMode;
		refreshFlag.if { envView.refresh }
	}

	domainMode_ { arg newDomainMode,refreshFlag = true;
		#[\unipolar,\bipolar].includes(newDomainMode).if {
			domainMode = newDomainMode;
			refreshFlag.if { envView.refresh }
		} {
			"domainMode is not a valid choice".warn
		}
	}

	minRange_ { arg newMinRange,refreshFlag = true;
		minRange = newMinRange;
		refreshFlag.if { rangeView.refresh }
	}

	maxRange_ { arg newMaxRange,refreshFlag = true;
		maxRange = newMaxRange;
		refreshFlag.if { rangeView.refresh }
	}

	scaleResponsiveness_ { arg newScaleResponsiveness;
		scaleResponsiveness = newScaleResponsiveness.asInteger.max(1)
	}

	env_ { arg newEnv,refreshFlag = true;
		newEnv.isKindOf(Env).if {
			env = newEnv;
			env.curves.respondsTo(\at).not.if { env.curves = { env.curves } ! env.times.size };
			breakPointTime = env.times[0..selectedBreakPoint.clip(0,env.levels.size) - 1].sum;
			this.prCreateEnvPlotData;
			refreshFlag.if { envView.refresh }
		} {
			Error("arg env has to be an instance of %\n".format(Env.name)).throw
		}
	}

	drawFunc_ { arg newDrawFunc,refreshFlag = true;
		drawFunc = newDrawFunc;
		refreshFlag.if { envView.refresh }
	}

	action_ { arg newAction;
		action = newAction;
	}

	// private methods
	prCreateEnvPlotData {
		// create break point data
		var xc,yc,unitDist = vhorzGridDist*numGridLinesPerUnit;

		yc = env.levels collect: _.linlin(minRange,maxRange,envGridHeight,0);
		xc = [0] ++ env.times.integrate collect: { |t|
			(t == selGridLineCoord.y).if {
				selGridLineCoord.x
			} {
				selGridLineCoord.x - ((selGridLineCoord.y - t)/timeStep*unitDist).round(1)
			}
		};
		breakPointCoords = [xc,yc].flop collect: _.asPoint;

		// create curve point data
		curvePointCoords = { Point() } ! (breakPointCoords.size - 1);
		this.prUpdateAllCurvePointCoords;
	}

	prUpdateAllBreakPointCoordsX {
		var unitDist = vhorzGridDist*numGridLinesPerUnit;
		[0] ++ env.times.integrate do: { |t,i|
			breakPointCoords[i].x = (t == selGridLineCoord.y).if { selGridLineCoord.x } { selGridLineCoord.x - ((selGridLineCoord.y - t)/timeStep*unitDist).round(1) }
		}
	}

	prUpdateAllBreakPointCoordsY {
		env.levels do: { |lvl,i|
			breakPointCoords[i].y = lvl.linlin(minRange,maxRange,envGridHeight,0)
		}
	}

	prUpdateAllBreakPointCoords {
		var unitDist = vhorzGridDist*numGridLinesPerUnit;
		[0] ++ env.times.integrate do: { |t,i|
			breakPointCoords[i].x = (t == selGridLineCoord.y).if { selGridLineCoord.x } { selGridLineCoord.x - ((selGridLineCoord.y - t)/timeStep*unitDist).round(1) };
			breakPointCoords[i].y = env.levels[i].linlin(minRange,maxRange,envGridHeight,0)
		}
	}

	prUpdateAllCurvePointCoordsY {
		 (breakPointCoords.size - 1) do: { |i|
			this.prCalcYCurvePoint(i)
		}
	}

	prUpdateAllCurvePointCoords {
		(breakPointCoords.size - 1) do: { |i|
			this.prCalcXCurvePoint(i);
			this.prCalcYCurvePoint(i)
		}
	}

	prCalcXCurvePoint { arg idx;
		curvePointCoords[idx].x = (breakPointCoords[idx].x + breakPointCoords[idx + 1].x)/2
	}

	prCalcYCurvePoint { arg idx,inc;
		(breakPointCoords[idx + 1].y > breakPointCoords[idx].y).if {
			inc.notNil.if {
				env.setCurve(idx,(env.curves[idx] - (inc.sign*0.25)).clip(-18,18))
			};
			curvePointCoords[idx].y = breakPointCoords[idx].y - (env.curves[idx].asWarp.map(0.5)*(breakPointCoords[idx].y - breakPointCoords[idx + 1].y))
		} {
			inc.notNil.if {
				env.setCurve(idx,(env.curves[idx] + (inc.sign*0.25)).clip(-18,18))
			};
			curvePointCoords[idx].y = breakPointCoords[idx + 1].y - (env.curves[idx].neg.asWarp.map(0.5)*(breakPointCoords[idx + 1].y - breakPointCoords[idx].y))
		}
	}

	prFindNearestGridCoord { arg x;
		var n = ((x - selGridLineCoord.x)/vhorzGridDist).round(1);
		^((n*vhorzGridDist + selGridLineCoord.x)@(n*timeIncr + selGridLineCoord.y).round(timeIncr))
	}

	prCalcTimeFromCoordX { arg x;
		^((x - selGridLineCoord.x)*timeStep/(vhorzGridDist*numGridLinesPerUnit) + selGridLineCoord.y)
	}

	prCalcRemNumGridLines {
		^((selGridLineCoord.y - selGridLineCoord.y.trunc(timeStep))/timeIncr).round(1).asInteger;
	}

	prDrawVertGridLinesRight { arg pos,bounds,rem;
		while({ pos.x <= bounds.width }) {
			Pen.moveTo(pos.x@0);
			Pen.lineTo(pos.x@bounds.height);
			(rem == 0).if {
				var str = switch(unitMode,
					\time, { this.prMakeStr(pos.y.round(timeStep)) },
					\tempo, { this.prMakeTempoStr(pos.y.round(timeStep)) }
				);
				Pen.stringAtPoint(str,pos.x@(bounds.height/2 - 5))
			};
			pos.x = pos.x + vhorzGridDist;
			pos.y = pos.y + timeIncr;
			rem = rem + 1;
			(rem == numGridLinesPerUnit).if { rem = 0 }
		}
	}

	prDrawVertGridLinesLeft { arg pos,bounds,rem;
		while({ pos.x >= maxHorzGridDist.neg }) {
			Pen.moveTo(pos.x@0);
			Pen.lineTo(pos.x@bounds.height);
			(rem == 0).if {
				var str = switch(unitMode,
					\time, { this.prMakeStr(pos.y.round(timeStep)) },
					\tempo, { this.prMakeTempoStr(pos.y.round(timeStep)) }
				);
				Pen.stringAtPoint(str,pos.x@(bounds.height/2 - 5))
			};
			pos.x = pos.x - vhorzGridDist;
			pos.y = pos.y - timeIncr;
			rem = rem - 1;
			(rem < 0).if { rem = numGridLinesPerUnit - 1 }
		}
	}

	prAdjustZeroPos {
		var currZeroPos = (selGridLineCoord.x - (selGridLineCoord.y/timeIncr*vhorzGridDist)).round(1);
		(currZeroPos > 0).if { selGridLineCoord.x = selGridLineCoord.x - currZeroPos }
	}

	prMakeStr { arg number,prec = 4;
		var str = number.asFloat.asStringPrec(prec);
		(number.frac < 1e-9).if { str = str ++ ".0" };
		^str
	}

	prMakeTempoStr { arg number;
		var numb = number.asFraction,measure = unitStep[\tempo][uI][\measure];
		(numb[1] >= numb[0] and: { numb[1] != measure[1].div(2) }).if {
			numb = ((measure[1].div(2)/numb[1])*numb).round(1).asInteger
		};
		numb[1] = numb[1]*2;
		^((number.abs < 1e-9).if { "" } { numb[0].asString ++ "/" ++ numb[1].asString })
	}

}

// hack to ensure envelope state is properly updated
+ Env {

	setLevel { arg idx,value;
		(idx == 0).if {
			this.levels = [value] ++ this.levels[idx + 1.. this.levels.lastIndex]
		} {
			(idx == this.levels.lastIndex).if {
				this.levels = this.levels[0..idx - 1] ++ [value]
			} {
				this.levels = this.levels[0..idx - 1] ++ [value] ++ this.levels[idx + 1.. this.levels.lastIndex]
			}
		}
	}

	setTime { arg idx,value;
		(idx == 0).if {
			this.times = [value] ++ this.times[idx + 1.. this.times.lastIndex]
		} {
			(idx == this.times.lastIndex).if {
				this.times = this.times[0..idx - 1] ++ [value]
			} {
				this.times = this.times[0..idx - 1] ++ [value] ++ this.times[idx + 1.. this.times.lastIndex]
			}
		}
	}

	setCurve { arg idx,value;
		(idx == 0).if {
			this.curves = [value] ++ this.curves[idx + 1.. this.curves.lastIndex]
		} {
			(idx == this.curves.lastIndex).if {
				this.curves = this.curves[0..idx - 1] ++ [value]
			} {
				this.curves = this.curves[0..idx - 1] ++ [value] ++ this.curves[idx + 1.. this.curves.lastIndex]
			}
		}
	}

}