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
import net.minecraft.network.chat.Component;


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

                // VoiceVox series
                String rate = model.param("rate");
                if (rate != null) {
                    synthesizer.getSynthesizerProperties().setSpeakingRate(Integer.parseInt(rate));
                    ZundaMod.LOGGER.info("{}, rate: {}", model.account, synthesizer.getSynthesizerProperties().getSpeakingRate());
                }
                String pitch = model.param("pitch");
                if (pitch != null) {
                    synthesizer.getSynthesizerProperties().setPitch(Integer.parseInt(pitch));
                    ZundaMod.LOGGER.info("{}, pitch: {}", model.account, synthesizer.getSynthesizerProperties().getPitch());
                }

                // qwen3tty
                String refAudio = model.param("refAudio");
                if (refAudio != null) {
                    System.setProperty("vavi.speech.qwen3tts.refAudio", refAudio);
                    ZundaMod.LOGGER.info("{}, refAudio: {}", "vavi.speech.qwen3tts.refAudio", refAudio);
                }
                String refText = model.param("refText");
                if (refText != null) {
                    System.setProperty("vavi.speech.qwen3tts.refText", refText);
                    ZundaMod.LOGGER.info("{}, refText: {}", "vavi.speech.qwen3tts.refText", refText);
                }
                String clone = model.param("clone");
                if (clone != null) {
                    System.setProperty("vavi.speech.qwen3tts.clone", clone);
                    ZundaMod.LOGGER.info("{}, clone: {}", "vavi.speech.qwen3tts.clone", clone);
                }
                String language = model.param("language");
                if (language != null) {
                    System.setProperty("vavi.speech.qwen3tts.language", language);
                    ZundaMod.LOGGER.info("{}, language: {}", "vavi.speech.qwen3tts.language", language);
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

    /** narration of the chat input box, "Message to send: %s" */
    private static final String CHAT_INPUT_NARRATION_KEY = "chat_screen.message";

    /**
     * While you type, {@code ChatScreen} narrates the input box as
     * {@link #CHAT_INPUT_NARRATION_KEY}, and once the message is sent
     * {@code ChatListener} narrates it again as "&lt;player&gt; says &lt;message&gt;".
     * That makes every message you send sound twice, here even in two voices,
     * since only the latter matches {@link #pattern}. Drop the former.
     *
     * @param msg the narration text
     * @return whether msg is the narration of the chat input box
     */
    private static boolean isChatInputEcho(String msg) {
        if (!ZundaMod.filterChatInputEcho) {
            return false;
        }
        // with chat narration off nothing repeats the input box, so keep saying it
        if (!Minecraft.getInstance().options.narrator().get().shouldNarrateChat()) {
            return false;
        }
        // the prefix is localized, ask the language for it instead of hard coding it
        String prefix = Component.translatable(CHAT_INPUT_NARRATION_KEY, "").getString();
        // an unresolved key gives the key itself, an empty prefix would match everything
        return !prefix.isEmpty() && msg.startsWith(prefix);
    }

    @Override
    public void say(String msg, boolean interrupt, float volume) {
        if (isChatInputEcho(msg)) {
ZundaMod.LOGGER.info("skip chat input echo: {}", msg);
            return;
        }

        if (synthesizers.isEmpty()) {
            defaultNarrator.say(msg, interrupt, volume);
            return;
        }

        Synthesizer synthesizer = null;
        String message = msg;
        String username = Minecraft.getInstance().getGameProfile().name();
        Matcher matcher = pattern.matcher(msg);
ZundaMod.LOGGER.info("say: {}, {}, {}, {}", matcher.matches(), username, msg, volume);
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
            int v = (int) (100 * volume);
ZundaMod.LOGGER.info("synthesizer: {}, {}, {}, {}, {}", synthesizer, message, interrupt, volume, v);
            synthesizer.getSynthesizerProperties().setVolume(v);
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
