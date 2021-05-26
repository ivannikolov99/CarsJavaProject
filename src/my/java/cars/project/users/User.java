package my.java.cars.project.users;

import java.util.Objects;

public class User {
   private final String email;
   private final String password;
   private double balance;

   public User(String email,String password){
       this.email=email;
       this.password=password;
       this.balance=0;
   }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance += balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
