<#import "/base/spring.ftl" as s/>
<#if msg ?? && msg != ''>
 <script type="text/javascript">
	alert("${msg!''}");
</script>
</#if>
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>商品类型</th>
            <th>商品ID</th>
            <th>商品名称</th>
        </tr>
    </thead>
    <tbody>
        <#list boundGoodsList  as map> 
            <tr>
				 <td>${map['branchType']!''}</td>
				 <td>${map['goodsId']!''}</td>
				 <td>${map['goodsName']!''}</td>
       	  </tr>
       </#list>
     </tbody>
</table>
