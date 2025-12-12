# Firebase Setup Guide

## 配置步骤

### 1. 创建 Firebase 项目

1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 点击 "添加项目" / "Add Project"
3. 输入项目名称：`Jerboa-DSAA2044`
4. 选择是否启用 Google Analytics（建议启用）
5. 等待项目创建完成

### 2. 添加 Android 应用

1. 在 Firebase 项目主页，点击 Android 图标
2. 填写应用信息：
   - **包名**：`com.jerboa`
   - **应用昵称**：Jerboa
   - **调试签名证书 SHA-1**（可选）：运行以下命令获取
     ```bash
     keytool -list -v -alias androiddebugkey \
       -keystore ~/.android/debug.keystore \
       -storepass android -keypass android
     ```
3. 点击 "注册应用"

### 3. 下载配置文件

1. 下载 `google-services.json` 文件
2. 将文件放置到项目路径：
   ```
   F:/Projects/DSAA2044-Jerboa/app/google-services.json
   ```
3. **重要**：确保 `.gitignore` 已包含此文件（避免泄露密钥）

### 4. 启用 Firebase 服务

在 Firebase Console 中启用以下服务：

#### Analytics (必需)
- 默认已启用
- 用于收集用户行为数据

#### Firestore (可选)
1. 转到 "Firestore Database"
2. 点击 "创建数据库"
3. 选择位置：`asia-east2 (香港)` 或 `asia-northeast1 (东京)`
4. 安全规则选择：**测试模式**（仅用于开发）
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.time < timestamp.date(2025, 12, 31);
       }
     }
   }
   ```

### 5. 验证集成

运行应用并检查 Logcat：

```bash
adb logcat | grep -E "FirebaseAnalytics|FA"
```

预期输出：
```
FA      : App measurement initialized, version: xxxxx
FA      : To enable debug logging run: adb shell setprop log.tag.FA VERBOSE
FA      : Event posted: post_view
```

### 6. 查看数据

1. 等待 24 小时（Analytics 数据有延迟）
2. 访问 Firebase Console → Analytics → Events
3. 查看自定义事件：
   - `post_view`
   - `post_interaction`
   - `for_you_tab_view`
   - `recommendation_request`

## 数据收集说明

### 事件类型

| 事件名称 | 参数 | 用途 |
|---------|------|------|
| `post_view` | post_id, post_title, community, content_length, view_source | 记录用户查看的帖子 |
| `post_interaction` | post_id, action_type, source | 记录用户互动（upvote, save, comment） |
| `for_you_tab_view` | timestamp | 记录 For You 页面访问 |
| `recommendation_request` | history_size, candidate_count, response_time_ms | 记录推荐 API 性能 |

### 隐私说明

- 仅收集匿名行为数据
- 不收集个人身份信息 (PII)
- 用户可在设置中关闭数据收集
- 符合 GDPR / CCPA 要求

## 调试模式

启用实时调试：

```bash
# 启用调试模式
adb shell setprop debug.firebase.analytics.app com.jerboa

# 查看实时事件
adb logcat -s FA
```

## 常见问题

### Q: google-services.json 文件在哪里？
A: 下载后放在 `app/` 目录下，与 `build.gradle.kts` 同级。

### Q: 编译报错 "google-services.json is missing"？
A: 确保已从 Firebase Console 下载配置文件并放到正确位置。

### Q: 看不到 Analytics 数据？
A: Analytics 有 24 小时延迟，使用 DebugView 查看实时数据。

### Q: 如何禁用 Analytics（测试用）？
A: 在代码中调用：
```kotlin
FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false)
```

## Release 3 要求对应

✅ **用户数据收集**：Firebase Analytics 收集用户行为
✅ **行为日志**：post_view, post_interaction 等事件
✅ **云端存储**：Firebase Firestore（可选）
✅ **预测管道**：用户历史 → 后端模型 → 个性化推荐

---

**配置完成后，请删除此文件或将其移至 `documents/` 目录。**
