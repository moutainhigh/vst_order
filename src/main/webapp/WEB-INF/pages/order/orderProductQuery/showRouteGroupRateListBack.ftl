<table>
    <colgroup>
        <col class="date">
        <col class="percent">
        <col class="persons-num">
        <col class="price">
        <col class="action">
    </colgroup>
    <thead>
    <tr>
        <th>发班团期</th>
        <th class="th-percentage">成团率 <i></i></th>
        <th>收客人数</th>
        <th class="th-price">价格 <i></i></th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>		
		<#if timePriceList??>
			<#list timePriceList as timePriceVo>
				<#if (timePriceVo.specDate?string("yyyy-MM-dd")?date("yyyy-MM-dd") >= startDate?date("yyyy-MM-dd"))>
	                <tr>
	                    <td>${timePriceVo.departureDate}(${timePriceVo.specDateWeek})</td>
	                    <td class="td-percentage">
		                    <#if groupRateMap[timePriceVo.departureDate]?? && groupRateMap[timePriceVo.departureDate].groupRate??>
		                    	${groupRateMap[timePriceVo.departureDate].groupRate*100}%
		                    <#else>
		                    	---
		                    </#if>
	                    </td>
	                    <td><#if groupRateMap[timePriceVo.departureDate]??>${groupRateMap[timePriceVo.departureDate].travellerNum!''}</#if></td>
	                    <td>
	                        <p class="adult">成人：<i>${timePriceVo.lowestSaledPriceYuan}</i></p>
	                        <p class="kid">儿童：<i>${timePriceVo.lowestSaledChildPriceYuan}</i></p>
	                    </td>
	                    <td class="order" data="${timePriceVo.departureDate}">预定</td>
	                </tr>
                </#if>
            </#list>
		</#if>
    </tbody>
</table>

<script>
  
$(function () {     
    
    //排序切换
    var $document = $(document);
    var arr = [];

    //初始化排序数组
    //initArr();

    //监听事件，排序
    $document.on('click', '.th-percentage', function () {
        var $this = $(this);
        //判断排序方向
        if ($this.hasClass('down')) {
            //从低到高
            sortTrs('percentage', -1);
            $this.removeClass('down');
        } else {
            //从高到底
            sortTrs('percentage', 1);
            $this.addClass('down');
        }
    });
    $document.on('click', '.th-price', function () {
        var $this = $(this);
        //判断排序方向
        if ($this.hasClass('down')) {
            //从低到高
            sortTrs('price', -1);
            $this.removeClass('down');
        } else {
            //从高到底
            sortTrs('price', 1);
            $this.addClass('down');
        }
    });

    function initArr(sortAttr) {
    	var trs = $('.guest-num-container tbody tr');
        arr = [];
        arr_noSort =[];
        if(sortAttr=="percentage"){        	
        	trs.each(function () {
	            var obj = {
	                elem: null,
	                percentage: 0,
	                price: 0
	            };
	            var $this = $(this);
	            obj.elem = $this;
	            if($this.find('.td-percentage').html()=="---"){
	            	arr_noSort.push(obj)
	            }else{
	            	obj.percentage = parseInt($this.find('.td-percentage').html());
	            	arr.push(obj)
	            }
	        });
        }else{
        	trs.each(function () {
	            var obj = {
	                elem: null,
	                percentage: 0,
	                price: 0
	            };
	            var $this = $(this);
	            obj.elem = $this;
	            obj.price = parseInt($this.find('.adult i').html());
	            arr.push(obj)
	        });
        }
        
    }

    function sortTrs(sortAttr, move) {
        initArr(sortAttr);
        arr.sort(function (a, b) {
            return (b[sortAttr] - a[sortAttr]) * move
        });
    	for(var i=0;i<arr_noSort.length;i++){
    		arr.push(arr_noSort[i]);
    	}
        rerender();
    }

    function rerender() {
        var tbody = $('.guest-num-container tbody');
        tbody.empty();
        for (var i = 0; i < arr.length; i++) {
            tbody.append(arr[i].elem)
        }
    }
    
    //预定
    $document.on('click', '.guest-num-container .order', function () {
        var date = $(this).attr("data");
    	$(".js_youwanTime").val(date);
    	requestProduct();
    });
    
});
</script>


