(
s.waitForBoot{

	JykimLaunchpadMk2.init(isMidiAutoConnected: false);
	// JykimLaunchpad.printVerboseOn();

	Routine {
		"Waited for launchpad init".postln;
		JykimLaunchpadMk2.onColorControl(
			JykimLaunchpadMk2.arrowUpX,
			0,
			JykimLaunchpadMk2.yellow
		);
		5.wait;

		// Example 1 : PlayNote (Global)
		JykimLaunchpadMk2.countPlayNoteX.do{
			|x|
			JykimLaunchpadMk2.countPlayNoteY.do{
				|y|
				JykimLaunchpadMk2.synthDef(x, y, {
					|busSendA, busSendB, busMaster, offset = 30|
					var sig, env, cps;

					cps = JykimLaunchpadMk2.midicpsPlayNote(x, y, offset);
					sig = LFTri.ar(cps)!2;
					env = EnvGen.kr(Env.perc, doneAction:2);
					sig = sig * env * 127.linexp(1,127,0.01,0.3);

					Out.ar(busSendA, sig);
				}).add;

				JykimLaunchpadMk2.registerOnPlayNote(
					x,y,{
						JykimLaunchpadMk2.synthNew(x, y, [
							\busSendA, JykimLaunchpadMk2.busSendA,
							\busSendB, JykimLaunchpadMk2.busSendB,
							\busMaster, JykimLaunchpadMk2.busMaster,
							\offset, 30
						]);
				});
			}
		};

		// Example 2 : Bank
		JykimLaunchpadMk2.countPlayNoteX.do{
			|x|
			JykimLaunchpadMk2.countPlayNoteY.do{
				|y|
				JykimLaunchpadMk2.synthDef(x, y, {
					|busSendA, busSendB, busMaster, offset = 30|
					var sig, env, cps;

					cps = JykimLaunchpadMk2.midicpsPlayNote(x, y, offset);
					sig = LFTri.ar(cps)!2;
					env = EnvGen.kr(Env.perc, doneAction:2);
					sig = sig * env * 127.linexp(1,127,0.01,0.3);

					Out.ar(busSendA, sig);
				}).add;

				JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankSession, x, y, {
					JykimLaunchpadMk2.synthNew(x, y, [
						\busSendA, JykimLaunchpadMk2.busSendA,
						\busSendB, JykimLaunchpadMk2.busSendB,
						\busMaster, JykimLaunchpadMk2.busMaster,
						\offset, 30
					]);
				});

				JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankUser1, x, y, {
					JykimLaunchpadMk2.synthNew(x, y, [
						\busSendA, JykimLaunchpadMk2.busSendA,
						\busSendB, JykimLaunchpadMk2.busSendB,
						\busMaster, JykimLaunchpadMk2.busMaster,
						\offset, 33
					]);
				});

				JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankUser2, x, y, {
					JykimLaunchpadMk2.synthNew(x, y, [
						\busSendA, JykimLaunchpadMk2.busSendA,
						\busSendB, JykimLaunchpadMk2.busSendB,
						\busMaster, JykimLaunchpadMk2.busMaster,
						\offset, 35
					]);
				});
			}
		};

		// Example 3 : Send A
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

		// Example 4 : Toggle Mode
		JykimLaunchpadMk2.setToggleModeBankNote(
			JykimLaunchpadMk2.bankSession,
			0,0
		);

		JykimLaunchpadMk2.synthDefBank(JykimLaunchpadMk2.bankSession, 0, 0, {
			|busSendA, busSendB, busMaster|
			var sig, env, cps;

			cps = JykimLaunchpadMk2.midicpsPlayNote(0, 0, 30);
			sig = LFTri.ar(cps)!2;

			Out.ar(busSendB, sig);
		}).add;

		JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankSession, 0, 0,{
			JykimLaunchpadMk2.synthNewBank(JykimLaunchpadMk2.bankSession, 0, 0, [
				\busSendA, JykimLaunchpadMk2.busSendA,
				\busSendB, JykimLaunchpadMk2.busSendB,
				\busMaster, JykimLaunchpadMk2.busMaster
			]);
		});

		JykimLaunchpadMk2.registerOffBankNote(JykimLaunchpadMk2.bankSession, 0,0,{
			JykimLaunchpadMk2.synthFree(0, 0);
		});

		"Launchpad init done".postln;
		JykimLaunchpadMk2.offColorControl(
			JykimLaunchpadMk2.arrowUpX,
			0,
		)
	}.play;

}
)