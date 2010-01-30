package com.inexas.wikibi;

import java.util.*;

public class RandomNames implements Iterable<RandomNames.FullName> {
	private final static String[] firstNames = {
	        "Abigail", "Alexander", "Alexis", "Alyssa", "Andrew",
	        "Anna", "Anthony", "Ashley", "Ava", "Benjamin",
	        "Brianna", "Chloe", "Christian", "Christopher", "Daniel",
	        "David", "Dylan", "Elizabeth", "Ella", "Emily",
	        "Emma", "Ethan", "Grace", "Hannah", "Isabella",
	        "Jacob", "James", "John", "Jonathan", "Joseph",
	        "Joshua", "Kayla", "Lauren", "Madison", "Matthew",
	        "Mia", "Michael", "Natalie", "Nathan", "Nicholas",
	        "Noah", "Olivia", "Ryan", "Samantha", "Samuel",
	        "Sarah", "Sophia", "Taylor", "Tyler", "William"
	        };
	private final static String[] lastNames = {
	        "Adams", "Allen", "Anderson", "Baker", "Brown",
	        "Campbell", "Carter", "Clark", "Collins", "Davis",
	        "Edwards", "Evans", "Garcia", "Gonzalez", "Green",
	        "Hall", "Harris", "Hernandez", "Hill", "Jackson",
	        "Johnson", "Jones", "King", "Lee", "Lewis",
	        "Lopez", "Martin", "Martinez", "Miller", "Mitchell",
	        "Moore", "Nelson", "Parker", "Perez", "Phillips",
	        "Roberts", "Robinson", "Rodriguez", "Scott", "Smith",
	        "Taylor", "Thomas", "Thompson", "Turner", "Walker",
	        "White", "Williams", "Wilson", "Wright", "Young"
	        };

	public static class FullName {
		public final String firstName, lastName;

		public FullName(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
	}
	
	private class MyIterator implements Iterator<FullName> {
		private int count;
		public MyIterator(int count) {
			this.count = count;
        }
		public boolean hasNext() {
			final boolean result;
			if(count > 0) {
				count--;
				result = true;
			} else if(count == -1) {
				return true;
			} else {
				result = false;
			}
			return result;
		}
		public FullName next() {
			return new FullName(
			        firstNames[random.nextInt(firstNames.length)],
			        lastNames[random.nextInt(lastNames.length)]);
		}
		public void remove() {
			throw new RuntimeException("Not supported");
		}
	}

	private final Random random = new Random();
	private final int iterations;

	public static void main(String[] args) {
	    for(final FullName fullName : new RandomNames(10)) {
	    	System.out.println(fullName.firstName + ' ' + fullName.lastName);
	    }
    }

	/**
	 * Iterator for ever
	 */
	public RandomNames() {
		iterations = -1;
    }
	
	public RandomNames(int iterations) {
		this.iterations = iterations;
    }
	
    public Iterator<FullName> iterator() {
		return new MyIterator(iterations);
    }

}
