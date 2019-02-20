<html xmlns="http://www.w3.org/1999/xhtml"> 
 <head> 
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
 <title>团队境内合同</title> 
 </head> 

<body>
<div class="txt-hetong">
	<p style="font-size:18px;">
	    <#if htmlString??>
	          ${(htmlString)!''}
	    <#elseif success??>
	          ${(success)!''}
        <#else>
              ${(error)!''}
	    </#if>
       
    </p><br/>
 </div>
</body> 
</html>