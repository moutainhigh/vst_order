/**
*vst_pet工具类
*@author ranlongfei
*@date 2013-11-20
*/

var vst_pet_util = {
		/**
		 * 系统用户类
		 * @param showNode 显示姓名的控件
		 * @param idNode 保存ID的控件，一般是个隐藏域
		 */
		superUserSuggest : function(showNode, idNode) {
			$(showNode).jsonSuggest({
				//url : "/vst_back/supp/supplier/searchSupplierList.do",
				url : "/vst_back/pet/permUser/searchUser.do",
				maxResults : 10,
				minCharacters : 1,
				onSelect : function(item) {
					$(idNode).val(item.id);
				}
			});
		},

       
		/**
		 * 通用列表补全查询
		 * @param showNode 显示姓名的控件
		 * @param idNode 保存ID的控件，一般是个隐藏域
		 */
       commListSuggest : function(showNode,idNode,_url, _data){
    	   $(showNode).jsonSuggest({
				url : _url,
				maxResults : 10,
				minCharacters : 1,
				data:_data,
				onSelect : function(item) {
					if(null != idNode)
					{
						$(idNode).val(item.id);
				
					}
				}
			});
       },
       /**
        * 页面转拼音的工具方法
        */
       convert2pinyin : function(param) {
    	   if(!param) {
    		   return "";
    	   }
    	   var result = param;
    	   $.ajax({
    		   url : "/vst_back/pub/utils/getPinYin.do",
    		   type : "POST",
    		   async : false,
    		   data : {param:param},
    		   success : function(dt){
    			   result = dt;
    		   }
    	   });	
    	   return result;
       }
};
