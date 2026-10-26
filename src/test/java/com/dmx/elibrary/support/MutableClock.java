package com.dmx.elibrary.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Test clock whose instant can be set or advanced. */
public class MutableClock extends Clock {

    private Instant instant;

    public MutableClock(Instant initial) {
        this.instant = initial;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
