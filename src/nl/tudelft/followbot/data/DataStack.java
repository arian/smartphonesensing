package nl.tudelft.followbot.data;

import java.util.ArrayList;

/**
 * This is a ring data structure with a fixed size. It fills the structure first
 * and when it's full it will overwrite the oldest value.
 * 
 * @author Arian Stolwijk
 * 
 * @param <T>
 */
public class DataStack<T> {

	private int max = 0;
	private int size = 0;
	private int pointer = 0;
	private final ArrayList<T> stack;

	public DataStack(int n) {
		max = n;
		stack = new ArrayList<T>();
	}

	/**
	 * Push a new item to the stack. If the stack is full it will remove the
	 * oldest value.
	 * 
	 * @param item
	 * @return this object
	 */
	public DataStack<T> push(T item) {
		if (size < max) {
			stack.add(item);
			size++;
		} else {
			stack.set(pointer, item);
		}
		pointer = (pointer + 1) % max;
		return this;
	}

	/**
	 * 
	 * @return total size of the stack
	 */
	public int getSize() {
		return size;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public T get(int i) {
		if (i >= size) {
			throw new IndexOutOfBoundsException();
		}
		return stack.get((i + pointer + size) % size);
	}

	/**
	 * 
	 * @return array of all doubles, with the oldest value in index 0.
	 */
	public ArrayList<T> toArray() {
		ArrayList<T> s = new ArrayList<T>();
		for (int i = 0; i < size; i++) {
			s.add(get(i));
		}
		return s;
	}

}
