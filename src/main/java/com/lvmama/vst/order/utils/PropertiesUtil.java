package com.lvmama.vst.order.utils;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by luoweiyi on 14-11-10.
 */
public class PropertiesUtil {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

	static{
		PropertiesUtil.getInstance();
	}

    private static Properties properties;

    private static volatile PropertiesUtil instance = null;

    private void init() {
        InputStream input = null;
        try {
            properties = new Properties();
            input = getClass().getResourceAsStream("/const.properties");
            properties.load(input);
        } catch (Throwable e) {
            logger.warn("Cannot load const file!",e);
        }finally {
            if(input != null){
                try{
                    input.close();
                }catch(Throwable e){
                    logger.warn("input.close() exception!",e);
                }
            }
        }
        // ResourceBundle.getBundle("const");
    }

    private PropertiesUtil() {
        init();
    }

    public static PropertiesUtil getInstance() {
        if (instance == null) {
            synchronized (PropertiesUtil.class) {
                if (instance == null) {
                    instance = new PropertiesUtil();
                }
            }
        }
        return instance;
    }

    public static String getValue(String key){

        if(StringUtils.isNotBlank(ZooKeeperConfigProperties.getProperties(key))){
            return ZooKeeperConfigProperties.getProperties(key);
        }

        String value = "";
        if(properties != null){
            value = properties.getProperty(key);
        }

        return value;
    }

    public static void main(String[] args){
        PropertiesUtil.getInstance();
        getValue("topHead");
    }
}
