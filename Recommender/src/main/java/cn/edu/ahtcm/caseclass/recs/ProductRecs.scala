package cn.edu.ahtcm.caseclass.recs

/**
 * 定义商品相似度列表
 *
 * @param productId     商品 ID
 * @param recsProductId 推荐商品 ID
 * @param score         评分
 */
case class ProductRecs(productId: Int, recsProductId: Int, score: Double)
