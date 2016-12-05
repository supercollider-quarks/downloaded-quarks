m.config.genNames.keysValuesDo{|key, name|
	var gen = m.gens[name];

	m.ctls.keyPlayers[key] = KeyPlayer(name);

	// mapping for 0..9 to switch between gens
	"1234567890".do{|char|
		var genName, idx;

		idx = char.digit;
		genName = m.config.genNames[idx];

		genName.notNil.if{
			m.ctls.keyPlayers[key].put(char, {
				m.utils.generatorSelectAction(genName)
			})
		};

	};

	// if there's a function in the Gens called kpAction_% (where % is one of a lowercase character), register it as an action to the corresponding keyplayer
	"qwertzuiopasdfghjklyxcvbnm".do{|char|
		var actionName = "kpAction_%".format(char).asSymbol;
		gen.getHalo(actionName).notNil.if{
			m.ctls.keyPlayers[key].put(char,
				{gen.getHalo(actionName).value})
		};
	}
};

/////////////////// TOOLS
m.utils.generatorSelectAction = {|env, genName|
	// activate gen
	(genName.notNil).if{
		m.selectGenerator(genName);
		m.utils.selectPPMapping(genName);
		m.utils.updateKeyPlayer(genName);
	};
};


m.utils.updateKeyPlayer = {|env, genName|
	m.views.keyPlayers.switchToPlayer(genName);
};

/* tests
m.views.keyPlayers.switchToPlayer(\buzz);
m.views.keyPlayers.switchToPlayer(\ringer);

m.views.keyPlayers.buttons.collect(_.states).postcs
*/

nil; // return value