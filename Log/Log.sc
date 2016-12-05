Log : Singleton {
	classvar defaultFormatter, onErrorAction, <levels, exceptionHandler;
	var <>actions, <>formatter, <>shouldPost = true, <>maxLength = 500, <lines, <level, levelNum;

	*initClass {
		defaultFormatter = {
			|item, log|
			"[%]".format(log.name.asString().toUpper()).padRight(12) ++ item[\string];
		};

		levels = (
			debug: 0,
			info: 1,
			warning: 3,
			error: 6,
			critical: 9
		);
	}

	*logErrors {
		| shouldLog = true |
		var rootThread = thisThread, handler;

		while { rootThread.parent.notNil } {
			rootThread = rootThread.parent;
		};

		if (shouldLog) {
			exceptionHandler = {
				| exc |
				try {
					Log(\error).error(exc.errorString.replace("ERROR: ", ""));
				};

				rootThread.parent.handleError(exc);
			};

			rootThread.exceptionHandler = exceptionHandler;

			OnError.add(onErrorAction = {
				Log(\error, "---");
			})
		} {
			if (rootThread.exceptionHandler == exceptionHandler) {
				rootThread.exceptionHandler = exceptionHandler = nil;
			}
		}
	}

	init {
		actions = IdentitySet();
		lines = LinkedList(maxLength);
		formatter = defaultFormatter;
		this.level = \info;
	}

	level_{
		|inLevel|
		level = inLevel;
		levelNum = levels[level];
	}

	addEntry {
		| item |
		lines.add(item);
		if (lines.size() > maxLength) {
			lines.popFirst();
		}
	}

	debug {
		| str ...items |
		this.set(str.format(*items), \debug)
	}

	info {
		| str ...items |
		this.set(str.format(*items), \info)
	}

	warning {
		| str ...items |
		this.set(str.format(*items), \warning)
	}

	error {
		| str ...items |
		this.set(str.format(*items), \error)
	}

	critical {
		| str ...items |
		this.set(str.format(*items), \critical)
	}

	log {
		| str, inLevel = \default |
		this.log(str, inLevel)
	}

	set {
		| str, inLevel = \default |
		var logLevel, logItem;
		logLevel = levels[inLevel] ? 0;
		if (logLevel >= levelNum) {
			logItem = (
				\string: str,
				\level: inLevel,
				\time: Date.getDate()
			);
			logItem[\formatted] = this.format(logItem);

			this.addEntry(logItem);

			if (shouldPost) {
				logItem[\formatted].postln;
			};

			actions.do({
				| action |
				action.value(logItem, this);
			});
		}
	}

	format {
		| item |
		^formatter.value(item, this);
	}
}

LogWindow : Singleton {
	var <action, <window, <textView, <names, <logs, textViewSize = 0, connected = false,
	font, boldFont, regularColor, errorColor, pending, lastFlush = 0;

	init {
		logs = IdentitySet();
		names = IdentitySet();
		font = Font("Source Code Pro", 11);
		boldFont = Font("Source Code Pro", 11, bold: true);
		regularColor = Color.grey(0.3);
		errorColor = Color.red(0.8);
		pending = List(size:200);

		action = {
			| item, log |
			pending.add([item, log]);
			{ this.writeLater() }.defer(0.01);
		};
	}

	writeLater {
		var time = AppClock.seconds;
		if ((time - lastFlush) < 0.25) {
			{ this.writeLater() }.defer(time - lastFlush + 0.25);
		} {
			lastFlush = time;
			this.doWrite();
		}
	}

	doWrite {
		var item, log, logString, logStringSize;

		if (textView.notNil) {
			if (textView.isClosed.not) {
				pending.do {
					| p |
					#item, log = p;
					if (item.notNil) {
						logString = item[\formatted] + "\n";
						logStringSize = logString.size();
						textView.setStringColor(regularColor, textViewSize-1, 1);
						textView.setString(logString, 999999999, 0);
						textView.setFont(boldFont, textViewSize, 12);
						if ((log.name == \error) || (Log.levels[item.level] >= Log.levels[\error])) {
							textView.setStringColor(errorColor, textViewSize, logStringSize - 1)
						};

						textViewSize = logStringSize + textViewSize;
					}
				};

				if (pending.isEmpty.not) {
					pending.clear();
					textView.select(textViewSize, 0);
				}
			}
		}
	}

	set {
		| namesArray |
		var newNames;

		namesArray.postln;
		namesArray = namesArray ?? [name];

		if (namesArray.isKindOf(Symbol) || namesArray.isKindOf(String)) {
			namesArray = [ namesArray ];
		};

		newNames = IdentitySet.newFrom(namesArray);

		if (connected) {
			// added
			(newNames.difference(names)).do {
				|name|
				this.initForLog(Log(name));
			};

			// removed
			(names.difference(newNames)).do {
				|name|
				this.deinitForLog(Log(name));
			}
		};

		names = newNames;
		logs = IdentitySet.newFrom(names.collect(Log(_)))
	}

	disconnect {
		if (connected) {
			connected = false;
			logs.do({
				| log |
				log.actions.remove(action);
			});
		}
	}

	connect {
		if (connected.not) {
			connected = true;
			if (window.notNil) {
				if (window.isClosed.not) {
					logs.do(this.initForLog(_));
				}
			}
		}
	}

	initForLog {
		| log |
		log.actions.add(action);
	}

	deinitForLog {
		| log |
		log.actions.remove(action);
	}

	update {
		action.value();
	}

	clear {
		if (window.notNil) {
			textView.string = "\n";
			textViewSize = 1;
		}
	}

	close {
		if (window.notNil) {
			window.close();
		}
	}

	front {
		if (window.notNil and: { window.isClosed }) { window = nil };
		if (window.isNil) {
			{
				window = View().name_(this.name.asString);
				textView = TextView()
				.autohidesScrollers_(true)
				.editable_(false)
				.background_(Color(0.85, 0.9, 0.85, 0.7))
				.font_(font);

				textView.string_("\n");
				textView.setStringColor(regularColor, 0, 1);

				window.recallPosition(\LogWindow, name);
				window.autoRememberPosition(\LogWindow, name);

				window.layout_(VLayout(textView).margins_(5).spacing_(0));

				CmdPeriod.add(this);

				this.connect();
				window.onClose_({
					this.disconnect();
					window = nil;
					textView = nil;
					CmdPeriod.remove(this);
					textViewSize = 0;
				});
			}.defer();
		};

		this.update();
		window.front;
	}

	cmdPeriod {
		this.clear();
	}
}