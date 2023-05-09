package segment;

import segment.entity.SegmentBuffer;

import java.util.concurrent.atomic.AtomicLong;

public class BufferFactory {
    public SegmentBuffer createSegmentBuffer(String domainName,Long currentId,Long step){
        SegmentBuffer segmentBuffer = new SegmentBuffer();
        segmentBuffer.setDomainName(domainName);
        segmentBuffer.setCurrentId(new AtomicLong(currentId));
        segmentBuffer.setStep(step);
        return segmentBuffer;
    }
}
