package cn.edu.ahtcm.caseclass.bean

/**
 * Rating 数据集
 *
 * @param userId    用户 ID
 * @param productId 商品 ID
 * @param score     评分
 * @param timestamp 时间戳
 */
case class Rating(userId: Int, productId: Int, score: Double, timestamp: Int)
