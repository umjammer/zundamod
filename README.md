[![Fabric](https://img.shields.io/badge/Mod_Loader-Fabric-blue)](https://fabricmc.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-green)](https://www.minecraft.net/)
[![Java CI](https://github.com/umjammer/zundamod/actions/workflows/build.yml/badge.svg)](https://github.com/umjammer/zundamod/actions/workflows/build.yml)
[![GitHub Release Assets](https://github.com/umjammer/zundamod/actions/workflows/publish.yml/badge.svg)](https://github.com/umjammer/zundamod/actions/workflows/publish.yml)
![Java](https://img.shields.io/badge/Java-25-b07219)

# ZundaMod

<img src="https://user-images.githubusercontent.com/493908/216399074-bbdd72f8-333b-4125-9e4d-7e44aeeb248e.png" width="150" alt="ずんだもん" /><sub><a href="https://seiga.nicovideo.jp/seiga/im10788496?ref=pc_watch_description"> © 坂本アヒル</a></sub>

A replacement of narration mod for Minecraft with Fabric launcher.

see also [Modrinth](https://modrinth.com/mod/zundamod)

## Usage

run [VoiceVox.app](https://voicevox.hiroshiba.jp/) before Minecraft starting

make sure `Opening/Options.../Accessibility Settings.../Narrator:` is NOT `OFF`!

~~for [better japanese input](https://github.com/nakanotti/CocoaInput)~~

### property file

- `${MINECRAFT}/config/zundamod.properties`
```properties
models=3

class.1=vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode
voice.1=ずんだもん(ノーマル)
account.1=$$
params.1=rate:110,pitch:15,volume:75

class.2=vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode
voice.2=ずんだもん(ノーマル)
account.2=@@
params.2=rate:120,pitch:30,volume:75

class.3=vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode
voice.3=春日部つむぎ(ノーマル)
account.3=umjammer
params.3=rate:5,volume:100
```

- models ... number of voice settings
- class ... synthesizer class, available below

```
vavi.speech.aquestalk10.jsapi2.AquesTalk10EngineListFactory
vavi.speech.googlecloud.jsapi2.GoogleCloudEngineListFactory
vavi.speech.gyutan.jsapi2.GyutanEngineListFactory
vavi.speech.rococoa.jsapi2.RococoaEngineListFactory
vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory
vavi.speech.coeiroink.jsapi2.CoeiroInkEngineListFactory
vavi.speech.aivis.jsapi2.AivisEngineListFactory
vavi.speech.googlegenai.jsapi2.GoogleGenAITTSListFactory
vavi.speech.qwen3tts.jsapi2.Qwen3TtsEngineListFactory
```

- voice ... voice name, see each application
- account ... minecraft account, voice is changed for each account setting
  - special: `$$` system narration, `@@` self
- params ... params for voice, see each application

for more detail, see [vavi-speech2](https://github.com/umjammer/vavi-speech2)

## Depends

 * [Fabric Loader](https://fabricmc.net/develop/)
 * [Cloth Config API](https://modrinth.com/mod/cloth-config) (🚧)
 * [Mod Menu](https://modrinth.com/mod/modmenu) (🚧)

## TODO

 * configuration
   * url
   * voice
   * volume
   * speed
 * ~~diet dependencies jar more~~

## Warning

 * this mod is tested only on mac, and not tested well
 * this mod is experimental. don't ask me anything