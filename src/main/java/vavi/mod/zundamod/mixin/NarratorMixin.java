/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.mod.zundamod.mixin;

import com.mojang.text2speech.Narrator;
import com.mojang.text2speech.Narrator.FatalException;
import com.mojang.text2speech.Narrator.InitializeException;
import com.mojang.text2speech.NarratorLinux;
import com.mojang.text2speech.NarratorMac;
import com.mojang.text2speech.NarratorWindows;
import com.mojang.text2speech.OperatingSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vavi.mod.zundamod.VoiceVoxNarrator;
import vavi.mod.zundamod.ZundaMod;


/**
 * VoiceVoxNarrator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-06-18 nsano initial version <br>
 */
@Mixin(Narrator.class)
public interface NarratorMixin {

	@Inject(at = @At("RETURN"), method = "getNarrator", cancellable = true)
    private static void replace(CallbackInfoReturnable<Narrator> cir) {
		try {
            VoiceVoxNarrator narrator = new VoiceVoxNarrator();
			ZundaMod.LOGGER.info(narrator.getClass().getName());
            narrator.setDefaultNarrator(getDefaultNarrator());
            cir.setReturnValue(narrator);
		} catch (Narrator.InitializeException e) {
            ZundaMod.LOGGER.warn("custom narrator setting failed, use default", e);
		}
	}

    @Unique
    private static Narrator getDefaultNarrator() {
        try {
            return switch (OperatingSystem.get()) {
                case LINUX -> new NarratorLinux();
                case WINDOWS -> new NarratorWindows();
                case MAC_OS -> new NarratorMac();
                default -> throw new InitializeException("Unsupported platform " + System.getProperty("os.name"));
            };
        } catch (FatalException e) {
            throw e;
        } catch (Throwable e) {
            ZundaMod.LOGGER.error("Error while loading the narrator", e);
            return Narrator.EMPTY;
        }
    }
}
