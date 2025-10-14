package eu.ill.visa.core.domain;


import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleDuration {

    private static final Pattern PATTERN = Pattern.compile("(?i)(\\d+)([mhd])");
    private static final Pattern MULTI_PATTERN = Pattern.compile("(?i)(\\d+)([mhd])");

    private final String durationText;
    private final Duration duration;

    public SimpleDuration(String durationText) {
        this.durationText = durationText;
        this.duration = this.parse(durationText);
    }

    public SimpleDuration(Duration duration) {
        this.duration = duration;
        this.durationText = this.format(duration);
    }

    public SimpleDuration(Long durationMinutes) {
        this.duration = Duration.ofMinutes(durationMinutes);
        this.durationText = this.format(duration);
    }

    public String getDurationText() {
        return durationText;
    }

    public Duration getDuration() {
        return duration;
    }

    private Duration parse(String text) {
        Matcher m = MULTI_PATTERN.matcher(text.trim());
        Duration total = Duration.ZERO;
        while (m.find()) {
            long value = Long.parseLong(m.group(1));
            total = switch (m.group(2).toLowerCase()) {
                case "m" -> total.plusMinutes(value);
                case "h" -> total.plusHours(value);
                case "d" -> total.plusDays(value);
                default -> total;
            };
        }
        if (total.isZero()) {
            throw new IllegalArgumentException("Invalid duration: " + text);
        }
        return total;
    }


    private String format(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0m";
        }

        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d");
        if (hours > 0) sb.append(hours).append("h");
        if (minutes > 0 || sb.isEmpty()) sb.append(minutes).append("m");

        return sb.toString();
    }


}
