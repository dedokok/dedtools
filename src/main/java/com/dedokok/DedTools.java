package com.dedokok;

//import com.dedokok.addons.AddonManager;
//import com.dedokok.addons.MeteorAddon;
import com.dedokok.events.game.OpenScreenEvent;
import com.dedokok.events.meteor.KeyInputEvent;
import com.dedokok.events.meteor.MouseClickEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.GuiThemes;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.tabs.Tabs;
import com.dedokok.mixin.MinecraftMixin;
import com.dedokok.renderer.Fonts;
import com.dedokok.renderer.Renderer2D;
import com.dedokok.systems.Systems;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.hud.screens.AddHudElementScreen;
import com.dedokok.systems.hud.screens.HudEditorScreen;
import com.dedokok.systems.hud.screens.HudElementScreen;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.AutoReceiver;
import com.dedokok.systems.modules.Feature.DiscordPresence;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.MeteorStarscript;
import com.dedokok.utils.misc.input.KeyAction;
import com.dedokok.utils.misc.input.KeyBinds;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import java.lang.invoke.MethodHandles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DedTools implements ClientModInitializer {
	public static final String MOD_ID = "dedtools";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ModMetadata MOD_META;
	public static final String NAME;
	public static final com.dedokok.utils.misc.Version VERSION;
	//public static final String BUILD_NUMBER;
	//apublic static MeteorAddon ADDON;

	public static DedTools INSTANCE;


	public static boolean isStartedCountt = false;
	int ticks = 0;

	public static Minecraft mc;
	public static final IEventBus EVENT_BUS = new EventBus();
	public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
	public static final Logger LOG;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	static {
		MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

		NAME = MOD_META.getName();
		LOG = LoggerFactory.getLogger(NAME);

		String versionString = MOD_META.getVersion().getFriendlyString();
		Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(versionString);
		// When building and running through IntelliJ and not Gradle it doesn't replace the version so just use a dummy
		versionString = !matcher.find() ? "0.0.0" : "%s.%s.%s".formatted(matcher.group(1), matcher.group(2), matcher.group(3));

		VERSION = new com.dedokok.utils.misc.Version(versionString);
	}
	@Override
	public void onInitializeClient() {
		if (INSTANCE == null) {
			INSTANCE = this;
			return;
		}

		mc = Minecraft.getInstance();

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOG.info("Force loading mixins");
			MixinEnvironment.getCurrentEnvironment().audit();
		}


		LOG.info("Initializing {}", NAME);
		// Pre-load
		if (!FOLDER.exists()) {
			FOLDER.getParentFile().mkdirs();
			FOLDER.mkdir();
			Systems.addPreLoadTask(() -> Modules.get().get(DiscordPresence.class).enable());
		}


		// Регистрируем lambda factory для своего пакета — обязательно ДО любых subscribe()
		EVENT_BUS.registerLambdaFactory("com.dedokok", (lookupInMethod, klass) ->
				(MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);
		Fonts.refresh();
		Tabs.init();
		GuiThemes.init();
		Categories.init();
		Systems.init();
		MeteorStarscript.init();
		Renderer2D.init();



		EVENT_BUS.subscribe(this);


		Modules.get().sortModules();
		Systems.load();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			long t0 = System.currentTimeMillis();
			DedTools.LOG.info("Shutdown hook started");

			Systems.save();
			GuiThemes.save();
			DedTools.LOG.info("Shutdown hook finished, total {} ms", System.currentTimeMillis() - t0);

		}));
		GuiThemes.postInit();
		GuiRenderer.init();
		isStartedCountt=true;
	}



	@EventHandler
	private void onTick(TickEvent.Post event) {
		ticks++;
		if(isStartedCountt && ticks >= 200) {
			Systems.save();
			ticks = 0;
		}
//		if (mc.gui.screen() == null && mc.gui.overlay() == null && KeyBinds.OPEN_COMMANDS.consumeClick()) {
//			mc.gui.setScreen(new ChatScreen(Config.get().prefix.get(), true));
//		}

		if (mc.player == null) return;

		boolean using = mc.player.isUsingItem(); // если метод называется иначе — Ctrl+Click по mc.player, найди актуальное имя
		if (using != lastUsingState) {
			System.out.println("[DEBUG] isUsingItem changed: " + lastUsingState + " -> " + using + " at " + System.currentTimeMillis());
			Thread.dumpStack();
			lastUsingState = using;
		}


	}
	private boolean lastUsingState = false;

	@EventHandler
	private void onKey(KeyInputEvent event) {
		if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matches(event.input)) {
			toggleGui();
		}
	}

	@EventHandler
	private void onMouseClick(MouseClickEvent event) {
		if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.click)) {
			toggleGui();
		}
	}

	private void toggleGui() {
		if (Utils.canCloseGui()) mc.gui.screen().onClose();
		else if (Utils.canOpenGui()) Tabs.get().getFirst().openScreen(GuiThemes.get());
	}

	// Hide HUD

	private boolean wasWidgetScreen, wasHudHiddenRoot;

	@EventHandler(priority = EventPriority.LOWEST)
	private void onOpenScreen(OpenScreenEvent event) {
		if (event.screen instanceof WidgetScreen) {
			if (!wasWidgetScreen) wasHudHiddenRoot = mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden;
			if (GuiThemes.get().hideHUD() || wasHudHiddenRoot) {
				// Always show the MC HUD in the HUD editor screen since people like
				// to align some items with the hotbar or chat
				mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden = !(event.screen instanceof HudEditorScreen)
						&& !(event.screen instanceof AddHudElementScreen)
						&& !(event.screen instanceof HudElementScreen);
			}
		} else {
			if (wasWidgetScreen) mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden = wasHudHiddenRoot;
			wasHudHiddenRoot = mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden;
		}

		wasWidgetScreen = event.screen instanceof WidgetScreen;
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(DedTools.MOD_ID, path);
	}

	public class TickState {
		public static boolean isStarted = false;
		public static int ticks = 0;
	}
}
