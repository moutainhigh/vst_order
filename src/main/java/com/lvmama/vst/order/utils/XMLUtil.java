package com.lvmama.vst.order.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XMLUtil {

    public static String toXML(Object obj) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");//编码格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// 是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);// 是否省略xm头声明信息
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T fromXML(String xml, Class<T> valueType) {
        try {
            JAXBContext context = JAXBContext.newInstance(valueType);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
