package net.toxbank.client.exceptions;

public class InvalidInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6642755413170643431L;
	
	 public InvalidInputException(String message) {
		 super(String.format("Invalid input: %s",message));
	 }
}
