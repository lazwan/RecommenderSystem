package cn.edu.ahtcm.caseclass.recs

/**
 * 定义用户的推荐列表
 *
 * @param userId        用户 ID
 * @param recsProductId 推荐商品 ID
 * @param score         评分
 */
case class UserRecs(userId: Int, recsProductId: Int, score: Double)
