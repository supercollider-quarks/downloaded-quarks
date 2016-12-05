/*
2013
Marinus Klaassen
rotterdamruis.nl
*/


ScoreMixer : ScoreWidget {
	var <scores, lemur, projectSaveAndLoad, yOffset;

	*new { |argLemur|
		^super.newCopyArgs.init(argLemur)
	}

	getState {
		var aPreset = Dictionary.new;
		scores do: { |aScore, i|
			aPreset[i.asSymbol] = aScore.getState;
		};
		^aPreset;
	}

	loadState { |aPreset|
		aPreset do: { |aScorePreset, i|
			var tempScore;
			if (scores[i].isNil) {
				tempScore = PatternControllerScore.new(lemur, i);
				tempScore.makeScoreMixerChannelGui(parent, i * 50 + yOffset, 48);
				scores = scores.add(tempScore);
			} {
				tempScore = scores[i];
			};
			tempScore.loadState(aScorePreset);
		};
		gui[\addCanvas].moveTo(10,scores.size * 50 + yOffset + 5);
	}

	positionChannels {
		scores do: { |aScore, i|
			aScore.index = i;
			aScore.mixerCanvas.moveTo(0, i * 50 + yOffset);

		};
		gui[\addCanvas].moveTo(10,scores.size * 50 + yOffset + 5);
	}

	init { |argLemur|
		yOffset = 55;
		scores = List.new;
		lemur = argLemur;
		projectSaveAndLoad = ProjectSaveAndLoad.new;
		projectSaveAndLoad.storeAction = {
			this.getState;
		};
		projectSaveAndLoad.readAction = { |aPreset|
			this.loadState(aPreset);
		};
	}

	addScore {
		var index = scores.size;
		var tempScore = PatternControllerScore.new(lemur, index.postln);
		tempScore.makeScoreGui;

		tempScore.makeScoreMixerChannelGui(parent.postln, scores.size.postln * 50 + yOffset, 48);

		tempScore.closeAction = { |index|
			scores.removeAt(index);
			this.positionChannels;
		};
		scores = scores.add(tempScore);
		gui[\addCanvas].moveTo(10,scores.size * 50 + yOffset + 5);

	}

	makeGui {
		gui = ();
		parent = Window.new("Score Mixer", Rect(1000,300,400,500), false, true)
		.background_(Color.new255(* ({ 150 }!3 ++ 230)));

		projectSaveAndLoad.makeGui(parent,Rect(0,0,parent.bounds.width,20));

		gui[\balk] = CompositeView(parent, Rect(0,20,parent.bounds.width,30))
		.background_(Color.black.alpha_(0.4));

		gui[\addCanvas] = CompositeView(parent, Rect(
			10,
			scores.size * 50 + yOffset + 5,
			40,
			20));
		gui[\addButton] = MButtonP(gui[\addCanvas], gui[\addCanvas].bounds.extent)
		.action_({ this.addScore });
		scores do: { |aScore, i| aScore.makeScoreMixerChannelGui(parent, i * 50 + yOffset, 48); };
		parent.front;
	}

	closeGui {
		scores do: (_.closeMixerChannelGui)
	}
}
