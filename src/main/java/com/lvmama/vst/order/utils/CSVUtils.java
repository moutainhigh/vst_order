package com.lvmama.vst.order.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.order.job.MailTicketOrderDataProcessJobService;

public class CSVUtils {
	private static final Logger LOG= LoggerFactory
			.getLogger(MailTicketOrderDataProcessJobService.class);
	@SuppressWarnings("rawtypes")
	public static File createCSVFile(List exportData, LinkedHashMap map, String outPutPath,  
	            String fileName) {  
		File csvFile = null;  
		BufferedWriter csvFileOutputStream = null;  
		try {  
			File file = new File(outPutPath);  
			if (!file.exists()) {  
				file.mkdir();  
			}  
			//定义文件名格式并创建  
			csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));  
			LOG.info("csvFile：" + csvFile);  
			// UTF-8使正确读取分隔符","  
			csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "GBK"), 1024);  
			LOG.info("csvFileOutputStream：" + csvFileOutputStream); 
			// 写入文件头部  
			for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {  
				 java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();  
				 csvFileOutputStream.write((String) propertyEntry.getValue() != null ? new String(((String) propertyEntry.getValue()).getBytes("GBK"), "GBK") : "");
				 LOG.info(new String(((String) propertyEntry.getValue()).getBytes("GBK"), "GBK"));  
				 if (propertyIterator.hasNext()) {  
					 csvFileOutputStream.write(",");  
				 }  
			}  
			csvFileOutputStream.write("\r\n"); 
			// 写入文件内容  
			for (Iterator iterator = exportData.iterator(); iterator.hasNext();) {  
				 Object row = (Object) iterator.next();  
				 for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {  
					   java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();  
					   csvFileOutputStream.write((String) BeanUtils.getProperty(row,((String) propertyEntry.getKey()) != null? (String) propertyEntry.getKey() : ""));  
					   if (propertyIterator.hasNext()) {  
						   csvFileOutputStream.write(",");  
					   }  
				 }  
				 if (iterator.hasNext()) {  
					 csvFileOutputStream.write("\r\n");  
				 }  
			}  
			csvFileOutputStream.flush();  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				csvFileOutputStream.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}  
		return csvFile;  
	} 
}
