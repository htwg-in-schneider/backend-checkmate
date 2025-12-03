package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TutorController.
 * Testen sowohl REST-Endpunkte als auch die Datenbank (TutorRepository).
 */
@SpringBootTest
@Profile("test")
public class TutorControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private TutorRepository tutorRepository;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();

        // vor jedem Test DB leeren
        tutorRepository.deleteAll();
    }

    @Test
    public void testGetTutors() throws Exception {
        // GIVEN: Ein Tutor ist in der Datenbank
        Tutor tutor = new Tutor();
        tutor.setName("Lisa Weber");
        tutor.setSubject("Mathe I • Analysis");
        tutor.setSemester(5);
        tutor.setImage("https://example.com/lisa.jpg");
        tutorRepository.save(tutor);

        // WHEN: Alle Tutor:innen werden über REST abgefragt
        mockMvc.perform(get("/api/tutors"))
                // THEN: Status OK und Daten stimmen
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lisa Weber"))
                .andExpect(jsonPath("$[0].subject").value("Mathe I • Analysis"))
                .andExpect(jsonPath("$[0].semester").value(5))
                .andExpect(jsonPath("$[0].image").value("https://example.com/lisa.jpg"));
    }

    @Test
    public void testGetTutorById() throws Exception {
        // GIVEN: Ein Tutor ist in der Datenbank
        Tutor tutor = new Tutor();
        tutor.setName("Jonas Keller");
        tutor.setSubject("Programmierung mit Java");
        tutor.setSemester(3);
        tutor.setImage("https://example.com/jonas.jpg");
        tutor = tutorRepository.save(tutor);

        // WHEN: Tutor wird über ID angefragt
        mockMvc.perform(get("/api/tutors/" + tutor.getId()))
                .andDo(MockMvcResultHandlers.print())
                // THEN: Status OK und Details stimmen
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonas Keller"))
                .andExpect(jsonPath("$.subject").value("Programmierung mit Java"))
                .andExpect(jsonPath("$.semester").value(3))
                .andExpect(jsonPath("$.image").value("https://example.com/jonas.jpg"));
    }

    @Test
    public void testCreateTutor() throws Exception {
        // GIVEN: DB ist leer (durch @BeforeEach)

        // WHEN: Neuer Tutor wird via POST angelegt
        String tutorPayload =
                "{" +
                    "\"name\":\"Mia Hoffmann\"," +
                    "\"subject\":\"BWL Grundlagen • Statistik\"," +
                    "\"semester\":4," +
                    "\"image\":\"https://example.com/mia.jpg\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/api/tutors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tutorPayload))
                // THEN (1): Status OK (oder Created, je nach Implementierung) und Daten stimmen
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mia Hoffmann"))
                .andExpect(jsonPath("$.subject").value("BWL Grundlagen • Statistik"))
                .andExpect(jsonPath("$.semester").value(4))
                .andExpect(jsonPath("$.image").value("https://example.com/mia.jpg"))
                .andReturn();

        // THEN (2): Response enthält eine ID
        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode json = new ObjectMapper().readTree(responseContent);
        Long id = json.has("id") && !json.get("id").isNull() ? json.get("id").asLong() : null;
        assertNotNull(id, "created tutor should have id");

        // THEN (3): Tutor ist wirklich in der DB gespeichert
        Tutor saved = tutorRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Saved tutor not found, id: " + id));
        assertEquals("Mia Hoffmann", saved.getName());
        assertEquals("BWL Grundlagen • Statistik", saved.getSubject());
        assertEquals(4, saved.getSemester());
        assertEquals("https://example.com/mia.jpg", saved.getImage());
    }

    @Test
    public void testUpdateTutor() throws Exception {
        // GIVEN: Ein existierender Tutor in der DB
        Tutor existing = new Tutor();
        existing.setName("Old Name");
        existing.setSubject("Altes Fach");
        existing.setSemester(1);
        existing.setImage("https://example.com/old.jpg");
        Long id = tutorRepository.save(existing).getId();

        // WHEN: Tutor wird via PUT aktualisiert
        String updatePayload =
                "{" +
                    "\"name\":\"Neuer Name\"," +
                    "\"subject\":\"Neues Fach\"," +
                    "\"semester\":2," +
                    "\"image\":\"https://example.com/new.jpg\"" +
                "}";

        mockMvc.perform(put("/api/tutors/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                // THEN (1): Status OK und neue Daten stimmen
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neuer Name"))
                .andExpect(jsonPath("$.subject").value("Neues Fach"))
                .andExpect(jsonPath("$.semester").value(2))
                .andExpect(jsonPath("$.image").value("https://example.com/new.jpg"));

        // THEN (2): Daten sind auch in der DB aktualisiert
        Tutor updated = tutorRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Updated tutor not found, id: " + id));
        assertEquals("Neuer Name", updated.getName());
        assertEquals("Neues Fach", updated.getSubject());
        assertEquals(2, updated.getSemester());
        assertEquals("https://example.com/new.jpg", updated.getImage());
    }

    @Test
    public void testDeleteTutor() throws Exception {
        // GIVEN: Ein Tutor existiert in der DB
        Tutor tutor = new Tutor();
        tutor.setName("To Be Deleted");
        tutor.setSubject("Wird gelöscht");
        tutor.setSemester(7);
        tutor.setImage("https://example.com/delete.jpg");
        tutor = tutorRepository.save(tutor);

        // WHEN: Tutor wird via DELETE entfernt
        mockMvc.perform(delete("/api/tutors/" + tutor.getId()))
                // THEN (1): Status No Content
                .andExpect(status().isNoContent());

        // THEN (2): Tutor ist nicht mehr in der DB
        Optional<Tutor> deleted = tutorRepository.findById(tutor.getId());
        assertFalse(deleted.isPresent(), "Tutor should have been deleted");
    }
}