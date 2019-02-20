package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdPremUserRel;
import com.lvmama.vst.comm.vo.ResultHandle;

/**
 * 客服与用户接口定义
 * @author Zhang.Wei
 */
public interface IOrdPremUserRelService {

    /**
   	 * 保存客服与注册用户的关系
   	 * @param ordPremUserRel 客服与注册用户实体
   	 * @return ResultHandle对象
   	 * @author Zhang.Wei
   	 */
    public	ResultHandle saveOrdPremUserRel(OrdPremUserRel ordPremUserRel);

}
