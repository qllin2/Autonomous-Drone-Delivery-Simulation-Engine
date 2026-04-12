package drone;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DefaultDroneComparator - priority-aging")
class DefaultDroneComparatorTest {

    DefaultDroneComparator comparator;

    @BeforeEach
    void setUp() {
        Simulation simulation = mock(Simulation.class);
        when(simulation.now()).thenReturn(20); // set a known current tick
        comparator = new DefaultDroneComparator(simulation);
    }

    @Test
    @DisplayName("drone waiting more than 12 ticks should be force-promoted (anti-starvation)")
    void starvingDroneIsPromoted() {
        Drone starving = mock(Drone.class);
        Drone normal = mock(Drone.class);
        when(starving.getAccessWaitTicks(20)).thenReturn(13); // exceeds threshold of 12
        when(normal.getAccessWaitTicks(20)).thenReturn(5); // within threshold of 12
        // starving drone should rank before normal (negative return value)
        assertTrue(comparator.compare(starving, normal) < 0);
    }

    @Test
    @DisplayName("fragile drone base score 24 should outrank heavy drone base score 8")
    void fragileDroneHasHigherPriority() {
        Drone fragile = mock(Drone.class);
        Drone heavy = mock(Drone.class);
        when(fragile.getAccessWaitTicks(20)).thenReturn(0);
        when(heavy.getAccessWaitTicks(20)).thenReturn(0);
        when(fragile.wasCarryingFragile()).thenReturn(true);
        when(fragile.wasCarryingHeavy()).thenReturn(false);
        when(heavy.wasCarryingFragile()).thenReturn(false);
        when(heavy.wasCarryingHeavy()).thenReturn(true);
        assertTrue(comparator.compare(fragile, heavy) < 0);
    }

    @Test
    @DisplayName("aging boost (AGING_WEIGHT=3) should allow a normal drone to outrank a heavy drone")
    void agingBoostCanOverrideBaseScore() {
        Drone aged = mock(Drone.class);
        Drone heavy = mock(Drone.class);
        // aged waited 3 ticks -> ageBoost = 3 * 3 = 9 > HEAVY_BASE = 8
        when(aged.getAccessWaitTicks(20)).thenReturn(3);
        when(heavy.getAccessWaitTicks(20)).thenReturn(0);
        when(aged.wasCarryingFragile()).thenReturn(false);
        when(aged.wasCarryingHeavy()).thenReturn(false);
        when(heavy.wasCarryingFragile()).thenReturn(false);
        when(heavy.wasCarryingHeavy()).thenReturn(true);
        assertTrue(comparator.compare(aged, heavy) < 0);
    }

    @Test
    @DisplayName("when both drones are promoted, the longer-waiting drone goes first")
    void bothPromotedOlderWaiterGoesFirst() {
        Drone older = mock(Drone.class);
        Drone newer = mock(Drone.class);
        when(older.getAccessWaitTicks(20)).thenReturn(15);
        when(newer.getAccessWaitTicks(20)).thenReturn(13);
        assertTrue(comparator.compare(older, newer) < 0);
    }
}
