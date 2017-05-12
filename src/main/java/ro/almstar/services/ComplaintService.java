package ro.almstar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.almstar.domain.Complaint;
import ro.almstar.repositories.ComplaintRepository;

import java.util.*;

/**
 * Created by itsix on 3/7/2017.
 */
@Service
public class ComplaintService {
    @Autowired
    ComplaintRepository complaintRepository;

    public Complaint findById(Long id) {
        return complaintRepository.findOne(id);
    }

    public List<Complaint> getComplaintsForCompany(String companyName) {
        return complaintRepository.getComplaintsForCompany(companyName);
    }

    public List<Complaint> getComplaintsForCompanyID(Long companyID) {
        return complaintRepository.getComplaintsForCompanyID(companyID);
    }

    private Map<String, Object> toD3Format(Iterator<Map<String, Object>> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
        int i = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map("name", row.get("company"), "label", "company"));
            int target = i;
            i++;
//            for (Object name : (Collection) row.get("complaints")) {
            for (String name : (String[]) row.get("complaints")) {
                Map<String, Object> actor = map("date", name, "label", "complaint");
                int source = nodes.indexOf(actor);
                if (source == -1) {
                    nodes.add(actor);
                    source = i++;
                }
                relations.add(map("source", source, "target", target));
            }
        }
        return map("nodes", nodes, "links", relations);
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    public Map<String, Object> graph(int limit) {
        Iterator<Map<String, Object>> result = complaintRepository.graph(limit).iterator();
        return toD3Format(result);
    }

//    @Autowired
//    Neo4jTemplate template;
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        template.query("MATCH (n:Person) SET n:`_Person`",null).finish();
//        template.query("MATCH (n:Movie) SET n:`_Movie`",null).finish();
//    }
}
