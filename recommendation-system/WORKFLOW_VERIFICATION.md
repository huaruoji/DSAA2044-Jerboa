# For You 推荐系统 - 使用流程验证

## 🔄 完整工作流程

### 1. 用户首次打开 "For You" 页面

**发生的事情：**
1. `ForYouViewModel` 初始化，连接到 `UserHistoryRepository`
2. 检查本地历史记录 → **发现为空**（新用户）
3. 使用**冷启动关键词**发送请求到后端

**API 请求：**
```json
POST /api/recommend
{
  "history_contents": [
    "technology news updates",
    "science discoveries research",
    "interesting discussions community",
    "current events world news",
    "helpful advice tips"
  ],
  "top_k": 10
}
```

**后端处理：**
- 合并所有关键词
- TF-IDF 向量化
- 计算相似度
- 返回通用热门内容

**用户看到：**
- 10 条通用推荐帖子
- 页面顶部显示: "Algorithm: TF-IDF • Cold start"

---

### 2. 用户点击第一篇推荐帖子

**示例帖子：**
- 标题: "New AI Research Breakthrough"
- 内容: "Scientists discover..."

**发生的事情：**
1. 用户点击帖子卡片
2. `HomeActivity` 调用 `forYouViewModel.onPostViewed(title, text)`
3. `UserHistoryRepository.addToHistory()` 执行：
   ```kotlin
   // 存储格式：标题权重 × 2
   "New AI Research Breakthrough New AI Research Breakthrough Scientists discover..."
   ```
4. 保存到 SharedPreferences
5. 浏览器打开帖子 URL

**本地存储状态：**
```
历史记录 [1]: "New AI Research Breakthrough New AI Research Breakthrough Scientists discover..."
```

---

### 3. 用户返回继续浏览，点击第二篇

**新帖子：**
- 标题: "Python Machine Learning Tutorial"
- 内容: "Learn how to build..."

**发生的事情：**
1. 再次记录到历史
2. 历史记录变成 FIFO 队列（最新的在前）

**本地存储状态：**
```
历史记录 [2]:
1. "Python Machine Learning Tutorial Python Machine Learning Tutorial Learn how to build..."
2. "New AI Research Breakthrough New AI Research Breakthrough Scientists discover..."
```

---

### 4. 用户切换到其他 Tab 再回到 "For You"

**发生的事情：**
1. `LaunchedEffect(selectedFeedTab)` 触发
2. 调用 `forYouViewModel.loadRecommendations()`
3. **现在历史不为空了！** → 使用真实历史

**API 请求：**
```json
POST /api/recommend
{
  "history_contents": [
    "Python Machine Learning Tutorial Python Machine Learning Tutorial Learn how to build...",
    "New AI Research Breakthrough New AI Research Breakthrough Scientists discover..."
  ],
  "top_k": 10
}
```

**后端处理：**
- 合并用户历史（AI + Python + ML 相关词汇频率高）
- TF-IDF 向量化
- **推荐与用户兴趣相似的内容**

**用户看到：**
- 10 条**个性化**推荐（更多 AI/ML/Python 相关）
- 页面顶部显示: "Algorithm: TF-IDF • Based on your history"

---

### 5. 持续使用（闭环形成）

```
浏览 → 点击 → 记录历史 → 刷新 → 个性化推荐 → 浏览 → ...
```

**历史记录增长：**
- 第 3 篇: "Deep Learning Framework Comparison"
- 第 4 篇: "Data Science Career Guide"
- 第 5 篇: "TensorFlow vs PyTorch"
- ...
- 最多存储 50 条，使用最近 10 条生成推荐

**推荐越来越精准：**
- 初期：通用科技内容
- 中期：AI/ML 相关
- 后期：深度学习、Python、数据科学等细分领域

---

## 🧪 验证步骤

### 验证 1: 检查历史是否被记录

在 `ForYouViewModel` 添加日志：
```kotlin
fun onPostViewed(postTitle: String, postText: String = "") {
    historyRepository.addToHistory(postTitle, postText)
    Log.d("ForYou", "✓ Recorded: $postTitle")
    Log.d("ForYou", "History count: ${historyRepository.getCount()}")
}
```

**预期输出（Logcat）：**
```
D/ForYou: ✓ Recorded: Python Machine Learning Tutorial
D/ForYou: History count: 2
```

### 验证 2: 检查 API 请求内容

在 `ForYouViewModel.loadRecommendations()` 添加日志：
```kotlin
val historyContents = historyRepository.getHistoryForRecommendation()
Log.d("ForYou", "Sending ${historyContents.size} history items to API")
historyContents.forEachIndexed { i, content ->
    Log.d("ForYou", "  [$i] ${content.take(50)}...")
}
```

**预期输出（Logcat）：**
```
D/ForYou: Sending 2 history items to API
D/ForYou:   [0] Python Machine Learning Tutorial Python Machine...
D/ForYou:   [1] New AI Research Breakthrough New AI Research...
```

### 验证 3: 检查后端接收

在 Flask 日志中查看：
```bash
# 应该看到 POST 请求，而不是 GET
POST /api/recommend
Content-Type: application/json
Body: {"history_contents": [...], "top_k": 10}
```

### 验证 4: 检查推荐变化

**测试步骤：**
1. 清除 App 数据（模拟新用户）
2. 打开 "For You" → 记录推荐内容
3. 点击 3-5 篇关于"Python"的帖子
4. 切换 Tab 后回到 "For You"
5. **验证：新推荐应该包含更多 Python 相关内容**

---

## 🐛 如果不工作，检查这些

### 问题 1: 仍然看到 GET 请求

**原因：** Android 代码未更新或未重新编译

**解决：**
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

### 问题 2: 推荐内容没有变化

**原因：** 历史未被记录

**检查：**
1. 是否调用了 `onPostViewed()`？
2. SharedPreferences 是否有写入权限？
3. 查看 Logcat 是否有异常

**调试命令：**
```bash
# 查看 SharedPreferences 内容
adb shell run-as com.jerboa cat /data/data/com.jerboa/shared_prefs/user_reading_history.xml
```

### 问题 3: API 返回错误

**检查 Flask 日志：**
```python
# 添加更多日志到 app.py
print(f"Received history_contents: {len(history_contents)} items")
print(f"First item: {history_contents[0][:100]}")
```

---

## ✅ 成功的标志

1. **Logcat 显示：**
   ```
   D/ForYou: ✓ Recorded: [post title]
   D/ForYou: History count: [增长的数字]
   D/ForYou: Sending N history items to API
   ```

2. **Flask 日志显示：**
   ```
   POST /api/recommend HTTP/1.1" 200
   Received history_contents: N items
   ```

3. **UI 显示：**
   ```
   Algorithm: TF-IDF • Based on your history
   ```

4. **推荐内容：**
   - 与您点击的帖子主题相关
   - 越用越精准

---

## 📊 数据流图

```
┌─────────────┐
│   用户点击   │
│    帖子      │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────┐
│  onPostViewed(title, text)  │
│  ForYouViewModel            │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│   addToHistory()            │
│   UserHistoryRepository     │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│   SharedPreferences         │
│   本地存储（持久化）         │
└─────────────────────────────┘

用户切换回 For You Tab
       │
       ▼
┌─────────────────────────────┐
│  loadRecommendations()      │
│  ForYouViewModel            │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  getHistoryForRecommendation│
│  UserHistoryRepository      │
└──────┬──────────────────────┘
       │ (返回最近10条)
       ▼
┌─────────────────────────────┐
│  POST /api/recommend        │
│  RecommendationClient       │
│  { history_contents: [...] }│
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  Flask API                  │
│  recommend_from_history()   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  TF-IDF 向量化 + 相似度计算  │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  返回个性化推荐列表          │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  显示在 For You 页面         │
│  "Based on your history"    │
└─────────────────────────────┘
```

---

**现在整个闭环已完成！重新编译 App 并测试！** 🎉
