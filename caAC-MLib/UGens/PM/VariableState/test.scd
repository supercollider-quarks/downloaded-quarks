
s.reboot; s.scope; s.meter; s.freqscope


// coupled spring, 2 masses 1 spring
{ CoupledSpring.ar(Impulse.ar(0.1),4000,8000)*0.1!2 }.play;

{ CoupledSpring.ar(Impulse.ar(10),300,1000,0.1)*0.1!2 }.play;

{ CoupledSpring.ar(Impulse.ar(10),10000,2000,0.1,2,2)*0.4!2 }.play;

// coupled spring 2 walls, 2 masses, 3 springs
{ CoupledSpring2.ar(0.000001,0.0000025,3.1,1,0.13) * 0.01!2 }.play

// coupled spring 2 walls, 3 masses, 4 springs
{ CoupledSpring3.ar(0.0000003, 0.0000002, 0.000007, 0.41, 1.001, 0.4, 0.499) * 0.01 ! 2}.play

// NCoupling of 10 masses.
(
var mass = {0.00000003 } ! 10;
var stiffness = { 2 } ! (mass.size+1);
{ NCoupling.ar(mass.size.postln,mass,stiffness) * 0.1 }.play;
)











	