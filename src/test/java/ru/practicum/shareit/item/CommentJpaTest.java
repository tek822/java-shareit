package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Transactional
class CommentJpaTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;

    private User user = new User(0L, "User1", "user1@mail.com");
    private Item item1 = new Item(0L, "Item1", "Item1 description", true, user, null);
    private Item item2 = new Item(0L, "Item2", "Item2 description", true, user, null);
    private Comment comment1 = new Comment(0L, "Comment1", item1, user, LocalDateTime.now());
    private Comment comment2 = new Comment(0L, "Comment2", item1, user, LocalDateTime.now());
    private Comment comment3 = new Comment(0L, "Comment3", item2, user, LocalDateTime.now());

    @BeforeEach
    void initDb() {
        user = userRepository.save(user);

        item1.setOwner(user);
        item1 = itemRepository.save(item1);

        item1.setOwner(user);
        item2 = itemRepository.save(item2);

        comment1.setAuthor(user);
        comment1.setItem(item1);
        comment1 = commentRepository.save(comment1);

        comment2.setAuthor(user);
        comment2.setItem(item1);
        comment2 = commentRepository.save(comment2);

        comment3.setAuthor(user);
        comment3.setItem(item2);
        comment3 = commentRepository.save(comment3);
    }

    @AfterEach
    void clearDb() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByItemIdTest() {
        List<Comment> comments = commentRepository.findAllByItemId(item1.getId());

        assertNotNull(comments);
        assertTrue(comments.containsAll(List.of(comment1, comment2)));
    }

    @Test
    void findAllByItemIdInTest() {
        List<Comment> comments = commentRepository.findAllByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertNotNull(comments);
        assertTrue(comments.containsAll(List.of(comment1, comment2, comment3)));
    }
}
