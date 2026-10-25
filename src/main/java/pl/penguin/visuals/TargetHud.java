package pl.penguin.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import pl.penguin.visuals.ui.Ui;

/** Obramowanie i panel z informacjami obok gracza, na którego patrzysz. */
public final class TargetHud {
	private TargetHud() {
	}

	public static void render(DrawContext c, MinecraftClient mc, RenderTickCounter tickCounter) {
		PenguinConfig cfg = PenguinConfig.get();
		if ((!cfg.targetPanel && !cfg.outline) || mc.currentScreen != null || mc.world == null) {
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
		if (cfg.targetPanel) {
			drawPanel(c, mc, cfg, target, x0, y0, x1, y1);
		}
	}

	private static void drawPanel(DrawContext c, MinecraftClient mc, PenguinConfig cfg, PlayerEntity target,
								  int x0, int y0, int x1, int y1) {
		int rows = (cfg.panelNick ? 1 : 0) + (cfg.panelSet ? 1 : 0) + (cfg.panelDurability ? 2 : 0);
		if (rows == 0) {
			return;
		}

		TextRenderer tr = mc.textRenderer;
		ArmorInfo info = ArmorInfo.of(target);
		String nick = target.getName().getString();
		String setLine = "Set: " + info.setName;
		int percent = info.percent();
		String percentText = percent < 0 ? "-" : percent + "%";
		String piecesLine = info.piecesLine();

		int innerWidth = 84;
		if (cfg.panelNick) {
			innerWidth = Math.max(innerWidth, tr.getWidth(nick));
		}
		if (cfg.panelSet) {
			innerWidth = Math.max(innerWidth, tr.getWidth(setLine));
		}
		if (cfg.panelDurability) {
			innerWidth = Math.max(innerWidth, tr.getWidth(piecesLine));
		}

		int panelWidth = innerWidth + 12;
		int panelHeight = 8 + rows * 11;
		int screenWidth = mc.getWindow().getScaledWidth();
		int screenHeight = mc.getWindow().getScaledHeight();

		int px = x1 + 8;
		if (px + panelWidth > screenWidth - 2) {
			px = x0 - 8 - panelWidth;
		}
		int py = (y0 + y1) / 2 - panelHeight / 2;
		py = Math.max(2, Math.min(screenHeight - panelHeight - 2, py));

		Ui.rect(c, px, py, panelWidth, panelHeight, 0xA0101520);
		Ui.rect(c, px, py, 2, panelHeight, Ui.accent());

		int tx = px + 7;
		int ty = py + 4;
		if (cfg.panelNick) {
			c.drawText(tr, nick, tx, ty, 0xFFFFFFFF, true);
			ty += 11;
		}
		if (cfg.panelSet) {
			c.drawText(tr, setLine, tx, ty, 0xFF000000 | info.setColor, true);
			ty += 11;
		}
		if (cfg.panelDurability) {
			int barWidth = panelWidth - 14 - tr.getWidth(percentText) - 4;
			Ui.rect(c, tx, ty + 2, barWidth, 5, 0xFF2A3140);
			if (percent > 0) {
				Ui.rect(c, tx, ty + 2, Math.max(1, barWidth * percent / 100), 5, 0xFF000000 | durabilityColor(percent));
			}
			c.drawText(tr, percentText, tx + barWidth + 4, ty, 0xFFD7DEE9, true);
			ty += 11;
			c.drawText(tr, piecesLine, tx, ty, 0xFF9AA5B8, false);
		}
	}

	private static int durabilityColor(int percent) {
		if (percent > 60) {
			return 0x4CD964;
		}
		if (percent > 30) {
			return 0xFFC83B;
		}
		return 0xFF4D4D;
	}
}
