// UnitTest.reset; UnitTestScript("NodeSnapshot", thisProcess.nowExecutingPath).runScript;
{
	|test|
	var object, oldObject;

	~testSnap = {
		|func|
		~snapshot = nil;
		TreeSnapshot.get({ |sn| ~snapshot = sn });
		test.asynchAssert({ ~snapshot.notNil }, {
			func.value(~snapshot);
		});
	};

	~hasSnap = { ~snapshot.notNil };

	test.bootServer(); 1.wait;

	SynthDef(\NodeSnapshotTestA, {
		|a, b = 2, c = #[1, 2]|
		Out.ar(10, DC.ar([0, 0]));
		Out.kr(20, DC.kr([0, 0, 0]));
	}).add;
	s.sync;

	~synth = Synth(\NodeSnapshotTestA);
	~testSnap.({
		|sn|
		test.assertEquals(sn.root.children[0].children.size, 1, "correct # of children");

		~synthsnap = sn.root.children[0].children[0];
		test.assert(~synthsnap.isKindOf(SynthSnapshot), "Has a synth.");
		test.assertEquals(~synthsnap.defName, \NodeSnapshotTestA, "Correct def name.");
		test.assertEquals(~synthsnap.controls.size, 3, "Correct number of controls.");

		test.assertEquals(~synthsnap.controls[\a], 0, "value of a");
		test.assertEquals(~synthsnap.controls[\b], 2, "value of b");
		test.assertEquals(~synthsnap.controls[\c], [1, 2], "value of c");

		test.assertEquals(~synthsnap.outputs.size, 2, "correct outputs");
		test.assertEquals(~synthsnap.outputs[0].numberOfChannels, 2);
		test.assertEquals(~synthsnap.outputs[0].startingChannel, 10);
		test.assertEquals(~synthsnap.outputs[0].type, Out);
		test.assertEquals(~synthsnap.outputs[0].rate, 'audio');
		test.assertEquals(~synthsnap.outputs[1].numberOfChannels, 3);
		test.assertEquals(~synthsnap.outputs[1].startingChannel, 20);
		test.assertEquals(~synthsnap.outputs[1].type, Out);
		test.assertEquals(~synthsnap.outputs[1].rate, 'control');

		test.assertEquals(~synthsnap.asSynth.nodeID, ~synth.nodeID, "correct nodeId");

		~synth.set(
			\a, 10,
			\b, 20,
			\c, [30, 40]
		);
		~testSnap.({
			|sn|
			~synthsnap = sn.root.children[0].children[0];
			test.assertEquals(~synthsnap.controls[\a], 10, "correct a after set");
			test.assertEquals(~synthsnap.controls[\b], 20, "correct b after set");
			test.assertEquals(~synthsnap.controls[\c], [30, 40], "correct c after set");
		})
	})
}
