package rocchio;

import java.util.ArrayList;

/**
 * Created by stj.eaydin on 9.07.2017.
 */
public class CosineSimilarity implements Runnable {

    private Double similarityAngle;

    private ArrayList<Double> vector1;
    private ArrayList<Double> vector2;

    CosineSimilarity(ArrayList<Double> v1, ArrayList<Double> v2) {

        vector1 = v1;

        vector2 = v2;

    }

    @Override
    public void run() {

        Double total = 0.0;
        Double length1 = 0.0, length2 = 0.0;

        for (int i = 0; i < vector2.size(); ++i) {
            total += (vector1.get(i) * vector2.get(i));
        }

        for (int i = 0; i < vector2.size(); ++i) {
            length1 += Math.pow(vector1.get(i), 2);
            length2 += Math.pow(vector2.get(i), 2);
        }

        length1 = Math.sqrt(length1);
        length2 = Math.sqrt(length2);


        Double result = total / (length1 * length2);

        similarityAngle = Math.toDegrees(Math.acos(result));

    }

    public Double getSimilarityAngle() {
        return similarityAngle;
    }

    public void setVector1(ArrayList<Double> vector1) {
        this.vector1 = vector1;
    }

    public void setVector2(ArrayList<Double> vector2) {
        this.vector2 = vector2;
    }
}