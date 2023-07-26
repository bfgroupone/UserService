package groupone.userservice.dao;

import groupone.userservice.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao extends AbstractHibernateDao<User> {

    public UserDao() {
        setClazz(User.class);
    }
    void setSessionFactory(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
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

    public void deleteUser(User user) {
        this.delete(user);
    }
    public User getUserById(Integer user_id){
        return this.findById(user_id);
    }

    public void setValidationToken(User user, String token) {
        user.setValidationToken(token);
    }

    public List<User> getUserGroupByIdList(List<Integer> userIdList) {
        Session session = this.getCurrentSession();
        String hql = "FROM User Where userId IN (:userIdList)";
        Query<User> query = session.createQuery(hql, User.class);
        query.setParameterList("userIdList", userIdList);
        return query.list();
    }
}
