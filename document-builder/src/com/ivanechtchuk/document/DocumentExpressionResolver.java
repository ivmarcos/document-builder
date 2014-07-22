package com.ivanechtchuk.document;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

class DocumentExpressionResolver {
	
	static Logger logger = Logger.getLogger(DocumentExpressionResolver.class.getName());
	
	private final DocumentWrapper wrapper;
	private Map<String, Method> methodsMap;
	private Map<Method, Object[]> parametersMap;
	
	public DocumentExpressionResolver(DocumentWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public Object resolveExpression(Object instance, String ELExpression) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<?> instanceClass = instance.getClass();
		String[] expressions = ELExpression.replace("{"+wrapper.getDocument().getVar()+".", "").replaceAll("\\{|\\}", "").replace(".","_").split("_");
		Field field;
		Object instanceValue = instance;
		for(String ex : expressions) {
			if (!isBindingValueExpression(ELExpression)) return ELExpression;
			if (isMethodExpression(ex)) return findValueByMethodExpression(ex, instanceValue);
			try {
				field = instanceClass.getDeclaredField(ex);
			} catch (NoSuchFieldException e) {
				throw new DocumentExpressionException("Error in expression syntax. No such field "+ex+" in class "+instanceClass.getName());
			}
			field.setAccessible(true);
			try{
				instanceValue = field.get(instanceValue);
			}catch(NullPointerException e) {
				return null;
			}
			instanceClass = field.getType();
		}
		return instanceValue;
	}
	 
	public Object resolve(Object instance, String[] expressions) throws SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
		if (expressions.length>1) {
			StringBuilder builder = new StringBuilder();
			for (String expression : expressions) {
				Object expressionValue = resolveExpression(instance, expression);
				if (expressionValue!=null) builder.append(expressionValue);
			}
			return builder.toString();
		}else {
			return resolveExpression(instance, expressions[0]);
		}
	}
	
	private boolean isMethodExpression(String expression) {
		return expression.endsWith(")");
	}
	
	private boolean isBindingValueExpression(String expression) {
		return expression.startsWith("{");
	}
	
	private Object[] findParametersFromMethodExpression(Method method, String expression){
		if (parametersMap==null) {
			parametersMap = new HashMap<Method, Object[]>();
		}
		Object[] parameters = parametersMap.get(method);
		if (parameters==null) {
			if (method.getParameterTypes().length==0) return null;
			parameters = new Object[] {};
			int firstIndex = expression.indexOf("(");
			int endIndex = expression.indexOf(")");
			int i = 0;
			for (String param : expression.substring(firstIndex, endIndex).split(",")) {
				Class<?> parameterClass = method.getParameterTypes()[i];
				if (parameterClass==int.class||parameterClass==Integer.class) {
					parameters[i] = Integer.valueOf(param);
				}else if (parameterClass==String.class) {
					parameters[i] = param;
				}else if (parameterClass==Long.class||parameterClass==long.class) {
					parameters[i] = Long.valueOf(param);
				}else if (parameterClass==Double.class||parameterClass==double.class) {
					parameters[i] = Double.valueOf(param);
				}else if (parameterClass==BigDecimal.class) {
					parameters[i] = new BigDecimal(param);
				}
				i++;
			}
			parametersMap.put(method, parameters);
		}
		return parameters;
	}
	
	
	private Object findValueByMethodExpression(String expression, Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (methodsMap==null) methodsMap = new HashMap<String, Method>();
		Method method = methodsMap.get(expression);
		if (method==null) {
			int firstIndex = expression.indexOf("(");
			String methodName = expression.substring(0, firstIndex);
			for (Method methodClass : instance.getClass().getMethods()) {
				if (methodClass.getName().equals(methodName)) {
					method = methodClass;
					break;
				}
			}
			if (method==null) throw new DocumentParserException("No methods found by name "+methodName+ " in class "+instance.getClass().getName());
			methodsMap.put(expression, method);
		}
		logger.info("Invoking method "+expression);
		try {
			return method.invoke(instance, findParametersFromMethodExpression(method, expression));
		}catch(NullPointerException e) {
			return null;
		}
	}
}
