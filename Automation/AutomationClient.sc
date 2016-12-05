/*
  AutomationClient is part of Automation, a SuperCollider Quark.
  (c) 2009 Neels J. Hofmeyr <neels@hofmeyr.de>, GNU GPL v3 or later.

  AutomationClient acts as a docking node for the Automation class.
  Please first see Automation.

  This class can dock to any GUI element like a slider or a button, or
  anything else that satisfies below criteria, and make it subject to
  Automation's control:

  Live changes can be recorded, saved, loaded and replayed.

  These are the criteria for a controllable thing, e.g. a GUI element:

      controllableThing.value, controllableThing.value_
        getter and setter of a member variable of any kind.

      controllableThing.action, controllableThing.action_
        getter and setter for a function variable. That function is called
        whenever a new value needs to be propagated. This should be setup
        before docking, the function gets sucked into AutomationClient.

      and that by calling value_, action is not run implicitly.

  Note: by docking a GUI element, the GUI element's action is replaced by
  AutomationClient.internalAction, and its previous action is put in the
  AutomationClient instance. If you want to change the action *after*
  docking, you need to set the action in the AutomationClient instance, not
  in the GUI element.
*/

/* Detect the type of a value of a GUI element and call appropriate typed file
 * store/read functions. */
AutomationKind {
    classvar <>valueKinds = nil;

    *registerKinds{
        valueKinds = List.new;
        valueKinds.add(AutomationKindFloat());
        valueKinds.add(AutomationKindBoolean());
        valueKinds.add(AutomationKindInt());
        valueKinds.add(AutomationKindString());
    }

    *get{|val|
        var valueKind = nil;

        if (valueKinds == nil) {
            AutomationKind.registerKinds;
        };

        block{|break|
            valueKinds.do{|kind|
                if (kind.matches(val)) {
                    valueKind = kind;
                    break.value;
                };
            };
        };
        ^valueKind;
    }

    putItem {|file, pos, val|
        "invalid AutomationKind, nothing written".postln;
    }

    getItem {|file|
        "invalid AutomationKind, nothing read".postln;
        file.seek(0, 2);
        ^nil;
    }

    matches {|val|
        ^false;
    }
}

AutomationKindFloat : AutomationKind {
    putItem {|file, pos, val|
        file.putItemFloat(pos, val);
    }

    getItem {|file|
        ^file.getItemFloat;
    }

    matches {|val|
        ^(val.isKindOf(Float));
    }
}

AutomationKindInt : AutomationKindFloat {
    matches {|val|
        ^(val.isKindOf(Integer));
    }
}

AutomationKindBoolean : AutomationKind {
    *asFloat {|val|
        if (val){
            ^1.0;
        }
        ^0.0;
    }

    *fromFloat {|val|
        ^val.asBoolean;
    }

    putItem {|file, pos, val|
        file.putItemFloat(pos, AutomationKindBoolean.asFloat(val));
    }

    getItem {|file|
        var item = file.getItemFloat;
        item[1] = AutomationKindBoolean.fromFloat(item[1]);
        ^item;
    }

    matches {|val|
        ^(val.isKindOf(Boolean));
    }
}

AutomationKindString : AutomationKind {
    putItem {|file, pos, val|
        file.putItemString(pos, val);
    }

    getItem {|file|
        ^file.getItemString;
    }

    matches {|val|
        ^(val.isKindOf(String));
    }
}


AutomationFileBase {
    var file = nil,
        <controlledElement = "nil", <controlledValueKind = "nil";
    classvar fileKinds = nil;

    *registerKinds {
        fileKinds = List.new;
        fileKinds.add(AutomationFileText);
        fileKinds.add(AutomationFileDouble);
    }

    *detect {|file|
        if (fileKinds == nil) {
            AutomationFileBase.registerKinds;
        };
        fileKinds.do{|kind|
            if (kind.matches(file)) {
                ^kind.new(file);
            };
        };
        ^nil;
    }

    *matches {
        ^false;
    }

    forElement {|element|
        if (element == nil) {
            controlledElement = "nil";
            controlledValueKind = "nil";
        }{
            controlledElement = "" ++ element.class
                ++ "@" ++ element.absoluteBounds.left
                ++ "x" ++ element.absoluteBounds.top;
            controlledValueKind = "" ++ element.value.class;
        };
    }

    startPut {
    }

    startGet {
    }

    putItemFloat {|pos, val|
        "Float not implemented for this file type".postln;
    }

    getItemFloat {
        "Float not implemented for this file type".postln;
        file.seek(0, 2);
        ^nil;
    }

    putItemString {|pos, val|
        "String not implemented for this file type".postln;
    }

    getItemString {
        "String not implemented for this file type".postln;
        file.seek(0, 2);
        ^nil;
    }

    hasMore {
        ^(file.pos < file.length);
    }

    close {
        file.close;
    }
}

/* Store just a stream of position and value doubles. Incapable of values that
 * cannot be represented by a single floating point number. It raw data without
 * header or type information, hence it always matches. */
AutomationFileDouble : AutomationFileBase {
    *new {|ifile|
        ^super.new.constructor(ifile);
    }

    constructor {|ifile|
        file = ifile;
    }

    *matches {|file|
        ^true;
    }

    putItemFloat {|pos, val|
        file.putDouble(pos);
        file.putDouble(val);
    }

    getItemFloat {
        var pos, val;
        pos = file.getDouble;
        val = file.getDouble;
        ^[pos, val];
    }
}

AutomationFileText : AutomationFileBase {
    classvar expectFirstLine = "Automation txt v1";

    *new {|ifile|
        ^super.new.constructor(ifile);
    }

    constructor{|ifile|
        file = ifile;
    }

    *getFirstLine {|file|
        var firstLine;
        firstLine = file.getLine(expectFirstLine.size + 10);
        ^firstLine;
    }

    *matches {|file|
        var l, m;
        l = AutomationFileText.getFirstLine(file);
        m = false;
        if (l == expectFirstLine) {
            m = true;
        };
        file.seek(0, 0);
        ^m;
    }

    startPut {
        file.write(expectFirstLine ++ "\n");
        file.write("K" + controlledElement + controlledValueKind ++ "\n");
    }

    startGet {
        if (AutomationFileText.getFirstLine(file) != expectFirstLine) {
            file.seek(0, 2);
        };
    }

    putItemFloat {|pos, val|
        file.write("f" + pos + val ++ "\n")
    }

    *parseFloat {|line|
        ^line.asFloat;
    }

    *parseString {|line|
        ^line;
    }

    parseKind {|tokens|
        controlledElement = tokens[1];
        controlledValueKind = tokens[2];
    }

    getItem {
        var l, v, pos, valAt;
        {
            l = file.getLine(4096);
            v = l.split($ );
            case {v[0] == "K"}{
                this.parseKind(v);
            }
            {v[0] == "f"}
            {
                pos = AutomationFileText.parseFloat(v[1]);
                valAt = v[0].size + 1 + v[1].size + 1;
                ^[pos, AutomationFileText.parseFloat(l.copyToEnd(valAt))];
            }
            {v[0] == "s"}
            {
                pos = AutomationFileText.parseFloat(v[1]);
                valAt = v[0].size + 1 + v[1].size + 1;
                ^[pos, AutomationFileText.parseString(l.copyToEnd(valAt))];
            }
            { true }
            {
                Error("Unknown value type:" + v[0]).throw;
            };
        }.loop;
    }

    getItemFloat {
        var item, val;
        item = this.getItem;
        val = item[1];
        if (val.isKindOf(Float)) {
            ^item;
        }{
            if (val.isKindOf(String)) {
                try {
                    ^[item[0], val.toFloat];
                }{|error|
                    ("cannot parse" + val + "as number").postln;
                    ^[item[0], 0]
                };
            }
        };
        Error("Unknown item type:" + val.class).throw;
    }

    putItemString {|pos, val|
        file.write("s" + pos + val.replace("\n", "\\n") ++ "\n");
    }

    getItemString {
        var item, val;
        item = this.getItem;
        val = item[1];
        if (val.isKindOf(String)) {
            ^item;
        }{
            ^[item[0], ""+val];
        };
    }

}

AutomationClient {
    var <>automation = nil,
        <>name = nil,
        <>action = nil,
        values = nil,
        playCursor = -1, recordCursor = -1,
        controllableThing = nil,
        <valueKind = nil;

    *new {|controllableThing, automation, name|
        ^super.new.constructor(controllableThing, automation, name);
    }

    constructor {|icontrollableThing, iautomation, iname|
        controllableThing = icontrollableThing;
        automation = iautomation;
        name = iname;

        values = List.new;

        valueKind = AutomationKind.get(controllableThing.value);
        if (valueKind == nil) {
            ("Unknown GUI value kind:" + controllableThing.value.class
             + "for" + controllableThing.class).postln;
        };

        action = controllableThing.action;
        controllableThing.action = {|view| this.internalAction(view); };

        automation.addClient(this);
    }


    stopRecording {
        recordCursor = -1;
    }

    seek { |seconds|
        this.stopRecording;

        // optimize a rewind
        if (seconds <= 0){
            playCursor = -1;
        };

        this.bang(seconds);
    }

    value {
        ^controllableThing.value;
    }

    value_ {|val|
        controllableThing.value_(val);
        this.internalAction;
        ^val;
    }

    save {|dir|
        var filename, f, file, backupname, item;
        // add a trailing slash.
        // TODO: only works on systems with a '/' file separator.
        filename = dir;
        if (filename.size > 0){
            if (filename.at( filename.size - 1 ) != $/) {
                filename = filename ++ $/;
            };
        };
        // add my name to the dir with the trailing slash, as a filename
        filename = filename ++ name;

        // backup existing file?
        if (File.exists(filename)) {
            backupname = filename ++ ".backup_" ++ Date.getDate.stamp;
            while({File.exists(backupname)},{
                backupname = filename ++ "_" ++ 999.rand;
            });
            ("mv" + filename + backupname).systemCmd;
        };

        // now write it out.
        f = File(filename, "wb");
        if (f.isOpen.not) {
            ("Automation: FAILED to open `" ++ filename ++ "'").postln;
            ^false;
        };

        file = AutomationFileText(f);
        file.forElement(controllableThing);
        file.startPut;
        values.do{|row|
            valueKind.putItem(file, row[0], row[1]);
        };
        file.close;
        if (automation.verbose){
            ("Automation: Saved" + values.size + "values to `" ++ filename ++ "'").postln;
        };
    }

    load {|dir|
        var filename, file, pos, val;

        // make sure we're not directly overwriting loaded values.
        this.stopRecording;

        // add a trailing slash.
        // TODO: only works on systems with a '/' file separator.
        filename = dir;
        if (filename.size > 0){
            if (filename.at( filename.size - 1 ) != $/) {
                filename = filename ++ "/";
            };
        };
        // add my name to the dir with the trailing slash, as a filename
        filename = filename ++ name;

        // read it in
        file = File(filename, "rb");
        if (file.isOpen.not) {
            ("Automation: FAILED to open `" ++ filename ++ "'").postln;
            ^false;
        };

        file = AutomationFileBase.detect(file);
        if (file == nil) {
            (""++filename++": unknown file type").postln;
            ^false;
        };

        try {
            file.startGet;

            values.free;
            values = List.new;

            // a double is 8 bytes, and there's two doubles per value
            // (time and value).
            block{|break|
                {
                    if (file.hasMore.not) {
                        break.value;
                    };
                    values.add(valueKind.getItem(file));
                }.loop;
            };

            file.close;

            if (values.size < 1){
                ("Automation: NO VALUES in `" ++ filename ++ "'").postln;
                ^false;
            };

            if (automation.verbose){
                ("Automation:" + file.class + "loaded"
                 + values.size + "values from `" ++ filename ++ "'"
                 + file.controlledElement + file.controlledValueKind).postln
            };
            ^true;
        }{|error|
            (""+filename++" ("++file.class++"): error: "++error.what).postln;
        }
    }




    // Evaluate whether the internal event cursors should move and
    // update the client gui accordingly.
    // Return the absolute time of the next upcoming value.
    bang{|nowtime|
        var val;
        // adjust playCursor position
        if (playCursor > values.size){
            playCursor = values.size - 1;
        }{
            if (playCursor < 0){
                playCursor = -1;
            };
        };

        // move backward?
        while({ if (playCursor > 0) {
                    (nowtime < values[playCursor][0])
                }{
                    false
                };
              }, {
            playCursor = playCursor - 1;
        });

        // move forward?
        while({ if ((playCursor + 1) < values.size) {
                    (nowtime >= values[playCursor + 1][0])
                }{
                    false
                }
              }, {
            playCursor = playCursor + 1;
        });

        if (recordCursor < 0) {
            // we're in play mode. Set "slider"'s value.
            if (playCursor >= 0){
                val = values[playCursor][1];
                automation.defer{
                    if (val != controllableThing.value){
                            controllableThing.value_(val);
                            action.value(controllableThing);
                    };
                };
            };
        }{
            // we're in recording mode.
            // remove upcoming saved values that are after the
            // recording cursor and pass "now".
            if (playCursor > recordCursor) {
                (playCursor - recordCursor).do{
                    values.removeAt(playCursor);
                    playCursor = playCursor - 1;
                };
            };
        };

        // return the time of the next value coming up after this one.
        if ((playCursor + 1) >= values.size){
            ^inf;
        }{
            ^values[playCursor+1][0];
        };
    }


    // record a given time and value. You may pass a cursor at which
    // to continue recording (for internal calls).
    // Returns the new cursor index.
    record { |time, val, cursor=(-1)|
        var cursorTime, startedRecording;

        startedRecording = false;

        // if not recording yet, accurately determine the position.
        if (cursor < 0){
            startedRecording = true;
            cursor = playCursor;
            if ((cursor < 0) || (cursor >= values.size)){
                cursor = -1;
            };

            // move backward?
            while({ if (cursor >= 0){
                        (time < values[cursor][0])
                    }{  false  }
                  }, {
                cursor = cursor - 1;
            });
            // move forward?
            while({ if ((cursor + 1) < values.size){
                        (time >= values[cursor + 1][0])
                    }{  false  }
                  }, {
                cursor = cursor + 1;
            });
        };

        // record this value. But where to put it?
        if (cursor < 0) {
            // we're supposed to insert the item at the start.
            cursorTime = -inf; // make the next condition pass
            cursor = -1; // make sure it gets added at index 0
        }{
            // let's see when a new value would count as a separate
            // time step (at least minTimeStep later).
            cursorTime = values[cursor][0] + this.automation.minTimeStep;
        };

        if (time > cursorTime){
            // the new value is well later than the current one.
            // add after the current one.
            cursor = cursor + 1;
            values.insert(cursor, [time, val]);
            // we've inserted a value, keep the playCursor stationary.
            if (playCursor >= cursor){ playCursor = playCursor + 1; };
        }{
            // the new value's time is very close to the current one. Replace!
            // Do not change the time though to avoid dragging this
            // value along in case of rapidly incoming updates.
            values[cursor][1] = val;
        };

        ^cursor;
    }


    // records the GUI's current value at the given time.
    // You may have to wrap this in a defer{ ... } (Mac).
    snapshot {|now|
        var cursor = recordCursor;
        cursor = this.record(now, controllableThing.value);
        if ((recordCursor >= 0) &&
            (cursor > recordCursor) &&
            (cursor <= playCursor)){
            // oh no, we're interfering with the recording
            // process. Let's fix it.
            if (cursor > (recordCursor + 1)){
                (cursor - (recordCursor + 1)).do{
                    values.removeAt(recordCursor);
                    playCursor = playCursor - 1;
                };
            };
            recordCursor = recordCursor + 1;
        };
    }


    // this is set up to be called upon an action by
    // the GUI element (e.g. slider)
    internalAction {|view|
        var now, val, startedRecording;

        // avoid negative values, these only show up when playLatency
        // results in a negative now.
        now = max(automation.now, 0.0);

        val = controllableThing.value;

        // call the action set by the user
        action.value(view);

        // now do the automation action
        if (automation.doRecord){
            startedRecording = (recordCursor < 0);
            recordCursor = this.record(now, val, recordCursor);

            if (startedRecording){
                automation.clientStartsRecordingMsg;
            };
        }{
            // Control's recording button is not pressed.
            // Make sure recording is disabled.
            recordCursor = -1;
        };
    }

}

