package cn.edu.ahtcm.caseclass.bean

/**
 * Product 数据集
 *
 * @param productId  商品 ID
 * @param name       商品名称
 * @param price      商品价格
 * @param imageUrl   商品图片 URL
 * @param categories 商品分类
 */
// case class Product(productId: Int, name: String, imageUrl: String, categories: String, tags: String)
case class Product(productId: Int, name: String, price: String, imageUrl: String, categories: String)
