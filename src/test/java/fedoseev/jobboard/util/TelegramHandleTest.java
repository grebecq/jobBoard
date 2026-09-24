package fedoseev.jobboard.util;

import fedoseev.jobboard.exception.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TelegramHandleTest {

    @ParameterizedTest
    @ValueSource(strings = {"ivan_dev", "@ivan_dev", " @ivan_dev ", "t.me/ivan_dev", "https://t.me/ivan_dev", "https://t.me/ivan_dev/", "HTTPS://T.ME/ivan_dev"})
    void normalize_stripsPrefixes(String raw) {
        assertEquals("ivan_dev", TelegramHandle.normalize(raw));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void normalize_blankIsNull(String raw) {
        assertNull(TelegramHandle.normalize(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "иван_дев", "ivan dev", "https://evil.com/ivan_dev", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void normalize_rejectsInvalid(String raw) {
        assertThrows(BadRequestException.class, () -> TelegramHandle.normalize(raw));
    }
}
