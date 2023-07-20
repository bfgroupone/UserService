package groupone.userservice.dao;

import groupone.userservice.entity.User;
import groupone.userservice.exception.InvalidTypeAuthorization;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserDao extends AbstractHibernateDao<User> {

    public UserDao() {
        setClazz(User.class);
    }

//    public void setUserActive(User user, boolean isActive) {
//        user.setActive(isActive);
//    }

    public Optional<User> loadUserByEmail(String email){
        return this.getAll().stream().filter(user -> user.getEmail().equals(email)).findAny();
    }

    public User getUserById(int id) {
        return this.findById(id);
    }

    public List<User> getAllUsers() {
        return this.getAll();
    }

    public void addUser(User user) throws DataIntegrityViolationException {
        this.add(user);
    }

    public void setType(User user, int type) throws InvalidTypeAuthorization {
        if(type == 0) throw new InvalidTypeAuthorization("Cannot assign SUPER ADMIN type to users.");
        else user.setType(type);
    }

    public void deleteUser(User user) {
        this.delete(user);
    }

}
