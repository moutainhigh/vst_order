/**
 * 
 * 订单支付业务
 * 
 * @author wenzhengtao
 * 
 */
function getO2OUserInfo(userName,sign){
	  $.ajax({

	         type : "get", //jquey是不支持post方式跨域的
	         async:false,
	         url : "http://md.lvmama.com/o2o_back/query/api/getO2OUserByUserName.do?userName="+userName+"&sign="+sign, //跨域请求的URL
	         dataType : "jsonp",
	         jsonp: "jsoncallback",
	         jsonpCallback:"getO2oUser",
	         success : function(json){ 
	        	 if (json.success) {
	        		 var o2oUserVo=json.attributes.o2oUserVo;
		        	 var storeName=o2oUserVo.storeName;
		        	 var userRelName=o2oUserVo.realName;
		        	 var userPhoneNumber=o2oUserVo.phoneNumber;
		        	 var subCompanyName=o2oUserVo.subCompanyName;
		        	 var subText="【"+subCompanyName+"】/【"+storeName+"】";
		        	 var userText="【"+userRelName+"】/【"+userPhoneNumber+"】";
		        	 $("#distributorCodeTitle").html("子公司/门店：");
		        	 $("#distributorCodeInfo").html(subText);
		        	 $("#distributionChannelTitle").html("门店下单人/电话：");
		        	 $("#distributionChannelInfo").html(userText);
				}
	         } 
	     }); 
}
	

