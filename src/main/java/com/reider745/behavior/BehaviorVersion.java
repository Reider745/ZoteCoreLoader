package com.reider745.behavior;

/** Numeric comparison of Bedrock format versions (1.16 and 1.16.0 are equivalent). */
public record BehaviorVersion(int major, int minor, int patch) implements Comparable<BehaviorVersion> {
    public static BehaviorVersion parse(String value) {
        if (!value.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("Invalid behavior format version: " + value);
        }
        String[] parts = value.split("\\.");
        return new BehaviorVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                parts.length == 3 ? Integer.parseInt(parts[2]) : 0);
    }

    public static void requireSupported(String value, String minimum) {
        BehaviorVersion version = parse(value);
        if (version.compareTo(parse(minimum)) < 0 || version.compareTo(parse("1.16.201")) > 0) {
            throw new IllegalArgumentException("Unsupported behavior format version: " + value);
        }
    }

    @Override
    public int compareTo(BehaviorVersion other) {
        int result = Integer.compare(major, other.major);
        if (result == 0) result = Integer.compare(minor, other.minor);
        return result == 0 ? Integer.compare(patch, other.patch) : result;
    }
}
