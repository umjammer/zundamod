package vavi.mod.zundamod.mixin;

import com.mojang.text2speech.Narrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vavi.mod.zundamod.VoiceVoxNarrator;
import vavi.mod.zundamod.ZundaMod;


// we can't mixin to interface
@Mixin(Narrator.class)
public interface NarratorMixin {

	@Inject(at = @At("RETURN"), method = "getNarrator", cancellable = true)
    private static void replace(CallbackInfoReturnable<Narrator> cir) {
		try {
			Narrator narrator = new VoiceVoxNarrator();
			ZundaMod.LOGGER.info(narrator.getClass().getName());
            cir.setReturnValue(narrator);
		} catch (Narrator.InitializeException e) {
			throw new IllegalStateException(e);
		}
	}
}