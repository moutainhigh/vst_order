package com.lvmama.vst.comm.mybatis;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.hold.DataSourceAdapter;
import com.lvmama.hold.DataSourceContextHolder;
import com.lvmama.pub.DistributedContext;
import com.lvmama.vst.comm.mybatis.annotation.ForceRead;

public class VstOrderTransactionInterceptor extends TransactionInterceptor {
	
	private static final long serialVersionUID = 2911243048843029544L;

	private static Logger log = LoggerFactory.getLogger(VstOrderTransactionInterceptor.class);
    /**
     * 数据源选择适配器
     */
    private DataSourceAdapter dataSourceAdapter;
    /**
     * 读写分离控制开关
     */
    private boolean dataSourceHoldEnabled = false;
    
    /**
     * 数据从主库同步到从库的估计时间 单位秒
     */
    private Long dbSyncSecond=2L;
    
    private boolean checkWriteInReadonly=false;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean flag = false;
        Object result = null;
        try {
            if (dataSourceHoldEnabled && DataSourceContextHolder.getDataSourceName() == null) {
                Method method = invocation.getMethod();
                if(method.getAnnotation(ReadOnlyDataSource.class) == null) {
                	//默认库
                	DataSourceContextHolder.defaultDataSource();
                }else {
                	if(method.getAnnotation(ForceRead.class) == null && checkWriteInReadonly && DistributedContext.getDBWriteRecord().isUpdatedIn(dbSyncSecond)) {
                		DataSourceContextHolder.defaultDataSource();
                	}else {
            			if (dataSourceAdapter == null) {
                			DataSourceContextHolder.readDataSource();
                		} else {
                			DataSourceContextHolder.readDataSource(dataSourceAdapter,method);
                		}
                	}
                }
                
                flag = true;

                if (log.isDebugEnabled()) {
                	log.debug("#### DataSourceHold : " + DataSourceContextHolder.getDataSourceName() + " " + method.getName());
                }
            }

            result = super.invoke(invocation);

        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            if (flag) {
                DataSourceContextHolder.clearDataSource();
            }
        }
        return result;
    }

    public void setDataSourceAdapter(DataSourceAdapter dataSourceAdapter) {
        this.dataSourceAdapter = dataSourceAdapter;
    }

    public void setDataSourceHoldEnabled(boolean dataSourceHoldEnabled) {
        this.dataSourceHoldEnabled = dataSourceHoldEnabled;
    }

	public void setDbSyncSecond(Long dbSyncSecond) {
		this.dbSyncSecond = dbSyncSecond;
	}

	public void setCheckWriteInReadonly(boolean checkWriteInReadonly) {
		this.checkWriteInReadonly = checkWriteInReadonly;
	}
}
