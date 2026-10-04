package com.fudn.movieservice.repository;

import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public MovieRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Movie> searchMovies(String keyword, String genreId, MovieStatus status) {
        Query query = new Query();
        if (keyword != null && !keyword.trim().isEmpty()) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("movieTitle").regex(keyword, "i"),
                    Criteria.where("movieDirector").regex(keyword, "i")
            );
            query.addCriteria(keywordCriteria);
        }
        if (genreId != null && !genreId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("genreId").is(genreId));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        return mongoTemplate.find(query, Movie.class);
    }
}
