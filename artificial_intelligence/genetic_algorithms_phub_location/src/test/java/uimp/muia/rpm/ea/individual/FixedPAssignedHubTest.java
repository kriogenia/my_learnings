package uimp.muia.rpm.ea.individual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedPAssignedHubTest {

    @Test
    void correct() {
        var chromosome = new Byte[]{ 0, 1, 2, 1 };
        assertDoesNotThrow(() -> new FixedPAssignedHub(3, chromosome));
        assertThrows(AssertionError.class, () -> new FixedPAssignedHub(2, chromosome));
        assertThrows(AssertionError.class, () -> new FixedPAssignedHub(4, chromosome));
    }

}