/*
// check which ones are already loaded
MKtlDesc.postLoaded;
MKtlDesc('nanoKEY2')
MKtlDesc.allDescs.keys.asArray == [\nanoKEY2]
*/


TestMKtlDesc : UnitTest {
    setUp {
        // this will be called before each test

		// remove all descs
		MKtlDesc.allDescs.clear
    }
    tearDown {
        // this will be called after each test
    }

    test_fromFileName {

        this.assert(
			MKtlDesc('nanoPAD2') === MKtlDesc.fromFileName("nanoPAD2"),
			"MKtlDesc('nanoPAD2') === MKtlDesc.fromFileName(\"nanoPAD2\")"
		);
	}

	test_descFileExts {
        this.assert(
			MKtlDesc.findFile.collect{|n| n.basename.split($.)[1..2]}.asSet == Set[ ["desc", "scd"] ],
			"descFiles found and all have extension .desc.scd"
		)
	}

	test_allDescs {
		MKtlDesc('nanoKEY2');
		this.assert(
			MKtlDesc.allDescs.keys.asArray == [\nanoKEY2],
			"insert one desc results in one insertion"
		);
		this.assert(
			{
				var allDescKeys = MKtlDesc.allDescs.keys;
				MKtlDesc('nanoKEY2');
				MKtlDesc.allDescs.keys === allDescKeys;
			},
			"insert already present desc does not change allDesc",
			onFailure: {"critical failure in MKtlDesc, abort.".inform}
		);
	}
}