package com.MeadowEast.xue;

public class CardStatus {
	private int index;
	private int level;
	private int previous;
	public CardStatus(int index, int level){
		this.level = level;
		this.index = index;
		this.previous = level;
	}
	public int getIndex(){
		return index;
	}
	public int getLevel(){
		return level;
	}
	public void wrong(){
		if (level > 0){
			previous = level;
			level -= 1;
		}
	}
	public void right(){
		if (level < 4){
			previous = level;
			level += 1;
		}
	}
	public String toString(){
		return "CardStatus: index="+index+" level="+level;
	}
}
