package ro.almstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.almstar.domain.Company;
import ro.almstar.domain.Complaint;
import ro.almstar.exceptions.ResourceNotFoundException;
import ro.almstar.services.CompanyService;
import ro.almstar.services.ComplaintService;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by itsix on 3/7/2017.
 */
@RestController
@RequestMapping("/api/v1/")
public class MyRestController {

    @Autowired
    ComplaintService complaintService;

    @Autowired
    CompanyService companyService;

    /**
     * Sample call: http://localhost:8090/api/v1/complaints/7
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "complaints/{id}", method = RequestMethod.GET)
    public Complaint findById(@PathVariable(value = "id") Long id) {
        Complaint complaint = complaintService.findById(id);
        if (complaint == null) {
            throw new ResourceNotFoundException("Complaint with ID=" + id + " not found!");
        }
        return complaint;
    }

    /**
     * Requires the JSR 303 and JSR 349 define specifications for the Bean Validation API (version 1.0 and 1.1, respectively).
     * Might also need Hibernate Validator - for some extra validations: @Email, @NotEmpty or @DateTimeFormat
     *
     * @param complaint
     * @return
     */
    @RequestMapping(value = "/complaints", method = RequestMethod.POST)
    public ResponseEntity<?> createComplaint(@Valid @RequestBody Complaint complaint) {

        //TODO
        //complaint = complaintService.save(complaint);

        // Set the location header for the newly created resource
        HttpHeaders responseHeaders = new HttpHeaders();
        URI newComplaintURI = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(complaint.getId())
                .toUri();
        responseHeaders.setLocation(newComplaintURI);

        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
    }

//    /**
//     * Sample call: http://localhost:8090/api/v1/complaints/companies/AMEX
//     * @param companyName
//     * @return
//     */
//    @RequestMapping(value = "complaints/companies/{companyName}", method = RequestMethod.GET)
//    public List<Complaint> getComplaintsForCompany(@PathVariable(value="companyName") String companyName) {
//    @RequestMapping(value = "complaints/companies", method = RequestMethod.GET)
//    public List<Complaint> getComplaintsForCompany(@RequestParam(value="companyName") String companyName) {
//        return complaintService.getComplaintsForCompany(companyName);
//    }

    @RequestMapping(value = "complaints/company", method = RequestMethod.GET)
    public List<Complaint> getComplaintsForCompanyID(@RequestParam(value = "id") Long companyID) {
        return complaintService.getComplaintsForCompanyID(companyID);
    }

    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        return complaintService.graph(limit == null ? 1 : limit);
    }

    @RequestMapping(value = "/companies/findByNameContaining", method = RequestMethod.GET)
    public Collection<Company> findByNameContaining(@RequestParam("companyName") String name) {
        return companyService.findByNameContaining(name);
    }
}
