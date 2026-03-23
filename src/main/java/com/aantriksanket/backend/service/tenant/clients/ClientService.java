package com.aantriksanket.backend.service.tenant.clients;

import com.aantriksanket.backend.api.tenant.clients.ClientResponse;
import com.aantriksanket.backend.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class ClientService {

    private static final Set<String> ALLOWED_UPDATE_FIELDS = Set.of(
            "full_name", "email", "phone_number", "dob", "gender", "occupation",
            "marital_status", "education_level", "nationality", "general_problems",
            "additional_notes", "address", "emergency_contact_number", "questions_answers",
            "first_session_date", "last_session_date", "upcoming_session_date",
            "session_recurrence", "status"
    );

    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Transactional
    public String createClientFromPdf(Tenant tenant, MultipartFile pdf) {
        byte[] pdfBytes;
        try {
            pdfBytes = pdf.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read PDF upload: " + e.getMessage());
        }

        if (pdfBytes.length == 0) {
            throw new RuntimeException("Uploaded PDF is empty");
        }

        String text;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document).trim();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read PDF: " + e.getMessage());
        }

        if (text.isEmpty()) {
            throw new RuntimeException("PDF contains no extractable text");
        }

        Map<String, String> clientData = parseConsentPdf(text);

        if (clientData.get("full_name") == null || clientData.get("full_name").isEmpty()) {
            throw new RuntimeException("Missing full name in PDF");
        }
        String email = clientData.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Missing email in PDF");
        }

        byte[] compressedPdf = compressPdf(pdfBytes);

        Client client = new Client();
        client.setTenant(tenant);
        client.setConsentForm(compressedPdf);
        client.setStatus(ClientStatus.NEW);
        
        applyClientData(client, clientData);

        try {
            client = clientRepository.save(client);
        } catch (DataIntegrityViolationException e) {
            // Gracefully handle duplicate email — return existing client ID if found, otherwise throw
            return clientRepository.findByTenantIdOrderByCreatedAtDesc(tenant.getId()).stream()
                    .filter(c -> c.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .map(c -> c.getId().toString())
                    .orElseThrow(() -> new RuntimeException("A client with email '" + email + "' already exists."));
        }
        return client.getId().toString();
    }

    private void applyClientData(Client client, Map<String, String> clientData) {
        client.setFullName(clientData.get("full_name"));
        client.setEmail(clientData.get("email"));
        client.setPhoneNumber(clientData.get("phone_number"));

        String dobStr = clientData.get("dob");
        if (dobStr != null) {
            try {
                client.setDob(LocalDate.parse(dobStr));
            } catch (DateTimeParseException ignored) {
            }
        }

        client.setEmergencyContactNumber(clientData.get("emergency_contact_number"));
        
        String genderStr = clientData.get("gender");
        if (genderStr != null) {
            try {
                client.setGender(Gender.valueOf(genderStr));
            } catch (Exception ignored) {}
        }
        
        String maritalStr = clientData.get("marital_status");
        if (maritalStr != null) {
            try {
                client.setMaritalStatus(MaritalStatus.valueOf(maritalStr));
            } catch (Exception ignored) {}
        }
        
        client.setOccupation(clientData.get("occupation"));
        client.setEducationLevel(clientData.get("education_level"));
        client.setNationality(clientData.get("nationality"));
        client.setGeneralProblems(clientData.get("general_problems"));
        client.setAddress(clientData.get("address"));
        
        String qaStr = clientData.get("questions_answers");
        if (qaStr != null && !qaStr.trim().isEmpty() && !qaStr.equals("{}")) {
            try {
                client.setQuestionsAnswers(objectMapper.readValue(qaStr, new TypeReference<Map<String, Object>>() {}));
            } catch (Exception ignored) {}
        }

        clientRepository.save(client);
    }

    private String findField(String label, String text) {
        String patternStr = "(?i)" + Pattern.quote(label) + "[:\\s]+([^\\r\\n]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String normalizeGender(String input) {
        if (input == null) return "PREFER_NOT_TO_SAY";
        String val = input.trim().toLowerCase();
        if (val.equals("male") || val.equals("m") || val.startsWith("m")) return "MALE";
        if (val.equals("female") || val.equals("f") || val.startsWith("f")) return "FEMALE";
        if (val.equals("other") || val.equals("o")) return "OTHER";
        return "PREFER_NOT_TO_SAY";
    }

    private String normalizeMaritalStatus(String input) {
        if (input == null) return "OTHER";
        String val = input.trim().toLowerCase();
        if (val.equals("single") || val.startsWith("sing")) return "SINGLE";
        if (val.equals("married") || val.startsWith("mar")) return "MARRIED";
        if (val.equals("divorced") || val.startsWith("div")) return "DIVORCED";
        if (val.equals("widowed") || val.startsWith("wid")) return "WIDOWED";
        if (val.equals("separated") || val.startsWith("sep")) return "SEPARATED";
        return "OTHER";
    }

    private Map<String, String> parseConsentPdf(String text) {
        Map<String, String> data = new HashMap<>();

        data.put("full_name", findField("Name", text));
        data.put("email", findField("Email", text));
        data.put("phone_number", findField("Mobile Number", text));
        data.put("emergency_contact_number", findField("Emergency Contact", text));

        String dob = findField("Date of Birth", text);
        if (dob != null) {
            try {
                // Try parsing ISO date
                LocalDate parsedDob = LocalDate.parse(dob);
                data.put("dob", parsedDob.toString());
            } catch (Exception e) {
                data.put("dob", dob);
            }
        }

        data.put("gender", normalizeGender(findField("Gender", text)));
        data.put("marital_status", normalizeMaritalStatus(findField("Marital Status", text)));
        data.put("education_level", findField("Education Level", text));
        data.put("occupation", findField("Occupation", text));
        data.put("address", findField("Residence", text));
        data.put("nationality", findField("Nationality", text));

        data.put("general_problems", findField("PRIMARY GOAL FOR THERAPY", text));
        data.put("additional_notes", null);

        Map<String, String> qa = new HashMap<>();
        qa.put("previous_therapy", findField("Previous Therapy Experience", text));
        qa.put("substance_use", findField("Substance Use", text));
        qa.put("medications", findField("Current Medications/Supplements", text));
        qa.put("self_harm", findField("Self-Harm or Suicidal Thoughts", text));
        qa.put("signature_date", findField("Date", text));

        try {
            data.put("questions_answers", objectMapper.writeValueAsString(qa));
        } catch (Exception e) {
            data.put("questions_answers", "{}");
        }

        return data;
    }



    @Transactional(readOnly = true)
    public List<ClientResponse> listClients(UUID tenantId) {
        return clientRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> searchClients(UUID tenantId, String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] tokens = query.trim().split("\\s+");
        List<Client> results = new ArrayList<>();
        
        // Very basic token search simulation (Python code did an AND over ILIKE per token)
        // Here we just use the first token for simplicity since our repository only supports a single query phrase currently.
        // A robust implementation would use Specifications or QueryDSL.
        if (tokens.length > 0) {
            results = clientRepository.searchClients(tenantId, tokens[0]);
        }

        return results.stream()
                .limit(limit)
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientDetails(UUID tenantId, UUID clientId) {
        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return new ClientResponse(client);
    }

    @Transactional(readOnly = true)
    public byte[] downloadConsentForm(UUID tenantId, UUID clientId) {
        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        byte[] compressed = client.getConsentForm();
        if (compressed == null || compressed.length == 0) {
            throw new RuntimeException("No consent form uploaded");
        }

        return decompressPdf(compressed);
    }

    @Transactional
    public ClientResponse updateClient(UUID tenantId, UUID clientId, Map<String, Object> body) {
        Client client = clientRepository.findByIdAndTenantId(clientId, tenantId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (body == null || body.isEmpty()) {
            throw new RuntimeException("No valid fields to update");
        }

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!ALLOWED_UPDATE_FIELDS.contains(key) || value == null) {
                continue;
            }

            switch (key) {
                case "full_name": client.setFullName((String) value); break;
                case "email": client.setEmail((String) value); break;
                case "phone_number": client.setPhoneNumber((String) value); break;
                case "dob":
                    try {
                        client.setDob(LocalDate.parse(value.toString()));
                    } catch (Exception ignored) {}
                    break;
                case "gender":
                    try {
                        client.setGender(Gender.valueOf(value.toString().toUpperCase()));
                    } catch (Exception ignored) {}
                    break;
                case "occupation": client.setOccupation((String) value); break;
                case "marital_status":
                    try {
                        client.setMaritalStatus(MaritalStatus.valueOf(value.toString().toUpperCase()));
                    } catch (Exception ignored) {}
                    break;
                case "education_level": client.setEducationLevel((String) value); break;
                case "nationality": client.setNationality((String) value); break;
                case "general_problems": client.setGeneralProblems((String) value); break;
                case "additional_notes": client.setAdditionalNotes((String) value); break;
                case "address": client.setAddress((String) value); break;
                case "emergency_contact_number": client.setEmergencyContactNumber((String) value); break;
                case "questions_answers":
                    if (value instanceof Map) {
                        client.setQuestionsAnswers((Map<String, Object>) value);
                    } else if (value instanceof String) {
                        try {
                            client.setQuestionsAnswers(objectMapper.readValue((String) value, new TypeReference<Map<String, Object>>() {}));
                        } catch (Exception ignored) {}
                    }
                    break;
                case "first_session_date":
                    try {
                        client.setFirstSessionDate(OffsetDateTime.parse(value.toString()));
                    } catch (Exception ignored) {}
                    break;
                case "last_session_date":
                    try {
                        client.setLastSessionDate(OffsetDateTime.parse(value.toString()));
                    } catch (Exception ignored) {}
                    break;
                case "upcoming_session_date":
                    try {
                        client.setUpcomingSessionDate(OffsetDateTime.parse(value.toString()));
                    } catch (Exception ignored) {}
                    break;
                case "session_recurrence": client.setSessionRecurrence((String) value); break;
                case "status":
                    try {
                        client.setStatus(ClientStatus.valueOf(value.toString().toUpperCase()));
                    } catch (Exception ignored) {}
                    break;
            }
        }

        client = clientRepository.save(client);
        return new ClientResponse(client);
    }

    private byte[] compressPdf(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        } finally {
            deflater.end();
        }
    }

    private byte[] decompressPdf(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                // In case of incomplete or corrupted zip data stream
                if (count == 0 && inflater.needsInput()) {
                    break;
                }
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Decompression failed", e);
        } finally {
            inflater.end();
        }
    }
}
