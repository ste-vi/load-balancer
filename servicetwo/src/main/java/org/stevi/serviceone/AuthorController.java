package org.stevi.serviceone;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/authors")
public class AuthorController {

    @SneakyThrows
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Author> getAuthors() {
        log.info("getting authors");
        return List.of(
                new Author(1L, "Author 1"),
                new Author(2L, "Author 2"),
                new Author(3L, "Author 3")
        );
    }

    record Author(Long id, String name) {

    }
}
