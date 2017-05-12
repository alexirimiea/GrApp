package ro.almstar.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import ro.almstar.validators.ValidYear;

import javax.validation.constraints.NotNull;

/**
 * Created by itsix on 3/7/2017.
 */
@NodeEntity(label = "Complaint")
public class Complaint {

    @GraphId
    private Long id;

    //Custom Validator
    @ValidYear
    private Integer year;
    //Hibernate Validator annotation
    @NotNull(message = "Month cannot be null!")
    //@NotEmpty cannot be used for Integer or a UnexpectedTypeException will be thrown
    private Integer month;
    private Integer dat;//TODO rename with "day"

    @Relationship(type = "TO", direction = Relationship.INCOMING)
    @JsonManagedReference(value="complaintResponse")
    private Response response;

    @Relationship(type = "AGAINST")
    @JsonBackReference(value="company")
    private Company company;

    @Relationship(type = "ABOUT")
    @JsonManagedReference(value="complaintProduct")
    private Product product;

    @Relationship(type = "ABOUT")
    @JsonManagedReference(value="complaintSubProduct")
    private SubProduct subProduct;

    @Relationship(type = "WITH")
    @JsonManagedReference(value="complaintIssue")
    private Issue issue;

    @Relationship(type = "WITH")
    @JsonManagedReference(value="complaintSubIssue")
    private SubIssue subIssue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDat() {
        return dat;
    }

    public void setDat(Integer dat) {
        this.dat = dat;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public SubProduct getSubProduct() {
        return subProduct;
    }

    public void setSubProduct(SubProduct subProduct) {
        this.subProduct = subProduct;
    }
}
