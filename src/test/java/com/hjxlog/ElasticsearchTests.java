package com.hjxlog;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.hjxlog.domain.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Huang JX
 * @Date: 2022/02/11
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticsearchTests {

    @Resource
    public RestHighLevelClient restHighLevelClient;

    @Test
    public void testCreateIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("es_index");
        // ????????????????????????????????? @RunWith(SpringRunner.class)
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());// ????????????????????????
        System.out.println(response);// ??????????????????
        restHighLevelClient.close();
    }

    // ??????????????????
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("es_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // ??????????????????
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("es_index");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }


    // ??????????????????
    @Test
    public void testAddDocment() throws IOException {
        // ????????????
        User user = new User("es", 18);
        // ????????????
        IndexRequest request = new IndexRequest("es_index");
        //?????? put/es_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        // ????????????????????? json
        request.source(JSONUtil.toJsonStr(user), XContentType.JSON);

        // ??????????????????????????????????????????
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    // ????????????????????????????????? get /index/_doc/1
    @Test
    public void testIsExistsDoc() throws IOException {
        GetRequest getRequest = new GetRequest("es_index", "1");

        // ??????????????????_source????????????
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);

    }

    // ??????????????????
    @Test
    public void testIsGetDoc() throws IOException {
        GetRequest getRequest = new GetRequest("es_index", "1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString()); // ??????????????????
        System.out.println(getResponse);
    }

    // ????????????????????????
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("es_index", "1");
        User user = new User("hjx", 20);
        request.doc(JSONUtil.toJsonStr(user), XContentType.JSON);

        UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status()); // OK
        restHighLevelClient.close();
    }

    // ??????????????????
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("es_index", "1");
        request.timeout("1s");
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());// OK
    }

    // ????????????????????????????????? ??????????????????
    @Test
    public void testBulk() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("hjx-1", 1));
        users.add(new User("hjx-2", 2));
        users.add(new User("hjx-3", 3));
        users.add(new User("hjx-4", 4));
        users.add(new User("hjx-5", 5));
        users.add(new User("hjx-6", 6));
        // ??????????????????
        for (int i = 0; i < users.size(); i++) {
            bulkRequest.add(
                    // ?????????????????????
                    new IndexRequest("es_index")
                            .id("" + (i + 1)) // ????????????id ???????????????????????????id
                            .source(JSONUtil.toJsonStr(users.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.status());// ok
    }

    // ??????
    // SearchRequest ????????????
    // SearchSourceBuilder ????????????
    // HighlightBuilder ??????
    // TermQueryBuilder ????????????
    // MatchAllQueryBuilder
    // xxxQueryBuilder ...
    @Test
    public void testSearch() throws IOException {
        // 1.????????????????????????
        SearchRequest searchRequest = new SearchRequest("es_index");
        // 2.??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // (1)???????????? ??????QueryBuilders???????????????
        // ????????????
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "hjx");
        //        // ????????????
        //        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        // (2)??????<????????????>?????????????????? SearchSourceBuilder ??????????????????
        // ????????????
        searchSourceBuilder.highlighter(new HighlightBuilder());
        //        // ??????
        //        searchSourceBuilder.from();
        //        searchSourceBuilder.size();
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // (3)????????????
        searchSourceBuilder.query(termQueryBuilder);
        // 3.?????????????????????
        searchRequest.source(searchSourceBuilder);
        // 4.?????????????????????
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 5.??????????????????
        SearchHits hits = search.getHits();
        System.out.println(JSONUtil.toJsonStr(hits));
        System.out.println("=======================");
        for (SearchHit documentFields : hits.getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

}
