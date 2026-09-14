package matchingService.matchingService.matching;


import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TfIdfScorer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
            "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
            "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't",
            "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor",
            "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out",
            "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some",
            "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
            "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to",
            "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
            "your", "yours", "yourself", "yourselves",
            // Common resume noise words that dilute scoring
            "com", "gmail", "github", "linkedin", "phone", "email", "curriculum", "vitae", "resume",
            "experience", "projects", "education", "summary", "skills", "year", "years", "using", "work"
    );

    public record MatchResult(double scorePercentage, List<String> topKeywords) {}

    public MatchResult calculateScore(String docJob, String docResume) {
        if (docJob == null || docResume == null || docJob.isBlank() || docResume.isBlank()) {
            return new MatchResult(0.0, Collections.emptyList());
        }

        List<String> jobTokens = tokenize(docJob);
        List<String> resumeTokens = tokenize(docResume);

        if (jobTokens.isEmpty() || resumeTokens.isEmpty()) {
            return new MatchResult(0.0, Collections.emptyList());
        }

        Set<String> uniqueJobTerms = new LinkedHashSet<>(jobTokens);
        Set<String> uniqueResumeTerms = new HashSet<>(resumeTokens);

        // 1. Direct Job Skill Coverage (% of job requirements satisfied by resume)
        List<String> matchedTerms = new ArrayList<>();
        for (String term : uniqueJobTerms) {
            if (uniqueResumeTerms.contains(term)) {
                matchedTerms.add(term);
            }
        }

        double coverageRatio = (double) matchedTerms.size() / (double) uniqueJobTerms.size();
        double skillCoverageScore = coverageRatio * 100.0;

        // 2. Sub-linear TF-IDF (Log frequency dampening: 1 + ln(count))
        // This stops long resumes from being destroyed by length normalization
        Map<String, Double> jobTf = computeLogTf(jobTokens);
        Map<String, Double> resumeTf = computeLogTf(resumeTokens);

        double dotProduct = 0.0;
        double normJob = 0.0;
        double normResume = 0.0;

        for (String term : uniqueJobTerms) {
            double v1 = jobTf.getOrDefault(term, 0.0);
            double v2 = resumeTf.getOrDefault(term, 0.0);
            dotProduct += (v1 * v2);
            normJob += (v1 * v1);
        }

        // Only compute resume norm over terms relevant to the job domain to eliminate resume length penalty
        for (String term : uniqueJobTerms) {
            double v2 = resumeTf.getOrDefault(term, 0.0);
            normResume += (v2 * v2);
        }

        double cosineSim = 0.0;
        if (normJob > 0.0 && normResume > 0.0) {
            cosineSim = dotProduct / (Math.sqrt(normJob) * Math.sqrt(normResume));
        }

        double cosineScore = Math.min(100.0, cosineSim * 100.0);

        // 3. Blended Score: 70% skill coverage + 30% frequency relevance
        double finalScore = (0.70 * skillCoverageScore) + (0.30 * cosineScore);
        double roundedScore = Math.round(finalScore * 100.0) / 100.0;

        // Sort matched keywords by frequency in job & resume
        matchedTerms.sort((t1, t2) -> {
            double score2 = jobTf.getOrDefault(t2, 0.0) + resumeTf.getOrDefault(t2, 0.0);
            double score1 = jobTf.getOrDefault(t1, 0.0) + resumeTf.getOrDefault(t1, 0.0);
            return Double.compare(score2, score1);
        });

        List<String> topKeywords = matchedTerms.stream().limit(8).collect(Collectors.toList());

        return new MatchResult(roundedScore, topKeywords);
    }

    private List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();

        // Normalize compound terms & technical symbols before splitting
        String normalized = text.toLowerCase()
                .replaceAll("c\\+\\+", "cpp")
                .replaceAll("c#", "csharp")
                .replaceAll("spring\\s*boot", "springboot")
                .replaceAll("multi[-\\s]?threading", "multithreading")
                .replaceAll("data\\s*structures?", "datastructures")
                .replaceAll("react(\\.js|js)?", "react")
                .replaceAll("node(\\.js|js)?", "nodejs")
                .replaceAll("[^a-zA-Z0-9\\s]", " ");

        String[] rawTokens = normalized.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 1 && !STOP_WORDS.contains(trimmed)) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    private Map<String, Double> computeLogTf(List<String> tokens) {
        Map<String, Double> counts = new HashMap<>();
        for (String token : tokens) {
            counts.put(token, counts.getOrDefault(token, 0.0) + 1.0);
        }

        Map<String, Double> logTf = new HashMap<>();
        for (Map.Entry<String, Double> entry : counts.entrySet()) {
            // Sub-linear scaling: 1 + ln(count)
            logTf.put(entry.getKey(), 1.0 + Math.log(entry.getValue()));
        }
        return logTf;
    }
}