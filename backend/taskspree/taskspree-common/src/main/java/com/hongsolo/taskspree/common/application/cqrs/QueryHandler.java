package com.hongsolo.taskspree.common.application.cqrs;

/**
 * Defines a handler for a specific query.
 * @param <Q> The type of the query to handle.
 * @param <R> The type of the result.
 */
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}