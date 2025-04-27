package org.example;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) {
        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        Connection conn = dataSourceManager.getConnection();
        if (conn != null) {
            dataSourceManager.executeSqlScript("music-create.sql");
            System.out.println("--------------------------------Example 1----------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name FROM music");
            System.out.println("--------------------------------Example 2----------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name FROM music WHERE LOWER(name) NOT LIKE '%m%' and LOWER(name) NOT LIKE '%t%'");
            System.out.println("--------------------------------Example 3----------------------------------------------");
            dataSourceManager.executeInsert("INSERT OR IGNORE INTO music (name) VALUES ('A glass of Vodka')");
            dataSourceManager.executeInsert("INSERT OR IGNORE INTO music (name) VALUES ('Enjoy life')");
            dataSourceManager.executeInsert("INSERT OR IGNORE INTO music (name) VALUES ('Time Is Money')");
            dataSourceManager.selectQueryResult("SELECT id, name FROM music");
            System.out.println("----------------------------------------------------------------------------------------");
            Gson gson = new Gson();
            List<BookReader> bookreaders = null;
            try (Reader reader = Files.newBufferedReader(Paths.get("books.json"))) {
                TypeToken<List<BookReader>> token = new TypeToken<List<BookReader>>() {};
                bookreaders = gson.fromJson(reader, token.getType());
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            }
            List<Book> books = bookreaders.stream().flatMap(bookreader -> bookreader.getFavoriteBooks().stream()).distinct().collect(Collectors.toList());
            System.out.println("--------------------------------Example 4----------------------------------------------");
            dataSourceManager.executeSqlScript("create-lib.sql");
            for (BookReader bookReader : bookreaders) {
                insertReader(dataSourceManager, bookReader);
                for (Book book : bookReader.getFavoriteBooks()) {
                    insertBook(dataSourceManager, book);
                    inserReaderBook(dataSourceManager, bookReader, book, bookreaders, books);
                }
            }
            System.out.println("-------------------------------------------------Readers------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, surname, subscribed, phone FROM readers");
            System.out.println("-------------------------------------------------Books------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, isbn, publishing_year, author, publisher FROM books");
            System.out.println("-------------------------------------------------Readers_Books------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT r.name || ', ' || r.surname AS FIO, b.name AS BOOK_NAME, b.author AS BOOK_AUTHOR " +
                    "FROM readers r LEFT JOIN reader_book rb ON r.id = rb.reader_id LEFT JOIN books b ON rb.book_id = b.id");
            System.out.println("--------------------------------Example 5----------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, isbn, publishing_year, author, publisher FROM books ORDER BY publishing_year");
            System.out.println("--------------------------------Example 6----------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, isbn, publishing_year, author, publisher FROM books WHERE publishing_year > 2000 ORDER BY publishing_year");
            System.out.println("--------------------------------Example 7----------------------------------------------");
            List<Book> myBooks = new ArrayList<>();
            myBooks.add(new Book("The Count of Monte Cristo", "Alexander Duma", 2011, "235454656", "Litress"));
            myBooks.add(new Book("Captain Grant children", "Jules Verne", 2003, "6898923954", "Labirint"));
            myBooks.add(new Book("The Black Tulip", "Alexander Duma", 2011, "235457126", "Litress"));
            BookReader me = new BookReader("Vovа", "Medvedev", "900-053-9325", true, myBooks, new ArrayList<>());
            if (!bookreaders.contains(me)) {
                bookreaders.add(me);
                insertReader(dataSourceManager, me);
                for (Book book: me.getFavoriteBooks()) {
                    if (!books.contains(book)) {
                        books.add(book);
                        insertBook(dataSourceManager, book);
                        inserReaderBook(dataSourceManager, me, book, bookreaders, books);
                    }
                }
            }
            System.out.println("-------------------------------------------------Readers------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, surname, subscribed, phone FROM readers");
            System.out.println("-------------------------------------------------Books------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT id, name, isbn, publishing_year, author, publisher FROM books");
            System.out.println("-------------------------------------------------Readers_Books------------------------------------------------------------------");
            dataSourceManager.selectQueryResult("SELECT r.name || ', ' || r.surname AS FIO, b.name AS BOOK_NAME, b.author AS BOOK_AUTHOR " +
                    "FROM readers r LEFT JOIN reader_book rb ON r.id = rb.reader_id LEFT JOIN books b ON rb.book_id = b.id");
            System.out.println("--------------------------------Example 8----------------------------------------------");
            dataSourceManager.deleteTable("music");
            dataSourceManager.deleteTable("books");
            dataSourceManager.deleteTable("readers");
            dataSourceManager.deleteTable("reader_book");
            dataSourceManager.closeConnection();
        }
    }
    static void insertReader(DataSourceManager dataSourceManager, BookReader bookreader) {
        dataSourceManager.executeInsert(String.format(
                """
                INSERT INTO readers(name, surname, subscribed, phone)
                SELECT '%s', '%s', %b, '%s'
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM readers
                    WHERE name = '%s' AND surname = '%s' AND subscribed = %b AND phone = '%s'
                );
                """,
                bookreader.getName(), bookreader.getSurname(), bookreader.isSubscribed(), bookreader.getPhone(),
                bookreader.getName(), bookreader.getSurname(), bookreader.isSubscribed(), bookreader.getPhone()
            )
        );
    }
    static void insertBook(DataSourceManager dataSourceManager, Book book) {
        dataSourceManager.executeInsert(String.format(
                """
                INSERT INTO books(name, isbn, publishing_year, author, publisher)
                SELECT '%s', '%s', %d, '%s', '%s'
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM books
                    WHERE name = '%s' AND isbn = '%s' AND publishing_year = %d AND author = '%s' AND publisher = '%s'
                );
                """,
                book.getName(), book.getIsbn(), book.getPublishingYear(), book.getAuthor(), book.getPublisher(), book.getName(), book.getIsbn(), book.getPublishingYear(), book.getAuthor(), book.getPublisher()
            )
        );
    }
    static void inserReaderBook(DataSourceManager dataSourceManager, BookReader reader, Book book, List<BookReader> bookreaders, List<Book> books) {
        dataSourceManager.executeInsert(String.format(
                """
                INSERT INTO reader_book(reader_id, book_id)
                SELECT '%d', '%d'
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM reader_book
                    WHERE reader_id = '%d' AND book_id = '%d'
                );
                """,
                bookreaders.indexOf(reader)+1, books.indexOf(book)+1, bookreaders.indexOf(reader)+1, books.indexOf(book)+1
            )
        );
    }
}
