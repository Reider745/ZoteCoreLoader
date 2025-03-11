package com.reider745.api.hooks.instrument;

import com.reider745.Main;

public class BlockDump {
    public static void main(String[] args) throws Throwable {
        // Не делать никаких дополнительных действий!
        // Тут не тот ClassLoader в котором можно делать все, что хочешь!
        Main.INSTANCE.loader.addInitClass("com.reider745.instrument.BlockDump");
        Main.main(args);
    }
}
