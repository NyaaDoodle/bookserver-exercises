package com.kaplat.bookserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

@RestController
public class BookManager {
    private final Map<Integer, Book> idToBookDB = new HashMap<>();
    private int nextID = 0;
    private int requestCount = 0;

    @GetMapping("/books/health")
    public String health() {
        requestCount++;
        return "OK";
    }

    @PostMapping("/book")
    public ResponseEntity<?> createBook(@RequestBody final Book book) {
        requestCount++;
        if (idToBookDB.values().stream().anyMatch(b -> b.getTitle().equalsIgnoreCase(book.getTitle()))) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Book with the title [" + book.getTitle() + "] already exists in the system"), HttpStatus.CONFLICT);
        }
        else if (book.getYear() < 1940 || book.getYear() > 2100) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Can’t create new Book that its year [" + book.getYear() +"] is not in the accepted range [1940 -> 2100]"), HttpStatus.CONFLICT);
        }
        else if (book.getPrice() <= 0) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: Can’t create new Book with negative price"), HttpStatus.CONFLICT);
        }
        else {
            nextID++;
            book.setId(nextID);
            idToBookDB.put(nextID, book);
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
                return new ResponseEntity<>(new ResponseObject<>(null, ""), HttpStatus.BAD_REQUEST);
            }
            else {
                List<String> genresInputList = new ArrayList<>(Arrays.asList(genres_raw.split(",")));
                books = books.filter(book -> Collections.disjoint(book.getGenres(), genresInputList));
            }
        }
        return new ResponseEntity<>(new ResponseObject<>(books.count(), ""), HttpStatus.OK);
    }

    @GetMapping("/books")
    public ResponseEntity<?> getBooksData(@RequestParam("author") Optional<String> author,
                                          @RequestParam("price-bigger-than") Optional<Integer> price_bigger_than,
                                          @RequestParam("price-less-than") Optional<Integer> price_less_than,
                                          @RequestParam("year-bigger-than") Optional<Integer> year_bigger_than,
                                          @RequestParam("year-less-than") Optional<Integer> year_less_than,
                                          @RequestParam("genres") Optional<String> genres) {
        requestCount++;
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
                return new ResponseEntity<>(new ResponseObject<>(null, ""), HttpStatus.BAD_REQUEST);
            }
            else {
                List<String> genresInputList = new ArrayList<>(Arrays.asList(genres_raw.split(",")));
                books = books.filter(book -> Collections.disjoint(book.getGenres(), genresInputList));
            }
        }
        books = books.sorted(Comparator.comparing(Book::getTitle));
        return new ResponseEntity<>(new ResponseObject<>(books.toArray(), ""), HttpStatus.OK);
    }

    @GetMapping("/book")
    public ResponseEntity<?> getBook(@RequestParam("id") int id) {
        requestCount++;
        Book getBook = idToBookDB.get(id);
        if (getBook == null) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(new ResponseObject<>(getBook, ""), HttpStatus.OK);
        }
    }

    @PutMapping("/book")
    public ResponseEntity<?> updatePrice(@RequestParam("id") int id, @RequestParam("price") int price) {
        requestCount++;
        Book updateBook = idToBookDB.get(id);
        if (updateBook == null) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else if (price <= 0) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: price update for book " + id + " must be a positive integer"), HttpStatus.CONFLICT);
        }
        else {
            final int oldPrice = updateBook.getPrice();
            updateBook.setPrice(price);
            return new ResponseEntity<>(new ResponseObject<>(oldPrice, ""), HttpStatus.OK);
        }
    }

    @DeleteMapping("/book")
    public ResponseEntity<?> deleteBook(@RequestParam("id") int id) {
        requestCount++;
        Book removedBook = idToBookDB.remove(id);
        if (removedBook == null) {
            return new ResponseEntity<>(new ResponseObject<>(null, "Error: no such Book with id " + id), HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(new ResponseObject<>(idToBookDB.size(), ""), HttpStatus.OK);
        }
    }
}
