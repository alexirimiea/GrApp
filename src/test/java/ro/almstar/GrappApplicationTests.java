package ro.almstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ro.almstar.domain.Company;
import ro.almstar.domain.Complaint;
import ro.almstar.repositories.CompanyRepository;
import ro.almstar.repositories.ComplaintRepository;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class GrappApplicationTests {

    /**
     * When Spring Boot is configured to use Tomcat, it appears that returned content type is UTF-8
     */
    private static final MediaType UTF8_HAL_JSON_CONTENT_TYPE =
            new MediaType("application", "hal+json", Charset.forName("UTF-8"));

    public static final String SAMPLE_QUERY_COMPANIES_BY_NAME = "W FINANCIAL SERVICES";

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation))
//                .apply(springSecurity())
                .build();
    }

    @Test
    public void findById() throws Exception {
        ComplaintRepository complaintRepository = (ComplaintRepository) context.getBean("complaintRepository");
        // Convenient Pageable instance so that a single resource can be obtained from a given repository
        Pageable pageable = new PageRequest(0,1);
        Page<Complaint> complaintPage = complaintRepository.findAll(pageable);

        List<Complaint> complaintPageContent = complaintPage.getContent();
        Assert.assertNotNull(complaintPageContent);
        Assert.assertNotEquals(0, complaintPageContent.size());

        Complaint complaint = complaintPageContent.get(0);
        Long complaintId = complaint.getId();

        this.mockMvc.perform(
                get("/api/v1/complaints/{id}", complaintId)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE))
                .andDo(document("{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("Complaint's ID"))
                ));
    }

    @Test
    public void createComplaint() throws Exception {

        Map<String, String> newComplaint = new HashMap<>();
        newComplaint.put("year", "2017");
        newComplaint.put("month", "3");
        newComplaint.put("dat", "29");

        //TODO document javax.validation and/or Hibernate validation and/or custom validation rules (automatically?)
        ConstraintDescriptions complaintConstraints = new ConstraintDescriptions(Complaint.class);
//        List<String> yearConstraints = complaintConstraints.descriptionsForProperty("year");
//        List<String> monthConstraints = complaintConstraints.descriptionsForProperty("month");

        this.mockMvc.perform(
                post("/api/v1/complaints")
                    .accept(MediaTypes.HAL_JSON)
                    .contentType(MediaTypes.HAL_JSON)
                    .content(this.objectMapper.writeValueAsString(newComplaint)
                ))
                .andExpect(status().isCreated())
                .andDo(document("{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        attributes(key("title").value("Fields for complaint creation")),
                        fieldWithPath("year")
                                .description("Year")
                                .attributes(key("constraints").value("Must not be null. Must be greater than 1900.")),
                        fieldWithPath("month")
                                .description("Month")
                                .attributes(key("constraints").value("Must not be null. Must not be empty")),
                        fieldWithPath("dat")
                                .description("Day")
                                .attributes(key("constraints").value("Must not be null. Must be a valid day (1..31)"))
                    )
                ));
    }

    @Test
    public void getComplaintsForCompanyId() throws Exception {
        CompanyRepository companyRepository = (CompanyRepository) context.getBean("companyRepository");

        Collection<Company> companies = companyRepository.findByNameContaining(SAMPLE_QUERY_COMPANIES_BY_NAME);
        Assert.assertNotNull(companies);
        Assert.assertNotEquals(0, companies.size());
        Company company = companies.iterator().next();
        Long companyId = company.getId();

        this.mockMvc.perform(
                get("/api/v1/complaints/company")
                        .param("id", companyId.toString())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE))
                .andDo(document("{method-name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(parameterWithName("id").description("Company's ID"))
                ));
    }

    @Test
    public void findByNameContaining() throws Exception {
        this.mockMvc.perform(
                get("/api/v1/companies/findByNameContaining")
                        .param("companyName", SAMPLE_QUERY_COMPANIES_BY_NAME)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE))
                .andDo(document("{method-name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(parameterWithName("companyName").description("Company's name (or a part of)"))
                ));
    }

//    @Test
//    public void indexExample() throws Exception {
//        this.document.snippets(
//                links(
//                        linkWithRel("notes").description("The <<resources-notes,Notes resource>>"),
//                        linkWithRel("tags").description("The <<resources-tags,Tags resource>>")
//                ),
//                responseFields(
//                        fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")
//                )
//        );
//        this.mockMvc.perform(get("/rest/api/v1")).andExpect(status().isOk());
//    }

}
