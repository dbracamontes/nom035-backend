package com.example.nom035.config;

import com.example.nom035.entity.OptionAnswer;
import com.example.nom035.entity.Question;
import com.example.nom035.entity.Survey;
import com.example.nom035.repository.OptionAnswerRepository;
import com.example.nom035.repository.QuestionRepository;
import com.example.nom035.repository.SurveyRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicaLebenSurveySeeder implements CommandLineRunner {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final OptionAnswerRepository optionAnswerRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:seed/medica_leben_survey.json")
    private Resource definitionResource;

    @Override
    public void run(String... args) {
        if (definitionResource == null || !definitionResource.exists()) {
            log.debug("No Medica Leben survey definition found, skipping seeding.");
            return;
        }

        SurveyDefinition definition;
        try (InputStream stream = definitionResource.getInputStream()) {
            definition = objectMapper.readValue(stream, SurveyDefinition.class);
        } catch (IOException ex) {
            log.error("Unable to load Medica Leben definition", ex);
            return;
        }

        if (definition == null || definition.getQuestions() == null || definition.getQuestions().isEmpty()) {
            log.warn("Medica Leben definition is empty, skipping seeding.");
            return;
        }

        Survey survey = surveyRepository
                .findByTitleIgnoreCase(definition.getTitle())
                .orElseGet(() -> createSurvey(definition));

        List<Question> existingQuestions = questionRepository.findBySurveyId(survey.getId());
        boolean needsRefresh = existingQuestions.size() != definition.getQuestions().size();

        if (!needsRefresh && !existingQuestions.isEmpty()) {
            Map<Integer, QuestionDefinition> definitionByNumber = definition
                    .getQuestions()
                    .stream()
                    .collect(Collectors.toMap(QuestionDefinition::getNumber, q -> q));

            needsRefresh = existingQuestions.stream().anyMatch(existing -> {
                QuestionDefinition expected = definitionByNumber.get(existing.getSortOrder());
                if (expected == null) {
                    return true;
                }
                List<String> expectedOptions = Optional.ofNullable(expected.getOptions()).orElse(Collections.emptyList());
                if (expectedOptions.isEmpty()) {
                    return false;
                }
                int existingCount = existing.getOptions() != null ? existing.getOptions().size() : 0;
                return existingCount < expectedOptions.size();
            });
        }

        if (!needsRefresh && !existingQuestions.isEmpty()) {
            log.info("Medica Leben survey already seeded ({} questions)", existingQuestions.size());
            return;
        }

        if (!existingQuestions.isEmpty()) {
            log.info("Replacing existing Medica Leben survey definition ({} questions).", existingQuestions.size());
            questionRepository.deleteAll(existingQuestions);
        }

        definition.getQuestions().forEach(questionDefinition -> {
            Question question = new Question();
            question.setSurvey(survey);
            question.setSurveyId(survey.getId());
            question.setText(questionDefinition.getText());
            question.setType(questionDefinition.getType());
            question.setCategory(questionDefinition.getCategory());
            question.setSortOrder(questionDefinition.getNumber());
            question.setGuideType(definition.getGuideType());
            writeMetadata(question, questionDefinition.getMetadata());

            Question savedQuestion = questionRepository.save(question);

            List<String> optionLabels = Optional.ofNullable(questionDefinition.getOptions()).orElse(Collections.emptyList());
            AtomicInteger counter = new AtomicInteger(1);
            optionLabels.forEach(label -> {
                OptionAnswer option = new OptionAnswer();
                option.setQuestion(savedQuestion);
                option.setText(label);
                option.setSortOrder(counter.get());
                option.setValue(counter.getAndIncrement());

                boolean requiresFreeText = questionDefinition.hasOtherOption()
                        && "otros".equalsIgnoreCase(label);
                option.setRequiresFreeText(requiresFreeText);

                optionAnswerRepository.save(option);
            });
        });

        log.info("Seeded Medica Leben survey with {} questions", definition.getQuestions().size());
    }

    private Survey createSurvey(SurveyDefinition definition) {
        Survey survey = new Survey();
        survey.setTitle(definition.getTitle());
        survey.setDescription(definition.getDescription());
        survey.setGuideType(parseGuideType(definition.getGuideType()));
        survey.setActive(true);
        return surveyRepository.save(survey);
    }

    private Survey.GuideType parseGuideType(String raw) {
        if (raw == null) {
            return Survey.GuideType.Personalizado;
        }
        try {
            return Survey.GuideType.valueOf(raw.replace("Guía", "").trim());
        } catch (IllegalArgumentException ex) {
            return Survey.GuideType.Personalizado;
        }
    }

    private void writeMetadata(Question question, Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        try {
            question.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException ex) {
            log.warn("Unable to serialize metadata for question {}", question.getText(), ex);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SurveyDefinition {
        private String code;
        private String title;
        private String description;
        private String guideType;
        private Integer totalQuestions;
        private List<QuestionDefinition> questions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QuestionDefinition {
        private Integer number;
        private String text;
        private String type;
        private String category;
        private boolean allowMultiple;
        private List<String> options;
        private Map<String, Object> metadata;

        boolean hasOtherOption() {
            return metadata != null && Boolean.TRUE.equals(metadata.get("otherOption"));
        }
    }
}
