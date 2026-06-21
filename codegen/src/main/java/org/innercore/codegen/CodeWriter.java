package org.innercore.codegen;

/**
 * A tiny indentation-aware text buffer, mirroring the reference {@code BaseGenerator}
 * write/writeln helpers. {@code write} emits an optional leading indent then the
 * given parts with no trailing newline; {@code writeln} emits each line with an
 * optional indent and a trailing newline.
 */
public final class CodeWriter {
    private final StringBuilder sb = new StringBuilder();
    private final String indentChars;

    public CodeWriter() {
        this("    ");
    }

    public CodeWriter(String indentChars) {
        this.indentChars = indentChars;
    }

    public String text() {
        return sb.toString();
    }

    public void reset() {
        sb.setLength(0);
    }

    private void indent(int n) {
        for (int i = 0; i < n; i++) {
            sb.append(indentChars);
        }
    }

    public void write(String... parts) {
        write(0, parts);
    }

    public void write(int indent, String... parts) {
        if (indent > 0) {
            indent(indent);
        }
        for (String p : parts) {
            sb.append(p);
        }
    }

    /** Emit a single blank line. */
    public void writeln() {
        sb.append('\n');
    }

    public void writeln(String... lines) {
        writeln(0, lines);
    }

    public void writeln(int indent, String... lines) {
        if (lines.length == 0) {
            sb.append('\n');
            return;
        }
        for (String line : lines) {
            if (indent > 0 && line.length() > 0) {
                indent(indent);
            }
            sb.append(line).append('\n');
        }
    }
}
