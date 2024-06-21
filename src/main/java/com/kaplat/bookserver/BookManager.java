package com.kaplat.bookserver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@RestController
public class BookManager {
    private static final Logger requestLogger = (Logger) LoggerFactory.getLogger("request-logger");
    private static final Logger booksLogger = (Logger) LoggerFactory.getLogger("books-logger");
    private final Map<Integer, Book> idToBookDB = new HashMap<>();
    private int nextID = 0;
    private Integer requestCount = 0;

    @GetMapping("/books/health")
    public String health() {
        requestCount++;
        requestLoggerInfo("/books/health", "GET");
        Instant begin = Instant.now();
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        return "OK";
    }

    @PostMapping("/book")
    public ResponseEntity<?> createBook(@RequestBody final Book book) {
        requestCount++;
        requestLoggerInfo("/book", "POST");
        Instant begin = Instant.now();
        if (idToBookDB.values().stream().anyMatch(b -> b.getTitle().equalsIgnoreCase(book.getTitle()))) {
            booksLogger.error("Error: Book with the title [{}] already exists in the system", book.getTitle());
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Book with the title [" + book.getTitle() + "] already exists in the system"), HttpStatus.CONFLICT);
        }
        else if (book.getYear() < 1940 || book.getYear() > 2100) {
            booksLogger.error("Error: Can’t create new Book that its year [{}] is not in the accepted range [1940 -> 2100]", book.getYear());
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Can’t create new Book that its year [" + book.getYear() +"] is not in the accepted range [1940 -> 2100]"), HttpStatus.CONFLICT);
        }
        else if (book.getPrice() <= 0) {
            booksLogger.error("Error: Can’t create new Book with negative price");
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Can’t create new Book with negative price"), HttpStatus.CONFLICT);
        }
        else {
            booksLogger.info("Creating new Book with Title [{}]", book.getTitle());
            booksLogger.debug("Currently there are {} Books in the system. New Book will be assigned with id {}", nextID, nextID + 1);
            nextID++;
            book.setId(nextID);
            idToBookDB.put(nextID, book);
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(nextID, ""), HttpStatus.OK);
        }
    }

    @GetMapping("/books/total")
    public ResponseEntity<?> getBooksTotal(@RequestParam("author") Optional<String> author,
                                           @RequestParam("price-bigger-than") Optional<Integer> price_bigger_than,
                                           @RequestParam("price-less-than") Optional<Integer> price_less_than,
                                           @RequestParam("year-bigger-than") Optional<Integer> year_bigger_than,
                                           @RequestParam("year-less-than") Optional<Integer> year_less_than,
                                           @RequestParam("genres") Optional<String> genres) {
        requestCount++;
        requestLoggerInfo("/books/total", "GET");
        Instant begin = Instant.now();
        Stream<Book> books = idToBookDB.values().stream();
        if (author.isPresent()) {
            books = books.filter(book -> book.getAuthor().equalsIgnoreCase(author.get()));
        }
        if (price_bigger_than.isPresent()) {
            books = books.filter(book -> book.getYear() >= price_bigger_than.get());
        }
        if (price_less_than.isPresent()) {
            books = books.filter((book -> book.getPrice() <= price_less_than.get()));
        }
        if (year_bigger_than.isPresent()) {
            books = books.filter(book -> book.getYear() >= year_bigger_than.get());
        }
        if (year_less_than.isPresent()) {
            books = books.filter(book -> book.getYear() <= year_less_than.get());
        }
        if (genres.isPresent()) {
            String genres_raw = genres.get();
            if (!genres_raw.equals(genres_raw.toUpperCase())) {
                Instant end = Instant.now();
                requestLoggerDebug(Duration.between(begin, end));
                return new ResponseEntity<>(new ResponseObject<>(null, ""), HttpStatus.BAD_REQUEST);
            }
            else {
                List<String> genresInputList = new ArrayList<>(Arrays.asList(genres_raw.split(",")));
                books = books.filter(book -> Collections.disjoint(book.getGenres(), genresInputList));
            }
        }
        long booksCount = books.count();
        booksLogger.info("Total Books found for requested filters is {}", booksCount);
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        return new ResponseEntity<>(new ResponseObject<>(booksCount, ""), HttpStatus.OK);
    }

    @GetMapping("/books")
    public ResponseEntity<?> getBooksData(@RequestParam("author") Optional<String> author,
                                          @RequestParam("price-bigger-than") Optional<Integer> price_bigger_than,
                                          @RequestParam("price-less-than") Optional<Integer> price_less_than,
                                          @RequestParam("year-bigger-than") Optional<Integer> year_bigger_than,
                                          @RequestParam("year-less-than") Optional<Integer> year_less_than,
                                          @RequestParam("genres") Optional<String> genres) {
        requestCount++;
        requestLoggerInfo("/books", "GET");
        Instant begin = Instant.now();
        Stream<Book> books = idToBookDB.values().stream();
        if (author.isPresent()) {
            books = books.filter(book -> book.getAuthor().equalsIgnoreCase(author.get()));
        }
        if (price_bigger_than.isPresent()) {
            books = books.filter(book -> book.getYear() >= price_bigger_than.get());
        }
        if (price_less_than.isPresent()) {
            books = books.filter((book -> book.getPrice() <= price_less_than.get()));
        }
        if (year_bigger_than.isPresent()) {
            books = books.filter(book -> book.getYear() >= year_bigger_than.get());
        }
        if (year_less_than.isPresent()) {
            books = books.filter(book -> book.getYear() <= year_less_than.get());
        }
        if (genres.isPresent()) {
            String genres_raw = genres.get();
            if (!genres_raw.equals(genres_raw.toUpperCase())) {
                Instant end = Instant.now();
                requestLoggerDebug(Duration.between(begin, end));
                return new ResponseEntity<>(new ResponseObject<>(null, ""), HttpStatus.BAD_REQUEST);
            }
            else {
                List<String> genresInputList = new ArrayList<>(Arrays.asList(genres_raw.split(",")));
                books = books.filter(book -> Collections.disjoint(book.getGenres(), genresInputList));
            }
        }
        books = books.sorted(Comparator.comparing(Book::getTitle));
        Object[] outBooks = books.toArray();
        booksLogger.info("Total Books found for requested filters is {}", outBooks.length);
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        return new ResponseEntity<>(new ResponseObject<>(outBooks, ""), HttpStatus.OK);
    }

    @GetMapping("/book")
    public ResponseEntity<?> getBook(@RequestParam("id") int id) {
        requestCount++;
        requestLoggerInfo("/book", "GET");
        Instant begin = Instant.now();
        Book getBook = idToBookDB.get(id);
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        if (getBook == null) {
            booksLogger.error("Error: no such Book with id {}", id);
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else {
            booksLogger.debug("Fetching book id {} details", id);
            return new ResponseEntity<>(new ResponseObject<>(getBook, ""), HttpStatus.OK);
        }
    }

    @PutMapping("/book")
    public ResponseEntity<?> updatePrice(@RequestParam("id") int id, @RequestParam("price") int price) {
        requestCount++;
        requestLoggerInfo("/book", "PUT");
        Instant begin = Instant.now();
        Book updateBook = idToBookDB.get(id);
        if (updateBook == null) {
            booksLogger.error("Error: no such Book with id {}", id);
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else if (price <= 0) {
            booksLogger.error("Error: price update for book {} must be a positive integer", id);
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: price update for book " + id + " must be a positive integer"), HttpStatus.CONFLICT);
        }
        else {
            booksLogger.info("Update Book id [{}] price to {}", updateBook.getId(), price);
            booksLogger.debug("Book [{}] price change: {} --> {}", updateBook.getTitle(), updateBook.getPrice(), price);
            final int oldPrice = updateBook.getPrice();
            updateBook.setPrice(price);
            Instant end = Instant.now();
            requestLoggerDebug(Duration.between(begin, end));
            return new ResponseEntity<>(new ResponseObject<>(oldPrice, ""), HttpStatus.OK);
        }
    }

    @DeleteMapping("/book")
    public ResponseEntity<?> deleteBook(@RequestParam("id") int id) {
        requestCount++;
        requestLoggerInfo("/book", "DELETE");
        Instant begin = Instant.now();
        Book removedBook = idToBookDB.remove(id);
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        if (removedBook == null) {
            booksLogger.error("Error: no such Book with id {}", id);
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else {
            booksLogger.info("Removing book [{}]", removedBook.getTitle());
            booksLogger.debug("After removing book [{}] id: [{}] there are {} books in the system", removedBook.getTitle(), removedBook.getId(), nextID - 1);
            nextID--;
            return new ResponseEntity<>(new ResponseObject<>(idToBookDB.size(), ""), HttpStatus.OK);
        }
    }

    @GetMapping("/logs/level")
    public ResponseEntity<?> getLogLevel(@RequestParam("logger-name") String loggerName) {
        requestCount++;
        requestLoggerInfo("/logs/level", "GET");
        Instant begin = Instant.now();
        ResponseEntity<String> response = switch (loggerName) {
            case "request-logger" ->
                    new ResponseEntity<>(requestLogger.getLevel().toString().toUpperCase(), HttpStatus.OK);
            case "books-logger" -> new ResponseEntity<>(booksLogger.getLevel().toString().toUpperCase(), HttpStatus.OK);
            default -> new ResponseEntity<>("No logger found", HttpStatus.NOT_FOUND);
        };
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        return response;
    }

    @PutMapping("/logs/level")
    public ResponseEntity<?> setLogLevel(@RequestParam("logger-name") String loggerName, @RequestParam("logger-level") String loggerLevel) {
        requestCount++;
        requestLoggerInfo("/logs/level", "PUT");
        Instant begin = Instant.now();
        ResponseEntity<String> response;
        List<String> availableLevels = Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
        if (availableLevels.stream().anyMatch(level -> level.equals(loggerLevel))) {
            switch (loggerName) {
                case "request-logger":
                    requestLogger.setLevel(Level.valueOf(loggerLevel));
                    response = new ResponseEntity<>(requestLogger.getLevel().toString().toUpperCase(), HttpStatus.OK);
                    break;
                case "books-logger":
                    booksLogger.setLevel(Level.valueOf(loggerLevel));
                    response = new ResponseEntity<>(booksLogger.getLevel().toString().toUpperCase(), HttpStatus.OK);
                    break;
                default:
                    response = new ResponseEntity<>("No logger found", HttpStatus.NOT_FOUND);
                    break;
            }
        }
        else {
            response = new ResponseEntity<>("No level found", HttpStatus.NOT_FOUND);
        }
        Instant end = Instant.now();
        requestLoggerDebug(Duration.between(begin, end));
        return response;
    }

    private void requestLoggerInfo(final String resource_name, final String http_verb) {
        MDC.put("id", requestCount.toString());
        requestLogger.info("Incoming request | #{} | resource: {} | HTTP Verb {}", requestCount, resource_name, http_verb);
    }

    private void requestLoggerDebug(final Duration duration) {
        MDC.put("id", requestCount.toString());
        requestLogger.debug("request #{} duration: {}ms", requestCount, duration.toMillis());
    }
}
