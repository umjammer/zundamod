/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.mod.zundamod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * VoiceVoxNarrator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-06-18 nsano initial version <br>
 */
public class ZundaMod implements ModInitializer {

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("zundamod");

    static {
        System.setProperty("org.jvoicexml.jsapi2.synthesis.QueueManager.cancelStrategy", "drain");
    }

	/** whole configuration */
	@PropsEntity(url = "file:config/zundamod.properties")
	static class Conf {

		@Property
		int models;

        /** url */
		@Property
        String voiceVox;
        /** url */
        @Property
        String coeiroInk;
        /** url */
        @Property
        String aivis;
        /** url */
        @Property
        String qwen3tts;

		/** {@code false} keeps the chat input echo, see {@code VoiceVoxNarrator#isChatInputEcho} */
		@Property
        boolean filterChatInputEcho = true;
	}

	/** each configuration */
	@PropsEntity(url = "file:config/zundamod.properties")
	static class Model {
		@Property(name = "class.{0}")
		String clazz;
		@Property(name = "voice.{0}")
		String voice;
		@Property(name = "params.{0}")
		String params;
		@Property(name = "account.{0}")
		String account;

		private Map<String, String> kvs;

		String param(String key) {
			if (kvs == null) {
				kvs = new HashMap<>();
				if (params != null) {
					Arrays.stream(params.split(",")).forEach(pair -> {
						String[] kv = pair.split(":");
						kvs.put(kv[0], kv[1]);
					});
				}
				LOGGER.info("kvs[" + account + "]: " + kvs);
			}
			return kvs.get(key);
		}
		@Override public String toString() {
			return new StringJoiner(", ", Model.class.getSimpleName() + "[", "]")
					.add("clazz='" + clazz + "'")
					.add("voice='" + voice + "'")
					.add("params='" + params + "'")
					.add("account='" + account + "'")
					.toString();
		}
	}

	/** models */
	static List<Model> models = new ArrayList<>();

	/** drop the chat input box narration, which the chat narration says again */
	static boolean filterChatInputEcho;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ZundaMod.Conf conf = new Conf();
		try {
			PropsEntity.Util.bind(conf);
            LOGGER.info("models: {}", conf.models);

            if (conf.voiceVox != null) {
                System.setProperty("vavi.speech.voicevox.url", conf.voiceVox);
                LOGGER.info("vavi.speech.voicevox.url: {}", conf.voiceVox);
            }
            if (conf.coeiroInk != null) {
                System.setProperty("vavi.speech.coeiroink.url", conf.coeiroInk);
                LOGGER.info("vavi.speech.coeiroink.url: {}", conf.coeiroInk);
            }
            if (conf.aivis != null) {
                System.setProperty("vavi.speech.aivis.url", conf.aivis);
                LOGGER.info("vavi.speech.aivis.url: {}", conf.aivis);
            }
            if (conf.qwen3tts != null) {
                System.setProperty("vavi.speech.qwen3tts.url", conf.qwen3tts);
                LOGGER.info("vavi.speech.qwen3tts.url: {}", conf.qwen3tts);
            }

            filterChatInputEcho = conf.filterChatInputEcho;
            LOGGER.info("filterChatInputEcho: {}", filterChatInputEcho);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}

        // add models defined in the config file
		for (int i = 1; i <= conf.models; i++) {
			try {
				Model model = new Model();
				PropsEntity.Util.bind(model, String.valueOf(i));
				models.add(model);
                LOGGER.info("model[{}]: {}", i, model);
			} catch (Exception e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}
}
