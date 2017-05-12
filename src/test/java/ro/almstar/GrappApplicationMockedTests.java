package ro.almstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ro.almstar.controller.MyRestController;
import ro.almstar.domain.Company;
import ro.almstar.domain.Complaint;
import ro.almstar.services.CompanyService;
import ro.almstar.services.ComplaintService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
public class GrappApplicationMockedTests {

    /**
     * When Spring Boot is configured to use Tomcat, it appears that returned content type is UTF-8
     */
    private static final MediaType UTF8_HAL_JSON_CONTENT_TYPE =
            new MediaType("application", "hal+json", Charset.forName("UTF-8"));

    private static final Long COMPLAINT_ID = 1L;
    private static final Long COMPANY_ID = 1L;
    private static final String COMPANY_NAME = "Company name";

    @InjectMocks
    private MyRestController restController;

    @Mock
    private ComplaintService complaintService;

    @Mock
    private CompanyService companyService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Bean
    public ObjectMapper getObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper responseMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return responseMapper;
    }

    private Complaint createDummyComplaint() {
        Complaint complaint = new Complaint();

        complaint.setId(1L);
        complaint.setYear(2017);
        complaint.setMonth(4);
        complaint.setDat(21);

        createDummyCompany(complaint);

        return complaint;
    }

    private Company createDummyCompany(Complaint... complaint) {
        Company company = new Company();
        company.setId(COMPANY_ID);
        company.setName(COMPANY_NAME);
        if (complaint != null && complaint.length > 0) {
            company.setComplaint(complaint[0]);
            complaint[0].setCompany(company);
        }
        return company;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = standaloneSetup(restController)
                .build();
    }

    @Test
    public void findById() throws Exception {
        when(complaintService.findById(any(Long.class))).thenReturn(createDummyComplaint());

        this.mockMvc.perform(
                get("/api/v1/complaints/{id}", COMPLAINT_ID)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE));
    }

    @Test
    public void createComplaint() throws Exception {

        this.mockMvc.perform(
                post("/api/v1/complaints")
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON)
                        .content(this.getObjectMapper().writeValueAsString(createDummyComplaint())
                        ))
                .andExpect(status().isCreated());
    }

    @Test
    public void getComplaintsForCompanyId() throws Exception {
        List<Complaint> complaints = new ArrayList<Complaint>();
        complaints.add(createDummyComplaint());
        when(complaintService.getComplaintsForCompanyID(any(Long.class))).thenReturn(complaints);
        byte[] bytes = this.getObjectMapper().writeValueAsBytes(complaints);

        this.mockMvc.perform(
                get("/api/v1/complaints/company")
                        .param("id", COMPANY_ID.toString())
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE))
                .andExpect(content().bytes(bytes));
    }

    @Test
    public void findByNameContaining() throws Exception {
        List<Company> companies = new ArrayList<Company>();
        companies.add(createDummyCompany());
        when(companyService.findByNameContaining(any(String.class))).thenReturn(companies);

        this.mockMvc.perform(
                get("/api/v1/companies/findByNameContaining")
                        .param("companyName", COMPANY_NAME)
                        .accept(MediaTypes.HAL_JSON)
                        .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(UTF8_HAL_JSON_CONTENT_TYPE));
    }
}
