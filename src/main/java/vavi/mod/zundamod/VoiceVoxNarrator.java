/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.mod.zundamod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.speech.AudioException;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.EngineManager;
import javax.speech.EngineStateException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.Minecraft;


/**
 * VoiceVoxNarrator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-06-18 nsano initial version <br>
 */
public class VoiceVoxNarrator implements Narrator {

    /** account: synthesizer */
    private final SequencedMap<String, Synthesizer> synthesizers = new LinkedHashMap<>();

    /** minecraft default */
    private Narrator defaultNarrator;

    /** */
    public void setDefaultNarrator(Narrator narrator) {
        this.defaultNarrator = narrator;
    }

    /** */
    public VoiceVoxNarrator() throws InitializeException {
        List<Exception> exceptions = new ArrayList<>();
        ZundaMod.models.forEach(model -> {
            try {
                Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(getSynthesizerMode(model.clazz));
                synthesizer.allocate();
                synthesizer.waitEngineState(Engine.ALLOCATED);
                synthesizer.resume();
                synthesizer.waitEngineState(Synthesizer.RESUMED);

                String voiceName = model.voice;
                Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
                synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
                String rate = model.param("rate");
                if (rate != null) {
                    synthesizer.getSynthesizerProperties().setSpeakingRate(Integer.parseInt(rate));
                    ZundaMod.LOGGER.info(model.account + ", rate: " +synthesizer.getSynthesizerProperties().getSpeakingRate());
                }
                String pitch = model.param("pitch");
                if (pitch != null) {
                    synthesizer.getSynthesizerProperties().setPitch(Integer.parseInt(pitch));
                    ZundaMod.LOGGER.info(model.account + ", pitch: " +synthesizer.getSynthesizerProperties().getPitch());
                }
                String volume = model.param("volume");
                if (volume != null) {
                    synthesizer.getSynthesizerProperties().setVolume(Integer.parseInt(volume));
                    ZundaMod.LOGGER.info(model.account + ", volume: " +synthesizer.getSynthesizerProperties().getVolume());
                }

                synthesizers.put(model.account, synthesizer);
                ZundaMod.LOGGER.info(model.account + ", " + synthesizer.getClass().getName());
            } catch (Exception e) {
                ZundaMod.LOGGER.error(e.getMessage(), e);
                exceptions.add(e);
            }
        });
        if (!exceptions.isEmpty()) {
            throw new InitializeException(String.join("\n", exceptions.stream().map(Throwable::getMessage).toList()));
        }
    }

    /** synthesizer class */
    private static SynthesizerMode getSynthesizerMode(String clazz) throws Exception {
        return (SynthesizerMode) Class.forName(clazz).getDeclaredConstructor().newInstance();
    }

    /** chat identifier */
    private static final Pattern pattern = Pattern.compile("^(.+?) says (.+)$");

    @Override
    public void say(String msg, boolean interrupt, float volume) {
        if (synthesizers.isEmpty()) {
            defaultNarrator.say(msg, interrupt, volume);
            return;
        }

        Synthesizer synthesizer = null;
        String message = msg;
        String username = Minecraft.getInstance().getGameProfile().name();
        Matcher matcher = pattern.matcher(msg);
ZundaMod.LOGGER.info("say: " + matcher.matches() + ", " + username + ", " + msg);
        try {
            if (matcher.matches()) {
                synthesizer = synthesizers.get(matcher.group(1));
                if (synthesizer == null && username.equals(matcher.group(1))) {
                    synthesizer = synthesizers.get("@@");
                }
                if (synthesizer != null) {
                    message = matcher.group(2);
                }
            }
            if (synthesizer == null) {
                synthesizer = synthesizers.get("$$");
            }
        } catch (Exception e) {
            ZundaMod.LOGGER.warn(e.getMessage(), e);
        }
        if (synthesizer != null) {
ZundaMod.LOGGER.info("synthesizer: " + synthesizer + ", " + message + ", " + interrupt);
            synthesizer.speak(message, m -> ZundaMod.LOGGER.info(m.toString()));
        } else {
            defaultNarrator.say(msg, interrupt, volume);
        }
    }

    @Override
    public void clear() {
        synthesizers.values().forEach(synthesizer -> {
            try {
                synthesizer.cancelAll();
            } catch (EngineStateException e) {
                ZundaMod.LOGGER.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void destroy() {
        synthesizers.values().forEach(synthesizer -> {
            try {
                synthesizer.deallocate();
            } catch (AudioException | EngineException e) {
                ZundaMod.LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
