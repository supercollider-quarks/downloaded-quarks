/* Definities van de Buffer Control Widget klassen. Deze klasse vormen ook een interface tussen de BufPool klasse */


/* Statische klasse BufferDict omdat er geen meerdere objecten hoeven te bestaan */

BufferDict {

    /* De Buffer dict klasse is een placeholder voor de Buffers die door de BufPool klasse worden onderhouden */

    classvar  <>buffers, <>specs; /* Statische data-members */

    /* Statische methoden */
    *init {

        if (buffers.isNil) {

            buffers = Dictionary();
            specs   = Dictionary();

        }

    } /* init */


    *bankList {

        /* Verzamel al banknamen uit de buffer dictionary */
        ^buffers.keys.asArray;

    } /* bankList */


    *getBankSf {

        /* Maak een lijst van de soundfiles die aan de argument banknaam zijn toegewezen */
        |argBank|

        var sfNames = [];

        buffers[argBank] do: { |i|
            sfNames = sfNames.add("sf " + i);
        };

        /* Geef de soundfile lijst terug */
        ^sfNames;

    } /* getBankSf */


    *sfDict {

        var sfDict = Dictionary();
        this.bankList do: { |bankName|
            sfDict[bankName] = this.sfDict(bankName);

        };
    } /* sfDict */

} /* BufferDict */


/* Statische klasse BufferPool Control Model. Via deze klasse worden de dependant SBufControl object geregistreerd en geinformeerd wanneer
er een wijziging plaats vind in de buffer dataset */

BufferCM {  /* static class BufferPool Control Model */
    classvar <>model, bufferPool;

    *init {

        /* bufferPool = BufferPool; Make a references to the bufferPool object */
        model = Event();

    }



    *changed {

        | ...args |

        var what = args.removeAt(0);
        if (model.notNil) { model.changed(what, args); };

    }


} /* BufferCM */


SBufControlView {
    var <frame, <label, <bank, <soundfiles, <incBank, <decBank, <led,
    <samplerMode, <midi, <invert, <sampler, name, <bIsShownm, >oSBufControlView,
    bankItemsDependant, bankValueDependant, bIsShown;

    *new {

        |cNameIP, oSBufControlViewIP|

        ^super.newCopyArgs.init(cNameIP, oSBufControlViewIP);

    }

    init {

        |cNameIP, oSBufControlViewIP|

        name = cNameIP;
        oSBufControlView = oSBufControlViewIP;
    }


    gui { |argParent, argBounds|

        var bounds     = argBounds.asRect,
        width      = bounds.width,
        height     = bounds.height;

        frame = CompositeView(argParent, bounds);
        frame.background = Color.grey;

        label = StaticText.new(frame,Rect(4, 2, 44, height - 4));
        label.string_(name);

        bank = PopUpMenu(frame, Rect(50, 2, 70, height - 4));
        bank.items(oSBufControlView.model[\tBankname]);
        bank.value = oSBufControlView.model[\iBank];
        bank.action = { |menu|

            oSBufControlView.setBank(menu.value);
        };

        bankItemsDependant = {

            |oModelIP, oWhatIP, tBanknameIP|

            if (oWhatIP == \tBankname) {

                bank.items = tBanknameIP;

            }

        };

        bankValueDependant = {

            |oModel, oWhatIP, iBankIP|

            if (oWhatIP == \iBank) {

                bank.value = iBankIP;

            }

        };

        /* Add dependants to the model */
        oSBufControlView.model.addDependant(bankItemsDependant);
        oSBufControlView.model.addDependant(bankValueDependant);

        soundfiles = PopUpMenu(frame, Rect(130, 2, 70, height - 4));

        incBank = RoundButton(frame, Rect(210, 2, height - 4 , height - 4)).states_([[ "-" ]]);
        incBank.background = Color.yellow;

        decBank = RoundButton(frame, Rect(240, 2, height - 4 , height - 4) ).states_([[ "+" ]]);
        decBank.background = Color.yellow;

        led = LED(frame, Rect(268, 2, height - 4, height - 4));
        led.value = 0;

        sampler = Button(frame, Rect(295,2,40,height - 4))
        .font_(Font("Monaco", italic: true, size: 9))
        .states_([["SM OFF", Color.red, Color.black],
            ["SM ON", Color.black, Color.red]]);

        midi = Button(frame, Rect(340,2,40,height - 4))
        .font_(Font("Monaco", size: 9))
        .states_([["CC OFF", Color.red, Color.black],
            ["CC ON", Color.black, Color.red]]);

        invert = Button(frame, Rect(382,2,18,18))
        .states_([["ø", Color.red, Color.black],
            ["ø", Color.black, Color.red]]);

        bIsShown = true;
    }

    closeGui {
        frame.remove; label.remove; bank.remove; soundfiles.remove;
        incBank.remove; decBank.remove; led.remove; samplerMode.remove;
        midi.remove; invert.remove; sampler.remove;

        bIsShown = false;

        /* Remove dependants from the model */
        oSBufControlView.model.removeDependant(bankItemsDependant);
        oSBufControlView.model.removeDependant(bankValueDependant);


    }
}


SBufControl {
    classvar iBufControl = 0;

    var <>spec, <name, <>action, midiResp, learnFlag, <>bInvert, fncCMResponder;
    var bufferData;
    var <oBufControlView, <model, <>tempActionFunc, bIsShown;

    *new { |argName, argSpec|
        ^super.newCopyArgs.init(argName);
    }


    init {

        /* Instantieer de buffer data. argName is de buffer argument van een SynthDef */
        |argName = "sndbuf"|

        name = argName;
        bInvert = false;

        /* Registreer dit object doormiddel van een responder functie aan de BufferCM - BufPool object
        * Wanneer een verandering in de BufferPool wordt gemaakt wordt er een event afgevuurd */
        fncCMResponder = this.cmResponder();
        BufferCM.model.addDependant(fncCMResponder);

        /* TODO vul deze met default waarden. Waar moeten deze waarden vandaan komen?? */
        model = (iBank: 0, tBankname: [ ]);

    }


    setBank {

        /* Set iBank and update model dependants */
        |iBankIP|

        model[\iBank] = iBankIP;
        model.changed(\iBank, iBankIP);

    } /* setBank */


    setBanknameItems {
        /* Set iBank and update model dependants */
        |tBankItemIP|

        model[\tBankname] = tBankItemIP;
        model.changed(\tBankname, tBankItemIP);

    } /* setBanknameItems */


    cmResponder { /* Geeft een responder functie terug (zie hieronder) */


        ^{

            /* Wanneer er een wijziging plaatsvind in de BufPool en deze object method geregistreerd is wordt de method afgevuurd
            zodat deze method de data kan verwerken volgens de logica van deze klasse.
            De BufDict wordt onderhouden door de BufPool klasse */

            |oModelIP, cWhatIP, tArgsIP|

            var cBankname, iBank;

            case

            { cWhatIP == \changeBankname } {

                this.setBanknameItems(tArgsIP[2]);
                this.setBank(model[\iBank]);

            } { cWhatIP == \bankUpdate } {


                if ((model[\tBankname].size == 0) && (model[\iBank]  == 0)) {

                    [\setBankFirst, tArgsIP[0]].postln;

                    /* Er is nog geen banknames bekend */
                    this.setBanknameItems(tArgsIP[0]);
                    this.setBank(model[\iBank]);

                } {
                    /* Zoek op basis van naam */

                    /* Wat is de bankname van de huidige bank in de nieuwe lijst */
                    cBankname = model[\tBankname][model[\iBank]];

                    /* update de banknames */
                    this.setBanknameItems(tArgsIP[0]);

                    if (cBankname.notNil) {


                        /* Er is een index gevonden dus selecteer de bankname */
                        this.setBank(tArgsIP[0].indexOf(cBankname));

                    } {
                        iBank = model[\iBank] - 1;

                        /* Er is geen index gevonden dus deze is verwijderd
                        * Ga een index terug. Wanneer de nieuwe index < 0 is wordt nul de nieuwe index */
                        this.setBank(if (iBank < 0) { 0 } { iBank });

                    } /*  if (cBankname.notNil) */

                } /* if ((model[\tBankname].size == 0) && (model[\iBank]  == 0)) */

            } { cWhatIP == \bankRemove } {

                "REMOVE BANK".postln; tArgsIP.postln;

                iBank = model[\tBankname].indexOf(tArgsIP[0]);
                model[\tBankname].removeAt(iBank).postln;
                model[\tBankname].postln;
                this.setBanknameItems(model[\tBankname]);
                iBank = if (iBank == model[\iBank]) { iBank - 1 } { model[\iBank] };
                if (iBank >= model[\tBankname].size) { iBank = iBank - 1 };


                /* Er is geen index gevonden dus deze is verwijderd
                * Ga een index terug. Wanneer de nieuwe index < 0 is wordt nul de nieuwe index */

                this.setBank(if (iBank < 0) { 0 } { iBank });

            }; /* end case */

            if (tempActionFunc.notNil) {  tempActionFunc.value(oModelIP, cWhatIP, tArgsIP); };

        }

    } /* cmResponder */


    name_ { |cNameIP|

        oBufControlView.label.string_(cNameIP);

    }


    gui { |argParent, argBounds|

        oBufControlView = SBufControlView(name, this);
        oBufControlView.gui(argParent, argBounds);
    }

    closeGui {
        oBufControlView.closeGui; /* remove dependants */
        oBufControlView.remove; /* TODO Cleanup */
    }

    midiLearn {

        /*if (midiResp.isNil) {
        midiResp = CCResponder({ |src,chan,num,value|
        // { model[\setValueFunction].value(value / 127); }.defer;
        });
        };
        midiResp.learn; // wait for the first controller
        learnFlag = 1;*/
    }

    midiUnlearn {
        /*	midiResp.remove; midiResp = nil;
        learnFlag = 0;*/
    }

    /*
    value_ {|argValue|
    model[\setValueFunction].value(argValue)
    }

    value {
    ^model[\value]
    }

    name_ {|argName|
    name = argName;
    if (gui.notNil) { gui[\nameView].string_(name); };
    }
    */

    remove {

        this.closeGui;
        BufferCM.model.removeDependant(this.cmResponder);

    }
}