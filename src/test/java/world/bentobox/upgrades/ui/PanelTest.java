package world.bentobox.upgrades.ui;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class })
public class PanelTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private Island island;
    @Mock
    private UpgradesManager um;
    @Mock
    private World world;
    @Mock
    private User user;

    private UUID uuid;
    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Player p;
    private Panel panel;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // World
        when(world.toString()).thenReturn("world");
        // Player
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);
        when(user.getLocation()).thenReturn(location);
        when(user.getTranslation(anyString())).thenReturn("translation");

        when(um.getIslandLevel(island)).thenReturn(20);
        when(addon.getUpgradesManager()).thenReturn(um);

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);

        panel = new Panel(addon, island);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        User.clearUsers();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.ui.Panel#Panel(world.bentobox.upgrades.UpgradesAddon, world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testPanel() {
        assertNotNull(panel);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.ui.Panel#showPanel(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testShowPanel() {
        panel.showPanel(user);
        verify(p).openInventory(any(Inventory.class));
    }

}
