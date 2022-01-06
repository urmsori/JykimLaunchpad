JykimLaunchpad {
	classvar <> padOut;
	classvar printVerbose;

	classvar noteOnHandlers;
	classvar noteOffHandlers;
	classvar noteColors;
	classvar noteModes;
	classvar noteExclusives;

	classvar controlOnHandlers;
	classvar controlOffHandlers;
	classvar controlColors;
	classvar controlModes;
	classvar controlExclusives;

	const midiTypeNote = 0;
	const midiTypeControl = 1;

	const modeOnOff= 0;
	const modeToggleOff = 1;
	const modeToggleOn = 2;

	const <countPlayNoteX = 8;
	const <countPlayNoteY = 8;
	const <countConfigNoteX = 1;
	const <countConfigNoteY = 8;
	const <countNoteX = 10;
	const <countNoteY = 8;
	const <numNoteStart = 11;

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

		noteOnHandlers = Array2D.new(countNoteX, countNoteY);
		noteOffHandlers = Array2D.new(countNoteX, countNoteY);
		noteColors = Array2D.new(countNoteX, countNoteY);
		noteModes = Array2D.new(countNoteX, countNoteY);
		noteExclusives = Array2D.new(countNoteX, countNoteY);

		controlOnHandlers = Array2D.new(countControlX, countControlY);
		controlOffHandlers = Array2D.new(countControlX, countControlY);
		controlColors = Array2D.new(countControlX, countControlY);
		controlModes = Array2D.new(countControlX, countControlY);
		controlExclusives = Array2D.new(countControlX, countControlY);

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
				this.prNoteOnOffHandler(veloc, num, chan, src, true);
			});
			MIDIdef.noteOff(\JykimLaunchpadNoteOff, {
				|veloc, num, chan, src|
				this.prNoteOnOffHandler(veloc, num, chan, src, false);
			});
			MIDIdef.cc(\JykimLaunchpadCC, {
				|veloc, num, chan, src|
				if (veloc > 0, {
					this.prControlOnOffHandler(veloc, num, chan, src, true);
				},{
					this.prControlOnOffHandler(veloc, num, chan, src, false);
				})
			});

			this.setColorAll(this.red, this.grey, this.grey);
			this.offColorAll();

			midiSource.name.post;
			" is connected".postln;
			^true
		}, {
			"Launchpad Not available".postln;
			^false
		});
	}

	////////////////////////////////////////////////////////////////////////////

	*num2NoteXY{
		|num|
		var index, x, y;

		index = num - numNoteStart;
		x = index % countNoteX;
		y = index / countNoteX;
		y = y.asInteger();

		^[x, y];
	}

	*num2ControlXY{
		|num|
		var index, x, y;

		index = num - numControlStart;
		x = index % countControlX;
		y = index / countControlX;
		y = y.asInteger();

		^[x, y];
	}

	*controlXY2Num{
		|controlX, controlY|
		var num;
		num = (controlY * countControlX) + controlX + numControlStart;
		^num;
	}

	*noteXY2Num{
		|x, y|
		var num;
		num = (y * countNoteX) + x + numNoteStart;
		^num;
	}

	*playNoteXY2NoteXY{
		|playNoteX, playNoteY|
		var x, y;
		x = playNoteX;
		y = playNoteY;
		^[x, y];
	}

	*configNoteXY2NoteXY{
		|configNoteX, configNoteY|
		var x, y;
		x = configNoteX + countPlayNoteX;
		y = configNoteY;
		^[x, y];
	}

	*playNoteXY2Num{
		|playNoteX, playNoteY|
		var xy, num;
		xy = this.playNoteXY2NoteXY(playNoteX, playNoteY);
		num = this.noteXY2Num(xy[0], xy[1]);
		^num;
	}

	*configNoteXY2Num{
		|configNoteX, configNoteY|
		var xy, num;
		xy = this.playNoteXY2NoteXY(configNoteX, configNoteY);
		num = this.noteXY2Num(xy[0], xy[1]);
		^num;
	}

	////////////////////////////////////////////////////////////////////////////

	*prNoteOnOffHandler{
		|veloc, num, chan, src, isOn|
		var xy, x, y;

		xy = this.num2NoteXY(num);
		x = xy[0];
		y = xy[1];

		this.prModeRouter(x, y, midiTypeNote, isOn);
	}

	*prControlOnOffHandler{
		|veloc, num, chan, src, isOn|
		var xy, x, y;

		if ( printVerbose, {
			[veloc, num, chan, src].postln;
		});

		xy = this.num2ControlXY(num);
		x = xy[0];
		y = xy[1];

		this.prModeRouter(x, y, midiTypeControl, isOn);
	}

	*prModeRouter{
		|x, y, midiType, isOn|
		var modes, mode;

		modes = switch (midiType,
			midiTypeNote,   { noteModes },
			midiTypeControl, { controlModes }
		);
		mode = modes[x, y];

		if ( printVerbose, {
			[x, y, mode].postln;
		});

		if ( mode == modeOnOff,{
			this.prOnOffChanger(x, y, midiType, isOn);
		});

		if (isOn,{
			if ( mode == modeToggleOff, {
				this.prOffExclusive(x, y, midiType);
				this.prOnOffChanger(x, y, midiType, true);
				modes[x, y] = modeToggleOn;
			});

			if ( mode == modeToggleOn, {
				this.prOnOffChanger(x, y, midiType, false);
				modes[x, y] = modeToggleOff;
			});
		});
	}


	*prOnOffChanger{
		|x, y, midiType, isOn|
		var handlers, handler, color;

		if (isOn, {
			handlers = switch (midiType,
				midiTypeNote,   { noteOnHandlers },
				midiTypeControl, { controlOnHandlers }
			);
		},{
			handlers = switch (midiType,
				midiTypeNote,   { noteOffHandlers },
				midiTypeControl, { controlOffHandlers }
			);
		});

		handler = handlers[x, y];
		if ( handler.notNil, {
			handler.value();
		});

		color = switch (midiType,
			midiTypeNote,   { noteColors[x, y] },
			midiTypeControl, { controlColors[x, y] }
		);
		if ( color.notNil, {
			if ( isOn, {
				switch (midiType,
					midiTypeNote,   { this.onColorNote(x,y,color); },
					midiTypeControl, { this.onColorControl(x,y,color); }
				);
			}, {
				switch (midiType,
					midiTypeNote,   { this.offColorNote(x,y); },
					midiTypeControl, { this.offColorControl(x,y); }
				);
			});
		});
	}

	*prOffExclusive{
		|x, y, midiType|
		var modes, exclusive;

		exclusive = switch (midiType,
			midiTypeNote,   { noteExclusives[x, y] },
			midiTypeControl, { controlExclusives[x, y] }
		);

		if (exclusive.isNil,{
			^nil;
		});

		modes = switch (midiType,
			midiTypeNote,   { noteModes },
			midiTypeControl, { controlModes }
		);

		exclusive.do{
			|xy|
			var mode, xOther, yOther;
			xOther = xy[0];
			yOther = xy[1];
			if((xOther != x) || (yOther != y),{
				mode = modes[xOther, yOther];
				if(mode == modeToggleOn, {
					this.prModeRouter(xOther, yOther, midiType, true);
				});
			});
		};
	}

	////////////////////////////////////////////////////////////////////////////

	*registerOnPlayNote{
		|playNoteX, playNoteY, handler|
		this.prRegisterOnOffPlayNote(playNoteX, playNoteY, handler, noteOnHandlers);
	}
	*freeOnPlayNote{
		|playNoteX, playNoteY|
		this.prRegisterOnOffPlayNote(playNoteX, playNoteY, nil, noteOnHandlers);
	}

	*registerOffPlayNote{
		|playNoteX, playNoteY, handler|
		this.prRegisterOnOffPlayNote(playNoteX, playNoteY, handler, noteOffHandlers);
	}
	*freeOffPlayNote{
		|playNoteX, playNoteY|
		this.prRegisterOnOffPlayNote(playNoteX, playNoteY, nil, noteOffHandlers);
	}

	*prRegisterOnOffPlayNote{
		|playNoteX, playNoteY, handler, noteHandlers|
		var xy, noteHandler;
		xy = this.playNoteXY2NoteXY(playNoteX, playNoteY);
		this.prRegisterOnOff(xy[0], xy[1], handler, noteHandlers);
	}

	*registerOnConfigNote{
		|configNoteX, configNoteY, handler|
		this.prRegisterOnOffConfigNote(configNoteX, configNoteY, handler, noteOnHandlers);
	}
	*freeOnConfigNote{
		|configNoteX, configNoteY|
		this.prRegisterOnOffConfigNote(configNoteX, configNoteY, nil, noteOnHandlers);
	}

	*registerOffConfigNote{
		|configNoteX, configNoteY, handler|
		this.prRegisterOnOffConfigNote(configNoteX, configNoteY, handler, noteOffHandlers);
	}
	*freeOffConfigNote{
		|configNoteX, configNoteY|
		this.prRegisterOnOffConfigNote(configNoteX, configNoteY, nil, noteOffHandlers);
	}

	*prRegisterOnOffConfigNote{
		|configNoteX, configNoteY, handler, noteHandlers|
		var xy, noteHandler;
		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		this.prRegisterOnOff(xy[0], xy[1], handler, noteHandlers);
	}

	*registerOnControl{
		|controlX, controlY, handler|
		this.prRegisterOnOff(controlX, controlY, handler, controlOnHandlers);
	}
	*freeOnControl{
		|controlX, controlY|
		this.prRegisterOnOff(controlX, controlY, nil, controlOnHandlers);
	}

	*registerOffControl{
		|controlX, controlY, handler|
		this.prRegisterOnOff(controlX, controlY, handler, controlOffHandlers);
	}
	*freeOffControl{
		|controlX, controlY, handler|
		this.prRegisterOnOff(controlX, controlY, nil, controlOffHandlers);
	}

	*prRegisterOnOff{
		|x, y, handler, noteHandlers|
		var noteHandler;

		noteHandler = noteHandlers[x,y];
		if (noteHandler.notNil, {
			noteHandler.free;
		});

		noteHandlers[x,y] = handler;
	}

	////////////////////////////////////////////////////////////////////////////

	*setColorPlayNote{
		|playNoteX, playNoteY, color|
		var xy;
		xy = this.playNoteXY2NoteXY(playNoteX, playNoteY);
		noteColors[xy[0], xy[1]] = color;
	}

	*setColorConfigNote{
		|configNoteX, configNoteY, color|
		var xy;
		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		noteColors[xy[0], xy[1]] = color;
	}

	*setColorControl{
		|controlX, controlY, color|
		controlColors[controlX,controlY] = color;
	}

	*setColorAll{
		|playNoteColor, configNoteColor, controlColor|
		countPlayNoteX.do{
			|x|
			countPlayNoteY.do{
				|y|
				this.setColorPlayNote(x,y, playNoteColor);
			}
		};
		countConfigNoteX.do{
			|x|
			countConfigNoteY.do{
				|y|
				this.setColorConfigNote(x,y, configNoteColor);
			}
		};
		countControlX.do{
			|x|
			countControlY.do{
				|y|
				this.setColorControl(x,y, controlColor);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////

	*offColorAll{
		countNoteX.do{
			|x|
			countNoteY.do{
				|y|
				this.offColorNote(x, y);
			}
		};
		countControlX.do{
			|x|
			countControlY.do{
				|y|
				this.offColorControl(x, y);
			}
		};
	}

	*prOnColorNote{
		|num, color|
		padOut.noteOn(0, num, color);
	}
	*onColorNote{
		|x, y, color|
		var num;
		num = this.noteXY2Num(x, y);
		this.prOnColorNote(num, color);
	}
	*onColorPlayNote{
		|playNoteX, playNoteY, color|
		var num;
		num = this.playNoteXY2Num(playNoteX, playNoteY);
		this.prOnColorNote(num, color);
	}
	*onColorConfigNote{
		|configNoteX, configNoteY, color|
		var num;
		num = this.configNoteXY2Num(configNoteX, configNoteY);
		this.prOnColorNote(num, color);
	}

	*prOffColorNote{
		|num|
		padOut.noteOff(0, num, 0);
	}
	*offColorNote{
		|x, y|
		var num;
		num = this.noteXY2Num(x, y);
		this.prOffColorNote(num);
	}
	*offColorPlayNote{
		|playNoteX, playNoteY|
		var num;
		num = this.playNoteXY2Num(playNoteX, playNoteY);
		this.prOffColorNote(num);
	}
	*offColorConfigNote{
		|configNoteX, configNoteY|
		var num;
		num = this.configNoteXY2Num(configNoteX, configNoteY);
		this.prOffColorNote(num);
	}

	*prOnColorControl{
		|num, color|
		padOut.control(0, num, color);
	}
	*onColorControl{
		|controlX, controlY, color|
		var num;
		num = this.controlXY2Num(controlX, controlY);
		this.prOnColorControl(num, color);
	}

	*prOffColorControl{
		|num|
		padOut.control(0, num, 0);
	}
	*offColorControl{
		|controlX, controlY|
		var num;
		num = this.controlXY2Num(controlX, controlY);
		this.prOffColorControl(num);
	}

	////////////////////////////////////////////////////////////////////////////

	*setToggleModeNote{
		|x, y|
		noteModes[x, y] = modeToggleOff;
	}
	*setToggleModePlayNote{
		|playNoteX, playNoteY|
		var xy;
		xy = this.playNoteXY2NoteXY(playNoteX, playNoteY);
		this.setToggleModeNote(xy[0], xy[1]);
	}
	*setToggleModeConfigNote{
		|configNoteX, configNoteY, color|
		var xy;
		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		this.setToggleModeNote(xy[0], xy[1]);
	}
	*setToggleModeControl{
		|controlX, controlY|
		controlModes[controlX, controlY] = modeToggleOff;
	}

	////////////////////////////////////////////////////////////////////////////

	*setExclusiveConfigNote{
		|configNoteXYs|
		this.prSetUnsetExclusiveConfigNote(configNoteXYs, true);
	}
	*unsetExclusiveConfigNote{
		|configNoteXYs|
		this.prSetUnsetExclusiveConfigNote(configNoteXYs, false);
	}
	*prSetUnsetExclusiveConfigNote{
		|configNoteXYs, isSet|
		var xys;
		xys = configNoteXYs.collect{
			|configNoteXY|
			this.configNoteXY2NoteXY(configNoteXY[0], configNoteXY[1]);
		};
		this.prSetUnsetExclusive(xys, noteExclusives, isSet);
	}

	*setExclusivePlayNote{
		|playNoteXYs|
		this.prSetUnsetExclusivePlayNote(playNoteXYs, true);
	}
	*unsetExclusivePlayNote{
		|playNoteXYs|
		this.prSetUnsetExclusivePlayNote(playNoteXYs, false);
	}
	*prSetUnsetExclusivePlayNote{
		|playNoteXYs, isSet|
		var xys;
		xys = playNoteXYs.collect{
			|playNoteXY|
			this.playNoteXY2NoteXY(playNoteXY[0], playNoteXY[1]);
		};
		this.prSetUnsetExclusive(xys, noteExclusives, isSet);
	}

	*setExclusiveControl{
		|controlXYs|
		this.prSetUnsetExclusiveControl(controlXYs, true);
	}
	*unsetExclusiveControl{
		|controlXYs|
		this.prSetUnsetExclusiveControl(controlXYs, false);
	}
	*prSetUnsetExclusiveControl{
		|controlXYs, isSet|
		this.prSetUnsetExclusive(controlXYs, controlExclusives, isSet);
	}

	*prSetUnsetExclusive{
		|xys, exclusives, isSet|
		var setValue;
		if (isSet, {
			setValue = xys;
		},{
			setValue = nil;
		});
		xys.do{
			|xy|
			exclusives[xy[0], xy[1]] = setValue;
		}
	}

	////////////////////////////////////////////////////////////////////////////

	*midicpsPlayNote{
		|playNoteX, playNoteY, offset = 0|
		var num;
		num = this.playNoteXY2Num(playNoteX, playNoteY);
		num = num + offset;
		^num.midicps;
	}
}

JykimNumberDisplay {
	var <numMaps;
	var <allMap;

	*new {
		|offsetX, offsetY|
		var numMapsRaw, numMapsResult, allMapRaw, allMapResult;

		numMapsRaw = this.createNumMaps();
		numMapsResult = numMapsRaw.collect{
			|numMap|
			numMap.collect{
				|xy|
				[xy[0] + offsetX, xy[1] + offsetY];
			};
		};

		allMapRaw = this.createAllMap();
		allMapResult = allMapRaw.collect{
			|xy|
			[xy[0] + offsetX, xy[1] + offsetY];
		};

		^super.newCopyArgs(numMapsResult, allMapResult);
	}

	*createAllMap{
		^[
			[0,0],[0,1],[0,2],[0,3],[0,4],
			[1,0],[1,1],[1,2],[1,3],[1,4],
			[2,0],[2,1],[2,2],[2,3],[2,4]
		];
	}

	*createNumMaps{
		^[
			[ // 0
				[0,0],[0,1],[0,2],[0,3],[0,4],
				[1, 4],
				[2,0],[2,1],[2,2],[2,3],[2,4],
				[1, 0]
			],
			[ // 1
				[2,0],[2,1],[2,2],[2,3],[2,4]
			],
			[ // 2
				[0,4],[1,4],[2,4],
				[2,3],
				[0,2],[1,2],[2,2],
				[0,1],
				[0,0],[1,0],[2,0]
			],
			[ // 3
				[0,4],[1,4],[2,4],
				[2,3],
				[0,2],[1,2],[2,2],
				[2,1],
				[0,0],[1,0],[2,0]
			],
			[ // 4
				[0,4],[0,3],[0,2],
				[1,2],
				[2,4],[2,3],[2,2],[2,1],[2,0]
			],
			[ // 5
				[0,4],[1,4],[2,4],
				[0,3],
				[0,2],[1,2],[2,2],
				[2,1],
				[0,0],[1,0],[2,0]
			],
			[ // 6
				[0,0],[0,1],[0,2],[0,3],[0,4],
				[1,0],[1,2],
				[2,0],[2,1],[2,2]
			],
			[ // 7
				[0,2],[0,3],[0,4],
				[1,4],
				[2,4],[2,3],[2,2],[2,1],[2,0]
			],
			[ // 8
				[0,0],[0,1],[0,2],[0,3],[0,4],
				[1,0],[1,2],[1,4],
				[2,0],[2,1],[2,2],[2,3],[2,4]
			],
			[ // 9
				[0,2],[0,3],[0,4],
				[1,2],[1,4],
				[2,0],[2,1],[2,2],[2,3],[2,4]
			]
		];
	}
}

JykimLaunchpadMk2 : JykimLaunchpad{
	classvar <currentVolume;
	classvar <currentPan;
	classvar <currentSendA;

	classvar numDisp;
	classvar beforeMuteVolume;

	const volumeY = 7;
	const panY = 6;
	const sendAY = 5;
	const muteY = 2;

	const <volumeMin = 0;
	const <volumeMax = 9;

	const <panMin = 0;
	const <panMid = 4;
	const <panMax = 8;

	const <sendAMin = 0;
	const <sendAMax = 9;

	const numPadStartX = 5;
	const numPadStartY = 3;

	const arrowUpX = 0;
	const arrowDownX = 1;
	const arrowLeftX = 2;
	const arrowRightX = 3;

	*init {
		var result;
		result = super.init;

		if (result.not,{
			^false;
		});

		// NumberDisplay Object
		numDisp = JykimNumberDisplay(numPadStartX, numPadStartY);

		// Control Toggle
		(4..7).do{
			|controlX|
			this.setToggleModeControl(controlX, 0);
		};

		// Control Exclusive
		this.setExclusiveControl([[4, 0], [5, 0], [6, 0], [7, 0]]);

		// ConfigNote Toggle
		[0,2,4,5,6,7].do{
			|configNoteY|
			this.setToggleModeConfigNote(0, configNoteY);
		};

		// ConfigNote Exclusive
		this.setExclusiveConfigNote([[0, 7], [0, 6], [0, 5], [0, 4], [0, 2]]);

		// Volume
		currentVolume = volumeMax;
		this.registerOnConfigNote(0, volumeY, {
			this.prVolumeOnHandler(0, volumeY);
		});
		this.registerOffConfigNote(0, volumeY, {
			this.prVolumeOffHandler(0, volumeY);
		});

		// Pan
		currentPan = panMid;
		this.registerOnConfigNote(0, panY, {
			this.prPanOnHandler(0, panY);
		});
		this.registerOffConfigNote(0, panY, {
			this.prPanOffHandler(0, panY);
		});

		// SendA (ReverbA)
		currentSendA = sendAMin;
		this.registerOnConfigNote(0, sendAY, {
			this.prSendAOnHandler(0, sendAY);
		});
		this.registerOffConfigNote(0, sendAY, {
			this.prSendAOffHandler(0, sendAY);
		});

		// Mute
		this.registerOnConfigNote(0, muteY, {
			this.prMuteOnHandler();
		});
		this.registerOffConfigNote(0, muteY, {
			this.prMuteOffHandler();
		});


		^result;
	}

	////////////////////////////////////////////////////////////////////////////

	*offNumPad{
		numDisp.allMap.do{
			|xy|
			this.offColorPlayNote(xy[0], xy[1]);
		};
	}

	*onNumPad{
		|num, color = 2|
		var numMap;

		this.offNumPad();

		if((num < 0) || (num > 9), {
			^false;
		});

		numMap = numDisp.numMaps[num];
		numMap.do{
			|xy|
			this.onColorPlayNote(xy[0], xy[1], color);
		};
	}

	////////////////////////////////////////////////////////////////////////////

	*prVolumeOnHandler{
		|configNoteX, configNoteY|
		var xy, color;

		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		color = noteColors[xy[0], xy[1]];

		this.onNumPad(currentVolume, color);

		this.registerOnControl(arrowUpX, 0, {
			if (currentVolume < volumeMax, {
				currentVolume = currentVolume + 1;
			});
			this.onNumPad(currentVolume, color);
		});
		this.registerOnControl(arrowDownX, 0, {
			if (currentVolume > volumeMin,{
				currentVolume = currentVolume - 1;
			});
			this.onNumPad(currentVolume, color);
		});
	}

	*prVolumeOffHandler{
		|configNoteX, configNoteY|
		this.offNumPad();
		this.freeOnControl(arrowUpX, 0);
		this.freeOnControl(arrowDownX, 0);
	}

	////////////////////////////////////////////////////////////////////////////

	*prPanOnHandler{
		|configNoteX, configNoteY|
		var xy, color;

		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		color = noteColors[xy[0], xy[1]];

		this.onNumPad(currentPan, color);

		this.registerOnControl(arrowRightX, 0, {
			if (currentPan < panMax, {
				currentPan = currentPan + 1;
			});
			this.onNumPad(currentPan, color);
		});
		this.registerOnControl(arrowLeftX, 0, {
			if (currentPan > panMin,{
				currentPan = currentPan - 1;
			});
			this.onNumPad(currentPan, color);
		});
	}

	*prPanOffHandler{
		|configNoteX, configNoteY|
		this.offNumPad();
		this.freeOnControl(arrowRightX, 0);
		this.freeOnControl(arrowLeftX, 0);
	}

	////////////////////////////////////////////////////////////////////////////

	*prSendAOnHandler{
		|configNoteX, configNoteY|
		var xy, color;

		xy = this.configNoteXY2NoteXY(configNoteX, configNoteY);
		color = noteColors[xy[0], xy[1]];

		this.onNumPad(currentSendA, color);

		this.registerOnControl(arrowUpX, 0, {
			if (currentSendA < sendAMax, {
				currentSendA = currentSendA + 1;
			});
			this.onNumPad(currentSendA, color);
		});
		this.registerOnControl(arrowDownX, 0, {
			if (currentSendA > sendAMin,{
				currentSendA = currentSendA - 1;
			});
			this.onNumPad(currentSendA, color);
		});
	}

	*prSendAOffHandler{
		|configNoteX, configNoteY|
		this.offNumPad();
		this.freeOnControl(arrowUpX, 0);
		this.freeOnControl(arrowDownX, 0);
	}

	////////////////////////////////////////////////////////////////////////////

	*prMuteOnHandler{
		beforeMuteVolume = currentVolume;
		currentVolume = 0;
	}

	*prMuteOffHandler{
		currentVolume = beforeMuteVolume;
	}
}

