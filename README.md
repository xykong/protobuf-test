# protobuf-test
This project is for testing Protobuf api in Apache Jmeter. Google Protobuf is easy to use, but is not good for testing in auto testing tool, e.g: apache Jmeter.
By using this project, you can write protobuf api testing case just like the other http apis, you need use 'Java Request' to test protobuf api.

#Usage:

1. edit this file: protoUrlMap.properties in project. add your protobuf api mapping, the rule is as below:
   url keys are protobuf api, they will be used to map protobuf object
   support input object is protobuf object and response is protobuf object, so the mapping value will include two classess names.
   the first class name is: input protobuf class, the second class name is: out protobuf class
   if input or output is not protobuf object, just leave empty

2. after edit the properties file, build this project into jar using maven:  mvn build
3. put this jar: protobuf-test-1.0.0.jar and protobuf-java-2.6.1.jar and protobuf-java-format-1.2.jar into apache jmeter ext folder: 
   {apache-jmeter-path}/lib/ext
4. then can run jmeter. 
   Please Note: if you want to use json in jmeter, please install apache jmeter Json plug-in, please see this  wiki: https://jmeter-plugins.org/wiki/JSONPathAssertion/?utm_source=jmeter&utm_medium=helplink&utm_campaign=JSONPathAssertion
5. in Jmeter, add "Sample-->Java Request", then will find "com.protobuftest.ProtobufRestfulSample" in the classname dropdown list.
   below is parameter usage:
   a. if the request parameter is a field in protobuf object, please add this prefix: proto.     e.g.: proto.username=peter
   b. if the request parameter is a cookie key/value, please use this format: cookie[key]=aabbcc
   c. for normal request parameter, just use them like normal Jmeter Http Request.
6. you can add any assert for the response just like Http Request

#Notes:

1. when generated protobuf java file, please not add: option optimize_for = LITE_RUNTIME;  --- this will lose the ability of reflection, so protobuf-java cannot work.
