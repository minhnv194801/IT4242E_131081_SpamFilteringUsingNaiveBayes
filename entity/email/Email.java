package entity.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Email {
    private static final List<String> keywordLst = new ArrayList<>();
    private final int[] weightVector = new int[3000];
    private final boolean isSpam;

    private static final File keywordFile = new File("emailKeywords.txt");

    static {
        try (BufferedReader br = new BufferedReader(new FileReader(keywordFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                keywordLst.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Email(boolean isSpam, int... weights) {
        for (int i = 0; i < weights.length; i++) {
            weightVector[i] = weights[i];
        }

        this.isSpam = isSpam;
    }

    public Email(String mailContents) {
        isSpam = false;
        mailContents = mailContents.replaceAll("\n", " ");
        String[] tokens = mailContents.split(" ");
        for (String keyword : tokens) {
            keyword = keyword.replaceAll("[^a-zA-Z0-9]", "");
            keyword = keyword.toLowerCase();
            int index = keywordLst.indexOf(keyword);
            if (index != -1) {
                weightVector[index]++;
                System.out.println(keyword);
            }
        }
    }

    public int getNumberOfWord() {
        int number = 0;

        for (int i = 0; i < 3000; i++) {
            number += weightVector[i];
        }

        return number;
    }

    public int getWeight(int index) {
        return weightVector[index];
    }

    public boolean isSpam() {
        return this.isSpam;
    }
}
