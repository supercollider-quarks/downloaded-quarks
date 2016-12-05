
Sonify {}

// multichannel expansion support for Spec
+ SequenceableCollection {
	// this.size and v.size have to match
	unmap { |v| ^this.collect({ |item,i| item.unmap(v[i])}) }
	map { |v| ^this.collect({ |item,i| item.map(v[i])}) }
}