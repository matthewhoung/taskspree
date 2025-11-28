package com.hongsolo.taskspree.common.application.cqrs;

public interface IQueryBus {
    /**
     * Dispatches a query to its registered handler.
     * @param query The query to execute.
     * @return The result of the query execution.
     */
    <Q extends Query<R>, R> R execute(Q query);
}