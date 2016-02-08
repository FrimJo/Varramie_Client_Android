package com.spots.depricated;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by fredrikjohansson on 15-06-10.
 */
public class CircleArray<T> implements Iterable<T>{

    private final int _size;
    private final T[] _array;

    private int _pointer = 0;

    public CircleArray(final int size, Class<T> cls){
        _size = size;
        _array = (T[]) Array.newInstance(cls, size);
    }

    public synchronized void add(T object){
        _array[_pointer] = object;
        _pointer++;
        if(_pointer == _size)
            _pointer %= _size;
    }

    public synchronized T get(){
        T object = _array[_pointer];
        _pointer++;
        if(_pointer == _size)
            _pointer %= _size;
        return object;
    }

    public synchronized T first(){
        return _array[_pointer];
    }

    public synchronized int size(){
        return _size;
    }

    private int mod(int val){
        int temp = val % _size;
        if(temp < 0) temp += _size;
        return temp;
    }

    @Override
    public synchronized Iterator<T> iterator() {


        return new Iterator<T>() {

            private int _iterator_index = _pointer - 1;
            private int _iterator_pointer = _pointer;
            private T _next = null;


            @Override
            public boolean hasNext() {
                _next = null;
                int index = _iterator_index < 0 ? mod(_iterator_index) : _iterator_index;

                if(_array[index] == null)
                    return false;

                if (index == _iterator_pointer)
                    return false;

                _next = _array[index];
                return true;
            }

            @Override
            public T next() {
                if(_next == null)
                    throw new NoSuchElementException();
                _iterator_index--;
                return _next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

}
