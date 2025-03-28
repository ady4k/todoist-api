package com.ady4k.todoistapi.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;

    public UserDto(String username) {
        this.username = username;
    }

    public UserDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public UserDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
