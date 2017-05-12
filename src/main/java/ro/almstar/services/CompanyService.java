package ro.almstar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import ro.almstar.domain.Company;
import ro.almstar.repositories.CompanyRepository;

import java.util.Collection;

/**
 * Created by itsix on 3/7/2017.
 */
@Service
public class CompanyService {
    @Autowired
    CompanyRepository companyRepository;

    public Collection<Company> findByNameContaining(@Param("companyName") String name) {
        return companyRepository.findByNameContaining(name);
    }
}
