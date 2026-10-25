package pl.penguin.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.penguin.visuals.ui.LicenseScreen;
import pl.penguin.visuals.ui.PenguinScreen;

public class PenguinVisualsClient implements ClientModInitializer {
	public static final String MOD_ID = "penguinvisuals";
	public static final Logger LOGGER = LoggerFactory.getLogger("PenguinVisuals");

	public static KeyBinding OPEN_KEY;

	@Override
	public void onInitializeClient() {
		PenguinConfig.load();
		LicenseManager.init();

		OPEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.penguinvisuals.open",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_9,
			"category.penguinvisuals"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_KEY.wasPressed()) {
				if (client.currentScreen == null && client.player != null) {
					client.setScreen(LicenseManager.isActive() ? new PenguinScreen() : new LicenseScreen());
				}
			}
		});

		HitEffects.register();
		PenguinHud.register();

		LOGGER.info("Penguin Visuals zaladowany. Menu: klawisz 9");
	}
}
