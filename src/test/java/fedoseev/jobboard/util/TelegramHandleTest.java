package fedoseev.jobboard.util;

import fedoseev.jobboard.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelegramHandleTest {

    @Test
    void normalize_acceptsLinksAndNicks_rejectsGarbage() {
        for (String raw : List.of("ivan_dev", "@ivan_dev", " @ivan_dev ", "t.me/ivan_dev", "https://t.me/ivan_dev/")) {
            assertEquals("ivan_dev", TelegramHandle.normalize(raw), raw);
        }
        assertNull(TelegramHandle.normalize("  "));
        for (String raw : List.of("abc", "иван_дев", "https://evil.com/ivan_dev")) {
            assertThrows(BadRequestException.class, () -> TelegramHandle.normalize(raw), raw);
        }
    }
}
