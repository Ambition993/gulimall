package com.zhyf.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhyf.common.to.es.SkuEsModel;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.search.config.GulimallElasticSearchConfig;
import com.zhyf.gulimall.search.feign.ProductFeignService;
import com.zhyf.gulimall.search.service.MallSearchService;
import com.zhyf.gulimall.search.vo.AttrResponseVo;
import com.zhyf.gulimall.search.vo.SearchParam;
import com.zhyf.gulimall.search.vo.SearchResult;
import constant.EsConstant;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        //1 构建检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            // 2执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            // 3分析响应数据并封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        // 1返回所有的查询到商品
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
        // 2当前所有商品涉及到的属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attrsAgg");
        ParsedLongTerms attr_id = attr_agg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attr_id.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 得到属性ID
            Long attrId = bucket.getKeyAsNumber().longValue();
            // 属性name
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attrNameAgg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            // 属性值
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attrValueAgg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        // 3当前商品设计到的所有品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 品牌的id
            Long brandId = bucket.getKeyAsNumber().longValue();
            // 品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brandNameAgg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            // 品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brandImgAgg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        // 4当前商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalogAgg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        List<SearchResult.CatelogVo> catelogVos = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatelogVo catelogVo = new SearchResult.CatelogVo();
            // 得到分类ID
            String catalogId = bucket.getKeyAsString();
            catelogVo.setCatelogId(Long.parseLong(catalogId));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalogNameAgg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catelogVo.setCatelogName(catalogName);
            catelogVos.add(catelogVo);
        }
        result.setCatelogs(catelogVos);
        // 5 分页信息页码
        result.setPageNumber(param.getPageNum());
        // 6 总记录数 总页码
        long total = hits.getTotalHits().value;
        long totalPage = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotal(total);
        result.setTotalPages((int) totalPage);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        // 构建面包屑导航功能
        List<SearchResult.NavVo> navVos;
        List<String> attrs = param.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            navVos = attrs.stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // 分析每一个attrs传过来的数值
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                // 取消这个面包屑以后跳转到哪个地方
                // 请求地址的url 地址换掉
                // 拿到所有的查询条件 去掉当前
                //6.3 设置面包屑跳转链接
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
//            //
//            if (param.getBrandId() != null && param.getBrandId().size() > 0) {
//                List<SearchResult.NavVo> navs = result.getNavs();
//                SearchResult.NavVo navVo = new SearchResult.NavVo();
//                navVo.setNavName("品牌");
//                R r = productFeignService.brandInfos(param.getBrandId());
//                if (r.getCode() == 0) {
//                    List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
//                    });
//                    StringBuffer sb = new StringBuffer();
//                    String replace = "";
//                    for (BrandVo brandVo : brand) {
//                        sb.append(brandVo.getBrandName() + ";");
//                        replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
//                    }
//                    navVo.setNavValue(sb.toString());
//                    navVo.setLink("http://search.gulimall.com/list.html?" + replace);
//                }
//                navs.add(navVo);
//            }
        }
        //
        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

    /**
     * 构建检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // 构建DSL 语句
        /*
            1 模糊匹配 过滤 （按照属性 分类 品牌 价格区间 库存）
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 must 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        sourceBuilder.query(boolQuery);
        // 1.2 bool-filter  按照三级分类ID查询的
        if (null != param.getCatalog3Id()) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2 bool-filter 按照品牌ID查询
        if (null != param.getBrandId() && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2 bool-filter 按照所有指定属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                //attr = 1_5寸：8寸
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":"); //这个属性用的值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // 每一个都要生成 nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        // 1.2 bool-filter 按照库存是否有查询 0 无 1 有
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 1.2 bool-filter 价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                // 区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        // 把以前的所有条件都拿来封装
        sourceBuilder.query(boolQuery);
        //
        /*
          排序 分页 高亮
         */
        // 2.1 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        // 2.2 分页
        // from = (pageNum -1)*pageSize
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        /*
          聚合分析
         */
        //5.1 按照brand聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg").field("brandId");
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brandNameAgg").field("brandName");
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brandImgAgg").field("brandImg");
        brandAgg.subAggregation(brandNameAgg);
        brandAgg.subAggregation(brandImgAgg);
        sourceBuilder.aggregation(brandAgg);

        //5.2 按照catalog聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg").field("catalogId");
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalogNameAgg").field("catalogName");
        catalogAgg.subAggregation(catalogNameAgg);
        sourceBuilder.aggregation(catalogAgg);

        //5.3 按照attrs聚合
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrsAgg", "attrs");
        //按照attrId聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //按照attrId聚合之后再按照attrName和attrValue聚合
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName");
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue");
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);

        nestedAggregationBuilder.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(nestedAggregationBuilder);
        System.out.println("DSL:" + sourceBuilder.toString());
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
    }
}
