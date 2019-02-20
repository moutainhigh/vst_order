/**
 * 
 */
package com.lvmama.vst.order.utils;

import com.lvmama.vst.comm.utils.StringUtil;

/**
 * @author pengyayun
 *
 */
public class FieldValidUtils {
	
	
	public static boolean checkFieldLength(String field,int max){
		if (StringUtil.isNotEmptyString(field)&&getFieldCount(field)>max) {
			 return false;
		}
		return true;
	}
	
	public  static int getFieldCount(String s){  
        int length = 0;  
        for(int i = 0; i < s.length(); i++)  
        {  
            int ascii = Character.codePointAt(s, i);  
            if(ascii >= 0 && ascii <=255)  
                length++;  
            else  
                length += 2;  
                  
        }
        return length;  
   }
	
	public static void main(String[] args) {
		boolean result=checkFieldLength("王艺龙王艺龙王艺龙王艺龙王艺龙王艺龙王艺aaadddggge", 50);
		System.out.println(result);
	}
}
