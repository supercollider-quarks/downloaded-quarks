DependencyWalker {
	classvar fileMethodMap, fileClassMap;

	*generateFileMap {
		var classes;
		fileClassMap = IdentityDictionary();
		fileMethodMap = IdentityDictionary();

		classes = Class.allClasses;
		classes.remove(QMetaObject);
		classes.remove(QObject);
		classes.do {
			|class|
			fileClassMap.atFail(class.filenameSymbol, {
				var newList = List();
				fileClassMap[class.filenameSymbol] = newList;
				newList;
			}).add(class);

			class.methods.do {
				|method|
				fileMethodMap.atFail(method.filenameSymbol, {
					var newList = List();
					fileMethodMap[method.filenameSymbol] = newList;
					newList;
				}).add(method);
			}
		};
	}

	*methodsForFile {
		|file|
		if (fileMethodMap.isNil) { this.generateFileMap };
		^fileMethodMap[file.asSymbol] ?? []
	}

	*classesForFile {
		|file|
		if (fileClassMap.isNil) { this.generateFileMap };
		^fileClassMap[file.asSymbol] ?? []
	}

	*docsForFile {
		|file|
		file = file.asString;
		^SCDoc.documents.values.select {
			|doc|
			doc.fullPath == file
		}
	}

	*selectorsForFile {
		|file|
		var classSelectorsList = IdentitySet();
		var methodSelectorsList = IdentitySet();

		this.methodsForFile(file).do {
			|method|
			var i = 0;
			var toCheck = IdentitySet.newFrom(method.selectors);
			while {toCheck.notEmpty && (i < 500)} {
				var sel = toCheck.pop();
				if (sel.isKindOf(FunctionDef)) {
					toCheck.addAll(sel.selectors)
				} {
					if (sel.asSymbol.asClass.notNil) {
						classSelectorsList.add(sel)
					} {
						methodSelectorsList.add(sel)
					}
				};
				i = i + 1;
			};
		};

		this.classesForFile(file).do {
			|class|
			classSelectorsList.add(class.superclass.name)
		};

		^[classSelectorsList, methodSelectorsList];
	}

	*methodsForSelector {
		|selector|
		var methodList = List();
		Class.allClasses.do {
			|class|
			methodList.addAll(class.methods.select({ |m| m.name == selector }))
		};
		^methodList;
	}
}

QuarkValidator {
	classvar versionRE="v?(?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*)(\\.(?:0|[1-9][0-9]*)(?:-[\da-z\-]+(?:\\.[\\da-z\\-]+)*)?(?:\\+[\\da-z\\-]+(?:\\.[\\da-z\\-]+)*)?\\b)?";
	classvar urlPrefixes = #["http://", "https://", "ftp://", "git://", "git+ssh://", "git+http://", "git+https://"];
	classvar requiredFields = #[\name, \summary, \author, \license, \isCompatible, \version, \schelp, \url];

	var <>quarkPath, <>quark, <>data,
	<report, <valid=false;

	*data {
		|data, path|
		^super.new.data_(data).quarkPath_(path).doValidate();
	}

	*path {
		|path|
		^super.new.quarkPath_(path).doValidate();
	}

	*quark {
		|quark|
		^super.new.quark_(quark).doValidate();
	}

	doValidate {
		var fields, missingDeps;

		if (quarkPath.notNil) {
			quark = Quark.fromLocalPath(quarkPath);
		} {
			if (quark.notNil) {
				quarkPath = quark.localPath;
			}
		};

		if (data.isNil) {
			data = quark.data;
		};

		report = (
			loads: 			true,
		);

		fields = requiredFields.collectAs({
			|f|
			f -> "Missing."
		}, IdentityDictionary);

		data.keysValuesDo {
			|field, content|
			field = field.asSymbol;
			if (content.isKindOf(String)) {
				content = content.stripWhiteSpace();
			};

			if (this.respondsTo(field)) {
				fields[field] = this.perform(field, content) ?? true;
			};

			0.001.yieldIfPossible;
		};

		if (quarkPath.notNil) {
			report[\hasClasses] = PathName(quarkPath).deepFiles.detect({ |f| f.extension == "sc" }).notNil;
			report[\hasHelp] = PathName(quarkPath +/+ "HelpSource").deepFiles.detect({ |f| f.extension == "schelp" }).notNil;
		};

		missingDeps = this.missingDependencies();
		missingDeps.keysValuesDo {
			|class, targetQuark|
			report[\warnings] = report[\warnings].add(
				"Class % is used but not found in dependencies. (Possibly contained in %)".format(class, targetQuark)
			)
		};

		report = report.reject(_ == true);
		fields = fields.reject(_ == true);
		if (fields.size > 0) {
			report[\fields] = fields;
		};

		valid = (report.size == 0);
	}

	validateNotEmpty {
		|str|
		if (str.size == 0) {
			^"Field is empty"
		} {
			^true
		}
	}

	name 		{ |str| ^this.validateNotEmpty(str) }
	summary 	{ |str| ^this.validateNotEmpty(str) }
	author 		{ |str| ^this.validateNotEmpty(str) }
	license		{ |str| ^this.validateNotEmpty(str) }
	isCompatible {
		|func|
		func = func.compile;
		if (func.isKindOf(Function)) {
			if (func.value.value.isKindOf(Boolean).not) {
				^"Should evaluate to a boolean."
			} {
				^true;
			}
		} {
			if (func.isKindOf(Boolean).not) {
				^"Should evaluate to a boolean."
			} { ^true }
		}
	}
	test {
		|str|
		if (quarkPath.notNil && str.notEmpty) {
			if (File.exists(quarkPath +/+ str).not) {
				^"Test file was not found."
			}
		}
		^nil;
	}

	version {
		|str|
		var git, gitTag;
		var match = str.asString.findRegexp(versionRE);
		if (match.size == 0) {
			^"Formatted incorrectly - must be e.g. 0.1, v1.0.0, 2.7.2-foo+bar"
		} {
			match = match[0][1];
			if (quarkPath.notNil) {
				git = Git.forPath(quarkPath);
				if (git.notNil) {
					gitTag = git.tag ?? "";
					if (gitTag.beginsWith("tags/")) {
						gitTag = gitTag[5..]
					};

					if (match.stripWhiteSpace != gitTag.stripWhiteSpace) {
						^"Quark version '%' does not match git tag '%'".format(
							match, gitTag.stripWhiteSpace
						)
					}
				}
			}
		};
		^nil;
	}

	schelp {
		|docName|
		var docIndex = SCDoc.documents.values.detectIndex({
			|doc|
			(doc.title == "Classes/LanguageConfig") ||
			(doc.path == "Classes/LanguageConfig")
		});
		if (docIndex.isNil) {
			^"Must match an existing document name or path."
		} { ^nil };
	}

	url {
		|url|
		if (urlPrefixes.detect({ |pre| url.beginsWith(pre) }).isNil) {
			^"Must match one of: %".format(urlPrefixes.join(", "))
		} { ^nil }
	}

	missingDependencies {
		|ignoreInternal=true|
		var rootPath, files, methodDeps, classDeps, includedPaths, missingDeps;
		missingDeps = ();

		if (quarkPath.notNil) {
			rootPath = quarkPath;
			files = PathName(rootPath).deepFiles;
			files = files.select({ |f| f.extension == "sc" });

			methodDeps = IdentitySet();
			classDeps = IdentitySet();
			files.do {
				|f|
				var result = DependencyWalker.selectorsForFile(f.absolutePath);
				classDeps.addAll(result[0]);
				methodDeps.addAll(result[1]);
				0.001.yieldIfPossible();
			};

			includedPaths = List["SCClassLib", rootPath];
			includedPaths.addAll(
				data[\dependencies].collect {
					|dep|
					var quarkName, quark;

					0.001.yieldIfPossible();
					if (dep.isKindOf(Association)) {
						quarkName = dep.key;
					} {
						quarkName = dep.split($@)[0];
					};

					quark = Quarks.installed.detect({ |q| q.name == quarkName });
					if (quark.isNil) {
						try { quark = Quark(quarkName) }
					};
					quark !? { quark.name };
				}
			);

			classDeps = classDeps.reject({
				|class|
				var classFile = class.asClass.filenameSymbol.asString;
				includedPaths.detect({
					|inc|
					0.001.yieldIfPossible();
					classFile.find(inc).notNil;
				}).notNil
			});

			missingDeps = classDeps.collectAs({
				|className|
				var class = className.asClass;
				var quarkFile = Quark.findQuarkFile(class.filenameSymbol.asString);

				0.001.yieldIfPossible();

				if (quarkFile.notNil) {
					class.name -> QuarkEditor(quarkFile).name
				} {
					class.name -> class.filenameSymbol.asString
				}
			}, IdentityDictionary);
		}

		^missingDeps;
	}

	warnings {
		^report[\warnings] ?? []
	}

	errors {
		var reportLines = List();

		report.keysValuesDo {
			|key, val|
			switch (key)
			{ \loads } {
				reportLines.add("Quark did not load.")
			}
			{ \hasClass } {
				reportLines.add("Quark does not appear to contain any .sc class files.")
			}
			{ \hasHelp } {
				reportLines.add("Quark does not have a HelpSource containing .schelp files.")
			}
			{ \fields } {
				val.keysValuesDo {
					|field, problem|
					reportLines.add("Field '%' is incorrect: % (= %)".format(
						field, problem, data[field]
					))
				}
			}
		};

		^reportLines;
	}
}

QuarkEditorViewDependenciesView : TreeView {
	var depsList;

	*new {
		|...args|
		^super.new(*args).init
	}

	*qtClass { ^'QcTreeWidget' }

	init {
		this.columns = ["dependency"];
	}

	makeDependencyView {
		|index, dep|
		var view, text, removeButton;
		view = View().layout_(HLayout(

			text = TextField().string_(dep).action_({
				|v|
				this.setDependency(index, v.string);
			}),

			removeButton = Button().states_([["－"]]).maxSize_(18@18).action_({
				|v|
				this.removeRow(index)
			});
		).margins_(0));
		text.minSize = text.sizeHint;
		^view
	}

	makeAddView {
		^View().minHeight_(24).layout_(HLayout(
			[
				Button().states_([["＋"]]).maxSize_(18@18).action_({ this.newRow() }),
				a: \right
			]
		).margins_(0))
	}

	newRow {
		depsList.add("");
		this.dependencies = depsList;
		this.currentItem = this.itemAt(depsList.size - 1);
	}

	removeRow {
		|index|
		this.removeItem(this.itemAt(index));
		depsList.removeAt(index);
		this.changed(\value, depsList);
	}

	setDependency {
		|index, dep|
		depsList[index] = dep;
		this.changed(\value, depsList);
	}

	dependencies_{
		|inDepsList|
		var selection = this.currentItem !? { this.currentItem.index };

		this.clear();
		depsList = inDepsList.copy();
		depsList.do {
			|dep, i|
			var item;
			this.addItem([dep]);

			item = this.itemAt(i);
			item.setView(0, this.makeDependencyView(i, dep));
			item.strings = [""];
		};

		this.addItem([""]);
		this.itemAt(depsList.size).setView(0, this.makeAddView());

		3.do {
			|i|
			this.invokeMethod(\resizeColumnToContents, [i]);
		};

		this.minSize = this.sizeHint;
		//this.currentItem = selection ? 0;
	}

	value_{
		|val|
		^this.dependencies_(val)
	}
}

QuarkEditorView {
	var <path;
	var <editor;
	var window, pathView, editorView, saveButton, openButton, actionsMenu, <fields, actions;
	var gitView, gitStatus, gitTag, gitAddTagButton, gitRemote, gitPushButton;
	var testRunButton, helpButton;
	var gitUpdater;
	var classesView, docsView, errorsView;
	var newlyAddedClassFiles;
	var connections;
	var lineHeight = 22;

	*new {
		|path|
		^super.new.init(path);
	}

	fieldDescriptions {
		^[
			name: 			(),
			summary:		(lines:5),
			author:			(),
			copyright:		(),
			license:		(),
			version:		(),
			schelp:			(createMethod:\makeHelpField, label:"Default help"),
			url:			(),
			test:			(createMethod:\makeUnitTestField, label:"Unit test"),
			scversion:		(label:"SC Version"),
			isCompatible:	(),
			dependencies: 	(createMethod:\makeDependenciesField),
		].clump(2)
	}

	init {
		|inPath|
		var topView, infoView;
		var labelFont = Font(bold:true);

		fields = ();
		newlyAddedClassFiles = ();

		window = Window("Quark Editor", bounds:600@550).rememberPosition(\QuarkEditor);
		window.autoRememberPosition(\QuarkEditorView);

		editorView = View().layout_(GridLayout.rows(
			*this.fieldDescriptions.collect {
				|desc|
				this.makeField(*desc)
			}
		));

		infoView = View().layout_(VLayout(
			classesView = TreeView().columns_(["Class files"]),
			docsView = TreeView().columns_(["Help files"]),
		));

		gitView = View().layout_(HLayout(
			StaticText().string_("Git status: ").align_(\right).font_(labelFont),
			[gitStatus = StaticText(), s:2],

			StaticText().string_("Tag: ").align_(\right).font_(labelFont),
			[gitTag = StaticText(), s:2],

			StaticText().string_("Remote: ").align_(\right).font_(labelFont),
			[gitRemote = StaticText(), s:2],

			gitAddTagButton = Button().states_([["＋ tag"]]),

			gitPushButton = Button().states_([["push"]])
		).margins_(0));

		topView = View().layout_(HLayout(
			pathView = StaticText().font_(Font(size:14, bold:true)),
			openButton = Button().states_([["Open"]]).maxWidth_(80),
			saveButton = Button().states_([["Save"]]).maxWidth_(80).enabled_(false),
			actionsMenu = PopUpMenu().maxWidth_(22)
		).margins_(0));

		window.layout_(VLayout(
			topView,
			gitView,
			HLayout(
				[editorView, s:2],
				[infoView, s:1]
			),
			[errorsView = ListView(), s:2],
		));

		classesView.canReceiveDragHandler = { true };
		classesView.receiveDragHandler = {
			var drag = classesView.class.currentDrag;
			if (drag.isKindOf(String)) { drag = [drag] };
			drag.do {
				|draggedFile|
				if (File.exists(draggedFile.asString)) {
					this.addFile(draggedFile);
				}
			}
		};
		classesView.mouseDownAction_({
			|v, x, y, a, b, clicks|
			if (clicks > 1) {
				var class, method;
				var clicked = v.currentItem.strings;
				switch (clicked[1])
				{ "extension" } {
					#class, method = clicked[0][1..].split($:);
					class = class.asSymbol.asClass;
					method = class.findMethod(method.asSymbol);
					[class, method].postln;
					method.openCodeFile();
				}
				{ "class" } {
					class = clicked[0].asSymbol.asClass;
					class.postln.openCodeFile();
				}
				{
					clicked[1].openTextFile()
				};
			}
		});

		docsView.canReceiveDragHandler = classesView.canReceiveDragHandler;
		docsView.receiveDragHandler = classesView.receiveDragHandler;
		docsView.mouseDownAction_({
			|v, x, y, a, b, clicks|
			if (clicks > 1) {
				v.currentItem.strings[1].openTextFile();
			}
		});

		pathView.canReceiveDragHandler = { true };
		pathView.receiveDragHandler = {
			var drag = classesView.class.currentDrag;
			if (drag.isKindOf(String) && File.exists(drag)) {
				this.path = drag;
			}
		};

		saveButton.action = { editor.save() };
		openButton.action = {
			FileDialog({
				|path|
				this.path = path;
			}, fileMode:2, acceptMode:0, stripResult:true)
		};

		gitPushButton.action = {
			this.pushGit();
		};

		actions = [
			["Create Help Files", 			{ this.editor.createHelpFiles(); this.populateHelpView(); }],
			["Open Folder in Finder", 		{ "open %".format(path.shellQuote).unixCmd} ],
			["Open Quark file", 			{ "open %".format(this.quarkFile.shellQuote).unixCmd} ]
		];
		actionsMenu.allowsReselection = true;
		actionsMenu.items = actions.flop[0];
		actionsMenu.action = { |v| actions.flop[1][v.value].value; actionsMenu.value = nil; };

		gitAddTagButton.action = {
			var win, text, tagString, origin;
			win = Window("Add tag", bounds:200@40, border:false);
			win.layout_(VLayout(
				StaticText().string_("Tag name").font_(Font(bold:true)),
				text = TextField().action_({
					|v|
					if (v.string.stripWhiteSpace.size > 0) {
						protect {
							this.addGitTag(v.string.stripWhiteSpace)
						} {
							win.close();
						}
					}
				}),
				[
					HLayout(
						Button().states_([["Cancel"]]).action_({ win.close }),
						Button().states_([["Ok"]]).action_({ text.doAction() })
					), a:\right
				]
			));

			win.asView.focusLostAction = { win.close };
			origin = gitAddTagButton.absoluteBounds.origin;
			origin.y = Window.screenBounds.height - origin.y;
			win.bounds = win.bounds.origin_(origin);
			win.front;
			text.string = editor.version;
			text.focus();
		};

		window.front;
		window.onClose = this.onClose(_);

		this.path = inPath;
	}

	makeTextField {
		|lines=1|
		var field = (TextView()
			.usesTabToFocusNextView_(true)
			.fixedHeight_(lineHeight * lines)
			.updateOnAction
		);
		field.focusLostAction = _.doAction;
		^field
	}

	makeField {
		|name, desc|
		var view, label, field, labelString;

		labelString = (desc[\label] ?? name).asString.copy;
		labelString[0] = labelString[0].toUpper;
		label = (StaticText()
			.string_(labelString)
			.align_(\right)
			.font_(Font(size:14, bold:true))
		);

		if (desc[\createMethod].notNil) {
			field = this.perform(desc[\createMethod], name, desc);
		} {
			field = this.makeTextField(desc[\lines] ?? 1);
		};

		fields[name] = field;
		^[label, field]
	}

	makeDependenciesField {
		|name, desc|
		var label, view;
		view = QuarkEditorViewDependenciesView();
		view.connectTo({
			|...args|
			// "deps changed: %".format(args).postln;
		})
		^view;
	}

	makeHelpField {
		|name, desc|
		var field = this.makeTextField(1);

		field.layout_(HLayout(
			[
				helpButton = (Button()
					.states_([["open"]])
					.maxWidth_(50)
					.fixedHeight_(18)
					.action_({ this.openHelpFile() })
				),
				a:\right
			]
		).margins_(1));

		^field
	}

	makeUnitTestField {
		|name, desc|
		var field = this.makeTextField(1);

		field.layout_(HLayout(
			[
				testRunButton = (Button()
					.states_([["run"]])
					.maxWidth_(50)
					.fixedHeight_(18)
					.action_({ this.runUnitTest() })
				),
				a:\right
			]
		).margins_(1));

		^field
	}

	path_{
		|inPath|
		if (path != inPath) {
			path = inPath;
			pathView.string = path;
			this.editor = QuarkEditor(this.quarkFile(), true);
		}
	}

	editor_{
		|newEditor|
		if (newEditor != editor) {
			editor = newEditor;
			this.connect();
			this.updateModifiedState();
			this.updateFields();
			this.updateGit();
			this.updateValidation();
			{ this.populateClassesView() }.defer(0.001);
			{ this.populateHelpView() }.defer(0.001);
		}
	}

	addFile {
		|filePath|
		var newPath = path +/+ PathName(filePath).fileName;
		File.copy(filePath, newPath);
		if (File.exists(newPath)) {
			// File.delete(filePath);
			newlyAddedClassFiles[newPath.asSymbol] = filePath.asSymbol;
			this.populateClassesView();
			this.populateHelpView();
		} {
			Error("Copy from % to % failed.".format(filePath, newPath)).throw
		}
	}

	unitTestFile {
		var test = fields[\test].value ?? "";
		if (test.notEmpty) {
			test = path +/+ test;
			if (File.exists(test)) {
				^test.postln
			}
		};
		^nil;
	}

	runUnitTest {
		var testFile = this.unitTestFile;
		if (testFile.notNil) {
			UnitTestScript(editor.name, testFile).runScript;
		} {
			"Unit test path not found: %".format(testFile).warn;
		}
	}

	openHelpFile {
		var help = fields[\schelp].value ?? "";
		help.openHelpFile;
	}

	connect {
		this.disconnect();
		connections = ConnectionList.makeWith {
			fields.keysValuesDo {
				|fieldName, field|
				editor.signal(fieldName).connectTo(field.valueSlot);
				field.connectTo(editor.valueSlot(fieldName.asSetter));
			};

			editor.signal(\test).connectTo({
				{ testRunButton.enabled = (this.unitTestFile.notNil) }.defer(0);
			});

			editor.signal(\schelp).connectTo({
				{ helpButton.enabled = (editor.schelp ?? "").notEmpty }.defer(0);
			});

			editor.connectTo(this.methodSlot(\updateModifiedState));
			editor.connectTo(this.methodSlot(\updateValidation)).collapse(1);
			editor.signal(\loaded).connectTo(this.methodSlot(\updateFields));

			gitUpdater = SkipJack({ this.updateGit() }, 5);
		};
		connections;
	}

	disconnect {
		connections !? { connections.disconnect() };
		connections = nil;
		gitUpdater.stop; gitUpdater = nil;
	}

	populateClassesView {
		var files, classes, methods;

		files = PathName(path).deepFiles.select({ |f| f.extension == "sc" });
		files = files.collect {
			|f|
			var oldLocation = newlyAddedClassFiles[f.absolutePath.asSymbol];
			if (oldLocation.notNil) {
				PathName(oldLocation.asString)
			} {
				f
			}
		};

		classes = files.collectAs({
			|f|
			f.absolutePath.asSymbol -> List()
		}, IdentityDictionary);
		methods = classes.deepCopy();

		Class.allClasses.do({
			|c|
			if (classes[c.filenameSymbol].notNil && c.isMetaClass.not) {
				classes[c.filenameSymbol].add(c);
			} {
				c.methods.do {
					|m|
					if (methods[m.filenameSymbol].notNil) {
						methods[m.filenameSymbol].add(m)
					}
				}
			}
		});

		classesView.clear();

		files.do({
			|file|
			var path = file.absolutePath.asSymbol;
			var item = classesView.addItem([file.fileName, file.absolutePath]);
			classes[path].do {
				|class|
				item.addChild([class.name, "class"])
			};
			methods[path].do {
				|method|
				item.addChild(["+%:%".format(method.ownerClass, method.name), "extension"])
			};
		});
	}

	populateHelpView {
		var files = PathName(path).deepFiles.select({ |f| f.extension == "schelp" });
		var docs = files.collectAs({
			|f|
			var path = f.absolutePath;
			0.001.yieldIfPossible();
			path.absolutePath.asSymbol -> DependencyWalker.docsForFile(path)
		}, IdentityDictionary);

		docsView.clear();

		files.do {
			|file|
			var item;
			var path = file.absolutePath.asSymbol;
			item = docsView.addItem([file.fileName, file.absolutePath]);
			docs[path].reverseDo {
				|doc|
				item.addChild([doc.path, file.absolutePath])
			};
		}
	}

	pushGit {
		var root = Git.findRepoRoot(path);
		Git(root).push("origin", tags:true);
	}

	addGitTag {
		|tagName|
		var root = Git.findRepoRoot(path);
		if (tagName.notEmpty && root.notNil) {
			Git(root).addTag(tagName);
			this.updateGit();
			this.updateValidation();
		} {
			"Couldn't find git root for path %".format(path).warn
		}
	}

	updateGit {
		var git;
		var gitRoot = Git.findRepoRoot(path);
		var gitRemotes, gitRemoteUrl, tag;

		if (gitRoot.isNil) {
			gitView.enabled = false;
			gitStatus.string = "";
			gitRemote.string = "";
			gitTag.string = "";
		} {
			git = Git(gitRoot);
			gitRemotes = git.remotes;

			gitView.enabled = true;
			gitStatus.string = "% modifed files".format(git.status.size);
			gitStatus.fixedWidth = gitStatus.sizeHint.width + 6;

			if (gitRemotes.isKindOf(Dictionary) and: { gitRemotes[\origin].notNil }) {
				if (gitRemotes[\origin][\push].notNil) {
					gitRemoteUrl = gitRemotes[\origin][\push];
				}
			};
			if (gitRemoteUrl.isNil) {
				gitRemotes.do {
					|remote|
					if (remote[\push].notNil) {
						gitRemoteUrl = remote[\push]
					}
				}
			};
			gitRemote.string = gitRemoteUrl ?? "";

			tag = git.tag ?? {
				var log = git.log(1)[0];
				log !? { log[\commit_hash][0..7] } ?? ""
			};
			if (tag.beginsWith("tags/")) { tag = tag[5..] };
			gitTag.string = tag;
			gitTag.fixedWidth = gitTag.sizeHint.width + 6;
		}
	}

	updateModifiedState {
		saveButton.enabled = editor.modCount > 0;
		pathView.font = (pathView.font ?? Font(Font.defaultSansFace)).italic_(editor.modCount > 0);
	}

	updateValidation {
		errorsView.items = ["Validating..."];
		errorsView.selection = [];

		editor.validate({
			|validation|
			var errors, warnings;

			errorsView.stringColor = Color.black;

			if (validation.warnings.isEmpty && validation.errors.isEmpty) {
				errorsView.items = ["Valid"];
				errorsView.colors = [Color.green.alpha_(0.2)]
			} {
				errorsView.items = validation.errors ++ validation.warnings;
				errorsView.colors = (
					(Color.red.alpha_(0.2) ! validation.errors.size)
					++ (Color.yellow.alpha_(0.2) ! validation.warnings.size)
				)
			};

			errorsView.selection = [];
		});
	}

	updateFields {
		fields.keysValuesDo {
			|fieldName, field|
			field.value = editor.perform(fieldName) ? "";
		};
		testRunButton.enabled = (this.unitTestFile.notNil);
		helpButton.enabled = (editor.schelp ?? "").notEmpty;
	}

	onClose {
		this.disconnect();
		editor = nil;
	}

	quarkFile {
		var quarkFile = PathName(path).files.detect({ |f| f.extension == "quark" });
		if (quarkFile.notNil) {
			^quarkFile.absolutePath
		} {
			^path +/+ (PathName(path).folderName ++ ".quark")
		};
	}

	isQuark {
		^this.quarkFile.notNil;
	}
}

QuarkEditor {
	classvar defaultQuark;

	var <quarkFile, <quarkData, <modCount=0;

	*initClass {
		Class.initClassTree(Archive);
		defaultQuark = Archive.global.at(\QuarkEditor, \defaultQuark) ?? ();
	}

	*new {
		|path, create=true|
		^super.newCopyArgs(path).init(create && File.exists(path).not)
	}

	*defaultQuark {
		^(
			name:			"",
			summary:		"",
			author:			"",
			copyright:		"",
			license:		"",
			version: 		"0.0.1",
			schelp: 		"",
			test:			"",
			url:			"",
			test:			"",
			dependencies:	[],
			scversion:		"",
			isCompatible:	false,
			since:			""
		).putAll(defaultQuark)
	}

	*serialize {
		|quarkData|
		var lines = List();
		var fieldOrder = [\name, \summary, \author, \copyright, \license, \version, \schelp, \test, \url, \dependencies, \isCompatible, \since];
		fieldOrder.do {
			|field|
			if (quarkData[field].notNil) {
				lines.add(this.serializeItem(field, quarkData[field]));
			}
		};
		^"(\n%\n)".format(lines.join(",\n"));
	}

	*serializeItem {
		|field, data|
		var fieldStr, dataStr;
		fieldStr = field.asString.padLeft(14);

		switch (field)
		{ \dependencies } {
			dataStr = "[%%%]".format(
				"\n",
				data.collect({
					|dep|
					" ".wrapExtend(14)
					++ "\t\t"
					++ dep.asString.escapeChar($\\).escapeChar($").quote;
				}).join(",\n"),
				"\n" ++ " ".wrapExtend(16)
			)
		}
		{ \isCompatible } {
			dataStr = try { data.compile.asCompileString } { "false" };
			if (dataStr.isEmpty) { dataStr = "false" };
		}
		{ dataStr = data.asString.escapeChar($\\).escapeChar($").quote }
		^"%: %".format(fieldStr, dataStr)
	}

	init {
		|create|
		if (create) {
			quarkData = this.class.defaultQuark();
			this.save()
		} {
			this.load();
		}
	}

	load {
		if (modCount > 0) {
			"Load has overwritten unsaved changes.".warn;
		};

		if (File.exists(quarkFile)) {
			quarkData = thisProcess.interpreter.executeFile(quarkFile);
			if (quarkData.isNil) {
				Error("Failed to read quark file: %".format(quarkFile)).throw;
			};
			quarkData[\isCompatible] !? {
				if (quarkData[\isCompatible].isKindOf(String).not) {
					quarkData[\isCompatible] = quarkData[\isCompatible].asCompileString;
				}
			};
			modCount = 0;
			this.changed(\loaded);
		} {
			Error("File doesn't exist: %".format(quarkFile)).throw;
		}
	}

	save {
		var str = this.class.serialize(quarkData);
		File.use(quarkFile, "w", {
			|f| f.write(str)
		});
		modCount = 0;
		this.changed(\saved);
	}

	validate {
		|doneAction|
		if (doneAction.notNil) {
			// must do this on the AppClock thread before we can validate!
			SCDoc.documents;
			fork({
				var validator = QuarkValidator.data(quarkData, PathName(quarkFile).parentPath);
				{ doneAction.value(validator) }.defer;
			}, SystemClock)
		} {
			^QuarkValidator.data(quarkData, PathName(quarkFile).parentPath);
		}
	}

	name { ^quarkData[\name] }
	name_{ |v| this.setField(\name, v) }

	summary { ^quarkData[\summary] }
	summary_{ |v| this.setField(\summary, v) }

	author { ^quarkData[\author] }
	author_{ |v| this.setField(\author, v) }

	copyright { ^quarkData[\copyright] }
	copyright_{ |v| this.setField(\copyright, v) }

	license { ^quarkData[\license] }
	license_{ |v| this.setField(\license, v) }

	version { ^quarkData[\version] }
	version_{ |v| this.setField(\version, v) }

	scversion { ^quarkData[\scversion] }
	scversion_{ |v| this.setField(\scversion, v) }

	schelp { ^quarkData[\schelp] }
	schelp_{ |v| this.setField(\schelp, v) }

	test { ^quarkData[\test] }
	test_{ |v| this.setField(\test, v) }

	url { ^quarkData[\url] }
	url_{ |v| this.setField(\url, v) }

	dependencies { ^quarkData[\dependencies] }
	dependencies_{ |v| this.setField(\dependencies, v) }
	addDependency { |dependency| this.dependencies = this.dependencies.add(dependency) }

	isCompatible { ^quarkData[\isCompatible] }
	isCompatible_{ |v| this.setField(\isCompatible, v) }

	since { ^quarkData[\since] }
	since_{ |v| this.setField(\since, v) }

	setField {
		|field, value|
		if (quarkData[field] != value) {
			quarkData[field] = value;
			modCount = modCount + 1;
			this.changed(field, value)
		}
	}

	createHelpFiles {
		var rootPath, files, classes, missingHelp, helpFolder, template;
		rootPath = PathName(quarkFile).parentPath;
		files = PathName(rootPath).deepFiles.select({ |f| f.extension == "sc" });
		classes = files.collect({ |f| DependencyWalker.classesForFile(f.absolutePath) }).flatten;

		classes = IdentitySet.newFrom(classes).reject(_.isMetaClass);

		missingHelp = classes.select({ |c| SCDoc.documents["Classes/" ++ c.name].isUndocumentedClass });
		helpFolder = rootPath +/+ "HelpSource" +/+ "Classes";
		helpFolder.mkdir();
		missingHelp.do {
			|class|
			var helpPath = helpFolder +/+ (class.name ++ ".schelp");
			var doc = SCDocEntry.newUndocClass(class.name);
			if (File.exists(helpPath).not) {
				File.use(helpPath,"w", { arg f;
					f.write(SCDoc.makeClassTemplate(doc))
				})
			} {
				"Class '%' is undocumented, but appears to have a help file.".warn;
			}
		}
	}
}

+TextView {
	value {
		^this.string();
	}

	value_{
		|inVal|
		^this.string_(inVal)
	}

	valueAction_{
		|inVal|
		this.value = inVal;
		this.doAction();
	}
}

+Quark {
	*findQuarkFile {
		|path|
		path = PathName(path);
		while { path.folderName.notEmpty } {
			path.files.do {
				|f|
				if (f.extension == "quark") {
					^f.absolutePath
				}
			};
			path = PathName(path.parentPath);
		};
		^nil
	}
}

+Quark {
	edit {
		QuarkEditorView(this.localPath)
	}
}

+Object {
	yieldIfPossible {
		if (thisThread.isKindOf(Routine)) {
			this.yield;
		}
	}
}

+Git {
	*forPath {
		|path|
		path = Git.findRepoRoot(path);
		if (path.notNil) {
			^Git(path)
		} {
			^nil
		}
	}

	*findRepoRoot {
		|path|
		while { path != PathName(path).parentPath() } {
			if (File.exists(path +/+ ".git")) {
				^path
			};
			path = PathName(path).parentPath();
		};
		^nil
	}

	log {
		|limit ...args|
		var format, cmd, log, formatCodes;
		formatCodes = [
			["%H", 	\commit_hash],
			["%T", 	\tree_hash],
			["%an",	\author_name],
			["%ae",	\author_email],
			["%ad",	\date],
			["%s",	\subject],
			["%b",	\body],
		].flop;

		format = formatCodes[0].join("%x1f") ++ "%x1e";
		cmd = ["log", "--format='%'".format(format)];
		if (limit.notNil) {
			cmd = cmd ++ ["-%".format(limit.asInteger.asString)];
		};
		if (args.notNil) {
			cmd = cmd ++ args;
		};

		log = this.git(cmd);
		^log.split(30.asAscii).collect({
			|item|
			item = item.stripWhiteSpace();
			if (item.isEmpty.not) {
				item = item.split(31.asAscii);
				formatCodes[1].collectAs({
					|key, i|
					key -> item[i]
				}, IdentityDictionary);
			} {
				nil;
			}
		}).select(_.notNil);
	}

	status {
		var status, statusMap;
		statusMap = Dictionary [
			"??" -> \untracked,
			"" -> \unmodified,
			"M" -> \modified,
			"A" -> \added,
			"D" -> \delete,
			"R" -> \renamed,
			"C" -> \copied,
			"U" -> \unmerged,
		];

		status = this.git(["status", "--porcelain"]);
		^status.split($\n).collect({
			|line|
			var match = line.findRegexp("(..) (.*)");
			if (match.notEmpty) {
				(
					\status: statusMap[match[1][1].stripWhiteSpace],
					\file: localPath +/+ match[2][1].stripWhiteSpace
				)
			} {
				nil
			};
		}).select(_.notNil)
	}

	commit {
		|subject, body, filesToAdd|
		var cmd, msgString, msgPath;
		if (subject.stripWhiteSpace.isEmpty) {
			Error("A commit message is required.").throw
		};

		msgString = subject ++ "\n\n" ++ "body";
		msgPath = PathName.tmp +/+ "sc_git_commit_msg" ++ msgString.hash;
		msgString.write(msgPath, true);

		cmd = ["commit", "--file='%'".format(msgPath)];
		if (filesToAdd.isArray) {
			cmd = cmd ++ filesToAdd;
		};
		this.git(cmd);
	}

	ignore {
		|line|
		var ignoreString = "", ignores;
		var existingIndex;
		var ignoreFile = localPath +/+ ".gitignore";

		if (File.exists(ignoreFile)) {
			ignoreString = String.readNew(File(ignoreFile, "r"));
		};

		ignores = ignoreString.split($\n);
		if (ignores.detect({
			|i|
			i.stripWhiteSpace == line.stripWhiteSpace
		}).isNil) {
			ignores = ignores.add(line);
		};
		ignoreString = ignores.join("\n");
		ignoreString.write(ignoreFile, true);
	}

	unignore {
		|line|
		var ignoreFile = localPath +/+ ".gitignore";
		var ignoreString;

		if (File.exists(ignoreFile)) {
			ignoreString = String.readNew(File(ignoreFile, "r"));
			ignoreString = ignoreString.split($\n).reject({
				|existingLine|
				(existingLine.stripWhiteSpace == line.stripWhiteSpace)
			}).join("\n");
			ignoreString.write(ignoreFile, true);
		}
	}

	add {
		|filePattern="*"|
		var result = this.git(["add", filePattern]);
	}

	reset {
		|filePattern="*"|
		var result = this.git(["reset", filePattern]);
	}

	addTag {
		|tagName, force=false|
		var cmd = "tag";
		if (force) { cmd = cmd + "-f" };
		cmd = cmd + tagName.asString;

		this.git([cmd])
	}

	push {
		|remote, branch="HEAD", tags=false|
		var cmd = "push" + remote + branch;
		if (tags) { cmd = cmd + "--tags" };
		^this.git([cmd]);
	}

	remotes {
		var result = ();
		var out = this.git(["remote -v"]);
		var matches = out.findRegexp("^([a-zA-Z0-9]+)\\t([^\\t ]+) \\(([\\w+]+)\\)");

		matches.clump(4).do {
			|match|
			var name = match[1][1].asSymbol,
			url = match[2][1],
			type = match[3][1].asSymbol;

			result[name] ?? { result[name] = () };
			result[name][type] = url;
		};

		^result
	}
}