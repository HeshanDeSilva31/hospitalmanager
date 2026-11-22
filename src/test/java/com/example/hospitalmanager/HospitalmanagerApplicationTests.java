package com.example.hospitalmanager;

import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class HospitalmanagerApplicationTests {

	private static final String TEST_JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
	private static final String TEST_PASSWORD = "StrongPass123!";
	private static final @NonNull MediaType JSON =
	        Objects.requireNonNull(MediaType.APPLICATION_JSON);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void registrationCreatesPatientAndStoresBCryptHash() throws Exception {
	    String username = uniqueUsername("patient");
	    mockMvc.perform(post("/api/auth/register")
	                    .contentType(JSON)
	                    .content(registrationJson(username)))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.role").value("PATIENT"))
	            .andExpect(jsonPath("$.password").doesNotExist());

	    AppUser user = userRepository.findByUsername(username).orElseThrow();
	    assertTrue(passwordEncoder.matches(TEST_PASSWORD, user.getPassword()));
	    assertNotNull(patientRepository.findByUser_Username(username).orElseThrow());
	}

	@Test
	void duplicateRegistrationReturnsConflict() throws Exception {
	    String username = uniqueUsername("patient");
	    register(username);

	    mockMvc.perform(post("/api/auth/register")
	                    .contentType(JSON)
	                    .content(registrationJson(username)))
	            .andExpect(status().isConflict())
	            .andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void patientCannotAccessAdminPatients() throws Exception {
	    String username = uniqueUsername("patient");
	    register(username);
	    String token = login(username);

	    mockMvc.perform(get("/api/admin/patients").header("Authorization", "Bearer " + token))
	            .andExpect(status().isForbidden())
	            .andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void modifiedPatientRoleClaimIsRejected() throws Exception {
	    String username = uniqueUsername("patient");
	    register(username);
	    String token = login(username);
	    String[] parts = token.split("\\.");
	    ObjectNode claims = (ObjectNode) objectMapper.readTree(
	            Base64.getUrlDecoder().decode(parts[1]));
	    claims.put("role", "ROLE_ADMIN");
	    String modifiedPayload = Base64.getUrlEncoder().withoutPadding()
	            .encodeToString(objectMapper.writeValueAsBytes(claims));
	    String modifiedToken = parts[0] + "." + modifiedPayload + "." + parts[2];

	    mockMvc.perform(get("/api/admin/patients").header("Authorization", "Bearer " + modifiedToken))
	            .andExpect(status().isUnauthorized())
	            .andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void validSignatureDoesNotAllowTokenRoleToOverrideDatabaseRole() throws Exception {
	    String username = uniqueUsername("patient");
	    register(username);
	    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_JWT_SECRET));
	    String tokenWithElevatedClaim = Jwts.builder()
	            .setSubject(username)
	            .claim("role", "ROLE_ADMIN")
	            .setIssuedAt(new Date())
	            .setExpiration(new Date(System.currentTimeMillis() + 60_000))
	            .signWith(key, SignatureAlgorithm.HS256)
	            .compact();

	    mockMvc.perform(get("/api/admin/patients")
	                    .header("Authorization", "Bearer " + tokenWithElevatedClaim))
	            .andExpect(status().isForbidden());
	}

	@Test
	void expiredTokenIsRejected() throws Exception {
	    String username = uniqueUsername("missing");
	    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_JWT_SECRET));
	    String expiredToken = Jwts.builder()
	            .setSubject(username)
	            .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
	            .setExpiration(new Date(System.currentTimeMillis() - 1_000))
	            .signWith(key, SignatureAlgorithm.HS256)
	            .compact();

	    mockMvc.perform(get("/api/admin/patients").header("Authorization", "Bearer " + expiredToken))
	            .andExpect(status().isUnauthorized());
	}

	@Test
	void malformedTokenIsRejected() throws Exception {
	    mockMvc.perform(get("/api/admin/patients")
	                    .header("Authorization", "Bearer not.a.jwt"))
	            .andExpect(status().isUnauthorized())
	            .andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void adminCanAccessAdminPatients() throws Exception {
	    String username = uniqueUsername("admin");
	    AppUser administrator = new AppUser();
	    administrator.setUsername(username);
	    administrator.setPassword(passwordEncoder.encode(TEST_PASSWORD));
	    administrator.setName("Test Administrator");
	    administrator.setRole(Role.ADMIN);
	    userRepository.save(administrator);

	    mockMvc.perform(get("/api/admin/patients").header("Authorization", "Bearer " + login(username)))
	            .andExpect(status().isOk());
	}

	@Test
	void patientDoctorAppointmentAndMedicalRecordWorkflow() throws Exception {
	    String adminUsername = uniqueUsername("admin");
	    AppUser administrator = new AppUser();
	    administrator.setUsername(adminUsername);
	    administrator.setPassword(passwordEncoder.encode(TEST_PASSWORD));
	    administrator.setName("Test Administrator");
	    administrator.setRole(Role.ADMIN);
	    userRepository.save(administrator);
	    String adminToken = login(adminUsername);

	    String doctorUsername = uniqueUsername("doctor");
	    MvcResult doctorResult = mockMvc.perform(post("/api/admin/doctors")
	                    .header("Authorization", "Bearer " + adminToken)
	                    .contentType(JSON)
	                    .content(json(Map.of(
	                            "name", "Test Doctor",
	                            "username", doctorUsername,
	                            "password", TEST_PASSWORD))))
	            .andExpect(status().isCreated())
	            .andReturn();
	    Long doctorId = objectMapper.readTree(doctorResult.getResponse().getContentAsString())
	            .get("id").asLong();

	    String patientUsername = uniqueUsername("patient");
	    register(patientUsername);
	    String patientToken = login(patientUsername);
	    LocalDateTime appointmentDate = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
	    MvcResult appointmentResult = mockMvc.perform(post("/api/patients/appointments")
	                    .header("Authorization", "Bearer " + patientToken)
	                    .contentType(JSON)
	                    .content(json(Map.of(
	                            "doctorId", doctorId,
	                            "appointmentDate", appointmentDate))))
	            .andExpect(status().isCreated())
	            .andReturn();
	    Long appointmentId = objectMapper.readTree(appointmentResult.getResponse().getContentAsString())
	            .get("id").asLong();
	    Long patientId = patientRepository.findByUser_Username(patientUsername).orElseThrow().getId();
	    String doctorToken = login(doctorUsername);

	    mockMvc.perform(get("/api/doctors/appointments")
	                    .header("Authorization", "Bearer " + doctorToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$[0].patientId").value(patientId));

	    mockMvc.perform(post("/api/doctors/medical-records")
	                    .header("Authorization", "Bearer " + doctorToken)
	                    .contentType(JSON)
	                    .content(json(Map.of(
	                            "patientId", patientId,
	                            "diagnosis", "Routine checkup",
	                            "prescription", "Follow-up in one month"))))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.doctorId").value(doctorId));

	    mockMvc.perform(get("/api/patients/medical-records")
	                    .header("Authorization", "Bearer " + patientToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$[0].diagnosis").value("Routine checkup"));

	    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
	                    .patch("/api/appointments/{appointmentId}/status", appointmentId)
	                    .header("Authorization", "Bearer " + doctorToken)
	                    .contentType(JSON)
	                    .content(json(Map.of("status", "COMPLETED"))))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	private void register(String username) throws Exception {
	    mockMvc.perform(post("/api/auth/register")
	                    .contentType(JSON)
	                    .content(registrationJson(username)))
	            .andExpect(status().isCreated());
	}

	@NonNull
	private String login(String username) throws Exception {
	    MvcResult result = mockMvc.perform(post("/api/auth/login")
	                    .contentType(JSON)
	                    .content(json(
	                            Map.of("username", username, "password", TEST_PASSWORD))))
	            .andExpect(status().isOk())
	            .andReturn();
	    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
	    return Objects.requireNonNull(
	            Objects.requireNonNull(response.get("accessToken")).asText());
	}

	@NonNull
	private String registrationJson(String username) throws Exception {
	    return json(Map.of(
	            "name", "Test Patient",
	            "username", username,
	            "password", TEST_PASSWORD,
	            "email", username + "@example.test",
	            "phone", "555-0100",
	            "address", "Test address",
	            "age", 25));
	}

	@NonNull
	private <T> String json(@Nullable T value) throws Exception {
	    return Objects.requireNonNull(objectMapper.writeValueAsString(value));
	}

	private String uniqueUsername(String prefix) {
	    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
	}
}
