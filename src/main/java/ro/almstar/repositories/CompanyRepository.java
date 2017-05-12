package ro.almstar.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import ro.almstar.domain.Company;

import java.util.Collection;

/**
 * Created by itsix on 3/7/2017.
 */
public interface CompanyRepository extends GraphRepository<Company> {

    Collection<Company> findByNameContaining(@Param("companyName") String name);

}
