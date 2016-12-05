NodeSnapshot {
	var <>server, <>nodeId;

	asNode {
		^Node.basicNew(server, nodeId);
	}

	== {
		this.subclassResponsibility();
		// |other|
		// ^(this.class == other.class) and: {
		// 	(nodeId == other.nodeId) and: {
		// 		if (this.class == SynthSnapshot) {
		// 			this.defName == other.defName;
		// 		}
		// 	}
		// }
	}

}

GroupSnapshot : NodeSnapshot {
	var <>numChildren, <children;
	*new {
		^super.new.init()
	}

	init {
		children = List();
	}

	asGroup {
		^Group.basicNew(server, nodeId);
	}

	isSynth {
		^false
	}

	isGroup {
		^true
	}


	asString {
		arg indent = 0;
		var str, indentString = ("  " ! indent).join("");
		str = indentString ++ "+ Group: %".format(nodeId);
		if (children.notEmpty) {
			str = str + "\n" + children.collect({ |ch| ch.asString(indent + 1) }).join("\n");
		}
		^str
	}

	== {
		|other|
		^(this.class == other.class) and: {
			nodeId == other.nodeId;
		}
	}
}

SynthSnapshot : NodeSnapshot {
	var <defName, <desc, <controls;

	*new {
		^super.new.init()
	}

	isSynth {
		^true
	}

	isGroup {
		^false
	}

	init {
		controls = IdentityDictionary();
	}

	asSynth {
		^Synth.basicNew(defName, server, nodeId);
	}

	== {
		| other |
		// the only way we have of matching in cases where a synthdef has been changed from one update to the next is
		// matching via the def itself, not the def name
		if (this.class != other.class) {
			^false
		} {
			^(nodeId == other.nodeId) && (defName == other.defName) && (desc == other.desc);
		}
	}

	defName_{
		| inDefName |
		defName = inDefName.asSymbol;
		desc = SynthDescLib.match(defName);
	}

	outputs {
		if (desc.notNil) {
			^desc.outputs.collect({
				|out|
				out = out.copy();
				if (out.startingChannel.isKindOf(Symbol)) {
					out.startingChannel = controls[out.startingChannel];
				};
				out;
			})
		} {
			^[]
		}
	}

	outBusses {
		^this.outputs.collect {
			|output|
			Bus(output.rate, output.startingChannel, output.numberOfChannels, server);
		}
	}

	controlsString {
		|join=" ", dec = 0.01|
		^controls.asKeyValuePairs.clump(2).collect({
			|pair|
			"%: %".format(pair[0], pair[1].round(dec))
		}).join(join);
	}

	asString {
		arg indent = 0;
		var str, indentString = (("  " ! indent).join(""));
		str = indentString ++ "+ Synth(%): %s".format(nodeId, defName);
		if (controls.notEmpty) {
			str = str ++ "\n" ++ controls.asKeyValuePairs.clump(2).collect({
				|pair|
				indentString ++ "    %: %".format(*pair)
			}).join("\n")
		};
		^str
	}
}

TraceParser {
	var server, msg, index, hasControls, <rootNode, <nodes;

	*parse {
		|server, msg|
		^super.newCopyArgs(server, msg).parse();
	}

	parse {
		//[ /g_queryTree.reply, 1, 0, 2, 1, 0, 1000, -1, volumeAmpControl2, 4, volumeAmp, 0.2786121070385, volumeLag, 0.10000000149012, gate, 1, bus, 0 ]
		index = 1;
		nodes = List();
		hasControls = this.next() == 1;
		rootNode = this.parseNode();
	}

	parseNode {
		var snapshot, nodeId, numChildren;
		nodeId = this.next();
		numChildren = this.next();
		if (numChildren < 0) {
			snapshot = SynthSnapshot().server_(server).nodeId_(nodeId);
			nodes.add(snapshot);
			this.parseSynth(snapshot);
			^snapshot;
		} {
			snapshot = GroupSnapshot().server_(server).nodeId_(nodeId).numChildren_(numChildren);
			nodes.add(snapshot);
			this.parseGroup(snapshot);
			^snapshot;
		}
	}

	parseGroup {
		arg snapshot;
		snapshot.numChildren.do {
			var child = this.parseNode();
			snapshot.children.add(child);
		}
	}

	parseSynth {
		arg snapshot;
		var controlCount, lastControl;
		snapshot.defName = this.next();
		if (hasControls) {
			controlCount = this.next();
			controlCount.do {
				var name, val;
				name = this.next(); val = this.next();
				if (name.isKindOf(Symbol)) {
					lastControl = name;
					snapshot.controls[name] = val;
				} {
					snapshot.controls[lastControl] = snapshot.controls[lastControl].asArray.add(val);
				};
			}
		}
	}

	next {
		arg items = 1;
		var nextMsgItem;
		if (items == 1) {
			nextMsgItem = msg[index];

		} {
			nextMsgItem = msg[index..(index + items - 1)];
		};
		index = index + items;
		^nextMsgItem
	}

	@= {

	}
}

TreeSnapshot {
	classvar <>dump=false;
	var server, msg, <nodes, <root, <drawFunc;

	*get {
		arg action, node;
		var server;
		node = node ?? { RootNode(Server.default) };
		server = node.server;

		OSCFunc({
			arg msg;
			var snapshot, parsed;
			if (dump) { msg.postln };
			snapshot = TreeSnapshot(server, msg);
			{ action.value(snapshot) }.defer;
		}, '/g_queryTree.reply').oneShot;

		server.sendMsg("/g_queryTree", node.nodeID, 1);
	}

	*new {
		arg server, traceMsg;
		^super.newCopyArgs(server, traceMsg).parse()
	}

	nodeIDs {
		^nodes.collect(_.nodeId);
	}

	parse {
		var parsed;
		parsed = TraceParser.parse(server, msg);
		nodes = parsed.nodes;
		root = parsed.rootNode;
	}

	asString {
		^"TreeSnapshot\n" ++ root.asString(1);
	}

	storeOn {
		| stream, indent=0 |
		stream << "TreeSnapshot: " ++ this.asString();
	}
}

TreeSnapshotView : Singleton {
	var <view, <viewMap, <viewsInUse, currentSnapshot, collapse=false,
	groupColor, groupOutline, autoUpdateRoutine, autoUpdate=true;

	init {
		viewMap = IdentityDictionary();
		viewsInUse = IdentitySet();
		groupColor = Color.hsv(0.35, 0.6, 0.5, 0.5);
		groupOutline = Color.grey(1, 0.3);
	}

	front {
		if (view.isNil) {
			this.makeNew();
		} {
			view.front;
		};
	}

	autoUpdate {
		|up=true, server|
		autoUpdate = up;
		if (up) {
			if (autoUpdateRoutine.notNil) { autoUpdateRoutine.stop() };
			autoUpdateRoutine = SkipJack({
				TreeSnapshot.get({
					|sn|
					this.update(sn);
				}, RootNode(server ?? Server.default));
			}, 0.5);
		} {
			if (up.not && autoUpdateRoutine.notNil) {
				autoUpdateRoutine.stop;
				autoUpdateRoutine = nil;
			}
		}
	}

	update {
		|newSnapshot|
		var oldViews;

		if (view.notNil) {
			oldViews = viewsInUse;
			viewsInUse = IdentitySet();
			currentSnapshot = newSnapshot;
			this.makeViewNode(currentSnapshot.root);
			oldViews.difference(viewsInUse).do {
				|toRemove|
				toRemove.view.visible = false;
				//{ toRemove.view.remove() }.defer(0.1);
				if (viewMap[toRemove.snapshot.nodeId] == toRemove) {
					viewMap[toRemove.snapshot.nodeId] = nil;
				}
			}
		}
	}

	makeNew {
		TreeSnapshot.get({
			|sn|
			{
				currentSnapshot = sn;
				view = ScrollView(bounds:Rect(200, 200, 500, 600));
				view.canvas = View().layout_(VLayout(
					this.makeViewNode(sn.root),
					nil
				));
				view.canvas.background = QtGUI.palette.window;
				view.onClose = {
					this.autoUpdate(false);
					view = nil;
					viewsInUse.clear();
					viewMap.clear();
				};
				view.front;
				view.autoRememberPosition(\TreeSnapshotView, name);

				this.autoUpdate(autoUpdate);
			}.defer
		})
	}

	makeViewNode {
		| node ...args |
		var viewObj = viewMap[node.nodeId];
		if (viewObj.notNil) {
			if (viewObj.snapshot == node) {
				viewObj.snapshot = node;
			} {
				viewObj = nil;
			}
		};

		case
		{ node.isKindOf(GroupSnapshot) } {
			viewObj = viewObj ?? { this.makeViewGroup(node, *args) };
			this.populateViewGroup(viewObj);
		}
		{ node.isKindOf(SynthSnapshot) } {
			viewObj = viewObj ?? { this.makeViewSynth(node, *args) };
		};

		// { node.isKindOf(Collection) } {
		// 	if ((node.size() > 1) && collapse) {
		// 		^this.makeViewCollapsedSynth(node, *args);
		// 	} {
		// 		^this.makeViewSynth(node, *args);
		// 	}
		// }
		viewObj.set(node);
		viewsInUse.add(viewObj);
		viewMap[node.nodeId] = viewObj;
		^viewObj.view;
	}

	drawBorder {
		|v, color|
		Pen.addRect(v.bounds.moveTo(0, 0).insetBy(0.5, 0.5));
		Pen.strokeColor = color;
		Pen.strokeRect();
	}

	separateSynths {

	}

	makeViewGroup {
		| group |
		var gsv = GroupSnapshotView(group);
		gsv.view = (UserView()
			.background_(groupColor)
			.drawFunc_(this.drawBorder(_, groupOutline))
		);

		gsv.view.layout = VLayout().spacing_(3).margins_(5);
		gsv.view.layout.add(
			StaticText().font_(Font("M+ 1c", 10, true))
			.fixedHeight_(26)
			.string_("[%] group".format(group.nodeId))
			.mouseUpAction_({
				|v|
				gsv.folded = gsv.folded.not;
				gsv.childrenView.visible = gsv.folded.not;
			})
		);
		gsv.childrenView = View();
		gsv.childrenView.layout = VLayout().spacing_(0).margins_(0);
		gsv.view.layout.add(gsv.childrenView);
		//gsv.view.layout.add(nil);

		// if (collapse) {
		// 	(group.children
		// 		.separate(this.separateSynthDefs(_))
		// 		.collect(this.makeViewNode(_))
		// 	)
		// } {
		// 	group.children.collect(this.makeViewNode(_)).do {
		// 		|v|
		// 		gsv.view.layout.add(v);
		// 	};
		// };

		^gsv
	}

	populateViewGroup {
		| gsv |
		if (collapse) {
			(gsv.snapshot.children
				.separate(this.separateSynthDefs(_))
				.collect(this.makeViewNode(_))
			)
		} {
			var views, layout;
			layout = VLayout().margins_(0).spacing_(3);
			views = gsv.snapshot.children.collect(this.makeViewNode(_)).do {
				|v, i|
				layout.insert(v, i + 1);
				v;
			};
			//layout.insert(nil, gsv.snapshot.children.size);
			gsv.childrenView.layout = layout;
		};
	}

	makeViewSynth {
		|synth|
		var sv = SynthSnapshotView(synth);
		sv.view = UserView().layout_(
			VLayout().spacing_(0).margins_(2)
		);

		sv.view.layout.add(this.makeSynthHeader(sv));
		sv.view.layout.add(2);
		sv.view.layout.add(this.makeSynthBody(sv));

		(sv.view.background_(Color.grey(0.2, 0.8))
			.minHeight_(20)
			.drawFunc_(this.drawBorder(_, Color.grey(1, 0.3)))
		);

		^sv
	}

	makeSynthHeader {
		|sv|
		^HLayout(
			(StaticText()
				.string_("[%]".format(sv.synth.nodeId))
				.stringColor_(QtGUI.palette.highlightText)
				.font_(Font("M+ 1c", 10, false))
			),
			10,
			(StaticText()
				.string_("\\" ++ sv.synth.defName)
				.stringColor_(QtGUI.palette.highlightText)
				.font_(Font("M+ 1c", 10, true))
			),
			nil,
			[StaticText()
				.string_("TRACE")
				.align_(\right)
				.font_(Font("M+ 1c", 7.5, true))
				.mouseDownAction_({
					sv.synth.asSynth.trace();
				})
				, align:\topRight
			],
			8,
			[StaticText()
				.string_("✕")
				.align_(\right)
				.font_(Font("M+ 1c", 8, true))
				.mouseDownAction_({
					sv.synth.asSynth.free();
					TreeSnapshot.get({ |sn| this.update(sn); })
				})
				, align:\topRight
			]
		)
	}

	makeSynthBody {
		|sv|
		^HLayout(this.makeSynthControls(sv), nil)
	}

	makeSynthControl {
		| sv, controlName, value |
		var view, valueField;
		view = View().minWidth_(50);
		view.layout = HLayout().spacing_(0).margins_(1);
		view.layout.add(nil);
		view.layout.add(
			StaticText()
			.maxHeight_(10)
			.string_(controlName.asString.toUpper)
			.font_(Font("M+ 1c", 8, false))
			.stringColor_(QtGUI.palette.windowText),
			align: \bottomRight
		);

		sv.controls[controlName] = valueField = (TextField()
			.palette_(QPalette().window_(Color.clear).base_(Color.clear))
			.maxHeight_(14)
			.minWidth_(230)
			.maxWidth_(230)
			.stringColor_(QtGUI.palette.highlightText)
			.font_(Font("M+ 1c", 10, true))
		);

		view.layout.add(valueField, align: \bottomRight);

		^[view, align:\right]
	}

	/*	makeSynthControl {
	| sv, controlName, value |
	var view, nameField, valueField;
	// view = View().minWidth_(50);
	// view.layout = HLayout().spacing_(0).margins_(1);

	nameField = (StaticText()
	.maxHeight_(10)
	.string_(controlName.asString.toUpper)
	.align_(\right)
	.font_(Font("M+ 1c", 8, false))
	.stringColor_(QtGUI.palette.windowText)
	);

	sv.controls[controlName] = valueField = (TextField()
	.palette_(QPalette().window_(Color.clear).base_(Color.clear))
	.maxHeight_(14)
	.align_(\left)
	.stringColor_(QtGUI.palette.highlightText)
	.font_(Font("M+ 1c", 10, true))
	);

	// view.layout.add(valueField, align: \bottomRight);
	// view.layout.add(nil);

	^[ [nameField, align: \bottomRight], [valueField, align: \bottomLeft] ];
	}*/

	makeSynthControls {
		|sv, cols=6|
		var layout, controls, controlViews;
		controls = sv.synth.controls.asKeyValuePairs.clump(2);
		controlViews = controls.collect({
			|keyval|
			this.makeSynthControl(sv, *keyval)
		});
		controlViews = controlViews ++ (nil ! (cols - (controlViews.size % cols)));

		^GridLayout.rows(
			*(controlViews.clump(cols))
		).spacing_(1);
	}

	makeSynthOutput {
		|sv, output|
		var type, bus;
		type = (output.rate == \audio).if(" ▸", " ▹");
		bus = output.startingChannel;
		if (bus.isNumber.not) { bus = "∗" };

		^(StaticText()
			.align_(\left)
			.string_("% %".format(type, bus))
			.font_(Font("M+ 1c", 10))
			.stringColor_(QtGUI.palette.highlightText)
			.background_(Color.grey(0.5, 0.5))
			.maxHeight_(12)
			.minWidth_(20)
			.maxWidth_(20)
		);
	}

	makeSynthOutputs {
		|synth|
		var view = View().layout_(VLayout().spacing_(1).margins_(0));

		synth.outputs.do {
			|output|
			view.layout.add(this.makeSynthOutput(synth, output));
		}

		^view;
	}

}

GroupSnapshotView {
	var <>snapshot, <>view, <>childrenView, <>folded = false;

	*new {
		|groupSnapshot, view|
		^super.newCopyArgs(groupSnapshot, view);
	}

	childLayout_{
		view.layout
	}

	set {}
}

SynthSnapshotView {
	var <>snapshot, <>view, <>controls;

	*new {
		|synthSnapshot, view|
		^super.newCopyArgs(synthSnapshot, view, IdentityDictionary());
	}

	synth { ^snapshot }

	set {
		|newSynth|
		snapshot = newSynth;
		snapshot.controls.keysValuesDo {
			|controlName, value|
			var view = controls[controlName];
			if (view.isNil) {
				"View for control '%' of synth % was nil!".format(controlName, snapshot.nodeId).warn;
			} {
				if (view.hasFocus.not) {
					view.string_("%".format(value.round(0.001)))
					.action_({
						|v|
						var node, val;
						val = v.value;
						if ((val[0] == $c) || (val[0] == $a)) {
							snapshot.asSynth.map(controlName.asSymbol, val[1..].asInteger)
						} {
							val = val.interpret;
							if (val.notNil) {
								snapshot.asSynth.setn(controlName.asSymbol, val);
							}
						};

						{ v.background = Color.clear }.defer(0.1);
						v.background_(v.background.blend(Color.green(1, 0.1)));
						v.focus(false);
					})
				}

			};
		}
	}
}


+SynthDesc {
	== {
		|other|
		^(
			(name == name)
			&& (inputs == other.inputs)
			&& (outputs == other.outputs)
			&& (controlDict == other.controlDict)
			&& (constants == other.constants)
			&& (metadata == other.metadata)
		)
	}
}

+IODesc {
	== {
		|other|
		^(
			(rate == other.rate) &&
			(numberOfChannels == other.numberOfChannels) &&
			(startingChannel == other.startingChannel) &&
			(type == other.type)
		)
	}
}

+ControlName {
	== {
		|other|
		^(
			(name == other.name) &&
			(index == other.index) &&
			(rate == other.rate) &&
			(defaultValue == other.defaultValue) &&
			(argNum == other.argNum) &&
			(lag == other.lag)
		)
	}
}