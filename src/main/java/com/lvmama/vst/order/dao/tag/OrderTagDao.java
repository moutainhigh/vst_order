package com.lvmama.vst.order.dao.tag;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.dao.tag.po.OrderTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2018/6/6.
 */
@Repository("orderTagDao")
public class OrderTagDao extends MyBatisDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderTagDao.class);
    public OrderTagDao(){
        super("ORD_ORDER_TAG");
    }

    public Integer insert(OrderTag orderTag){
        if(null==orderTag){
            logger.info("下单保存拓展属性对象不能为空");
            throw new RuntimeException("下单保存拓展属性对象不能为空");
        }
        logger.info("下单保存拓展属性对象请求参数:{}",orderTag.toString());
        Long objectId=orderTag.getObjectId();
        if(null==objectId
                ||null==orderTag.getObjectType()
                ||null==orderTag.getTagValue()
                ||null==orderTag.getTagType()){
            logger.info("getObjectId||getObjectType||getTagValue||getTagType 不能为空.objectId={}",objectId==null?-1:objectId);
            throw new RuntimeException("getObjectId||getObjectType||getTagValue||getTagType 不能为空");

        }
        Integer result=super.insert("insert",orderTag);
        logger.info("下单保存拓展属性对象结果{}={}",objectId,result);
        return result;
    }
}
