package world.bentobox.upgrades.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Material;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.mocks.ServerMocks;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
public class UpgradeTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private User user;
    @Mock
    private Island island;
    @Mock
    private UpgradesData upgradesData;
    @Mock
    private UpgradesManager um;
    @Mock
    private VaultHook vh;

    private TestUpgrade testUpgrade;
    private UUID userId;
    private String islandId;

    @Before
    public void setUp() {
        ServerMocks.newServer();

        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        islandId = UUID.randomUUID().toString();

        when(user.getUniqueId()).thenReturn(userId);
        when(island.getUniqueId()).thenReturn(islandId);
        when(addon.getAddonByName("upgrades")).thenReturn(java.util.Optional.of(addon));
        when(addon.getUpgradesLevels(islandId)).thenReturn(upgradesData);

        when(um.getIslandLevel(island)).thenReturn(20);
        when(addon.getUpgradesManager()).thenReturn(um);

        when(vh.has(any(), anyDouble())).thenReturn(true); // Player has money
        when(addon.getVaultHook()).thenReturn(vh);

        testUpgrade = new TestUpgrade(addon, "test_upgrade", "Test Upgrade", Material.DIAMOND);
    }

    @After
    public void tearDown() {
        ServerMocks.unsetBukkitServer();
    }

    @Test
    public void testUpgradeInitialization() {
        assertNotNull(testUpgrade.getUpgradesAddon());
        assertEquals("test_upgrade", testUpgrade.getName());
        assertEquals("Test Upgrade", testUpgrade.getDisplayName());
        assertEquals(Material.DIAMOND, testUpgrade.getIcon());
    }

    @Test
    public void testCanUpgrade_WithSufficientResources() {
        Upgrade.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isLevelProvided()).thenReturn(true);
        when(addon.getUpgradesManager().getIslandLevel(island)).thenReturn(5);
        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().has(user, 100)).thenReturn(true);

        assertTrue(testUpgrade.canUpgrade(user, island));
    }

    @Test
    public void testCanUpgrade_WithInsufficientResources() {
        Upgrade.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(10, 200, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isLevelProvided()).thenReturn(true);
        when(addon.getUpgradesManager().getIslandLevel(island)).thenReturn(5);
        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().has(user, 200)).thenReturn(false);

        assertFalse(testUpgrade.canUpgrade(user, island));
    }

    @Test
    public void testDoUpgrade_SuccessfulTransaction() {
        Upgrade.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().withdraw(user, 100))
                .thenReturn(new EconomyResponse(100, 0, EconomyResponse.ResponseType.SUCCESS, ""));

        testUpgrade.doUpgrade(user, island);
        verify(upgradesData).setUpgradeLevel("test_upgrade", 1);
    }

    @Test
    public void testDoUpgrade_FailedTransaction() {
        Upgrade.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().withdraw(user, 100))
                .thenReturn(new EconomyResponse(100, 0, EconomyResponse.ResponseType.FAILURE, "Error"));

        assertFalse(testUpgrade.doUpgrade(user, island));
    }

    @Test
    public void testGetAndSetDescription() {
        String description = "Upgrade description";
        testUpgrade.setOwnDescription(user, description);

        assertEquals(description, testUpgrade.getOwnDescription(user));
    }

    @Test
    public void testGetAndSetUpgradeValues() {
        Upgrade.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        assertEquals(upgradeValues, testUpgrade.getUpgradeValues(user));
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
