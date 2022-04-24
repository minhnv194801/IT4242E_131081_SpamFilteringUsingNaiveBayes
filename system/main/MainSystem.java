package system.main;

import com.sun.tools.javac.Main;
import entity.email.Email;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MainSystem {
    private static MainSystem instance;

    private static double spamRate;
    private static double hamRate;
    private static int[] trainedSpamWeightVector = new int[3000];
    private static int[] trainedHamWeightVector = new int[3000];
    private static int numberOfWordInSpam = 0;
    private static int numberOfWordInHam = 0;
    private static int alpha = 1;
    private static int vocalbulary = 3000;

    static {
        try (BufferedReader br = new BufferedReader(new FileReader("trainedData.txt"))) {
            String line;
            line = br.readLine();
            String[] tokens = line.split(" ");
            spamRate = Double.parseDouble(tokens[0]);
            hamRate = Double.parseDouble(tokens[1]);
            for (int i = 0; i < vocalbulary; i++) {
                line = br.readLine();
                trainedSpamWeightVector[i] = Integer.parseInt(line);
                numberOfWordInSpam += trainedSpamWeightVector[i];
            }
            for (int i = 0; i < vocalbulary; i++) {
                line = br.readLine();
                trainedHamWeightVector[i] = Integer.parseInt(line);
                numberOfWordInHam += trainedHamWeightVector[i];
            }
        } catch (IOException e) {
            System.out.println("Error! Train data not found! Exit!");
            e.printStackTrace();
        }
    }

    public int predict(Email email) {
        boolean isSpam;
        double spamScore = Math.log10(spamRate);
        double hamScore = Math.log10(hamRate);

        for (int i = 0; i < 3000; i++) {
            int occurs = email.getWeight(i);
            double pwspam = (double) (trainedSpamWeightVector[i] + alpha)/ (double) (numberOfWordInSpam + alpha * vocalbulary);
            double pwham = (double) (trainedHamWeightVector[i] + alpha)/ (double) (numberOfWordInHam + alpha * vocalbulary);
            spamScore += occurs * Math.log10(pwspam);
            hamScore += occurs * Math.log10(pwham);
        }

        System.out.println("Spam score: " + spamScore + " vs ham score: " + hamScore);

        return (spamScore > hamScore)?1:0;
    }

    public int predict(String mailContents) {
        Email email = new Email(mailContents);
        return predict(email);
    }

    public static MainSystem getInstance() {
        if (instance == null) {
            instance = new MainSystem();
        }

        return instance;
    }
}
