package rocchio;

import javafx.util.Pair;
import org.tartarus.snowball.ext.turkishStemmer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by stj.eaydin on 9.07.2017.
 */
public class Prediction {

    //
    private Corpus corpus;

    //
    private ArrayList<ArrayList<Double>>documentTfIdfVector = new ArrayList<>();

    //
    private ArrayList<CosineSimilarity>cosineSimilarities = new ArrayList<>();

    private turkishStemmer stemmer = new turkishStemmer();


    public Prediction(String directoryName) throws IOException {

        // Create corpus object will calculate tf-idf vector for each document in constructor.
        corpus = new Corpus(directoryName);

        // Get document tf-idf vector from corpus.
        documentTfIdfVector = corpus.getDocumentsTfIdfVectors();

        createCosineSimilarityObjects();

        System.gc();

    }

    private void createCosineSimilarityObjects() {

        for (ArrayList<Double> documentVector : documentTfIdfVector)
            cosineSimilarities.add(new CosineSimilarity(null, documentVector));

    }


    public String getText(int index) {
        return corpus.getDocumentsText(index);
    }

    public List<Pair<Integer, Double>> predict(Integer index) {

        for (CosineSimilarity similarity : cosineSimilarities)
            similarity.setVector1(documentTfIdfVector.get(index));

        executeThreads();

        return getSmallestSublist(index,3);

    }

    private List<Pair<Integer, Double>> getSmallestSublist(Integer index, int sublistSize) {

        ArrayList<Pair<Integer,Double>>result = new ArrayList<>();

        for (int i = 0; i < cosineSimilarities.size(); ++i){
            if (i != index)
                result.add(new Pair<>(i, cosineSimilarities.get(i).getSimilarityAngle()));
        }

        result.sort(Comparator.comparingDouble(Pair::getValue));
        for (int i = 0; i < result.size(); ++i)
            if (Double.compare(result.get(i).getValue(),0.0) == 0)
                result.remove(i);

        return result.subList(0,sublistSize);

    }

    private void executeThreads() {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (CosineSimilarity cosineSimilarity : cosineSimilarities)
            executor.execute(cosineSimilarity);

        executor.shutdown();

        try {

            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (InterruptedException e) {

            System.out.println("Exception in predict method " + e);

        }
    }


    public void improveResult(Integer inputIndex, Integer similarIndex, List<Pair<Integer, Double>> results) {

        Document inputDocument   = corpus.getDocument(inputIndex);
        Document similarDocument = corpus.getDocument(similarIndex);

        String inputText    = inputDocument.getText();
        String similarText  = similarDocument.getText();

        List<String> intersectWords = getIntersectWords(inputText,similarText);
//        List<String> unionWords     = getUnionWords(inputText,similarText);

        Integer tempCount;
        for (String word:intersectWords){
            inputDocument.setWordCount(word,1);
            similarDocument.setWordCount(word,1);
        }

        inputDocument.calculateDocumentTFIDFVector();
        similarDocument.calculateDocumentTFIDFVector();

        // Update document tf-idf vector.
        documentTfIdfVector.set(inputIndex,inputDocument.getDocumentTfidfvector());
        documentTfIdfVector.set(similarIndex,similarDocument.getDocumentTfidfvector());

        // Update cosine similarities object.
        cosineSimilarities.get(inputIndex).setVector2(inputDocument.getDocumentTfidfvector());
        cosineSimilarities.get(similarIndex).setVector2(similarDocument.getDocumentTfidfvector());

    }

    private List<String> getUnionWords(String inputText, String similarText) {

        Set<String> tokenList = new HashSet<>();

        String[] inputTokens = inputText.split(" ");
        for (String token:inputTokens) {
            stemmer.setCurrent(token);
            if (stemmer.stem())
                tokenList.add(stemmer.getCurrent());
        }

        inputTokens = similarText.split(" ");
        for (String token:inputTokens){
            stemmer.setCurrent(token);
            if (stemmer.stem())
                tokenList.add(stemmer.getCurrent());
        }

        return new ArrayList<>(tokenList);
    }

    private List<String> getIntersectWords(String inputText, String similarText) {

        String[] inputTokens = inputText.split(" ");
        Set<String>inputTokenList = new HashSet<>();
        for (String inputToken : inputTokens) {
               stemmer.setCurrent(inputToken);
                if (stemmer.stem())
                    inputTokenList.add(stemmer.getCurrent());
        }

        String[] similarTokens = similarText.split(" ");
        Set<String>similarTokenList = new HashSet<>();
        for (String similarToken : similarTokens) {
            stemmer.setCurrent(similarToken);
            if (stemmer.stem())
                similarTokenList.add(stemmer.getCurrent());
        }

        inputTokenList.retainAll(similarTokenList);

        return new ArrayList<>(inputTokenList);
    }

    public void save(String saveFile) {

    }
}


