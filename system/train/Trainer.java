package system.train;

import entity.email.Email;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Train the machine learning system by read the dataset and then export it into 'trainedData.txt'.
 */
public class Trainer {
    private static File datasetFile = new File("emails.csv");

    public static void main(String[] args) {
        File file = new File("trainedData.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("File already existed");
        }

        try {
            int total = 0;
            int spamNum = 0;
            int hamNum = 0;
            int[] spamWeightVector = new int[3000];
            int[] hamWeightVector = new int[3000];
            FileWriter fileWriter = new FileWriter(file);

            String separator = ",";

            int i = 0;

            List<String> lines = Files.readAllLines(datasetFile.toPath(), StandardCharsets.UTF_8);

            for (String line : lines) {
                if (i == 0) {
                    i++;
                    continue;
                }

                String[] tokens = line.split(separator);
                boolean isSpam = tokens[3001].equals("1");
                for (int j = 0; j < tokens.length; j++) {
                    if (j != 0 && j != 3001) {
                        try {
                            if (isSpam) {
                                spamWeightVector[j - 1] += Integer.parseInt(tokens[j]);
                            } else {
                                hamWeightVector[j - 1] += Integer.parseInt(tokens[j]);
                            }
                        } catch (ArithmeticException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (isSpam) {
                    spamNum++;
                    total++;
                } else {
                    hamNum++;
                    total++;
                }
            }

            double spamRate = (double) spamNum / (double) total;

            fileWriter.write(String.valueOf(spamRate) + " " + String.valueOf(1 - spamRate) + "\n");
            for (int weight : spamWeightVector) {
                fileWriter.write(String.valueOf(weight) + "\n");
            }
            for (int weight : hamWeightVector) {
                fileWriter.write(String.valueOf(weight) + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}