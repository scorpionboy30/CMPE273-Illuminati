package misc;

import org.msgpack.annotation.Message;

@Message
public class CommandWrapper {

	private String methodName;

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName 
	 * 				the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
}
