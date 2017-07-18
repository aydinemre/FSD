package rocchio;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Corpus{

    private File corpusDirectory;

    private ArrayList<Document> documents;

    // Holds all words in all categories and documents.
    private ArrayList<String> corpusAllWordList;

    // Holds a word how many times passed in all documents.
    private HashMap<String,Integer> corpusAllWordCounts;

    // Holds a words how many times passed in different documents.
    private HashMap<String,Integer> corpusDifferentDocumentsCounts;

    private ArrayList<ArrayList<Double>> documentsTfIdfVectors;

    private String text;

    private ExecutorService executorService;

    /**
     * No parameter constructor.
     * @param dataSetDirectory
     */
    public Corpus(String dataSetDirectory) throws IOException {

        System.out.print("Init\t\t\t\t\t\t...\t");
        init(dataSetDirectory);
        System.out.println(" Successful");

        System.out.print("Create all documents\t\t... ");
        createAllDocuments();
        System.out.println(" Successful");

        System.out.print("Update corpus all word list\t... ");
        updateCorpusAllWordList();
        System.out.println(" Successful");

        System.out.print("Update corpus all word list ... ");
        updateCorpusAllWordCount();
        System.out.println(" Successful");

        System.out.println("Normalizing\t\t\t\t\t... ");
        normalize();
        System.out.println("Succesful");

        System.out.print("findHowManyTimesPassedInDifferentTextFile\t...");
        findHowManyTimesPassedInDifferentTextFile();
        System.out.println(" Successful");

        System.out.print("calculateVectorForEachDocument\t\t\t\t...");
        calculateVectorForEachDocument();
        System.out.println(" Successful");

        System.out.print("getAllDocumentsText\t\t\t\t\t\t\t...");
        getAllDocumentsText();
        System.out.println(" Successful");

    }

    /**
     * Check given directory name and initialize all private members.
     * @param dataSetDirectory directory which involve files.
     * @throws NotDirectoryException // If given directory name doesn't
     * exist or doesn't directory throws this exception
     */
    private void init(String dataSetDirectory) throws NotDirectoryException {

        // Check directory.
        corpusDirectory = new File(dataSetDirectory);
        if (!corpusDirectory.exists() || !corpusDirectory.isDirectory())
            throw new NotDirectoryException(dataSetDirectory);

        // Create a document array.
        documents = new ArrayList<>();

        // Create a list to hold all words.
        corpusAllWordList = new ArrayList<>();

        // Create a list to holds all words counts.
        corpusAllWordCounts = new HashMap<>();

        // Create a list to holds a word how many times passed in different documents ?
        corpusDifferentDocumentsCounts = new HashMap<>();

        // Create a list to holds all documents vectors.
        documentsTfIdfVectors = new ArrayList<>();

    }

    /**
     * We have a very big vector for each document.
     * We must ignore some words. Which words ?
     * In this method founds average count for all words.
     * Then choose a lower bound and upper bound.
     *
     */
    private void normalize() {

        System.out.println("Before normalization word vector length : " + corpusAllWordList.size());

        // Calculate mean of counts.
        Integer totalCount = 0,mean,low = 0;
        for (String word : corpusAllWordList){
            int temp = corpusAllWordCounts.get(word);
            if(temp > 1) {
                totalCount += temp;
                ++low;
            }
        }

        // MEAN !!!
        mean = totalCount / ( corpusAllWordCounts.size() - low );

        // Choose a lower and upper bound.
        // MAGIC NUMBER
        int lowerBound = mean / 2;
        int upperBound = mean * 6;

        System.out.println("Mean : " + mean + " lower bound : " + lowerBound + " upper bound : " + upperBound);

        // Apply a threshold with lower and upper bound.
        for (int i = 0; i < corpusAllWordList.size(); ++i){

            // If word count lower than lower bound or upper than upper bound remove from word list.
            int temp = corpusAllWordCounts.get(corpusAllWordList.get(i));
            if (temp <= lowerBound || temp >= upperBound) {
                corpusAllWordList.remove(i--);
            }
        }

        System.out.println("After normalization word vector length : " + corpusAllWordList.size());

    }

    /**
     * Create a document object for each file.
     * If directory is empty assertion fails.
     */
    private void createAllDocuments(){

        // Get list of files in directory.
        File[] files = corpusDirectory.listFiles(File::isFile);

        // Assertion.
        assert files != null;

        // Sort file names in numerical order
        Arrays.sort(files, (o1, o2) -> {
            int file1 = Integer.parseInt(o1.getName().substring(0,o1.getName().indexOf(".")));
            int file2 = Integer.parseInt(o2.getName().substring(0,o2.getName().indexOf(".")));
            return (file1-file2);
        });

        System.out.print("(" + files.length + " file founded in " + corpusDirectory.getName() + ") ");

        for (File file : files) {

            try {
                // Read file as byte array.
                byte[] encodedFile = Files.readAllBytes(Paths.get(file.getPath()));

                // Convert byte array to turkish string.
                text = new String(encodedFile, "ISO-8859-9");

                documents.add(new Document(text));

            } catch (IOException ignored) {}

        }

        startFun(); // execute thread pool

    }

    /**
     * Execute document thread.
     */
    private void startFun() {

        // Start thread pool
        executorService = Executors.newFixedThreadPool(10);
        for (Document document : documents)
            executorService.execute(document);

        // Shut down thread pool
        executorService.shutdown();

        // Wait for all thread.
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

    }

    private void getAllDocumentsText() {

        for (Document document : documents) {
            documentsTfIdfVectors.add(document.getDocumentTfidfvector());
        }
    }

    private void calculateVectorForEachDocument() {

        for (int i = 0; i < documents.size(); ++i){
            documents.get(i).setCorpusAllWordList( corpusAllWordList );
            documents.get(i).setCorpusDiffHash( corpusDifferentDocumentsCounts );
            documents.get(i).setCorpusTotalDocumentsCounts( documents.size() );
        }

       startFun();
    }

    /**
     * IDF: Inverse Document Frequency, which measures how important a term is.
     * While computing TF, all terms are considered equally important.
     * However it is known that certain terms, such as "is", "of", and "that",
     * may appear a lot of times but have little importance.
     * Thus we need to weigh down the frequent terms while scale up the rare ones, by computing the following:
     *
     * IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
     *
     * So we must know this : for each word how many different document involve this word ?
     * So we must have an other table.
     */
    private void findHowManyTimesPassedInDifferentTextFile() {

        // For each document do ...
        for (Document document : documents) {

            Integer globalCount;

            HashMap<String, Integer> documentWordTable = document.getDocumentWordTable();
            Iterator documentIterator = documentWordTable.entrySet().iterator();

            while (documentIterator.hasNext()) {

                Map.Entry documentEntryPair = (Map.Entry) documentIterator.next();

                globalCount = corpusDifferentDocumentsCounts.get(documentEntryPair.getKey());

                globalCount = globalCount == null ? 1 : ++globalCount;

                corpusDifferentDocumentsCounts.put((String) documentEntryPair.getKey(), globalCount);
            }
        }
    }

    /**
     * Get total count for each word.
     */
    private void updateCorpusAllWordCount() {

        // Iterate all documents
        for (Document document : documents) {

            Integer globalCount, localCount;

            // Get document word table.
            HashMap<String, Integer> documentWordTable = document.getDocumentWordTable();

            // Get document entry set iterator.
            Iterator iterator = documentWordTable.entrySet().iterator();

            // For each entry :
            while (iterator.hasNext()) {

                // Get entry
                Map.Entry pair = (Map.Entry) iterator.next();

                // Get word, global word table value and document word table value
                globalCount = corpusAllWordCounts.get(pair.getKey());
                localCount  = documentWordTable.get(pair.getKey());

                // Update global value
                globalCount = globalCount == null ? localCount : globalCount + localCount;

                // Update in global table.
                corpusAllWordCounts.put((String) pair.getKey(), globalCount);

            }
        }
    }

    /**
     * Documents created in previous step. Now lets collect results of them.
     */
    private void updateCorpusAllWordList() {

        Set<String> hashSet = new HashSet<>();

        // Get word list for each document and hold them in hash set.
        for (Document document : documents)
            hashSet.addAll(document.getDocumentWordList());

        // Update word list.
        corpusAllWordList.addAll(hashSet);

        // Sort word list(it is not necessary.)
        Collections.sort(corpusAllWordList);
    }

    private void copyTestFiles(File[] fileList) throws IOException {

        String targetDirectoryPath = "testFiles" + File.separator;

        for (double i = fileList.length * .8 ; i < fileList.length; ++i){
            try{
                Files.copy(fileList[(int) i].toPath(),new File(targetDirectoryPath + fileList[(int) i].getName()).toPath());
            }catch (FileAlreadyExistsException e){

            }
        }

    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public ArrayList<String> getCorpusAllWordList() {
        return corpusAllWordList;
    }

    public HashMap<String, Integer> getCorpusAllWordCounts() {
        return corpusAllWordCounts;
    }

    public HashMap<String, Integer> getCorpusDifferentDocumentsCounts() {
        return corpusDifferentDocumentsCounts;
    }

    public ArrayList<ArrayList<Double>> getDocumentsTfIdfVectors() {
        return documentsTfIdfVectors;
    }

    public String getDocumentsText(int index) {
        return documents.get(index).getText();
    }

    public Document getDocument(int index){ return documents.get(index);}

}
