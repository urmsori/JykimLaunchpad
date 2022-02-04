(
SynthDef.new(\pad,{
	|outBus = 0, freqCore = 440, gate=1|
	var in, osc, env, freq;

	freq = freqCore + SinOsc.ar(freqCore/2, mul:freqCore/4);

	osc = LFTri.ar(freq, iphase:SinOsc.ar(freq/2));

	env = EnvGen.ar(
		Env.adsr(
			attackTime: 1.0,
			sustainLevel: 1,
			releaseTime: 1.0,
		),
		gate:gate,
		doneAction:Done.freeSelf
	);

	in = osc * env;

	in = LPF.ar(in,
		freq: freqCore / (SinOsc.ar(0.5)/4 + 1.5),
		mul: (SinOsc.ar(0.5, phase:0.5)/4 + 1)
	);

	in = FreeVerb.ar(in,
		mix: 0.5,
		room: 1,
		damp: 0.5
	);

	Out.ar(outBus, in!2);
}).add;
)

(
~sound = Synth.new(\pad, [
	\freqCore, 440,
	\gate, 1
]);
)

~sound.set(\gate, 0);

69.midicps;
(69 - 72) % 12;
21-12;