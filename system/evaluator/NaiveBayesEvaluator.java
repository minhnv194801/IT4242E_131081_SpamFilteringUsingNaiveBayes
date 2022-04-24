package system.evaluator;

import entity.email.Email;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NaiveBayesEvaluator {
    private static final String datasetPath = "emails.csv";

    private static final List<Email> spamEmailLst = new ArrayList<>();
    private static final List<Email> hamEmailLst = new ArrayList<>();

    private static final int n = 10;
    private static final int alpha = 1;

    private static final List<List<Email>> partitionLst = new ArrayList<>();

    private static double spamRate;
    private static double hamRate;

    private static int[] trainedSpamWeightVector;
    private static int[] trainedHamWeightVector;

    private static int numberOfWordInSpam;
    private static int numberOfWordInHam;

    private static final int[][] evaluationMatrix = new int[2][2];

    static  {
        String separator = ",";

        int i = 0;

        File file = new File(datasetPath);

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            for (String line : lines) {
                if (i == 0) {
                    i++;
                    continue;
                }

                int[] array = new int[3000];
                String[] tokens = line.split(separator);
                boolean isSpam = tokens[3001].equals("1");
                for (int j = 0; j < tokens.length; j++) {
                    if (j != 0 && j != 3001) {
                        try {
                            array[j - 1] = Integer.parseInt(tokens[j]);
                        } catch (ArithmeticException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Email tmpEmail = new Email(isSpam, array);
                if (isSpam) {
                    spamEmailLst.add(tmpEmail);
                } else {
                    hamEmailLst.add(tmpEmail);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void divideDataset() {
        for (int i = 0; i < n; i++) {
            partitionLst.add(new ArrayList<>());
        }
        Collections.shuffle(spamEmailLst);
        Collections.shuffle(hamEmailLst);

        for (int i = 0; i < spamEmailLst.size(); i++) {
            partitionLst.get(i % n).add(spamEmailLst.get(i));
        }
        for (int i = 0; i < hamEmailLst.size(); i++) {
            partitionLst.get(i % n).add(hamEmailLst.get(i));
        }
    }

    private static void train(List<Email> trainDataset) {
        numberOfWordInSpam = 0;
        numberOfWordInHam = 0;
        int numberOfSpam = 0;
        int numberOfHam = 0;
        trainedSpamWeightVector = new int[3000];
        trainedHamWeightVector = new int[3000];

        for (Email anEmail : trainDataset) {
            if (anEmail.isSpam()) {
                numberOfSpam += 1;
                numberOfWordInSpam += anEmail.getNumberOfWord();
                for (int i = 0; i < 3000; i++) {
                    trainedSpamWeightVector[i] += anEmail.getWeight(i);
                }
            } else {
                numberOfHam += 1;
                numberOfWordInHam += anEmail.getNumberOfWord();
                for (int i = 0; i < 3000; i++) {
                    trainedHamWeightVector[i] += anEmail.getWeight(i);
                }
            }
        }

        spamRate = (double) numberOfSpam / (double) (numberOfSpam + numberOfHam);
        hamRate = (double) numberOfHam / (double) (numberOfSpam + numberOfHam);
    }

    private static void test(List<Email> testDataset) {
        for (Email email : testDataset) {
            boolean isSpam;
            double spamScore = Math.log10(spamRate);
            double hamScore = Math.log10(hamRate);

            for (int i = 0; i < 3000; i++) {
                int occurs = email.getWeight(i);
                double pwspam = (double) (trainedSpamWeightVector[i] + alpha)/ (double) (numberOfWordInSpam + alpha * 3000);
                double pwham = (double) (trainedHamWeightVector[i] + alpha)/ (double) (numberOfWordInHam + alpha * 3000);
                spamScore += occurs * Math.log10(pwspam);
                hamScore += occurs * Math.log10(pwham);
            }

            isSpam = (spamScore > hamScore);

            evaluationMatrix[email.isSpam()?0:1][isSpam?0:1] += 1;
        }
    }

    public static void main(String[] args) {
        divideDataset();

        for (int i = 0; i < n; i++) {
            List<Email> trainDataset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    List<Email> tmpLst = partitionLst.get(j);
                    trainDataset.addAll(tmpLst);
                }
            }

            train(trainDataset);
            test(partitionLst.get(i));
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(evaluationMatrix[i][j] + " ");
            }
            System.out.println();
        }

        double spamPrecision = (double) evaluationMatrix[0][0] / (double) (evaluationMatrix[0][0] + evaluationMatrix[1][0]);
        double spamRecall = (double) evaluationMatrix[0][0] / (double) (evaluationMatrix[0][0] + evaluationMatrix[0][1]);
        double hamPrecision = (double) evaluationMatrix[1][1] / (double) (evaluationMatrix[1][1] + evaluationMatrix[0][1]);
        double hamRecall = (double) evaluationMatrix[1][1] / (double) (evaluationMatrix[1][1] + evaluationMatrix[1][0]);
        double macroPrecision = (spamPrecision + hamPrecision) / 2;
        double macroRecall = (spamRecall + hamRecall) / 2;

        System.out.println("Precision: " + macroPrecision);
        System.out.println("Recall: " + macroRecall);
        System.out.println("F1: " + ((2 * macroPrecision * macroRecall) / (macroPrecision + macroRecall)));
    }
}