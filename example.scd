s.boot;

(
JykimLaunchpadMk2.init;
// JykimLaunchpad.printVerboseOn();
)

(
JykimLaunchpadMk2.setSendAEffect({
	|outBus, inBus, sendA|
	var input;
	input = In.ar(inBus);
	input = FreeVerb.ar(
		input,
		mix: sendA/JykimLaunchpadMk2.sendAMax,
		room: sendA/JykimLaunchpadMk2.sendAMax,
		damp: 0.5, mul: 1.0, add: 0.0
	);
	Out.ar(outBus, input);
});
)

(
JykimLaunchpadMk2.countPlayNoteX.do{
	|x|
	JykimLaunchpadMk2.countPlayNoteY.do{
		|y|
		JykimLaunchpadMk2.synthDef(x, y, {
			|busSendA, busSendB, busMaster|
			var sig, env, cps;

			cps = JykimLaunchpadMk2.midicpsPlayNote(x, y, 30);
			sig = LFTri.ar(cps)!2;
			env = EnvGen.kr(Env.perc, doneAction:2);
			sig = sig * env * 127.linexp(1,127,0.01,0.3);

			Out.ar(busSendA, sig);
		}).add;

		JykimLaunchpadMk2.registerOnPlayNote(x,y,{
			JykimLaunchpadMk2.synthNew(x, y, [
				\busSendA, JykimLaunchpadMk2.busSendA,
				\busSendB, JykimLaunchpadMk2.busSendB,
				\busMaster, JykimLaunchpadMk2.busMaster
			]);
		});
	}
};
)

(
JykimLaunchpadMk2.setToggleModePlayNote(0,0);

JykimLaunchpadMk2.synthDef(0, 0, {
	|busSendA, busSendB, busMaster|
	var sig, env, cps;

	cps = JykimLaunchpadMk2.midicpsPlayNote(0, 0, 30);
	sig = LFTri.ar(cps)!2;

	Out.ar(busSendB, sig);
}).add;

JykimLaunchpadMk2.registerOnPlayNote(0,0,{
	JykimLaunchpadMk2.synthNew(0, 0, [
		\busSendA, JykimLaunchpadMk2.busSendA,
		\busSendB, JykimLaunchpadMk2.busSendB,
		\busMaster, JykimLaunchpadMk2.busMaster
	]);
});

JykimLaunchpadMk2.registerOffPlayNote(0,0,{
	JykimLaunchpadMk2.synthFree(0, 0);
});
)
