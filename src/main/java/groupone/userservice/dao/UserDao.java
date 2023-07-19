package groupone.userservice.dao;

import groupone.userservice.entity.User;
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
//        try (Session session = this.getCurrentSession()) {
//            Transaction tx;
//            tx = session.beginTransaction();
//            session.save(user);
//            tx.commit();
//        } catch (Exception e) {
//            throw new DataIntegrityViolationException(e.getMessage() + " " + e.getCause().getLocalizedMessage());
//        }
        this.add(user);
    }

    public void deleteUser(User user) {
        this.delete(user);
    }
    public User getUserById(Integer user_id){
        return this.findById(user_id);
    }
}
