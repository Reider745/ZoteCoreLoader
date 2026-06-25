package org.innercore.codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory replacement for the reference pipeline's intermediate text files
 * ({@code gen_java_bindings.txt} + {@code gen_java_modules.txt}). It is the single
 * sink that resolvers feed and the generator reads.
 *
 * <p>The Java resolver is the only producer today. A future C++ resolver (libclang)
 * can append its native {@link BindingRecord}s here as well; the generator already
 * understands native records, so generation would then "just work". If the C++ pass
 * runs out-of-process, this class is the natural place to add JSON-lines
 * (de)serialization.
 */
public final class BindingStore {
    public final List<BindingRecord> records = new ArrayList<BindingRecord>();
    /** moduleName -> fully-qualified hand-written module class. */
    public final Map<String, String> javaModules = new LinkedHashMap<String, String>();

    public void addRecord(BindingRecord record) {
        records.add(record);
    }

    public void addModule(String moduleName, String fullClassName) {
        javaModules.put(moduleName, fullClassName);
    }
}
