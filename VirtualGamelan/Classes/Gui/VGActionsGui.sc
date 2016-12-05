// new dynamic version
VGActionsGui {
    var <actions, <>displayFunc, <orderedKeys, <>buttonState;
    var <tagBox, <skip, <buttonRefresh;

    *new { |parent, bounds, actions, displayFunc, orderedKeys, buttonState|
        ^super.newCopyArgs(actions, displayFunc, orderedKeys, buttonState).init(parent, bounds)
    }

    init { |parent, bounds|
        var comp = CompositeView(parent, Rect(0,0, bounds.width, bounds.height));
        var width = 105, height = 22, gap, buttons;

        comp.decorator = FlowLayout(comp.bounds);
        gap = comp.decorator.gap.y;
        orderedKeys = orderedKeys ?? { actions.keys.asArray.sort };
        buttonRefresh = { buttonState.value.do{|bVal, ix| buttons[ix].value = bVal; }};

        buttons = orderedKeys.collect { |key, i|
            var func = actions[key];
            Button(comp, Rect(0, 0, width, height))
            .states_([
                [key.asString, Color.black, Color.grey(0.88)],
                [key.asString, Color.yellow, Color.blue(0.5)]
            ])
            .action_({
                func.value;
                this.update;
            });
        };
        comp.decorator.nextLine;
        tagBox = StaticText(comp, Rect(0, 0, bounds.width - 30, bounds.height - 75))
        .background_(Color(0.5, 0.9, 0.6).vary)
        .resize_(5)
        .font_(Font("Helvetica", 11));

        skip = SkipJack({ this.updateString; }, 0.3, false, "MC");
        tagBox.onClose = { skip.stop };
        this.updateString;
    }

    updateString {
        tagBox.string = displayFunc.value(tagBox);
        buttonRefresh.value;
    }
}