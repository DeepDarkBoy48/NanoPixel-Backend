package robin.discordbot.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Unified response result
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;// 0-success 1-fail
    private String message;// prompt information
    private T data;// response data

    // Quickly return the operation success response result (with response data)
    public static <E> Result<E> success(E data) {
        return new Result<>(0, "operation success", data);
    }

    // Quickly return the operation success response result
    public static Result success() {
        return new Result(0, "operation success", null);
    }

    public static Result success(String message) {
        return new Result(0, message, null);
    }

    public static <E> Result<E> success(String message, E data) {
        return new Result<>(0, message, data);
    }

    // Quickly return the operation failure response result (with response data)
    public static Result error(String message) {
        return new Result(1, message, null);
    }
}