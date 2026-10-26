package com.dmx.elibrary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PersistenceSetupTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void contextLoadsAndSeedDataIsApplied() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM book", Long.class)).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM patron", Long.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM loan", Long.class)).isZero();
    }
}
