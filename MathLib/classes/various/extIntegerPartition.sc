 +  Integer {

	/*

	z1 algorithm by arthur carabott
	http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.42.1287&rep=rep1&type=pdf

	*/

	allPartitions {

		var x, m, h, r, t, ret;

		x = Array.fill(this, 1);
		x[0] = this;
		m = 0;
		h = 0;
		ret = List[this];

		while { x[0] != 1 } {

			if(x[h] == 2) {
				m = m + 1;
				x[h] = 1;
				h = h-1;
			} {
				r = x[h]-1;
				t = (m-h + 1);
				x[h] = r;

				while { t >= r } {
					h = h + 1;
					x[h] = r;
					t = t-r;
				};

				if(t == 0) {
					m = h
				} {
					m = h + 1;
					if(t > 1) {
						h = h + 1;
						x[h] = t;
					}
				}
			};

			ret.add(x[0..m]);
		};
		^ret
	}

}