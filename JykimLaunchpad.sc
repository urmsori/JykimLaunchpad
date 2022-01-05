JykimLaunchpad {
	classvar <> padOut;
	classvar printVerbose;

	classvar onNoteHandlers;
	classvar offNoteHandlers;
	classvar noteColors;
	classvar noteModes;

	classvar onControlHandlers;
	classvar offControlHandlers;
	classvar controlColors;
	classvar controlModes;

	const <modeOnOff= 0;
	const <modeToggleOff = 1;
	const <modeToggleOn = 2;

	const <countPlayNoteX = 8;
	const <countPlayNoteY = 8;
	const <countConfigNoteX = 1;
	const <countConfigNoteY = 8;
	const <countNoteX = 10;
	const <countNoteY = 8;
	const <numMidiStart = 11;

	const <countControlX = 8;
	const <countControlY = 1;
	const <numControlStart = 104;

	const <grey = 2;
	const <white = 3;
	const <orange = 4;
	const <red = 5;
	const <yellow = 12;
	const <green = 16;


	*printVerboseOn{
		printVerbose = true;
	}

	*printVerboseOff{
		printVerbose = false;
	}

	*init {
		var midiSource, midiDest;

		this.printVerboseOff();

		onNoteHandlers = Array2D.new(countNoteX, countNoteY);
		offNoteHandlers = Array2D.new(countNoteX, countNoteY);
		noteColors = Array2D.new(countNoteX, countNoteY);
		noteModes = Array2D.new(countNoteX, countNoteY);

		onControlHandlers = Array2D.new(countControlX, countControlY);
		offControlHandlers = Array2D.new(countControlX, countControlY);
		controlColors = Array2D.new(countControlX, countControlY);
		controlModes = Array2D.new(countControlX, countControlY);

		countNoteX.do{
			|x|
			countNoteY.do{
				|y|
				noteModes[x,y] = modeOnOff;
			}
		};
		countControlX.do{
			|x|
			countControlY.do{
				|y|
				controlModes[x,y] = modeOnOff;
			}
		};

		MIDIClient.init;

		midiSource = MIDIClient.sources.detect{
			|a|
			a.name.contains("Launchpad")
		};

		if ( midiSource.notNil, {
			midiDest = MIDIClient.destinations.detect{
				|a|
				a.name.contains("Launchpad")
			};

			MIDIIn.connect(0, midiSource.uid);

			padOut = MIDIOut(0, midiDest.uid);
			MIDIOut.connect(0, midiDest.uid);

			MIDIdef.noteOn(\JykimLaunchpadNoteOn, {
				|veloc, num, chan, src|
				this.noteOnOffHandler(veloc, num, chan, src, onNoteHandlers, true);
			});
			MIDIdef.noteOff(\JykimLaunchpadNoteOff, {
				|veloc, num, chan, src|
				this.noteOnOffHandler(veloc, num, chan, src, offNoteHandlers, false);
			});
			MIDIdef.cc(\JykimLaunchpadCC, {
				|veloc, num, chan, src|
				if (veloc > 0, {
					this.controlOnOffHandler(veloc, num, chan, src, true);
				},{
					this.controlOnOffHandler(veloc, num, chan, src, false);
				})
			});

			this.setAllColor(this.red, this.grey, this.grey);
			this.offAllColor();

			(4..7).do{
				|controlX|
				this.setControlToggleMode(controlX, 0);
			};

			countConfigNoteY.do{
				|configNoteY|
				this.setConfigNoteToggleMode(0, configNoteY);
			};


			midiSource.name.post;
			" is connected".postln;
		}, {
			"Launchpad Not available".postln;
		});
	}

	*noteOnOffHandler{
		|veloc, num, chan, src, handlers, isOn|
		var index, x, y, handler, color;

		if ( printVerbose, {
			[veloc, num, chan, src].postln;
		});

		index = num - numMidiStart;
		x = index % countNoteX;
		y = index / countNoteX;
		y = y.asInteger();

		this.modeRouter(x, y, isOn, noteModes, onNoteHandlers, offNoteHandlers, {
			|x, y, color|
			this.onNoteColor(x, y, color);
		}, {
			|x, y|
			this.offNoteColor(x, y);
		}, noteColors);
	}

	*controlOnOffHandler{
		|veloc, num, chan, src, isOn|
		var index, x, y;

		if ( printVerbose, {
			[veloc, num, chan, src].postln;
		});

		index = num - numControlStart;
		x = index % countControlX;
		y = index / countControlX;
		y = y.asInteger();

		this.modeRouter(x, y, isOn, controlModes, onControlHandlers, offControlHandlers, {
			|x, y, color|
			this.onControlColor(x,y,color);
		}, {
			|x, y|
			this.offControlColor(x, y);
		}, controlColors);
	}

	*modeRouter{
		|x, y, isOn, modes, onHandlers, offHandlers, onColorFunc, offColorFunc, colors|
		var mode;

		mode = modes[x, y];

		if ( printVerbose, {
			[x, y, mode].postln;
		});

		if ( mode == modeOnOff,{
			this.onOffChanger(x, y, isOn, onHandlers, offHandlers, onColorFunc, offColorFunc, colors);
		});

		if (isOn,{
			if ( mode == modeToggleOff, {
				this.onOffChanger(x, y, true, onHandlers, offHandlers, onColorFunc, offColorFunc, colors);
				modes[x, y] = modeToggleOn;
			});

			if ( mode == modeToggleOn, {
				this.onOffChanger(x, y, false, onHandlers, offHandlers, onColorFunc, offColorFunc, colors);
				modes[x, y] = modeToggleOff;
			});
		});
	}


	*onOffChanger{
		|x, y, isOn, onHandlers, offHandlers, onColorFunc, offColorFunc, colors|
		var handlers, handler, color;

		if (isOn, {
			handlers = onHandlers;
		},{
			handlers = offHandlers;
		});

		handler = handlers[x, y];
		if ( handler.notNil, {
			handler.value();
		});

		color = colors[x, y];
		if ( color.notNil, {
			if ( isOn, {
				onColorFunc.value(x, y, color);
			}, {
				offColorFunc.value(x, y);
			});
		});

	}

	*registerPlayNoteOn{
		|playNoteX, playNoteY, handler|
		this.registerPlayNoteOnOff(playNoteX, playNoteY, handler, onNoteHandlers);
	}

	*registerPlayNoteOff{
		|playNoteX, playNoteY, handler|
		this.registerPlayNoteOnOff(playNoteX, playNoteY, handler, offNoteHandlers);
	}

	*registerPlayNoteOnOff{
		|playNoteX, playNoteY, handler, noteHandlers|
		var x, y, noteHandler;

		x = playNoteX;
		y = playNoteY;

		this.registerOnOff(x, y, handler, noteHandlers);
	}

	*registerConfigNoteOn{
		|configNoteX, configNoteY, handler|
		this.registerConfigNoteOnOff(configNoteX, configNoteY, handler, onNoteHandlers);
	}

	*registerConfigNoteOff{
		|configNoteX, configNoteY, handler|
		this.registerConfigNoteOnOff(configNoteX, configNoteY, handler, offNoteHandlers);
	}

	*registerConfigNoteOnOff{
		|configNoteX, configNoteY, handler, noteHandlers|
		var x, y, noteHandler;

		x = configNoteX + countPlayNoteX;
		y = configNoteY;

		this.registerOnOff(x, y, handler, noteHandlers);
	}

	*registerControlOn{
		|controlX, controlY, handler|
		this.registerOnOff(controlX, controlY, handler, onControlHandlers);
	}
	*registerControlOff{
		|controlX, controlY, handler|
		this.registerOnOff(controlX, controlY, handler, offControlHandlers);
	}

	*registerOnOff{
		|x, y, handler, noteHandlers|
		var noteHandler;

		noteHandler = noteHandlers[x,y];
		if (noteHandler.notNil, {
			noteHandler.free;
		});

		noteHandlers[x,y] = handler;
	}

	*setPlayNoteColor{
		|playNoteX, playNoteY, color|
		var x, y;

		x = playNoteX;
		y = playNoteY;
		noteColors[x,y] = color;
	}

	*setConfigNoteColor{
		|configNoteX, configNoteY, color|
		var x, y;

		x = configNoteX + countPlayNoteX;
		y = configNoteY;
		noteColors[x,y] = color;
	}

	*setControlColor{
		|controlX, controlY, color|
		controlColors[controlX,controlY] = color;
	}

	*setAllColor{
		|playNoteColor, configNoteColor, controlColor|
		countPlayNoteX.do{
			|x|
			countPlayNoteY.do{
				|y|
				this.setPlayNoteColor(x,y, playNoteColor);
			}
		};
		countConfigNoteX.do{
			|x|
			countConfigNoteY.do{
				|y|
				this.setConfigNoteColor(x,y, configNoteColor);
			}
		};
		countControlX.do{
			|x|
			countControlY.do{
				|y|
				this.setControlColor(x,y, controlColor);
			}
		};
	}

	*offAllColor{
		countNoteX.do{
			|x|
			countNoteY.do{
				|y|
				this.offNoteColor(x, y);
			}
		};
		countControlX.do{
			|x|
			countControlY.do{
				|y|
				this.offControlColor(x, y);
			}
		};
	}

	*onNoteColorRaw{
		|num, color|
		padOut.noteOn(0, num, color);
	}
	*onNoteColor{
		|x, y, color|
		var num;
		num = (y * countNoteX) + x + numMidiStart;
		this.onNoteColorRaw(num, color);
	}
	*onPlayNoteColor{
		|playNoteX, playNoteY, color|
		var x, y;
		x = playNoteX;
		y = playNoteY;
		this.onNoteColorRaw(x, y, color);
	}
	*onConfigNoteColor{
		|configNoteX, configNoteY, color|
		var x, y;
		x = configNoteX + countPlayNoteX;
		y = configNoteY;
		this.onNoteColorRaw(x, y, color);
	}

	*offNoteColorRaw{
		|num|
		padOut.noteOff(0, num, 0);
	}
	*offNoteColor{
		|x, y|
		var num;
		num = (y * countNoteX) + x + numMidiStart;
		this.offNoteColorRaw(num);
	}
	*offPlayNoteColor{
		|playNoteX, playNoteY|
		var x, y;
		x = playNoteX;
		y = playNoteY;
		this.offNoteColorRaw(x, y);
	}
	*offConfigNoteColor{
		|configNoteX, configNoteY|
		var x, y;
		x = configNoteX + countPlayNoteX;
		y = configNoteY;
		this.offNoteColorRaw(x, y);
	}

	*onControlColorRaw{
		|num, color|
		padOut.control(0, num, color);
	}
	*onControlColor{
		|controlX, controlY, color|
		var num;
		num = (controlY * countControlX) + controlX + numControlStart;
		this.onControlColorRaw(num, color);
	}

	*offControlColorRaw{
		|num|
		padOut.control(0, num, 0);
	}
	*offControlColor{
		|controlX, controlY|
		var num;
		num = (controlY * countControlX) + controlX + numControlStart;
		this.offControlColorRaw(num);
	}

	*setNoteToggleMode{
		|x, y|
		noteModes[x, y] = modeToggleOff;
	}
	*setPlayNoteToggleMode{
		|playNoteX, playNoteY|
		var x, y;
		x = playNoteX;
		y = playNoteY;
		this.setNoteToggleMode(x, y);
	}
	*setConfigNoteToggleMode{
		|configNoteX, configNoteY, color|
		var x, y;
		x = configNoteX + countPlayNoteX;
		y = configNoteY;
		this.setNoteToggleMode(x, y);
	}
	*setControlToggleMode{
		|controlX, controlY|
		controlModes[controlX, controlY] = modeToggleOff;
	}

	*midicpsPlayNote{
		|playNoteX, playNoteY, offset = 11|
		var num;
		num = (playNoteY * countPlayNoteX) + playNoteX;
		num = num + offset;
		^num.midicps;
	}
}