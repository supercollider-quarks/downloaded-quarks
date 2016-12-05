/*+ Object {

	postll { arg level = 0;
		this.postln;
	}
}

+ Collection {
	postll { arg level = 0;
			this.do { |item|
				String.fill(level, Char.tab).post;
				item.postll(level + 1)
			}
	}

}*/