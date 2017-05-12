package ro.almstar.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by itsix on 3/7/2017.
 */
@NodeEntity(label = "SubProduct")
public class SubProduct {

    @GraphId
    private Long id;
    private String name;

    @Relationship(type = "ABOUT", direction = Relationship.INCOMING)
    @JsonBackReference(value="complaintSubProduct")
    private Complaint complaint;

    @Relationship(type = "IN_CATEGORY")
    @JsonManagedReference(value="subProduct")
    private Product product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }
}
