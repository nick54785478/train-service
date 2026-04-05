package com.example.demo.base.infra.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.demo.base.shared.event.BaseEvent;

/**
 * ThreadLocal Event Store
 * 負責管理 eventId 與跨 Aggregate 的 Domain Events
 * 不污染 Aggregate 本身
 */
public class EventContext {

    private String eventId;
    private final List<BaseEvent> domainEvents = new ArrayList<>();

    private static final ThreadLocal<EventContext> THREAD_LOCAL = ThreadLocal.withInitial(EventContext::new);

    public static EventContext current() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void addEvent(BaseEvent event) {
        domainEvents.add(event);
    }

    public List<BaseEvent> getEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearEvents() {
        domainEvents.clear();
    }
}