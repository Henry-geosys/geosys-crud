package com.geosys.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

class ProgressInfo {
	public String dateString = null;
	public String stage = null;
	public String startDateString = null;
	public String taskName = null;
	
	public ProgressInfo(String dateString, String stage) {
		this.dateString = dateString;
		this.stage = stage;
	}
	
	public ProgressInfo(String dateString, String stage, String startDate, String taskName) {
		this.dateString = dateString;
		this.stage = stage;
		this.startDateString = startDate;
		this.taskName = taskName;
	}
}

class ActivityEntity {
	public String sd = null;
	public String ed = null;
	public String nm = null;
	
	public ActivityEntity() {}
}

@Component
public class ExcelUtils {

    private static final boolean ASC = true;//升序
    private static final boolean DESC = false;//降序
    
    // HARDCODED Color map for WKCDA staging
    double[] getColorByStageName(String stage) {
    	double[] rgba = new double[4];
    	switch(stage) {
//			case "Setting Out":
//				rgba[0] = 255;
//				rgba[1] = 0;
//				rgba[2] = 0;
//				rgba[3] = 0.7;
//				break;
//			case "Rock Head Level Verification":
//				rgba[0] = 255;
//				rgba[1] = 127;
//				rgba[2] = 0;
//				rgba[3] = 0.7;
//				break;
//			case "Founding Level Verification":
//				rgba[0] = 255;
//				rgba[1] = 255;
//				rgba[2] = 0;
//				rgba[3] = 0.7;
//				break;
//			case "Rebar Installation":
//				rgba[0] = 0;
//				rgba[1] = 255;
//				rgba[2] = 255;
//				rgba[3] = 0.7;
//				break;
//			case "Concreting":
//				rgba[0] = 0;
//				rgba[1] = 0;
//				rgba[2] = 255;
//				rgba[3] = 0.7;
//				break;
//			case "Steel H-pile installation":
//				rgba[0] = 127;
//				rgba[1] = 255;
//				rgba[2] = 0;
//				rgba[3] = 0.7;
//				break;

			case "Excavation":
				rgba[0] = 255;
				rgba[1] = 223;
				rgba[2] = 52;
				rgba[3] = 1;
				break;
			case "Rock Drilling":
				rgba[0] = 255;
				rgba[1] = 134;
				rgba[2] = 77;
				rgba[3] = 1;
				break;
			case "Rebar Installation and Concreting":
				rgba[0] = 50;
				rgba[1] = 51;
				rgba[2] = 255;
				rgba[3] = 1;
				break;
			case "Verification Test":
				rgba[0] = 52;
				rgba[1] = 255;
				rgba[2] = 51;
				rgba[3] = 1;
				break;
//			case "Grouting":
//				rgba[0] = 0;
//				rgba[1] = 255;
//				rgba[2] = 0;
//				rgba[3] = 0.7;
//				break;
//			case "Completed":
//				rgba[0] = 80;
//				rgba[1] = 80;
//				rgba[2] = 80;
//				rgba[3] = 1;
//				break;
		}
    	
    	return rgba;
    }
    
    int getPhaseNumByStageName(String stage) {
    	switch(stage) {
//    		case "Setting Out":
//    			return 10;
//    		case "Rock Head Level Verification":
//    			return 20;
//    		case "Founding Level Verification":
//    			return 30;
//    		case "Rebar Installation":
//    			return 40;
//    		case "Concreting":
//    			return 50;
//    		case "Steel H-pile installation":
//    			return 60;
//    		case "Verification Test":
//    			return 90;
//    		case "Grouting":
//    			return 80;
//    		case "Completed":
//    			return 100;
			case "Excavation":
    			return 10;
    		case "Rock Drilling":
    			return 20;
    		case "Rebar Installation and Concreting":
    			return 30;
    		case "Verification Test":
    			return 40;
    	}
    	
    	return 0;
    }

    JSONObject appendActivity (ActivityEntity activity) {
    	JSONObject tempJson = new JSONObject();
        tempJson.accumulate("sd", activity.sd);
        tempJson.accumulate("ed", activity.ed);
        tempJson.accumulate("aed", activity.ed);
        tempJson.accumulate("nm", activity.nm);
        // tempJson.accumulate("nm", bimString);
        
        return tempJson;
    }
    
//    @Value("${geosys.layer_offset}")
//    private Long layer_offset;

    public String excelToJson(String layerPath, String bimPath, Long layer_offset) throws IOException {
        JSONObject resultJson = new JSONObject(true);
        JSONObject startTimeJson = new JSONObject(true);
        JSONObject endTimeJson = new JSONObject(true);
        JSONArray phaseArray = new JSONArray();
        JSONArray activityArray = new JSONArray();

        //将bim表格转成map数据
        Map<String, List<ProgressInfo>> bimMap = excelToMap(bimPath);

        //读取layer文本数据
        BufferedReader reader = new BufferedReader(new FileReader(layerPath));
        // String temp = reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        Set<String> tempDateSet = new HashSet<>();
        Map<String, ActivityEntity> activityMap = new HashMap<String, ActivityEntity>();
        Map<Integer, JSONObject> phaseMap = new HashMap<Integer, JSONObject>();
        // Set<String> bimIdDateSet = new HashSet<>();
        
        while ((line = reader.readLine()) != null) {
            String[] items = line.split(",");
            Long layerId = Long.parseLong(items[0]) + layer_offset;
            String bimString = items[1];
            //与bim表格数据作比较
            if (bimMap.containsKey(bimString)) {
            	List<ProgressInfo> progressList = bimMap.get(bimString);
            	
            	for (int i = 0; i < progressList.size(); i++) { 		      
            		ProgressInfo progressInfo = progressList.get(i); 	
            		
            		String dateString = progressInfo.dateString;
                    String stageString = progressInfo.stage;
                    String startDateString = progressInfo.startDateString;
                    String taskNameString = progressInfo.taskName;
                    
                    if(dateString.equals("1970-01-01")) {
                    	continue;			// Skip wrongly input data
                    }
                    
                    if(stageString!=null) {
                    	/// TODO: Do something to store phasing
                    	int phaseNum = getPhaseNumByStageName(stageString);
                    	// System.out.println(phaseNum);
                    	
                    	if(phaseNum>0) {
    	                	JSONObject phaseObj = phaseMap.get(phaseNum);
    	                	if(phaseObj==null) {
    	                		phaseObj = new JSONObject();
    	                		phaseObj.set("phase", phaseNum);
    	                		phaseObj.set("nm", stageString);

    	                		double[] clr = getColorByStageName(stageString);
    	                		JSONArray clrArr = new JSONArray();
    	                		clrArr.put(clr[0]);
    	                		clrArr.put(clr[1]);
    	                		clrArr.put(clr[2]);
    	                		clrArr.put(clr[3]);
    	                		phaseObj.set("clr", clrArr);
    	                		
    	                		JSONObject script = new JSONObject(true);
    	                		phaseObj.set("script", script);
    	                		phaseMap.put(phaseNum, phaseObj);
    	                	}
    	                	
    	                	JSONObject phaseScript = phaseObj.getJSONObject("script");
    	                	// phaseScript.append(dateString, layerId);
    	                	phaseScript.append(startDateString, layerId);
                    	}
                    }

                    //添加activity_list
                    // Hardcoded some strings for stages as completed items, 'Completed' and 'Grouting' for WKCDA
                    if(stageString==null) {
                    	if(startDateString!=null) {
                    		startTimeJson.append(startDateString, layerId);
                    	}
                    	else {
                    		startTimeJson.append(dateString, layerId);
                    	}
                    	endTimeJson.append(dateString, layerId);
                    	
                    	/// T2
                    	/*
                    	if(!tempDateSet.contains(dateString)) {
                    		ActivityEntity ae = new ActivityEntity();
                    		
                    		if(startDateString!=null) {
                    			ae.sd = startDateString;
                    		}
                    		else {
                    			ae.sd = dateString;
                    		}
                    		
                    		ae.ed = dateString;
                    		ae.nm = dateString;
                            tempDateSet.add(dateString);
                            activityMap.put(dateString, ae);
                    	}
                    	 */
                    	
                    	/// YLEPP only
                    	if(!tempDateSet.contains(bimString)) {
                    		ActivityEntity ae = new ActivityEntity();
                    		
                    		if(startDateString!=null) {
                    			ae.sd = startDateString;
                    		}
                    		else {
                    			ae.sd = dateString;
                    		}
                    		
                    		ae.ed = dateString;
                    		
                    		if(taskNameString!=null) {
                    			ae.nm = taskNameString;
                    		}
                    		else {
                    			ae.nm = bimString;
                    		}
                            tempDateSet.add(bimString);
                            activityMap.put(bimString, ae);
                    	}
                    }
                    else {
                    	/// WKCDA
                    	ActivityEntity ae = activityMap.get(bimString);
                    	if(ae==null) {
                    		ae = new ActivityEntity();
                    		ae.nm = bimString;
                    		activityMap.put(bimString, ae);
                    		tempDateSet.add(dateString);
                    	}
                    	else {
                    		ae = activityMap.get(bimString);
                    	}

						if(stageString.equals("Excavation")) {
//                    	if(stageString.equals("Setting Out")) {
                    		// ae.sd = dateString;
                    		ae.sd = startDateString;
                    		startTimeJson.append(startDateString, layerId);
                    	}
                    	else if(
//                    		stageString.equals("Grouting")
//                    		 || stageString.equals("Completed")
//                    		 || stageString.equals("Verification Test")
                    		// stageString.equals("Completed")
								stageString.equals("Verification Test")
                    	) {
							if("".equals(dateString)){
								Date date = new Date();

								Calendar rightNow = Calendar.getInstance();
								rightNow.setTime(date);
								rightNow.add(Calendar.MONTH, 1);

								Date dt1 = rightNow.getTime();
								dateString = DateUtil.formatDate(dt1);
							}
                    		endTimeJson.append(dateString, layerId);
                    		ae.ed = dateString;
                    	}
                    	activityMap.put(bimString, ae);

                    }
                }   
            }
        }
        reader.close();

        // For the activity list without end date, set as today, but mind that start date should not be null, meaning this progress has not yet started
        LocalDate currentdate = LocalDate.now();
        String todayStr = currentdate.toString();
        for (ActivityEntity ae : activityMap.values()) {
        	// System.out.println("("+ae.nm+", "+ae.sd+"-"+ae.ed+")");
        	
        	if(ae.sd==null && ae.ed==null) {
        		;
        	}
        	else {
	        	if(ae.sd==null) {
	        		ae.sd = ae.ed;
	        	}
	        	
	        	if(ae.ed==null) {
	        		ae.ed = todayStr;
	        	}
	        	JSONObject tempJson = appendActivity(ae);
        		activityArray.add(tempJson);
        	}
        }
        
        // Conclude phasemap into ordered phase list
        SortedSet<Integer> keys = new TreeSet<>(phaseMap.keySet());
        for (Integer key : keys) { 
        	JSONObject phaseObj = phaseMap.get(key);
           
        	JSONObject phaseScript = phaseObj.getJSONObject("script");
       		phaseScript = sortJson(phaseScript, ASC);
       		phaseObj.set("script", phaseScript);

       		phaseArray.add(phaseObj);
        }
        
        startTimeJson = sortJson(startTimeJson, ASC);
        endTimeJson = sortJson(endTimeJson, ASC);
        activityArray = sortArray(activityArray, ASC);
        resultJson.accumulate("start_time", startTimeJson);
        resultJson.accumulate("end_time", endTimeJson);
        resultJson.accumulate("actural_end_time", endTimeJson);
        resultJson.accumulate("phases", phaseArray);
        resultJson.accumulate("activity_list", activityArray);

        return resultJson.toString();
    }

    private Map<String, List<ProgressInfo>> excelToMap(String path) throws IOException {
        Map<String, List<ProgressInfo>> map = new HashMap<>();
        //读取excel文件
        XSSFWorkbook workbook = new XSSFWorkbook(path);
        if (workbook.getNumberOfSheets() == 0) {
        	workbook.close();
        	return map;
        }
        
        int sheetCount = workbook.getNumberOfSheets();
        for(int sc=0; sc<sheetCount; sc++) {
        	//组装map数据：第一列为key，最后一列为value
            XSSFSheet sheet = workbook.getSheetAt(sc);
            short dateColIdx = 1, stageColIdx = -1 , startDateCol=-1, taskNameCol=-1;
            for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
                XSSFRow row = sheet.getRow(i);
                short lastCellIdx = row.getLastCellNum();
                
                String key = row.getCell(0).getStringCellValue();
                // XSSFCell cell = row.getCell(lastCellIdx-1);
                if (i == 0) {
                	// Find the cell with value 'Completed Date' and mark it as date column
                	for(short j=0; j<lastCellIdx; j++) {
                		XSSFCell cellHeader = row.getCell(j);
                		String cellHeaderVal = cellHeader.getStringCellValue();
                		
                		if(cellHeaderVal.equals("Start Date")) {
                			startDateCol = j;
                		}
                		else if(cellHeaderVal.equals("Completed Date") || cellHeaderVal.equals("End Date")) {
                			dateColIdx = j;
                		}
                		else if(cellHeaderVal.equals("Stage")) {
                			stageColIdx = j;
                		}
                		else if(cellHeaderVal.equals("Task Name")) {
                			taskNameCol = j;
                		}
                	}
                	continue;
                }
                XSSFCell cellDate = row.getCell(dateColIdx);
                String dateString = (cellDate == null || "".equals(cellDate.getStringCellValue()))?"":DateUtil.formatDate(DateUtil.parse(cellDate.getStringCellValue(), "yyyy/MM/dd HH:mm:ss"));

                String stageString = null;
                if(stageColIdx>0) {
                	XSSFCell cellStage = row.getCell(stageColIdx);
					if(cellStage!=null) {
						stageString = cellStage.getStringCellValue();
					}else{
						continue;
					}
                }
                
                String startDateString = null;
                if(startDateCol>0) {
                	XSSFCell cellStartDate = row.getCell(startDateCol);
                	startDateString = (cellStartDate == null || "".equals(cellStartDate.getStringCellValue()))?"":DateUtil.formatDate(DateUtil.parse(cellStartDate.getStringCellValue(), "yyyy/MM/dd HH:mm:ss"));
                }
                
                String taskNameString = null;
                if(taskNameCol>0) {
                	XSSFCell cellTaskName = row.getCell(taskNameCol);
                	taskNameString = cellTaskName.getStringCellValue();
                }
                
                List<ProgressInfo> currentProgressList = map.get(key);
                if(currentProgressList==null) {
                	currentProgressList = new ArrayList<ProgressInfo>();
                	map.put(key, currentProgressList);
                }
                
                currentProgressList.add(new ProgressInfo(dateString, stageString, startDateString, taskNameString));
            }
        } 
        
        workbook.close();
        return map;
    }

    /**
     * @param json json数据
     * @param type 排序方式  0：升序， 1：降序
     */
    private JSONObject sortJson(JSONObject json, boolean type) {
        JSONObject resultJson = new JSONObject(true);//null表示不排序，不排序情况下，如果order为true按照加入顺序排序，否则按照hash排序

        String[] keyArray = json.keySet().toArray(new String[0]);
        if (type == ASC) {
            Arrays.sort(keyArray);
        } else {
            Arrays.sort(keyArray, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
        }
        for (String key : keyArray) {
            resultJson.accumulate(key, json.get(key));
        }

        return resultJson;
    }

    /**
     * @param array array数据
     * @param type  排序方式  0：升序， 1：降序
     */
    private JSONArray sortArray(JSONArray array, boolean type) {

        List<JSONObject> list = array.toList(JSONObject.class);
        if (type == ASC) {
            list.sort(new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    return o1.getStr("sd").compareTo(o2.getStr("sd"));
                }
            });
        } else {
            list.sort(new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    return o2.getStr("sd").compareTo(o1.getStr("sd"));
                }
            });
        }

        return new JSONArray(list);
    }
}

