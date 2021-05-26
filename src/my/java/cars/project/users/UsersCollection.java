package my.java.cars.project.users;

import java.util.*;

public class UsersCollection {
    private static UsersCollection instance=null;
    private static Set<User> registeredUsers;
    private static Map<Integer,User> loggedUsers;

    public static UsersCollection getInstance() {
        if(instance==null) {
            instance=new UsersCollection();
            registeredUsers=new HashSet<>();
            loggedUsers=new HashMap<>();
        }

        return instance;
    }

    public boolean addRegisteredUser(User user) {
        return registeredUsers.add(user);
    }

    public User getRegisteredUser(String email) {
        for(User u:registeredUsers) {
            if(u.getEmail().equals(email)){
                return u;
            }
        }
        return null;
    }

    public void addLoggedUser(int scHash,User user) {
        if(!loggedUsers.containsValue(user)){
            loggedUsers.put(scHash,user);
        }
    }

    public boolean removeLoggedUser(int scHash){
        if(loggedUsers.containsKey(scHash)){
            loggedUsers.remove(scHash);
            return true;
        }
        return false;
    }

    public boolean checkIfAlreadyLogged(User user){
        for(User users : loggedUsers.values()){
            if(user.getEmail().equals(users.getEmail())){
                return false;
            }
        }
        return true;
    }

    public boolean checkIfLogged(int scHash){
        return loggedUsers.containsKey(scHash);
    }

    public User getLoggedUser(int scHash){
        return loggedUsers.get(scHash);
    }

    public Set<User> getAllRegisteredUsers(){
        return Collections.unmodifiableSet(registeredUsers);
    }

}
