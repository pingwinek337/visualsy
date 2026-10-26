package pl.penguin.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import pl.penguin.visuals.ui.Ui;

import java.util.Locale;

/** Obramowanie, serduszka nad głową i panel z HP gracza, na którego patrzysz. */
public final class TargetHud {
	public static final String[] ANCHOR_NAMES = {
		"Lewy górny róg", "Prawy górny róg", "Lewy dolny róg", "Prawy dolny róg", "Przy graczu"
	};

	private static final String[] HEART = {
		".XXX.XXX.",
		"XXXXXXXXX",
		"XXXXXXXXX",
		"XXXXXXXXX",
		".XXXXXXX.",
		"..XXXXX..",
		"...XXX...",
		"....X...."
	};

	private TargetHud() {
	}

	public static void render(DrawContext c, MinecraftClient mc, RenderTickCounter tickCounter) {
		PenguinConfig cfg = PenguinConfig.get();
		if ((!cfg.targetPanel && !cfg.outline && !cfg.targetHearts) || mc.currentScreen != null || mc.world == null) {
			return;
		}

		float delta = tickCounter.getTickDelta(true);
		PlayerEntity target = TargetTracker.find(mc, delta, cfg.targetRange);
		if (target == null) {
			return;
		}

		// prostokąt na ekranie obejmujący gracza
		ScreenProjector projector = new ScreenProjector(mc);
		Vec3d pos = target.getLerpedPos(delta);
		double half = target.getWidth() / 2.0;
		double height = target.getHeight();

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double[] out = new double[2];
		for (int i = 0; i < 8; i++) {
			Vec3d corner = new Vec3d(
				pos.x + ((i & 1) == 0 ? -half : half),
				pos.y + ((i & 2) == 0 ? 0 : height),
				pos.z + ((i & 4) == 0 ? -half : half)
			);
			if (!projector.project(corner, out)) {
				return;
			}
			minX = Math.min(minX, out[0]);
			maxX = Math.max(maxX, out[0]);
			minY = Math.min(minY, out[1]);
			maxY = Math.max(maxY, out[1]);
		}

		int x0 = (int) Math.floor(minX) - 3;
		int y0 = (int) Math.floor(minY) - 3;
		int x1 = (int) Math.ceil(maxX) + 3;
		int y1 = (int) Math.ceil(maxY) + 3;

		if (cfg.outline) {
			TargetOutline.draw(c, x0, y0, x1, y1, cfg);
		}
		if (cfg.targetHearts) {
			drawHearts(c, cfg, target, (x0 + x1) / 2, y0);
		}
		if (cfg.targetPanel) {
			drawPanel(c, mc, cfg, target, x0, y0, x1, y1);
		}
	}

	// ------------------------------------------------------------------ serduszka nad głową

	private static void drawHearts(DrawContext c, PenguinConfig cfg, PlayerEntity target, int centerX, int topY) {
		float max = Math.max(1f, target.getMaxHealth());
		float health = Math.max(0f, Math.min(max, target.getHealth()));
		float units = health / max * 10f;

		int px = Math.max(1, Math.min(3, cfg.heartScale));
		int step = 8 * px;
		int width = 9 * step + 9 * px;
		int left = centerX - width / 2;
		int top = Math.max(2, topY - 9 * px - 4);

		for (int i = 0; i < 10; i++) {
			float fill = Math.max(0f, Math.min(1f, units - i));
			drawHeart(c, left + i * step, top, px, fill);
		}
	}

	private static void drawHeart(DrawContext c, int x, int y, int px, float fill) {
		int filledColumns = Math.round(9 * fill);
		for (int row = 0; row < HEART.length; row++) {
			String line = HEART[row];
			for (int col = 0; col < line.length(); col++) {
				if (line.charAt(col) == 'X') {
					Ui.rect(c, x + col * px + px, y + row * px + px, px, px, 0x70000000);
				}
			}
		}
		for (int row = 0; row < HEART.length; row++) {
			String line = HEART[row];
			for (int col = 0; col < line.length(); col++) {
				if (line.charAt(col) != 'X') {
					continue;
				}
				int color;
				if (col < filledColumns) {
					if (row == 1 && col == 1) {
						color = 0xFFFF9A9A;
					} else if (row >= 5) {
						color = 0xFFC62828;
					} else {
						color = 0xFFE53935;
					}
				} else {
					color = 0xFF3A1F24;
				}
				Ui.rect(c, x + col * px, y + row * px, px, px, color);
			}
		}
	}

	// ------------------------------------------------------------------ panel z HP

	private static void drawPanel(DrawContext c, MinecraftClient mc, PenguinConfig cfg, PlayerEntity target,
								  int x0, int y0, int x1, int y1) {
		TextRenderer tr = mc.textRenderer;
		String nick = target.getName().getString();
		float max = Math.max(1f, target.getMaxHealth());
		float health = Math.max(0f, target.getHealth());
		float absorption = target.getAbsorptionAmount();
		String hpText = String.format(Locale.ROOT, "%.1f HP", health + absorption);
		float fraction = Math.max(0f, Math.min(1f, health / max));

		int innerWidth = Math.max(84, tr.getWidth(hpText));
		if (cfg.panelNick) {
			innerWidth = Math.max(innerWidth, tr.getWidth(nick));
		}
		int panelWidth = innerWidth + 12;
		int panelHeight = 8 + (cfg.panelNick ? 11 : 0) + 11 + 6;

		int screenWidth = mc.getWindow().getScaledWidth();
		int screenHeight = mc.getWindow().getScaledHeight();

		int px;
		int py;
		switch (Math.floorMod(cfg.panelAnchor, ANCHOR_NAMES.length)) {
			case 0 -> {
				px = cfg.panelOffsetX;
				py = cfg.panelOffsetY;
			}
			case 1 -> {
				px = screenWidth - panelWidth - cfg.panelOffsetX;
				py = cfg.panelOffsetY;
			}
			case 2 -> {
				px = cfg.panelOffsetX;
				py = screenHeight - panelHeight - cfg.panelOffsetY;
			}
			case 3 -> {
				px = screenWidth - panelWidth - cfg.panelOffsetX;
				py = screenHeight - panelHeight - cfg.panelOffsetY;
			}
			default -> {
				px = x1 + 8;
				if (px + panelWidth > screenWidth - 2) {
					px = x0 - 8 - panelWidth;
				}
				py = (y0 + y1) / 2 - panelHeight / 2;
			}
		}
		px = Math.max(2, Math.min(screenWidth - panelWidth - 2, px));
		py = Math.max(2, Math.min(screenHeight - panelHeight - 2, py));

		Ui.rect(c, px, py, panelWidth, panelHeight, 0xA0101520);
		Ui.rect(c, px, py, 2, panelHeight, Ui.accent());

		int tx = px + 7;
		int ty = py + 4;
		if (cfg.panelNick) {
			c.drawText(tr, nick, tx, ty, 0xFFFFFFFF, true);
			ty += 11;
		}
		c.drawText(tr, hpText, tx, ty, 0xFF000000 | hpColor(fraction), true);
		ty += 11;

		int barWidth = panelWidth - 14;
		Ui.rect(c, tx, ty, barWidth, 3, 0xFF2A3140);
		if (fraction > 0f) {
			Ui.rect(c, tx, ty, Math.max(1, Math.round(barWidth * fraction)), 3, 0xFF000000 | hpColor(fraction));
		}
	}

	private static int hpColor(float fraction) {
		if (fraction > 0.6f) {
			return 0x4CD964;
		}
		if (fraction > 0.3f) {
			return 0xFFC83B;
		}
		return 0xFF4D4D;
	}
}
