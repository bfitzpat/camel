/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dozer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.camel.spi.ClassResolver;

/**
 * Allows a user to customize a field mapping using a POJO that is not
 * required to extend/implement Dozer-specific classes.
 */
public class CustomMapper extends BaseConverter {
    
    private ClassResolver resolver;
    
    public CustomMapper(ClassResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    public Object convert(Object existingDestinationFieldValue, 
            Object sourceFieldValue, 
            Class<?> destinationClass,
            Class<?> sourceClass) {
        try {
            return mapCustom(sourceFieldValue);
        } finally {
            done();
        }
    }
    
    Method selectMethod(Class<?> customClass, Object fromType) {
        Method method = null;
        for (Method m : customClass.getDeclaredMethods()) {
            if (m.getReturnType() != null 
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0].isAssignableFrom(fromType.getClass())) {
                method = m;
                break;
            }
        }
        return method;
    }

    private Method validateParameters(Class<?> customClass, String operation, ArrayList<String> parameters) {
        if (operation != null && !parameters.isEmpty()) {
        	Method[] allMethods = customClass.getMethods();
        	for (int i = 0; i < allMethods.length; i++) {
        		Method testMethod = allMethods[i];
        		if (testMethod.getName().equals(operation)) {
        			Class<?>[] parmsFromMethod = testMethod.getParameterTypes();
        			if ( parmsFromMethod.length == parameters.size() + 1 ) {
        				int validParms = 0;
        				for (int j = 0; j < parameters.size(); j++) {
        					String parts[] = parameters.get(j).split("=");
        					if (parts.length == 2) {
        						String parmTypeClass = parts[0];
        						Class<?> parmToTest = parmsFromMethod[j+1];
        						Class<?> customParm = resolver.resolveClass(parmTypeClass);
        						if (customParm != null && customParm.equals(parmToTest)) {
        							// keep going
        							validParms++;
        						}
        					}
        				}
        				if (validParms == parameters.size()) {
        					return testMethod;
        				}
        			}
        		}
        	}
        }
    	return null;
    }
    
    private Object invokeParmsMethod(Method toInvoke, 
    		Object customMapObj, Object source, ArrayList<String> parameters) throws Exception {
		ArrayList<Object> parmValues = new ArrayList<Object>(parameters.size());
		for (int j = 0; j < parameters.size(); j++) {
			String parts[] = parameters.get(j).split("=");
			if (parts.length == 2) {
				String parmTypeClass = parts[0];
				String value = parts[1];
				Class<?> customParm = resolver.resolveClass(parmTypeClass);
				Constructor<?> customParmConstructor = customParm.getConstructor(String.class);
				Object parmValue = customParmConstructor.newInstance(value);
				parmValues.add(parmValue);
			}
		}
		if (parameters.size() == 1) {
			return toInvoke.invoke(customMapObj, source, parmValues.get(0));
		}
		if (parameters.size() == 2) {
			return toInvoke.invoke(customMapObj, source, parmValues.get(0), parmValues.get(1));
		}
		if (parameters.size() == 3) {
			return toInvoke.invoke(customMapObj, source, parmValues.get(0), parmValues.get(1), parmValues.get(2));
		}
    	return null;
    }
    
    Object mapCustom(Object source) {
        Object customMapObj;
        Method mapMethod = null;
        
        // The converter parameter is stored in a thread local variable, so 
        // we need to parse the parameter on each invocation
        // ex: custom-converter-param="org.example.MyMapping,map"
        // className = org.example.MyMapping
        // operation = map
        String[] params = getParameter().split(",");
        String className = params[0];
        String operation = params.length > 1 ? params[1] : null;
        
        // now attempt to process any additional parameters passed along
        // ex: custom-converter-param="org.example.MyMapping,substring,beginindex=3,endIndex=10"
        // className = org.example.MyMapping
        // operation = substring
        // parameters = ["beginindex=3","endIndex=10"]
        ArrayList<String> parameters = new ArrayList<String>();
        if (params.length > 2) {
        	for (int i = 2; i < params.length; i++) {
        		parameters.add(params[i]);
        	}
        }
        
        try {
            Class<?> customClass = resolver.resolveClass(className);
            customMapObj = customClass.newInstance();
            
            // If a specific mapping operation has been supplied use that
            if (operation != null && !parameters.isEmpty()) {
            	mapMethod = validateParameters(customClass, operation, parameters);
            } else if (operation != null) {
                mapMethod = customClass.getMethod(operation, source.getClass());
            } else {
                mapMethod = selectMethod(customClass, source);
            }
        } catch (Exception cnfEx) {
            throw new RuntimeException("Failed to load custom mapping", cnfEx);
        }
        
        // Verify that we found a matching method
        if (mapMethod == null) {
            throw new RuntimeException("No eligible custom mapping methods in " + className);
        }
        
        // Invoke the custom mapping method
        try {
        	if (parameters != null && parameters.size() > 0) {
        		return invokeParmsMethod(mapMethod, customMapObj, source, parameters);
        	} else {
        		return mapMethod.invoke(customMapObj, source);
        	}
        } catch (Exception ex) {
            throw new RuntimeException("Error while invoking custom mapping", ex);
        }
    }
}
