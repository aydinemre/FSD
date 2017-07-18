package rocchio;

import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Scanner object to read input from user.
    private static Scanner scanner = new Scanner(System.in);

    // Prediction object.
    private static Prediction prediction;

    // Data set directory which has include files.
    private static final String dataSetDirectory = "testFiles" + File.separator;

    private static final String saveFile = "save_file" + File.separator;


    public static void main(String[] args) throws IOException {

        // Crete a new prediction object.
        prediction = new Prediction(dataSetDirectory);

        interactiveTest();

        prediction.save(saveFile);
    }

    private static void interactiveTest() {

        // Exit status.
        int status;

        String input;
        Integer inputIndex, resultIndex,similarIndex;
        Double similarityResult;
        List<Pair<Integer, Double>> result;

        do {

            System.out.print("Enter file index { Press 'e' or 'E' to exit }  :  ");

            input = scanner.nextLine();

            status = checkInput(input);

            if (status != -1) {

                inputIndex = Integer.parseInt(input);

                System.out.println("Chosen text : " + System.lineSeparator() + prediction.getText(inputIndex) + System.lineSeparator() + System.lineSeparator());

                result = prediction.predict(inputIndex);

                for (int i = 0; i < result.size(); i++) {
                    System.out.println("----------------------" + i + "---------------------------------");
                    Pair<Integer, Double> aResult = result.get(i);
                    resultIndex = aResult.getKey();
                    similarityResult = aResult.getValue();
                    System.out.println(resultIndex + "-" + similarityResult + System.lineSeparator() + prediction.getText(resultIndex) + System.lineSeparator() + System.lineSeparator());
                }

                // TODO:: Check input
                do{
                    System.out.println("Choose the best answer : ");
                    input = scanner.nextLine();
                    similarIndex = Integer.parseInt(input);
                }while (similarIndex < 0 ||similarIndex >= result.size());

                prediction.improveResult(inputIndex,similarIndex,result);

            }

        } while (status != -1);
    }


    private static int checkInput(String line) {

        if (line.toLowerCase().equals("e"))
            return -1;

        try {
            Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return 0;
        }

        return 1;
    }
//    private static void autoTest(int n) {
//
//        Integer resultIndex;
//        ArrayList<Integer>result;
//        Double minResult = -1.0;
//        for ( int i = 0; i < n ; ++i){
//
//            System.out.println( "------------------------------------------------------------------------------------");
//
//            //System.out.println("Chosen text : " + i + System.lineSeparator() + prediction.getText(i));
//
//            result = prediction.predict(i);
//
//            if (result.size() != 0) {
//
//                System.out.println(i + " " + prediction+prediction.getSimilarityScore());
//
//
//                //System.out.println("Predict result : " + resultIndex + " Score : " + prediction.getSimilarityScore() + " text : " + System.lineSeparator() + prediction.getText(resultIndex));
//
//                //System.out.println("Summary words : " + prediction.getIntersectWords(i, resultIndex));
//
//            }else
//                System.out.println("Predict result :" + i + ": -1");
//        }
//
//    }


}