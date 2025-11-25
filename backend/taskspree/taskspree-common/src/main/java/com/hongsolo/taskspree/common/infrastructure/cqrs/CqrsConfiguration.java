package com.hongsolo.taskspree.common.infrastructure.cqrs;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.GenericTypeResolver;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CqrsConfiguration {

    private final ApplicationContext applicationContext;
    private final SpringCommandBus commandBus;
    private final SpringQueryBus queryBus;

    @PostConstruct
    public void registerHandlers() {
        registerCommandHandlers();
        registerQueryHandlers();
    }

    @SuppressWarnings("unchecked")
    private void registerCommandHandlers() {
        // Get all beans that implement CommandHandler
        Map<String, CommandHandler> commandHandlers = applicationContext.getBeansOfType(CommandHandler.class);

        commandHandlers.values().forEach(handler -> {
            // Resolve the generic type <C, R> from CommandHandler<C, R>
            // Spring's GenericTypeResolver is robust against AOP proxies (e.g. @Transactional)
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);

            if (generics != null && generics.length >= 1) {
                Class<? extends Command> commandType = (Class<? extends Command>) generics[0];
                commandBus.register(commandType, handler);
                log.info("CQRS: Registered Handler for Command [{}]", commandType.getSimpleName());
            } else {
                log.warn("CQRS: Could not resolve generic type for handler [{}]", handler.getClass().getName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void registerQueryHandlers() {
        // Get all beans that implement QueryHandler
        Map<String, QueryHandler> queryHandlers = applicationContext.getBeansOfType(QueryHandler.class);

        queryHandlers.values().forEach(handler -> {
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), QueryHandler.class);

            if (generics != null && generics.length >= 1) {
                Class<? extends Query> queryType = (Class<? extends Query>) generics[0];
                queryBus.register(queryType, handler);
                log.info("CQRS: Registered Handler for Query [{}]", queryType.getSimpleName());
            } else {
                log.warn("CQRS: Could not resolve generic type for handler [{}]", handler.getClass().getName());
            }
        });
    }
}