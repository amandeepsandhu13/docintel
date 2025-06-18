package com.docintel.backend.util;
import com.docintel.backend.dto.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexRefiner {


    public static List<KeyValue> refineKeyValues(List<KeyValue> originalPairs, String fullContent) {
        List<KeyValue> refined = new ArrayList<>(originalPairs);

        // Example: extract email if not already found
        if (originalPairs.stream().noneMatch(kv -> kv.getKey().toLowerCase().contains("email"))) {
            Pattern emailPattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
            Matcher matcher = emailPattern.matcher(fullContent);
            if (matcher.find()) {
                refined.add(new KeyValue("Email (regex)", matcher.group()));
            }
        }

        // Add more patterns (phone, dates, amounts, etc.) here

        return refined;
    }

}
