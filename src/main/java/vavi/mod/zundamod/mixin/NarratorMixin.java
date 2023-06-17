package vavi.mod.zundamod.mixin;

import com.mojang.text2speech.Narrator;
import com.mojang.text2speech.NarratorMac;
import net.minecraft.client.util.NarratorManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vavi.mod.zundamod.VoiceVoxNarrator;
import vavi.mod.zundamod.ZundaMod;


// we can't mixin to interface
@Mixin(NarratorManager.class)
public class NarratorMixin {

	@Final
	@Mutable
	@Shadow
	private Narrator narrator;

	@Inject(at = @At("RETURN"), method = "<init>")
	public void replace(CallbackInfo ci) {
		try {
			this.narrator = new VoiceVoxNarrator();
			ZundaMod.LOGGER.info(this.narrator.getClass().getName());
		} catch (Narrator.InitializeException e) {
			throw new IllegalStateException(e);
		}
	}
}