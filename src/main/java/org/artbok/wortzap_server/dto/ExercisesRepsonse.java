package org.artbok.wortzap_server.dto;
import lombok.Getter;


@Getter
public class ExercisesRepsonse {
    private final String status;
    private final String data;

    public ExercisesRepsonse(String status, String data) {
        this.status = status;
        this.data = data;
    }

}