package com.protobuftest;

import java.io.Serializable;
import java.util.Map;

import com.protobuftest.ProtobufRestfulSample;
 
public class ProtobufRestfulSampleTest implements Serializable {
        
    public static final String Parameter_Protobuf_Prefix = "proto.";
	
	public static void main(String[] args) throws Exception{
		ProtobufRestfulSample instance = new ProtobufRestfulSample();
    	for(Map.Entry<String, String[]> entry : instance.getProtobufUrlMap().entrySet()){
/*    		System.out.println(entry.getKey()+" : "+entry.getValue()[0]+"|"+entry.getValue()[1]);
    		Class inputClass = Class.forName(entry.getValue()[0]);
    		Class builderClass = Class.forName(entry.getValue()[0]+"$Builder");
    		
    		
    		Object obj = instance.invokeObjectMethod(inputClass, null, "newBuilder", null, null);
    		String fieldName = "hostName"; // hostName is an object
    		String value = "testpeter2";
    		
    		instance.setFieldValue(builderClass, obj, fieldName, value);
    		instance.setFieldValue(builderClass, obj, "duration", "10");
    		((com.saasbee.webapp.proto.MeetingProtos.MeetingProto.Builder)obj).setHostName(value);
    		((com.saasbee.webapp.proto.MeetingProtos.MeetingProto.Builder)obj).setDuration(new Integer("10"));
    		
    		System.out.println(((com.saasbee.webapp.proto.MeetingProtos.MeetingProto.Builder)obj).getHostName());
    		System.out.println(((com.saasbee.webapp.proto.MeetingProtos.MeetingProto.Builder)obj).getDuration());
    		
    		Object inputClassObject = instance.invokeObjectMethod(builderClass, obj, "build", null, null);
    	    byte[] array = (byte[])instance.invokeObjectMethod(inputClassObject.getClass(), inputClassObject, "toByteArray", null, null);
    		//byte[] array = ((com.saasbee.webapp.proto.MeetingProtos.MeetingProto.Builder)obj).build().toByteArray();
    		System.out.println("length:"+array.length);*/

    	}
    }
    
//    private Object createProtobufOjbect(Class classInstance) throws Exception{
//    	Method m = classInstance.getMethod("newBuilder", null);
//		Object obj = m.invoke(null, null);
//		return obj;
//    }
    
}
