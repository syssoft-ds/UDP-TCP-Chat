package Utils;

public class UserDoesNotExistsException extends Exception{
    public UserDoesNotExistsException(String message) {
        super(message);
    }
}
