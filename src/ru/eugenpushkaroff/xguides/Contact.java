package ru.eugenpushkaroff.xguides;

import java.io.Serializable;

public class Contact implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String Comment;
	private String Image;
	private String ID;
	
	public Contact(String name, String Comment, String Image, String ID) {
		super();
		this.name = name;
		this.Comment = Comment;
		this.Image = Image;
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
		
	public String getComment() {
		return Comment;
	}
	
	public void setComment(String Comment) {
		this.Comment = Comment;
	}

	public String getImage() {
		return Image;
	}

	public void setImage(String Image) {
		this.Image = Image;
	}
	
	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}
	

}
