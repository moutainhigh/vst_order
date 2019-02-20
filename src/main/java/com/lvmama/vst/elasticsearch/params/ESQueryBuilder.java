package com.lvmama.vst.elasticsearch.params;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;

public class ESQueryBuilder {

	public SearchRequestBuilder requestParameters(SearchRequestBuilder searchRequestBuilder, ESParams params) {

		Set<String> paramNames = params.getNames();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		for (String name : paramNames) {
			if (StringUtils.isNotBlank(name)) {
				ParamType parameterType = params.getEnumParameterType(name);
				switch (parameterType) {
				case STRING:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				case CHARACTER:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				case LONG:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				case DATE:
					@SuppressWarnings("unchecked")
					List<Long> dateTimes = (List<Long>) params.getParameter(name);
					if (CollectionUtils.isNotEmpty(dateTimes)) {
						RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(name);
						rangeQueryBuilder.from(dateTimes.get(0));
						if (dateTimes.size() > 1) {
							rangeQueryBuilder.to(dateTimes.get(1));
						}
						boolQueryBuilder.must(rangeQueryBuilder);
					}
					break;
				case LIST:
					@SuppressWarnings("unchecked")
					List<Long> values = (List<Long>) params.getParameter(name);
					TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(name, values);
					if (CollectionUtils.isNotEmpty(values)) {
						boolQueryBuilder.must(termsQueryBuilder);
					}
					break;
				default:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				}
			}
		}
		searchRequestBuilder.setQuery(boolQueryBuilder);
		return searchRequestBuilder;
	}
	
	public SearchRequestBuilder request_parameters(SearchRequestBuilder searchRequestBuilder, ESParams params) {

		Set<String> paramNames = params.getNames();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		for (String name : paramNames) {
			if (StringUtils.isNotBlank(name)) {
				ParamType parameterType = params.getEnumParameterType(name);
				switch (parameterType) {
				case STRING:
  					if(name.equals("DISTRIBUTOR_CODE")&&params.getParameter(name)!=null){//渠道代码模糊查询
						boolQueryBuilder.must(QueryBuilders.prefixQuery(name, ((String)params.getParameter(name)).toLowerCase()));
					}else{
 						boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					}
					break;
				case CHARACTER:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				case LONG:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				case DATE:
					@SuppressWarnings("unchecked")
					List<Long> dateTimes = (List<Long>) params.getParameter(name);
					if (CollectionUtils.isNotEmpty(dateTimes)) {
						RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(name);
						rangeQueryBuilder.from(dateTimes.get(0));
						if (dateTimes.size() > 1) {
							rangeQueryBuilder.to(dateTimes.get(1));
						}
						boolQueryBuilder.must(rangeQueryBuilder);
					}
					break;
				case LIST:
					@SuppressWarnings("unchecked")
					List<Long> values = (List<Long>) params.getParameter(name);
					TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(name, values);
					if (CollectionUtils.isNotEmpty(values)) {
						boolQueryBuilder.must(termsQueryBuilder);
					}
					break;
				default:
					boolQueryBuilder.must(QueryBuilders.matchQuery(name, params.getParameter(name)).operator(
							MatchQueryBuilder.Operator.AND));
					break;
				}
			}
		}
		
		searchRequestBuilder.setQuery(boolQueryBuilder);
		return searchRequestBuilder;
	}
}
