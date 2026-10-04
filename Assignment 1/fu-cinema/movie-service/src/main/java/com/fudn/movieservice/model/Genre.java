package com.fudn.movieservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "genres")
public class Genre {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String genreName;
    
    private String description;
}
