package ru.job4j.tracker.store;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.tracker.model.Item;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SqlTrackerTest {

    private static Connection connection;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = new FileInputStream("db/liquibase_test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        System.out.println(tracker.findById(item.getId()));
        assertThat(tracker.findById(item.getId())).isEqualTo(item);
    }

    @Test
    public void whenUpdatingNameTheNameShouldBecomeNewOne() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item");
        Item item2 = new Item("item_new");
        tracker.add(item1);
        tracker.replace(item1.getId(), item2);
        assertThat(tracker.findById(item1.getId()).getName()).isEqualTo(item2.getName());
    }

    @Test
    public void whenDeletingExistingItemItShouldReturnTrueAndWhenDeletingNonExistentThanFalse() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        tracker.delete(item.getId());
        assertNull(tracker.findById(item.getId()));
    }

    @Test
    public void whenAdding3ItemsThereShouldBe3EntriesInDbAfterSearch() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        tracker.add(item);
        tracker.add(item);
        List<Item> items = tracker.findAll();
        assertThat(items.size()).isEqualTo(3);
    }

    @Test
    public void whenAdding3ItemsContainingSearchWordAfterSearchingInDbThereShouldBe3Entries() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item");
        Item item2 = new Item("item1");
        Item item3 = new Item("item2");
        tracker.add(item1);
        tracker.add(item2);
        tracker.add(item3);
        List<Item> items = tracker.findByName(item1.getName());
        assertThat(items.size()).isEqualTo(3);
    }

    @Test
    public void whenSearchingForRowExistingInDbByIdSuchARowShouldBeFound() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item");
        tracker.add(item1);
        Item item = tracker.findById(item1.getId());
        assertThat(item.getId()).isEqualTo(item1.getId());
    }
}