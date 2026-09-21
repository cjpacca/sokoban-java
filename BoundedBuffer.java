import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer {
    final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();
    final Condition notFull = lock.newCondition();
    final Object[] items = new Object[100];
    int putptr, takeptr, count;

    public void put(Object o) throws InterruptedException {
        lock.lock();
        try {
            while(count == items.length) {
                notFull.await();
            }
            items[putptr] = o;
            if(++putptr == items.length) {
                putptr = 0;
            }
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Object get() throws InterruptedException {
        lock.lock();
        try {
            while(count == 0) {
                notEmpty.await();
            }
            Object o = items[takeptr];
            if(++takeptr == items.length) {
                takeptr = 0;
            }
            --count;
            notFull.signal();
            return o;
        } finally {
            lock.unlock();
        }
    }
}
