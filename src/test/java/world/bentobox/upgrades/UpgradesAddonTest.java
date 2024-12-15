package world.bentobox.upgrades;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.mocks.ServerMocks;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, IslandsManager.class })
public class UpgradesAddonTest {

    private static File jFile;
    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private BentoBox plugin;
    @Mock
    private FlagsManager fm;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private AddonsManager am;
    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private Settings pluginSettings;

    private UpgradesAddon addon;

    @Mock
    private Logger logger;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private CompositeCommand cmd;
    @Mock
    private CompositeCommand adminCmd;
    @Mock
    private World world;
    private UUID uuid;

    @Mock
    private PluginManager pim;
    @Mock
    private VaultHook vh;
    private @NonNull String targetIslandId = UUID.randomUUID().toString();

    @BeforeClass
    public static void beforeClass() throws IOException {
        // Make the addon jar
        jFile = new File("addon.jar");
        // Copy over config file from src folder
        Path fromPath = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(fromPath, path);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry entry = new JarEntry(path.toString());
                tempJarOutputStream.putNextEntry(entry);
                while ((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        ServerMocks.newServer();
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        PowerMockito.mockStatic(IslandsManager.class, Mockito.RETURNS_MOCKS);
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);

        // Player has island to begin with
        when(im.getIsland(any(), any(UUID.class))).thenReturn(island);
        when(plugin.getIslands()).thenReturn(im);

        // Locales
        // Return the reference (USE THIS IN THE FUTURE)
        when(user.getTranslation(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Server
        Server server = mock(Server.class);
        when(Bukkit.getServer()).thenReturn(server);
        when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

        // Addon
        addon = new UpgradesAddon();
        File dataFolder = new File("addons/Bank");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "Upgrades", "1.3").description("test")
                .authors("tastybento").build();
        addon.setDescription(desc);
        // Addons manager
        when(am.getGameModeAddons()).thenReturn(List.of()); // No game modes
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test")
                .authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);
        when(gameMode.getOverWorld()).thenReturn(world);

        // Player command
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd);
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
        // Admin command
        Optional<CompositeCommand> opAdminCmd = Optional.of(adminCmd);
        when(gameMode.getAdminCommand()).thenReturn(opAdminCmd);

        // Flags manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        // Bukkit
        when(Bukkit.getScheduler()).thenReturn(scheduler);
        ItemMeta meta = mock(ItemMeta.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(itemFactory.getItemMeta(any())).thenReturn(meta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        when(Bukkit.getUnsafe()).thenReturn(unsafe);
        when(Bukkit.getPluginManager()).thenReturn(pim);

        // placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // World
        when(world.getName()).thenReturn("bskyblock-world");
        // Island
        when(island.getWorld()).thenReturn(world);
        when(island.getOwner()).thenReturn(uuid);

        // Vault
        when(plugin.getVault()).thenReturn(Optional.of(vh));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        ServerMocks.unsetBukkitServer();
        deleteAll(new File("database"));
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#onEnable()}.
     */
    @Test
    public void testOnEnableDisabled() {
        addon.onLoad();
        addon.setState(State.DISABLED);
        addon.onEnable();
        verify(plugin).logWarning("[Upgrades] Upgrades Addon is not available or disabled!");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#onEnable()}.
     */
    @Test
    public void testOnEnableNoAddons() {
        addon.onLoad();
        addon.setState(State.ENABLED);
        addon.onEnable();
        verify(plugin).logWarning("[Upgrades] Level addon not found so Upgrades won't look for Island Level");
        verify(plugin).logWarning("[Upgrades] Limits addon not found so Island Upgrade won't look for IslandLevel");
        verify(plugin).log("[Upgrades] Upgrades addon enabled");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#onDisable()}.
     */
    @Test
    public void testOnDisable() {
        addon.onDisable();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#onLoad()}.
     */
    @Test
    public void testOnLoad() {
        addon.onLoad();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#onReload()}.
     */
    @Test
    public void testOnReload() {
        addon.onReload();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getSettings()}.
     */
    @Test
    public void testGetSettings() {
        addon.getSettings();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getUpgradesManager()}.
     */
    @Test
    public void testGetUpgradesManager() {
        addon.getUpgradesManager();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getDatabase()}.
     */
    @Test
    public void testGetDatabase() {
        addon.getDatabase();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getUpgradesLevels(java.lang.String)}.
     */
    @Test
    public void testGetUpgradesLevels() {
        addon.onLoad();
        addon.onEnable();
        addon.getUpgradesLevels(targetIslandId);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#uncacheIsland(java.lang.String, boolean)}.
     */
    @Test
    public void testUncacheIsland() {
        addon.uncacheIsland(targetIslandId, false);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getLevelAddon()}.
     */
    @Test
    public void testGetLevelAddon() {
        addon.getLevelAddon();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getLimitsAddon()}.
     */
    @Test
    public void testGetLimitsAddon() {
        addon.getLimitsAddon();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getVaultHook()}.
     */
    @Test
    public void testGetVaultHook() {
        addon.getVaultHook();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#isLevelProvided()}.
     */
    @Test
    public void testIsLevelProvided() {
        assertFalse(addon.isLevelProvided());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#isLimitsProvided()}.
     */
    @Test
    public void testIsLimitsProvided() {
        assertFalse(addon.isLimitsProvided());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#isVaultProvided()}.
     */
    @Test
    public void testIsVaultProvided() {
        assertFalse(addon.isVaultProvided());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#getAvailableUpgrades()}.
     */
    @Test
    public void testGetAvailableUpgrades() {
        addon.getAvailableUpgrades();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.UpgradesAddon#registerUpgrade(world.bentobox.upgrades.api.Upgrade)}.
     */
    @Test
    public void testRegisterUpgrade() {
        Upgrade upgrade = new TestUpgrade(addon, "name", "Name", Material.ACACIA_BOAT);
        addon.registerUpgrade(upgrade);
    }

    private static class TestUpgrade extends Upgrade {

        public TestUpgrade(UpgradesAddon addon, String name, String displayName, Material icon) {
            super(addon, name, displayName, icon);
        }

        @Override
        public void updateUpgradeValue(User user, Island island) {
            // Test implementation
        }

        @Override
        public boolean isShowed(User user, Island island) {
            return true;
        }
    }

}
