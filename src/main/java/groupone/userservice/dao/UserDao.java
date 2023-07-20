package groupone.userservice.dao;

import groupone.userservice.entity.User;
import groupone.userservice.entity.UserType;
import groupone.userservice.exception.InvalidTypeAuthorization;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public int addUser(User user) throws DataIntegrityViolationException {
        return this.add(user);
    }

    public void setType(User user, int type) throws InvalidTypeAuthorization {
        if(type == UserType.SUPER_ADMIN.ordinal()) throw new InvalidTypeAuthorization("Cannot assign SUPER ADMIN type to users.");
        else user.setType(type);
    }

    public void deleteUser(User user) {
        this.delete(user);
    }
    public User getUserById(Integer user_id){
        return this.findById(user_id);
    }

    public void setValidationToken(User user, String token) {
        user.setValidationToken(token);
    }
}
