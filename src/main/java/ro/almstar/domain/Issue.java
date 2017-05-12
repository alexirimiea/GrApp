package ro.almstar.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by itsix on 3/7/2017.
 */
@NodeEntity(label = "Issue")
public class Issue {

    @GraphId
    private Long id;
    private String name;

    @Relationship(type = "WITH", direction = Relationship.INCOMING)
    @JsonBackReference(value="complaintIssue")
    private Complaint complaint;

    @Relationship(type = "IN_CATEGORY", direction = Relationship.INCOMING)
    @JsonBackReference(value="subIssue")
    private SubIssue subIssue;

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

    public SubIssue getSubIssue() {
        return subIssue;
    }

    public void setSubIssue(SubIssue subIssue) {
        this.subIssue = subIssue;
    }
}
