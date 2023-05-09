package com.zhekasmirnov.apparatus.ecs;

import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import com.zhekasmirnov.apparatus.ecs.core.Query;

public class EntityManagerWithJsApi extends EntityManager {
    public void performQuery1(Query query, FixedQueryConsumer1<Object> consumer) {
        performQuery(query, consumer);
    }

    public void performQuery2(Query query, FixedQueryConsumer2<Object, Object> consumer) {
        performQuery(query, consumer);
    }

    public void performQuery3(Query query, FixedQueryConsumer3<Object, Object, Object> consumer) {
        performQuery(query, consumer);
    }

    public void performQuery4(Query query, FixedQueryConsumer4<Object, Object, Object, Object> consumer) {
        performQuery(query, consumer);
    }

    public void performQuery5(Query query, FixedQueryConsumer5<Object, Object, Object, Object, Object> consumer) {
        performQuery(query, consumer);
    }

    public void performQuery6(Query query, FixedQueryConsumer6<Object, Object, Object, Object, Object, Object> consumer) {
        performQuery(query, consumer);
    }
}
