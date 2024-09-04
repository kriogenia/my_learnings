// Copyright Vespa.ai. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package ai.vespa.example.album;

import com.yahoo.application.Application;
import com.yahoo.application.Networking;
import com.yahoo.application.container.Search;
import com.yahoo.component.ComponentSpecification;
import com.yahoo.component.chain.Chain;
import com.yahoo.metrics.simple.MetricReceiver;
import com.yahoo.prelude.query.CompositeItem;
import com.yahoo.prelude.query.Item;
import com.yahoo.prelude.query.OrItem;
import com.yahoo.prelude.query.WordItem;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.result.Hit;
import com.yahoo.search.searchchain.Execution;
import com.yahoo.search.searchchain.testutil.DocumentSourceSearcher;
import com.yahoo.search.yql.MinimalQueryInserter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Iterator;

import static java.net.URLEncoder.encode;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests - demonstrates:
 * <ol>
 *     <li>Build queries from YQL</li>
 *     <li>How to get the query tree, and evaluate it</li>
 *     <li>Use of tracing</li>
 *     <li>using a mock backend for hits</li>
 *     <li>Use of Application for setting up a full environment (chains) / how to build chains</li>
 *     <li>Simple use of config injection</li>
 * </ol>
 */
class MetalSearcherTest {

    private Query metalQuery;

    /**
     *
     */
    @BeforeEach
    void initQuery() {
        metalQuery = new Query("/search/?yql=" +
                encode("select * from sources * where artist contains \"hetfield\" and album contains\"master of puppets\"",
                        StandardCharsets.UTF_8));
        metalQuery.getTrace().setLevel(6);
    }


    @Test
    void testAddedOrTerm1() {
        var builder = new MetalNamesConfig.Builder();
        builder.metalWords(Arrays.asList("hetfield", "metallica", "pantera"));
        var config = new MetalNamesConfig(builder);

        var myChain = new Chain<>(new MinimalQueryInserter(),  // added to chain in this order
                new MetalSearcher(config, new MetricReceiver.MockReceiver()));
        var context = Execution.Context.createContextStub();
        var execution = new Execution(myChain, context);

        var result = execution.search(metalQuery);
        System.out.println(result.getContext(false).getTrace());

        assertAddedOrTerm(metalQuery.getModel().getQueryTree().getRoot());
    }

    @Test
    void testAddedOrTerm2() {
        try (var app = Application.fromApplicationPackage(
                FileSystems.getDefault().getPath("src/main/application"),
                Networking.disable))
        {
            var search = app.getJDisc("default").search();
            var result = search.process(ComponentSpecification.fromString("metalchain"), metalQuery);
            System.out.println(result.getContext(false).getTrace());

            assertAddedOrTerm(metalQuery.getModel().getQueryTree().getRoot());
        }
    }

    @Test
    void testWithMockBackendProducingHits() {
        var docSource = new DocumentSourceSearcher();
        var testQuery = new Query();
        testQuery.getTrace().setLevel(6);
        testQuery.getModel().getQueryTree().setRoot(new WordItem("drum","album"));

        var mockResult = new Result(testQuery);
        mockResult.hits().add(new Hit("hit:1", 0.9));
        mockResult.hits().add(new Hit("hit:2", 0.8));
        docSource.addResult(testQuery, mockResult);

        var myChain = new Chain<>(new MetalSearcher(), docSource);  // no config to MetalSearcher
        var context = Execution.Context.createContextStub();
        var execution = new Execution(myChain, context);

        var result = execution.search(testQuery);
        System.out.println(result.getContext(false).getTrace());

        assertEquals(2, result.hits().size());
    }

    private void assertAddedOrTerm(Item root) {
        // Assert that an OR term is added to the root, with album:metal as one of the or-terms:
        assertInstanceOf(OrItem.class, root);
        for (var iter = ((CompositeItem)root).getItemIterator(); iter.hasNext(); ) {
            var item = iter.next();
            if (item instanceof WordItem) {
                assertEquals("album:metal", item.toString());
            }
        }
    }

}
