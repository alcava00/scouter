/*
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package scouter.client.stack.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import scouter.client.stack.config.ParserConfig;
import scouter.client.stack.utils.ResourceUtils;
import scouter.client.stack.utils.StringUtils;


public class BasicFileStackParser extends StackParser {
	
	public BasicFileStackParser(){
		
	}
	
	public BasicFileStackParser(ParserConfig config){
		super(config);
	}
	
	public void process(){
	    BufferedReader reader = null;
	    ZipInputStream zipInputStream = null;
	    try {
	    	if(getStackContents() != null){
	    		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(getStackContents().getBytes())));
	    	}else if(ResourceUtils.isZipFile(getStackFileInfo().getFilename())){
	    		zipInputStream = new ZipInputStream(new FileInputStream(new File(getStackFileInfo().getFilename())));
	    		zipInputStream.getNextEntry();
	    		reader = new BufferedReader(new InputStreamReader(zipInputStream));
	    	}else{
		    	reader = new BufferedReader(new FileReader(new File(getStackFileInfo().getFilename())));
	    	}
	    	
	    	StringBuilder  timeBuffer = null;
	    	String line = null;
	    	boolean isWorking = false;
	    	ArrayList<String> workingList = new ArrayList<String>(300);
	    	ArrayList<String> workerThread = getWorkerThread();
	    	ArrayList<String> workingThread = getWorkingThread();
	    	
	    	int lineCount = 0;
	    	int lineSize = 0;
	    	int workerCount = 0;
	    	int workingCount = 0;
	    	int totalWorkerCount = 0;
	    	int dumpCount = 0;
	    	ThreadStatusInfo tsinfo = new ThreadStatusInfo();
	    	
	    	ParserConfig config = getConfig();
	    	String timeFilter = config.getTimeFilter();
	    	int timePosition = config.getTimePosition();
	    	int timeSize = config.getTimeSize();
	    	int stackStartLine = config.getStackStartLine();
	    	String divideStack = config.getDivideStack();
	    	String timeMatchStr = null; 
	    	
	    	if(timeFilter != null && timeSize > 0){
	    		timeMatchStr = new StringBuilder(50).append("(.*)").append(timeFilter).append("(.*)").toString();
	    	}
	    	
	    	while((line = reader.readLine()) != null){
	    		progressBar();
	    		// Empty line or include divideStack string
	    		lineSize = line.trim().length();
	    		if( lineSize == 0 || (divideStack != null && line.indexOf(divideStack)>= 0)){
	    			if(isWorking && lineCount > stackStartLine){ 
	    				processStack(workingList, tsinfo);
	    			}
	    			isWorking = false;
	    			workingList = new ArrayList<String>(300);
	    			lineCount = 0;
	    			if(lineSize == 0){
	    				continue;
	    			}
	    		}
    		
	    		// Dump time
	    		if(lineCount == 0){
	    			if(timeMatchStr != null && line.matches(timeMatchStr) && line.length() >= (timePosition + timeSize)){
    					if(timeBuffer != null  && timeBuffer.length() > 10){
    						timeBuffer.append('\t').append(workerCount).append('\t').append(workingCount);    						
    						for(int tsIndex = 0; tsIndex < tsinfo.geSize(); tsIndex++){
        						timeBuffer.append('\t').append(tsinfo.getValue(tsIndex));    							
    						}
    						addTime(timeBuffer.toString());
    					}
    					timeBuffer = new StringBuilder(50);
    					timeBuffer.append(line.substring(timePosition, (timePosition + timeSize)));
    					tsinfo = new ThreadStatusInfo();
    					writeTime(line);
    					workerCount = 0;
    					workingCount = 0;
    					dumpCount++;
	    			}else{
	    				if(StringUtils.checkExist(line, workerThread)){
	    					workerCount++;
	    					totalWorkerCount++;
	    	    		}
	    				
	    			}
	    			
	    		} 
	    		
	    		// Working Thread
	    		if(!isWorking && StringUtils.checkExist(line, workingThread)){
	    			isWorking = true;
	    			workingCount++;
	    		}

				//Thread status count
	    		workingList.add(line);
	    		lineCount++;
	    		
	    	}
	    	
	    	// last stack
	    	if(isWorking && lineCount > stackStartLine){
				processStack(workingList, tsinfo);	 
	    	}
	    		    	
	    	// last time
			if(timeMatchStr != null && timeBuffer != null && timeBuffer.length() > 10){
				timeBuffer.append('\t').append(workerCount).append('\t').append(workingCount);
				for(int tsIndex = 0; tsIndex < tsinfo.geSize(); tsIndex++){
					timeBuffer.append('\t').append(tsinfo.getValue(tsIndex));    							
				}
				addTime(timeBuffer.toString());
			}
			
			setTotalWorkerCount(totalWorkerCount);
			setDumpCount(dumpCount);
	    }catch(Exception ex){
	    	throw new RuntimeException(ex);
	    }finally{
	    	if(zipInputStream != null){
	    		try { zipInputStream.closeEntry();}catch(Exception e){}	    		
	    	}
	    	if(reader != null){
	    		try { reader.close();}catch(Exception e){}
	    	}
	    }
	}
}
