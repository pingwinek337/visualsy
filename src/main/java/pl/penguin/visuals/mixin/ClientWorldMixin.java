package pl.penguin.visuals.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.penguin.visuals.SkyPreset;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

	/**
	 * Podmienia kolor nieba, ale zachowuje jasność z oryginału,
	 * więc cykl dnia i nocy dalej działa.
	 */
	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void penguinvisuals$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
		Integer override = SkyPreset.overrideRgb();
		if (override == null) {
			return;
		}

		int original = cir.getReturnValue();
		int or = (original >> 16) & 0xFF;
		int og = (original >> 8) & 0xFF;
		int ob = original & 0xFF;
		float brightness = Math.max(or, Math.max(og, ob)) / 255f;
		brightness = Math.max(0.03f, Math.min(1f, brightness));

		int r = (int) (((override >> 16) & 0xFF) * brightness);
		int g = (int) (((override >> 8) & 0xFF) * brightness);
		int b = (int) ((override & 0xFF) * brightness);

		cir.setReturnValue(0xFF000000 | (r << 16) | (g << 8) | b);
	}
}
