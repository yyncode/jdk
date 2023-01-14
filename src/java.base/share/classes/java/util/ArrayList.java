/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.ArraysSupport;

/**
 * Resizable-array implementation of the {@code List} interface.  Implements
 * all optional list operations, and permits all elements, including
 * {@code null}.  In addition to implementing the {@code List} interface,
 * this class provides methods to manipulate the size of the array that is
 * used internally to store the list.  (This class is roughly equivalent to
 * {@code Vector}, except that it is unsynchronized.)
 *
 * <p>The {@code size}, {@code isEmpty}, {@code get}, {@code set},
 * {@code iterator}, and {@code listIterator} operations run in constant
 * time.  The {@code add} operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time.  All of the other operations
 * run in linear time (roughly speaking).  The constant factor is low compared
 * to that for the {@code LinkedList} implementation.
 *
 * <p>Each {@code ArrayList} instance has a <i>capacity</i>.  The capacity is
 * the size of the array used to store the elements in the list.  It is always
 * at least as large as the list size.  As elements are added to an ArrayList,
 * its capacity grows automatically.  The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized
 * time cost.
 *
 * <p>An application can increase the capacity of an {@code ArrayList} instance
 * before adding a large number of elements using the {@code ensureCapacity}
 * operation.  This may reduce the amount of incremental reallocation.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an {@code ArrayList} instance concurrently,
 * and at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally.  (A structural modification is
 * any operation that adds or deletes one or more elements, or explicitly
 * resizes the backing array; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * <p id="fail-fast">
 * The iterators returned by this class's {@link #iterator() iterator} and
 * {@link #listIterator(int) listIterator} methods are <em>fail-fast</em>:
 * if the list is structurally modified at any time after the iterator is
 * created, in any way except through the iterator's own
 * {@link ListIterator#remove() remove} or
 * {@link ListIterator#add(Object) add} methods, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of
 * concurrent modification, the iterator fails quickly and cleanly, rather
 * than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements in this list
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    @java.io.Serial
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * Default initial capacity.
     * 默认初始化容量 10
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     * 用于空实例的共享空数组实例
     * 在 {@link #ArrayList(int)} 或 {@link #ArrayList(Collection)} 构造方法中，
     * 如果传入的初始化大小或者集合大小为 0 时，将 {@link #elementData} 指向它。
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     *
     * 共享的空数组对象，用于 {@link #ArrayList()} 构造方法。
     * 通过使用该静态变量，和 {@link #EMPTY_ELEMENTDATA} 区分开来，在第一次添加元素时。
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    /**
     * 存储 ArrayList 元素的数组缓冲区。
     * ArrayList 的容量就是这个数组缓冲区的长度。
     * 添加第一个元素时，任何具有 elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的空 ArrayList 都将扩展为 DEFAULT_CAPACITY。
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * The size of the ArrayList (the number of elements it contains).
     * ArrayList 的大小（它包含的元素数）。
     * 注意，size 代表的是 ArrayList 已使用 elementData 的元素的数量，
     * 对于开发者看到的 #size() 也是该大小。并且，当我们添加新的元素时，
     * 恰好其就是元素添加到 elementData 的位置（下标）。
     * 当然，我们知道 ArrayList 真正的大小是 elementData 的大小。
     * @serial
     */
    private int size;

    /**
     *
     * 根据传入的初始化容量，创建 ArrayList 数组。
     * 如果我们在使用时，如果预先知道数组大小，一定要使用该构造方法，可以避免数组扩容提升性能，同时也是合理使用内存。
     *
     * 比较特殊的是，如果初始化容量为 0 时，使用 EMPTY_ELEMENTDATA 空数组。在添加元素的时候，会进行扩容创建需要的数组。
     *
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            //初始化容量大于 0 时，创建 Object 数组
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            //初始化容量等于 0 时，使用 EMPTY_ELEMENTDATA 对象
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            //初始化容量小于 0 时，抛出 IllegalArgumentException 异常
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     * 构造初始容量为 10 的空列表。
     */
    public ArrayList() {
        //为什么初始化为 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 这个空数组？
        //答：ArrayList 考虑到节省内存，一些使用场景下仅仅是创建了 ArrayList 对象，实际并未使用。所以，ArrayList 优化成初始化是个空数组，在首次添加元素时，才真正初始化为容量为 10 的数组。
        //为什么单独声明了 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 空数组，而不直接使用 EMPTY_ELEMENTDATA 呢？
        //答：DEFAULTCAPACITY_EMPTY_ELEMENTDATA 首次扩容为 10 ，而 EMPTY_ELEMENTDATA 按照 1.5 倍扩容从 0 开始而不是 10 。
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * 使用传入的 c 集合，作为 ArrayList 的 elementData 。
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public ArrayList(Collection<? extends E> c) {
        //将 c 转换成 Object 数组
        Object[] a = c.toArray();
        // 如果数组大小大于 0
        if ((size = a.length) != 0) {
            if (c.getClass() == ArrayList.class) {
                elementData = a;
            } else {
                //如果集合元素不是 ArrayList 类型，则会创建新的 Object[] 数组，并将 elementData 赋值到其中，最后赋值给 elementData 。
                elementData = Arrays.copyOf(a, size, Object[].class);
            }
        } else {
            // replace with empty array.
            //如果数组大小等于 0 ，则使用 EMPTY_ELEMENTDATA 。
            elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * Trims the capacity of this {@code ArrayList} instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an {@code ArrayList} instance.
     * 会创建大小恰好够用的新数组，并将原数组复制到其中
     */
    public void trimToSize() {
        // 增加修改次数
        modCount++;
        // 如果有多余的空间，则进行缩容
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA // 大小为 0 时，直接使用 EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size); // 大小大于 0 ，则创建大小为 size 的新数组，将原数组复制到其中
        }
    }

    /**
     * Increases the capacity of this {@code ArrayList} instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     * 保证 elementData 数组容量至少有 minCapacity,我们可以将这个方法理解成主动扩容。
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length // 如果 minCapacity 大于数组的容量
            && !(elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
                 && minCapacity <= DEFAULT_CAPACITY)) { // 如果 elementData 是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的时候，需要最低 minCapacity 容量大于 DEFAULT_CAPACITY ，因为实际上容量是 DEFAULT_CAPACITY 。
            // 数组修改次数加一
            modCount++;
            // 扩容
            grow(minCapacity);
        }
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if minCapacity is less than zero
     */
    private Object[] grow(int minCapacity) {
        int oldCapacity = elementData.length;
        // <2> 如果原容量大于 0 ，或者数组不是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 时，计算新的数组大小，并创建扩容
        if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            // 计算新的数组大小。
            // 一般情况下，从 oldCapacity >> 1 可以看处，是 1.5 倍扩容。
            // 但是会有两个特殊情况：
            // 1）初始化数组要求大小为 0 的时候，0 >> 1 时（>> 1 为右移操作，相当于除以 2）还是 0 ，此时使用 minCapacity 传入的 1 。
            // 2）在下文中，我们会看到添加多个元素，此时传入的 minCapacity 不再仅仅加 1 ，而是扩容到 elementData 数组恰好可以添加下多个元素，而该数量可能会超过当前 ArrayList 0.5 倍的容量。
            int newCapacity = ArraysSupport.newLength(oldCapacity,
                    minCapacity - oldCapacity, /* minimum growth */
                    oldCapacity >> 1           /* preferred growth */);
            return elementData = Arrays.copyOf(elementData, newCapacity);
            // <3> 如果是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 数组，直接创建新的数组即可。如果无参构造方法使用 EMPTY_ELEMENTDATA 的话，无法实现该效果了。
        } else {
            return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
        }
    }

    /**
     * 扩容数组，并返回它。整个的扩容过程，首先创建一个新的更大的数组，一般是 1.5 倍大小
     * @return
     */
    private Object[] grow() {
        //调用 #grow(int minCapacity) 方法，要求扩容后至少比原有大 1 。因为是最小扩容的要求，实际是允许比它大。
        return grow(size + 1);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * 如果此列表包含指定的元素，则返回 true。
     * 更正式地说，当且仅当此列表包含至少一个元素 e 使得 Objects.equals（o， e） 返回 true。
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     *
     * 返回此列表中指定元素第一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最低索引i使得Objects.equals(o, get(i)) ，如果没有这样的索引则返回 -1。
     */
    public int indexOf(Object o) {
        return indexOfRange(o, 0, size);
    }

    int indexOfRange(Object o, int start, int end) {
        Object[] es = elementData;
        // o 为 null 的情况
        if (o == null) {
            for (int i = start; i < end; i++) {
                if (es[i] == null) {
                    return i;
                }
            }
        } else {
            // o 非 null 的情况
            for (int i = start; i < end; i++) {
                if (o.equals(es[i])) {
                    return i;
                }
            }
        }
        // 找不到，返回 -1
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     *
     * 返回此列表中指定元素最后一次出现的索引，如果此列表中不包含该元素，则返回 -1。
     * 更正式地说，返回最高索引 i，使得 Objects.equals（o， get（i）），如果没有这样的索引，则返回 -1。
     */
    public int lastIndexOf(Object o) {
        return lastIndexOfRange(o, 0, size);
    }

    int lastIndexOfRange(Object o, int start, int end) {
        Object[] es = elementData;
        // o 为 null 的情况
        if (o == null) {
            for (int i = end - 1; i >= start; i--) { // 倒序
                if (es[i] == null) {
                    return i;
                }
            }
        } else {
            // o 非 null 的情况
            for (int i = end - 1; i >= start; i--) { // 倒序
                if (o.equals(es[i])) {
                    return i;
                }
            }
        }
        // 找不到，返回 -1
        return -1;
    }

    /**
     * Returns a shallow copy of this {@code ArrayList} instance.  (The
     * elements themselves are not copied.)
     *
     * @return a clone of this {@code ArrayList} instance
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * 返回一个数组，该数组按正确的顺序（从第一个元素到最后一个元素）包含此列表中的所有元素。
     *
     * 将 ArrayList 转换成 [] 数组。
     *
     * 返回的是 Object[] 类型噢。
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.  If the list fits in the
     * specified array, it is returned therein.  Otherwise, a new array is
     * allocated with the runtime type of the specified array and the size of
     * this list.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.  (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     *
     * 以正确的顺序（从第一个元素到最后一个元素）返回一个包含此列表中所有元素的数组；
     * 返回数组的运行时类型是指定数组的类型。如果列表适合指定的数组，则在其中返回。
     * 否则，将使用指定数组的运行时类型和此列表的大小分配一个新数组。
     * 如果列表适合指定的数组并有剩余空间（即，数组的元素多于列表），则紧跟在集合末尾的数组中的元素将设置为null 。
     * （仅当调用者知道列表不包含任何空元素时，这才有助于确定列表的长度。）
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // <1> 如果传入的数组小于 size 大小，则直接复制一个新数组返回
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        // <2> 将 elementData 复制到 a 中
        System.arraycopy(elementData, 0, a, 0, size);
        // <2.1> 如果传入的数组大于 size 大小，则将 size 赋值为 null
        if (a.length > size)
            a[size] = null;
        // <2.2> 返回 a
        return a;
    }

    // Positional Access Operations

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    @SuppressWarnings("unchecked")
    static <E> E elementAt(Object[] es, int index) {
        return (E) es[index];
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * 返回此列表中指定位置的元素。
     *
     * 随机访问 index 位置的元素，时间复杂度为 O(1) 。
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        // 校验 index 不要超过 size
        Objects.checkIndex(index, size);
        // 获得 index 位置的元素
        return elementData(index);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * 用指定元素替换此列表中指定位置的元素。
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        // 校验 index 不要超过 size
        Objects.checkIndex(index, size);
        // 获得 index 位置的原元素
        E oldValue = elementData(index);
        // 修改 index 位置为新元素
        elementData[index] = element;
        // 返回 index 位置的原元素
        return oldValue;
    }

    /**
     * This helper method split out from add(E) to keep method
     * bytecode size under 35 (the -XX:MaxInlineSize default value),
     * which helps when add(E) is called in a C1-compiled loop.
     */
    private void add(E e, Object[] elementData, int s) {
        // <2> 如果容量不够，进行扩容
        // 备注：如果元素添加的位置就超过末尾（数组下标是从 0 开始，而数组大小比最大下标大 1），说明数组容量不够，需要进行扩容，那么就需要调用 #grow() 方法，进行扩容。
        if (s == elementData.length)
            elementData = grow();
        // <3> 设置到末尾
        elementData[s] = e;
        // <4> 数量大小加一
        size = s + 1;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * 顺序添加单个元素到数组。
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        // <1> 增加数组修改次数
        // 备注：增加数组修改次数 modCount 。在父类 AbstractList 上，定义了 modCount 属性，用于记录数组修改次数。
        modCount++;
        // 添加元素
        add(e, elementData, size);
        // 返回添加成功
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * 在此列表中的指定位置插入指定元素。将当前位于该位置的元素（如果有）和任何后续元素向右移动（将其索引加一）。
     * 插入单个元素到指定位置。
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        // 校验位置是否在数组范围内
        rangeCheckForAdd(index);
        // 增加数组修改次数
        modCount++;
        // 如果数组大小不够，进行扩容
        final int s;
        Object[] elementData;
        if ((s = size) == (elementData = this.elementData).length)
            elementData = grow();
        // 将 index + 1 位置开始的元素，进行往后挪
        System.arraycopy(elementData, index,
                         elementData, index + 1,
                         s - index);
        // 设置到指定位置
        elementData[index] = element;
        // 数组大小加一
        size = s + 1;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * 移除此列表中指定位置的元素。将任何后续元素向左移动（从其索引中减去一个）
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        // 校验 index 不要超过 size
        Objects.checkIndex(index, size);
        final Object[] es = elementData;

        // 记录该位置的原值
        @SuppressWarnings("unchecked") E oldValue = (E) es[index];
        // <X>快速移除
        fastRemove(es, index);
        // 返回该位置的原值
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        // 如果是自己，直接返回相等
        if (o == this) {
            return true;
        }

        // 如果不为 List 类型，直接不相等
        if (!(o instanceof List)) {
            return false;
        }

        // 获得当前的数组修改次数
        final int expectedModCount = modCount;
        // ArrayList can be subclassed and given arbitrary behavior, but we can
        // still deal with the common case where o is ArrayList precisely
        // <X> 根据不同类型，调用不同比对的方法。主要考虑 ArrayList 可以直接使用其 elementData 属性，性能更优。
        boolean equal = (o.getClass() == ArrayList.class)
            ? equalsArrayList((ArrayList<?>) o)
            : equalsRange((List<?>) o, 0, size);
        // 如果修改次数发生改变，则抛出 ConcurrentModificationException 异常
        checkForComodification(expectedModCount);
        return equal;
    }

    boolean equalsRange(List<?> other, int from, int to) {
        final Object[] es = elementData;
        // 如果 to 大于 es 大小，说明说明发生改变，抛出 ConcurrentModificationException 异常
        if (to > es.length) {
            throw new ConcurrentModificationException();
        }
        // 通过迭代器遍历 other ，然后逐个元素对比
        var oit = other.iterator();
        for (; from < to; from++) {
            // 如果 oit 没有下一个，或者元素不相等，返回 false 不匹配
            if (!oit.hasNext() || !Objects.equals(es[from], oit.next())) {
                return false;
            }
        }
        // 通过 oit 是否遍历完。实现大小是否相等的效果
        return !oit.hasNext();
    }

    private boolean equalsArrayList(ArrayList<?> other) {
        // 获得 other 数组修改次数
        final int otherModCount = other.modCount;
        final int s = size;
        boolean equal;
        // 判断数组大小是否相等
        if (equal = (s == other.size)) {
            final Object[] otherEs = other.elementData;
            final Object[] es = elementData;
            // 如果 s 大于 es 或者 otherEs 的长度，说明发生改变，抛出 ConcurrentModificationException 异常
            if (s > es.length || s > otherEs.length) {
                throw new ConcurrentModificationException();
            }
            // 遍历，逐个比较每个元素是否相等
            for (int i = 0; i < s; i++) {
                if (!Objects.equals(es[i], otherEs[i])) {
                    equal = false;
                    break; // 如果不相等，则 break
                }
            }
        }
        // 如果 other 修改次数发生改变，则抛出 ConcurrentModificationException 异常
        other.checkForComodification(otherModCount);
        return equal;
    }

    private void checkForComodification(final int expectedModCount) {
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        // 获得当前的数组修改次数
        int expectedModCount = modCount;
        // 计算哈希值
        int hash = hashCodeRange(0, size);
        // 如果修改次数发生改变，则抛出 ConcurrentModificationException 异常
        checkForComodification(expectedModCount);
        return hash;
    }

    int hashCodeRange(int from, int to) {
        final Object[] es = elementData;
        // 如果 to 超过大小，则抛出 ConcurrentModificationException 异常
        if (to > es.length) {
            throw new ConcurrentModificationException();
        }
        // 遍历每个元素，* 31 求哈希。
        int hashCode = 1;
        for (int i = from; i < to; i++) {
            Object e = es[i];
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * {@code Objects.equals(o, get(i))}
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * 从此列表中移除第一次出现的指定元素（如果存在）。
     * 如果列表不包含该元素，则它不变。更正式地说，删除具有最低索引i的元素，这样Objects.equals(o, get(i)) （如果存在这样的元素）。
     * 如果此列表包含指定元素，则返回true （或者等效地，如果此列表因调用而更改）。
     *
     * 移除首个为 o 的元素，并返回是否移除到。
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean remove(Object o) {
        final Object[] es = elementData;
        final int size = this.size;
        // <Z> 寻找首个为 o 的位置
        int i = 0;
        found: {
            if (o == null) { // o 为 null 的情况
                for (; i < size; i++)
                    if (es[i] == null)
                        break found;
            } else { // o 非 null 的情况
                for (; i < size; i++)
                    if (o.equals(es[i]))
                        break found;
            }
            // 如果没找到，返回 false
            return false;
        }
        // 快速移除
        fastRemove(es, i);
        // 找到了，返回 true
        return true;
    }

    /**
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     *
     * 跳过边界检查并且不返回删除的值的私有删除方法。
     */
    private void fastRemove(Object[] es, int i) {
        // 增加数组修改次数
        modCount++;
        // <Y>如果 i 不是移除最末尾的元素，则将 i + 1 位置的数组往前挪
        final int newSize;
        if ((newSize = size - 1) > i) // -1 的原因是，size 是从 1 开始，而数组下标是从 0 开始。
            System.arraycopy(es, i + 1, es, i, newSize - i);
        // 将新的末尾置为 null ，帮助 GC
        es[size = newSize] = null;
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     * 从此列表中删除所有元素。此调用返回后列表将为空。
     */
    public void clear() {
        // 当前的数组修改次数+1
        modCount++;
        // 遍历数组，倒序设置为 null
        final Object[] es = elementData;
        for (int to = size, i = size = 0; i < to; i++)
            es[i] = null;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * 将指定集合中的所有元素追加到此列表的末尾，
     * 按照它们由指定集合的迭代器返回的顺序。
     * 如果在操作进行时修改了指定的集合，则此操作的行为是未定义的。
     * （这意味着如果指定的集合是这个列表并且这个列表是非空的，那么这个调用的行为是未定义的。）
     *
     * 批量添加多个元素。在我们明确知道会添加多个元素时，推荐使用该该方法而不是添加单个元素，避免可能多次扩容。
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        // 转成 a 数组
        Object[] a = c.toArray();
        // 增加修改次数
        modCount++;
        // 如果 a 数组大小为 0 ，返回 ArrayList 数组无变化
        int numNew = a.length;
        if (numNew == 0)
            return false;
        // <1> 如果 elementData 剩余的空间不够，则进行扩容。要求扩容的大小，至于能够装下 a 数组。如果要求扩容的空间太小，则扩容 1.5 倍。
        Object[] elementData;
        final int s;
        if (numNew > (elementData = this.elementData).length - (s = size))
            elementData = grow(s + numNew);
        // <2> 将 a 复制到 elementData 从 s 开始位置
        System.arraycopy(a, 0, elementData, s, numNew);
        // 数组大小加 numNew
        size = s + numNew;
        return true;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * 从指定位置开始，将指定集合中的所有元素插入到此列表中。
     * 将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。新元素将按照指定集合的迭代器返回的顺序显示在列表中。
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        // 校验位置是否在数组范围内
        rangeCheckForAdd(index);

        // 转成 a 数组
        Object[] a = c.toArray();
        // 增加数组修改次数
        modCount++;
        // 如果 a 数组大小为 0 ，返回 ArrayList 数组无变化
        int numNew = a.length;
        if (numNew == 0)
            return false;
        // 如果 elementData 剩余的空间不够，则进行扩容。要求扩容的大小，至于能够装下 a 数组。
        Object[] elementData;
        final int s;
        if (numNew > (elementData = this.elementData).length - (s = size))
            elementData = grow(s + numNew);

        // 【差异点】如果 index 开始的位置已经被占用，将它们后移
        int numMoved = s - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index,
                             elementData, index + numNew,
                             numMoved);
        // 将 a 复制到 elementData 从 s 开始位置
        System.arraycopy(a, 0, elementData, index, numNew);
        // 数组大小加 numNew
        size = s + numNew;
        return true;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * 从此列表中删除索引介于fromIndex和toIndex之间的所有元素。
     * 将任何后续元素向左移动（减少它们的索引）。
     * 此调用通过(toIndex - fromIndex)元素缩短列表。
     * （如果toIndex==fromIndex ，此操作无效。）
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        // 范围不正确，抛出 IndexOutOfBoundsException 异常
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException(
                    outOfBoundsMsg(fromIndex, toIndex));
        }
        // 增加数组修改次数
        modCount++;
        // <X> 移除 [fromIndex, toIndex) 的多个元素（不包含toIndex索引对应的值）
        shiftTailOverGap(elementData, fromIndex, toIndex);
    }

    /**
     * Erases the gap from lo to hi, by sliding down following elements.
     * 通过向下滑动以下元素来消除从 lo 到 hi 的间隙。
     * */
    private void shiftTailOverGap(Object[] es, int lo, int hi) {
        // 将 es 从 hi 位置开始的元素，移到 lo 位置开始。
        System.arraycopy(es, hi, es, lo, size - hi);
        // 将从 [size - hi + lo, size) 的元素置空，因为已经被挪到前面了。
        for (int to = size, i = (size -= hi - lo); i < to; i++)
            es[i] = null;
    }

    /**
     * A version of rangeCheck used by add and addAll.
     * add 和 addAll 使用的 rangeCheck 版本。
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * A version used in checking (fromIndex > toIndex) condition
     */
    private static String outOfBoundsMsg(int fromIndex, int toIndex) {
        return "From Index: " + fromIndex + " > To Index: " + toIndex;
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * 从此列表中移除指定集合中包含的所有元素。
     *
     * 通过两个变量 w（写入位置）和 r（读取位置），按照 r 顺序遍历数组(elementData)，如果不存在于指定的多个元素中，则写入到 elementData 的 w 位置，
     * 然后 w 位置 + 1 ，跳到下一个写入位置。通过这样的方式，实现将不存在 elementData 覆盖写到 w 位置。
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, false, 0, size);
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all
     * of its elements that are not contained in the specified collection.
     *
     * 仅保留此列表中包含在指定集合中的元素。换句话说，从该列表中删除所有未包含在指定集合中的元素。
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, true, 0, size);
    }

    /**
     *
     * complement 参数，翻译过来是“补足”的意思。怎么理解呢？表示如果 elementData 元素在 c 集合中时，是否保留。
     * 如果 complement 为 false 时，表示在集合中，就不保留，这显然符合 #removeAll(Collection<?> c) 方法要移除的意图。
     * 如果 complement 为 true 时，表示在集合中，就暴露，这符合我们后面会看到的 #retainAll(Collection<?> c) 方法要求交集的意图。
     * <1> 处，首先我们要知道这是一个基于 Optimize 优化的目的。我们是希望先判断是否 elementData 没有任何一个符合 c 的，这样就无需进行执行对应的移除逻辑。但是，我们又希望能够避免重复遍历，于是就有了这样一块的逻辑。总的来说，这块逻辑的目的是，优化，顺序遍历 elementData 数组，找到第一个不符合 complement ，然后结束遍历。
     * <1.1> 处，遍历到尾，都没不符合条件的，直接返回 false 。也就是说，丫根就不需要进行移除的逻辑。
     * <1.2> 处，如果包含结果不符合 complement 时，结束循环。可能有点难理解，我们来举个例子。假设 elementData 是 [1, 2, 3, 1] 时，c 是 [2] 时，那么在遍历第 0 个元素 1 时，则 c.contains(es[r]) != complement => false != false 不符合，所以继续缓存；然后，在遍历第 1 个元素 2 时，c.contains(es[r]) != complement => true != false 符合，所以结束循环。此时，我们便找到了第一个需要移除的元素的位置。当然，移除不是在这里执行，我们继续往下看。
     * <2> 处，设置开始写入 w 为 r ，注意不是 r++ 。这样，我们后续在循环 elementData 数组，就会从 w 开始写入。并且此时，r 也跳到了下一个位置，这样间接我们可以发现，w 位置的元素已经被“跳过”了。
     * <3> 处，继续遍历 elementData 数组，如何符合条件，则进行移除。可能有点难理解，我们继续上述例子。遍历第 2 个元素 3 时候，c.contains(es[r]) == complement => false == false 符合，所以将 3 写入到 w 位置，同时 w 指向下一个位置；遍历第三个元素 1 时候，c.contains(es[r]) == complement => true == false 不符合，所以不进行任何操作。
     * <4> 处，如果 contains 方法发生异常，则将 es 从 r 位置的数据写入到 es 从 w 开始的位置。这样，保证我们剩余未遍历到的元素，能够挪到从从 w 开始的位置，避免多出来一些元素。
     * <5> 处，是不是很熟悉，将数组 [w, end) 位置赋值为 null 。
     * @param c
     * @param complement 表示如果 elementData 元素在 c 集合中时，是否保留。
     * 如果 complement 为 false 时，表示在集合中，就不保留，这显然符合 #removeAll(Collection<?> c) 方法要移除的意图。
     * 如果 complement 为 true 时，表示在集合中，就暴露，这符合我们后面会看到的 #retainAll(Collection<?> c) 方法要求交集的意图。
     * @param from
     * @param end
     * @return
     */
    boolean batchRemove(Collection<?> c, boolean complement,
                        final int from, final int end) {
        // 校验 c 非 null 。
        Objects.requireNonNull(c);
        final Object[] es = elementData;
        int r;
        // Optimize for initial run of survivors
        // <1> 优化，顺序遍历 elementData 数组，找到第一个不符合 complement ，然后结束遍历。
        for (r = from;; r++) {
            // <1.1> 遍历到尾，都没不符合条件的，直接返回 false 。
            if (r == end)
                return false;
            // <1.2> 如果包含结果不符合 complement 时，结束
            if (c.contains(es[r]) != complement)
                break;
        }
        // <2> 设置开始写入 w 为 r ，注意不是 r++ 。
        // r++ 后，用于读取下一个位置的元素。因为通过上面的优化循环，我们已经 es[r] 是不符合条件的。
        int w = r++;
        try {
            // <3> 继续遍历 elementData 数组，如何符合条件，则进行移除
            for (Object e; r < end; r++)
                if (c.contains(e = es[r]) == complement) // 判断符合条件
                    es[w++] = e; // 移除的方式，通过将当前值 e 写入到 w 位置，然后 w 跳到下一个位置。
        } catch (Throwable ex) {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            // <4> 如果 contains 方法发生异常，则将 es 从 r 位置的数据写入到 es 从 w 开始的位置
            System.arraycopy(es, r, es, w, end - r);
            w += end - r;
            // 继续抛出异常
            throw ex;
        } finally { // <5>
            // 增加数组修改次数
            modCount += end - w;
            // 将数组 [w, end) 位置赋值为 null 。
            shiftTailOverGap(es, w, end);
        }
        return true;
    }

    /**
     * Saves the state of the {@code ArrayList} instance to a stream
     * (that is, serializes it).
     *
     * 将ArrayList实例的状态保存到流中（即序列化它）。
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData The length of the array backing the {@code ArrayList}
     *             instance is emitted (int), followed by all of its elements
     *             (each an {@code Object}) in the proper order.
     */
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out element count, and any hidden stuff
        // 获得当前的数组修改次数
        int expectedModCount = modCount;
        // <1> 写入非静态属性、非 transient 属性
        s.defaultWriteObject();

        // Write out size as capacity for behavioral compatibility with clone()
        // <2> 写入 size ，主要为了与 clone 方法的兼容
        s.writeInt(size);

        // Write out all elements in the proper order.
        // <3> 逐个写入 elementData 数组的元素
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        // 如果 other 修改次数发生改变，则抛出 ConcurrentModificationException 异常
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Reconstitutes the {@code ArrayList} instance from a stream (that is,
     * deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        // Read in size, and any hidden stuff
        // 读取非静态属性、非 transient 属性
        s.defaultReadObject();

        // Read in capacity
        // 读取 size ，不过忽略不用
        s.readInt(); // ignored

        if (size > 0) {
            // like clone(), allocate array based upon size not capacity
            // 像 clone（） 一样，根据大小而不是容量分配数组
            SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Object[].class, size);
            // 创建 elements 数组
            Object[] elements = new Object[size];

            // Read in all elements in the proper order.
            // 逐个读取
            for (int i = 0; i < size; i++) {
                elements[i] = s.readObject();
            }

            // 赋值给 elementData
            elementData = elements;
        } else if (size == 0) {
            // 如果 size 是 0 ，则直接使用空数组
            elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new java.io.InvalidObjectException("Invalid size: " + size);
        }
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        rangeCheckForAdd(index);
        return new ListItr(index);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        // prevent creating a synthetic constructor
        Itr() {}

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i < size) {
                final Object[] es = elementData;
                if (i >= es.length)
                    throw new ConcurrentModificationException();
                for (; i < size && modCount == expectedModCount; i++)
                    action.accept(elementAt(es, i));
                // update once at end to reduce heap write traffic
                cursor = i;
                lastRet = i - 1;
                checkForComodification();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations.
     *
     * <p>This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for {@link #indexOf(Object)} and
     * {@link #lastIndexOf(Object)}, and all of the algorithms in the
     * {@link Collections} class can be applied to a subList.
     *
     * <p>The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList<>(this, fromIndex, toIndex);
    }

    private static class SubList<E> extends AbstractList<E> implements RandomAccess {
        private final ArrayList<E> root;
        private final SubList<E> parent;
        private final int offset;
        private int size;

        /**
         * Constructs a sublist of an arbitrary ArrayList.
         */
        public SubList(ArrayList<E> root, int fromIndex, int toIndex) {
            this.root = root;
            this.parent = null;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }

        /**
         * Constructs a sublist of another SubList.
         */
        private SubList(SubList<E> parent, int fromIndex, int toIndex) {
            this.root = parent.root;
            this.parent = parent;
            this.offset = parent.offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = parent.modCount;
        }

        public E set(int index, E element) {
            Objects.checkIndex(index, size);
            checkForComodification();
            E oldValue = root.elementData(offset + index);
            root.elementData[offset + index] = element;
            return oldValue;
        }

        public E get(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            return root.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return size;
        }

        public void add(int index, E element) {
            rangeCheckForAdd(index);
            checkForComodification();
            root.add(offset + index, element);
            updateSizeAndModCount(1);
        }

        public E remove(int index) {
            Objects.checkIndex(index, size);
            checkForComodification();
            E result = root.remove(offset + index);
            updateSizeAndModCount(-1);
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            root.removeRange(offset + fromIndex, offset + toIndex);
            updateSizeAndModCount(fromIndex - toIndex);
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;
            checkForComodification();
            root.addAll(offset + index, c);
            updateSizeAndModCount(cSize);
            return true;
        }

        public void replaceAll(UnaryOperator<E> operator) {
            root.replaceAllRange(operator, offset, offset + size);
        }

        public boolean removeAll(Collection<?> c) {
            return batchRemove(c, false);
        }

        public boolean retainAll(Collection<?> c) {
            return batchRemove(c, true);
        }

        private boolean batchRemove(Collection<?> c, boolean complement) {
            checkForComodification();
            int oldSize = root.size;
            boolean modified =
                root.batchRemove(c, complement, offset, offset + size);
            if (modified)
                updateSizeAndModCount(root.size - oldSize);
            return modified;
        }

        public boolean removeIf(Predicate<? super E> filter) {
            checkForComodification();
            int oldSize = root.size;
            boolean modified = root.removeIf(filter, offset, offset + size);
            if (modified)
                updateSizeAndModCount(root.size - oldSize);
            return modified;
        }

        public Object[] toArray() {
            checkForComodification();
            return Arrays.copyOfRange(root.elementData, offset, offset + size);
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            checkForComodification();
            if (a.length < size)
                return (T[]) Arrays.copyOfRange(
                        root.elementData, offset, offset + size, a.getClass());
            System.arraycopy(root.elementData, offset, a, 0, size);
            if (a.length > size)
                a[size] = null;
            return a;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof List)) {
                return false;
            }

            boolean equal = root.equalsRange((List<?>)o, offset, offset + size);
            checkForComodification();
            return equal;
        }

        public int hashCode() {
            int hash = root.hashCodeRange(offset, offset + size);
            checkForComodification();
            return hash;
        }

        public int indexOf(Object o) {
            int index = root.indexOfRange(o, offset, offset + size);
            checkForComodification();
            return index >= 0 ? index - offset : -1;
        }

        public int lastIndexOf(Object o) {
            int index = root.lastIndexOfRange(o, offset, offset + size);
            checkForComodification();
            return index >= 0 ? index - offset : -1;
        }

        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(int index) {
            checkForComodification();
            rangeCheckForAdd(index);

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = SubList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = root.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = root.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public void forEachRemaining(Consumer<? super E> action) {
                    Objects.requireNonNull(action);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i < size) {
                        final Object[] es = root.elementData;
                        if (offset + i >= es.length)
                            throw new ConcurrentModificationException();
                        for (; i < size && root.modCount == expectedModCount; i++)
                            action.accept(elementAt(es, offset + i));
                        // update once at end to reduce heap write traffic
                        cursor = i;
                        lastRet = i - 1;
                        checkForComodification();
                    }
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = SubList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        root.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = SubList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (root.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList<>(this, fromIndex, toIndex);
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (root.modCount != modCount)
                throw new ConcurrentModificationException();
        }

        private void updateSizeAndModCount(int sizeChange) {
            SubList<E> slist = this;
            do {
                slist.size += sizeChange;
                slist.modCount = root.modCount;
                slist = slist.parent;
            } while (slist != null);
        }

        public Spliterator<E> spliterator() {
            checkForComodification();

            // ArrayListSpliterator not used here due to late-binding
            return new Spliterator<E>() {
                private int index = offset; // current index, modified on advance/split
                private int fence = -1; // -1 until used; then one past last index
                private int expectedModCount; // initialized when fence set

                private int getFence() { // initialize fence to size on first use
                    int hi; // (a specialized variant appears in method forEach)
                    if ((hi = fence) < 0) {
                        expectedModCount = modCount;
                        hi = fence = offset + size;
                    }
                    return hi;
                }

                public ArrayList<E>.ArrayListSpliterator trySplit() {
                    int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
                    // ArrayListSpliterator can be used here as the source is already bound
                    return (lo >= mid) ? null : // divide range in half unless too small
                        root.new ArrayListSpliterator(lo, index = mid, expectedModCount);
                }

                public boolean tryAdvance(Consumer<? super E> action) {
                    Objects.requireNonNull(action);
                    int hi = getFence(), i = index;
                    if (i < hi) {
                        index = i + 1;
                        @SuppressWarnings("unchecked") E e = (E)root.elementData[i];
                        action.accept(e);
                        if (root.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                    return false;
                }

                public void forEachRemaining(Consumer<? super E> action) {
                    Objects.requireNonNull(action);
                    int i, hi, mc; // hoist accesses and checks from loop
                    ArrayList<E> lst = root;
                    Object[] a;
                    if ((a = lst.elementData) != null) {
                        if ((hi = fence) < 0) {
                            mc = modCount;
                            hi = offset + size;
                        }
                        else
                            mc = expectedModCount;
                        if ((i = index) >= 0 && (index = hi) <= a.length) {
                            for (; i < hi; ++i) {
                                @SuppressWarnings("unchecked") E e = (E) a[i];
                                action.accept(e);
                            }
                            if (lst.modCount == mc)
                                return;
                        }
                    }
                    throw new ConcurrentModificationException();
                }

                public long estimateSize() {
                    return getFence() - index;
                }

                public int characteristics() {
                    return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
                }
            };
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        final Object[] es = elementData;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++)
            action.accept(elementAt(es, i));
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator(0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */
    final class ArrayListSpliterator implements Spliterator<E> {

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         */

        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        /** Creates new spliterator covering the given range. */
        ArrayListSpliterator(int origin, int fence, int expectedModCount) {
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            if ((hi = fence) < 0) {
                expectedModCount = modCount;
                hi = fence = size;
            }
            return hi;
        }

        public ArrayListSpliterator trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator(lo, index = mid, expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)elementData[i];
                action.accept(e);
                if (modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((a = elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = modCount;
                    hi = size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return getFence() - index;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    // A tiny bit set implementation

    private static long[] nBits(int n) {
        return new long[((n - 1) >> 6) + 1];
    }
    private static void setBit(long[] bits, int i) {
        bits[i >> 6] |= 1L << i;
    }
    private static boolean isClear(long[] bits, int i) {
        return (bits[i >> 6] & (1L << i)) == 0;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return removeIf(filter, 0, size);
    }

    /**
     * Removes all elements satisfying the given predicate, from index
     * i (inclusive) to index end (exclusive).
     */
    boolean removeIf(Predicate<? super E> filter, int i, final int end) {
        Objects.requireNonNull(filter);
        int expectedModCount = modCount;
        final Object[] es = elementData;
        // Optimize for initial run of survivors
        for (; i < end && !filter.test(elementAt(es, i)); i++)
            ;
        // Tolerate predicates that reentrantly access the collection for
        // read (but writers still get CME), so traverse once to find
        // elements to delete, a second pass to physically expunge.
        if (i < end) {
            final int beg = i;
            final long[] deathRow = nBits(end - beg);
            deathRow[0] = 1L;   // set bit 0
            for (i = beg + 1; i < end; i++)
                if (filter.test(elementAt(es, i)))
                    setBit(deathRow, i - beg);
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            modCount++;
            int w = beg;
            for (i = beg; i < end; i++)
                if (isClear(deathRow, i - beg))
                    es[w++] = es[i];
            shiftTailOverGap(es, w, end);
            return true;
        } else {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return false;
        }
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        replaceAllRange(operator, 0, size);
        // TODO(8203662): remove increment of modCount from ...
        modCount++;
    }

    private void replaceAllRange(UnaryOperator<E> operator, int i, int end) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final Object[] es = elementData;
        for (; modCount == expectedModCount && i < end; i++)
            es[i] = operator.apply(elementAt(es, i));
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        modCount++;
    }

    void checkInvariants() {
        // assert size >= 0;
        // assert size == elementData.length || elementData[size] == null;
    }
}
