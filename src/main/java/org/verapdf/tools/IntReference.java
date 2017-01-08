package org.verapdf.tools;

/**
 * Represents int value that can be passed by reference.
 *
 * @author Sergey Shemyakov
 */
public class IntReference {

    private int[] num = new int[1];

    /**
     * Default constructor that sets integer to 0.
     */
    public IntReference() {
        this(0);
    }

    /**
     * Constructor that sets integer to given value.
     *
     * @param num is integer that will be stored.
     */
    public IntReference(int num) {
        this.num[0] = num;
    }

    /**
     * @return integer that is represented by this reference.
     */
    public int get() {
        return num[0];
    }

    /**
     * Increments internal integer.
     */
    public void increment() {
        this.num[0]++;
    }

    /**
     * Decrements internal integer.
     */
    public void decrement() {
        this.num[0]--;
    }

    /**
     * Sets internal integer to given value.
     *
     * @param num is value.
     */
    public void set(int num) {
        this.num[0] = num;
    }

    /**
     * Checks if internal integer equals to another int.
     *
     * @return true if internal integer equals to passed value.
     */
    public boolean equals(int num) {
        return this.num[0] == num;
    }
}
