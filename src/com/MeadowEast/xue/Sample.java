package com.MeadowEast.xue;

import java.util.Random;

public class Sample {
	// Return an array of m distinct integers randomly selected from 0..n-1 
	static public int [] sample(int m, int n){
		int [] mArray = new int [m];
		int [] nArray = new int [n];
		for (int i = 0; i < n; ++i){
			nArray[i] = i;
		}
		Random rnd = new Random();
		for (int i = 0; i < m; ++i){
			//get a random item from remaining range
			int current = rnd.nextInt(n-i);
			//add it to mArray
			mArray[i] = nArray[current];
			//swap it out of range
			int tmp = nArray[n-i-1];
			nArray[n-i-1] = nArray[current];
			nArray[current] = tmp;			
		}
		return mArray;
	}
}
