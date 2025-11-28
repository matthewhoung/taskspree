package com.hongsolo.taskspree.common.application.cqrs;

public interface ICommandBus {
    /**
     * Dispatches a command to its registered handler.
     * @param command The command to execute.
     * @return The result of the command execution.
     */
    <C extends Command<R>, R> R execute(C command);
}