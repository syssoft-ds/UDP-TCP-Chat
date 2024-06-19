package DataClasses;

import java.io.Serializable;
import java.util.List;

public class UserSyncData implements Serializable {
    private List<String> userNameList;

    public UserSyncData(List<String> userNameList){
        this.userNameList = userNameList;
    }

    public List<String> getUserNameList() {
        return userNameList;
    }
}
