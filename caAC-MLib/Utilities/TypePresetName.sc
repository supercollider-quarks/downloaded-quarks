TypePresetName {

	*new { |action| ^super.newCopyArgs.makeGui(action) }

	makeGui { |action|
		var tmp,field;
		tmp = Window.new("Enter a name!!",Rect(600,400,200,100),false);
		field = TextField(tmp, Rect(15, 60, 170, 20))
		.string_("");
		Button(tmp, Rect(15,20,80,30))
		.states_([["store", Color.black, Color.red]])
		.action_({
			var paramText = field.string;
			if (paramText.size > 0 && action.notNil) {
				action.value(paramText);
				tmp.close;
			};
		});
		Button(tmp, Rect(105, 20, 80,30))
		.states_([["decline", Color.black, Color.red]])
		.action_({  tmp.close;
		});
		tmp.front;
	}
}