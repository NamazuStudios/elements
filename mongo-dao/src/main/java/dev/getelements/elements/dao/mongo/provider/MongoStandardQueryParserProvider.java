package dev.getelements.elements.dao.mongo.provider;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoStandardQueryParserProvider implements Provider<StandardQueryParser> {

    @Inject
    private Provider<Analyzer> analyzerProvider;

    @Override
    public StandardQueryParser get() {

        final StandardQueryParser standardQueryParser = new StandardQueryParser();

        standardQueryParser.setAllowLeadingWildcard(false);
        standardQueryParser.setAnalyzer(analyzerProvider.get());

        return standardQueryParser;

    }

}
