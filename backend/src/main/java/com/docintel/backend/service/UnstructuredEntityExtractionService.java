package com.docintel.backend.service;

import com.docintel.backend.dto.ExtractedEntities;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UnstructuredEntityExtractionService {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4})\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b([A-Z][a-z]+\\s[A-Z][a-z]+)\\b");

    public ExtractedEntities extractEntities(String text) {
        ExtractedEntities entities = new ExtractedEntities();
        entities.setDates(extractMatches(DATE_PATTERN, text));
        entities.setEmails(extractMatches(EMAIL_PATTERN, text));
        entities.setNames(extractMatches(NAME_PATTERN, text));
        return entities;
    }

    private List<String> extractMatches(Pattern pattern, String text) {
        List<String> results = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            results.add(matcher.group().trim());
        }
        return results;
    }
}
