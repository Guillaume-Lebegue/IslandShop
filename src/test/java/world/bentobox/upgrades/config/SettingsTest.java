package world.bentobox.upgrades.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.config.Settings.Expression;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
public class SettingsTest {

    @Mock
    private UpgradesAddon addon;
    private Settings settings;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Config
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File("src/main/resources/config.yml");
        assertTrue(configFile.exists());
        config.load(configFile);

        when(addon.getConfig()).thenReturn(config);

        settings = new Settings(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#Settings(world.bentobox.upgrades.UpgradesAddon)}.
     */
    @Test
    public void testSettings() {
        assertNotNull(settings);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDisabledGameModes()}.
     */
    @Test
    public void testGetDisabledGameModes() {
        assertTrue(settings.getDisabledGameModes().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getHasRangeUpgrade()}.
     */
    @Test
    public void testGetHasRangeUpgrade() {
        assertTrue(settings.getHasRangeUpgrade());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxRangeUpgrade(java.lang.String)}.
     */
    @Test
    public void testGetMaxRangeUpgrade() {
        assertEquals(10, settings.getMaxRangeUpgrade(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultRangeUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultRangeUpgradeTierMap() {
        assertFalse(settings.getDefaultRangeUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonRangeUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonRangeUpgradeTierMap() {
        assertTrue(settings.getAddonRangeUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxBlockLimitsUpgrade(org.bukkit.Material, java.lang.String)}.
     */
    @Test
    public void testGetMaxBlockLimitsUpgrade() {
        assertEquals(0, settings.getMaxBlockLimitsUpgrade(Material.STONE, ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultBlockLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultBlockLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultBlockLimitsUpgradeTierMap().isEmpty());

    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonBlockLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonBlockLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonBlockLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaterialsLimitsUpgrade()}.
     */
    @Test
    public void testGetMaterialsLimitsUpgrade() {
        assertFalse(settings.getMaterialsLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityIcon(org.bukkit.entity.EntityType)}.
     */
    @Test
    public void testGetEntityIcon() {
        assertEquals(null, settings.getEntityIcon(EntityType.CAT));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityGroupIcon(java.lang.String)}.
     */
    @Test
    public void testGetEntityGroupIcon() {
        assertEquals(null, settings.getEntityGroupIcon(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxEntityLimitsUpgrade(org.bukkit.entity.EntityType, java.lang.String)}.
     */
    @Test
    public void testGetMaxEntityLimitsUpgrade() {
        assertEquals(0, settings.getMaxEntityLimitsUpgrade(EntityType.MINECART_HOPPER, ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxEntityGroupLimitsUpgrade(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetMaxEntityGroupLimitsUpgrade() {
        assertEquals(0, settings.getMaxEntityGroupLimitsUpgrade("", ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultEntityLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityGroupLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultEntityGroupLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityGroupLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonEntityLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityGroupLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonEntityGroupLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityGroupLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityLimitsUpgrade()}.
     */
    @Test
    public void testGetEntityLimitsUpgrade() {
        assertFalse(settings.getEntityLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityGroupLimitsUpgrade()}.
     */
    @Test
    public void testGetEntityGroupLimitsUpgrade() {
        assertFalse(settings.getEntityGroupLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxCommandUpgrade(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetMaxCommandUpgrade() {
        assertEquals(0, settings.getMaxCommandUpgrade("", ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultCommandUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultCommandUpgradeTierMap() {
        assertFalse(settings.getDefaultCommandUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonCommandUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonCommandUpgradeTierMap() {
        assertTrue(settings.getAddonCommandUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandUpgrade()}.
     */
    @Test
    public void testGetCommandUpgrade() {
        assertFalse(settings.getCommandUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandIcon(java.lang.String)}.
     */
    @Test
    public void testGetCommandIcon() {
        assertNull(settings.getCommandIcon(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandName(java.lang.String)}.
     */
    @Test
    public void testGetCommandName() {
        assertEquals(null, settings.getCommandName(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#parse(java.lang.String, java.util.Map)}.
     */
    @Test
    public void testParse() {
        Expression expression = Settings.parse("40*200", Map.of());
        assertEquals(8000D, expression.eval(), 0.1D);
    }

}
