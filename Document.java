package rocchio;

import org.tartarus.snowball.ext.turkishStemmer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *  This class created to hold tf-idf vector for each nlphw2.Document.
 *  This class can load or save tfidf vector.
 *
 */
public class Document implements Runnable {

    private turkishStemmer stemmer = new turkishStemmer();

    // Word table holds : each word how many times passed ?
    private HashMap<String,Integer> documentWordTable;

    // Word list holds all words in document.
    private ArrayList<String> documentWordList;

    // nlphw2.Corpus vector will calculate with all words in all documents.
    private ArrayList<Double> documentTfidfvector;

    // Most seen value in this document.
    private Integer documentMostSeenValue;

    private String documentText;

    private Integer mode = 0;

    //
    private List<String> corpusAllWordList;
    private HashMap<String, Integer> corpusDiffHash;
    private int corpusTotalDocumentsCounts;

    /**
     * Constructor takes document text and holds it in private member.
     * @param text document text.
     */
    public Document(String text){

        documentText = text;

        documentWordTable = new HashMap<>();

        documentWordList = new ArrayList<>();

        documentTfidfvector = new ArrayList<>();

        documentMostSeenValue = 0;

    }

    /**
     * Takes file name and counts all words in given file.
     * @return false in an error. Otherwise return true.
     */
    public Boolean countAllWords() {

        // Replace all tabs and new lines to space and split by space.
        documentText = documentText.replaceAll("[#%:?@\\[\\]^,\\-()*&$'+.\"]"," ").replaceAll("[\t\n\r]+"," ");
        String[] words = documentText.split(" ");

        // Word counter.
        Integer wordCount;

        // For each word do ...
        for (int i = 0; i < words.length; ++i) {

            // If word is space or null character continue.
            if (!checkWords(words[i]))
                continue;

            // Get first five character of word.
//            if (words[i].length() > 6)
//                words[i] = words[i].substring(0, 6);

            stemmer.setCurrent(words[i]);
            if (stemmer.stem())
                words[i] = stemmer.getCurrent();

            // Found how many times passed this word.
            wordCount = documentWordTable.get(words[i]);

            // if word count is null assing its count to 1.
            wordCount = wordCount == null ? 1 : ++wordCount;

            // Update word table.
            documentWordTable.put(words[i],wordCount);
        }

        // Add all founded word to word list.
        documentWordList.addAll(documentWordTable.keySet());

        // Sort word list.
        Collections.sort(documentWordList);

        // Update most seen value.
        updateMostSeenValue();

        return true;
    }

    private boolean checkWords(String word) {

        if ( word.equals(" ") || word.equals("") || word.length() <= 1)
            return false;

        return word.matches(".*[a-zA-Z]+.*");

    }

    /**
     * Private method.
     * This method traverse all table and gets max
     * seen value.And assign it to private member.
     */
    private void updateMostSeenValue() {

        documentMostSeenValue = 0;

        for (Integer value: documentWordTable.values())
            if ( value > documentMostSeenValue )
                documentMostSeenValue = value;

    }

    // Getter for word list.
    public ArrayList<String> getDocumentWordList() {
        return documentWordList;
    }

    public ArrayList<Double> calculateDocumentTFIDFVector(){

        Integer wordCount;
        Double termFrequency, inverseDocumentFrequency;
        String word;
        documentTfidfvector.clear();
        for (String item : corpusAllWordList) {

            // Holds word in temporary.
            word = item;

            // Gets this word count.
            wordCount = documentWordTable.get(word);

            // If word count not equal to null calculate term frequency and inverse document frequency.
            if (wordCount != null) {

                termFrequency = (double) documentWordTable.get(word) / (double) documentMostSeenValue;
                inverseDocumentFrequency = Math.log((double) corpusTotalDocumentsCounts / (double) corpusDiffHash.get(word));
                documentTfidfvector.add(termFrequency * inverseDocumentFrequency);

            } else {

                documentTfidfvector.add(0.0);

            }

        }

        // Return tf idf vector clone.
        return new ArrayList<Double>(documentTfidfvector);

    }
    // Getter for word table
    public HashMap<String, Integer> getDocumentWordTable() {
        return documentWordTable;
    }


    public ArrayList<Double> getDocumentTfidfvector() {
        return documentTfidfvector;
    }

    public String getText() {
        return documentText;
    }

    @Override
    public void run() {

        if (mode == 0) {
            countAllWords();
            mode = 1;
        }
        else
            calculateDocumentTFIDFVector();


    }

    public void setCorpusAllWordList(List<String> corpusAllWordList) {
        this.corpusAllWordList = corpusAllWordList;
    }

    public List<String> getCorpusAllWordList() {
        return corpusAllWordList;
    }

    public void setCorpusDiffHash(HashMap<String, Integer> corpusDiffHash) {
        this.corpusDiffHash = corpusDiffHash;
    }

    public HashMap<String, Integer> getCorpusDiffHash() {
        return corpusDiffHash;
    }

    public void setCorpusTotalDocumentsCounts(int corpusTotalDocumentsCounts) {
        this.corpusTotalDocumentsCounts = corpusTotalDocumentsCounts;
    }

    public void setWordCount(String word, int value) {

        if (documentWordTable.containsKey(word))
            documentWordTable.put(word,documentWordTable.get(word) + value);
        else
            documentWordTable.put(word,value);
    }
}
