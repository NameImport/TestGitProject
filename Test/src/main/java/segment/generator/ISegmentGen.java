package segment.generator;

/**
 * 序列号生成器.
 */
public interface ISegmentGen {
    /**
     * 生成唯一序列号.
     *
     * @param domainName 领域名称
     * @return Long 全局唯一的序列号
     */
    Long getSegmentId(String domainName);

    void init();
}
