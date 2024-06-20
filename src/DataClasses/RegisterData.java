package DataClasses;

import java.io.Serializable;

public class RegisterData implements Serializable {
    private String userName;
    public RegisterData(String userName){
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
