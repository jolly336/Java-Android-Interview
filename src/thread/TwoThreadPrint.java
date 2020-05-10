package thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 题目：用两个线程，一个输出字母，一个输出数字，交替输出 1A2B3C4D...26Z
 * 实现方式：
 * 1. LockSupport
 * 2. CAS 自旋
 * 3. synchronized,wait,nofity
 * 4. ReentrantLock + newCondition 队列来实现
 * <p>
 * Created by Nelson on 2020/4/23.
 */
public class TwoThreadPrint {

    public static void main(String[] args) {
        //new LockSupportPrint().print();
        //new CASPrint().print();
        new SynchronizedPrint().print();
        //new LockPrint().print();
        //new LockConditionPrint().print();
    }

    /**
     * 方法一：LockSupport
     */
    static class LockSupportPrint {

        Thread t1 = null, t2 = null;

        public void print() {
            char[] aI = "1234567".toCharArray();
            char[] aC = "ABCDEFG".toCharArray();

            t1 = new Thread(() -> {
                for (char c : aI) {
                    System.out.print(c);
                    LockSupport.unpark(t2); // 叫醒 t2
                    LockSupport.park(); // t1 阻塞
                }
            }, "t1");

            t2 = new Thread(() -> {
                for (char c : aC) {
                    LockSupport.park(); // t2 阻塞
                    System.out.print(c);
                    LockSupport.unpark(t1); // 叫醒 t1
                }

            }, "t2");

            t1.start();
            t2.start();
        }
    }

    /**
     * 方法二：CAS 自旋锁
     */
    static class CASPrint {
        enum ReadyToRun {T1, T2}

        static volatile ReadyToRun r = ReadyToRun.T1; //思考为什么必须 volatile？

        public void print() {
            char[] aI = "1234567".toCharArray();
            char[] aC = "ABCDEFG".toCharArray();

            new Thread(() -> {
                for (char c : aI) {
                    while (r != ReadyToRun.T1) {
                    }
                    System.out.print(c);
                    r = ReadyToRun.T2;
                }
            }, "t1").start();

            new Thread(() -> {
                for (char c : aC) {
                    while (r != ReadyToRun.T2) {
                    }
                    System.out.print(c);
                    r = ReadyToRun.T1;
                }
            }, "t2").start();
        }
    }


    /**
     * 方法三：synchronized,wait,nofity
     * <p>
     * Q: 如果要保证 t2 在 t1 之前打印，也就是要保证首先输出的是 A 而不是 1，这个时候该如何做？
     * 解法：1）使用个变量控制 2）CountDownLatch 控制
     */
    static class SynchronizedPrint {
        private static volatile boolean t2Started = false;
        private static CountDownLatch latch = new CountDownLatch(1);

        public void print() {
            char[] aI = "1234567".toCharArray();
            char[] aC = "ABCDEFG".toCharArray();

            Object lock = new Object();
            Thread t1 = new Thread(() -> {
//            try {
//                latch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

                synchronized (lock) {
                    while (!t2Started) {
                        try {
                            System.out.println("t2 还没开启，t1 wait");
                            lock.wait();
                        } catch (InterruptedException e) {
                            // no-op
                        }
                    }

                    System.out.println("t2 已经开启，t1 开始打印字符串");

                    for (char c : aI) {
                        System.out.print(c);
                        try {
                            // 思考：notify 和 wait 位置能交换下吗？NO，wait 阻塞了，不可能唤醒的：）
                            lock.notify();
                            lock.wait(); // 让出锁
                        } catch (InterruptedException e) {
                            // no-op
                        }
                    }

                    lock.notify(); // 必须，否则无法停止程序
                }
            }, "t1");

            Thread t2 = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("t2 获取到锁，");
                    for (char c : aC) {
                        System.out.print(c);
                        t2Started = true;
//                        latch.countDown();
                        try {
                            lock.notify();
                            lock.wait();
                        } catch (InterruptedException e) {
                            // no-op
                        }
                    }

                    lock.notify();
                }
            }, "t2");

            t1.start();
            t2.start();
        }
    }

    /**
     * 方法四：ReentrantLock + newCondition 单队列来实现
     */
    static class LockPrint {
        public void print() {
            char[] aI = "1234567".toCharArray();
            char[] aC = "ABCDEFG".toCharArray();

            Lock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            new Thread(() -> {
                try {
                    lock.lock();

                    for (char c : aI) {
                        System.out.print(c);
                        condition.signal();
                        condition.await();
                    }

                    condition.signal();

                } catch (Exception e) {
                    // no-op
                } finally {
                    lock.unlock();
                }
            }, "t1").start();

            new Thread(() -> {
                try {
                    lock.lock();

                    for (char c : aC) {
                        System.out.print(c);
                        condition.signal();
                        condition.await();
                    }

                    condition.signal();
                } catch (Exception e) {
                    // no-op
                } finally {
                    lock.unlock();
                }

            }, "t2").start();
        }
    }

    /**
     * 方法四：ReentrantLock + newCondition 多队列来实现
     */
    static class LockConditionPrint {
        public void print() {
            char[] aI = "1234567".toCharArray();
            char[] aC = "ABCDEFG".toCharArray();

            Lock lock = new ReentrantLock();
            // condition 是一个队列，叫醒哪个队列里面的线程
            Condition conditionT1 = lock.newCondition();
            Condition conditionT2 = lock.newCondition();

            new Thread(() -> {
                try {
                    lock.lock();

                    for (char c : aI) {
                        System.out.print(c);
                        // 在 conditionT2 队列里的线程叫醒
                        conditionT2.signal();
                        // 当前线程进入 conditionT1 队列
                        conditionT1.await();
                    }

                    conditionT2.signal();

                } catch (Exception e) {
                    // no-op
                } finally {
                    lock.unlock();
                }
            }, "t1").start();

            new Thread(() -> {
                try {
                    lock.lock();

                    for (char c : aC) {
                        System.out.print(c);
                        conditionT1.signal();
                        conditionT2.await();
                    }

                    conditionT1.signal();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

            }, "t2").start();
        }
    }
}