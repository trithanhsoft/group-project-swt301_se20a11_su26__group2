package com.swp391.coding_platform.controller.problem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.AdminProblemRequest;
import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.service.problem.AdminProblemService;
import com.swp391.coding_platform.service.problem.ProblemTagService;
import com.swp391.coding_platform.service.problem.ProblemTestcaseService;
import com.swp391.coding_platform.service.problem.UserProblemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AdminProblemController.class)
public class AdminProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminProblemService adminProblemService;

    @MockBean
    private ProblemTagService problemTagService;

    @MockBean
    private ProblemTestcaseService problemTestcaseService;

    @MockBean
    private UserProblemService userProblemService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        context.setAuthentication(auth);
        
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    public void getProblems_ShouldReturnOk() throws Exception {
        when(adminProblemService.getAdminProblems()).thenReturn(List.of(new AdminProblemResponse()));

        mockMvc.perform(get("/admin/problems")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTags_ShouldReturnOk() throws Exception {
        when(problemTagService.getAllTags()).thenReturn(List.of());

        mockMvc.perform(get("/admin/problems/tags")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void createProblem_ShouldReturnOk() throws Exception {
        AdminProblemRequest req = new AdminProblemRequest();
        req.setTitle("Two Sum");
        
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        res.setTitle("Two Sum");

        when(adminProblemService.createAdminProblem(any(), eq(1))).thenReturn(res);

        mockMvc.perform(post("/admin/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(10))
                .andExpect(jsonPath("$.result.title").value("Two Sum"));
    }

    @Test
    public void updateProblem_ShouldReturnOk() throws Exception {
        AdminProblemRequest req = new AdminProblemRequest();
        req.setTitle("Updated Title");

        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        res.setTitle("Updated Title");

        when(adminProblemService.updateAdminProblem(eq(10), any())).thenReturn(res);

        mockMvc.perform(put("/admin/problems/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.title").value("Updated Title"));
    }

    @Test
    public void deleteProblem_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/admin/problems/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted problem successfully"));

        verify(adminProblemService, times(1)).deleteAdminProblem(10);
    }

    @Test
    public void updateProblemScope_ShouldReturnOk() throws Exception {
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        when(adminProblemService.updateAdminProblemScope(10, "CONTEST")).thenReturn(res);

        mockMvc.perform(post("/admin/problems/10/scope")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("problemScope", "CONTEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(10));
    }

    @Test
    public void updateProblemPublicStatus_ShouldReturnOk() throws Exception {
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        when(adminProblemService.updateAdminProblemPublicStatus(10, true)).thenReturn(res);

        mockMvc.perform(post("/admin/problems/10/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("isPublic", true))))
                .andExpect(status().isOk());
    }

    @Test
    public void rollbackProblem_ShouldReturnOk() throws Exception {
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        when(adminProblemService.rollbackAdminProblem(10, 50)).thenReturn(res);

        mockMvc.perform(post("/admin/problems/10/rollback/50"))
                .andExpect(status().isOk());
    }

    @Test
    public void activateProblem_ShouldReturnOk() throws Exception {
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(10);
        when(adminProblemService.activateAdminProblem(10, 5)).thenReturn(res);

        mockMvc.perform(post("/admin/problems/10/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("totalTestcases", 5))))
                .andExpect(status().isOk());
    }

    @Test
    public void getProblemTestcases_ShouldReturnOk() throws Exception {
        when(problemTestcaseService.getProblemTestcases(10)).thenReturn(List.of());

        mockMvc.perform(get("/admin/problems/10/testcases"))
                .andExpect(status().isOk());
    }

    @Test
    public void saveProblemTestcases_ShouldReturnOk() throws Exception {
        AdminTestcaseRequest req = new AdminTestcaseRequest();
        when(problemTestcaseService.saveProblemTestcases(eq(10), anyList())).thenReturn(List.of());

        mockMvc.perform(post("/admin/problems/10/testcases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(req))))
                .andExpect(status().isOk());
    }

    @Test
    public void getProblemVersions_ShouldReturnOk() throws Exception {
        when(adminProblemService.getProblemVersions(10)).thenReturn(List.of());

        mockMvc.perform(get("/admin/problems/10/versions"))
                .andExpect(status().isOk());
    }

    @Test
    public void cloneProblem_ShouldReturnOk() throws Exception {
        AdminProblemResponse res = new AdminProblemResponse();
        res.setId(11);
        when(adminProblemService.cloneProblem(10, 1)).thenReturn(res);

        mockMvc.perform(post("/admin/problems/10/clone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(11));
    }
}
