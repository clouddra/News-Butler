// GenericsNote: Converted.
/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.cs4274.news_butler.util;


import java.io.Serializable;
import java.util.Comparator;

/**
 * Reverses the order of another comparator by reversing the arguments
 * to its {@link #compare(Object, Object) compare} method.
 *
 * @author Henri Yandell
 * @author Matt Hall, John Watkinson, Michael A. Smith
 * @version $Revision: 1.1 $ $Date: 2005/10/11 17:05:20 $
 * @see java.util.Collections#reverseOrder()
 * @since Commons Collections 2.0
 */
public class ReverseComparator <T> implements Comparator<T>, Serializable {

    /**
     * Serialization version from Collections 2.0.
     */
    private static final long serialVersionUID = 2858887242028539265L;

    /**
     * The comparator being decorated.
     */
    private Comparator<T> comparator;

    /**
     * Creates a comparator that inverts the comparison
     * of the given comparator.  Pass in a {@link ComparableComparator}
     * for reversing the natural order, as per
     * {@link java.util.Collections#reverseOrder()}</b>.
     *
     * @param comparator Comparator to reverse
     */
    public ReverseComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares two objects in reverse order.
     *
     * @param obj1 the first object to compare
     * @param obj2 the second object to compare
     * @return negative if obj1 is less, positive if greater, zero if equal
     */
    public int compare(T obj1, T obj2) {
        return comparator.compare(obj2, obj1);
    }

    //-----------------------------------------------------------------------
    /**
     * Implement a hash code for this comparator that is consistent with
     * {@link #equals(Object) equals}.
     *
     * @return a suitable hash code
     * @since Commons Collections 3.0
     */
    public int hashCode() {
        return "ReverseComparator".hashCode() ^ comparator.hashCode();
    }

    /**
     * Returns <code>true</code> iff <i>that</i> Object is
     * is a {@link Comparator} whose ordering is known to be
     * equivalent to mine.
     * <p/>
     * This implementation returns <code>true</code>
     * iff <code><i>object</i>.{@link Object#getClass() getClass()}</code>
     * equals <code>this.getClass()</code>, and the underlying
     * comparators are equal.
     * Subclasses may want to override this behavior to remain consistent
     * with the {@link Comparator#equals(Object) equals} contract.
     *
     * @param object the object to compare to
     * @return true if equal
     * @since Commons Collections 3.0
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (null == object) {
            return false;
        } else if (object.getClass().equals(this.getClass())) {
            ReverseComparator thatrc = (ReverseComparator) object;
            return comparator.equals(thatrc.comparator);
        } else {
            return false;
        }
    }

}
