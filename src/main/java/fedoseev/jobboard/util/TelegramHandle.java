package fedoseev.jobboard.util;

import fedoseev.jobboard.exception.BadRequestException;

import java.util.regex.Pattern;

public final class TelegramHandle {

    private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9_]{5,32}$");
    private static final Pattern PREFIX = Pattern.compile("^(https?://)?(www\\.)?(t\\.me|telegram\\.me)/", Pattern.CASE_INSENSITIVE);

    private TelegramHandle() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String handle = PREFIX.matcher(raw.trim()).replaceFirst("");
        if (handle.startsWith("@")) {
            handle = handle.substring(1);
        }
        if (handle.endsWith("/")) {
            handle = handle.substring(0, handle.length() - 1);
        }
        if (!VALID.matcher(handle).matches()) {
            throw new BadRequestException("Некорректный Telegram: укажите ник вида @username (5–32 символа: латиница, цифры, _)");
        }
        return handle;
    }
}
