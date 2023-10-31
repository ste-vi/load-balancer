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
@RequestMapping("/books")
public class BookController {

    @SneakyThrows
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Book> getBooks() {
        Thread.sleep(20000);
        log.info("getting books");
        return List.of(
                new Book(1L, "Book 1", LocalDate.now().minusYears(10)),
                new Book(2L, "Book 2", LocalDate.now().minusYears(20)),
                new Book(3L, "Book 3", LocalDate.now().minusYears(30))
        );
    }

    @GetMapping("/ok")
    @ResponseStatus(HttpStatus.OK)
    public void test() {
        log.info("getting books");
    }

    private record Book(Long id, String name, LocalDate releaseDate) {

    }
}
