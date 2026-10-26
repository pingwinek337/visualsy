package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.penguin.visuals.LicenseManager;

/** Ekran aktywacji licencji. Bez poprawnego klucza mod nic nie robi. */
public class LicenseScreen extends Screen {
	private static final int PW = 360;
	private static final int PH = 252;
	private static final int MAX_RAW = 20;

	private final StringBuilder raw = new StringBuilder();
	private final Fx.Background snow = new Fx.Background(90);

	private String message = "";
	private int messageColor = 0xFFFF6B6B;
	private long messageAt = 0L;
	private long shakeAt = 0L;
	private long successAt = 0L;
	private long openAt = 0L;
	private long lastNanos = System.nanoTime();
	private float hoverActivate = 0f;
	private float hoverPaste = 0f;
	private int px;
	private int py;

	public LicenseScreen() {
		super(Text.literal("Penguin Visuals - licencja"));
	}

	@Override
	protected void init() {
		px = (width - PW) / 2;
		py = (height - PH) / 2;
		if (openAt == 0L) {
			openAt = System.currentTimeMillis();
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void tick() {
		if (successAt != 0L && System.currentTimeMillis() - successAt > 800L && client != null) {
			client.setScreen(new PenguinScreen());
		}
	}

	// ------------------------------------------------------------------ rysowanie

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
		lastNanos = now;

		long ms = System.currentTimeMillis();
		float open = Ui.ease((ms - openAt) / 300f);
		float shake = 0f;
		if (shakeAt != 0L && ms - shakeAt < 450L) {
			float t = (ms - shakeAt) / 450f;
			shake = (float) (Math.sin((ms - shakeAt) * 0.06) * 8.0 * (1f - t));
		}

		Ui.setAlpha(open);
		try {
			Ui.gradient(c, 0, 0, width, height, 0xB4080C16, 0xD2030509);
			snow.render(c, width, height);

			c.getMatrices().push();
			c.getMatrices().translate(shake, (1f - open) * 18f, 0f);
			drawPanel(c, mouseX, mouseY, dt, ms);
			c.getMatrices().pop();
		} finally {
			Ui.setAlpha(1f);
		}
	}

	private void drawPanel(DrawContext c, int mx, int my, float dt, long ms) {
		Ui.panel(c, px, py, PW, PH);

		// błysk na górnej krawędzi
		float phase = (ms % 3200L) / 3200f;
		for (int sx = px + 6; sx < px + PW - 6; sx += 2) {
			float pos = (sx - px) / (float) PW;
			float dist = Math.abs(pos - phase);
			dist = Math.min(dist, 1f - dist);
			float a = Math.max(0f, 1f - dist * 7f);
			if (a > 0.02f) {
				Ui.rect(c, sx, py, 2, 2, Ui.withAlpha(Ui.accent(), (int) (a * 255)));
			}
		}

		int cx = px + PW / 2;

		// pingwin z cieniem
		int bob = (int) Math.round(Math.sin(ms / 420.0) * 2.0);
		Ui.round(c, cx - 15 + Math.abs(bob), py + 66, 30 - Math.abs(bob) * 2, 4, 2, 0x44000000);
		Ui.drawPenguin(c, cx - 20, py + 18 + bob, 4);

		// tytuł
		float scale = 1.4f;
		int fullW = (int) (textRenderer.getWidth("PENGUIN VISUALS") * scale);
		int tx = cx - fullW / 2;
		Ui.scaledText(c, textRenderer, "PENGUIN", tx, py + 78, scale, Ui.accent(), true);
		Ui.scaledText(c, textRenderer, "VISUALS", tx + (int) (textRenderer.getWidth("PENGUIN ") * scale), py + 78, scale,
			0xFFFFFFFF, true);
		Ui.textCentered(c, textRenderer, "Wpisz klucz licencyjny, aby odblokować mod", cx, py + 97, 0xFF8B95A7, false);

		// pole klucza
		int bx = px + 24;
		int by = py + 116;
		int bw = PW - 48;
		int bh = 28;
		Ui.round(c, bx, by, bw, bh, 6, 0xFF141B27);
		boolean ok = successAt != 0L;
		int border = ok ? 0xFF6BE08A : Ui.accent();
		Ui.roundOutline(c, bx, by, bw, bh, 6, Ui.withAlpha(border, 0xB0));

		String shown = display();
		if (shown.isEmpty()) {
			Ui.text(c, textRenderer, "PENG-XXXX-XXXX-XXXX-XXXX", bx + 12, by + 10, 0xFF3F4A5F, false);
		}
		Ui.text(c, textRenderer, shown, bx + 12, by + 10, 0xFFFFFFFF, false);
		if (!ok && (ms / 500) % 2 == 0) {
			Ui.rect(c, bx + 12 + textRenderer.getWidth(shown), by + 8, 1, 12, Ui.accent());
		}
		String counter = raw.length() + "/" + MAX_RAW;
		Ui.text(c, textRenderer, counter, bx + bw - 10 - textRenderer.getWidth(counter), by + 10, 0xFF55607A, false);

		// pasek postępu pod polem
		Ui.round(c, bx + 6, by + bh + 5, bw - 12, 3, 1, 0xFF1C2431);
		int filled = (bw - 12) * raw.length() / MAX_RAW;
		if (filled > 0) {
			Ui.round(c, bx + 6, by + bh + 5, Math.max(3, filled), 3, 1, border);
		}

		// przyciski
		int btnY = py + 164;
		int btnW = (bw - 8) / 2;
		boolean hA = Ui.inside(mx, my, bx, btnY, btnW, 24);
		boolean hP = Ui.inside(mx, my, bx + btnW + 8, btnY, btnW, 24);
		hoverActivate += ((hA ? 1f : 0f) - hoverActivate) * Math.min(1f, dt * 16f);
		hoverPaste += ((hP ? 1f : 0f) - hoverPaste) * Math.min(1f, dt * 16f);
		Ui.button(c, textRenderer, bx, btnY, btnW, 24, "Aktywuj", hoverActivate, Ui.accent());
		Ui.button(c, textRenderer, bx + btnW + 8, btnY, btnW, 24, "Wklej ze schowka", hoverPaste, 0xFF8B95A7);

		// komunikat
		if (!message.isEmpty()) {
			long age = ms - messageAt;
			boolean persistent = ok;
			if (persistent || age < 3000L) {
				int alpha = persistent ? 255 : (int) (255 * Math.min(1f, (3000L - age) / 600f));
				Ui.textCentered(c, textRenderer, message, cx, py + 200, Ui.withAlpha(messageColor, alpha), true);
			}
		}

		// stopka
		Ui.rect(c, px + 3, py + PH - 24, PW - 6, 1, 0xFF1F2938);
		Ui.text(c, textRenderer, "ESC - zamknij", px + 14, py + PH - 16, 0xFF55607A, false);
		String version = "v1.0.0";
		Ui.text(c, textRenderer, version, px + PW - 14 - textRenderer.getWidth(version), py + PH - 16, 0xFF55607A, false);
	}

	/** Klucz z automatycznymi myślnikami: PENG-XXXX-XXXX-XXXX-XXXX. */
	private String display() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			if (i > 0 && i % 4 == 0) {
				sb.append('-');
			}
			sb.append(raw.charAt(i));
		}
		return sb.toString();
	}

	// ------------------------------------------------------------------ sterowanie

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && successAt == 0L) {
			int bx = px + 24;
			int bw = PW - 48;
			int btnY = py + 164;
			int btnW = (bw - 8) / 2;
			if (Ui.inside(mouseX, mouseY, bx, btnY, btnW, 24)) {
				tryActivate();
				return true;
			}
			if (Ui.inside(mouseX, mouseY, bx + btnW + 8, btnY, btnW, 24)) {
				paste();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (successAt != 0L) {
			return true;
		}
		if (Character.isLetterOrDigit(chr)) {
			if (raw.length() < MAX_RAW) {
				raw.append(Character.toUpperCase(chr));
			}
			message = "";
			return true;
		}
		if (chr == '-' || chr == ' ') {
			return true;
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (successAt != 0L) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			if (raw.length() > 0) {
				raw.deleteCharAt(raw.length() - 1);
			}
			message = "";
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			tryActivate();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_V && Screen.hasControlDown()) {
			paste();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void paste() {
		if (client == null) {
			return;
		}
		raw.setLength(0);
		for (char ch : client.keyboard.getClipboard().toCharArray()) {
			if (Character.isLetterOrDigit(ch) && raw.length() < MAX_RAW) {
				raw.append(Character.toUpperCase(ch));
			}
		}
		message = "";
	}

	private void tryActivate() {
		if (LicenseManager.activate(display())) {
			successAt = System.currentTimeMillis();
			message = "Licencja aktywowana!";
			messageColor = 0xFF6BE08A;
			messageAt = successAt;
		} else {
			shakeAt = System.currentTimeMillis();
			message = "Nieprawidłowy klucz licencyjny.";
			messageColor = 0xFFFF6B6B;
			messageAt = shakeAt;
		}
	}
}
