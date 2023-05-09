package dev.getelements.elements.dao.mongo.provider;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoStandardAnalyzerProvider implements Provider<Analyzer> {

    @Override
    public Analyzer get() {
        return new StandardAnalyzer();
    }

}
