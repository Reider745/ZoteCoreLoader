package com.zhekasmirnov.apparatus.ecs.core;

public abstract class EntitySystem {
    private final Query query;

    protected final DelayedActionQueue queue = new DelayedActionQueue();
    private int amortizedQueueCapacity = 16;
    private static final int AMORTIZED_CAPACITY_THRESHOLD = 64;
    private static final float CAPACITY_AMORTIZATION_VALUE = 0.3f;
    private static final float CAPACITY_CLEAR_THRESHOLD = 2.5f;

    public EntitySystem(Query query) {
        this.query = query;
    }

    public void run(EntityManager entityManager) {
        queue.reserve(amortizedQueueCapacity);
        queue.setEntityManager(entityManager);
        performQuery(entityManager, query);
        queue.flushNoClear();
        int count = queue.getEntityCount();
        if (amortizedQueueCapacity < AMORTIZED_CAPACITY_THRESHOLD) {
            amortizedQueueCapacity = Math.max(amortizedQueueCapacity, count);
        } else {
            amortizedQueueCapacity += (int) ((count - amortizedQueueCapacity) * CAPACITY_AMORTIZATION_VALUE);
        }
        if (amortizedQueueCapacity * CAPACITY_CLEAR_THRESHOLD < queue.getEntityCapacity()) {
            queue.clear();
        } else {
            queue.clearNoDealloc();
        }
    }

    abstract void performQuery(EntityManager entityManager, Query query);


    public static abstract class Of extends EntitySystem implements EntityManager.VariadicQueryConsumer {
        public Of(Class<?>... cls) {
            super(new Query(cls));
        }

        public Of(String... type) {
            super(new Query(type));
        }

        public Of(Object... type) {
            super(new Query(type));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performVariadicQuery(query, this);
        }
    }

    public static abstract class Of0 extends EntitySystem implements EntityManager.FixedQueryConsumer0 {
        public Of0(String... tags) {
            super(new Query(tags));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of1<T1> extends EntitySystem implements EntityManager.FixedQueryConsumer1<T1> {
        public Of1(Class<T1> cls1, String... tags) {
            super(new Query(tags, cls1));
        }

        public Of1(String type1, String... tags) {
            super(new Query(tags, type1));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of2<T1, T2> extends EntitySystem implements EntityManager.FixedQueryConsumer2<T1, T2> {
        public Of2(Class<T1> cls1, Class<T2> cls2, String... tags) {
            super(new Query(tags, cls1, cls2));
        }

        public Of2(String type1, String type2, String... tags) {
            super(new Query(tags, type1, type2));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of3<T1, T2, T3> extends EntitySystem implements EntityManager.FixedQueryConsumer3<T1, T2, T3> {
        public Of3(Class<T1> cls1, Class<T2> cls2, Class<T2> cls3, String... tags) {
            super(new Query(tags, cls1, cls2, cls3));
        }

        public Of3(String type1, String type2, String type3, String... tags) {
            super(new Query(tags, type1, type2, type3));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of4<T1, T2, T3, T4> extends EntitySystem implements EntityManager.FixedQueryConsumer4<T1, T2, T3, T4> {
        public Of4(Class<T1> cls1, Class<T2> cls2, Class<T3> cls3, Class<T4> cls4, String... tags) {
            super(new Query(tags, cls1, cls2, cls3, cls4));
        }

        public Of4(String type1, String type2, String type3, String type4, String... tags) {
            super(new Query(tags, type1, type2, type3, type4));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of5<T1, T2, T3, T4, T5> extends EntitySystem implements EntityManager.FixedQueryConsumer5<T1, T2, T3, T4, T5> {
        public Of5(Class<T1> cls1, Class<T2> cls2, Class<T3> cls3, Class<T4> cls4, Class<T5> cls5, String... tags) {
            super(new Query(tags, cls1, cls2, cls3, cls4, cls5));
        }

        public Of5(String type1, String type2, String type3, String type4, String type5, String... tags) {
            super(new Query(tags, type1, type2, type3, type4, type5));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }

    public static abstract class Of6<T1, T2, T3, T4, T5, T6> extends EntitySystem implements EntityManager.FixedQueryConsumer6<T1, T2, T3, T4, T5, T6> {
        public Of6(Class<T1> cls1, Class<T2> cls2, Class<T2> cls3, Class<T4> cls4, Class<T5> cls5, Class<T6> cls6, String... tags) {
            super(new Query(tags, cls1, cls2, cls3, cls3, cls4, cls5, cls6));
        }

        public Of6(String type1, String type2, String type3, String type4, String type5, String type6, String... tags) {
            super(new Query(tags, type1, type2, type3, type3, type4, type5, type6));
        }

        void performQuery(EntityManager entityManager, Query query) {
            entityManager.performQuery(query, this);
        }
    }
}
