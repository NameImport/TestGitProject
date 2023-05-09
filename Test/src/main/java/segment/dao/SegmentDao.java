package segment.dao;

import segment.entity.SegmentBuffer;

/**
 * 序列号DAO类.
 */
public class SegmentDao {
    /**
     * 模拟存储数据库.
     *
     * @param segmentBuffer 序列号缓存
     */
    public void saveSegment(SegmentBuffer segmentBuffer) {
        String segmentMsg = String.format("模拟持久化序列号信息,领域名称为{%s},当前ID为{%d},步长为{%d}", segmentBuffer.getDomainName(), segmentBuffer.getMaxId(), segmentBuffer.getStep());
        System.out.println(segmentMsg);
    }
}
