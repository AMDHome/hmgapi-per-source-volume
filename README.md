# Per-source volume for HMG-API

Restores individual volume levels for each audio source (FM, AM, Bluetooth, Android Auto, CarPlay…).

A mod for [HMG-API](https://github.com/Open-HMG/HMG-API), the mod manager for Hyundai, Kia and Genesis Gen5W head units. It brings back separate volume levels for each media source: FM, AM, SiriusXM, Bluetooth audio, Android Auto, CarPlay, USB and so on. Each one remembers its own level, across source changes and restarts.

Newer head unit software (for example the US 260507 build) links every media source to one level: turning the volume down on Bluetooth turns it down on the radio too. Older software kept them apart. This mod puts that back.

**Provided as is.** I used Claude heavily in the making of this mod; it did most of the heavy lifting. I tested the mod on a 2022 Kia Niro EV (DE22EV). I cannot guarantee that it will work on your car. I also can't guarantee that this mod will be actively maintained (I'm quite lazy). If you'd like to take it over, open an issue.

## What it does

Turn the mod on or off in the HMG-API app. Changes apply at the next restart of the head unit.

| Setting | Default | What |
|---|---|---|
| Log at start-up | on | Display a one line log in HMG-API's log stating whether the volume groups were found and split. Problems are logged either way. |

Terminology: Slots ≈ Sources

The audio service (`MicomAudioService`) sorts volume slots into types, and every media source's slot is the same type (media). On software using the "new volume group" mode, a volume change on one media slot is copied to every other media slot and saved for all of them. The mod gives each media slot a type of its own, with only itself in it, the same way the stock software already handles navigation warnings, CarPlay alerts and beeps. Copying, saving and the check that sends the level to the amplifier then each act on one source.

It's a change to two tables (`MicomAudioManagerConstant.VolumeGroupTable` and `mapVolumeGroup`), made once in the zygote at start-up. It uses no hook points and changes no files.

After installing, you will need to set each source's volume once. Until then a source comes back at whatever was last saved for it (stock default, or the level from before the mod).

## Supported cars

| Car | Region | Head unit software | Notes |
|---|---|---|---|
| Kia Niro EV (`DE22EV`) | USA | 260507 |  |

Other Gen5W cars may work. The audio service seems to be shared to some extent. The mod doesn't check which car or software it's on. If the tables are missing, it does nothing; but if the tables exist and the software handles them differently, it could misbehave. Read "If something goes wrong" before trying it on another car.

## If something goes wrong

- **The head unit restarts when you turn the volume knob, or the knob stops changing the volume:** This means your car handles volume significantly differently from mine, and the mod won't work for you as is. Turn the mod off or uninstall it in the HMG-API app, and restart to restore stock functionality. You can open an issue if you want, but I make no promises.
- **The head unit restarts during start-up:** after 3 failed starts, HMG-API's boot guard starts it without any mods. Remove this mod, then turn HMG-API's hooks back on ([Troubleshooting](https://github.com/Open-HMG/HMG-API/wiki/Troubleshooting#the-head-unit-restarted-over-and-over)). This shouldn't happen but it's here in case something I didn't think of comes up.

The mod writes nothing to `/system`. Removing it and restarting the head unit gives back the stock behaviour; the per-source levels stay in the head unit's settings until the stock software links them again on the next volume change.

To check it's working: After start-up, HMG-API's log shows `Split 35 media volume groups out of type 0 (types 1000-1034)` (the count depends on the car). That means the mod ran and has remapped your sources' slot types. Try setting the volume on 2 different sources to see if the different values stick. Also check if all your different volume levels load properly after restarting the head unit. If so, then you're in the clear. Congrats.

## Installing

Install HMG-API first ([Installing HMG-API](https://github.com/Open-HMG/HMG-API/wiki/Installing-HMG-API)). Then, from this repository's releases:

- **USB stick**: put `per-source-volume-<version>.zip` in `HMGAPI/updates/` on the stick, then HMG-API app → Check USB updates → the mod → Apply changes.
- **NEMESIS**: unzip `per-source-volume-<version>-nemesis.zip` onto the top level of a USB stick and confirm the install in NEMESIS Manager.
- **adb**: `adb push` the zip, then `hmgapid engine install <zip>`, `plan`, `apply`.

[Installing and removing mods](https://github.com/Open-HMG/HMG-API/wiki/Installing-and-removing-mods) has the steps. Restart the head unit afterwards.

## Building

```
per-source-volume/   the mod: mod.json, settings.json, src/ (hooks.jar)
```

With HMG-API's mod SDK unpacked in `$HMGAPI_SDK` and the Android SDK in `ANDROID_HOME`
(any JDK that supports `--release 8`, e.g. 17 or 21):

```
$HMGAPI_SDK/bin/build-mod.sh per-source-volume out/build/per-source-volume
```
```
$HMGAPI_SDK/bin/mkmodpkg.sh per-source-volume <key>
```

How mods build outside HMG-API: HMG-API's `docs/mod-reference.md`, "Building a mod
outside this repo".

## Licence

AGPL-3.0 ([LICENSE](LICENSE)).
