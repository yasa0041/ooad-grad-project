package hibernate;

import net.sf.ehcache.CacheManager;
import org.hibernate.Session;
import hibernate.model.EmployeeEntity;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.SortedMap;

public class HibernateTest {

    //https://stackoverflow.com/questions/43037814/how-to-get-all-data-in-the-table-with-hibernate
    private static <T> List<T> searchEmployee(Session session, Class<T> type, Object startEmployeeId) {
        session.beginTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> model = cq.from(type);
        cq.where(cb.equal(model.get("employeeId"), startEmployeeId));

        List<T> data = session.createQuery(cq).setHint("org.hibernate.cacheable", true).getResultList();

        session.getTransaction().commit();

        return data;
    }

    private static <T> List<T> getAllData(Session session, Class<T> type) {
        session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);
        List<T> data = session.createQuery(criteria).getResultList();

        session.getTransaction().commit();

        return data;
    }

    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        //Add new Employee object
        insertNtimes(10, session);

        queryNTime(10, session);

    }

    private static void insertNtimes(int N, Session session) {
        for (int i = 0; i < N; i++ ) {
            EmployeeEntity emp = new EmployeeEntity();
            emp.setEmail("demo-user@mail.com" + i);
            emp.setFirstName("demo" + i);
            emp.setLastName("user" + i);

            System.out.println(i + "th INSERT into DB");
            timeInsert(session, emp);
        }

    }

    private static void queryNTime(int N, Session session) {
        for (int i = 0; i < N; i++){
            System.out.println(i+"th SELECT query");
            timeSelect(session, EmployeeEntity.class).forEach(x -> System.out.println(x));
        }

    }

    private static List<EmployeeEntity> timeSelect(Session session, Class<EmployeeEntity> employeeEntityClass) {
        long startTime = Instant.now().toEpochMilli();
//        var employeeList = searchEmployee(session, employeeEntityClass, 1);
        var employeeList = getAllData(session, employeeEntityClass);
        long endTime = Instant.now().toEpochMilli();
        System.out.println("** Finished SELECT query in "+(endTime-startTime)+"ms **");
        return employeeList;
    }

    private static void timeInsert(Session session, EmployeeEntity emp) {
        session.beginTransaction();

        long startTime = Instant.now().toEpochMilli();
        session.save(emp);
        long endTime = Instant.now().toEpochMilli();
        System.out.println("** Finished INSERT query in "+(endTime-startTime)+"ms **");

        session.getTransaction().commit();
    }

}