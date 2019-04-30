package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater, DataSource dataSource) {
        this.albumsUpdater = albumsUpdater;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            if (startAlbumSchedulerTask()) {
                updateLastRunTimestamp();
                logger.debug("Starting albums update");
                albumsUpdater.update();
                logger.debug("Finished albums update");
            } else {
                logger.debug("Nothing to start");
            }
        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private boolean startAlbumSchedulerTask() {
        logger.warn("Checking whether albums have been updated recently");
        Long timeDiff = jdbcTemplate.queryForObject("SELECT UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP((SELECT started_at from album_scheduler_task LIMIT 1));", Long.class);
        logger.warn("Albums last updated {} seconds ago", timeDiff);
        return timeDiff > 120L;
    }

    private void updateLastRunTimestamp() {
        logger.warn("Updated timestamp for album update");
        jdbcTemplate.execute("UPDATE album_scheduler_task SET started_at = NOW();");
    }
}
