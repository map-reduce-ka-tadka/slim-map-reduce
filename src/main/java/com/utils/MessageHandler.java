package com.utils;

import java.io.Serializable;

/**
 * This Class handles messages between server and clients.
 * @author Abhijeet Sharma
 * @version 1.0
 * @since April 19, 2016
 */
public class MessageHandler implements Serializable {

	private static final long serialVersionUID = 1L;		
	private int opcode;
	private String message;
	private int status;
	
	/**
	 * @param opcode
	 * @param message
	 * @param status [-1 => Failure, 0 => Wait, 1 => Success]
	 */
	public MessageHandler(int opcode, String message, int status) {
		this.opcode = opcode;
		this.message = message;
		this.status = status;
	}
	
	/**
	 * @return the code
	 */
	public int getCode() {
		return opcode;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(int opcode) {
		this.opcode = opcode;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Displays the fields of the object in String format
	 */
	@Override
	public String toString() {
		return "MessageHandler [opcode=" + opcode + ", message=" + message + ", status=" + status + "]";
	}

	/**
	 * Generates the Hashcode of the Object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + opcode;
		result = prime * result + status;
		return result;
	}

	/**
	 * Compare the current object with another object
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageHandler other = (MessageHandler) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (opcode != other.opcode)
			return false;
		if (status != other.status)
			return false;
		return true;
	}	
}