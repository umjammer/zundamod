/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.mod.zundamod;

import java.util.Arrays;


import javax.speech.AudioException;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import com.mojang.text2speech.Narrator;


/**
 * VoiceVoxNarrator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-06-18 nsano initial version <br>
 */
public class VoiceVoxNarrator implements Narrator {

    private Synthesizer synthesizer;

    public VoiceVoxNarrator() throws InitializeException {
        try {
            EngineManager.registerEngineListFactory(vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory.class.getName());

            synthesizer = (Synthesizer) EngineManager.createEngine(vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode.DEFAULT);
            synthesizer.allocate();
            synthesizer.waitEngineState(Engine.ALLOCATED);
            synthesizer.resume();
            synthesizer.waitEngineState(Synthesizer.RESUMED);

            String voiceName = "ずんだもん(ノーマル)";
            Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
            synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
//            synthesizer.getSynthesizerProperties().setVolume(5);

            ZundaMod.LOGGER.info(synthesizer.getClass().getName());
        } catch (AudioException | EngineException | InterruptedException e) {
            ZundaMod.LOGGER.error(e.getMessage(), e);
            throw new InitializeException(e.getMessage(), e);
        }
    }

    @Override
    public void say(String msg, boolean interrupt) {
        if (interrupt) {
            synthesizer.cancel();
        } else {
            synthesizer.speak(msg, m -> ZundaMod.LOGGER.info(m.toString()));
        }
    }

    @Override
    public void clear() {
        synthesizer.cancelAll();
    }

    @Override
    public void destroy() {
        try {
            synthesizer.deallocate();
        } catch (AudioException | EngineException e) {
            ZundaMod.LOGGER.error(e.getMessage(), e);
        }
    }
}
