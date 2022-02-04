(
s.waitForBoot{

	JykimLaunchpadMk2.init(isMidiAutoConnected: true);
	// JykimLaunchpad.printVerboseOn();

	Routine {
		//////////////// Init Start ////////////////////
		"Waited for launchpad init".postln;
		JykimLaunchpadMk2.onColorControl(
			JykimLaunchpadMk2.arrowUpX,
			0,
			JykimLaunchpadMk2.yellow
		);
		5.wait;

		//////////////// Send A ////////////////////
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

		//////////////// High -> Low pad ////////////////////
		JykimLaunchpadMk2.setOffColorPlayNoteLineCMajor(6, 7, JykimLaunchpadMk2.white);

		JykimLaunchpadMk2.countPlayNoteX.do{
			|x|
			(6..7).do{
				|y|
				JykimLaunchpadMk2.synthDefBank(JykimLaunchpadMk2.bankSession, x, y, {
					|busSendA, busSendB, busMaster, gate = 1, noteOffset=21, dur=2|
					var in, midiNum, freqStart, freq;

					midiNum = JykimLaunchpadMk2.midinumPlayNote(x, y, noteOffset);
					freqStart = midiNum.midicps;

					freq = XLine.ar(freqStart, freqStart/2, dur);
					freq = freq + SinOsc.ar(freq/2, mul:freq/8);
					freq = SinOsc.ar(freq, mul:XLine.ar(freq, freq/2, dur));
					freq = FreeVerb.ar(freq, mix: 0.5, room: 1, damp: 0.2);

					in = SinOsc.ar(freq);
					in = BBandPass.ar(in, freqStart);

					in = in * EnvGen.ar(Env.adsr(
						attackTime:0.1,
						sustainLevel:0.5,
						releaseTime:2.0
					), gate:gate, doneAction: Done.freeSelf);

					Out.ar(busSendA, in*0.5);
				}).add;

				JykimLaunchpadMk2.setToggleModeBankNote(
					JykimLaunchpadMk2.bankSession,
					x,y
				);

				JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankSession, x, y, {
					JykimLaunchpadMk2.synthNewBank(JykimLaunchpadMk2.bankSession, x, y, [
						\busSendA, JykimLaunchpadMk2.busSendA,
						\busSendB, JykimLaunchpadMk2.busSendB,
						\busMaster, JykimLaunchpadMk2.busMaster,
						\gate, 1,
						\noteOffset, 24+24
					]);
				});
				JykimLaunchpadMk2.registerOffBankNote(JykimLaunchpadMk2.bankSession, x, y, {
					JykimLaunchpadMk2.synthSet(x, y, [
						\gate, 0
					]);
				});
			};
		};

		//////////////// Ambient pad ////////////////////
		JykimLaunchpadMk2.setOffColorPlayNoteLineCMajor(4, 5, JykimLaunchpadMk2.white);

		JykimLaunchpadMk2.countPlayNoteX.do{
			|x|
			(4..5).do{
				|y|
				JykimLaunchpadMk2.synthDefBank(JykimLaunchpadMk2.bankSession, x, y, {
					|busSendA, busSendB, busMaster, gate=1, noteOffset = 10|
					var in, midiNum, osc, env, freqCore, freq;

					midiNum = JykimLaunchpadMk2.midinumPlayNote(x, y, noteOffset);
					freqCore = midiNum.midicps;

					freq = freqCore + SinOsc.ar(freqCore/2, mul:freqCore/4);

					osc = LFTri.ar(freq, iphase:SinOsc.ar(freq/2));

					env = EnvGen.ar(
						Env.adsr(
							attackTime: 1.0,
							sustainLevel: 1,
							releaseTime: 2.0,
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

					Out.ar(busSendA, in);
				}).add;

				JykimLaunchpadMk2.setToggleModeBankNote(
					JykimLaunchpadMk2.bankSession,
					x,y
				);

				JykimLaunchpadMk2.registerOnBankNote(JykimLaunchpadMk2.bankSession, x, y, {
					JykimLaunchpadMk2.synthNewBank(JykimLaunchpadMk2.bankSession, x, y, [
						\busSendA, JykimLaunchpadMk2.busSendA,
						\busSendB, JykimLaunchpadMk2.busSendB,
						\busMaster, JykimLaunchpadMk2.busMaster,
						\gate, 1,
						\noteOffset, 32
					]);
				});
				JykimLaunchpadMk2.registerOffBankNote(JykimLaunchpadMk2.bankSession, x, y, {
					JykimLaunchpadMk2.synthSet(x, y, [
						\gate, 0
					]);
				});
			};
		};


		//////////////// Init Done ////////////////////
		"Launchpad init done".postln;
		JykimLaunchpadMk2.offColorControl(
			JykimLaunchpadMk2.arrowUpX,
			0,
		)
	}.play;

}
)

[0,2,4,5,7,9,11,12,14].do{|x| x+1;}
