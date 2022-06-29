package entity.email;

import system.evaluator.NaiveBayesEvaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Email {
    private List<Integer> weightVector = new ArrayList<Integer>();
    /**
     * Ground truth spam value of the email
     */
    private final boolean isSpam;

    public Email(boolean isSpam, List<Integer> weights) {
        this.weightVector = weights;
        this.isSpam = isSpam;
    }

    /**
     * Return the total amount of words in the email
     */
    public int getNumberOfWord() {
        int number = 0;

        for (int i = 0; i < weightVector.size(); i++) {
            number += weightVector.get(i);
        }

        return number;
    }

    /**
     * Get a particular word's weight in the email
     */
    public int getWeight(int index) {
        return weightVector.get(index);
    }

    /**
     * Get ground truth spam value of the email
     */
    public boolean isSpam() {
        return this.isSpam;
    }
}
