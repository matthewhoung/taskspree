package com.hongsolo.taskspree.common.infrastructure.cqrs;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.behaviors.PipelineBehavior;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class CommandBus implements ICommandBus {

    private final Map<Class<?>, CommandHandler> handlers = new ConcurrentHashMap<>();
    private final List<PipelineBehavior> behaviors;

    public <C extends Command<R>, R> void register(Class<C> commandClass, CommandHandler<C, R> handler) {
        if (handlers.containsKey(commandClass)) {
            throw new IllegalStateException("Duplicate handler registered for command: " + commandClass.getName());
        }
        handlers.put(commandClass, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> R execute(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler registered for command: " + command.getClass().getName());
        }

        PipelineBehavior.Delegate<R> pipeline = buildPipeline(command, handler);

        return pipeline.next();
    }

    /**
     * Constructs the chain of responsibility (pipeline) for the given command.
     */
    private <C extends Command<R>, R> PipelineBehavior.Delegate<R> buildPipeline(C command, CommandHandler<C, R> handler) {
        // The final step is always the actual handler execution
        PipelineBehavior.Delegate<R> pipeline = () -> handler.handle(command);

        // Wrap the pipeline with behaviors in reverse order
        // (Validation -> Logging -> Transaction -> Handler)
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            PipelineBehavior behavior = behaviors.get(i);
            PipelineBehavior.Delegate<R> next = pipeline;
            pipeline = () -> behavior.handle(command, next);
        }

        return pipeline;
    }
}