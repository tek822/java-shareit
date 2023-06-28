package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserClient userClient;
    private final Gson gson = new Gson();

    private final UserDto userDto1 = new UserDto(1L, "user1name", "user1@mail.com");
    private final UserDto userDto2 = new UserDto(2L, "user2name", "user2@mail.com");

    @Test
    void userCreateTest() throws Exception {
        when(userClient.addUser(any(UserDto.class))).thenReturn(ResponseEntity.ok(gson.toJson(userDto1)));

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));

        verify(userClient).addUser(userDto1);
    }

    @Test
    void createInvalidNameUserTest() throws Exception {
        UserDto emptyNameUser = new UserDto(-1L, "", "user@mail.com");

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(emptyNameUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).addUser(emptyNameUser);
    }

    @Test
    void createInvalidEmailUserTest() throws Exception {
        UserDto invalidEmailUser = new UserDto(-1L, "user", "user(at)mail.com");

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(invalidEmailUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).addUser(invalidEmailUser);
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userClient.getAllUsers()).thenReturn(ResponseEntity.ok(gson.toJson(List.of(userDto1, userDto2))));

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userClient, Mockito.times(1)).getAllUsers();
    }

    @Test
    void getUserTest() throws Exception {
        when(userClient.getUser(anyLong())).thenReturn(ResponseEntity.ok(gson.toJson(userDto1)));

        mockMvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));

        verify(userClient, Mockito.times(1)).getUser(1L);
    }

    @Test
    void getUserFailureTest() throws Exception {
        when(userClient.getUser(anyLong())).thenReturn(
                new ResponseEntity<Object>("User not found", HttpStatus.NOT_FOUND)
        );

        mockMvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userClient, Mockito.times(1)).getUser(anyLong());
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient, Mockito.times(1)).removeUser(1L);
    }

    @Test
    void updateUserNameTest() throws Exception {
        UserDto update = new UserDto(userDto1.getId(), "updatedName", null);
        UserDto updatedDto = new UserDto(userDto1.getId(), update.getName(), userDto1.getEmail());

        when(userClient.updateUser(anyLong(), any(UserDto.class))).thenReturn(ResponseEntity.ok(gson.toJson(updatedDto)));

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(update))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(update.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));

        verify(userClient, Mockito.times(1)).updateUser(anyLong(), any());
    }

    @Test
    void updateUserInvalidEmailTest() throws Exception {
        UserDto update = new UserDto(userDto1.getId(), null, "mail(at)com");

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(update))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).updateUser(anyLong(), any());
    }
}
