# jykim Launchpad Supercollider Project
This project starts to control Novation Launchpad MK2 using Supercollider
It is for my personal use, and anyone can use or copy.

# Example
```
s.boot;

(
JykimLaunchpad.init;
// JykimLaunchpad.printVerboseOn();

JykimLaunchpad.countPlayNoteX.do{
	|x|
	JykimLaunchpad.countPlayNoteY.do{
		|y|
		JykimLaunchpad.registerPlayNoteOn(x,y,{
			{
			var sig, env, cps;
			cps = JykimLaunchpad.midicpsPlayNote(x, y, 30);
			sig = LFTri.ar(cps)!2;
			env = EnvGen.kr(Env.perc, doneAction:2);
			sig = sig * env * 127.linexp(1,127,0.01,0.3);
			}.play;
		});
	}
}
)
```