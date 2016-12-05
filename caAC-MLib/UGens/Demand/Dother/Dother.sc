
/*
input is demand or control signal.
operator is on integer:
0 : ==
1 : <
2 : <=
3 : >
4 : >=
any other number will result in ==

test is value to be compared with..
*/

Dif : DUGen {
	*new { arg input, operator = 1, test = 0, iftrue = 1, iffalse = 0, length = inf;
		^this.multiNew('demand', length, input, operator, test, iftrue, iffalse)
	}
}
