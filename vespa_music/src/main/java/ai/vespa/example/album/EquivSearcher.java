// Copyright Vespa.ai. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package ai.vespa.example.album;

import com.yahoo.prelude.query.CompositeItem;
import com.yahoo.prelude.query.EquivItem;
import com.yahoo.prelude.query.Item;
import com.yahoo.prelude.query.PhraseItem;
import com.yahoo.prelude.query.TermItem;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.query.QueryTree;
import com.yahoo.search.searchchain.Execution;
import com.yahoo.yolean.chain.After;

import java.util.List;
import java.util.Map;

/**
 * A searcher doing recursive query rewriting - not currently added to any chain.
 * Add to services.xml to enable:
 *  &lt;searcher id="ai.vespa.example.album.EquivSearcher" bundle="albums"/&gt;
*/
@After("MinimalQueryInserter")
public class EquivSearcher extends Searcher {

    private final Map<String, List<String>> artistSpellings = Map.of(
            "metallica", List.of("metalica", "metallika"),
            "rammstein", List.of("ramstein", "raamstein"));

    @Override
    public Result search(Query query, Execution execution) {
        if (artistSpellings.isEmpty()) {
            return execution.search(query);
        }

        var tree = query.getModel().getQueryTree();
        var newRoot = equivize(tree.getRoot());
        tree.setRoot(newRoot);
        query.trace("Equivizing", true, 2);
        return execution.search(query);
    }

    private Item equivize(Item item) {
        if (item instanceof TermItem termItem) {
            var term  = termItem.stringValue();
            var index = termItem.getIndexName();
            if ("artist".equals(index) && artistSpellings.containsKey(term)) {
                return new EquivItem(item, artistSpellings.get(term));
            }
        }
        else if (item instanceof CompositeItem compositeItem) {
            for (var i = 0; i < compositeItem.getItemCount(); ++i)
                compositeItem.setItem(i, equivize(compositeItem.getItem(i)));
            return compositeItem;
        }

        return item;
    }

}
