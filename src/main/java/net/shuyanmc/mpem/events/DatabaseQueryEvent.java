package net.shuyanmc.mpem.events;

import net.shuyanmc.mpem.AsyncEvent;
import net.shuyanmc.mpem.AsyncHandler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@AsyncHandler
public class DatabaseQueryEvent extends AsyncEvent {
    private final String query;
    private final CompletableFuture<DatabaseQueryEvent> future = new CompletableFuture<>();
    private Object result;
    private Throwable error;

    public DatabaseQueryEvent(String query, boolean async) {
        super(async);
        this.query = query;
    }

    public static CompletableFuture<DatabaseQueryEvent> query(String query, boolean async, Consumer<DatabaseQueryEvent> handler) {
        DatabaseQueryEvent event = new DatabaseQueryEvent(query, async);
        if (handler != null) {
            event.getFuture().thenAccept(handler);
        }
        event.post();
        return event.getFuture();
    }
/*
    public Result getResult() {
        return (Result) result;
    }*/

    public String getQuery() {
        return query;
    }

    public Throwable getError() {
        return error;
    }

    public CompletableFuture<DatabaseQueryEvent> getFuture() {
        return future;
    }

    @Override
    protected void handleEvent(AsyncEvent event) {
        DatabaseQueryEvent queryEvent = (DatabaseQueryEvent) event;
        try {
            // 模拟数据库查询
            Thread.sleep(100); // 模拟IO延迟
            queryEvent.result = "Result for: " + queryEvent.query;
            queryEvent.future.complete(queryEvent);
        } catch (InterruptedException e) {
            queryEvent.error = e;
            queryEvent.future.completeExceptionally(e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            queryEvent.error = e;
            queryEvent.future.completeExceptionally(e);
        }
    }
}