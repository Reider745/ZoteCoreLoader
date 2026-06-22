package org.innercore.icstd.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME = "start time";

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        context.getStore(ExtensionContext.Namespace.GLOBAL)
                .put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        long startTime = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .remove(START_TIME, long.class);
        System.out.printf("End test %s - %d ms%n", context.getDisplayName(), System.currentTimeMillis() - startTime);
    }
}