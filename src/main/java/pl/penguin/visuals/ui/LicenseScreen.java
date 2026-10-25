package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.penguin.visuals.LicenseManager;

/** Ekran aktywacji licencji. Bez poprawnego klucza mod nie działa. */
public class LicenseScreen extends Screen {
	private static final int PW = 340;
	private static final int PH = 178;
	private static final int MAX_LENGTH = 24;

	private final StringBuilder input = new StringBuilder();
	private String message = "";
	private int messageColor = 0xFFFF6B6B;
	private int px;
	private int py;

	public LicenseScreen() {
		super(Text.literal("Penguin Visuals - licencja"));
	}

	@Override
	protected void init() {
		px = (width - PW) / 2;
		py = (height - PH) / 2;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		c.fill(0, 0, width, height, 0x99050810);
		Ui.panel(c, px, py, PW, PH);

		c.fillGradient(px + 1, py + 1, px + PW - 1, py + 28, 0xFF1A2230, 0xFF131A25);
		Ui.rect(c, px + 1, py + 28, PW - 2, 2, Ui.accent());
		Ui.drawPenguin(c, px + 8, py + 3, 2);
		c.drawText(textRenderer, "PENGUIN", px + 36, py + 10, Ui.accent(), true);
		c.drawText(textRenderer, " VISUALS", px + 36 + textRenderer.getWidth("PENGUIN"), py + 10, 0xFFFFFFFF, true);

		c.drawText(textRenderer, "Aktywacja licencji", px + 20, py + 44, 0xFFFFFFFF, true);
		c.drawText(textRenderer, "Wpisz klucz licencyjny, aby odblokować mod.", px + 20, py + 58, 0xFF8B95A7, false);

		// pole tekstowe
		int bx = px + 20;
		int by = py + 76;
		int bw = PW - 40;
		Ui.rect(c, bx, by, bw, 22, 0xFF1B2330);
		Ui.rect(c, bx, by + 21, bw, 1, Ui.accent());
		if (input.length() == 0) {
			c.drawText(textRenderer, "PENG-XXXX-XXXX-XXXX-XXXX", bx + 6, by + 7, 0xFF4A5468, false);
		}
		String shown = input.toString();
		c.drawText(textRenderer, shown, bx + 6, by + 7, 0xFFFFFFFF, false);
		if ((System.currentTimeMillis() / 500) % 2 == 0) {
			int caretX = bx + 6 + textRenderer.getWidth(shown);
			Ui.rect(c, caretX, by + 5, 1, 12, Ui.accent());
		}

		// przyciski
		int btnY = py + 108;
		int btnW = (bw - 8) / 2;
		boolean hoverActivate = Ui.inside(mouseX, mouseY, bx, btnY, btnW, 20);
		boolean hoverPaste = Ui.inside(mouseX, mouseY, bx + btnW + 8, btnY, btnW, 20);
		Ui.button(c, textRenderer, bx, btnY, btnW, 20, "Aktywuj", hoverActivate, Ui.accent());
		Ui.button(c, textRenderer, bx + btnW + 8, btnY, btnW, 20, "Wklej ze schowka", hoverPaste, 0xFF8B95A7);

		if (!message.isEmpty()) {
			c.drawText(textRenderer, message, bx, py + 138, messageColor, false);
		}
		c.drawText(textRenderer, "ESC - zamknij", px + 20, py + PH - 16, 0xFF6B7587, false);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int bx = px + 20;
			int bw = PW - 40;
			int btnY = py + 108;
			int btnW = (bw - 8) / 2;
			if (Ui.inside(mouseX, mouseY, bx, btnY, btnW, 20)) {
				tryActivate();
				return true;
			}
			if (Ui.inside(mouseX, mouseY, bx + btnW + 8, btnY, btnW, 20)) {
				paste();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if ((Character.isLetterOrDigit(chr) || chr == '-') && input.length() < MAX_LENGTH) {
			input.append(Character.toUpperCase(chr));
			message = "";
			return true;
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			if (input.length() > 0) {
				input.deleteCharAt(input.length() - 1);
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
		String clip = client.keyboard.getClipboard();
		input.setLength(0);
		for (char ch : clip.trim().toCharArray()) {
			if ((Character.isLetterOrDigit(ch) || ch == '-') && input.length() < MAX_LENGTH) {
				input.append(Character.toUpperCase(ch));
			}
		}
		message = "";
	}

	private void tryActivate() {
		if (LicenseManager.activate(input.toString())) {
			if (client != null) {
				client.setScreen(new PenguinScreen());
			}
		} else {
			message = "Nieprawidłowy klucz licencyjny.";
			messageColor = 0xFFFF6B6B;
		}
	}
}
