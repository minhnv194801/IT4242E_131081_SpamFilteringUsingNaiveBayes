package system.evaluator;

import entity.email.Email;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class NaiveBayesEvaluator {
    /**
     * Path to the dataset file
     */
    private static final String datasetPath = "emails.csv";
    /**
     * Path to the file store the list of stopwords to ignore
     */
    private static final String stopwordsPath = "stopwords.txt";
    /**
     * Internal keywords list fetch from the dataset
     */
    private static final List<String> keywordLst = new ArrayList<String>();
    /**
     * Internal stopwords list fetch from the stopwords file
     */
    private static final List<String> stopwordLst = new ArrayList<String>();
    /**
     * Map the keyword to index of the dataset for easier management
     */
    private static final Map<String, Integer> indexMap = new HashMap<String, Integer>();
    /**
     * List of the index that we actually care about
     */
    private static final List<Integer> indexLst = new ArrayList<Integer>();

    /**
     * List of spam email we got from trainset
     */
    private static final List<Email> spamEmailLst = new ArrayList<>();
    /**
     * List of normal email we got from trainset
     */
    private static final List<Email> hamEmailLst = new ArrayList<>();
    /**
     * The number of words in all spam mails of the trainset
     */
    private static int numberOfWordInSpam;
    /**
     * The number of words in all normal mails of the trainset
     */
    private static int numberOfWordInHam;

    /**
     * n variable for k-fold validation
     */
    private static final int n = 10;
    /**
     * alpha variable for Naive Bayes
     */
    private static final int alpha = 1;

    /**
     * The k list of email
     */
    private static final List<List<Email>> partitionLst = new ArrayList<>();

    /**
     * Trained spam email rate
     */
    private static double spamRate;
    /**
     * Trained normal email rate
     */
    private static double hamRate;
    /**
     * The list of trained value for spam email (P(wi|Spam))
     */
    public static List<Double> trainedSpamWeightVector = new ArrayList<Double>();
    /**
     * The list of trained value for spam email (P(wi|Ham))
     */
    public static List<Double> trainedHamWeightVector = new ArrayList<Double>();

    /**
     * The result matrix on testset
     */
    private static final int[][] evaluationMatrix = new int[2][2];

    static  {
        String separator = ",";

        File stopwordFile = new File(stopwordsPath);
        try {
            List<String> lines = Files.readAllLines(stopwordFile.toPath(), StandardCharsets.UTF_8);
            for (String line: lines) {
                stopwordLst.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(datasetPath);
        int i = 0;
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            for (String line : lines) {
                // Specially handle the first line of the dataset
                if (i == 0) {
                    i++;
                    String[] tokens = line.split(separator);
                    // Read all keywords from dataset
                    for (int j = 0; j < tokens.length; j++) {
                        if (j != 0 && j != 3001) {
                            keywordLst.add(tokens[j]);
                            indexMap.put(tokens[j], j);
                        }
                    }

                    // Remove all stopword from keyword list
                    keywordLst.removeAll(stopwordLst);
                    for (String kw: keywordLst) {
                        // Add keyword index into list of notable index
                        indexLst.add(indexMap.get(kw));
                    }
                    continue;
                }

                // Handle all the normal lines of the dataset
                List<Integer> array = new ArrayList<Integer>();
                String[] tokens = line.split(separator);
                boolean isSpam = tokens[3001].equals("1");
                for (int j = 0; j < tokens.length; j++) {
                    if (j != 0 && j != 3001) {
                        try {
                            // If index is notable then add the value into email data
                            if (indexLst.contains(j)) {
                                array.add(Integer.parseInt(tokens[j]));
                            }
                        } catch (ArithmeticException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Create an instance of the mail
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

    /**
     * Perform stratified sampling for n folds
     */
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
        // Reset the previous trained data
        numberOfWordInSpam = 0;
        numberOfWordInHam = 0;
        int numberOfSpam = 0;
        int numberOfHam = 0;
        trainedSpamWeightVector = new ArrayList<Double>();
        trainedHamWeightVector = new ArrayList<Double>();

        for (Email anEmail : trainDataset) {
            if (anEmail.isSpam()) {
                numberOfSpam += 1;
                numberOfWordInSpam += anEmail.getNumberOfWord();
                for (int i = 0; i < keywordLst.size(); i++) {
                    if (i >= trainedSpamWeightVector.size()) {
                        trainedSpamWeightVector.add(0.0);
                    }
                    // Calculating N(wi|Spam)
                    trainedSpamWeightVector.set(i, trainedSpamWeightVector.get(i) + anEmail.getWeight(i));
                }
            } else {
                numberOfHam += 1;
                numberOfWordInHam += anEmail.getNumberOfWord();
                for (int i = 0; i < keywordLst.size(); i++) {
                    if (i >= trainedHamWeightVector.size()) {
                        trainedHamWeightVector.add(0.0);
                    }
                    // Calculating N(wi|Ham)
                    trainedHamWeightVector.set(i, trainedHamWeightVector.get(i) + anEmail.getWeight(i));
                }
            }
        }

        // P(Spam) and P(Ham)
        spamRate = (double) numberOfSpam / (double) (numberOfSpam + numberOfHam);
        hamRate = (double) numberOfHam / (double) (numberOfSpam + numberOfHam);

        for (int i = 0; i < trainedSpamWeightVector.size(); i++) {
            // P(Wi|Spam)
            trainedSpamWeightVector.set(i, (trainedSpamWeightVector.get(i) + alpha) / (double) (numberOfWordInSpam + alpha * keywordLst.size()));
            // P(Wi|Ham)
            trainedHamWeightVector.set(i, (trainedHamWeightVector.get(i) + alpha) / (double) (numberOfWordInHam + alpha * keywordLst.size()));
        }
    }

    private static void test(List<Email> testDataset) {
        for (Email email : testDataset) {
            boolean isSpam;
            // P(Spam|Content) = P(Spam)
            double spamScore = Math.log10(spamRate);
            // P(Ham|Content) = P(Ham)
            double hamScore = Math.log10(hamRate);

            // for i = 0 to n P(Spam|Content) += P(Wi|Spam)
            // for i = 0 to n P(Ham|Content) += P(Wi|Ham)
            for (int i = 0; i < keywordLst.size(); i++) {
                // Calculate the sum of all P(Wi|Spam) and P(Wi|Ham)
                int occurs = email.getWeight(i);
                spamScore += occurs * Math.log10(trainedSpamWeightVector.get(i));
                hamScore += occurs * Math.log10(trainedHamWeightVector.get(i));
            }

            // Comparing P(Spam|Content) and P(Ham|Content)
            isSpam = (spamScore > hamScore);

            // Add the test result to evaluation matrix
            evaluationMatrix[email.isSpam()?0:1][isSpam?0:1] += 1;
        }
    }

    public static void main(String[] args) {
        // Divide dataset into n folds
        divideDataset();

        // Use 1 fold as testset and the other as trainset
        for (int i = 0; i < n; i++) {
            List<Email> trainDataset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    List<Email> tmpLst = partitionLst.get(j);
                    trainDataset.addAll(tmpLst);
                }
            }
            // Train the model using the other fold as trainset
            train(trainDataset);
            // Test the trained model using the i-th fold
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