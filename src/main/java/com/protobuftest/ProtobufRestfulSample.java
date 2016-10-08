package com.protobuftest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.googlecode.protobuf.format.JsonFormat;
 
public class ProtobufRestfulSample extends AbstractJavaSamplerClient implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	private static final String ProtoUrlMapFile = "protoUrlMap.properties";
    
    private static Map<String, String[]> protobufUrlMap = new HashMap<String, String[]>();
    
    private static final String ClassName_Protobuf = "$Builder";
    
    public static final String Parameter_Protobuf_Prefix = "proto.";
    
    static{
    	// init protobufUrlMap from protobuf url define properties file
    	try {
    		InputStream in = ProtobufRestfulSample.class.getClassLoader().getResourceAsStream(ProtoUrlMapFile);       
            Properties p = new Properties();
			p.load(in);
			Set<Object> keySet = p.keySet();
	        if(keySet!=null){
	        	String key = null;
	        	String value = null;
	        	for(Object keyObj : keySet){
	        		if(keyObj!=null){
	        			key = keyObj.toString().trim();
	        			value = p.getProperty(key);
	        			if(value!=null && value.trim().length()>0){
	        				String[] classNameArray = new String[2];
	        				if(value.indexOf(",")!=-1){
	        					String[] args = value.split(",");
	        					classNameArray[0] = args[0].trim();
	        					classNameArray[1] = args[1].trim();
	        				}else {
	        					classNameArray[0] = null;
	        					classNameArray[1] = value.trim();
	        				}
	        				protobufUrlMap.put(key, classNameArray);
	        			}
	        		}
	        	}
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public Object invokeObjectMethod(Class classInstance, Object object, String methodName, Class paramClass, Object paramObj) throws Exception{
    	Method m = null;
    	Object obj = null;
    	if(paramClass==null){
    	    m = classInstance.getMethod(methodName, null);
    	    obj = m.invoke(object, null);
    	}else {
    		m = classInstance.getMethod(methodName, paramClass);
    		obj = m.invoke(object, paramObj);
    	}
		
		return obj;
    }
    
    public void setFieldValue(Class classInstance, Object object, String fieldName, String value) {
    	try {
        	Field field = classInstance.getDeclaredField(fieldName+"_");
        	Object valueObj = transFieldValueType(value, field);
        	Class fieldClass = field.getType();
        	if(fieldClass.isAssignableFrom(String.class)){
        		fieldClass = String.class;
        	}
        	String setMethodName = "set"+fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
			invokeObjectMethod(classInstance, object, setMethodName, fieldClass, valueObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private Object transFieldValueType(String value, Field field){
    	if(field.getType()==Integer.TYPE){
    		return new Integer(value);
    	}if(field.getType()==Long.TYPE){
    		return new Long(value);
    	}if(field.getType()==Float.TYPE){
    		return new Float(value);
    	}if(field.getType()==Double.TYPE){
    		return new Double(value);
    	}if(field.getType()==Boolean.TYPE){
    		return new Boolean(value);
    	}if(field.getType()==Short.TYPE){
    		return new Boolean(value);
    	}else {
    		return value;
    	}
    }
 
    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
    	getLogger().info("----getDefaultParameters() called:"+System.currentTimeMillis());
//    	Arguments args = super.getDefaultParameters();
//    	if(args!=null&&args.getArgumentCount()>0){
//    		return args;
//    	}
//    	args = new Arguments();
//    	args.addArgument("URL", "${Server URL}");
//    	args.addArgument("actionMethod", "POST");
//    	args.addArgument("proto.", "${protobuf parameter}");
//    	args.addArgument("cookie[zpk]", "");
//        return args;
    	return null;
    }
    
    @Override
    public void setupTest(JavaSamplerContext context) {
    	getLogger().info("----setupTest() called");
    	super.setupTest(context);
    }
 
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
System.out.println("----debug info: start run");
        // pull parameters
        String url = context.getParameter( "URL" );
        String actionMethod = context.getParameter( "actionMethod" );
        if(actionMethod==null||actionMethod.isEmpty()){
        	actionMethod = HttpClientRequest.RequestMethod_POST;
        }
        
        Map<String, String> paramMap = new HashMap<String, String>();
        Map<String, String> nonProtoParamMap = new HashMap<String, String>();
        Iterator<String> itetor = context.getParameterNamesIterator();
        String paramKey = null;
        while(itetor.hasNext()){
        	paramKey = itetor.next();
        	paramMap.put(paramKey, context.getParameter(paramKey));
        }
        
        
        byte[] byteArray = null;
        // get all parameters into Map, find whether url is protobuf url
        String[] protobufClassArr = null;
        for(Map.Entry<String, String[]> entry : protobufUrlMap.entrySet()){
	    	if(url.indexOf(entry.getKey())!=-1){
	    		protobufClassArr = entry.getValue();
	    		break;
	    	}
	    }
        // handle protobuf input object
        if(protobufClassArr!=null && protobufClassArr[0]!=null && !"".equals(protobufClassArr[0])){
        	//TODO find protobuf class, then init it with paramMap, then transfer protobuf object to byteArray
        	// and put all non-protobuf fields into new parameter map
        	String param_key = null;
        	try {
				Class inputClass = Class.forName(protobufClassArr[0]);
				Class builderClass = Class.forName(protobufClassArr[0]+ClassName_Protobuf);
	    		Object obj = invokeObjectMethod(inputClass, null, "newBuilder", null, null);
	    		for(Map.Entry<String, String> entry : paramMap.entrySet()){
	    			param_key = entry.getKey();
	    			if(param_key.startsWith(Parameter_Protobuf_Prefix)){
	    				if(param_key.length()>Parameter_Protobuf_Prefix.length()){
	    					setFieldValue(builderClass, obj, param_key.substring(param_key.indexOf(Parameter_Protobuf_Prefix+1)), entry.getValue());
	    				}
	    			}else {
	    				nonProtoParamMap.put(param_key, entry.getValue());
	    			}
	    			
	    		}
	    		Object inputClassObject = invokeObjectMethod(builderClass, obj, "build", null, null);
	    		byteArray = (byte[])invokeObjectMethod(inputClassObject.getClass(), inputClassObject, "toByteArray", null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }else {
        	nonProtoParamMap = paramMap;
        }
     
        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch
System.out.println("----debug info: begin to call");        
        try {
        	HttpClientRequest.ResponseResult responseResult = HttpClientRequest.request(url, actionMethod, nonProtoParamMap, byteArray);
            //
        	String resultJson = "";
        	if(responseResult.isSuccess()){
        		if(responseResult.isStream()){
        			// handle protobuf output object
        			if(protobufClassArr!=null && protobufClassArr[1]!=null && !"".equals(protobufClassArr[1])){
        				// using protoBuf handle to json
        				Class outClass = Class.forName(protobufClassArr[1]);
        				InputStream responseStream = responseResult.getResultStream();
        				Object responseProtobufObj = invokeObjectMethod(outClass, null, "parseFrom", InputStream.class, responseStream);
        				//resultJson = com.google.protobuf.util.JsonFormat.printer().print((MessageOrBuilder)responseProtobufObj);
        				resultJson = JsonFormat.printToString((Message)responseProtobufObj);
        			}else {
        				resultJson = "error: response is stream, but no protobuf mapping for this url!";
        			}
        		}else {
        			resultJson = responseResult.getResultString();
        		}
        	}else {
        		resultJson = responseResult.getErrorMessage();
        	}
        	
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(responseResult.isSuccess());
            //result.setResponseMessage(resultJson);
            result.setResponseData(resultJson, "UTF-8");
            if(responseResult.isSuccess()){
                result.setResponseCodeOK(); // 200 code
            }else {
                result.setResponseCode(String.valueOf(responseResult.getStatuscode()));
            }
        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( false );
            result.setResponseMessage( "Exception: " + e );
 
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), "UTF-8");
            result.setDataType( org.apache.jmeter.samplers.SampleResult.TEXT );
            result.setResponseCode("500");
        }
 
        return result;
    }

	public static Map<String, String[]> getProtobufUrlMap() {
		return protobufUrlMap;
	}
    
    
}
