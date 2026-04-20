package com.example.aipet;
public class AnalyzeResponse {
    public String species;
    public String breed;
    public double confidence;
    public MatchInfo match_info;

    public static class MatchInfo {
        public boolean match;
        public String name;
        public double similarity;
        public String breed;
        public String species;
    }
}