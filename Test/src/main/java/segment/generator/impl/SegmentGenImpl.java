package segment.generator.impl;

import segment.BufferFactory;
import segment.dao.SegmentDao;
import segment.entity.SegmentBuffer;
import segment.generator.ISegmentGen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SegmentGenImpl implements ISegmentGen {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final Map<String, SegmentBuffer> domain2SegmentBufferMap = new HashMap<>(16);
    private SegmentDao segmentDao = new SegmentDao();

    public void init() {
        BufferFactory bufferFactory = new BufferFactory();
        SegmentBuffer issue = bufferFactory.createSegmentBuffer("ISSUE", 0L, 1000L);
        SegmentBuffer acct = bufferFactory.createSegmentBuffer("ACCT", 1000L, 30000L);
        SegmentBuffer svct = bufferFactory.createSegmentBuffer("SVCT", 0L, 2000L);

        loadSegmentBuffer(issue);
        loadSegmentBuffer(acct);
        loadSegmentBuffer(svct);

        domain2SegmentBufferMap.put(issue.getDomainName(), issue);
        domain2SegmentBufferMap.put(acct.getDomainName(), acct);
        domain2SegmentBufferMap.put(svct.getDomainName(), svct);
    }

    @Override
    public Long getSegmentId(String domainName) {
        readLock.lock();
        SegmentBuffer segmentBuffer = domain2SegmentBufferMap.get(domainName);
        if (segmentBuffer == null) {
            readLock.unlock();
            return null;
        }
        Long id = tryGetId(segmentBuffer);
        readLock.unlock();
        if (id == null) {
            id = swapAndGetId(segmentBuffer);
        }
        tryGetNextBuffer(segmentBuffer);
        return id;
    }

    private Long tryGetId(SegmentBuffer segmentBuffer) {
        //从缓存槽中获取ID，若缓存槽已经满了就返回null
        Long currentId = segmentBuffer.getNewId();
        if (currentId < segmentBuffer.getMaxId()) {
            return currentId;
        }

        return null;
    }

    /**
     * 切换序号缓存槽并且获取新的ID.
     *
     * @param segmentBuffer 序号缓存槽
     * @return Long 新的ID序号
     */
    private Long swapAndGetId(SegmentBuffer segmentBuffer) {
        //等待缓存槽准备完毕.
        while (!segmentBuffer.getNextReady() && segmentBuffer.getCurrentUpdating().get()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //此时的序号缓存以经完全用完，加锁避免切换过程中其他线程获取到此序号段，这里会有重复操作，但是不影响使用
        writeLock.lock();
        SegmentBuffer nextBuffer = segmentBuffer.getNextBuffer();
        domain2SegmentBufferMap.put(nextBuffer.getDomainName(), nextBuffer);
        writeLock.unlock();
        return nextBuffer.getNewId();
    }

    /**
     * 采用无锁结构来实现缓存序号的预备工作.
     *
     * @param segmentBuffer 序号缓存
     */
    private void tryGetNextBuffer(SegmentBuffer segmentBuffer) {
        if (!segmentBuffer.getNextReady() && segmentBuffer.getCurrentId().get() / new Double(segmentBuffer.getMaxId()) > 0.7 && segmentBuffer.getCurrentUpdating().compareAndSet(false, true)) {
            //开线程去预备当前缓存槽的下一个槽
            new Thread(() -> {
                SegmentBuffer newBuffer = new SegmentBuffer();

                newBuffer.setStep(segmentBuffer.getStep());
                newBuffer.setCurrentId(new AtomicLong(segmentBuffer.getMaxId()));
                newBuffer.setNextReady(false);
                newBuffer.setDomainName(segmentBuffer.getDomainName());
                newBuffer.setMaxId(newBuffer.getCurrentId().get() + newBuffer.getStep());
                newBuffer.setCurrentUpdating(new AtomicBoolean(false));

                segmentBuffer.setNextBuffer(newBuffer);
                //更新号段到数据库.
                segmentDao.saveSegment(newBuffer);
                //模拟存储到数据库的延迟.
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                segmentBuffer.setNextReady(true);
            }).start();
        }
    }

    /**
     * 初始化序列号缓存.
     *
     * @param segmentBuffer 序列号缓存
     */
    private void loadSegmentBuffer(SegmentBuffer segmentBuffer) {
        segmentBuffer.setMaxId(segmentBuffer.getCurrentId().get() + segmentBuffer.getStep());
        segmentBuffer.setCurrentUpdating(new AtomicBoolean(false));
        segmentBuffer.setNextBuffer(null);
        segmentBuffer.setNextReady(false);
    }

    public static void main(String[] args) {
        ISegmentGen segmentGen = new SegmentGenImpl();
        segmentGen.init();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    Long issue = segmentGen.getSegmentId("ISSUE");
                    System.out.println(issue);
                }
            }).start();
        }
    }
}
