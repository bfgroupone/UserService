package groupone.userservice.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

public abstract class AbstractHibernateDao<T> {

    @Autowired
    @Resource(name="sessionFactory")
    protected SessionFactory sessionFactory;

    protected Class<T> clazz;

    protected final void setClazz(final Class<T> clazzToSet) {
        clazz = clazzToSet;
    }

    public List<T> getAll() {
        Session session = getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);
        criteria.from(clazz);
        return session.createQuery(criteria).getResultList();
    }

    public T findById(int id) {
        return getCurrentSession().get(clazz, id);
    }

    public int add(T item) {
        return (int) getCurrentSession().save(item);
    }

    public void delete(T item) {
        getCurrentSession().delete(item);
    }

    protected Session getCurrentSession() {
        Session session;
        try
        {
            session =  sessionFactory.getCurrentSession();
        }
        catch (HibernateException e)
        {
            session =  sessionFactory.openSession();
        }
        return session;
    }
}
