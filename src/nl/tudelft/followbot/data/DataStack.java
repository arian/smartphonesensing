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

	public interface Filter<T> {
		public boolean filter(T t);
	}

	public interface Map<X, Y> {
		public Y map(X t);
	}

	public interface Each<T> {
		public void each(T t);
	}

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
	 * Return the last pushed item
	 * 
	 * @return T
	 */
	public T peek() {
		return stack.get((pointer + size - 1) % size);
	}

	/**
	 * Filters the datastack
	 * 
	 * @param pred
	 *            filter method
	 * @return filtered stack
	 */
	public DataStack<T> filter(Filter<T> pred) {
		DataStack<T> d = new DataStack<T>(size);
		for (int i = 0; i < size; i++) {
			T x = get(i);
			if (pred.filter(x)) {
				d.push(x);
			}
		}
		return d;
	}

	/**
	 * Filters the datastack
	 * 
	 * @param <Y>
	 * 
	 * @param <L>
	 * 
	 * @param pred
	 *            filter method
	 * @return filtered stack
	 */
	public <Y> DataStack<Y> map(Map<T, Y> pred) {
		DataStack<Y> d = new DataStack<Y>(size);
		for (int i = 0; i < size; i++) {
			d.push(pred.map(get(i)));
		}
		return d;
	}

	public DataStack<T> each(Each<T> pred) {
		for (int i = 0; i < size; i++) {
			pred.each(get(i));
		}
		return this;
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
