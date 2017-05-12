package ro.almstar.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import ro.almstar.domain.Complaint;
import ro.almstar.domain.Issue;
import ro.almstar.domain.Product;
import ro.almstar.domain.Response;

import java.util.List;
import java.util.Map;

/**
 * Created by itsix on 3/7/2017.
 */
public interface ComplaintRepository extends GraphRepository<Complaint> {

    @Query("MATCH (complaint:Complaint) \n" +
            "MATCH (complaint)<-[:TO]-(response:Response)\n" +
            "MATCH (complaint)-[:WITH]->(issue:Issue)\n" +
            "MATCH (complaint)-[:ABOUT]->(product:Product)\n" +
            "\n" +
            "WHERE ID(complaint)=1590361 \n" +
            "\n" +
            "OPTIONAL MATCH (complaint)-[:WITH]->(subIssue:SubIssue)-[:IN_CATEGORY]->(issue)\n" +
            "OPTIONAL MATCH (complaint)-[:ABOUT]->(subProduct:SubProduct)-[:IN_CATEGORY]->(product)\n" +
            "\n" +
            "RETURN complaint, issue, subIssue, product, subProduct, response")
    FullComplaint findFullComplaintById(@Param("id") Long id);

    @QueryResult
    public class FullComplaint {
        Complaint complaint;
        Issue issue;
        //            SubIssue subIssue;
        Product product;
        //            SubProduct subProduct;
        Response response;
    }


    @Query("MATCH (complaint:Complaint) WHERE ID(complaint)={id} return complaint")
    Complaint findByIdUsingQuery(@Param("id") Long id);


    /**
     * Derived finder - will not go in depth
     *
     * @param id
     * @return
     * @see http://stackoverflow.com/questions/33525766/how-to-get-the-direct-relationship-entities-and-directly-related-nodes-in-custom
     */
    Complaint findById(Long id);

    /**
     * Depth is 1 by default
     *
     * @param id
     * @return
     */
    Complaint findOne(Long id);

    @Query("MATCH (complaint:Complaint)-[against:AGAINST]->(company:Company) WHERE company.name={companyName} return complaint")
    List<Complaint> getComplaintsForCompany(@Param("companyName") String companyName);

    @Query("MATCH (complaint:Complaint)-[against:AGAINST]->(company:Company) WHERE ID(company)={companyID} return complaint")
    List<Complaint> getComplaintsForCompanyID(@Param("companyID") Long companyID);

    @Query("MATCH (company:Company)<-[:AGAINST]-(complaint:Complaint) RETURN company.name as company, collect(complaint.year+'-'+complaint.month+'-'+complaint.dat) as complaints LIMIT {limit}")
//TODO rename "dat"
    List<Map<String, Object>> graph(@Param("limit") int limit);

//    // Co-Actors
//    Set<Person> findByActorsMoviesActorName(String name);
//
//    @Query("MATCH (movie:Movie)-[:HAS_GENRE]->(genre)<-[:HAS_GENRE]-(similar)
//            WHERE id(movie) = {0} RETURN similar")
//            List<Movie> findSimilarMovies(Movie movie);

}
