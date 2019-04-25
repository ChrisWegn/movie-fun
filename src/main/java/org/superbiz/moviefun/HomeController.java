package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;
import java.util.function.Supplier;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumsTransactionManager;
    private final PlatformTransactionManager moviesTransactionManager;

    public HomeController(
            MoviesBean moviesBean,
            AlbumsBean albumsBean,
            MovieFixtures movieFixtures,
            AlbumFixtures albumFixtures,
            @Qualifier("albumsTransactionManager") PlatformTransactionManager albumsTransactionManager,
            @Qualifier("moviesTransactionManager") PlatformTransactionManager moviesTransactionManager
    ) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionManager = albumsTransactionManager;
        this.moviesTransactionManager = moviesTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @Transactional(transactionManager = "albumsTransactionManager")
    private <T> T albumsTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional(transactionManager = "albumsTransactionManager")
    private <T> T moviesTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        moviesTransaction(() -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
            return null;
        });

        albumsTransaction(() -> {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            return null;
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
