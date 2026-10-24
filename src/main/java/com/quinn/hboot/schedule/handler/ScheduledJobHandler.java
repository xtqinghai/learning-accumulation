package com.quinn.hboot.schedule.handler;

/** Registered task handler. Only handlers implemented in code can be scheduled. */
public interface ScheduledJobHandler {

    String key();

    String name();

    String execute() throws Exception;
}
