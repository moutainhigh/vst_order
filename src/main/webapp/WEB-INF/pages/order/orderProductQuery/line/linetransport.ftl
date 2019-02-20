 <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v6/header_new.css,/styles/v5/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/selectbox.css,/styles/v5/modules/tags.css,/styles/v5/modules/form.css,/styles/v5/modules/arrow.css,/styles/v5/modules/button.css,/styles/v5/modules/paging.css,/styles/v6/public/dianping.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/line/detail/product-detail.css,/styles/line/detail/calendar.css,/js/bower_components/PikaChoose/styles/base.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/calendar.css,/styles/v5/modules/dialog.css,styles/new_v/ob_login/l_fast_login.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/styles/v5/comments_enterorder.css">
<link rel="stylesheet" href="/vst_order/js/book/line_traffic.css" type="text/css">
 <#if transprotProdPackageList?? && transprotProdPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">	

  				<#if transprotProdPackageList?size == 1><!--只有一组-->
  					<#if transprotProdPackageList[0].prodPackageGroupTransport?exists>
	  					<#assign changeProdPackage = transprotProdPackageList[0] />
	  					<@lineTransportdetail.lineTransportdetail changeProdPackage transprotProdPackageList[0].prodPackageGroupTransport.transportType productItemIdIndex 'true'/>
	  					<#assign productItemIdIndex = productItemIdIndex+1 />
						<script>
							productItemIdIndex='${productItemIdIndex}';	       
						</script>
	  				</#if>
	  			<#elseif transprotProdPackageList?size == 2><!--单程-->
	  				<#if transprotProdPackageList[0].prodPackageGroupTransport?exists>
	  					<#assign changeProdPackage = transprotProdPackageList[0] />
	  					<#assign ifMore = 'true' />
	  					<@lineTransportdetail.lineTransportdetail changeProdPackage transprotProdPackageList[0].prodPackageGroupTransport.transportType productItemIdIndex ifMore/>
	  					<#assign productItemIdIndex = productItemIdIndex+1 />
						<script>
							productItemIdIndex='${productItemIdIndex}';	       
						</script>
	  				</#if>
	  				<#if transprotProdPackageList[1].prodPackageGroupTransport?exists>
	  					<#assign changeProdPackage = transprotProdPackageList[1] />
	  					<#assign ifMore = 'false' />
	  					<@lineTransportdetail.lineTransportdetail changeProdPackage transprotProdPackageList[1].prodPackageGroupTransport.transportType productItemIdIndex ifMore/>
	  					<#assign productItemIdIndex = productItemIdIndex+1 />
						<script>
							productItemIdIndex='${productItemIdIndex}';	       
						</script>
	  				</#if>	
	  			</#if>   					
  		</div>
  	</div>
  </div>
 </#if> 
 <script src="http://pic.lvmama.com/min/index.php?f=/js/bower_components/jquery/jquery.min.js,/js/bower_components/jquery.actual/jquery.actual.min.js,/js/bower_components/underscore/underscore.js,/js/bower_components/backbone/backbone.js,/js/bower_components/marionette/lib/backbone.marionette.min.js,/js/bower_components/moment/min/moment.min.js,/js/bower_components/PikaChoose/lib/jquery.jcarousel.min.js,/js/bower_components/PikaChoose/lib/jquery.pikachoose.js,/js/bower_components/PikaChoose/lib/jquery.touchwipe.min.js"></script>

<script src="http://pic.lvmama.com/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/v6/public/searchComplete.js,/js/v5/modules/pandora-poptip.js,/js/v6/public/dianping.js,/js/v5/modules/pandora-select.js,/js/common/losc.js"></script>

<script>
$(function(){ 
	
	//$(".qidi_zhuan").poptip();
	/*机型参数*/
$('.plane_table').delegate(".plane_type", "mouseenter", showJXinfo);
$('.plane_table').delegate(".plane_type", "mouseout", hideJXinfo);
	
	
	
});
</script>