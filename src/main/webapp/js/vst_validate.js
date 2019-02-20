/**
*扩展JQuery 的验证规则，自定义验证规则不能使用属性配置方式，而且方法名不能用重复
*@author mayonghua
*@date 2013-10-28
*/

/**
 * 验证固定电话
 */
jQuery.validator.addMethod("isTel", function(value, element) {
    var tel = /^\d{3,4}-?\d{7,9}$/;//电话号码格式010-12345678   
    return this.optional(element) || (tel.test(value));       
 }, "请正确填写您的电话号码");

/**
 * 验手机号
 */
jQuery.validator.addMethod("isMobile", function(value, element) {
    var mobile = /^1[3|4|5|8|7|9][0-9]\d{8}$/;//电话号码格式010-12345678   
    return this.optional(element) || (mobile.test(value));       
 }, "请正确填写您的手机号码");

/**
 * 只能是字母
 */
jQuery.validator.addMethod("isAlphabet", function(value, element) {
    var alphabet = /^[a-zA-Z]+$/;//电话号码格式010-12345678   
    return this.optional(element) || (alphabet.test(value));       
 }, "只能输入字母");


/**
 * 验证传真
 */
jQuery.validator.addMethod("isFax", function(value, element) {
    var fax = /^\d{3,4}-?\d{7,9}$/;//电话号码格式010-12345678   
    return this.optional(element) || (fax.test(value));       
 }, "请正确填写您的传真号码");

/**
 * 验证数字
 */
jQuery.validator.addMethod("isNum", function(value, element) {
    var num = /^((100|[1-9]?\d)(\.\d{2})?)$/;//电话号码格式010-12345678   
    return this.optional(element) || (num.test(value));       
 }, "只能填写数字及小数点");
/**
 * 验证特殊字符
 */
jQuery.validator.addMethod("isChar", function(value, element) {
    var chars =  /^([\u4e00-\u9fa5]|[a-zA-Z0-9])+$/;//验证特殊字符  
    return this.optional(element) || (chars.test(value));       
 }, "不可输入特殊字符");

 /**
 * 验证数字 只能填写正数(或2位小数)
 */
jQuery.validator.addMethod("isAmountNum", function(value, element) {
    //var num = /^[1-9]?\d*$/;
      var num = /^(([1-9]?\d*)(\.\d{2})?)$/;
    return this.optional(element) || (num.test(value));       
 }, "只能填写正数(或2位小数)");
 
  /**
 * 验证新数据与旧数据相等不
 */
jQuery.validator.addMethod("isOldValue", function(value, element) {
    
   //console.debug($(this));
    var oldData = $(element).attr("oldData");
    alert(oldData);
    if(oldData==value){
    	return false;
    }else{
    	return true;
    }
          
 }, "不可与原值相同");