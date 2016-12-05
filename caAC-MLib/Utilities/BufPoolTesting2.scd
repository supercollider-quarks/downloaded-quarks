/*
Belangrijk!!

Creeer een load file and save file knop zodat een project opgeslagen kan worden
en weer kan worden opgeroepen in een later stadium.

Bij elke actie schrijf naar een default locatie op schijf
Implementeer de BufRecord objecxt
methodes die aanwezen moeten zijn:
- na opname sla buffer data als geluids bestand in de default locatie op schijf.
- methodes/parameters die nodig zijn:
= record
= doneAction
= makeGui
= recordSettings
= channels
= duration
= metadata

Verder wanneer een bank verandert is, update de onderliggende objecten die de buffers
gebruiken in hun processen. Dit om realtime sampling and processing mogelijk te maken.

Maak een bank slider/selector zodat de EmbedGui objecten en the Score & Controller interface ook
gebruik kan maken van deze buffers.
*/



a = BufferPool();
a.makeGui;


// Record a buffer and append it to the current bank!!

Server.local.boot
Server.local.waitForBoot { "Yes" }
(
s.boot; s.waitForBoot {
	w = Window.new("", Rect(200, 800, 604, 400));
	w.background = Color.grey(0.2, 0.9);



	~bufferDataBase = IdentityDictionary();

	~soundfileViews = IdentityDictionary();

	a = BPBankView();
	a.makeGui(w, Rect(5, 5, 180, 300));
	a.addAction = { |thisBank|
		var soundfileView = BPSoundfileView();
		var firstFlag = true;

		if (~soundfileViews[~currentBankName.asSymbol].notNil) {
			~soundfileViews[~currentBankName.asSymbol].closeGui;
		};
		~bufferDataBase[thisBank.string.asSymbol] = Array();

		soundfileView.bankName = thisBank.string;

		soundfileView.makeGui(w, Rect(200, 5, 400, 300));
		soundfileView.addBufAction = { |buf, path, what, index, bankName| {
			soundfileView.add(index, path);
			if (firstFlag) { firstFlag = false; soundfileView.removeSoundFile(0)  };
			~bufferDataBase[thisBank.string.asSymbol] = ~bufferDataBase[thisBank.string.asSymbol].insert(index + 1, buf);
			}.defer;
		};
		soundfileView.swapAction = { |bank, index1, index2|
			var a1 = ~bufferDataBase[bank.asSymbol][index1];
			var a2 = ~bufferDataBase[bank.asSymbol][index2];
			~bufferDataBase[bank.asSymbol][index1] = a2;
			~bufferDataBase[bank.asSymbol][index2] = a1;
		};

		soundfileView.selectAction = { |argBankName, argIndex, argName|
			var selectedBuffer;
			"start heir".postln;
			argBankName.postln; argIndex.postln; argName.postln;
			selectedBuffer = ~bufferDataBase[argBankName.asSymbol][argIndex];
			t.changeBuffer(selectedBuffer);
		};

		~soundfileViews[thisBank.string.asSymbol] = soundfileView;
		~currentBankName = thisBank.string;
	};

	a.selectAction = { |bank, index, bankName|
		if (~currentBankName.postln != bankName.postln) {
			~soundfileViews[~currentBankName.asSymbol].closeGui;
			~soundfileViews[bankName.asSymbol].makeGui(w, Rect(200, 5, 400, 300));
			~currentBankName = bankName;
		};
	};

	a.removeAction = { |...args|
		~soundfileViews[args[2].asSymbol].remove;
		~soundfileViews[args[2].asSymbol] = nil;
	};

	a.stringAction = { |argString, oldString, argWhat, argIndex|
		var t1, t2;
		~currentBankName = argString;
		~soundfileViews[oldString.asSymbol].bankName = argString;

		t1 = ~bufferDataBase[oldString.asSymbol];
		t2 = ~soundfileViews[oldString.asSymbol];

		~bufferDataBase[argString.asSymbol] = t1;
		~soundfileViews[argString.asSymbol] = t2;

		~bufferDataBase[oldString.asSymbol] = nil;
		~soundfileViews[oldString.asSymbol] = nil;
	};

	a.add; // Add an initial bank. Replace this with an empty drop interface action.

	t = BPTransporter();
	t.makeGui(w, Rect(5, 320, 350, 60));
	t.editAction = { |buttonValue|
		~currentBankName.postln;
		if (buttonValue > 0) {
			a.units do: { |i| i.edit(true); };
			~soundfileViews[~currentBankName.asSymbol].units do: (_.edit(true));
		} {
			a.units do: { |i| i.edit(false); };
			~soundfileViews[~currentBankName.asSymbol].units do: (_.edit(false));
		}
	};
	t.recorderDoneAction = { |buffer| buffer.postln; };
	w.front;
}
)

