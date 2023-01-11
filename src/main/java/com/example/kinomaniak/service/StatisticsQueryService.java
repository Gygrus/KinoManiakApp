package com.example.kinomaniak.service;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Service
public class StatisticsQueryService {

    @PersistenceContext
    EntityManager em;

    public List<Object[]> getBestCashiers(LocalDate fromDate, LocalDate toDate) {
        Query query =em.createNativeQuery("SELECT e.mail, e.name, e.sur_name, count(*) FROM employee e " +
                "INNER JOIN ticket t ON t.ID_EMPLOYEE = e.id " +
                "INNER JOIN film_show fs ON fs.id = t.ID_FILMSHOW " +
                "WHERE fs.date <= :to_date AND fs.date >= :from_date " +
                "GROUP BY e.mail, e.name, e.sur_name " +
                "ORDER BY count(*) desc"
                );
        query.setParameter("to_date", toDate);
        query.setParameter("from_date", fromDate);
        List<Object[]> records = query.getResultList();
        return records;
    }

    public List<Object[]> getMostWatchedMovies(LocalDate fromDate, LocalDate toDate) {
        Query query =em.createNativeQuery("SELECT m.title, m.director, m.duration, count(*) FROM movie m " +
                "INNER JOIN film_show fs ON fs.movie_id = m.id " +
                "INNER JOIN ticket t ON t.ID_FILMSHOW = fs.id " +
                "WHERE fs.date <= :to_date AND fs.date >= :from_date " +
                "GROUP BY m.title, m.director, m.duration " +
                "ORDER BY count(*) desc"
        );
        query.setParameter("to_date", toDate);
        query.setParameter("from_date", fromDate);
        List<Object[]> records = query.getResultList();
        return records;
    }

    public List<Object[]> getMostProfitableMovies(LocalDate fromDate, LocalDate toDate) {
        Query query =em.createNativeQuery("SELECT m.title, m.director, m.duration, sum(fs.ticket_price) FROM movie m " +
                "INNER JOIN film_show fs ON fs.movie_id = m.id " +
                "INNER JOIN ticket t ON t.ID_FILMSHOW = fs.id " +
                "WHERE fs.date <= :to_date AND fs.date >= :from_date " +
                "GROUP BY m.title, m.director, m.duration " +
                "ORDER BY sum(fs.ticket_price) desc"
        );
        query.setParameter("to_date", toDate);
        query.setParameter("from_date", fromDate);
        List<Object[]> records = query.getResultList();
        return records;
    }

    public List<Object[]> getMostPopularHalls(LocalDate fromDate, LocalDate toDate) {
        Query query =em.createNativeQuery("SELECT h.hall_no, h.capacity, count(*) FROM hall h " +
                "INNER JOIN film_show fs ON fs.hall_id = h.id " +
                "INNER JOIN ticket t ON t.ID_FILMSHOW = fs.id " +
                "WHERE fs.date <= :to_date AND fs.date >= :from_date " +
                "GROUP BY h.hall_no, h.capacity " +
                "ORDER BY count(*) desc"
        );
        query.setParameter("to_date", toDate);
        query.setParameter("from_date", fromDate);
        List<Object[]> records = query.getResultList();
        return records;
    }

    public List<Object[]> getEarningsFromTickets(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> records = null;
        Query query;
        LocalDate curDate = fromDate;
        while (curDate.compareTo(toDate)<=0){

            query = em.createNativeQuery("SELECT :curDate, coalesce(sum(coalesce(fs.ticket_price, 0)), 0) FROM film_show fs " +
                    "LEFT OUTER JOIN ticket t ON t.ID_FILMSHOW = fs.id " +
                    "WHERE abs(DATE_PART('day', :curDate - fs.date)) = 0 " +
                            "AND DATE_PART('day', fs.date) = :curDateDay " +
                    "ORDER BY sum(fs.ticket_price) desc"
            );
            query.setParameter("curDate", Timestamp.valueOf(curDate.atStartOfDay()));
            query.setParameter("curDateDay", curDate.getDayOfMonth());

            if (records == null) records = query.getResultList();
            else records.addAll(query.getResultList());

            curDate = curDate.plusDays(1);
        }
        return records;
    }
}