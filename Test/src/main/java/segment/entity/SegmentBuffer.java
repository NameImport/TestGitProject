package segment.entity;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 序号缓存.
 */
public class SegmentBuffer {
    private String domainName;
    private Long maxId;
    private AtomicLong currentId;
    private Long step;
    private AtomicBoolean currentUpdating;
    private Boolean nextReady;
    private SegmentBuffer nextBuffer;
    public Long getNewId(){
        return currentId.incrementAndGet();
    }
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public SegmentBuffer getNextBuffer() {
        return nextBuffer;
    }

    public void setNextBuffer(SegmentBuffer nextBuffer) {
        this.nextBuffer = nextBuffer;
    }

    public Boolean getNextReady() {
        return nextReady;
    }

    public void setNextReady(Boolean nextReady) {
        this.nextReady = nextReady;
    }

    public AtomicBoolean getCurrentUpdating() {
        return currentUpdating;
    }

    public void setCurrentUpdating(AtomicBoolean currentUpdating) {
        this.currentUpdating = currentUpdating;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }

    public AtomicLong getCurrentId() {
        return currentId;
    }

    public void setCurrentId(AtomicLong currentId) {
        this.currentId = currentId;
    }

    public Long getStep() {
        return step;
    }

    public void setStep(Long step) {
        this.step = step;
    }
}
