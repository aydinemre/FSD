# FSD
Find Similar Documents using tfidf vectors

TF-IDF (Term Frequency-Inverse Document Frequency) is a text mining technique used to categorize documents. So i create basic Java Program. This program takes a folder which holds many documents and then calculates TFIDF vector for each document.    

# How To Calculate TFIDF Vector ?  

TF-IDF computes a weight which represents the importance of a term inside a document. It does this by comparing the frequency of usage inside an individual document as opposed to the entire data set (a collection of documents).  

The importance increases proportionally to the number of times a word appears in the individual document itself--this is called Term Frequency. However, if multiple documents contain the same word many times then you run into a problem. That's why TF-IDF also offsets this value by the frequency of the term in the entire document set, a value called Inverse Document Frequency.  

TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document)
IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
Value = TF * IDF  

TF-IDF is computed for each term in each document. Typically, you will be interested either in one term in particular (like a search engine), or you would be interested in the terms with the highest TF-IDF in a specific document (such as generating tags for blog posts).

### Usage

TODO::