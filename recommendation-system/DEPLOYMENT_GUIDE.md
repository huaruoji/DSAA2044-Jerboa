# For You 推荐系统 - 完整部署指南

## 🎯 系统架构

### 后端 (Python Flask)
- **可扩展的推荐引擎**: 支持 TF-IDF（当前）和 BERT（未来）
- **基于历史的推荐**: 根据用户阅读历史生成个性化推荐
- **向后兼容**: 支持旧版 GET 和新版 POST API

### 前端 (Android Kotlin)
- **用户历史管理**: 自动记录用户阅读过的帖子
- **智能冷启动**: 新用户使用默认关键词
- **闭环反馈**: 点击推荐内容自动加入历史

---

## 📦 部署步骤

### 1. 后端部署

#### 步骤 1: 训练模型（如果还没有训练）

```bash
cd recommendation-system
python train_recommendation_model.py
```

**预期输出**:
```
✓ Loaded 10,000+ posts
✓ TF-IDF matrix created
✓ Model saved to models/
```

#### 步骤 2: 启动 Flask 服务器

```bash
python app.py
```

**预期输出**:
```
Loading TF-IDF recommendation model...
✓ Model loaded: 10,000 posts, 10,000 features
 * Running on http://0.0.0.0:5000
```

#### 步骤 3: 测试 API

在另一个终端运行：

```bash
python test_api.py
```

**预期输出**:
```
✓ Health Check: PASS
✓ Legacy GET: PASS
✓ New POST: PASS
✓ Model Info: PASS
```

---

### 2. Android 部署

#### 步骤 1: 构建项目

```bash
./gradlew assembleDebug
```

#### 步骤 2: 安装到设备

```bash
./gradlew installDebug
# 或使用脚本
./scripts/deploy_to_device.sh
```

#### 步骤 3: 配置服务器地址

确保 `RecommendationClient.kt` 中的 BASE_URL 指向你的 Flask 服务器：

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:5000/api/"
```

---

## 🔧 API 文档

### 1. Health Check
```http
GET /api/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "Content-Based Recommendation API",
  "version": "1.0.0"
}
```

### 2. Get Recommendations (新版 - 推荐使用)

```http
POST /api/recommend
Content-Type: application/json

{
  "history_contents": [
    "artificial intelligence research",
    "machine learning tutorials"
  ],
  "top_k": 10,
  "min_score": 0.0,
  "exclude_ids": []
}
```

**Response:**
```json
{
  "success": true,
  "algorithm": "TF-IDF",
  "count": 10,
  "recommendations": [
    {
      "id": "post123",
      "title": "Deep Learning Fundamentals",
      "text": "Introduction to neural networks...",
      "url": "https://...",
      "subreddit": "MachineLearning",
      "score": 1523,
      "similarity_score": 0.85,
      "created_utc": 1646160815
    }
  ]
}
```

### 3. Get Recommendations (旧版 - 向后兼容)

```http
GET /api/recommend?q=machine%20learning&top_k=5
```

**Response:** 同上

### 4. Model Info

```http
GET /api/model/info
```

**Response:**
```json
{
  "success": true,
  "data": {
    "algorithm": "TF-IDF",
    "num_posts": 10000,
    "num_features": 10000,
    "training_date": "2025-12-08 20:00:00",
    "strategy": "content-based (subreddit-independent)"
  }
}
```

---

## 🚀 工作流程

### 用户首次使用（冷启动）
1. 用户打开 "For You" 页面
2. Android 检测历史为空
3. 发送默认冷启动关键词到 API
4. 显示通用推荐内容

### 用户持续使用（个性化）
1. 用户点击推荐的帖子
2. `ForYouViewModel.onPostViewed()` 自动记录标题
3. 历史存储在 `SharedPreferences`（最多 50 条）
4. 下次刷新时，使用最近 10 条历史生成推荐

### 推荐算法
1. 合并用户历史文本（标题权重 × 2）
2. 使用 TF-IDF 向量化
3. 计算与所有帖子的余弦相似度
4. 返回 Top-K 最相似的帖子

---

## 🔄 升级到 BERT（未来）

### 当前架构已支持！

只需三步即可升级：

#### 1. 训练 BERT 模型
```python
# 创建 bert_recommender.py
class BERTRecommender(BaseRecommender):
    def load_model(self):
        self.model = torch.load('models/bert_model.pt')
        # ...
```

#### 2. 修改启动参数
```bash
# 设置环境变量
export RECOMMENDER_ALGORITHM=bert
python app.py
```

#### 3. 无需修改 Android 代码！
API 接口保持不变，Android 端自动使用新模型。

---

## 📊 监控和调试

### 查看用户历史（调试）

在 Android 中添加：
```kotlin
val historyCount = forYouViewModel.getHistoryCount()
Log.d("ForYou", "User has $historyCount items in history")
```

### 清除历史
```kotlin
forYouViewModel.clearHistory()
```

### Flask 日志
```bash
# 查看请求日志
tail -f app.log
```

---

## ⚙️ 配置选项

### 后端配置

在 `app.py` 中：
```python
# 切换算法
ALGORITHM = 'tfidf'  # 或 'bert'

# 调整参数
DEFAULT_TOP_K = 10
MAX_TOP_K = 100
```

### Android 配置

在 `UserHistoryRepository.kt` 中：
```kotlin
// 历史记录数量
private const val MAX_HISTORY_SIZE = 50

// 用于推荐的数量
private const val MAX_ITEMS_FOR_RECOMMENDATION = 10

// 冷启动关键词
private val DEFAULT_COLD_START_KEYWORDS = listOf(...)
```

---

## 🐛 故障排除

### 问题 1: Android 连接超时
**解决**: 
- 确保 Flask 运行在 `0.0.0.0:5000`
- 检查防火墙设置
- 使用 `http://` 而非 `https://`

### 问题 2: 推荐结果为空
**解决**:
- 检查模型是否已训练
- 查看 Flask 日志中的错误
- 降低 `min_score` 阈值

### 问题 3: 历史记录不生效
**解决**:
- 确保调用了 `onPostViewed()`
- 检查 SharedPreferences 权限
- 清除 app 数据重试

---

## 📈 性能优化

### 后端优化
- 使用 Redis 缓存频繁查询
- 异步处理大批量请求
- 启用 Gzip 压缩

### Android 优化
- 使用 DataStore 替代 SharedPreferences
- 实现本地缓存机制
- 添加下拉刷新

---

## 🎉 完成！

系统现已完全集成并支持：
✅ 基于用户历史的个性化推荐  
✅ 冷启动处理  
✅ 模型升级路径（TF-IDF → BERT）  
✅ 向后兼容的 API  
✅ 自动历史记录  
✅ 闭环反馈系统  

---

**作者**: DSAA2044 Team  
**日期**: December 2025  
**版本**: 2.0
