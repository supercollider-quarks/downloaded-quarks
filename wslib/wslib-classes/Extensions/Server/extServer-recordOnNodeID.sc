// wslib 2006

+ Server {
	recordOnNodeID { |nodeID|
		this.recordBuf.isNil.if({"Please execute Server-prepareForRecord before recording".warn; }, {
			this.recordNode.isNil.if({
				this.recordNode = Synth_ID.tail(RootNode(this), "server-record", [\bufnum,
					this.recordBuf.bufnum], nodeID );
			}, { this.recordNode.run(true) });
			"Recording".postln;
		});
	}

}