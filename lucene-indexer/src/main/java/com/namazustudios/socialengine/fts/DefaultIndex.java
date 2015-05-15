package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by patricktwohig on 5/14/15.
 */
public class DefaultIndex implements Index {

    private DocumentGenerator documentGenerator;

    private IndexWriter indexWriter;

    private IndexReader indexReader;

    @Override
    public void add(Object model) {
//        try {
//            final DocumentEntry<?,?> documentEntry = documentGenerator.generate(model);
//            indexWriter.addDocument(documentEntry.getDocument());
//        } catch (IOException ex) {
//            throw new SearchException(ex);
//        }
    }

    @Override
    public void delete(Object model) {

    }

//    @Override
//    public <IdentifierT, ClassT> Iterable<GeneratorDocumentEntry<IdentifierT, ClassT>> search(
//            Class<IdentifierT> identifierClass,
//            Class<ClassT> cls,
//            String lql) {
//        return null;
//    }
//
//    @Override
//    public <IdentifierT, ClassT> Iterable<GeneratorDocumentEntry<IdentifierT, ClassT>> search(
//            Class<IdentifierT> identifierClass,
//            Class<ClassT> cls,
//            String lql,
//            Query query) {
//        return null;
//    }

    public DocumentGenerator getDocumentGenerator() {
        return documentGenerator;
    }

    @Inject
    public void setDocumentGenerator(DocumentGenerator documentGenerator) {
        this.documentGenerator = documentGenerator;
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    @Inject
    public void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    public IndexReader getIndexReader() {
        return indexReader;
    }

    @Inject
    public void setIndexReader(IndexReader indexReader) {
        this.indexReader = indexReader;
    }

}
