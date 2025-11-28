package com.hongsolo.taskspree.common.infrastructure.cqrs;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryBus implements IQueryBus {

    private final Map<Class<?>, QueryHandler> handlers = new ConcurrentHashMap<>();

    public <Q extends Query<R>, R> void register(Class<Q> queryClass, QueryHandler<Q, R> handler) {
        if (handlers.containsKey(queryClass)) {
            throw new IllegalStateException("Duplicate handler registered for query: " + queryClass.getName());
        }
        handlers.put(queryClass, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> R execute(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlers.get(query.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler registered for query: " + query.getClass().getName());
        }

        return handler.handle(query);
    }
}