package pl.penguin.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import pl.penguin.visuals.ui.Ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Mały HUD w lewym górnym rogu. */
public final class PenguinHud {
	private PenguinHud() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(PenguinHud::render);
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (!LicenseManager.isActive()) {
			return;
		}
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden || mc.options.debugEnabled) {
			return;
		}

		PenguinConfig cfg = PenguinConfig.get();
		List<String> lines = new ArrayList<>();
		if (cfg.hudFps) {
			lines.add("FPS: " + mc.getCurrentFps());
		}
		if (cfg.hudCoords) {
			lines.add(String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
				mc.player.getX(), mc.player.getY(), mc.player.getZ()));
		}
		if (!cfg.hudWatermark && lines.isEmpty()) {
			return;
		}

		String title = "Penguin";
		String titleRest = " Visuals";
		int titleWidth = mc.textRenderer.getWidth(title + titleRest) + 14;

		int width = cfg.hudWatermark ? titleWidth : 0;
		for (String line : lines) {
			width = Math.max(width, mc.textRenderer.getWidth(line));
		}
		width += 10;

		int x = 4;
		int y = 4;
		int height = 6 + (cfg.hudWatermark ? 13 : 0) + lines.size() * 11;

		Ui.rect(context, x, y, width, height, 0x90101520);
		Ui.rect(context, x, y, width, 1, Ui.accent());

		int textY = y + 4;
		if (cfg.hudWatermark) {
			Ui.drawPenguin(context, x + 4, textY - 1, 1);
			context.drawText(mc.textRenderer, title, x + 18, textY, Ui.accent(), true);
			context.drawText(mc.textRenderer, titleRest, x + 18 + mc.textRenderer.getWidth(title), textY, 0xFFFFFFFF, true);
			textY += 13;
		}
		for (String line : lines) {
			context.drawText(mc.textRenderer, line, x + 5, textY, 0xFFD7DEE9, true);
			textY += 11;
		}
	}
}
