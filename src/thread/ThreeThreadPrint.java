package thread;

/**
 * 题目：用三个线程，线程“1”，“2”， “3”，顺序打印字母A-Z，输出结果是1A、2B、3C、1D 2E… ？
 * 实现：使用 JVM synchronized 锁来实现线程同步，并配合 wait() 和 notifyAll()
 * <p>
 * Created by Nelson on 2020/5/10.
 */
public class ThreeThreadPrint {

    public static void main(String[] args) {
        new ThreeThreadPrint().print();
    }

    private char c = 'A';
    private int i = 0;

    private void print() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        int threadId = Integer.parseInt(Thread.currentThread().getName());
                        while (i < 26) {
                            if (i % 3 == threadId - 1) {
                                System.out.println(threadId + "" + (char) c++);
                                i++;
                                notifyAll();
                            } else {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        // no-op
                    }
                }
            }
        };

        Thread t1 = new Thread(runnable, "1");
        Thread t2 = new Thread(runnable, "2");
        Thread t3 = new Thread(runnable, "3");
        t1.start();
        t2.start();
        t3.start();
    }
}