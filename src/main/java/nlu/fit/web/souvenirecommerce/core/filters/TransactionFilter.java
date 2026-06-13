package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.*;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TransactionFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            chain.doFilter(request, response);

            if (transaction != null && transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Transaction filter rolled back due to exception", e);
            throw e;
        }
    }
}
