package com.zhekasmirnov.apparatus.ecs;

import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class ECS {
    private static final EntityManager entityManager = new EntityManagerWithJsApi();

    @JSStaticFunction
    public static EntityManager getEntityManager() {
        return entityManager;
    }
}
